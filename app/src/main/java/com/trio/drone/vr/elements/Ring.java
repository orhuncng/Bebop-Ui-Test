package com.trio.drone.vr.elements;

import android.content.res.Resources;
import android.util.DisplayMetrics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.trio.drone.vr.SceneListener;
import com.trio.drone.vr.util.AnimationState;
import com.trio.drone.vr.util.GdxUtils;
import com.trio.drone.vr.util.LimitedData;

import java.util.Locale;

public class Ring implements SceneListener
{
    private static final float RING_BORDER_MARGIN_X = 300f;
    private static final float TEXT_MARGIN_X = 80f;
    private static final float TEXT_LABEL_MARGIN_Y = 40f;
    private static final float TEXT_MARGIN_Y = 18f;
    private static final float TEXT_OUTLIER_LABEL_MARGIN_Y = -9f;
    private static final float TEXT_OUTLIER_MARGIN_Y = -30f;

    private Sprite sprite;
    private Sprite outlierSprite;

    private LimitedData ringValue = new LimitedData();
    private LimitedData outlierValue = new LimitedData();

    private boolean leftAligned;

    private float centerY;
    private float textCenterX;

    private String unit;
    private String outlierUnit;

    private String label;
    private String outlierLabel;

    public Ring(String label, String unit, String outlierLabel,
                String outlinerUnit, boolean leftAligned)
    {
        this.leftAligned = leftAligned;
        this.label = label;
        this.outlierLabel = outlierLabel;
        this.unit = " " + unit;
        outlierUnit = " " + outlinerUnit;
    }

    public void setRingValue(float value) {
        ringValue.setValue(value);
    }

    public void setOutlierValue(float value) {
        outlierValue.setValue(value);
    }

    public void setRingValueLimit(float limit) {
        ringValue.setLimit(limit);
    }

    public void setOutlierValueLimit(float limit) {
        outlierValue.setLimit(limit);
    }

    @Override
    public void create(DisplayMetrics metrics, Resources res)
    {
        float centerX;

        if (leftAligned) {
            centerX = RING_BORDER_MARGIN_X;
            textCenterX = centerX - TEXT_MARGIN_X / 8f;
        }
        else {
            centerX = metrics.widthPixels - RING_BORDER_MARGIN_X;
            textCenterX = centerX - TEXT_MARGIN_X;
        }

        centerY = (metrics.heightPixels / 2f) - 150f;

        sprite = new Sprite(GdxUtils.get().createSprite("rings"));
        sprite.setCenter(centerX, centerY);
        sprite.setOriginCenter();

        outlierSprite = new Sprite(GdxUtils.get().createSprite("ring_outlier"));

        float outlierX = sprite.getWidth() / (leftAligned ? -2f : 2f);

        if (leftAligned) outlierSprite.setFlip(true, false);

        outlierSprite.setCenter(centerX - outlierX, centerY);
        outlierSprite.setOrigin(outlierX, outlierSprite.getHeight() / 2f);

        setRingValue(0f);
        setOutlierValue(0f);
    }

    @Override
    public void update()
    {
        sprite.setRotation(ringValue.getPerc() > 1f ? 360f : ringValue.getPerc() * 360f);

        float rotation;
        if (outlierValue.getPerc() > 1f) rotation = -90f;
        else if (outlierValue.getPerc() < -1f) rotation = 90f;
        else rotation = outlierValue.getPerc() * -90f;

        outlierSprite.setRotation(leftAligned ? -rotation : rotation);

        if (ringValue.inHecticAlertState()) {
            sprite.setScale(AnimationState.get().getHecticBackstreets());
            sprite.setColor(0.8f, 0.2f, 0f, AnimationState.get().getHecticBlink());
        } else if (ringValue.inAlertState()) {
            sprite.setScale(AnimationState.get().getBackstreets());
            sprite.setColor(0.7f, 0.3f, 0f, AnimationState.get().getBlink());
        }
        else {
            sprite.setScale(1f);
            sprite.setColor(Color.WHITE);
        }

        if (outlierValue.inHecticAlertState()) {
            outlierSprite.setScale(AnimationState.get().getHecticBackstreets());
            outlierSprite.setColor(0.8f, 0.2f, 0f, AnimationState.get().getHecticBlink());
        } else if (outlierValue.inAlertState()) {
            outlierSprite.setScale(AnimationState.get().getBackstreets());
            outlierSprite.setColor(0.7f, 0.3f, 0f, AnimationState.get().getBlink());
        }
        else {
            outlierSprite.setScale(1f);
            outlierSprite.setColor(Color.WHITE);
        }
    }

    @Override
    public void draw(SpriteBatch batch)
    {
        sprite.draw(batch);
        outlierSprite.draw(batch);

        BitmapFont font24 = GdxUtils.get().getFont24();
        BitmapFont font18 = GdxUtils.get().getFont18();

        if (ringValue.inHecticAlertState()) {
            font24.setColor(1f, 0.3f, 0f, 1f);
            font18.setColor(1f, 0.3f, 0f, 1f);
        } else if (ringValue.inAlertState()) {
            font24.setColor(.95f, 0.4f, 0f, 1f);
            font18.setColor(.95f, 0.4f, 0f, 1f);
        }

        font18.draw(batch, label, textCenterX, centerY + TEXT_LABEL_MARGIN_Y);
        font24.draw(batch, String.format(Locale.US, "%.1f", ringValue.getValue()) + unit,
                textCenterX, centerY + TEXT_MARGIN_Y);

        GdxUtils.get().resetFont24Color();
        GdxUtils.get().resetFont18Color();

        if (outlierValue.inHecticAlertState())
            font18.setColor(1f, 0.3f, 0f, 1f);
        else if (outlierValue.inAlertState())
            font18.setColor(.95f, 0.4f, 0f, 1f);

        font18.draw(batch, outlierLabel, textCenterX, centerY + TEXT_OUTLIER_LABEL_MARGIN_Y);
        font18.draw(batch, String.format(Locale.US, "%.1f", outlierValue.getValue()) + outlierUnit,
                textCenterX, centerY + TEXT_OUTLIER_MARGIN_Y);

        GdxUtils.get().resetFont18Color();
    }

    @Override
    public void shutdown() { }
}
