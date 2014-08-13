package cn.geowind.takeout.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import cn.geowind.takeout.R;
import cn.geowind.takeout.app.App;
import cn.geowind.takeout.entity.Restaurant;
import cn.geowind.takeout.ui.MainActivity;
import cn.geowind.takeout.ui.RestaurantActivity;
import cn.geowind.takeout.ui.widget.IndexBar;
import cn.geowind.takeout.ui.widget.IndexBar.OnIndexChangeListener;
import cn.geowind.takeout.util.TimeUtil;
import cn.geowind.takeout.util.ToastUtil;
import cn.geowind.takeout.util.Utils;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;

/**
 * 外卖店列表Fragment
 * 
 * @author 朱霜
 * @school University of South China
 * @date 2013.09
 * @see MainActivity
 */
public class TakeoutFragment extends BaseFragment implements
		OnItemClickListener, OnScrollListener, OnIndexChangeListener {
	private Context mContext;
	private ActionBar mActionBar;
	private LayoutInflater mInflater;
	private ListView mListView;
	private IndexBar mIndexBar;

	private AVQuery<AVObject> mQuery;
	private List<AVObject> mList;
	/**
	 * 保存所有Restaurant注册码的首字母，索引与mList对应
	 */
	private List<String> mLetters;
	private TakeOutAdapter mAdapter;
	private ImageLoader mImageLoader;

	private String TAG = "TakeoutFragment";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getActivity();
		mQuery = AVQuery.getQuery(Restaurant.CLASS_NAME);
		mQuery.setCachePolicy(AVQuery.CachePolicy.CACHE_ELSE_NETWORK);
		mQuery.setMaxCacheAge(TimeUtil.TAKEOUT_FRAGMENT);
		mQuery.orderByAscending(Restaurant.REGISTER_CODE);
		mImageLoader = new ImageLoader(App.requestQueue, App.bitmapLruCache);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_takeout, container,
				false);
		mInflater = LayoutInflater.from(mContext);
		mListView = (ListView) view.findViewById(R.id.listview);
		mIndexBar = (IndexBar) view.findViewById(R.id.indexbar);
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mActionBar = MainActivity.getMainActionBar();
		mActionBar.setTitle(getResources().getString(
				R.string.actionbar_title_takeout));
		mActionBar.setDisplayShowCustomEnabled(false);
		mListView.setOnItemClickListener(this);
		if (mQuery.hasCachedResult() || Utils.isNetworkConnected(mContext)) {
			mQuery.findInBackground(new FindCallback<AVObject>() {

				@SuppressLint("DefaultLocale")
				@Override
				public void done(List<AVObject> list, AVException e) {
					if (e == null) {
						mList = list;
						mAdapter = new TakeOutAdapter();
						mListView.setAdapter(mAdapter);

						mListView.setOnScrollListener(TakeoutFragment.this);
						mIndexBar
								.setOnIndexChangeListener(TakeoutFragment.this);
						mLetters = new ArrayList<String>();
						int len = mList.size();
						for (int i = 0; i < len; i++) {
							/* 把该Restaurant的注册码第一个字母加入到List中 */
							mLetters.add(
									i,
									mList.get(i)
											.getString(Restaurant.REGISTER_CODE)
											.substring(0, 1).toUpperCase());
						}
						System.out.println(mLetters.toString());
						// Set转换成String[]
						// String[] letters = mSet.toArray(new
						// String[mSet.size()]);
					} else {
						Log.e(TAG, "parseQuery.findInBackground 出现异常");
						e.printStackTrace();
					}
				}
			});
		} else {
			ToastUtil.toast(mContext, ToastUtil.NO_NETWORK);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		AVObject obj = mList.get(position);
		Intent intent = new Intent(mContext, RestaurantActivity.class);
		intent.putExtra(Restaurant.OBJECT_ID, obj.getObjectId());
		intent.putExtra(Restaurant.NAME, obj.getString(Restaurant.NAME));
		startActivity(intent);
	}

	@Override
	public void onIndexChange(String letter) {
		int position = mLetters.indexOf(letter);
		mListView.setSelection(position);
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {

	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		/* 当mLetter初始化之后才去通知 */
		if (mLetters != null) {
			/* 通知IndexBar去更新UI */
			mIndexBar.notifyIndexSetChanged(mLetters.get(firstVisibleItem));
		}
	}

	class TakeOutAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mList.size();
		}

		@Override
		public Object getItem(int position) {
			return mList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = mInflater.inflate(
						R.layout.takeout_restaurant_list_item, null, false);
				convertView.setTag(holder);
				holder.img = (NetworkImageView) convertView
						.findViewById(R.id.img);
				holder.restaurant = (TextView) convertView
						.findViewById(R.id.restaurant);
				holder.locale = (TextView) convertView
						.findViewById(R.id.restaurant_locale);
				holder.keywords = (TextView) convertView
						.findViewById(R.id.restaurant_keywords);
				holder.businessTime = (TextView) convertView
						.findViewById(R.id.restaurant_business_time);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			AVObject obj = mList.get(position);
			holder.img.setImageUrl(obj.getString(Restaurant.IMG_URL),
					mImageLoader);
			holder.restaurant.setText(obj.getString(Restaurant.NAME));
			holder.locale.setText(obj.getString(Restaurant.LOCALE));
			holder.businessTime.setText("营业时间："
					+ obj.getString(Restaurant.BUSINESS_TIME));
			holder.keywords.setText(obj.getString(Restaurant.KEYWORDS));
			return convertView;
		}

		class ViewHolder {
			NetworkImageView img;
			TextView restaurant;
			TextView businessTime;
			TextView keywords;
			TextView locale;
		}
	}

}
