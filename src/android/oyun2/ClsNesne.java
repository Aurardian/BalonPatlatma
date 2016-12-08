package android.oyun2;

import org.anddev.andengine.engine.handler.timer.TimerHandler;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.opengl.texture.Texture;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;

import android.content.Context;

public class ClsNesne{
	
	public int oTextureWidth, oTextureHeight;
	public Texture oTexture;
	public TextureRegion oTextureRegion;
	public Sprite oSprite;
	public TimerHandler timer;
	
	public ClsNesne(int oTextureWidth,int oTextureHeight, TextureOptions oTextureOptions, Context oContext, String oAssetPath, int oTexturePositionX, int oTexturePositionY)
	{
		this.oTextureWidth = oTextureWidth;
		this.oTextureHeight = oTextureHeight;
		oTexture = new Texture(oTextureWidth, oTextureHeight, oTextureOptions);
		oTextureRegion = TextureRegionFactory.createFromAsset(oTexture, oContext, oAssetPath, oTexturePositionX, oTexturePositionY);
	}

	public ClsNesne() {
		
	}
}
