package com.trio.drone.vr.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class GdxUtils
{
    private static GdxUtils instance;
    private ShaderProgram defaultShader;
    private TextureAtlas atlas;

    private GdxUtils()
    {
        defaultShader = SpriteBatch.createDefaultShader();
        atlas = new TextureAtlas(Gdx.files.internal("images/vr/vr_gui.atlas"));
    }

    public static GdxUtils getInstance()
    {
        if (instance == null) instance = new GdxUtils();
        return instance;
    }

    public ShaderProgram getDefaultShader() { return defaultShader; }

    public TextureAtlas getAtlas() { return atlas; }

    public Sprite createSprite(String name) { return atlas.createSprite(name); }
}
