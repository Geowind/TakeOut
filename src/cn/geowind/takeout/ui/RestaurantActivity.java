package cn.geowind.takeout.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.geowind.takeout.R;
import cn.geowind.takeout.app.App;
import cn.geowind.takeout.app.Constants;
import cn.geowind.takeout.entity.Comment;
import cn.geowind.takeout.entity.Food;
import cn.geowind.takeout.entity.Restaurant;
import cn.geowind.takeout.entity.Restaurant.Status;
import cn.geowind.takeout.ui.fragment.MenuFragment;
import cn.geowind.takeout.ui.widget.LoadingDialog;
import cn.geowind.takeout.util.ToastUtil;
import cn.geowind.takeout.util.Utils;

import com.avos.avoscloud.AVAnalytics;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.CountCallback;
import com.viewpagerindicator.TabPageIndicator;

/**
 * 外卖店详情页
 * 
 * @author 朱霜
 * @school University of South China
 * @date 2013.09/2014.04/2014.06
 */
public class RestaurantActivity extends BaseActivity implements OnClickListener {
	private String restaurantId;
	private String restaurantName;

	private DrawerLayout mDrawerLayout;
	private RelativeLayout drawer;
	private LoadingDialog mDialog;

	private ActionBar mActionBar;
	private ViewPager mViewPager;
	private TabPageIndicator mIndicator;
	private MenuPagerAdapter mPagerAdapter;
	private Restaurant mRestaurant;
	private AVQuery<AVObject> commentQuery;

	private boolean isWifiConnected = false;
	/**
	 * Restaurant对应的kind属性
	 */
	private String kind = null;
	private String[] pageTitles = { "7块以下", "8-10块", "11-15块", "16块以上" };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_restaurant);

		Intent intent = getIntent();
		restaurantId = intent.getStringExtra(Restaurant.OBJECT_ID);
		restaurantName = intent.getStringExtra(Restaurant.NAME);

		mActionBar = getSupportActionBar();
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setDisplayShowHomeEnabled(false);
		mActionBar.setTitle(restaurantName);
		mDialog = new LoadingDialog(this, getResources().getString(
				R.string.trying_to_loading));

		drawer = (RelativeLayout) findViewById(R.id.drawer);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerlayout);
		mDrawerLayout.setDrawerShadow(R.drawable.ic_drawer_shadow,
				GravityCompat.END);

		mPagerAdapter = new MenuPagerAdapter(getSupportFragmentManager());
		mViewPager = (ViewPager) findViewById(R.id.viewpager);
		mIndicator = (TabPageIndicator) findViewById(R.id.indicator);

		mDialog.show();

		/* 因为在MainActivity中缓存了Restaurant的数据，所以直接从Preferences中获取 */
		mRestaurant = Restaurant.fromJson(getSharedPreferences(
				Constants.PREFS_RESTAURANT, Context.MODE_PRIVATE).getString(
				restaurantId, ""));
		kind = mRestaurant.kind;
		mViewPager.setAdapter(mPagerAdapter);
		mIndicator.setViewPager(mViewPager);
		performInit();

		if (Utils.isNetworkConnected(this)) {
			/* 先判断是否在Wifi状态下 */
			isWifiConnected = Utils.isWifiConnected(this);

			commentQuery = AVQuery.getQuery(Comment.CLASS_NAME);
			commentQuery.whereEqualTo(Comment.RESTAURANT_ID, restaurantId);
			commentQuery.countInBackground(new CountCallback() {

				@Override
				public void done(int size, AVException e) {
					if (e == null) {
						TextView comment = (TextView) findViewById(R.id.drawer_comments);
						if (size == 0) {
							comment.setText(R.string.comment_empty);
						} else {
							comment.setText("已有" + size + "条评论");
						}
						comment.setOnClickListener(RestaurantActivity.this);
					}
				}
			});
			mDialog.dismiss();
		} else {
			mDialog.dismiss();
			ToastUtil.toast(this, ToastUtil.NO_NETWORK);
		}
	}

	/**
	 * 执行更新UI操作
	 */
	private void performInit() {
		String area = "[" + mRestaurant.area + "]";
		if (mRestaurant.getStatus() == Status.OK) {
			mActionBar.setSubtitle("营业中" + area);
		} else {
			mActionBar.setSubtitle("休息中" + area);
		}
		initDrawer();
	}

	/**
	 * 初始化Drawer
	 */
	private void initDrawer() {
		((TextView) findViewById(R.id.drawer_restaurant_name))
				.setText(restaurantName);
		((TextView) findViewById(R.id.drawer_restaurant_details))
				.setText(mRestaurant.details);
		((TextView) findViewById(R.id.drawer_restaurant_keywords))
				.setText(mRestaurant.keywords);
		((TextView) findViewById(R.id.drawer_restaurant_tel1))
				.setText(mRestaurant.tel1);
		((TextView) findViewById(R.id.drawer_restaurant_tel2))
				.setText(mRestaurant.tel2);
		((TextView) findViewById(R.id.drawer_restaurant_business_time))
				.setText(mRestaurant.businessTime);
		((TextView) findViewById(R.id.drawer_restaurant_others))
				.setText(mRestaurant.others);
		((TextView) findViewById(R.id.drawer_restaurant_address))
				.setText(mRestaurant.address);
		((ImageButton) findViewById(R.id.drawer_restaurant_call_btn1))
				.setOnClickListener(this);
		((ImageButton) findViewById(R.id.drawer_restaurant_call_btn2))
				.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.drawer_comments) {
			Intent intent = new Intent(this, RestaurantCommentsActivity.class);
			intent.putExtra(Comment.RESTAURANT, restaurantName);
			intent.putExtra(Comment.RESTAURANT_ID, restaurantId);
			startActivity(intent);
		} else if (id == R.id.drawer_restaurant_call_btn1) {
			AVAnalytics.onEvent(this, App.Event.CALL);
			Uri uri = Uri.parse("tel:" + mRestaurant.tel1);
			Intent intent = new Intent(Intent.ACTION_DIAL, uri);
			startActivity(intent);
		} else if (id == R.id.drawer_restaurant_call_btn2) {
			AVAnalytics.onEvent(this, App.Event.CALL);
			Uri uri = Uri.parse("tel:" + mRestaurant.tel2);
			Intent intent = new Intent(Intent.ACTION_DIAL, uri);
			startActivity(intent);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.action_restaurant, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		case R.id.action_drawer:
			if (mDrawerLayout.isDrawerOpen(drawer)) {
				mDrawerLayout.closeDrawer(drawer);
			} else {
				mDrawerLayout.openDrawer(drawer);
				AVAnalytics.onEvent(this, App.Event.DRAWER);
			}
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	class MenuPagerAdapter extends FragmentPagerAdapter {

		public MenuPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			MenuFragment menuFragment = new MenuFragment();
			Bundle bundle = new Bundle();
			bundle.putString(Food.RESTURANT_ID, restaurantId);
			bundle.putBoolean("wifi", isWifiConnected);
			/* 如果是第0项的话返回的内容为推荐菜，否则返回价位菜 */
			if (0 == position) {
				bundle.putBoolean("special", true);
			} else {
				/* 否则把价位传到Fragment里面去 */
				bundle.putBoolean("special", false);
				bundle.putInt(Food.RANK,
						Integer.valueOf(kind.substring(position - 1, position)));
			}
			menuFragment.setArguments(bundle);
			return menuFragment;
		}

		@Override
		public int getCount() {
			/* 因为还有推荐页 ，所以count为长度加一 */
			return kind.length() + 1;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			if (0 != position) {
				/* kind是类似"0123"这样的字符串。0、1、2、3代表价位 */
				return pageTitles[Integer.valueOf(kind.substring(position - 1,
						position))];
			}
			/* 如果是第0项的话返回"推荐" */
			return "推荐";
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			super.destroyItem(container, position, object);
		}

	}
}
