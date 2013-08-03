package put.medicallocator.ui.intent;

import android.content.Intent;
import put.medicallocator.io.route.model.RouteSpec;
import put.medicallocator.ui.misc.RouteOverlayManager;

public class ShowRouteIntentHandler implements IntentHandler {

    /**
     * Indicates the action name for the intent produced by the {@link ShowRouteIntentHandler}.
     */
    public static final String INTENT_ACTION = "SHOW_ROUTE";

    /**
     * An identifier for the {@link RouteSpec} object, associated with the {@link Intent}.
     */
    private static final String ROUTE_SPEC_SERIALIZABLE = "EXTRA_ROUTE_SPEC";

    private final RouteOverlayManager routeOverlayManager;

    public ShowRouteIntentHandler(RouteOverlayManager routeOverlayManager) {
        this.routeOverlayManager = routeOverlayManager;
    }

    @Override
    public void process(Intent intent) {
        routeOverlayManager.showRoute((RouteSpec) intent.getSerializableExtra(ROUTE_SPEC_SERIALIZABLE));
    }

    @Override
    public boolean supports(Intent intent) {
        return INTENT_ACTION.equals(intent.getAction());
    }

    @Override
    public Object retainState() {
        return null;
    }

    @Override
    public void restoreState(Object object) {
        // Do nothing.
    }

    /**
     * Setups the provided {@link Intent} in a way that is going to be supported by {@link ShowRouteIntentHandler}.
     */
    public static void setupIntent(Intent intent, RouteSpec routeSpec) {
        intent.setAction(INTENT_ACTION);
        intent.putExtra(ROUTE_SPEC_SERIALIZABLE, routeSpec);
    }

}
