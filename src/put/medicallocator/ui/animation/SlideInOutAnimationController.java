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

    private final Animator.AnimatorListener animationEndListener;

    public SlideInOutAnimationController(View view) {
        this.view = view;
        animator = ViewPropertyAnimator.animate(view);

        animationEndListener = new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                ViewHelper.setTranslationX(SlideInOutAnimationController.this.view, 0);
                SlideInOutAnimationController.this.view.setVisibility(View.GONE);
            }
        };
    }

    @Override
    public void animateIn() {
        view.setVisibility(View.VISIBLE);
        ViewHelper.setAlpha(view, 0f);
        ViewHelper.setTranslationX(view, -view.getWidth());
        animator.setListener(null).alpha(1f).translationX(0).start();
    }

    public void animateOut() {
        animator.setListener(animationEndListener).alpha(0f).translationX(view.getWidth()).start();
    }
}