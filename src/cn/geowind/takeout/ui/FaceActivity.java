package cn.geowind.takeout.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import cn.geowind.takeout.R;
import cn.geowind.takeout.app.App;
import cn.geowind.takeout.app.Constants;

/**
 * 欢迎引导界面的Activity
 * 
 * @author 朱霜
 * @school University of South China
 * @date 2014.04
 */
public class FaceActivity extends Activity implements OnClickListener,
		OnPageChangeListener {
	private Resources mRes;
	private ViewPager mViewPager;
	private Button mFaceOpen;
	private LinearLayout mIndicator;
	private ImageView[] indicators;
	private LayoutInflater mInflater;
	private static final int[] mImages = { R.drawable.a, R.drawable.b,
			R.drawable.c, R.drawable.d };
	private boolean isLogined = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_face);
		mRes = getResources();
		mViewPager = (ViewPager) findViewById(R.id.viewpager);
		mFaceOpen = (Button) findViewById(R.id.face_open);
		mIndicator = (LinearLayout) findViewById(R.id.face_indicator);
		mInflater = getLayoutInflater();

		mViewPager.setAdapter(new FacePagerAdapter());
		mViewPager.setOnPageChangeListener(this);
		renderIndicators(mImages.length);

		mFaceOpen.setOnClickListener(this);

		isLogined = getSharedPreferences(Constants.PREFS_ACCOUNT,
				Context.MODE_PRIVATE).getBoolean("logined", false);
	}

	/**
	 * 
	 * @param count
	 */
	protected void renderIndicators(int count) {
		indicators = new ImageView[count];
		for (int i = 0; i < count; i++) {
			indicators[i] = new ImageView(this);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					15, 15);
			params.setMargins(5, 0, 5, 0);
			indicators[i].setLayoutParams(params);
			if (i == App.currentPagerItem) {
				indicators[i]
						.setBackgroundResource(R.drawable.ic_pager_indicator_curr);
			} else {
				indicators[i]
						.setBackgroundResource(R.drawable.ic_pager_indicator);
			}
			mIndicator.addView(indicators[i]);
		}
	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent();
		int id = v.getId();
		if (id == R.id.face_open) {
			intent.setClass(this, MainActivity.class);
		}
		/**
		 * 传递一个boolean值到MainActivity， 这样跳转到MainActivity可以判断是否是从FaceActivity跳转过来的
		 */
		intent.putExtra("fromFace", true);
		startActivity(intent);
		finish();

	}

	@Override
	public void onPageScrollStateChanged(int state) {

	}

	@Override
	public void onPageScrolled(int position, float positionOffset,
			int positionOffsetPixels) {

	}

	@Override
	public void onPageSelected(int position) {
		for (int i = 0; i < mImages.length; i++) {
			if (i == position) {
				indicators[i]
						.setBackgroundResource(R.drawable.ic_pager_indicator_curr);
			} else {
				indicators[i]
						.setBackgroundResource(R.drawable.ic_pager_indicator);
			}
		}
		if (mImages.length - 1 == position) {
			/**
			 * 如果用户还没有登录过，则显示这些按钮，否则不显示这些按钮
			 */
			if (!isLogined) {
				mFaceOpen.postDelayed(new Runnable() {

					@Override
					public void run() {
						mFaceOpen.setVisibility(View.VISIBLE);
					}
				}, 300);
			}
		} else {
			mFaceOpen.setVisibility(View.GONE);
		}
	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
	}

	class FacePagerAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return mImages.length;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object view) {
			((ViewPager) container).removeView((ImageView) view);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			View view = mInflater.inflate(R.layout.face_item, null, false);
			ImageView img = (ImageView) view.findViewById(R.id.img);
			// img.setImageResource(mImages[position]);// bug
			img.setImageDrawable(mRes.getDrawable(mImages[position]));
			// img.setImageBitmap(BitmapFactory.decodeResource(mRes,
			// mImages[position]));
			// ((ViewPager) container).addView(view, 0);
			((ViewPager) container).addView(view);
			return view;
		}
	}

}
