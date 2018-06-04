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

public class Ring implements SceneListener
{
    private static final float BLINK_LIMIT = 0.7f;
    private static final float HECTIC_BLINK_LIMIT = 0.9f;
    private static final float OUTLIER_BLINK_LIMIT = 0.4f;
    private static final float OUTLIER_HECTIC_BLINK_LIMIT = 0.6f;

    private static final float OUTLIER_MARGIN_X = 5f;
    private static final float RING_BORDER_MARGIN_X = 300f;

    private static final float TEXT_MARGIN_X = 80f;
    private static final float TEXT_LABEL_MARGIN_Y = 40f;
    private static final float TEXT_MARGIN_Y = 18f;
    private static final float TEXT_OUTLIER_LABEL_MARGIN_Y = -9f;
    private static final float TEXT_OUTLIER_MARGIN_Y = -30f;

    private Sprite sprite;
    private Sprite outlierSprite;

    private float value;
    private float outlierValue;
    private float maxValue;
    private float maxOutlierValue;
    private float valuePerc;
    private float outlierValuePerc;

    private boolean leftAligned;

    private float centerY;
    private float textCenterX;

    private String unit;
    private String outlierUnit;

    private String label;
    private String outlierLabel;

    public Ring(float maxValue, float maxOutlierValue, String label, String unit,
            String outlierLabel, String outlinerUnit, boolean leftAligned)
    {
        setMaxValue(maxValue);
        setMaxOutlierValue(maxOutlierValue);
        this.leftAligned = leftAligned;
        this.label = label;
        this.outlierLabel = outlierLabel;
        this.unit = " " + unit;
        outlierUnit = " " + outlinerUnit;
    }

    public void setValue(float value)
    {
        this.value = value;
        setValuePerc();
    }

    public void setMaxValue(float value)
    {
        maxValue = value;
        setValuePerc();
    }

    public void setOutlierValue(float value)
    {
        outlierValue = value;
        setOutlierValuePerc();
    }

    public void setMaxOutlierValue(float value)
    {
        maxOutlierValue = value;
        setOutlierValuePerc();
    }

    private void setValuePerc() { valuePerc = value / maxValue; }

    private void setOutlierValuePerc() { outlierValuePerc = outlierValue / maxOutlierValue; }

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

        sprite = new Sprite(GdxUtils.getInstance().createSprite("rings"));
        sprite.setCenter(centerX, centerY);
        sprite.setOriginCenter();

        outlierSprite = new Sprite(GdxUtils.getInstance().createSprite("ring_outlier"));

        float outlierX = sprite.getWidth() / (leftAligned ? -2f : 2f);

        if (leftAligned) outlierSprite.setFlip(true, false);

        outlierSprite.setCenter(centerX - outlierX, centerY);
        outlierSprite.setOrigin(outlierX, outlierSprite.getHeight() / 2f);

        setValue(0f);
        setOutlierValue(0f);
    }

    @Override
    public void update()
    {
        sprite.setRotation(Math.abs(valuePerc) > 1f ? 360f : valuePerc * 360f);

        float rotation;
        if (outlierValuePerc > 1f) rotation = -90f;
        else if (outlierValuePerc < -1f) rotation = 90f;
        else rotation = outlierValuePerc * -90f;

        outlierSprite.setRotation(leftAligned ? -rotation : rotation);

        if (Math.abs(valuePerc) > HECTIC_BLINK_LIMIT) {
            sprite.setScale(AnimationState.getInstance().getHecticBackstreets());
            sprite.setColor(0.8f, 0.2f, 0f, AnimationState.getInstance().getHecticBlink());
        }
        else if (Math.abs(valuePerc) > BLINK_LIMIT) {
            sprite.setScale(AnimationState.getInstance().getBackstreets());
            sprite.setColor(0.7f, 0.3f, 0f, AnimationState.getInstance().getBlink());
        }
        else {
            sprite.setScale(1f);
            sprite.setColor(Color.WHITE);
        }

        if (Math.abs(outlierValuePerc) > OUTLIER_HECTIC_BLINK_LIMIT) {
            outlierSprite.setScale(AnimationState.getInstance().getHecticBackstreets());
            outlierSprite.setColor(0.8f, 0.2f, 0f, AnimationState.getInstance().getHecticBlink());
        }
        else if (Math.abs(outlierValuePerc) > OUTLIER_BLINK_LIMIT) {
            outlierSprite.setScale(AnimationState.getInstance().getBackstreets());
            outlierSprite.setColor(0.7f, 0.3f, 0f, AnimationState.getInstance().getBlink());
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

        BitmapFont font24 = GdxUtils.getInstance().getFont24();
        BitmapFont font18 = GdxUtils.getInstance().getFont18();

        if (Math.abs(valuePerc) > HECTIC_BLINK_LIMIT) {
            font24.setColor(1f, 0.3f, 0f, 1f);
            font18.setColor(1f, 0.3f, 0f, 1f);
        }
        else if (Math.abs(valuePerc) > BLINK_LIMIT) {
            font24.setColor(.95f, 0.4f, 0f, 1f);
            font18.setColor(.95f, 0.4f, 0f, 1f);
        }

        font18.draw(batch, label, textCenterX, centerY + TEXT_LABEL_MARGIN_Y);
        font24.draw(batch, String.valueOf(value) + unit, textCenterX, centerY + TEXT_MARGIN_Y);

        GdxUtils.getInstance().resetFont24Color();
        GdxUtils.getInstance().resetFont18Color();

        if (Math.abs(outlierValuePerc) > OUTLIER_HECTIC_BLINK_LIMIT)
            font18.setColor(1f, 0.3f, 0f, 1f);
        else if (Math.abs(outlierValuePerc) > OUTLIER_BLINK_LIMIT)
            font18.setColor(.95f, 0.4f, 0f, 1f);

        font18.draw(batch, outlierLabel, textCenterX, centerY + TEXT_OUTLIER_LABEL_MARGIN_Y);
        font18.draw(batch, String.valueOf(outlierValue) + outlierUnit, textCenterX,
                centerY + TEXT_OUTLIER_MARGIN_Y);

        GdxUtils.getInstance().resetFont18Color();
    }

    @Override
    public void shutdown() { }
}
