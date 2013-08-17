package put.medicallocator.ui.overlay;

import android.graphics.*;
import android.graphics.drawable.Drawable;
import com.google.android.maps.*;
import put.medicallocator.io.model.Facility;
import put.medicallocator.io.route.model.RouteSpec;
import put.medicallocator.ui.model.RouteInformation;
import put.medicallocator.utils.ArrayUtils;
import put.medicallocator.utils.MyLog;

import java.util.ArrayList;

/**
 * {@link Overlay} implementation for drawing route purpose on the {@link MapView}.
 * Uses {@link Path} to draw smoothly the route.
 *
 * <p>Path is built only when necessary (for the first time
 * and when zoom changes). Since the application is GPU accelerated,
 * Path is being drawn on the {@link Bitmap} first (which is not accelerated).
 * GPU acceleration couldn't be used for path drawing, because in that mode path is being drawn as a texture
 * and devices have some limitations about the texture size (f.e. 2048x2048). Because of the fact that path
 * may be simply much larger than this, it is drawn using the CPU on the Bitmap {@link Canvas}.</p>
 *
 * <p>Bitmap is of the {@link MapView} size. To draw proper part of the Path on the bitmap, there are some translations
 * on-going calculated based on the initial position of some points from the Route.
 * This is also used to skip redrawing path every time {@link MapView} is moved.</p>
 */
public class RouteOverlay extends Overlay {

    private static final String TAG = RouteOverlay.class.getSimpleName();

    /** Defines the width of the path inner part. */
    private static final int INNER_LINE_WIDTH = 4;

    /** Defines the lat/lng span (multipled by 10^6) to be included additionally when route is displayed. */
    private static final int SPAN_OF_TOLERANCE_E6 = 25000;

    /** Used to define path inner part look. */
    private final Paint innerPaint = new Paint();

    /** Used to define path outer part (stroking) look. */
    private final Paint outerPaint = new Paint();

    /** Collects all the points creating the actual route. */
    private final ArrayList<GeoPoint> points = new ArrayList<GeoPoint>();

    /** Cached path instance used to draw the route. */
    private final Path path = new Path();

    /** {@link Point} instance used to either build path or to calculate translations. */
    private final Point point = new Point();

    /** {@link Bitmap} where the path is being drawn. */
    private final Bitmap bitmap;

    /** {@link Canvas} to facilitate drawing on the {@link Bitmap} (using CPU). */
    private final Canvas bitmapCanvas;

    /** {@link Facility} being a target of the route. It's here to always draw the route's target point. */
    private final Facility targetFacility;

    /** Shared instance of {@link FacilityTypeDrawableCache} used to draw the {@link #targetFacility}. */
    private final FacilityTypeDrawableCache drawableCache;

    /** Last span of the longitude. Used to detect whether zoom changed. */
    private int lastLongitudeSpan = Integer.MIN_VALUE;

    /** Reference {@link Point} used to detect offset between the initial route position and its current position. */
    private Point firstDrawnReferencePoint = new Point();

    /** {@link Point} storing the offset itself. */
    private Point offset = new Point();

    /**
     * Constructor. Creates internal structures and caches everything what possible to avoid creating any objects
     * in drawing itself.
     */
    public RouteOverlay(RouteInformation routeInfo, FacilityTypeDrawableCache drawableCache, MapView mv) {
        this.bitmap = Bitmap.createBitmap(mv.getWidth(), mv.getHeight(), Bitmap.Config.ARGB_8888);
        this.bitmapCanvas = new Canvas(bitmap);
        this.targetFacility = routeInfo.getTargetFacility();
        this.drawableCache = drawableCache;

        int minLat = Integer.MAX_VALUE, minLng = Integer.MAX_VALUE, maxLat = Integer.MIN_VALUE, maxLng = Integer.MIN_VALUE;

        final RouteSpec route = routeInfo.getRouteSpec();
        if (ArrayUtils.isNotEmpty(route.getPoints())) {
            for (int i = 0; i < route.getPoints().length; i++) {
                points.add(new GeoPoint(
                        (int) (route.getPoints()[i].getLatitude() * 1E6),
                        (int) (route.getPoints()[i].getLongitude() * 1E6)));

                final int currentLatitude = points.get(i).getLatitudeE6();
                final int currentLongitude = points.get(i).getLongitudeE6();

                minLat = minLat > currentLatitude ? currentLatitude : minLat;
                minLng = minLng > currentLongitude ? currentLongitude : minLng;
                maxLat = maxLat < currentLatitude ? currentLatitude : maxLat;
                maxLng = maxLng < currentLongitude ? currentLongitude : maxLng;
            }

            final int latSpan = maxLat - minLat;
            final int lngSpan = maxLng - minLng;

            final int moveToLat = (minLat + latSpan / 2);
            final int moveToLong = (minLng + lngSpan / 2);

            GeoPoint moveTo = new GeoPoint(moveToLat, moveToLong);

            moveAndAnimateBySpan(mv, latSpan, lngSpan, moveTo);

            initPaints();
        }

        MyLog.d(TAG, "Initializion done");
    }

    @Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
        super.draw(canvas, mapView, shadow);

        if (shadow) {
            return;
        }

        buildPath(mapView);
        drawPathsUsingTranslate();
        drawTargetOverlayOnBitmap(mapView);

        canvas.drawBitmap(bitmap, 0f, 0f, null);
    }

    private void drawPathsUsingTranslate() {
        bitmapCanvas.save();
        bitmapCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        bitmapCanvas.translate(offset.x, offset.y);
        bitmapCanvas.drawPath(path, outerPaint);
        bitmapCanvas.drawPath(path, innerPaint);
        bitmapCanvas.restore();
    }

    /**
     * Builds the route {@link Path}, if required (if zoom changed or if path is built for the first time).
     * Otherwise, calculates the offset to be used to draw previously created {@link Path}.
     */
    public Path buildPath(MapView mapView) {
        final Projection projection = mapView.getProjection();
        if (points.size() > 1) {
            projection.toPixels(points.get(0), point);
            final int currentLongitudeSpan = projection.fromPixels(0, mapView.getHeight() / 2).getLongitudeE6() - projection.fromPixels(mapView.getWidth(), mapView.getHeight() / 2).getLongitudeE6();
            if (currentLongitudeSpan == lastLongitudeSpan) {
                // For some reason, offsetting the path doesn't work on the HTC One X, 4.1.1. It's GPU accelerated,
                // but Nexus 7 (also accelerated) works just fine. For this reason, Canvas is translated.
                // path.offset(point.x - lastDrawnReferencePoint.x, point.y - lastDrawnReferencePoint.y);
                offset.set(point.x - firstDrawnReferencePoint.x, point.y - firstDrawnReferencePoint.y);
            } else {
                offset.set(0, 0);
                path.rewind();
                path.moveTo(point.x, point.y);
                firstDrawnReferencePoint.set(point.x, point.y);

                for (int i = 1; i < points.size(); i++) {
                    projection.toPixels(points.get(i), point);
                    path.lineTo(point.x, point.y);
                }
            }
            lastLongitudeSpan = currentLongitudeSpan;
        }

        return path;
    }

    private void drawTargetOverlayOnBitmap(MapView mapView) {
        mapView.getProjection().toPixels(targetFacility.getLocation(), point);

        final DrawableContext drawableContext = drawableCache.get(targetFacility.getFacilityType());
        final Drawable drawable = drawableContext.drawable;
        drawable.setBounds(point.x - drawableContext.halfWidth, point.y - drawableContext.halfHeight,
                point.x + drawableContext.halfWidth, point.y + drawableContext.halfHeight);
        drawable.draw(bitmapCanvas);
    }

    private void moveAndAnimateBySpan(MapView mv, int latSpan, int lngSpan, GeoPoint moveTo) {
        final MapController mapController = mv.getController();
        mapController.animateTo(moveTo);
        mapController.zoomToSpan(latSpan + SPAN_OF_TOLERANCE_E6, lngSpan + SPAN_OF_TOLERANCE_E6);
    }

    private void initPaints() {
        innerPaint.setColor(0x28D7FF);
        innerPaint.setStyle(Paint.Style.STROKE);
        innerPaint.setStrokeWidth(INNER_LINE_WIDTH);
        innerPaint.setAntiAlias(false);
        innerPaint.setAlpha(0x80);

        outerPaint.setColor(0x1E9AFF);
        outerPaint.setStyle(Paint.Style.STROKE);
        outerPaint.setStrokeWidth(INNER_LINE_WIDTH * 2);
        outerPaint.setAntiAlias(true);
        outerPaint.setAlpha(0xAB);
    }

}
