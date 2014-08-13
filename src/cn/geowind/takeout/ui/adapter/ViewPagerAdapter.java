package cn.geowind.takeout.ui.adapter;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import cn.geowind.takeout.R;
import cn.geowind.takeout.app.App;
import cn.geowind.takeout.app.App.Event;
import cn.geowind.takeout.entity.Food;
import cn.geowind.takeout.entity.Pager;
import cn.geowind.takeout.entity.Restaurant;
import cn.geowind.takeout.ui.FoodDetailActivity;
import cn.geowind.takeout.ui.RestaurantActivity;
import cn.geowind.takeout.ui.WebActivity;
import cn.geowind.takeout.ui.fragment.HomeFragment;
import cn.geowind.takeout.util.TimeUtil;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.avos.avoscloud.AVAnalytics;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVQuery.CachePolicy;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.LogUtil.log;

/**
 * 推荐页滚动图片的Adapter
 * 
 * @author 朱霜
 * @school University of South China
 * @date 2013.10
 * @see HomeFragment
 */
public class ViewPagerAdapter extends PagerAdapter {
	private Context context;
	private int pagerSize;
	private AVQuery<AVObject> pagerQuery;

	public ViewPagerAdapter(Context context, int pagerSize) {
		super();
		this.context = context;
		this.pagerSize = pagerSize;
		pagerQuery = AVQuery.getQuery(Pager.CLASS_NAME);
		pagerQuery.setCachePolicy(CachePolicy.CACHE_ELSE_NETWORK);
		pagerQuery.setMaxCacheAge(TimeUtil.VIEWPAGER_ADAPTER);
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		final View view = View.inflate(context, R.layout.pager_item, null);
		final Pager pager = new Pager();

		pagerQuery.whereEqualTo(Pager.POSITION, String.valueOf(position));
		pagerQuery.findInBackground(new FindCallback<AVObject>() {

			@Override
			public void done(List<AVObject> list, AVException e) {
				if (e == null) {
					final AVObject obj = list.get(0);
					pager.img = obj.getString(Pager.IMG_URL);
					pager.data = obj.getString(Pager.DATA);
					pager.title = obj.getString(Pager.TITLE);
					pager.type = obj.getString(Pager.TYPE);
					pager.others = obj.getString(Pager.OTHERS);

					ImageLoader imageLoader = new ImageLoader(App.requestQueue,
							App.bitmapLruCache);
					pager.pager = (NetworkImageView) view
							.findViewById(R.id.pager);
					pager.pager.setImageUrl(pager.img, imageLoader);
					pager.pager.setOnClickListener(new OnClickListener() {

						@SuppressWarnings("deprecation")
						@Override
						public void onClick(View v) {
							AVAnalytics.onEvent(context, Event.PAGER);
							Intent intent;
							if (pager.type.equals(Pager.TYPE_FOOD)) {
								intent = new Intent(context,
										FoodDetailActivity.class);
								Food food = new Food();
								food.objectId = pager.data;
								food.img = pager.img;
								food.name = pager.title;
								food.price = Double.parseDouble(pager.others);
								intent.putExtra(Food.CLASS_NAME, food);
								context.startActivity(intent);
							} else if (pager.type.equals(Pager.TYPE_RESTAURANT)) {
								intent = new Intent(context,
										RestaurantActivity.class);
								intent.putExtra(Restaurant.OBJECT_ID,
										pager.data);
								intent.putExtra(Restaurant.NAME, pager.title);
								context.startActivity(intent);
							} else if (pager.type.equals(Pager.TYPE_HTML)) {
								intent = new Intent(context, WebActivity.class);
								intent.putExtra(Pager.DATA, pager.data);
								intent.putExtra(Pager.TITLE, pager.title);
								context.startActivity(intent);
							}
						}
					});
				} else {
					log.e("ViewPagerAdapter", "获取Pager数据异常", e);
				}
			}
		});
		/**
		 * 将当前这一项Pager加入到ViewPager中
		 */
		((ViewPager) container).addView(view, 0);
		return view;
	}

	@Override
	public int getCount() {
		return pagerSize;
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}

	/**
	 * 必须实现这个方法，否则从一项Pager切换到之前的Pager时会发生异常
	 */
	@Override
	public void destroyItem(ViewGroup container, int position, Object view) {
		((ViewPager) container).removeView((NetworkImageView) view);
	}

}
