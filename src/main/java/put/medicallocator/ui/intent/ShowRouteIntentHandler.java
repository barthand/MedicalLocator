package put.medicallocator.ui.intent;

import android.content.Intent;
import put.medicallocator.io.IFacilityDAO;
import put.medicallocator.io.model.Facility;
import put.medicallocator.io.route.model.RouteSpec;
import put.medicallocator.ui.misc.RouteOverlayManager;
import put.medicallocator.ui.model.RouteInformation;

public class ShowRouteIntentHandler implements IntentHandler {

    /**
     * Indicates the action name for the intent produced by the {@link ShowRouteIntentHandler}.
     */
    public static final String INTENT_ACTION = "SHOW_ROUTE";

    /**
     * An identifier for the {@link RouteSpec} object, associated with the {@link Intent}.
     */
    private static final String ROUTE_SPEC_SERIALIZABLE = "EXTRA_ROUTE_SPEC";

    /**
     * An identifier for the target facility ID, associated with the {@link Intent}.
     */
    private static final String TARGET_FACILITY_ID = "EXTRA_TARGET_FACILITY_ID";

    private final RouteOverlayManager routeOverlayManager;
    private final IFacilityDAO facilityDAO;

    public ShowRouteIntentHandler(RouteOverlayManager routeOverlayManager, IFacilityDAO facilityDAO) {
        this.routeOverlayManager = routeOverlayManager;
        this.facilityDAO = facilityDAO;
    }

    @Override
    public void process(Intent intent) {
        final RouteSpec routeSpec = (RouteSpec) intent.getSerializableExtra(ROUTE_SPEC_SERIALIZABLE);
        final Facility targetFacility = facilityDAO.findById(intent.getLongExtra(TARGET_FACILITY_ID, -1));
        routeOverlayManager.showRoute(new RouteInformation(routeSpec, targetFacility));
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
    public static void setupIntent(Intent intent, RouteSpec routeSpec, Long targetFacilityId) {
        intent.setAction(INTENT_ACTION);
        intent.putExtra(ROUTE_SPEC_SERIALIZABLE, routeSpec);
        intent.putExtra(TARGET_FACILITY_ID, targetFacilityId);
    }

}
