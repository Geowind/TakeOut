package cn.geowind.takeout.ui.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import cn.geowind.takeout.R;
import cn.geowind.takeout.app.App;
import cn.geowind.takeout.entity.Food;
import cn.geowind.takeout.ui.fragment.HomeFragment;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.avos.avoscloud.AVObject;

/**
 * 推荐Fragment中GridView Adapter
 * 
 * @author 朱霜
 * @school University of South China
 * @date 2013.09
 * @see HomeFragment
 */
public class RecommendGridAdapter extends BaseAdapter {
	private Context context;
	private List<AVObject> parseObjectList;
	private ImageLoader imageLoader;

	public RecommendGridAdapter(Context context, List<AVObject> parseObjectList) {
		super();
		this.context = context;
		this.parseObjectList = parseObjectList;
		imageLoader = new ImageLoader(App.requestQueue, App.bitmapLruCache);
	}

	@Override
	public int getCount() {
		return parseObjectList.size();
	}

	@Override
	public Object getItem(int position) {
		return parseObjectList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = View.inflate(context, R.layout.recommend_grid_item,
					null);
			holder.image = (NetworkImageView) convertView
					.findViewById(R.id.img);
			holder.name = (TextView) convertView.findViewById(R.id.food_name);
			holder.price = (TextView) convertView.findViewById(R.id.price);
			holder.restaurant = (TextView) convertView
					.findViewById(R.id.restaurant);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		final AVObject obj = parseObjectList.get(position);
		holder.image.setImageUrl(obj.getString(Food.IMG_URL), imageLoader);
		holder.name.setText(obj.getString(Food.NAME));
		holder.restaurant.setText(obj.getString(Food.RESTURANT));
		holder.price.setText("￥" + obj.getDouble(Food.PRICE));

		return convertView;
	}

	static class ViewHolder {
		NetworkImageView image;
		TextView name;
		TextView price;
		TextView oldprice;
		TextView restaurant;
	}

}
