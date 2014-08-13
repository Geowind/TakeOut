package cn.geowind.takeout.ui.fragment;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;
import cn.geowind.takeout.R;
import cn.geowind.takeout.app.App;
import cn.geowind.takeout.entity.Food;
import cn.geowind.takeout.ui.FoodDetailActivity;
import cn.geowind.takeout.ui.RestaurantActivity;
import cn.geowind.takeout.util.TimeUtil;
import cn.geowind.takeout.util.Utils;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVQuery.CachePolicy;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.LogUtil.log;

/**
 * 外卖店详情页菜单Fragment
 * 
 * @author 朱霜
 * @school University of South China
 * @date 2013.09/2014.04
 * @see RestaurantActivity
 */
public class MenuFragment extends Fragment {
	private Activity mActivity;
	private GridView mGridView;
	private List<AVObject> mList;
	private MenuAdapter mAdapter;
	private LayoutInflater mInflater;

	private AVQuery<AVObject> mQuery;
	private ImageLoader mImageLoader;

	private Bundle bundle;
	private String restaurantId;
	private String rank;
	/**
	 * 判断是否为推荐页
	 */
	private boolean isSpecial;
	private boolean isWifiConnected;
	private static final String TAG = "RestaurantMenu";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		bundle = getArguments();
		restaurantId = bundle.getString(Food.RESTURANT_ID);
		rank = String.valueOf(bundle.getInt(Food.RANK));
		isSpecial = bundle.getBoolean("special");
		isWifiConnected = bundle.getBoolean("wifi");

		mQuery = new AVQuery<AVObject>(Food.CLASS_NAME);
		mQuery.setCachePolicy(CachePolicy.CACHE_ELSE_NETWORK);
		mQuery.setMaxCacheAge(TimeUtil.RESTAURANT_MENU);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_menu, container, false);
		mGridView = (GridView) view.findViewById(R.id.gridview);
		mGridView.setEmptyView(view.findViewById(R.id.empty));
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mActivity = getActivity();
		mInflater = mActivity.getLayoutInflater();
		mImageLoader = new ImageLoader(App.requestQueue, App.bitmapLruCache);

		mQuery.whereEqualTo(Food.RESTURANT_ID, restaurantId);
		if (isSpecial) {
			/* 如果是推荐页的话只显示specialty为true的菜 */
			mQuery.whereEqualTo(Food.SPECIALTY, true);
		} else {
			/* 如果是价位菜的话则显示该价位段并且specialty为false的菜 */
			// mQuery.whereNotEqualTo(Food.SPECIALTY, true);
			mQuery.whereEqualTo(Food.RANK, rank);
		}
		mQuery.orderByAscending(Food.PRICE);
		mQuery.findInBackground(new FindCallback<AVObject>() {

			@Override
			public void done(final List<AVObject> list, AVException e) {
				if (e == null) {
					mList = list;
					mAdapter = new MenuAdapter();
					mGridView.setAdapter(mAdapter);
					mGridView.setOnItemClickListener(new OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> parent,
								View view, int position, long id) {
							Intent intent = new Intent(mActivity,
									FoodDetailActivity.class);
							intent.putExtra(Food.CLASS_NAME,
									Food.parseFood(list.get(position)));
							mActivity.startActivity(intent);
							mActivity.overridePendingTransition(
									R.anim.in_from_right, R.anim.out_to_left);
						}
					});
				} else {
					log.e(TAG, "获取菜单数据异常", e);
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * RestaurantMenu GridView Adapter
	 * 
	 * @author 朱霜
	 * @school University of South China
	 * @date 2013.09
	 * @see MenuFragment
	 */
	class MenuAdapter extends BaseAdapter {

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
			if (isWifiConnected) {
				convertView = performGetViewWhenWifiConnect(position,
						convertView);
			} else {
				convertView = performGetViewWhenNormal(position, convertView);
			}
			return convertView;
		}

		/**
		 * 在普通联网状态下调用此方法，不显示图片
		 */
		private View performGetViewWhenNormal(int position, View convertView) {
			ViewHolder holder;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.menu_grid_item_normal,
						null, false);
				holder.foodName = (TextView) convertView
						.findViewById(R.id.food_name);
				holder.hot = (TextView) convertView.findViewById(R.id.hot);
				holder.price = (TextView) convertView.findViewById(R.id.price);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			final AVObject obj = mList.get(position);
			holder.foodName.setText(obj.getString(Food.NAME));
			String sHot = "已售" + obj.getInt(Food.HOT) + "份";
			holder.hot.setText(Utils.hightLight(sHot, "#DD4814", 2,
					sHot.length() - 1));
			holder.price.setText("￥" + obj.getDouble(Food.PRICE));
			return convertView;
		}

		/**
		 * 当在Wifi状态下调用这个方法
		 * 
		 * @param position
		 * @param convertView
		 * @return
		 */
		private View performGetViewWhenWifiConnect(int position,
				View convertView) {
			ViewHolder holder;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.menu_grid_item_wifi,
						null, false);
				holder.image = (NetworkImageView) convertView
						.findViewById(R.id.img);
				holder.image
						.setErrorImageResId(R.drawable.picture_not_available);
				holder.foodName = (TextView) convertView
						.findViewById(R.id.food_name);
				holder.hot = (TextView) convertView.findViewById(R.id.hot);
				holder.price = (TextView) convertView.findViewById(R.id.price);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			final AVObject obj = mList.get(position);
			String img = obj.getString(Food.IMG_URL);
			if (img == null) {
				holder.image.setImageUrl(Utils.PICTURE_NOT_AVAILABLE,
						mImageLoader);
			} else {
				holder.image.setImageUrl(img, mImageLoader);
			}
			holder.foodName.setText(obj.getString(Food.NAME));
			String sHot = "已售" + obj.getInt(Food.HOT) + "份";
			holder.hot.setText(Utils.hightLight(sHot, "#DD4814", 2,
					sHot.length() - 1));
			holder.price.setText("￥" + obj.getDouble(Food.PRICE));
			return convertView;
		}

		class ViewHolder {
			NetworkImageView image;
			TextView foodName;
			TextView price;
			TextView hot;
		}

	}
}
