package cn.geowind.takeout.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.Toast;
import cn.geowind.takeout.R;
import cn.geowind.takeout.entity.Food;
import cn.geowind.takeout.ui.adapter.CategoryAdapter;
import cn.geowind.takeout.ui.widget.LoadingDialog;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.LogUtil.log;

/**
 * 按分类搜索的结果Activity
 * 
 * @author 朱霜
 * @school University of South China
 * @date 2013.10
 */
public class CategorySearchActivity extends BaseActivity implements
		OnChildClickListener {
	private ActionBar mActionBar;
	private ExpandableListView mListView;
	private CategoryAdapter adapter;
	private String categoryQuery;
	private LoadingDialog mDialog;

	private Set<String> set;
	private List<String> restaurants;
	private List<List<AVObject>> foods;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.category_search_activity);
		categoryQuery = getIntent().getStringExtra(Food.CATEGORY);

		mActionBar = getSupportActionBar();
		mActionBar.setTitle(categoryQuery);
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setDisplayShowHomeEnabled(false);

		mListView = (ExpandableListView) findViewById(R.id.listview);
		mListView.setEmptyView(findViewById(R.id.empty));
		mDialog = new LoadingDialog(this, getResources().getString(
				R.string.trying_to_loading));
		mDialog.show();
		queryFood(categoryQuery);
	}

	private void queryFood(String queryString) {
		AVQuery<AVObject> query = new AVQuery<AVObject>(Food.CLASS_NAME);
		query.whereEqualTo(Food.CATEGORY, queryString);
		System.out.println("queryString:" + queryString);
		query.findInBackground(new FindCallback<AVObject>() {

			@Override
			public void done(List<AVObject> l, AVException e) {
				if (e == null) {
					mDialog.dismiss();
					set = new TreeSet<String>();
					int size = l.size();
					for (int i = 0; i < size; i++) {
						String restaurant = l.get(i).getString(Food.RESTURANT);
						set.add(restaurant);
					}
					int setSize = set.size();
					mActionBar.setSubtitle("共查找到" + setSize + "家商家");
					// restaurants = set.toArray(new String[set.size()]);
					/**
					 * 将Set转化成List
					 */
					restaurants = new ArrayList<String>(set);
					foods = new ArrayList<List<AVObject>>();
					for (int i = 0; i < restaurants.size(); i++) {
						System.out.println(restaurants.get(i));
						List<AVObject> array = new ArrayList<AVObject>();
						for (int j = 0; j < size; j++) {
							AVObject obj = l.get(j);
							if (restaurants.get(i).equals(
									obj.getString(Food.RESTURANT))) {
								array.add(obj);
							}
						}
						foods.add(array);
					}
					adapter = new CategoryAdapter(CategorySearchActivity.this,
							restaurants, foods);
					mListView.setAdapter(adapter);
				} else {
					log.e("CategorySearchActivity", "按类查询获取数据异常", e);
					mDialog.dismiss();
					Toast.makeText(CategorySearchActivity.this,
							R.string.loading_fail, Toast.LENGTH_SHORT).show();
				}
			}
		});
		mListView.setOnChildClickListener(this);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		finish();
		return true;
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		Intent intent = new Intent(CategorySearchActivity.this,
				FoodDetailActivity.class);
		intent.putExtra(Food.CLASS_NAME, Food.parseFood((AVObject) adapter
				.getChild(groupPosition, childPosition)));
		startActivity(intent);
		return true;
	}
}
