package com.buhbuhlz.buhbuhlz.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.Timer;

import java.util.ArrayList;
import java.util.Random;

import static java.lang.Math.floor;
import static java.lang.Math.incrementExact;
import static java.lang.Math.max;


public class BuhbUhlz extends ApplicationAdapter implements ApplicationListener, InputProcessor {
	SpriteBatch batch;
	Texture background;
	Random rand = new Random();
	boolean bubbleTime = false;
	double bubbleTimer = 3; //sec
    double bubbleStDev = 2; // standard deviation for Gaussian distribution
	double bubbleLife = 1.5; //sec
	float startPortion = 10; //screen portion (10 = 10th of the screen) for the start bubble size
	float maxPortion = 4; //screen portion (10 = 10th of the screen) for the max bubble size

	ArrayList<Integer> bubbleXs = new ArrayList<Integer>();
	ArrayList<Integer> bubbleYs = new ArrayList<Integer>();
	ArrayList<Integer> bubbleColors = new ArrayList<Integer>();
	ArrayList<Long> bubbleCreationTime = new ArrayList<Long>();
	ArrayList<Float> bubbleRs = new ArrayList<Float>(); // bubble radius (used for touch test)
	ArrayList<Boolean> bubbleTouched = new ArrayList<Boolean>();
	ShapeRenderer shapeRenderer;

	public void startTimer() {
		double bubbleDelay = (Math.abs(rand.nextGaussian()) * (-bubbleStDev)) + bubbleTimer;
		if (bubbleDelay < 0.1) {
			bubbleDelay = 0.1;
		}
		//Gdx.app.log("bubble delay:", String.valueOf(bubbleDelay));
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

        Gdx.input.setInputProcessor(this);

		startTimer();

	}

	public void makeBubble(int i) {
		float currentY;
		float currentX;
		float radius;
		float color;
		float screenWidth = Gdx.graphics.getWidth();
		long creationTime;
		float startRadius = screenWidth/startPortion;
		float maxRadius = screenWidth/maxPortion;
		if ((i == bubbleXs.size() && bubbleXs.size() > 0) || bubbleXs.size() == 0) {
			currentY = rand.nextInt(Gdx.graphics.getHeight());
			currentX = rand.nextInt(Gdx.graphics.getWidth());
			radius = startRadius;
			color = rand.nextInt(6) + 1; //from 1 to 6 (no white (0) and black (7))
			creationTime = TimeUtils.millis();
			bubbleYs.add(Math.round(currentY));
			bubbleXs.add(Math.round(currentX));
			bubbleColors.add(Math.round(color));
			bubbleCreationTime.add(creationTime);
			bubbleTouched.add(false);
			bubbleRs.add(radius);
		} else {
			currentY = bubbleYs.get(i);
			currentX = bubbleXs.get(i);
            color = bubbleColors.get(i);
            creationTime = bubbleCreationTime.get(i);
			radius = (float) (((TimeUtils.millis() - creationTime) * (maxRadius - startRadius))/(bubbleLife * 1000)) + startRadius;
			bubbleRs.set(i, radius);
		}
		float R = (float) floor(color/4);
		float G = (float) floor((color % 4)/2);
		float B = (float) (color % 4)%2;
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		shapeRenderer.begin(ShapeType.Filled);
		shapeRenderer.setColor(R, G, B, .5f);
		shapeRenderer.circle(currentX, currentY, radius);
		shapeRenderer.setColor(1f, 1f, 1f, .06f);
		shapeRenderer.circle(currentX - radius/3, currentY + radius/3, (float) (radius * 0.5));
		shapeRenderer.setColor(1f, 1f, 1f, .06f);
		shapeRenderer.circle(currentX - radius/3, currentY + radius/3, (float) (radius * 0.4));
		shapeRenderer.setColor(1f, 1f, 1f, .06f);
		shapeRenderer.circle(currentX - radius/3, currentY + radius/3, (float) (radius * 0.3));
		shapeRenderer.setColor(1f, 1f, 1f, .06f);
		shapeRenderer.circle(currentX - radius/3, currentY + radius/3, (float) (radius * 0.2));
        shapeRenderer.setColor(1f, 1f, 1f, .06f);
        shapeRenderer.circle(currentX - radius/3, currentY + radius/3, (float) (radius * 0.1));
        shapeRenderer.end();
	}

	@Override
	public void render () {
		batch.begin();

		batch.draw(background,(-Gdx.graphics.getWidth() * Gdx.graphics.getHeight() / background.getHeight() / 2) + (Gdx.graphics.getWidth() / 2),0,Gdx.graphics.getWidth() * Gdx.graphics.getHeight() / background.getHeight(),Gdx.graphics.getHeight());

		batch.end();

		int i = 0;
		while (i < bubbleXs.size()) {
            if (! bubbleTouched.get(i)) {
                makeBubble(i);
            }
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
				bubbleTouched.remove(i);
				bubbleRs.remove(i);
				i--;
			}
			i++;
		}


	}
	
	@Override
	public void dispose () {
		batch.dispose();

	}

	@Override
	public boolean keyDown(int keycode) {
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		int i = 0;
        float radius;
        float currentY;
        float currentX;
        while (i < bubbleXs.size()) {
            if (! bubbleTouched.get(i)) {
                radius = bubbleRs.get(i);
                currentY = bubbleYs.get(i);
                currentX = bubbleXs.get(i);
                if (Math.sqrt(Math.pow(screenX - currentX, 2) + Math.pow(screenY - currentY, 2)) <= radius) {
                    //Gdx.app.log("Touched:", "Catch!!!");
                    bubbleTouched.set(i, true);
                    i = bubbleXs.size(); //exit loop
                }
            }
            i++;
        }
        return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		return false;
	}
}
