package com.example.coinman.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.Random;

public class CoinMan extends ApplicationAdapter {
	SpriteBatch batch;
	Texture bg;
	Texture[] man;
	Texture dizzy;
	int manState=0;
	int pause=0;
	float gravity=0.4f;
	float velocity=0;
	int manY=0;
	Rectangle manRect;
	BitmapFont font;

	ArrayList<Integer> coinX= new ArrayList<>();
	ArrayList<Integer> coinY= new ArrayList<>();
	ArrayList<Rectangle> coinRectangles= new ArrayList<>();
	Texture coin;
	int coinCount;
	Sound coinCollected;

	private AssetManager assetManager;

	ArrayList<Integer> bombX= new ArrayList<>();
	ArrayList<Integer> bombY= new ArrayList<>();
	ArrayList<Rectangle> bombRectangles= new ArrayList<>();
	Texture bomb;
	int bombCount;
	Sound bombCollected;
	boolean bombBlasted=true;

	Random random;
	int score=0;
	int gameState=0;

	@Override
	public void create () {
		batch = new SpriteBatch();
		bg= new Texture("bg.png");
		man = new Texture[4];
		man[0]= new Texture("frame-1.png");
		man[1]= new Texture("frame-2.png");
		man[2]= new Texture("frame-3.png");
		man[3]= new Texture("frame-4.png");

		manY=Gdx.graphics.getHeight()/2;

		random= new Random();
		coin= new Texture("coin.png");
		bomb=new Texture("bomb.png");


		font= new BitmapFont();
		font.setColor(Color.WHITE);
		font.getData().setScale(10);


		assetManager=new AssetManager();
		assetManager.load("coinCollected1.mp3",Sound.class);
		assetManager.load("bombCollected1.mp3",Sound.class);
		assetManager.finishLoading();

		dizzy= new Texture("dizzy-1.png");
	}

	public void makeCoin(){
		float height=random.nextFloat()*Gdx.graphics.getHeight();
		coinY.add((int)height);
		coinX.add(Gdx.graphics.getWidth());
	}
	public void makeBomb(){
		float height=random.nextFloat()*Gdx.graphics.getHeight();
		bombY.add((int)height);
		bombX.add(Gdx.graphics.getWidth());
	}

	@Override
	public void render () {
		batch.begin();
		batch.draw(bg,0,0, Gdx.graphics.getWidth(),Gdx.graphics.getHeight());

		if(gameState==1){
			//Game is Live

			//Coin
			if(coinCount<100)
				coinCount++;
			else {
				coinCount = 0;
				makeCoin();
			}
			coinRectangles.clear();
			for(int i=0;i<coinY.size();i++){
				batch.draw(coin,coinX.get(i),coinY.get(i));
				coinX.set(i,coinX.get(i)-5);
				if(coinX.get(i)<=0) {
//				coinX.set(i, -coin.getWidth());
					coinX.remove(i);
					coinY.remove(i);
					break;
				}
				coinRectangles.add(new Rectangle(coinX.get(i),coinY.get(i),coin.getWidth(),coin.getHeight()));

			}

			//Bomb
			random= new Random();
			if(bombCount<300-(random.nextFloat())*70)
				bombCount++;
			else{
				bombCount=0;
				makeBomb();
			}
			bombRectangles.clear();
			for(int i=0;i<bombY.size();i++){
				batch.draw(bomb,bombX.get(i),bombY.get(i));
				bombX.set(i,bombX.get(i)-10);
				if(bombX.get(i)<=0)
					bombX.set(i,-bomb.getWidth());
				bombRectangles.add(new Rectangle(bombX.get(i),bombY.get(i),bomb.getWidth(),bomb.getHeight()));
			}

            //onTouch
            if(Gdx.input.justTouched()){
                velocity=-15;
            }

            //To Slow down the run
            if(pause<6){
                pause++;
            }else{
                pause=0;
                if(manState<3){
                    manState++;
                }else{
                    manState=0;
                }
            }

            velocity+=gravity;
            manY-=velocity;
            if(manY<=0){
                manY=0;
            }else if(manY>=Gdx.graphics.getHeight()-(man[manState].getHeight()/2)){
                manY=Gdx.graphics.getHeight()-(man[manState].getHeight()/2);
            }

		}else if(gameState==0){
			//Waiting to start
			if(Gdx.input.justTouched())
				gameState=1;
		}else if(gameState==2){
			//Game Over
            if(Gdx.input.justTouched()){
                gameState=1;
                manY=Gdx.graphics.getHeight()/2;
                coinY.clear();
                coinX.clear();
                coinRectangles.clear();
                bombRectangles.clear();
                bombY.clear();
                bombX.clear();
                score=0;
                velocity=0;
                coinCount=0;
                bombCount=0;
                bombBlasted=true;
            }
		}

		if(gameState==2){
		    batch.draw(dizzy,(float) ((Gdx.graphics.getWidth()/2)-(man[manState].getWidth()/4)),manY,(float) man[manState].getWidth()/2,(float)man[manState].getHeight()/2);
        }else{
            batch.draw(man[manState],(float) ((Gdx.graphics.getWidth()/2)-(man[manState].getWidth()/4)),manY,(float) man[manState].getWidth()/2,(float)man[manState].getHeight()/2);
        }
		manRect= new Rectangle((float) ((Gdx.graphics.getWidth()/2)-(man[manState].getWidth()/4)),manY,(float)man[manState].getWidth()/2,(float)man[manState].getHeight()/2);
		for (int i=0;i<coinRectangles.size();i++){
			if(Intersector.overlaps(manRect,coinRectangles.get(i))){
				score++;
				coinCollected=assetManager.get("coinCollected1.mp3",Sound.class);
				coinCollected.play();
				coinRectangles.remove(i);
				coinX.remove(i);
				coinY.remove(i);
				break;
			}
		}
		for (int i=0;i<bombRectangles.size();i++){
			if(Intersector.overlaps(manRect,bombRectangles.get(i))){
				gameState=2;
				if(bombBlasted){
					bombBlasted=false;
					bombCollected = assetManager.get("bombCollected1.mp3", Sound.class);
					bombCollected.play();
				}
				break;
			}
		}
		font.draw(batch,String.valueOf(score),100,200);
		batch.end();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
	}
}
