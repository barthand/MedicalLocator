package put.medicallocator.ui.animation;

/**
 * Simple interface for animation controllers supporting in/out animation.
 */
public interface InOutAnimationController {

    /**
     * Starts the in animation.
     */
    void animateIn();

    /**
     * Starts the out animation.
     */
    void animateOut();
}
