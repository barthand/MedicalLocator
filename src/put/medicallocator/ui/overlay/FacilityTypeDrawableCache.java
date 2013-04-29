package put.medicallocator.ui.overlay;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import put.medicallocator.io.model.FacilityType;

import java.util.WeakHashMap;

/**
 * Simple cache of {@link Drawable}s used for particular {@link FacilityType}s.
 * <p>It's <b>not thread-safe</b>, its methods should be invoked only from the UI thread.</p>
 */
public class FacilityTypeDrawableCache {

    private final Context context;
    private final WeakHashMap<FacilityType, DrawableContext> cache;
    
    public FacilityTypeDrawableCache(Context context) {
        super();
        this.context = context;
        this.cache = new WeakHashMap<FacilityType, DrawableContext>();
    }

    /**
     * Returns the {@link DrawableContext} associated with particular {@link FacilityType}.
     * If it is not present, it is created and put into the cache.
     */
    public DrawableContext get(FacilityType type) {
        if (!cache.containsKey(type)) {
            final Resources resources = context.getResources();  
            final Drawable drawable = resources.getDrawable(type.getDrawableId());
            final DrawableContext drawableContext = new DrawableContext(drawable);
            cache.put(type, drawableContext);
        }
        return cache.get(type);
    }
    
}
