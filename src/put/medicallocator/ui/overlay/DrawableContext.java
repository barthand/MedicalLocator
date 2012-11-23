package put.medicallocator.ui.overlay;

import android.graphics.drawable.Drawable;

public class DrawableContext {
    final Drawable drawable;
    final int halfWidth;
    final int halfHeight;

    public DrawableContext(Drawable drawable) {
        this.drawable = drawable;
        this.halfWidth = drawable.getIntrinsicWidth() / 2;
        this.halfHeight = drawable.getIntrinsicHeight() / 2;
    }

    public Drawable getDrawable() {
        return drawable;
    }

    public int getHalfWidth() {
        return halfWidth;
    }

    public int getHalfHeight() {
        return halfHeight;
    }
    
}