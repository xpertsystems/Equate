package com.llamacorp.equate.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.llamacorp.equate.R;

public class AnimatedHoldButton extends SecondaryTextButton {
	protected static final int SECONDARY_FONT_PERCENTAGE = 85;
	private static final int CLICK_HOLD_TIME = 300;	

	protected float mSecAdditionalXOffset = getContext().getResources().
			getDimensionPixelSize(R.dimen.button_secondary_text_additional_offset_x);
	protected float mSecAdditionalYOffset = getContext().getResources().
			getDimensionPixelSize(R.dimen.button_secondary_text_additional_offset_y);

	
	private OnClickListener mClickListen = null;
	private OnLongClickListener mLongClickListen = null;
	private Handler mColorHoldHandler;
	private boolean mLongClickPerformed=false;
   private Drawable mNormalDrawable;
   private int mPressedColor;

	//used to count up holding time
	private int mHoldInc;
	
	private String mPrimaryText;


	public AnimatedHoldButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		mPrimaryText = "";

      mNormalDrawable = getBackground();

		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.AnimatedHoldButton, 0, 0);
		try {
			mPrimaryText = ta.getString(R.styleable.AnimatedHoldButton_primary_text);
         mPressedColor = ta.getColor(R.styleable.AnimatedHoldButton_pressed_color,
                 getResources().getColor(R.color.op_button_pressed));
		} finally { ta.recycle();}

		//this is needed for so paint knows what to measure in layoutText
		setText(mPrimaryText);
	}

	@Override
	protected void findSecondaryTextCoord(){
		mSecXCoord = mButtonWidth - mSecTextWidth - mSecAdditionalXOffset;
		mSecYCoord = 0 + mSecTextHeight + mSecAdditionalYOffset;
	}

	/** Setup custom clicking and long clicking handlers
	 * Click will be performed if button is pressed down and released
	 * before the long click timeout.  As the long click timeout is 
	 * expiring, button's color will change and finally flash at the timeout
	 * event, at which point the long click function will be called. */
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mHoldInc=0;
			mLongClickPerformed = false;

			if (mColorHoldHandler != null) return true;
			mColorHoldHandler = new Handler();
			mColorHoldHandler.postDelayed(mColorRunnable, 10);
			break;
		case MotionEvent.ACTION_UP:
			if (mColorHoldHandler == null) return true;
			if(!mLongClickPerformed) 
				clickButton();

			setBackground(mNormalDrawable);
			mColorHoldHandler.removeCallbacks(mColorRunnable);
			mColorHoldHandler = null;

			break;
		}
		return true;
	}



	//set up the runnable for when button is held down
	Runnable mColorRunnable = new Runnable() {
		private static final int NUM_COLOR_CHANGES=10;

		@Override 
		public void run() {
         int mGradStartCol = mPressedColor;
         int mGradEndCol = getResources().getColor(R.color.op_button_long_press_accent);
         int mAccentColor = mPressedColor;
         int mFinalColor = mPressedColor;

         //after hold operation has been performed and 100ms is up, set final color
			if(mHoldInc==-1){
				setBackgroundColor(mFinalColor);
				return;
			}
			//color the button black for a second and perform long click operation
			if(mHoldInc==NUM_COLOR_CHANGES){
				longClickButton();
				mLongClickPerformed = true;
				setBackgroundColor(mAccentColor);
				//only post again so it runs to catch the final bit of code
				mColorHoldHandler.postDelayed(this, 100);
				mHoldInc=-1;
				return;
			}
			mColorHoldHandler.postDelayed(this, CLICK_HOLD_TIME/NUM_COLOR_CHANGES);

			float deltaRed= (float)Color.red(mGradStartCol) + ((float)Color.red(mGradEndCol)-(float)Color.red(mGradStartCol))*((float)mHoldInc)/((float)NUM_COLOR_CHANGES);
			float deltaGreen= (float)Color.green(mGradStartCol) + ((float)Color.green(mGradEndCol)-(float)Color.green(mGradStartCol))*((float)mHoldInc)/((float)NUM_COLOR_CHANGES);
			float deltaBlue= (float)Color.blue(mGradStartCol) + ((float)Color.blue(mGradEndCol)-(float)Color.blue(mGradStartCol))*((float)mHoldInc)/((float)NUM_COLOR_CHANGES);

			setBackgroundColor(Color.argb(255, (int)deltaRed, (int)deltaGreen, (int)deltaBlue));
			mHoldInc++;
		}
	};		

	/** Calls listener's onClick, which gets setup by code controlling button */
	private void clickButton(){
		if(mClickListen != null)
			mClickListen.onClick(this);
	}
	
	/** Calls listener's onLongClick, which gets setup by code controlling button */
	private void longClickButton(){
		if(mLongClickListen != null)
			mLongClickListen.onLongClick(this);
	}

	@Override
	protected String getPrimaryText(){
		if(mPrimaryText==null)
			return "";
		return mPrimaryText;
	}
	
	@Override
	public void setOnClickListener(OnClickListener l){
		mClickListen = l;
	}

	@Override
	public void setOnLongClickListener(OnLongClickListener l){
		mLongClickListen = l;
	}
}

