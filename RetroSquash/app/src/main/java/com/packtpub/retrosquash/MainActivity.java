package com.packtpub.retrosquash;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.io.IOException;
import java.util.Random;


public class MainActivity extends AppCompatActivity {
    Canvas canvas;
    SquashCourtView squashCourtView;

    //Sound
    //initialize sound variables
    private SoundPool soundPool;
    int sample1 = -1;
    int sample2 = -1;
    int sample3 = -1;
    int sample4 = -1;

    //For getting display details like the number of pixels
    Display display;
    Point size;
    int screenWidth;
    int screenHeight;
    //Game objects
    int racketWidth;
    int racketHeight;
    Point racketPosition;

    Point ballPosition;
    int ballWidth;

    //for ball movement
    boolean ballIsMovingLeft;
    boolean ballIsMovingRight;
    boolean ballIsMovingUp;
    boolean ballIsMovingDown;

    //for racket movement
    boolean racketIsMovingLeft;
    boolean racketIsMovingRight;

    //stats
    long lastFrameTime;
    int fps;
    int score;
    int lives;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        squashCourtView = new SquashCourtView(this);

        setContentView(squashCourtView);
        //Sound code
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        try {
            //Create objects of the 2 required classes
            AssetManager assetManager = getAssets();
            AssetFileDescriptor descriptor;

            //create our three fx in memory ready for use
            descriptor = assetManager.openFd("sample1.ogg");
            sample1 = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("sample2.ogg");
            sample2 = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("sample3.ogg");
            sample3 = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("sample4.ogg");
            sample4 = soundPool.load(descriptor, 0);

        } catch (IOException e) {
            //catch exceptions here
        }
        //Could this be an object with getters and setters
        //Don't want just anyone changing screen size.
        //Get the screen size in pixels
        display = getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;

        Log.i("info", "screen size x is " + size.x);
        Log.i("info", "screen size y is " + size.y);


        //The game objects
        racketPosition = new Point();

        racketPosition.x = screenWidth / 2;

        Log.i("info","racket x is " + racketPosition.x);
        racketPosition.y = screenHeight - 800;
        Log.i("info","racket y is " + racketPosition.y);

        racketWidth = screenWidth / 8;


        racketHeight = 10;
        ballWidth = screenWidth / 35;


        ballPosition = new Point();


        ballPosition.x = screenWidth / 2;


        ballPosition.y = 1 + ballWidth;


        lives = 3;
    }

    class SquashCourtView extends SurfaceView implements Runnable {
        Thread ourThread = null;

        SurfaceHolder ourHolder;

        volatile boolean playingSquash;

        Paint paint;

        public SquashCourtView(Context context) {
            super(context);
            ourHolder = getHolder();
            paint = new Paint();
            ballIsMovingDown = true;
            //Send the ball in random direction
            Random randomNumber = new Random();
            int ballDirection = randomNumber.nextInt(3);

            switch (ballDirection) {

                case 0:
                    ballIsMovingLeft = true;
                    ballIsMovingRight = false;
                    break;

                case 1:
                    ballIsMovingRight = true;
                    ballIsMovingLeft = false;
                    break;

                case 2:
                    ballIsMovingLeft = false;
                    ballIsMovingRight = false;
                    break;
            }
        }

        @Override
        public void run() {
            while (playingSquash) {
                updateCourt();
                drawCourt();
                controlFPS();
            }
        }

        public void updateCourt() {

            if (racketIsMovingRight) {
                racketPosition.x = racketPosition.x + 15;
            }

            if (racketIsMovingLeft) {
                racketPosition.x = racketPosition.x - 15;
            }
            //detect collisions
            //hit right of screen
            if (ballPosition.x + ballWidth > screenWidth) {
                ballIsMovingLeft = true;
                ballIsMovingRight = false;
                soundPool.play(sample1, 1, 1, 0, 0, 1);
            }

            //hit left of screen
            if (ballPosition.x < 0) {
                ballIsMovingLeft = false;
                ballIsMovingRight = true;
                soundPool.play(sample1, 1, 1, 0, 0, 1);
            }
            //Edge of ball has hit bottom of screen
            if (ballPosition.y > screenHeight - ballWidth) {
                lives = lives - 1;
                if (lives == 0) {
                    lives = 3;
                    score = 0;
                    soundPool.play(sample4, 1, 1, 0, 0, 1);
                }
                ballPosition.y = 2 + ballWidth;//back to top of screen

                //what horizontal direction should we use
                //for the next falling ball
                Random randomNumber = new Random();
                int startX = randomNumber.nextInt(screenWidth
                        - ballWidth) + 1;
                ballPosition.x = startX + ballWidth;
                int ballDirection = randomNumber.nextInt(3);
                switch (ballDirection) {

                    case 0:
                        ballIsMovingLeft = true;
                        ballIsMovingRight = false;
                        break;

                    case 1:
                        ballIsMovingRight = true;
                        ballIsMovingLeft = false;
                        break;

                    case 2:
                        ballIsMovingLeft = false;
                        ballIsMovingRight = false;
                        break;
                }
            }
            //we hit the top of the screen
            if (ballPosition.y <= 0) {
                ballIsMovingDown = true;
                ballIsMovingUp = false;
                ballPosition.y = 1;
                soundPool.play(sample2, 1, 1, 0, 0, 1);
            }
            //depending upon the two directions we should
//be moving in adjust our x any positions
            if (ballIsMovingDown) {
                ballPosition.y += 6;
            }

            if (ballIsMovingUp) {
                ballPosition.y -= 10;
            }

            if (ballIsMovingLeft) {
                ballPosition.x -= 12;
            }

            if (ballIsMovingRight) {
                ballPosition.x += 12;
            }
//Has ball hit racket
            if (ballPosition.y + ballWidth >= (racketPosition.y - racketHeight / 2)) {

                int halfRacket = racketWidth / 2;


                if (ballPosition.x + ballWidth > (racketPosition.x - halfRacket) && ballPosition.x - ballWidth < (racketPosition.x + halfRacket)) {
                    //rebound the ball vertically and play a sound
                    soundPool.play(sample3, 1, 1, 0, 0, 1);
                    score++;
                    ballIsMovingUp = true;
                    ballIsMovingDown = false;

                    //now decide how to rebound the ball horizontally
                    if (ballPosition.x > racketPosition.x) {
                        ballIsMovingRight = true;
                        ballIsMovingLeft = false;
                    } else {
                        ballIsMovingRight = false;
                        ballIsMovingLeft = true;
                    }
                }
            }
        }
        public void drawCourt() {
            if (ourHolder.getSurface().isValid()) {
                canvas = ourHolder.lockCanvas();

                Paint paint = new Paint();
                canvas.drawColor(Color.BLACK);//the background

                paint.setColor(Color.argb(255, 255, 255, 255));

                paint.setTextSize(45);

                canvas.drawText("Score:" + score + " Lives:" + lives + " fps:" + fps, 20, 40, paint);
                //Draw the squash racket
                canvas.drawRect(racketPosition.x - (racketWidth / 2),
                        racketPosition.y - (racketHeight / 2), racketPosition.x + (racketWidth / 2),
                        racketPosition.y + racketHeight, paint);

                //Draw the ball
                canvas.drawRect(ballPosition.x, ballPosition.y,
                        ballPosition.x + ballWidth,
                        ballPosition.y + ballWidth, paint);

                ourHolder.unlockCanvasAndPost(canvas);
            }
        }
        public void controlFPS() {

            long timeThisFrame = (System.currentTimeMillis() - lastFrameTime);

            long timeToSleep = 15 - timeThisFrame;
            if (timeThisFrame > 0) {
                fps = (int) (1000 / timeThisFrame);
            }


            if (timeToSleep > 0) {

                try {
                    ourThread.sleep(timeToSleep);
                } catch (InterruptedException e) {
                }
            }

            lastFrameTime = System.currentTimeMillis();
        }
        public void pause() {
            playingSquash = false;
            try {
                ourThread.join();
            } catch (InterruptedException e) {
            }
        }

        public void resume() {
            playingSquash = true;
            ourThread = new Thread(this);
            ourThread.start();
        }

    }







    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {

        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                if (motionEvent.getX() >= screenWidth / 2) {
                    racketIsMovingRight = true;
                    racketIsMovingLeft = false;
                } else {
                    racketIsMovingLeft = true;
                    racketIsMovingRight = false;
                }
                break;
            case MotionEvent.ACTION_UP:
                racketIsMovingRight = false;
                racketIsMovingLeft = false;
                break;
        }
        return true;
    }



    @Override
    protected void onStop() {
        super.onStop();
        while (true) {
            squashCourtView.pause();
            break;
        }
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        squashCourtView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        squashCourtView.resume();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            squashCourtView.pause();
            finish();
            return true;
        }
        return false;
    }




}










