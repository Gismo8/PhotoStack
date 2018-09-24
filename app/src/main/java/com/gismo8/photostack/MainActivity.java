package com.gismo8.photostack;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.RelativeLayout;

import com.gismo8.photostack.utils.PhotoStackManager;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    /**
     * To run the app just hit "Run". The app will automatically build a stack of photos,
     * and you'll be able to interact with the top photo
     */

    private PhotoStackManager photoStackManager;

    @BindView(R.id.root)
    protected RelativeLayout root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        photoStackManager = new PhotoStackManager(this, root);
        photoStackManager.addPhotoStackToLayout();
    }
}
