package com.trio.dronetest;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.trio.drone.R;
import com.trio.drone.core.VrMenuActivity;

/**
 * Created by orhun on 6/3/2018.
 */

public class VRIntroActivity extends AppIntro {
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Instead of fragments, you can also use our default slide
        // Just set a title, description, background and image. AppIntro will do the rest.
        addSlide(AppIntroFragment.newInstance("Wifi Connection", "Connect your smartphone to Drone by Wifi", R.drawable.wi_fi_image, Color.parseColor("#0277BD")));
        addSlide(AppIntroFragment.newInstance("Cardboard", "Prepare your cardboard ", R.drawable.cardboard_image, Color.parseColor("#0288D1")));
        addSlide(AppIntroFragment.newInstance("TakeOff", "Drone can be takeoff after tutorial", R.drawable.drone_bebop, Color.parseColor("#039BE5")));
        addSlide(AppIntroFragment.newInstance("Place Drone", "After takeoff place your drone to cardboard and FLY with VR controller", R.drawable.screen_image, Color.parseColor("#03A9F4")));

        // OPTIONAL METHODS
        // Override bar/separator color.
        setBarColor(Color.parseColor("#3F51B5"));
        setSeparatorColor(Color.parseColor("#2196F3"));

        // Hide Skip/Done button.
        //showSkipButton(false);
        //setProgressButtonEnabled(false);

        // Turn vibration on and set intensity.
        // NOTE: you will probably need to ask VIBRATE permission in Manifest.
        //setVibrate(true);
        //setVibrateIntensity(30);
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        // Do something when users tap on Skip button.
        Intent intent = new Intent(this, VrMenuActivity.class);
        startActivity(intent);
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        // Do something when users tap on Done button.
        Intent intent = new Intent(this, VrMenuActivity.class);
        startActivity(intent);
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
        // Do something when the slide changes.
    }
}