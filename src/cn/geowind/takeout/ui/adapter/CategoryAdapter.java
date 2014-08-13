package cn.geowind.takeout.ui.adapter;

import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import cn.geowind.takeout.R;
import cn.geowind.takeout.entity.Food;
import cn.geowind.takeout.ui.CategorySearchActivity;
import cn.geowind.takeout.util.Utils;

import com.avos.avoscloud.AVObject;

/**
 * 分类搜索Adapter
 * 
 * @author 朱霜
 * @school University of South China
 * @date 2013.10
 * @see CategorySearchActivity
 */
public class CategoryAdapter extends BaseExpandableListAdapter {
	private Resources res;
	private LayoutInflater inflater;
	private List<String> restaurants;
	private List<List<AVObject>> foods;

	public CategoryAdapter(Context context, List<String> restaurants,
			List<List<AVObject>> foods) {
		super();
		this.restaurants = restaurants;
		this.foods = foods;
		res = context.getResources();
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getGroupCount() {
		return restaurants.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return foods.get(groupPosition).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return restaurants.get(groupPosition);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return foods.get(groupPosition).get(childPosition);
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		TextView group;
		if (convertView == null) {
			convertView = inflater.inflate(
					R.layout.category_search_list_group_item, null);
			group = (TextView) convertView.findViewById(R.id.category_group);
			System.out.println("getGroupView" + restaurants.get(groupPosition));
			convertView.setTag(group);
		} else {
			group = (TextView) convertView.getTag();
		}
		group.setText(restaurants.get(groupPosition));
		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = inflater.inflate(
					R.layout.category_search_list_child_item, null);
			holder = new ViewHolder();
			holder.name = (TextView) convertView.findViewById(R.id.food_name);
			holder.price = (TextView) convertView.findViewById(R.id.price);
			holder.hot = (TextView) convertView.findViewById(R.id.hot);
			holder.favorites = (TextView) convertView
					.findViewById(R.id.favorites);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		AVObject obj = foods.get(groupPosition).get(childPosition);
		holder.name.setText(obj.getString(Food.NAME));
		holder.price.setText("￥" + obj.getDouble(Food.PRICE));
		String sHot = "已售" + obj.getInt(Food.HOT) + "份";
		holder.hot.setText(Utils.hightLight(sHot, "#DD4814", 2,
				sHot.length() - 1));
		String text = obj.getInt(Food.FAVORITES)
				+ res.getString(R.string.food_detail_favorites_text);
		holder.favorites.setText(Utils.hightLight(text, "#F3822B", 0,
				text.length() - 3));
		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	static class ViewHolder {
		TextView name;
		TextView price;
		TextView hot;
		TextView favorites;
	}

}
