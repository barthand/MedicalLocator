package put.medicallocator.ui.overlay;

import java.util.ArrayList;

import put.medicallocator.io.route.Route;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class RouteOverlay extends Overlay {
	Route route;
	ArrayList<GeoPoint> points;

	public RouteOverlay(Route route, MapView mv) {
		this.route = route;
		if (route.route.length > 0) {
			points = new ArrayList<GeoPoint>();
			for (int i = 0; i < route.route.length; i++) {
				points.add(new GeoPoint((int) (route.route[i][1] * 1000000),
						(int) (route.route[i][0] * 1000000)));
			}
			int moveToLat = (points.get(0).getLatitudeE6() + (points.get(
					points.size() - 1).getLatitudeE6() - points.get(0)
					.getLatitudeE6()) / 2);
			int moveToLong = (points.get(0).getLongitudeE6() + (points.get(
					points.size() - 1).getLongitudeE6() - points.get(0)
					.getLongitudeE6()) / 2);
			GeoPoint moveTo = new GeoPoint(moveToLat, moveToLong);

			MapController mapController = mv.getController();
			mapController.animateTo(moveTo);
		}
	}

	@Override
	public boolean draw(Canvas canvas, MapView mv, boolean shadow, long when) {
		super.draw(canvas, mv, shadow);
		drawPath(mv, canvas);
		return true;
	}

	public void drawPath(MapView mv, Canvas canvas) {
		int x1 = -1, y1 = -1, x2 = -1, y2 = -1;
		Paint paint = new Paint();
		paint.setColor(Color.GREEN);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(3);
		paint.setAlpha(120);
		for (int i = 0; i < points.size(); i++) {
			Point point = new Point();
			mv.getProjection().toPixels(points.get(i), point);
			x2 = point.x;
			y2 = point.y;
			if (i > 0) {
				canvas.drawLine(x1, y1, x2, y2, paint);
			}
			x1 = x2;
			y1 = y2;
		}
	}
}
