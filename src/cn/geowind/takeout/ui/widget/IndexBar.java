package cn.geowind.takeout.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * 自定义的View，实现ListView A~Z快速索引效果
 * 
 * @author 朱霜
 * @school University of South China
 * @date 2014.04
 */
public class IndexBar extends View {
	private Paint mPaint = new Paint();
	private OnIndexChangeListener mListener;
	/**
	 * 显示的索引字母
	 */
	private String[] mLetters = { "#", "A", "B", "C", "D", "E", "F", "G", "H",
			"I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U",
			"V", "W", "X", "Y", "Z" };
	/**
	 * 当前选中的索引,从0开始
	 */
	private int mIndex = -1;

	public IndexBar(Context context) {
		super(context);
	}

	public IndexBar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public IndexBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int width = getWidth();
		int height = getHeight();

		int len = mLetters.length;
		int singleHeight = height / len;

		for (int i = 0; i < len; i++) {
			mPaint.setColor(Color.GRAY);
			mPaint.setAntiAlias(true);
			mPaint.setTextSize(20f);

			/**
			 * 如果这一样被选中，则换一种颜色以示区分
			 */
			if (i == mIndex) {
				mPaint.setColor(Color.parseColor("#DD4814"));
				mPaint.setTypeface(Typeface.DEFAULT_BOLD);
			}

			/**
			 * 要画的字母
			 */
			String letter = mLetters[i];
			/**
			 * 计算出要画的字母的坐标
			 */
			float posX = width / 2 - mPaint.measureText(letter) / 2;
			float posY = singleHeight + singleHeight * i;

			/**
			 * 画出字母
			 */
			canvas.drawText(letter, posX, posY, mPaint);
			/**
			 * 重置画笔
			 */
			mPaint.reset();
		}

	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		int action = event.getAction();
		/**
		 * 点击的Y坐标
		 */
		float y = event.getY();
		/**
		 * 算出点击字母的索引
		 */
		mIndex = (int) ((y / getHeight()) * mLetters.length);
		switch (action) {
		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_MOVE:
			if (mListener != null && mIndex > 0 && mIndex < mLetters.length) {
				mListener.onIndexChange(mLetters[mIndex]);
				invalidate();
			}
			break;
		case MotionEvent.ACTION_UP:
			if (mListener != null) {
				if (mIndex <= 0) {
					mListener.onIndexChange("A");
				} else if (mIndex > 0 && mIndex < mLetters.length) {
					mListener.onIndexChange(mLetters[mIndex]);
				} else if (mIndex >= mLetters.length) {
					mListener.onIndexChange("Z");
				}
				 /**
				 * 这个状态不要把mIndex重置为-1，要不然弹起手之后该字母还是高亮色
				 */
				// mIndex = -1;
				invalidate();
			}
			break;
		}
		return true;
	}

	/**
	 * 通知index应该改变
	 * 
	 * @param letter
	 */
	public void notifyIndexSetChanged(String letter) {
		for (int i = 0; i < mLetters.length; i++) {
			if (letter.equals(mLetters[i])) {
				mIndex = i;
				invalidate();
				break;
			}
		}
	}

	/**
	 * 设置监听器
	 * 
	 * @param listener
	 */
	public void setOnIndexChangeListener(OnIndexChangeListener listener) {
		this.mListener = listener;
	}

	/**
	 * 索引变化监听器
	 * 
	 * @author 朱霜
	 * @school University of South China
	 * @date 2014.04
	 */
	public interface OnIndexChangeListener {

		/**
		 * 索引变化时调用这个方法
		 */
		public void onIndexChange(String letter);
	}

}
