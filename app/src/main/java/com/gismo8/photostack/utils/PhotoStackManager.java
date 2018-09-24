package com.gismo8.photostack.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.widget.RelativeLayout;

import com.gismo8.photostack.R;
import com.gismo8.photostack.interfaces.PolaroidAnimationListener;
import com.gismo8.photostack.view.Polaroid;

import java.util.Stack;

public class PhotoStackManager implements PolaroidAnimationListener {

    /**
     * PhotoStack Manager can be added to any Activity with a Relative Layout, it  controls the
     * Polaroids' Touch listeners.
     * Users are not allowed to move another card until the last touched card is
     * moved to the bottom of the Stack
     */


    private static Stack<Polaroid> polaroidStack;
    private final Context context;
    private final RelativeLayout layout;


    public PhotoStackManager(Context context, RelativeLayout layout) {
        this.layout = layout;
        this.context = context;
        polaroidStack = new Stack<>();
        fillStackWithDrawablePhotos();
    }

    private void fillStackWithDrawablePhotos() {
        addPolaroid(R.drawable.utrecht1);
        addPolaroid(R.drawable.utrecht2);
        addPolaroid(R.drawable.utrecht3);
        addPolaroid(R.drawable.utrecht4);
        addPolaroid(R.drawable.utrecht5);
        addPolaroid(R.drawable.utrecht6);
        addPolaroid(R.drawable.utrecht7);
        addPolaroid(R.drawable.utrecht8);
        addPolaroid(R.drawable.utrecht9);
        addPolaroid(R.drawable.utrecht10);
    }

    public void addPhotoStackToLayout() {
        for (final Polaroid polaroid : polaroidStack) {
            layout.addView(polaroid);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) polaroid.getLayoutParams();
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            polaroid.setLayoutParams(layoutParams);
        }
        polaroidStack.peek().setTopPhotoInStack(true);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void addPolaroid(int resourceId) {
        Polaroid newPolaroid = new Polaroid(context, resourceId, this);
        polaroidStack.add(newPolaroid);
        newPolaroid.setElevation(context.getResources().getDimension(R.dimen.cardview_default_elevation) * polaroidStack.size());
    }

    @Override
    public void animationStarted() {
        polaroidStack.peek().setTopPhotoInStack(false);
        movePolaroidToBottom(polaroidStack.pop());
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void animationEnded() {
        resetElevations();
        polaroidStack.peek().setTopPhotoInStack(true);
    }

    private void movePolaroidToBottom(Polaroid polaroid) {
        polaroidStack.add(0, polaroid);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void resetElevations() {
        for (int i = 0; i < polaroidStack.size(); i++) {
            polaroidStack.get(i).setElevation(context.getResources().getDimension(R.dimen.cardview_default_elevation) * (i));
        }
    }
}
