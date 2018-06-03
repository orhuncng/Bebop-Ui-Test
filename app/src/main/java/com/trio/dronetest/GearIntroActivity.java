package com.trio.dronetest;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.trio.drone.R;

/**
 * Created by orhun on 6/3/2018.
 */

public class GearIntroActivity extends AppIntro {
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Instead of fragments, you can also use our default slide
        // Just set a title, description, background and image. AppIntro will do the rest.
        addSlide(AppIntroFragment.newInstance("Connection", "Connect your smartwatch to android Phone by Bluetooth", R.drawable.bluetooth_image, Color.parseColor("#A1887F")));
        addSlide(AppIntroFragment.newInstance("Open App", "Open your smartwatch app ", R.drawable.android_logo, Color.parseColor("#e53935")));
        addSlide(AppIntroFragment.newInstance("Set your Arm", "Wear your smartwatch and adjust your arm ", R.drawable.watch_image, Color.parseColor("#B39DDB")));
        addSlide(AppIntroFragment.newInstance("Takeoff", "You are ready to FLY ", R.drawable.watch_image, Color.parseColor("#FF9800")));

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
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        // Do something when users tap on Done button.
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
        // Do something when the slide changes.
    }
}