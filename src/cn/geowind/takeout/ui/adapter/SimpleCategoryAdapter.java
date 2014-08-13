package cn.geowind.takeout.ui.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import cn.geowind.takeout.R;
import cn.geowind.takeout.ui.fragment.SearchFragment;

/**
 * Search界面第一排ListView的Adapter
 * 
 * @author 朱霜
 * @school University of South China
 * @date 2013.10
 * @see SearchFragment
 */
public class SimpleCategoryAdapter extends BaseAdapter {
	private Resources res;
	private LayoutInflater inflater;
	private String[] categorys;
	private int selectedPosition = -1;

	public SimpleCategoryAdapter(Context context, String[] categorys) {
		super();
		inflater = LayoutInflater.from(context);
		res = context.getResources();
		this.categorys = categorys;
	}

	@Override
	public int getCount() {
		return categorys.length;
	}

	@Override
	public Object getItem(int position) {
		return categorys[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// RelativeLayout layout;
		TextView category;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.category_simple_item, null,
					false);
			// layout = (RelativeLayout) convertView
			// android.R.layout.simple_list_item_1
			// .findViewById(R.id.simple_category_layout);
			category = (TextView) convertView
					.findViewById(R.id.simple_category_text);
			convertView.setTag(category);
		} else {
			category = (TextView) convertView.getTag();
		}
		if (position == selectedPosition) {
			category.setTextColor(res.getColor(R.color.actionbar_title_color));
			category.setBackgroundColor(res.getColor(R.color.main_color));
		} else {
			category.setTextColor(res
					.getColor(R.color.grid_item_food_name_color));
			category.setBackgroundColor(res.getColor(R.color.whilte_main_color));
		}
		category.setText(categorys[position]);
		return convertView;
	}

	public void setSelectedPosition(int selectedPosition) {
		this.selectedPosition = selectedPosition;
	}
}
