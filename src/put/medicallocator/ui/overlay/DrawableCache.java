package put.medicallocator.ui.overlay;

import java.util.HashMap;
import java.util.Map;

import put.medicallocator.io.model.FacilityType;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

public class DrawableCache {

    private final Context context;
    private final Map<FacilityType, DrawableContext> cache;
    
    public DrawableCache(Context context) {
        super();
        this.context = context;
        this.cache = new HashMap<FacilityType, DrawableContext>();
    }

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
