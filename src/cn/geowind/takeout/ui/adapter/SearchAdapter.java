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
import cn.geowind.takeout.ui.fragment.SearchFragment;
import cn.geowind.takeout.util.Utils;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.avos.avoscloud.AVObject;

/**
 * 搜索Fragment中ListView的Adapter
 * 
 * @author 朱霜
 * @school University of South China
 * @date 2013.09/2014.05
 * @see SearchFragment
 */
public class SearchAdapter extends BaseAdapter {
	private Context context;
	private List<AVObject> mList;
	private ImageLoader imageLoader;

	public SearchAdapter(Context context, List<AVObject> list) {
		super();
		this.context = context;
		this.mList = list;
		imageLoader = new ImageLoader(App.requestQueue, App.bitmapLruCache);
	}

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
			convertView = View
					.inflate(context, R.layout.search_list_item, null);
			holder = new ViewHolder();
			holder.image = (NetworkImageView) convertView
					.findViewById(R.id.img);
			holder.image.setErrorImageResId(R.drawable.picture_not_available);
			holder.name = (TextView) convertView.findViewById(R.id.food_name);
			holder.price = (TextView) convertView.findViewById(R.id.price);
			holder.hot = (TextView) convertView.findViewById(R.id.hot);
			holder.restaurant = (TextView) convertView
					.findViewById(R.id.restaurant);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		final AVObject obj = mList.get(position);
		String img = obj.getString(Food.IMG_URL);
		if (img == null) {
			holder.image.setImageUrl(Utils.PICTURE_NOT_AVAILABLE, imageLoader);
		} else {
			holder.image.setImageUrl(img, imageLoader);
		}
		holder.name.setText(obj.getString(Food.NAME));
		holder.price.setText("￥" + obj.getDouble(Food.PRICE));
		String text = "已售出" + obj.getInt(Food.HOT) + "份";
		holder.hot.setText(Utils.hightLight(text, "#F3822B", 3,
				text.length() - 1));
		holder.restaurant.setText(obj.getString(Food.RESTURANT));
		return convertView;
	}

	static class ViewHolder {
		NetworkImageView image;
		TextView name;
		TextView price;
		TextView hot;
		TextView restaurant;
	}

}
