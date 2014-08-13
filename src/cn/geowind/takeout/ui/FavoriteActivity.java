package cn.geowind.takeout.ui;

import cn.geowind.takeout.R;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

/**
 * 用户的收藏管理Activity
 * 
 * @author 朱霜
 * @school University of South China
 * @date 2014.02
 */
public class FavoriteActivity extends BaseActivity {
	private ActionBar mActionBar;
	private static final String TITLE = "我的收藏";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.favorite_activity);
		mActionBar = getSupportActionBar();
		mActionBar.setTitle(TITLE);
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setDisplayShowHomeEnabled(false);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			finish();
		}
		return super.onOptionsItemSelected(item);
	}
}
