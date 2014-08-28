package cn.geowind.takeout.ui;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.TextView;
import cn.geowind.takeout.R;
import cn.geowind.takeout.entity.Pager;
import cn.geowind.takeout.util.UpdateManager;
import cn.geowind.takeout.util.Utils;

/**
 * 关于
 * 
 * @author 朱霜
 * @school University of South China
 * @date 2013.09
 */
public class AboutActivity extends BaseActivity implements OnClickListener,
		OnTouchListener {
	private Resources res;
	private TextView appVersionName;
	private TextView appFace;
	private TextView aboutApp;
	private TextView aboutTeam;
	private TextView appCopyright;

	private static int color = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		getSupportActionBar().setTitle(R.string.action_about);

		res = getResources();
		appVersionName = (TextView) findViewById(R.id.app_version);
		appFace = (TextView) findViewById(R.id.app_face);
		aboutApp = (TextView) findViewById(R.id.about_app);
		aboutTeam = (TextView) findViewById(R.id.about_team);
		appCopyright = (TextView) findViewById(R.id.app_copyright);

		String appVersionStr = new UpdateManager(this).getAppVersionName();
		appVersionName.setText(res.getString(R.string.about_app_version)
				+ appVersionStr);
		appCopyright.setText(Utils.hightLight(
				res.getString(R.string.about_app_copyright), "#DD4814", 10, 20));

		appVersionName.setBackgroundResource(R.drawable.background_top);
		appFace.setBackgroundResource(R.drawable.background_white_center);
		aboutApp.setBackgroundResource(R.drawable.background_white_center1);
		aboutTeam.setBackgroundResource(R.drawable.background_white_bottom);

		appVersionName.setOnClickListener(this);
		appVersionName.setOnTouchListener(this);
		appFace.setOnClickListener(this);
		appFace.setOnTouchListener(this);
		aboutApp.setOnClickListener(this);
		aboutApp.setOnTouchListener(this);
		aboutTeam.setOnClickListener(this);
		aboutTeam.setOnTouchListener(this);
		color = aboutApp.getTextColors().getDefaultColor();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		finish();
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		Intent intent;
		switch (v.getId()) {
		case R.id.app_version:
			break;
		case R.id.about_app:
			intent = new Intent(this, WebActivity.class);
			intent.putExtra(Pager.TITLE, res.getString(R.string.about_app));
			intent.putExtra(Pager.DATA, "http://blog.geowind.cn/takeout/");
			startActivity(intent);
			break;
		case R.id.about_team:
			intent = new Intent(this, WebActivity.class);
			intent.putExtra(Pager.TITLE, res.getString(R.string.about_team));
			intent.putExtra(Pager.DATA, "http://blog.geowind.cn/geowind/");
			startActivity(intent);
			break;
		case R.id.app_face:
			intent = new Intent(this, FaceActivity.class);
			startActivity(intent);
			break;
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int id = v.getId();
		TextView tv = (TextView) v;
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			v.setBackgroundResource(R.drawable.img_about_item_press);
			tv.setTextColor(res.getColor(R.color.actionbar_title_color));
			break;
		case MotionEvent.ACTION_UP:
			tv.setTextColor(color);
			if (id == R.id.app_version) {
				tv.setBackgroundResource(R.drawable.background_top);
			} else if (id == R.id.app_face) {
				tv.setBackgroundResource(R.drawable.background_white_center);
			} else if (id == R.id.about_app) {
				tv.setBackgroundResource(R.drawable.background_white_center1);
			} else {
				tv.setBackgroundResource(R.drawable.background_white_bottom);
			}
			break;
		}
		return false;
	}
}
