package cn.geowind.takeout.ui.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import cn.geowind.takeout.R;
import cn.geowind.takeout.ui.fragment.SearchFragment;

/**
 * Search界面第二排ListView的Adapter
 * 
 * @author 朱霜
 * @school University of South China
 * @date 2013.10
 * @see SearchFragment
 */
public class CategoryDetailAdapter extends BaseAdapter {
	private Resources res;
	private LayoutInflater inflater;
	private int[][] drawbles = new int[][] {
			new int[] { R.drawable.chaocai, R.drawable.baozhifan,
					R.drawable.chaofan, R.drawable.gaijiaofan,
					R.drawable.huifan },
			new int[] { R.drawable.fensifentiao, R.drawable.banmian,
					R.drawable.chaomian, R.drawable.tangmian },
			new int[] { R.drawable.bing, R.drawable.shuijiao, R.drawable.zhou,
					R.drawable.hundun },
			new int[] { R.drawable.hanbao, R.drawable.jipai, R.drawable.xishi,
					R.drawable.xiaoshi, R.drawable.pisa },
			new int[] { R.drawable.shaokao },
			new int[] { R.drawable.bingpin, R.drawable.naicha,
					R.drawable.niunaidouru }, new int[] { R.drawable.qita } };
	private int index;

	public CategoryDetailAdapter(Context context) {
		super();
		inflater = LayoutInflater.from(context);
		res = context.getResources();
	}

	@Override
	public int getCount() {
		return drawbles[index].length;
	}

	@Override
	public Object getItem(int position) {
		return drawbles[index][position];
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
			convertView = inflater.inflate(R.layout.category_details_item,
					null, false);
			holder.content = (ImageView) convertView
					.findViewById(R.id.category_details_content);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		// holder.content.setText(content[index][position]);
		// holder.content.setImageResource(drawbles[index][position]);
		holder.content.setImageDrawable(res
				.getDrawable(drawbles[index][position]));
		return convertView;
	}

	public void setCategoryClickPosition(int position) {
		index = position;
	}

	public int getCategoryClickPosition() {
		return index;
	}

	static class ViewHolder {
		public ImageView content;
	}

}
