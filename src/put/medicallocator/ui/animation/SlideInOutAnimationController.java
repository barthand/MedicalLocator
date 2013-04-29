package put.medicallocator.ui.animation;

import android.view.View;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

/**
 * Simple {@link InOutAnimationController} responsible for animating provided {@link View} using the fade animation.
 */
public class SlideInOutAnimationController implements InOutAnimationController {

    private final View view;
    private final ViewPropertyAnimator animator;

    public SlideInOutAnimationController(View view) {
        this.view = view;
        animator = ViewPropertyAnimator.animate(view);
    }

    @Override
    public void animateIn() {
        view.setVisibility(View.VISIBLE);
        ViewHelper.setAlpha(view, 0f);
        ViewHelper.setTranslationX(view, -view.getWidth());
        animator.alpha(1f).translationX(0).setListener(null).start();
    }

    public void animateOut() {
        animator.alpha(0f).translationX(view.getWidth()).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.GONE);
                ViewHelper.setTranslationX(view, 0);
                ViewHelper.setAlpha(view, 1f);
            }
        }).start();
    }
}