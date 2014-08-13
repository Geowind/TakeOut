package cn.geowind.takeout.ui.fragment;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.geowind.takeout.R;
import cn.geowind.takeout.app.App;
import cn.geowind.takeout.entity.Pager;
import cn.geowind.takeout.ui.AboutActivity;
import cn.geowind.takeout.ui.FeedbackActivity;
import cn.geowind.takeout.ui.MainActivity;
import cn.geowind.takeout.ui.SettingsActivity;
import cn.geowind.takeout.ui.WebActivity;
import cn.geowind.takeout.ui.adapter.ViewPagerAdapter;
import cn.geowind.takeout.util.TimeUtil;
import cn.geowind.takeout.util.ToastUtil;
import cn.geowind.takeout.util.Utils;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVInstallation;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVPush;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.SendCallback;
import com.avos.avoscloud.AVQuery.CachePolicy;
import com.avos.avoscloud.CountCallback;
import com.avos.avoscloud.LogUtil.log;

/**
 * 推荐页Fragment
 * 
 * @author 朱霜
 * @school University of South China
 * @date 2013.09
 * @see MainActivity
 */
public class HomeFragment extends BaseFragment implements OnPageChangeListener,
		OnClickListener {
	private FragmentActivity mActivity;
	private ActionBar actionBar;
	private static ViewPager viewPager;
	private LinearLayout pagerIndicator;
	private ImageView[] indicators;
	private ViewPagerAdapter viewPagerAdapter;

	private TextView mRecommendTv;
	private TextView mShakeFoodTv;

	private ViewPager mFragmentViewPager;
	private FragmentAdapter mFragmentAdapter;

	private static int pagerCurrentItem = 0;
	/**
	 * 并发工具结合Handler实现ViewPager的自动切换
	 */
	private ScheduledExecutorService ses;
	private static Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			viewPager.setCurrentItem(pagerCurrentItem);
		}
	};

	private AVQuery<AVObject> mQuery;
	private int count;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mQuery = AVQuery.getQuery(Pager.CLASS_NAME);
		mQuery.setCachePolicy(CachePolicy.CACHE_ELSE_NETWORK);
		mQuery.setMaxCacheAge(TimeUtil.VIEWPAGER_COUNT);
		/* 调用这个方法就可以在Fragment控制ActionBar显示菜单 */
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_home, container, false);
		mActivity = getActivity();
		/* 初始化嵌套的Fragment */
		mFragmentViewPager = (ViewPager) view
				.findViewById(R.id.fragment_container);

		viewPager = (ViewPager) view.findViewById(R.id.viewpager);
		pagerIndicator = (LinearLayout) view.findViewById(R.id.pager_indicator);

		if (mQuery.hasCachedResult() || Utils.isNetworkConnected(mActivity)) {
			mQuery.countInBackground(new CountCallback() {

				@Override
				public void done(int size, AVException e) {
					if (e == null) {
						count = size;
						viewPagerAdapter = new ViewPagerAdapter(mActivity,
								count);
						viewPager.setOffscreenPageLimit(2);
						viewPager.setAdapter(viewPagerAdapter);
						renderIndicators(count);
					} else {
						log.e(getTag(), "获取Pager的页数异常", e);
					}
				}
			});
		} else {
			ToastUtil.toast(mActivity, ToastUtil.NO_NETWORK);
		}
		viewPager.setOnPageChangeListener(this);
		/* 当Fragment可见的时候开始自动切换 */
		ses = Executors.newSingleThreadScheduledExecutor();
		ses.scheduleAtFixedRate(new ScrollTask(), 5, 5, TimeUnit.SECONDS);

		mRecommendTv = (TextView) view.findViewById(R.id.index_recommend);
		mShakeFoodTv = (TextView) view.findViewById(R.id.index_shake_food);
		mRecommendTv.setOnClickListener(this);
		mShakeFoodTv.setOnClickListener(this);

		mFragmentAdapter = new FragmentAdapter(getChildFragmentManager());
		mFragmentViewPager.setAdapter(mFragmentAdapter);
		mFragmentViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			int defaultColor = mShakeFoodTv.getTextColors().getDefaultColor();
			int mainColor = getResources().getColor(R.color.main_color);

			@Override
			public void onPageSelected(int position) {
				if (position == 0) {
					mRecommendTv.setTextColor(mainColor);
					mShakeFoodTv.setTextColor(defaultColor);
				} else {
					mShakeFoodTv.setTextColor(mainColor);
					mRecommendTv.setTextColor(defaultColor);
				}
			}

			@Override
			public void onPageScrolled(int position, float positionOffset,
					int positionOffsetPixels) {
			}

			@Override
			public void onPageScrollStateChanged(int state) {
			}
		});
		return view;
	}

	protected void renderIndicators(int count) {
		indicators = new ImageView[count];
		for (int i = 0; i < count; i++) {
			indicators[i] = new ImageView(mActivity);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					10, 10);
			params.setMargins(3, 0, 3, 0);
			indicators[i].setLayoutParams(params);
			if (i == App.currentPagerItem) {
				indicators[i]
						.setBackgroundResource(R.drawable.ic_pager_indicator_curr);
			} else {
				indicators[i]
						.setBackgroundResource(R.drawable.ic_pager_indicator);
			}
			pagerIndicator.addView(indicators[i]);
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		actionBar = MainActivity.getMainActionBar();
		actionBar.setTitle(getResources().getString(R.string.app_name));
		actionBar.setDisplayShowCustomEnabled(false);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.action_main, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = null;
		switch (item.getItemId()) {
		case R.id.action_settins:
			intent = new Intent(mActivity, SettingsActivity.class);
			startActivity(intent);
			break;
		case R.id.action_feedback:
			intent = new Intent(mActivity, FeedbackActivity.class);
			startActivity(intent);
			break;
		case R.id.action_about:
			intent = new Intent(mActivity, AboutActivity.class);
			startActivity(intent);
			break;
		case R.id.action_cooperation:
           //3bff5c39-c114-418a-8a9e-3acf1decbf96
			AVQuery<AVInstallation> query = AVInstallation.getQuery();
			query.whereEqualTo("installationId", "3bff5c39-c114-418a-8a9e-3acf1decbf96");
			AVPush.sendMessageInBackground("來自外賣小助手用戶版的推送。", query, new SendCallback() {
				
				@Override
				public void done(AVException e) {
					if(e == null){
						Toast.makeText(mActivity, "推送成功", Toast.LENGTH_LONG).show();
					}else{
						Toast.makeText(mActivity, "推送失败 ", Toast.LENGTH_LONG).show();
						e.printStackTrace();
					}
				}
			});
			intent = new Intent(mActivity, WebActivity.class);
			intent.putExtra(Pager.TITLE,
					getResources().getString(R.string.action_cooperation));
			intent.putExtra(Pager.DATA, "https://jinshuju.net/f/EiUPb5");
			startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		/*
		 * 当切换到其他Fragment时，记录下当前ViewPager的索引到App.currentPagerItem,
		 * 以便于切换回来时还是显示当前的这一项Pager
		 */
		App.currentPagerItem = viewPager.getCurrentItem();
		/* 当Fragment不可见的时候，停止自动切换 */
		ses.shutdown();
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.index_recommend:
			mFragmentViewPager.setCurrentItem(0);
			break;
		case R.id.index_shake_food:
			mFragmentViewPager.setCurrentItem(1);
			break;
		}
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
		for (int i = 0; i < count; i++) {
			if (i == position) {
				indicators[i]
						.setBackgroundResource(R.drawable.ic_pager_indicator_curr);
			} else {
				indicators[i]
						.setBackgroundResource(R.drawable.ic_pager_indicator);
			}
		}
	}

	/**
	 * 执行切换任务
	 */
	private class ScrollTask implements Runnable {

		@Override
		public void run() {
			synchronized (viewPager) {
				pagerCurrentItem = (pagerCurrentItem + 1) % count;
				handler.obtainMessage().sendToTarget();
			}
		}
	}

	class FragmentAdapter extends FragmentPagerAdapter {
		final int SIZE = 2;

		public FragmentAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			if (position == 0) {
				return new RecommendFragment();
			} else {
				return new ShakeFoodFragment();
			}

		}

		@Override
		public int getCount() {
			return SIZE;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			super.destroyItem(container, position, object);

		}
	}
}