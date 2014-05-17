package course.labs.GraphicsLab;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

public class BubbleActivity extends Activity {

	// These variables are for testing purposes, do not modify
	private final static int RANDOM = 0;
	private final static int SINGLE = 1;
	private final static int STILL = 2;
	private static int speedMode = RANDOM;

	private static final int MENU_STILL = Menu.FIRST;
	private static final int MENU_SINGLE_SPEED = Menu.FIRST + 1;
	private static final int MENU_RANDOM_SPEED = Menu.FIRST + 2;

	private static final String TAG = "Lab-Graphics";
	private static final String TAG_DEBUG = "MY-Debug";

	// Main view
	private RelativeLayout mFrame;

	// Bubble image
	private Bitmap mBitmap;

	// Display dimensions
	private int mDisplayWidth, mDisplayHeight;

	// Sound variables

	// AudioManager
	private AudioManager mAudioManager;
	// SoundPool
	private SoundPool mSoundPool;
	// ID for the bubble popping sound
	private int mSoundID;
	// Audio volume
	private float mStreamVolume;

	// Gesture Detector
	private GestureDetector mGestureDetector;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		// Set up user interface
		mFrame = (RelativeLayout) findViewById(R.id.frame);

		// Load basic bubble Bitmap
		mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.b128);

	}

	@Override
	protected void onResume() {
		super.onResume();

		// Manage bubble popping sound
		// Use AudioManager.STREAM_MUSIC as stream type

		mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

		mStreamVolume = (float) mAudioManager
				.getStreamVolume(AudioManager.STREAM_MUSIC)
				/ mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

		// TODO1 - make a new SoundPool, allowing up to 10 streams
		mSoundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);

		// TODO1 - set a SoundPool OnLoadCompletedListener that calls setupGestureDetector()
        mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int i, int status) {
                if (0 == status) {
                    setupGestureDetector();
                }
            }
        });
		
		// TODO1 - load the sound from res/raw/bubble_pop.wav
		mSoundID = mSoundPool.load(this, R.raw.bubble_pop, 1);

	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {

			// Get the size of the display so this view knows where borders are
			mDisplayWidth = mFrame.getWidth();
			mDisplayHeight = mFrame.getHeight();

		}
	}

	// Set up GestureDetector
	private void setupGestureDetector() {

		mGestureDetector = new GestureDetector(this,

		new GestureDetector.SimpleOnGestureListener() {

			// If a fling gesture starts on a BubbleView then change the
			// BubbleView's velocity

			@Override
			public boolean onFling(MotionEvent event1, MotionEvent event2,
					float velocityX, float velocityY) {

				// TODO1 - Implement onFling actions.
				// You can get all Views in mFrame using the
				// ViewGroup.getChildCount() method

                // смотрим, попали ли в начальные координаты какой-нибудь
                // пузырь?
                for(int i = 0; i < mFrame.getChildCount(); i++) {
                    BubbleView bubble = (BubbleView) mFrame.getChildAt(i);

                    float x = event1.getX();
                    float y = event1.getY();

                    // если начало попало в пузырь
                    assert bubble != null;
                    if (bubble.intersects(x, y)) {
                        bubble.deflect(velocityX, velocityY);
                        break;
                    }

                }
				
				
				
				return true;
				
			}

			// If a single tap intersects a BubbleView, then pop the BubbleView
			// Otherwise, create a new BubbleView at the tap's location and add
			// it to mFrame. You can get all views from mFrame with ViewGroup.getChildAt()

			@Override
			public boolean onSingleTapConfirmed(MotionEvent event) {

                // TODO1 - Implement onSingleTapConfirmed actions.
				// You can get all Views in mFrame using the
				// ViewGroup.getChildCount() method

                Log.i(TAG_DEBUG, "Внутри SingleTapConfirmed");

                float x = event.getX();
                float y = event.getY();

                boolean needCreate = true;

                // проходим по всем пузырям
                for(int i = 0; i < mFrame.getChildCount(); i++) {
                    BubbleView bubble = (BubbleView) mFrame.getChildAt(i);

                    // Проверяем, нет ли по указанным координатам пузыря
                    assert bubble != null;
                    if (bubble.intersects(x, y)) {
                        bubble.stop(true);
                        needCreate = false;
//                        break;
                    }
                }

                if (needCreate) {
                    BubbleView bubbleView = new BubbleView(getApplicationContext(), x, y);
                    bubbleView.start();
                    mFrame.addView(bubbleView);
                }

                return true;
			}
		});
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

        Log.i(TAG_DEBUG, "Entered onTouchEvent");

		// TODO1 - delegate the touch to the gestureDetector
        return  mGestureDetector.onTouchEvent(event);

	}

	@Override
	protected void onPause() {
		
		// TODO1 - Release all SoundPool resources
        if (null != mSoundPool) {
            mSoundPool.unload(mSoundID);
            mSoundPool.release();
            mSoundPool = null;
        }

		super.onPause();
	}

	// BubbleView is a View that displays a bubble.
	// This class handles animating, drawing, popping amongst other actions.
	// A new BubbleView is created for each bubble on the display

	private class BubbleView extends View {

		private static final int BITMAP_SIZE = 64;
		private static final int REFRESH_RATE = 40;
		private final Paint mPainter = new Paint();
		private ScheduledFuture<?> mMoverFuture;
		private int mScaledBitmapWidth;
		private Bitmap mScaledBitmap;

		// location, speed and direction of the bubble
		private float mXPos, mYPos, mDx, mDy;
		private long mRotate, mDRotate;

		public BubbleView(Context context, float x, float y) {
			super(context);
			log("Creating Bubble at: x:" + x + " y:" + y);

			// Create a new random number generator to
			// randomize size, rotation, speed and direction
			Random r = new Random();

			// Creates the bubble bitmap for this BubbleView
			createScaledBitmap(r);

			// Adjust position to center the bubble under user's finger
			mXPos = x - mScaledBitmapWidth / 2;
			mYPos = y - mScaledBitmapWidth / 2;

			// Set the BubbleView's speed and direction
			setSpeedAndDirection(r);
			
			// Set the BubbleView's rotation
			setRotation(r);

			mPainter.setAntiAlias(true);

		}

		private void setRotation(Random r) {

			if (speedMode == RANDOM) {
				
				// TODO1 - set rotation in range [1..3]
				mDRotate = r.nextInt(2) + 1;

				
			} else {
			
				mDRotate = 0;
			
			}
		}

		private void setSpeedAndDirection(Random r) {

			// Used by test cases
			switch (speedMode) {

			case SINGLE:

				// Fixed speed
				mDx = 10;
				mDy = 10;
				break;

			case STILL:

				// No speed
				mDx = 0;
				mDy = 0;
				break;

			default:

				// TODO1 - Set movement direction and speed
				// Limit movement speed in the x and y
				// direction to [-3..3].
                mDx = -3 + r.nextInt(7);
                mDy = -3 + r.nextInt(7);

			
			
			}
		}

		private void createScaledBitmap(Random r) {

			if (speedMode != RANDOM) {

				mScaledBitmapWidth = BITMAP_SIZE * 3;
			
			} else {
			
				//TODO1 - set scaled bitmap size in range [1..3] * BITMAP_SIZE
				mScaledBitmapWidth = BITMAP_SIZE * (r.nextInt(3) + 1);
			
			}

			// TODO1 - create the scaled bitmap using size set above
            //noinspection SuspiciousNameCombination
			mScaledBitmap = Bitmap.createScaledBitmap(mBitmap, mScaledBitmapWidth, mScaledBitmapWidth,
                    false);
		}

		// Start moving the BubbleView & updating the display
		private void start() {

			// Creates a WorkerThread
			ScheduledExecutorService executor = Executors
					.newScheduledThreadPool(1);

			// Execute the run() in Worker Thread every REFRESH_RATE
			// milliseconds
			// Save reference to this job in mMoverFuture
			mMoverFuture = executor.scheduleWithFixedDelay(new Runnable() {
				@Override
				public void run() {
					// TODO1 - implement movement logic.
					// Each time this method is run the BubbleView should
					// move one step. If the BubbleView exits the display, 
					// stop the BubbleView's Worker Thread. 
					// Otherwise, request that the BubbleView be redrawn. 
                    if (!moveWhileOnScreen()) {
                        // Пузырь ещё на экране, и его надо перерисовать
                        postInvalidate();
                    } else {
                        stop(false);
                        mMoverFuture.cancel(true);
                    }

					
				}
			}, 0, REFRESH_RATE, TimeUnit.MILLISECONDS);
		}

		private synchronized boolean intersects(float x, float y) {

           	// TODO1 - Return true if the BubbleView intersects position (x,y)

            // проверяем, попадают ли координаты в окружность с центром
            // текущего пузыря с радиусом в половину размера
            // (x-center_x)^2 + (y - center_y)^2 < radius^2
            float radius = mScaledBitmapWidth / 2;
            float centerX = mXPos + radius;
            float centerY = mYPos + radius;

            float a = x - centerX;
            float b = y - centerY;

            boolean isInside = Math.pow(a, 2) + Math.pow(b, 2) < Math.pow(radius, 2);

            Log.i(TAG_DEBUG, "Внутри? " + isInside);

            return isInside;
		}

		// Cancel the Bubble's movement
		// Remove Bubble from mFrame
		// Play pop sound if the BubbleView was popped
		
		private void stop(final boolean popped) {

			if (null != mMoverFuture && mMoverFuture.cancel(true)) {

				// This work will be performed on the UI Thread
				
				mFrame.post(new Runnable() {
					@Override
					public void run() {
						
						// TODO1 - Remove the BubbleView from mFrame
                        mFrame.removeView(BubbleView.this);

						if (popped) {
							log("Pop!");

							// TODO1 - If the bubble was popped by user,
							// play the popping sound
                            mSoundPool.play(mSoundID, mStreamVolume, mStreamVolume, 1, 0, 1.0f);
						
						}

						log("Bubble removed from view!");
					
					}
				});
			}
		}

		// Change the Bubble's speed and direction
		private synchronized void deflect(float velocityX, float velocityY) {
			log("velocity X:" + velocityX + " velocity Y:" + velocityY);

			//TODO - set mDx and mDy to be the new velocities divided by the REFRESH_RATE
			
			mDx = velocityX / REFRESH_RATE;
            mDy = velocityY / REFRESH_RATE;
		}

		// Draw the Bubble at its current location
		@Override
		protected synchronized void onDraw(Canvas canvas) {

			// TODO1 - save the canvas
            canvas.save();

			// TODO1 - increase the rotation of the original image by mDRotate
            mRotate += mDRotate;

			// TODO1 Rotate the canvas by current rotation
            canvas.rotate(mRotate, mXPos + mScaledBitmapWidth/2, mYPos + mScaledBitmapWidth/2);

			
			// TODO1 - draw the bitmap at it's new location
            canvas.drawBitmap(mScaledBitmap, mXPos, mYPos, mPainter);

			
			// TODO1 - restore the canvas
            canvas.restore();

			
		}


		private synchronized boolean moveWhileOnScreen() {

			// TODO1 - Move the BubbleView

            // двигаем и потом смотрим, пропали ли с экрана
            mXPos += mDx;
            mYPos += mDy;

			// Returns true if the BubbleView has exited the screen
            return isOutOfView();

		}

		private boolean isOutOfView() {

			// TODO1 - Return true if the BubbleView has exited the screen
            boolean isOutX = 0 > mXPos + mScaledBitmapWidth || mXPos > mDisplayWidth;
            boolean isOutY = 0 > mYPos + mScaledBitmapWidth || mYPos > mDisplayHeight;

			return isOutX || isOutY;

		}
	}

	// Do not modify below here
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(Menu.NONE, MENU_STILL, Menu.NONE, "Still Mode");
		menu.add(Menu.NONE, MENU_SINGLE_SPEED, Menu.NONE, "Single Speed Mode");
		menu.add(Menu.NONE, MENU_RANDOM_SPEED, Menu.NONE, "Random Speed Mode");

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_STILL:
			speedMode = STILL;
			return true;
		case MENU_SINGLE_SPEED:
			speedMode = SINGLE;
			return true;
		case MENU_RANDOM_SPEED:
			speedMode = RANDOM;
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private static void log (String message) {
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Log.i(TAG,message);
	}
}