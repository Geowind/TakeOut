package cn.geowind.takeout.ui;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import cn.geowind.takeout.R;

import com.avos.avoscloud.AVAnalytics;

/**
 * 加了Activity切换动画的Activity，app很多Activity都是继承自这个Activity
 * 
 * @author 朱霜
 * @school University of South China
 * @date 2013.09
 */
public class BaseActivity extends ActionBarActivity {

	@Override
	protected void onResume() {
		super.onResume();
		AVAnalytics.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		AVAnalytics.onPause(this);
	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
	}

	@Override
	public void startActivity(Intent intent) {
		super.startActivity(intent);
		overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
	}
}
