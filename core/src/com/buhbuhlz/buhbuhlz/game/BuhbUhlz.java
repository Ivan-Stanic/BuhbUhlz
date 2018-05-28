package com.buhbuhlz.buhbuhlz.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.Timer;

import java.util.ArrayList;
import java.util.Random;

import static java.lang.Math.floor;
import static java.lang.Math.max;


public class BuhbUhlz extends ApplicationAdapter {
	SpriteBatch batch;
	Texture background;
	Random rand = new Random();
	boolean bubbleTime = false;
	double bubbleTimer = 3; //sec
	double bubbleLife = 1.5; //sec
	float startPortion = 10; //screen portion (10 = 10th of the screen) for the start bubble size
	float maxPortion = 4; //screen portion (10 = 10th of the screen) for the max bubble size

	Logger logger;

	ArrayList<Integer> bubbleXs = new ArrayList<Integer>();
	ArrayList<Integer> bubbleYs = new ArrayList<Integer>();
	ArrayList<Integer> bubbleColors = new ArrayList<Integer>();
	ArrayList<Long> bubbleCreationTime = new ArrayList<Long>();
	ShapeRenderer shapeRenderer;

	public void startTimer() {
		double bubbleDelay = (Math.abs(rand.nextGaussian()) * (-bubbleTimer)) + bubbleTimer;
		if (bubbleDelay < 0.1) {
			bubbleDelay = 0.1;
		}
		//logger.info(String.valueOf(bubbleDelay));
		Timer.schedule(new Timer.Task()
		{
			@Override
			public void run()
			{
				bubbleTime = true;
				Timer.instance().clear();
			}
		}, (float) bubbleDelay);
	}

	@Override
	public void create () {
		batch = new SpriteBatch();
		int randBg = rand.nextInt(15) + 1;
		background = new Texture("bg_" + String.valueOf(randBg) + ".jpg");
		shapeRenderer = new ShapeRenderer();

		startTimer();

	}

	public void makeBubble(int i) {
		float startY;
		float startX;
		float radius;
		float color;
		float screenWidth = Gdx.graphics.getWidth();
		long creationTime;
		float startRadius = screenWidth/startPortion;
		float maxRadius = screenWidth/maxPortion;
		if ((i == bubbleXs.size() && bubbleXs.size() > 0) || bubbleXs.size() == 0) {
			startY = rand.nextInt(Gdx.graphics.getHeight());
			startX = rand.nextInt(Gdx.graphics.getWidth());
			radius = startRadius;
			color = rand.nextInt(8);
			creationTime = TimeUtils.millis();
			bubbleYs.add(Math.round(startY));
			bubbleXs.add(Math.round(startX));
			bubbleColors.add(Math.round(color));
			bubbleCreationTime.add(creationTime);
		} else {
			startY = bubbleYs.get(i);
			startX = bubbleXs.get(i);
            color = bubbleColors.get(i);
            creationTime = bubbleCreationTime.get(i);
			radius = (float) (((TimeUtils.millis() - creationTime) * (maxRadius - startRadius))/(bubbleLife * 1000)) + startRadius;
		}
		float R = (float) floor(color/4);
		float G = (float) floor((color % 4)/2);
		float B = (float) (color % 4)%2;
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		shapeRenderer.begin(ShapeType.Filled);
		shapeRenderer.setColor(R, G, B, .5f);
		shapeRenderer.circle(startX, startY, radius);
		shapeRenderer.setColor(1f, 1f, 1f, .08f);
		shapeRenderer.circle(startX - radius/3, startY + radius/3, (float) (radius * 0.5));
		shapeRenderer.setColor(1f, 1f, 1f, .08f);
		shapeRenderer.circle(startX - radius/3, startY + radius/3, (float) (radius * 0.4));
		shapeRenderer.setColor(1f, 1f, 1f, .08f);
		shapeRenderer.circle(startX - radius/3, startY + radius/3, (float) (radius * 0.3));
		shapeRenderer.setColor(1f, 1f, 1f, .08f);
		shapeRenderer.circle(startX - radius/3, startY + radius/3, (float) (radius * 0.2));
        shapeRenderer.setColor(1f, 1f, 1f, .08f);
        shapeRenderer.circle(startX - radius/3, startY + radius/3, (float) (radius * 0.1));
        shapeRenderer.end();
	}

	@Override
	public void render () {
		batch.begin();

		batch.draw(background,(-Gdx.graphics.getWidth() * Gdx.graphics.getHeight() / background.getHeight() / 2) + (Gdx.graphics.getWidth() / 2),0,Gdx.graphics.getWidth() * Gdx.graphics.getHeight() / background.getHeight(),Gdx.graphics.getHeight());

		batch.end();

		int i = 0;
		while (i < bubbleXs.size()) {
			makeBubble(i);
			i++;
		}
		if (bubbleTime) {
			makeBubble(i);
			bubbleTime = false;
			startTimer();
		}
		i = 0;
		while (i < bubbleXs.size()) {
			if ((TimeUtils.millis() - bubbleCreationTime.get(i)) > bubbleLife * 1000){
				bubbleXs.remove(i);
				bubbleYs.remove(i);
				bubbleColors.remove(i);
				bubbleCreationTime.remove(i);
				i--;
			}
			i++;
		}


	}
	
	@Override
	public void dispose () {
		batch.dispose();

	}
}
