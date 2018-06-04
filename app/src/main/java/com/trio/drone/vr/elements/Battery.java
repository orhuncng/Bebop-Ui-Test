package com.trio.drone.vr.elements;

import android.content.res.Resources;
import android.util.DisplayMetrics;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.trio.drone.vr.SceneListener;
import com.trio.drone.vr.util.AnimationState;
import com.trio.drone.vr.util.GdxUtils;

public class Battery implements SceneListener
{
    private static final float HECTIC_BLINK_LIMIT = 0.15f;
    private static final float BLINK_LIMIT = 0.3f;
    private static final float GREEN_LIMIT = 0.5f;
    private Sprite sprite;
    private Sprite levelSprite;
    private float level;
    private float levelSpriteHeight;

    public void setLevel(float level) { this.level = level; }

    @Override
    public void create(DisplayMetrics metrics, Resources res)
    {
        float centerX = (metrics.widthPixels / 2f) - 200f;
        float centerY = (metrics.heightPixels / 2f) - 20f;

        sprite = new Sprite(GdxUtils.get().createSprite("battery"));
        sprite.setPosition(centerX, centerY);
        levelSpriteHeight = sprite.getHeight();
        sprite.setOriginCenter();

        levelSprite = new Sprite(GdxUtils.get().createSprite("battery_level"));
        levelSprite.setPosition(centerX, centerY + 8f);
        levelSprite.setOriginCenter();

        sprite.rotate(-90f);
        levelSprite.rotate(-90f);
    }

    @Override
    public void update()
    {
        if (level < BLINK_LIMIT) levelSprite.setColor(Color.SCARLET);
        else if (level < GREEN_LIMIT) levelSprite.setColor(Color.ORANGE);
        else levelSprite.setColor(Color.LIME);

        levelSprite.setSize(levelSprite.getWidth(), levelSpriteHeight * level);

        if (level < HECTIC_BLINK_LIMIT) {
            sprite.setColor(0.8f, 0.2f, 0f, AnimationState.get().getHecticBlink());
            levelSprite.setAlpha(AnimationState.get().getHecticBlink());
        }
        else if (level < BLINK_LIMIT) {
            sprite.setColor(0.7f, 0.3f, 0f, AnimationState.get().getBlink());
            levelSprite.setAlpha(AnimationState.get().getBlink());
        }
        else sprite.setColor(Color.WHITE);
    }

    @Override
    public void draw(SpriteBatch batch)
    {
        levelSprite.draw(batch);
        sprite.draw(batch);
        GdxUtils.get().getFont18().draw(batch, String.valueOf((int) level * 100) + "%",
                sprite.getX(), sprite.getY() + 53f);
    }

    @Override
    public void shutdown() { }
}
