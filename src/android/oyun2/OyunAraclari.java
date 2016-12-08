package android.oyun2;

import org.anddev.andengine.engine.handler.physics.PhysicsHandler;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.opengl.texture.Texture;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;

import android.content.Context;

public class OyunAraclari{
	
	public int oTextureWidth, oTextureHeight;
	public Texture oTexture;
	public TextureRegion oTextureRegion;
	public Sprite oSprite;
	public PhysicsHandler oPhy;
	public int pathCountX = 0;
	public int pathCountY = 0;
	
	public OyunAraclari(int oTextureWidth,int oTextureHeight, TextureOptions oTextureOptions, Context oContext, String oAssetPath, int oTexturePositionX, int oTexturePositionY, float oSpritePositionX, float oSpritePositionY)
	{
		this.oTextureWidth = oTextureWidth;
		this.oTextureHeight = oTextureHeight;
		oTexture = new Texture(oTextureWidth, oTextureHeight, oTextureOptions);
		oTextureRegion = TextureRegionFactory.createFromAsset(oTexture, oContext, oAssetPath, oTexturePositionX, oTexturePositionY);
		oSprite = new Sprite(oSpritePositionX, oSpritePositionY, oTextureRegion);
		oPhy = new PhysicsHandler(oSprite);
		oSprite.registerUpdateHandler(oPhy);
	}

	public OyunAraclari() {
		
	}
}
