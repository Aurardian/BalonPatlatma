package android.oyun2;

import java.util.Random;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.engine.handler.timer.ITimerCallback;
import org.anddev.andengine.engine.handler.timer.TimerHandler;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
import org.anddev.andengine.entity.scene.CameraScene;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.text.ChangeableText;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.opengl.font.FontFactory;
import org.anddev.andengine.opengl.texture.BuildableTexture;
import org.anddev.andengine.opengl.texture.Texture;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.sensor.accelerometer.AccelerometerData;
import org.anddev.andengine.sensor.accelerometer.IAccelerometerListener;
import org.anddev.andengine.ui.activity.BaseGameActivity;
import android.graphics.Color;
import android.view.KeyEvent;

public class BalonPatlatmaActivity extends BaseGameActivity implements IAccelerometerListener
{
	private static final int CAMERA_WIDTH = 800;
    private static final int CAMERA_HEIGHT = 480;
    private Camera camera;
    private Engine engine;
	Scene sahneOyun, sahneAnaMenu, sahnePauseMenu;
	
	BuildableTexture bTex;
	Font font;
	ChangeableText cTexSkor;
	
	TimerHandler timerOyunSuresi;
	Random rnd;
	
	private int skor = 0;
	
	private int balonDiziX = 8, balonDiziY = 3;
	int kalip = 1;
	
	/* Yukardan gelecek balonlar i�in 3 yol belirledik.
	 * A�a��daki diziler ile de bu yollar�n hangisinden balon
	 * gelece�ini belirleyece�iz. 1'ler balonun var oldu�unu belirtirken
	 * 0 balon olmad���n� belirler
	*/
	byte kalip1[][]={
			{1,0,0},
			{0,0,0},
			{0,1,0},
			{0,0,0},
			{0,0,1},
			{0,0,0},
			{1,0,0},
			{0,0,0}
			};
	byte kalip2[][]={
			{1,0,1},
			{0,0,1},
			{1,0,0},
			{0,0,0},
			{0,0,1},
			{0,0,0},
			{1,0,0},
			{0,0,0}
			};
	byte kalip3[][]={
			{1,0,0},
			{1,0,0},
			{0,1,0},
			{0,0,1},
			{0,0,1},
			{0,0,0},
			{1,0,0},
			{0,1,0}
			};
	byte kalip4[][]={
			{0,0,0},
			{0,1,0},
			{0,0,0},
			{0,0,1},
			{0,0,1},
			{1,0,0},
			{1,0,0},
			{0,1,0}
			};
	byte kalip5[][]={
			{0,0,1},
			{0,0,1},
			{1,0,0},
			{0,0,1},
			{1,0,1},
			{0,0,0},
			{1,0,0},
			{0,1,0}
			};
	
	private boolean oPhyTanimlandiMi = true;

	// Oyun ara�lar� tan�mlan�yor. H�z verebilmemiz i�in fiziki bir ortam gereklidir.
	// Bu y�zden oyunAraclari s�n�f�nda bir de PhysicsHandler nesnesi bulunmakta. 
	OyunAraclari arka1, arka2, oyuncu;
	OyunAraclari [][]balonlar = new OyunAraclari[balonDiziX][balonDiziY];
	
	// Men� nesneleri tan�mlan�yor.
	private ClsNesne anaMenuArka, anaMenuOyna,
	anaMenuOynaHover, anaMenuCikis, anaMenuCikisHover,
	pauseMenuArka, pauseMenuMenu, pauseMenuRestart, pauseMenuResume;
	
	// Balon patlama animasyonu i�in nesneler tan�mlan�yor
	Texture texPatlama;
	TiledTextureRegion tilTexRegPatlama;
	AnimatedSprite animSpritePatlama;
	
	// Sahne kontrolleri yan�mlan�yor
	private boolean anaMenuSahnesiMi = true, oyunSahnesiMi = false, pauseMenuSahnesiMi = false;
	
	// ��ne h�z� tan�mlan�yor
	private float oyuncuHizi = 0;
	
	@Override
	public Engine onLoadEngine() 
	{
		// Camera boyutu, konumu ve �e�itli ayarlamalar ile birlikte motor ayarlar� yap�l�yor.
		camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
	    final EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE, new FillResolutionPolicy(), camera);
	    engineOptions.getTouchOptions().setRunOnUpdateThread(true);
	    engine = new Engine(engineOptions);
		
		return engine;
	}

	@Override
	public void onLoadResources() 
	{
		//Ana men� nesneleri olu�turuluyor
		anaMenuArka = new ClsNesne(1024, 512, TextureOptions.BILINEAR_PREMULTIPLYALPHA, this, "gfx/back.png", 0, 0);
		anaMenuOyna = new ClsNesne(64, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA, this, "gfx/bt_play.png", 0, 0);
		anaMenuOynaHover = new ClsNesne(64, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA, this, "gfx/bt_play_hover.png", 0, 0);
		anaMenuCikis = new ClsNesne(64, 128, TextureOptions.BILINEAR_PREMULTIPLYALPHA, this, "gfx/bt_quit.png", 0, 0);
		anaMenuCikisHover = new ClsNesne(64, 128, TextureOptions.BILINEAR_PREMULTIPLYALPHA, this, "gfx/bt_quit_hover.png", 0, 0);
				
		// Pause men� nesneleri olu�turuluyor
		pauseMenuArka = new ClsNesne(1024, 512, TextureOptions.BILINEAR_PREMULTIPLYALPHA, this, "gfx/pauseArkaplan.png", 0, 0);
		pauseMenuRestart = new ClsNesne(128, 128, TextureOptions.BILINEAR_PREMULTIPLYALPHA, this, "gfx/restart.png", 0, 0);
		pauseMenuResume = new ClsNesne(128, 128, TextureOptions.BILINEAR_PREMULTIPLYALPHA, this, "gfx/resume.png", 0, 0);
		pauseMenuMenu = new ClsNesne(128, 128, TextureOptions.BILINEAR_PREMULTIPLYALPHA, this, "gfx/menu.png", 0, 0);

		// Patlama animasyonu i�in texture ve tiledTextureRegion nesneleri olu�turuluyor
		texPatlama = new Texture(256, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		tilTexRegPatlama = TextureRegionFactory.createTiledFromAsset(texPatlama, this, "gfx/patlama_anim.png", 0, 0, 1, 4);
		
		// Oyun ara�lar� olu�turuluyor.
		arka1 = new OyunAraclari(1024, 512, TextureOptions.BILINEAR_PREMULTIPLYALPHA, this, "gfx/arkaplan.jpg", 0, 0, 800 - 1024, 0);
		arka2 = new OyunAraclari(1024, 512, TextureOptions.BILINEAR_PREMULTIPLYALPHA, this, "gfx/arkaplan.jpg", 0, 0, 800 - 2048, 0);
		oyuncu = new OyunAraclari(128, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA, this, "gfx/needle.png", 0, 0, 600, 200);
		
		// Balonlar rastgele renklerde olu�turuluyor
		int a;
		for(int i = 0; i < balonDiziX; i++)
		{
			for(int j = 0; j < balonDiziY; j++)
			{
				rnd = new Random();
				a = rnd.nextInt(4);
				if( a == 0)
					balonlar[i][j] = new OyunAraclari(128, 128, TextureOptions.BILINEAR_PREMULTIPLYALPHA, this, "gfx/balon0.png", 0, 0, i*256 - 1600, 60 + j * 122);
				else if( a == 1)
					balonlar[i][j] = new OyunAraclari(128, 128, TextureOptions.BILINEAR_PREMULTIPLYALPHA, this, "gfx/balon1.png", 0, 0, i*256 - 1600, 60 + j * 122);
				else if( a == 2)
					balonlar[i][j] = new OyunAraclari(128, 128, TextureOptions.BILINEAR_PREMULTIPLYALPHA, this, "gfx/balon2.png", 0, 0, i*256 - 1600, 60 + j * 122);
				else if( a == 3)
					balonlar[i][j] = new OyunAraclari(128, 128, TextureOptions.BILINEAR_PREMULTIPLYALPHA, this, "gfx/balon3.png", 0, 0, i*256 - 1600, 60 + j * 122);
				engine.getTextureManager().loadTexture(balonlar[i][j].oTexture);
				
			}
		}
		
		//Ekrana text nesnesi yazd�rmak i�in gerekli BuildableTexture nesnesi olu�turuluyor.
		if(bTex == null)
		{
			bTex = new BuildableTexture(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		}
		else
		{
			bTex = null;
			bTex = new BuildableTexture(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		}
		
		// Ekrana text nesnesi yazd�rmak i�in gerekli Font nesnesi olu�turuluyor.
		this.font = FontFactory.createStrokeFromAsset(this.bTex, this, "gfx/CippHand.ttf", 75, true, Color.WHITE, 2, Color.rgb(97, 134, 147));// FontFactory.createFromAsset(bTex, this, "gfx/CippHand.ttf", 25, true, color.black);
		
		// Texture nesneleri, donan�ma y�klenmek �zere bir dizide tutuluyor.
		Texture []textures = {texPatlama, bTex, arka1.oTexture, arka2.oTexture, oyuncu.oTexture,
				anaMenuArka.oTexture, anaMenuOyna.oTexture, anaMenuOynaHover.oTexture, anaMenuCikis.oTexture, anaMenuCikisHover.oTexture,
				pauseMenuArka.oTexture, pauseMenuMenu.oTexture, pauseMenuRestart.oTexture, pauseMenuResume.oTexture};
		
		// Texture ve Font nesneleri y�kleniyor.
		this.engine.getFontManager().loadFont(font);
		this.engine.getTextureManager().loadTextures(textures);
	}

	@Override
	public Scene onLoadScene() 
	{
		this.engine.registerUpdateHandler(new FPSLogger());
		
		// Sens�r etkinle�tiriliyor.
		this.enableAccelerometerSensor(this);
		
		// Sahne nesneleri olu�tutuluyor
		this.sahneOyun = new Scene();
		this.sahneAnaMenu = new Scene();
		this.sahnePauseMenu = new CameraScene(this.camera);
		
		this.cTexSkor = new ChangeableText(20, 240, font, "", 6);
		
		this.cTexSkor.setRotation(-90);
		this.cTexSkor.setText("0");
		
		// Animasyon nesnesi olu�turuluyor.
		this.animSpritePatlama = new AnimatedSprite(0, 0, tilTexRegPatlama);
		
		// Metot isimlerinden hangi metodun hangi i�lemleri yapaca��n� ��karabilirsiniz
		this.anaMenuNesneleriniOlustur();
		this.pauseMenuNesneleriniOlustur();
		this.arkaplanKontrolleri();
		
		// sahneOyun Sahnesine nesneler �izdiriliyor
		this.sahneOyun.attachChild(arka1.oSprite);
		this.sahneOyun.attachChild(arka2.oSprite);
		this.sahneOyun.attachChild(oyuncu.oSprite);
		this.sahneOyun.attachChild(cTexSkor);
		this.sahneOyun.attachChild(animSpritePatlama);
		
		this.animSpritePatlama.setVisible(false);
		
		// Balonlar�n g�r�n�rl�kleri yukar�daki integer dizi kal�plar�na g�re ayarlan�yor
		for(int i = 0; i < 8; i++)
		{
			for(int j = 0; j < 3; j++)
			{	
				balonlar[i][j].oPhy.setVelocity(200, 0);
				this.sahneOyun.attachChild(balonlar[i][j].oSprite);
				if(kalip1[i][j] == 1)
				{
					
				}
				else
				{
					balonlar[i][j].oSprite.setVisible(false);
				}
			}
		}
		return sahneAnaMenu;
	}

	@Override
	public void onLoadComplete() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
    public void onAccelerometerChanged(final AccelerometerData pAccelerometerData) 
	{
		oyuncuHizi = pAccelerometerData.getY();
    }
	
	// Arkaplan nesnesinin hareketini ve oyuncunun h�z�n� kontrol eden metot
	// ama�, resimlerden biri a�a��dan ekrandan ��kt��� anda tekrar �ste, 
	// yani di�er resmin �st�ne yap���k bir �ekilde hareket ettirmek ve bu d�ng�y� sa�lamak
	private void arkaplanKontrolleri()
	{
		
		this.engine.registerUpdateHandler(new IUpdateHandler() {
			
			@Override
			public void reset() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onUpdate(float pSecondsElapsed) 
			{
				arka1.oPhy.setVelocityX(300f*3f);
				arka2.oPhy.setVelocityX(300f*3f);
				
				
				if(oyuncuHizi > 0)
				{
					if(oyuncu.oSprite.getY() <= 60)
					{
						oyuncu.oPhy.setVelocity(0, oyuncuHizi*40);
					}
					else if(oyuncu.oSprite.getY() > 60 &&  oyuncu.oSprite.getY() <= CAMERA_HEIGHT - (60 + 64))
					{
						oyuncu.oPhy.setVelocity(0, oyuncuHizi*40);
					}
					else
					{
						oyuncu.oPhy.setVelocity(0, 0);
					}
				}
				
				if(oyuncuHizi < 0)
				{
					if(oyuncu.oSprite.getY() > CAMERA_HEIGHT - (60 + 64))
					{
						oyuncu.oPhy.setVelocity(0, oyuncuHizi*40);
					}
					else if(oyuncu.oSprite.getY() > 60 &&  oyuncu.oSprite.getY() <= CAMERA_HEIGHT - (60 + 64))
					{
						oyuncu.oPhy.setVelocity(0, oyuncuHizi*40);
					}
					else
					{
						oyuncu.oPhy.setVelocity(0, 0);
					}
				}
				
				
				
        		
				if(arka1.oSprite.getX() >= 800)
				{
					arka1.oSprite.setPosition(arka2.oSprite.getX()-1024, 0);
				}
				if(arka2.oSprite.getX() >= 800)
				{
					arka2.oSprite.setPosition(arka1.oSprite.getX()-1024, 0);
				}
				
				if(balonlar[0][0].oSprite.getX() >= 800)
				{
					kalipDegistir();
				}
				
				for(int i = 0; i < balonDiziX; i++)
				{
					for(int j = 0; j < balonDiziY; j++)
					{	
						// Balon �eklinden dolay� �arp��malar� if kontrol� ile ayr�nt�l� bir �ekilde kontrol etmemiz gerekiyor
						// Aksi taktirde normalde kare olan resmin i�inde balon k���k bir alan kapl�yor. 
						// Bu durumda i�ne balona �arpmadan patlayabiliyor. Bunu �nlemek i�in if bloklar� ile kendi kontrollerimizi yap�yoruz
						if(balonlar[i][j].oSprite.collidesWith(oyuncu.oSprite) && 
								(oyuncu.oSprite.getY() < balonlar[i][j].oSprite.getY() + balonlar[i][j].oSprite.getWidth()) && (oyuncu.oSprite.getY() > balonlar[i][j].oSprite.getY() + 50) &&
								(oyuncu.oSprite.getX() < balonlar[i][j].oSprite.getX() + balonlar[i][j].oSprite.getHeight()/2)
								)
						{
							if(balonlar[i][j].oSprite.isVisible())
							{
								skor++;
								cTexSkor.setText(Integer.toString(skor));
								balonlar[i][j].oSprite.setVisible(false);
								animSpritePatlama.setPosition(balonlar[i][j].oSprite.getX(), balonlar[i][j].oSprite.getY() - 40);
								animSpritePatlama.setVisible(true);
								animSpritePatlama.animate(150);
								
								engine.registerUpdateHandler(timerOyunSuresi = new TimerHandler(0.5f, false, new ITimerCallback() {
									
									@Override
									public void onTimePassed(TimerHandler pTimerHandler) 
									{
										// TODO Auto-generated method stub
										animSpritePatlama.setVisible(false);
										animSpritePatlama.stopAnimation(0);
									}
								}));
							}
							
						}
					}
				}
			}
		});
	}
	
	// Kal�plar�n yani balon yerlerinin de�i�tirilmesini sa�layan metot
	private void kalipDegistir() 
	{
		kalip++;
		for(int i = 0; i < 8; i++)
		{
			for(int j = 0; j < 3; j++)
			{
				if(kalip == 1)
				{
					if(kalip3[i][j] == 1)
					{
						balonlar[i][j].oSprite.setVisible(false);
					}
					if(kalip1[i][j] == 1)
					{
						balonlar[i][j].oSprite.setVisible(true);
						balonlar[i][j].oSprite.setPosition(i*256 - 1600, 60 + j * 122);
					}
				}
				
				if(kalip == 2)
				{
					if(kalip1[i][j] == 1)
					{
						balonlar[i][j].oSprite.setVisible(false);
					}
					if(kalip2[i][j] == 1)
					{
						balonlar[i][j].oSprite.setVisible(true);
						balonlar[i][j].oSprite.setPosition(i*256 - 1600, 60 + j * 122);
					}
				}
				
				if(kalip == 3)
				{
					if(kalip2[i][j] == 1)
					{
						balonlar[i][j].oSprite.setVisible(false);
					}
					if(kalip3[i][j] == 1)
					{
						balonlar[i][j].oSprite.setVisible(true);
						balonlar[i][j].oSprite.setPosition(i*256 - 1600, 60 + j * 122);
					}
				}
				
				if(kalip == 4)
				{
					if(kalip3[i][j] == 1)
					{
						balonlar[i][j].oSprite.setVisible(false);
					}
					if(kalip4[i][j] == 1)
					{
						balonlar[i][j].oSprite.setVisible(true);
						balonlar[i][j].oSprite.setPosition(i*256 - 1600, 60 + j * 122);
					}
				}
				
				if(kalip == 5)
				{
					if(kalip4[i][j] == 1)
					{
						balonlar[i][j].oSprite.setVisible(false);
					}
					if(kalip5[i][j] == 1)
					{
						balonlar[i][j].oSprite.setVisible(true);
						balonlar[i][j].oSprite.setPosition(i*256 - 1600, 60 + j * 122);
					}
					if(i == balonDiziX -1 && j == balonDiziY - 1)
					{
						kalip = 1;
					}
				}
			}
		}		
	}  
	
	private void anaMenuNesneleriniOlustur()
	{
		anaMenuArka.oSprite = new Sprite(0, 0, anaMenuArka.oTextureRegion);
		

		anaMenuOynaHover.oSprite = new Sprite(312, 107, anaMenuOynaHover.oTextureRegion);
		anaMenuOyna.oSprite = new Sprite(312, 107, anaMenuOyna.oTextureRegion)
		{
			@Override
            public boolean onAreaTouched(TouchEvent pSceneTouchEvent,
            		float pTouchAreaLocalX, float pTouchAreaLocalY) 
			{
    			if(pSceneTouchEvent.isActionDown())
    			{
    				anaMenuOyna.oSprite.setVisible(false);
    				anaMenuOynaHover.oSprite.setVisible(true);
    			}
    			if(pSceneTouchEvent.isActionUp())
    			{   
    				anaMenuOyna.oSprite.setVisible(true);
    				anaMenuOynaHover.oSprite.setVisible(false);
    				anaMenuSahnesiMi = false;
    				oyunSahnesiMi = true;
    				restartGame();
    				engine.setScene(sahneOyun);
    			}
                return true;
            }
		};
		
		//Hover ve as�l nesne ayn� koordinatlarda olu�turuluyor
		anaMenuCikisHover.oSprite = new Sprite(685, 60, anaMenuCikisHover.oTextureRegion);
		anaMenuCikis.oSprite = new Sprite(685, 60, anaMenuCikis.oTextureRegion)
		{
			@Override
            public boolean onAreaTouched(TouchEvent pSceneTouchEvent,
                            float pTouchAreaLocalX, float pTouchAreaLocalY) 
			{
    			if(pSceneTouchEvent.isActionDown())
    			{
    				anaMenuCikis.oSprite.setVisible(false);
    				anaMenuCikisHover.oSprite.setVisible(true);
    			}
    			if(pSceneTouchEvent.isActionUp())
    			{   
    				finish();
    	            System.exit(0);
    			}
                return true;
            }
		};
		
		// Hover nesnelerinin g�r�n�rl�k �zelli�i false yap�l�yor
		anaMenuOynaHover.oSprite.setVisible(false);
		anaMenuCikisHover.oSprite.setVisible(false);
		
		// sahneMenu nesneleri sahneye �izdiriliyor
		this.sahneAnaMenu.attachChild(anaMenuArka.oSprite);
		this.sahneAnaMenu.attachChild(anaMenuOyna.oSprite);
		this.sahneAnaMenu.attachChild(anaMenuOynaHover.oSprite);
		this.sahneAnaMenu.attachChild(anaMenuCikis.oSprite);
		this.sahneAnaMenu.attachChild(anaMenuCikisHover.oSprite);
		
		// sahneMenu �zerindeki butonlar�n RegisterArea
		// �zellikleri tan�mlan�yor 
		// (Hover nesnelerin dokunma  �zellikleri hari�)
		this.sahneAnaMenu.registerTouchArea(anaMenuOyna.oSprite);
		this.sahneAnaMenu.registerTouchArea(anaMenuCikis.oSprite);
		
		
	}
	
	// Pause menu nesneleri olu�turuluyor. Yine �effaf bir sahne olacak pause menu sahnesi de
	private void pauseMenuNesneleriniOlustur()
	{
		pauseMenuArka.oSprite = new Sprite(0, 0, pauseMenuArka.oTextureRegion);
		pauseMenuRestart.oSprite = new Sprite(200, 110, pauseMenuRestart.oTextureRegion)
		{
			public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) 
			{
				if (pSceneTouchEvent.isActionDown()) 
				{
					
				}
				if (pSceneTouchEvent.isActionUp()) 
				{
					sahneOyun.clearChildScene();
					restartGame();
				}
				return true;
			}
		};
		pauseMenuResume.oSprite = new Sprite(200, 220, pauseMenuResume.oTextureRegion)
		{
			public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) 
			{
				if (pSceneTouchEvent.isActionDown()) 
				{
					
				}
				if (pSceneTouchEvent.isActionUp()) 
				{
					pauseMenuSahnesiMi = false;
    				oyunSahnesiMi = true;
					sahneOyun.clearChildScene();
					resumeGame();
				}
				return true;
			}
		};
		pauseMenuMenu.oSprite = new Sprite(200, 330, pauseMenuMenu.oTextureRegion)
		{
			public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) 
			{
				if (pSceneTouchEvent.isActionDown()) 
				{
					
           				
				}
				if (pSceneTouchEvent.isActionUp()) 
				{
    				//restart();
    				anaMenuSahnesiMi = true;
    				oyunSahnesiMi = false;
    				sahneOyun.clearChildScene();
					engine.setScene(sahneAnaMenu);
				}
				return true;
			}
		};
		
		sahnePauseMenu.attachChild(pauseMenuArka.oSprite);
		sahnePauseMenu.attachChild(pauseMenuMenu.oSprite);
		sahnePauseMenu.attachChild(pauseMenuRestart.oSprite);
		sahnePauseMenu.attachChild(pauseMenuResume.oSprite);
		
		sahnePauseMenu.registerTouchArea(pauseMenuMenu.oSprite);
		sahnePauseMenu.registerTouchArea(pauseMenuRestart.oSprite);
		sahnePauseMenu.registerTouchArea(pauseMenuResume.oSprite);
		
		// �effaf sahneler(CameraScene) i�in gerekli iki sat�r
		this.sahneOyun.setTouchAreaBindingEnabled(true);
		this.sahnePauseMenu.setBackgroundEnabled(false);
	}
	
	// Oyunun durdurulmas�n� sa�layan metot
	private void pauseGame()
	{
		if(oPhyTanimlandiMi)
		{	
			for(int i = 0; i < 8; i++)
			{
				for(int j = 0; j < 3; j++)
				{	
					balonlar[i][j].oSprite.unregisterUpdateHandler(balonlar[i][j].oPhy);
				}
			}
			
			// Oyunu durdurmak i�in fiziksel olaylar� durdurmak yeterli olacakt�r.
			// A�a��daki ifadelerle tan�mlanm�� olan update handler nesnesini devre d��� b�rak�yoruz.
			// Oyunu tekrar devam ettirmek i�in bu ifadeyi tekrar tan�ml�yoruz
			arka1.oSprite.unregisterUpdateHandler(arka1.oPhy);
			arka2.oSprite.unregisterUpdateHandler(arka2.oPhy);
			oyuncu.oSprite.unregisterUpdateHandler(oyuncu.oPhy);
			oPhyTanimlandiMi = false;
		}
	}
	
	private void resumeGame()
	{
		if(!oPhyTanimlandiMi)
		{
			for(int i = 0; i < 8; i++)
			{
				for(int j = 0; j < 3; j++)
				{	
					balonlar[i][j].oSprite.registerUpdateHandler(balonlar[i][j].oPhy);
				}
			}
			
			// updateHandler nesnesi tekrar tan�mlan�yor
			arka1.oSprite.registerUpdateHandler(arka1.oPhy);
			arka2.oSprite.registerUpdateHandler(arka2.oPhy);
			oyuncu.oSprite.registerUpdateHandler(oyuncu.oPhy);
			oPhyTanimlandiMi = true;
		}
	}
	
	// De�erler s�f�rlan�yor
	private void restartGame()
	{
		pauseMenuSahnesiMi = false;
		oyunSahnesiMi = true;
		kalip = 0;
		skor = 0;
		cTexSkor.setText(Integer.toString(skor));
		oyuncu.oSprite.setPosition(600, 200);
		kalipDegistir();
		resumeGame();
	}
	
	// Fiziksel tu�lar�n kullan�m�n� sa�layan metotlar.
	@Override
	public boolean onKeyDown(final int pKeyCode, final KeyEvent pEvent)
	{
		// Geri Tu�una bas�ld���nda yap�lacaklar
		if(pKeyCode == KeyEvent.KEYCODE_BACK && pEvent.getAction() == KeyEvent.ACTION_DOWN) 
		{
			if(oyunSahnesiMi)
			{
				pauseMenuSahnesiMi = true;
				oyunSahnesiMi = false;
				pauseGame();
				sahneOyun.setChildScene(sahnePauseMenu);
			}
			else if(pauseMenuSahnesiMi)
			{
				pauseMenuSahnesiMi = false;
				oyunSahnesiMi = true;
				resumeGame();
				sahneOyun.clearChildScene();
			}
			else if(anaMenuSahnesiMi)
			{
				System.exit(0);
			}
            
			return true;
		}
		// Menu tu�una bas�ld���nda yap�lacaklar
		else if(pKeyCode == KeyEvent.KEYCODE_MENU && pEvent.getAction() == KeyEvent.ACTION_DOWN) 
		{		
			// Bu k�s�mda bir �ey yapmaya gerek duymad�k
			// Ama istenilen bir g�rev buraya yaz�labilir.
			return true;
		}
		else 
		{
			return super.onKeyDown(pKeyCode, pEvent);
		}
	}
}