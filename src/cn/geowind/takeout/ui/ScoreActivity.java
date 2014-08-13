package cn.geowind.takeout.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import cn.geowind.takeout.R;

/**
 * 积分Activity
 * 
 * @author 朱霜
 * @school University of South China
 * @date 2014.05
 */
public class ScoreActivity extends BaseActivity {
	private ActionBar mActionBar;

	private static final String TITLE = "积分专区";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_score);

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
