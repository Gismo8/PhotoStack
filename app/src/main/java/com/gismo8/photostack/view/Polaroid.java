package com.gismo8.photostack.view;

import android.animation.Animator;
import android.content.Context;
import android.graphics.PointF;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;

import com.gismo8.photostack.R;
import com.gismo8.photostack.interfaces.PolaroidAnimationListener;

import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.supercharge.shimmerlayout.ShimmerLayout;

public class Polaroid extends CardView implements View.OnTouchListener {

    private static final float ROTATION_NEGATIVE_MAX = -12;
    private static final float ROTATION_MAX = 12;
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM_ROTATE = 2;
    private static final long MOVE_TO_BACK_ANIMATION_DURATION = 600;
    private static final long MOVE_TO_BACK_ANIMATION_DURATION_FAST = 600;

    private int mode = NONE;
    private final PointF mid = new PointF();
    private float oldDist = 1f;
    private float oldRot = 0f;
    private float newRot = 0f;
    private float initialX;
    private float initialY;
    private float viewInitialPositionY;
    private float viewInitialPositionX;
    private float viewInitialRotation;
    private float viewInitialScaleX;
    private float viewInitialScaleY;
    private final float sendBackFloatValue = -100f;

    private boolean isTopPhotoInStack = false;
    private PolaroidAnimationListener animationListener;
    private int photoResourceId = 0;

    @BindView(R.id.polaroidFrame)
    protected CardView polaroidFrame;
    @BindView(R.id.photo)
    protected ImageView photo;
    @BindView(R.id.shimmerLayout)
    protected ShimmerLayout shimmerLayout;

    public Polaroid(@NonNull Context context) {
        super(context);
        initLayout(context);
    }

    public Polaroid(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initLayout(context);
    }

    public Polaroid(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initLayout(context);
    }

    public Polaroid(Context context, int photoResourceId, PolaroidAnimationListener animationListener) {
        super(context);
        this.photoResourceId = photoResourceId;
        this.animationListener = animationListener;
        initLayout(context);
    }

    private void initLayout(Context context) {
        LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        inflater.inflate(R.layout.view_polaroid, this);
        ButterKnife.bind(this);

        float generatedRandomFloat = randomFloat();
        setRotation(generatedRandomFloat);
        float squareRandomFloat = generatedRandomFloat * generatedRandomFloat;
        setTranslationX(squareRandomFloat);
        setTranslationY(squareRandomFloat);
        setPhoto(photoResourceId);
        setOnTouchListener(this);
    }

    private float randomFloat() {
        Random rand = new Random();
        return rand.nextFloat() * (Polaroid.ROTATION_MAX - Polaroid.ROTATION_NEGATIVE_MAX) + Polaroid.ROTATION_NEGATIVE_MAX;
    }

    private void setPhoto(int imageResourceID) {
        if (imageResourceID != 0) {
            photo.setImageDrawable(getContext().getResources().getDrawable(imageResourceID));
            photo.requestLayout();
        }
    }

    public void setTopPhotoInStack(boolean topPhotoInStack) {
        isTopPhotoInStack = topPhotoInStack;
    }

    /**
     * Polaroid is prepared for loading pictures from network with the ShimmerLayout
     */


    public void startShimming() {
        shimmerLayout.startShimmerAnimation();
    }

    public void stopShimming() {
        shimmerLayout.stopShimmerAnimation();
    }

    /**
     * Handle all kind of touches and multitouches here. When user lifts all fingers Polaroid
     * animates back to the bottom of the stack.
     */

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public boolean onTouch(final View v, MotionEvent event) {
        if (!isTopPhotoInStack) {
            return false;
        }
        // handle touch events here if polaroid is on top of stack
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mode = DRAG;
                initialX = event.getRawX();
                initialY = event.getRawY();
                viewInitialPositionY = v.getTranslationY();
                viewInitialPositionX = v.getTranslationX();
                viewInitialScaleX = v.getScaleX();
                viewInitialScaleY = v.getScaleY();
                viewInitialRotation = v.getRotation();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                if (oldDist > 5f) {
                    midPoint(mid, event);
                    mode = ZOOM_ROTATE;
                }
                oldRot = rotation(event);
                break;
            case MotionEvent.ACTION_UP:
                mode = NONE;
                float animateLeftOrRight = event.getRawX() - initialX > 0.0f ? v.getWidth() * 0.5f : -(v.getWidth() * 0.5f);
                long movePolaroidAwayAnimationTime = event.getRawY() - initialY > 0.0f ? MOVE_TO_BACK_ANIMATION_DURATION : MOVE_TO_BACK_ANIMATION_DURATION_FAST;
                v.animate()
                        .translationX(viewInitialPositionX + (animateLeftOrRight))
                        .translationY(viewInitialPositionY - (v.getHeight() * 1.2f))
                        .rotation(viewInitialRotation)
                        .scaleX(viewInitialScaleX)
                        .scaleY(viewInitialScaleY)
                        .setListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animator) {
                                if (animationListener != null) {
                                    animationListener.animationStarted();
                                }
                            }

                            @Override
                            public void onAnimationEnd(Animator animator) {
                                v.setTranslationZ(sendBackFloatValue);
                                v.animate()
                                        .translationX(viewInitialPositionX)
                                        .translationY(viewInitialPositionY)
                                        .setDuration(MOVE_TO_BACK_ANIMATION_DURATION)
                                        .setInterpolator(new AccelerateDecelerateInterpolator())
                                        .setListener(new Animator.AnimatorListener() {
                                            @Override
                                            public void onAnimationStart(Animator animator) {

                                            }

                                            @Override
                                            public void onAnimationEnd(Animator animator) {
                                                v.setTranslationZ(0);
                                                if (animationListener != null) {
                                                    animationListener.animationEnded();
                                                }
                                            }

                                            @Override
                                            public void onAnimationCancel(Animator animator) {

                                            }

                                            @Override
                                            public void onAnimationRepeat(Animator animator) {

                                            }
                                        })
                                        .start();
                            }

                            @Override
                            public void onAnimationCancel(Animator animator) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animator) {

                            }
                        })
                        .setDuration(movePolaroidAwayAnimationTime).setInterpolator(new AccelerateDecelerateInterpolator()).start();
                break;
            case MotionEvent.ACTION_POINTER_UP:
                mode = DRAG;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == ZOOM_ROTATE) {
                    if (event.getPointerCount() == 2 || event.getPointerCount() == 3) {
                        newRot = rotation(event);
                        float r = newRot - oldRot;
                        v.setRotation(v.getRotation() + r);
                    }
                    float newDist = spacing(event);
                    if (newDist > 10f) {
                        float scale = (newDist / oldDist);
                        float maxScale = v.getScaleX() * scale > 4f ? 4f : v.getScaleX() * scale;
                        v.setScaleX(maxScale);
                        v.setScaleY(maxScale);
                    }
                }
                float dx = event.getRawX() - initialX;
                float dy = event.getRawY() - initialY;
                v.setTranslationX(dx);
                v.setTranslationY(dy);
                break;
        }
        return true;
    }

    /**
     * Determine the space between the first two fingers
     */
    private float spacing(MotionEvent event) {
        if (event.getPointerCount() > 1) {
            float x = event.getX(0) - event.getX(1);
            float y = event.getY(0) - event.getY(1);
            float s = x * x + y * y;
            return (float) Math.sqrt(s);
        } else
            return 1f;
    }

    /**
     * Calculate the mid point of the first two fingers
     */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    /**
     * Calculate the degree to be rotated by.
     */
    private float rotation(MotionEvent event) {
        double delta_x = (event.getX(0) - event.getX(1));
        double delta_y = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }

}
