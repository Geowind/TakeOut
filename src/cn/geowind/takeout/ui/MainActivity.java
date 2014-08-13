package cn.geowind.takeout.ui;

import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import cn.geowind.takeout.R;
import cn.geowind.takeout.app.App;
import cn.geowind.takeout.app.Constants;
import cn.geowind.takeout.entity.Restaurant;
import cn.geowind.takeout.ui.SettingsActivity.Key;
import cn.geowind.takeout.ui.fragment.HomeFragment;
import cn.geowind.takeout.ui.fragment.ProfileFragment;
import cn.geowind.takeout.ui.fragment.SearchFragment;
import cn.geowind.takeout.ui.fragment.TakeoutFragment;
import cn.geowind.takeout.util.TimeUtil;
import cn.geowind.takeout.util.UpdateManager;
import cn.geowind.takeout.util.Utils;

import com.avos.avoscloud.AVAnalytics;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;

/**
 * 主界面
 * 
 * @author 朱霜
 * @school University of South China
 * @date 2013.09
 */
public class MainActivity extends BaseActivity {
	private static ActionBar mActionBar;
	private FragmentTabHost mFragmentTabHost;
	private LayoutInflater mInflater;
	/* 在每次打开MainActivity的时候判断缓存的Restaurant信息是否过期，如果过期则重新去网上获取 */
	private AVQuery<AVObject> mQuery;

	private String[] tabs = { "推荐", "店铺", "搜索", "我的" };
	private int[] drawables = { R.drawable.tab_home_btn,
			R.drawable.tab_more_btn, R.drawable.tab_square_btn,
			R.drawable.tab_selfinfo_btn };
	private Class<?>[] fragments = { HomeFragment.class, TakeoutFragment.class,
			SearchFragment.class, ProfileFragment.class };

	private long exitTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/*
		 * 打开MainActivity的时候先判断Intent
		 * 中logined属性是否false，不为false表示是从FaceActivity跳转过来的， 不能再跳转到FaceActivity了。
		 * 如果为false的话表示不是从FaceActivity跳转过来的，则判断Preferences里面是否包含"logined"属性或者
		 * "logined"属性是否为false
		 * 如果不包含或者logined属性为false,则依然保存该属性为false，并且跳转到FaceActivity。
		 * 
		 * logined属性表示用户是否登录过，只能在LoginActivity用户登录成功之后把这个属性改为true.
		 */
		// if (getIntent().getBooleanExtra("fromFace", false) == false) {
		// SharedPreferences prf = getSharedPreferences(
		// Constants.PREFS_ACCOUNT, Context.MODE_PRIVATE);
		// if (!prf.contains(Constants.ACCOUNT_LOGINED)
		// || prf.getBoolean(Constants.ACCOUNT_LOGINED, true) == false) {
		// prf.edit().putBoolean(Constants.ACCOUNT_LOGINED, false).apply();
		// startActivity(new Intent(getApplicationContext(),
		// FaceActivity.class));
		// // 同时要Finish掉MainActivity
		// finish();
		// return;
		// }
		// }
		setContentView(R.layout.activity_main);
		mActionBar = getSupportActionBar();
		mActionBar.setDisplayHomeAsUpEnabled(false);
		mActionBar.setDisplayShowHomeEnabled(false);
		mActionBar.setDisplayShowCustomEnabled(false);

		mInflater = getLayoutInflater();
		mFragmentTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
		mFragmentTabHost.setup(this, getSupportFragmentManager(),
				R.id.realtabcontent);
		for (int i = 0; i < fragments.length; i++) {
			mFragmentTabHost.addTab(mFragmentTabHost.newTabSpec(tabs[i])
					.setIndicator(getTabItemView(i)), fragments[i], null);

			mFragmentTabHost.getTabWidget().getChildAt(i)
					.setBackgroundResource(R.drawable.selector_tab_background);
		}

		if (Utils.isNetworkConnected(this)) {
			AVAnalytics.trackAppOpened(getIntent());
			/* 检查新版本 */
			if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
					Key.KEY_AUTO_CHECK_UPDATE, true)) {
				UpdateManager um = new UpdateManager(this);
				um.checkUpdate(true);
			}

			/* 每打开MainActivity就更新一次商家的营业状态、营业时间和kind */
			SharedPreferences prefs = getSharedPreferences(
					Constants.PREFS_RESTAURANT, Context.MODE_PRIVATE);
			final Editor editor = prefs.edit();

			/* 设置为先从缓存获取，如果没有缓存或者缓存超时则去服务器上获取，按注册码排序 */
			mQuery = AVQuery.getQuery(Restaurant.CLASS_NAME);
			mQuery.setCachePolicy(AVQuery.CachePolicy.CACHE_ELSE_NETWORK);
			mQuery.setMaxCacheAge(TimeUtil.TAKEOUT_FRAGMENT);
			mQuery.orderByAscending(Restaurant.REGISTER_CODE);
			mQuery.findInBackground(new FindCallback<AVObject>() {

				@Override
				public void done(List<AVObject> list, AVException e) {
					if (e == null) {
						String id;
						for (AVObject obj : list) {
							id = obj.getObjectId();
							/* 以Json的形式保存到Preferences里面 */
							editor.putString(id, Restaurant.toJson(Restaurant
									.parseRestaurant(obj)));
						}
						editor.apply();
					} else {
						e.printStackTrace();
					}
				}
			});
			/* 判断是否在Wifi状态下 */
			if (Utils.isWifiConnected(this)) {
				Toast.makeText(this, R.string.tips_for_wifi, Toast.LENGTH_SHORT)
						.show();
			} else {
				Toast.makeText(this, R.string.tips_for_non_wifi,
						Toast.LENGTH_SHORT).show();
			}
		}

	}

	private View getTabItemView(int i) {
		View view = mInflater.inflate(R.layout.tab_item_view, null);
		ImageView image = (ImageView) view.findViewById(R.id.tab_item_image);
		image.setImageResource(drawables[i]);
		TextView text = (TextView) view.findViewById(R.id.tab_item_text);
		text.setText(tabs[i]);
		return view;
	}

	/**
	 * 单例模式，给Activity里面的Fragment提供一个公共的方法获取ActionBar
	 * 
	 * @return
	 */
	public static ActionBar getMainActionBar() {
		return mActionBar;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		/* 退出应用程序时将App类里面的currentPagerItem设置为0 */
		App.currentPagerItem = 0;
	}

	@Override
	public void onBackPressed() {
		if (System.currentTimeMillis() - exitTime > 2000) {
			Toast.makeText(this, R.string.press_back_one_more_time,
					Toast.LENGTH_SHORT).show();
			exitTime = System.currentTimeMillis();
		} else {
			super.onBackPressed();
			// System.exit(0);
		}
	}
}
