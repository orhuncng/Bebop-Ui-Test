package com.trio.drone.vr.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class GdxUtils
{
    private static final float FONT_COLOR_R = 0.92f;
    private static final float FONT_COLOR_G = 0.92f;
    private static final float FONT_COLOR_B = 0.92f;

    private static GdxUtils instance;

    private ShaderProgram defaultShader;
    private TextureAtlas atlas;
    private BitmapFont font24;
    private BitmapFont font18;

    private GdxUtils()
    {
        defaultShader = SpriteBatch.createDefaultShader();
        atlas = new TextureAtlas(Gdx.files.internal("images/vr/pack.atlas"));
        font24 = createBitmapFont(24);
        font18 = createBitmapFont(18);
    }

    public static GdxUtils get()
    {
        if (instance == null) instance = new GdxUtils();
        return instance;
    }

    public ShaderProgram getDefaultShader() { return defaultShader; }

    public Sprite createSprite(String name) { return atlas.createSprite(name); }

    private static BitmapFont createBitmapFont(int size)
    {
        BitmapFont font;

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
                Gdx.files.internal("fonts/kartika.ttf"));

        FreeTypeFontGenerator.FreeTypeFontParameter parameter =
                new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = size;

        font = generator.generateFont(parameter);

        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear,
                Texture.TextureFilter.Linear);

        generator.dispose();

        return font;
    }

    public BitmapFont getFont24() { return font24; }

    public BitmapFont getFont18() { return font18; }

    public void resetFont24Color()
    {
        font24.setColor(FONT_COLOR_R, FONT_COLOR_G, FONT_COLOR_B, 1f);
    }

    public void resetFont18Color()
    {
        font18.setColor(FONT_COLOR_R, FONT_COLOR_G, FONT_COLOR_B, 1f);
    }
}
