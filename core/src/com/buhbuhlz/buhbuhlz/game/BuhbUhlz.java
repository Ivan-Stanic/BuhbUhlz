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

import static java.lang.Math.PI;
import static java.lang.Math.floor;
import static java.lang.Math.incrementExact;
import static java.lang.Math.max;


public class BuhbUhlz extends ApplicationAdapter implements ApplicationListener, InputProcessor {
	SpriteBatch batch;
	Texture background;
	Random rand = new Random();
	boolean bubbleTime = false;
	double bubbleTimer = 3; //sec, will be shortened for higher game levels
    double bubbleStDev = 2; // standard deviation for Gaussian distribution, should be adjusted with bubbleTimer
	double bubbleLife = 1.5; //sec, will be shortened for higher game levels in accordance with distance and speed (if introduced)
	float startPortion = 10; //screen portion (10 = 10th of the screen) for the start bubble size
	float maxPortion = 4; //screen portion (10 = 10th of the screen) for the max bubble size
	float maxDistance = 3; //screen portion (10 = 10th of the screen) for the max bubble distance

	ArrayList<Integer> bubbleXs = new ArrayList<Integer>();
	ArrayList<Integer> bubbleYs = new ArrayList<Integer>();
	ArrayList<Integer> bubbleColors = new ArrayList<Integer>();
	ArrayList<Long> bubbleCreationTime = new ArrayList<Long>();
	ArrayList<Float> bubbleRs = new ArrayList<Float>(); // bubble radius (used for touch test)
	ArrayList<Boolean> bubbleTouched = new ArrayList<Boolean>();
	ArrayList<Float> bubbleHeading = new ArrayList<Float>(); //bubble heading in rad, may dynamically change on higher levels
	ArrayList<Float> bubbleDistance = new ArrayList<Float>(); //the total distance of bubble movement in px (currently set to the same value for all, may be different in the future or on the higher game levels)
	//distance may also determine the speed of the bubble
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

	public void newBubble() {
		float screenWidth = Gdx.graphics.getWidth();
		float startRadius = screenWidth/startPortion;
		float maxRadius = screenWidth/maxPortion;
		float actualMaxDistance = screenWidth/maxDistance; // Temporarily set to constant value and adjusted for screen size
		float radius = startRadius;
		float currentY = rand.nextInt((int) (Gdx.graphics.getHeight() - (4 * startRadius))) + (2 * startRadius); //center of the new bubble is 2 starting radii from the edge of the screen
		float currentX = rand.nextInt((int) (Gdx.graphics.getWidth() - (4 * startRadius))) + (2 * startRadius); //center of the new bubble is 2 starting radii from the edge of the screen
		int color = rand.nextInt(6) + 1; //from 1 to 6 (no white (0) and black (7))
		long creationTime = TimeUtils.millis();
		float heading = rand.nextInt(360);
		bubbleYs.add(Math.round(currentY));
		bubbleXs.add(Math.round(currentX));
		bubbleColors.add(Math.round(color));
		bubbleCreationTime.add(creationTime);
		bubbleTouched.add(false);
		bubbleRs.add(radius);
		bubbleDistance.add(actualMaxDistance);
		bubbleHeading.add(heading);
		startTimer();
		drawBubble(color, currentX, currentY, radius);
	}

	public void existingBubble(int i) {
		float screenWidth = Gdx.graphics.getWidth();
		float startRadius = screenWidth/startPortion;
		float maxRadius = screenWidth/maxPortion;
		float actualMaxDistance = bubbleDistance.get(i);
		int startY = bubbleYs.get(i);
		int startX = bubbleXs.get(i);
		int color = bubbleColors.get(i);
		long creationTime = bubbleCreationTime.get(i);
		float radius = (float) (((TimeUtils.millis() - creationTime) * (maxRadius - startRadius))/(bubbleLife * 1000)) + startRadius;
		float distance = (float) (((TimeUtils.millis() - creationTime) * actualMaxDistance)/(bubbleLife * 1000));
		float heading = bubbleHeading.get(i);
		float currentY = (float) (startY + (distance * Math.sin(Math.toRadians(heading))));
		float currentX = (float) (startX + (distance * Math.cos(Math.toRadians(heading))));
		bubbleRs.set(i, radius);
		drawBubble(color, currentX, currentY, radius);
	}

	public void drawBubble(int color, float currentX, float currentY, float radius) {
		float R = (float) floor(color/4);
		float G = (float) floor((color % 4)/2);
		float B = (color % 4)%2;
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
                existingBubble(i);
            }
			i++;
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
				bubbleHeading.remove(i);
				bubbleDistance.remove(i);
				i--;
			}
			i++;
		}
		if (bubbleTime) {
			bubbleTime = false;
			newBubble();
			//startTimer();
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
                if (Math.sqrt(Math.pow(screenX - currentX, 2) + Math.pow((Gdx.graphics.getHeight() - screenY) - currentY, 2)) <= radius) {
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
