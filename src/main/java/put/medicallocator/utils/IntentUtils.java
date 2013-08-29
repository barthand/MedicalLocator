package put.medicallocator.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

public final class IntentUtils {

    private IntentUtils() {
    }

    /**
     * Checks whether there is an {@link Activity} available in the system which may support
     * the provided {@code intent}.
     */
    public static boolean isIntentAvailable(Context context, Intent intent) {
        final PackageManager packageManager = context.getPackageManager();
        if (packageManager != null) {
            return CollectionUtils.isNotEmpty(packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY));
        }
        return false;
    }

}
