package cn.geowind.takeout.ui.fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import cn.geowind.takeout.R;
import cn.geowind.takeout.app.App;
import cn.geowind.takeout.app.Constants;
import cn.geowind.takeout.entity.Food;
import cn.geowind.takeout.ui.FoodDetailActivity;
import cn.geowind.takeout.util.Utils;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.GetCallback;

/**
 * 收藏Fragment
 * 
 * @author 朱霜
 * @school University of South China
 * @date 2013.09
 */
public class FavoriteFragment extends Fragment {
	private FragmentActivity activity;
	private ListView listView;
	private LayoutInflater mInflater;
	private FavoriteAdapter adapter;
	private SharedPreferences prefsFavoriteFood;

	private Map<String, ?> map;
	private ArrayList<Food> list;
	private AVQuery<AVObject> parseQuery;
	private ImageLoader imageLoader;
	private String selectedFoodId;
	/**
	 * 长按时选中的Food的position
	 */
	private int selectedFoodPosition;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activity = getActivity();
		mInflater = activity.getLayoutInflater();
		prefsFavoriteFood = activity.getSharedPreferences(
				Constants.PREFS_FAVORITE_FOOD, Context.MODE_PRIVATE);
		imageLoader = new ImageLoader(App.requestQueue, App.bitmapLruCache);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_favorite, container,
				false);

		listView = (ListView) view.findViewById(R.id.listview);
		listView.setEmptyView(view.findViewById(R.id.empty));
		map = (Map<String, ?>) prefsFavoriteFood.getAll();

		list = convertMapToArrayList(map);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		registerForContextMenu(listView);
		adapter = new FavoriteAdapter();
		listView.setAdapter(adapter);

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Food food = list.get(position);
				Intent intent = new Intent(activity, FoodDetailActivity.class);
				intent.putExtra(Food.CLASS_NAME, food);
				activity.startActivity(intent);
				activity.overridePendingTransition(R.anim.in_from_right,
						R.anim.out_to_left);
			}
		});
		listView.setOnItemLongClickListener(new OnItemLongClickListener() {

			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				Vibrator vibrator = (Vibrator) activity
						.getSystemService(Context.VIBRATOR_SERVICE);
				vibrator.vibrate(100);
				Food food = (Food) parent.getItemAtPosition(position);
				selectedFoodId = food.objectId;
				selectedFoodPosition = position;

				parseQuery = AVQuery.getQuery(Food.CLASS_NAME);
				// true if the callback consumed the long click, false otherwise
				return false;
			}
		});
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		activity.getMenuInflater().inflate(R.menu.menu_favorite, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_cancel_favorite:
			if (Utils.isNetworkConnected(activity)) {
				parseQuery.getInBackground(selectedFoodId,
						new GetCallback<AVObject>() {

							@Override
							public void done(AVObject obj, AVException e) {
								obj.increment(Food.FAVORITES, -1);
								obj.saveInBackground();
							}
						});
			}
			prefsFavoriteFood.edit().remove(selectedFoodId).apply();
			list.remove(selectedFoodPosition);
			break;
		case R.id.menu_cancel_all_favorite:
			prefsFavoriteFood.edit().clear().apply();
			list.clear();
			break;
		}
		adapter.notifyDataSetChanged();
		return super.onContextItemSelected(item);
	}

	/**
	 * 将Map集合转换成ArrayList
	 * 
	 * @param map
	 * @return
	 */
	private ArrayList<Food> convertMapToArrayList(Map<String, ?> map) {
		ArrayList<Food> list = new ArrayList<Food>();
		/**
		 * 把Map里面的键返回生成一个 Set集合，然后再用Set去迭代取出Map里面对应键的值，加入到List里面
		 */
		Set<String> set = map.keySet();
		Iterator<String> iterator = set.iterator();
		while (iterator.hasNext()) {
			String jsonString = prefsFavoriteFood
					.getString(iterator.next(), "");
			System.out.println(jsonString);
			try {
				Food food = Food.fromJson(jsonString);
				list.add(food);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		/**
		 * 按收藏的时间从大到小排序
		 */
		Collections.sort(list, new Comparator<Food>() {

			@Override
			public int compare(Food lhs, Food rhs) {
				return (int) (rhs.time - lhs.time);
			}
		});
		return list;
	}

	/**
	 * 
	 * @author 朱霜
	 * @school University of South China
	 * @date
	 */
	class FavoriteAdapter extends BaseAdapter {
		private Resources res;

		public FavoriteAdapter() {
			super();
			res = getResources();
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
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
				convertView = mInflater.inflate(R.layout.favorite_list_item,
						null, false);
				holder.image = (NetworkImageView) convertView
						.findViewById(R.id.img);
				holder.name = (TextView) convertView
						.findViewById(R.id.food_name);
				holder.price = (TextView) convertView.findViewById(R.id.price);
				holder.favorites = (TextView) convertView
						.findViewById(R.id.favorites);
				holder.restaurant = (TextView) convertView
						.findViewById(R.id.restaurant);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			final Food food = list.get(position);
			holder.image.setImageUrl(food.img, imageLoader);
			holder.name.setText(food.name);
			holder.price.setText("￥" + food.price);
			String text = food.favorites
					+ res.getString(R.string.food_detail_favorites_text);
			holder.favorites.setText(Utils.hightLight(text, "#F3822B", 0,
					text.length() - 3));
			holder.restaurant.setText(food.restaurant);
			return convertView;
		}

		class ViewHolder {
			NetworkImageView image;
			TextView name;
			TextView price;
			TextView favorites;
			TextView restaurant;
		}

	}
}
