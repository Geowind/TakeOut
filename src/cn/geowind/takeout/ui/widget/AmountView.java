package cn.geowind.takeout.ui.widget;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import cn.geowind.takeout.R;

/**
 * 自定义UI控件，记录菜的份数
 * 
 * @author 朱霜
 * @school University of South China
 * @date 2014.01/2014.03
 */
public class AmountView extends TextView implements OnItemClickListener {
	private Context mContext;
	private OnSelectListener mListener;
	/**
	 * 最大的份数
	 */
	private int mMaxAmount = 5;
	/**
	 * 当前显示的份数
	 */
	private int mAmount = 1;
	private PopupWindow mPopup;;
	private ListView mListView;
	private ArrayAdapter<Integer> mAdapter;
	private Integer[] data = { 1, 2, 3, 4, 5 };

	public AmountView(Context context) {
		super(context);
		mContext = context;
		setText(mAmount + "");
	}

	public AmountView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		setText(mAmount + "");
	}

	public AmountView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		setText(mAmount + "");
	}

	// @Override
	// public boolean dispatchTouchEvent(MotionEvent event) {
	// if (event.getAction() == MotionEvent.ACTION_UP) {
	// mAmount = mAmount % mMaxAmount + 1;
	// setText(mAmount + "");
	// System.out.println("AmoutView dispatchTouchEvent");
	// }
	// return super.dispatchTouchEvent(event);
	// }

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_UP) {
			// mAmount = mAmount % mMaxAmount + 1;
			// setText(mAmount + "");
			if (mPopup == null) {
				/**
				 * 初始化PopupWindow
				 */
				LayoutInflater inflater = LayoutInflater.from(mContext);
				mListView = (ListView) inflater.inflate(
						R.layout.pop_amount_view_list, null, false);
				mAdapter = new ArrayAdapter<Integer>(mContext,
						R.layout.pop_amount_view_list_item, data);
				mListView.setAdapter(mAdapter);
				mListView.setOnItemClickListener(this);
				mPopup = new PopupWindow(mListView, this.getWidth(),
						LayoutParams.WRAP_CONTENT, true);
				mPopup.setTouchable(true);
				mPopup.setOutsideTouchable(true);
				mPopup.setBackgroundDrawable(new BitmapDrawable());
			}
			mPopup.showAsDropDown(this);
		}
		return super.onTouchEvent(event);
	}

	public void setMaxAmount(int maxAmount) {
		mMaxAmount = maxAmount;
		for (int i = 0; i < mMaxAmount; i++) {
			data[i] = i + 1;
		}
	}

	public void setAmount(int amount) {
		mAmount = amount;
		setText("" + mAmount);
	}

	public int getAmount() {
		return mAmount;
	}

	public void setOnSelectListener(OnSelectListener listener) {
		mListener = listener;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		mAmount = data[position];
		setText(mAmount + "");
		if (mListener != null) {
			mListener.onSelect(this, mAmount);
		}
		mPopup.dismiss();
	}

	/**
	 * 
	 * @author 朱霜
	 * @school University of South China
	 * @date 2014.03
	 */
	public interface OnSelectListener {
		public void onSelect(View view, int amount);
	}

}
