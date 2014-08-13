package cn.geowind.takeout.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;
import cn.geowind.takeout.R;
import cn.geowind.takeout.entity.User;
import cn.geowind.takeout.util.ToastUtil;
import cn.geowind.takeout.util.Utils;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.SaveCallback;

public class SelectLocaleActivity extends BaseActivity implements
		OnClickListener {
	private ActionBar mActionBar;
	private TextView mHongchen;
	private TextView mBeixiao;
	private TextView mNanxiao;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_locale);
		mActionBar = getSupportActionBar();
		mActionBar.setTitle("选择校区");
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setDisplayShowHomeEnabled(false);

		mHongchen = (TextView) findViewById(R.id.is_hongcheng);
		mBeixiao = (TextView) findViewById(R.id.is_beixiao);
		mNanxiao = (TextView) findViewById(R.id.is_nanxiao);
		mHongchen.setOnClickListener(this);
		mBeixiao.setOnClickListener(this);
		mNanxiao.setOnClickListener(this);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			finish();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		if (!Utils.isNetworkConnected(this)) {
			ToastUtil.toast(this, ToastUtil.NO_NETWORK);
			return;
		}
		AVUser user = AVUser.getCurrentUser();
		user.put(User.LOCALE, v.getTag().toString());
		user.saveInBackground(new SaveCallback() {

			@Override
			public void done(AVException e) {
				if (e == null) {
					Toast.makeText(SelectLocaleActivity.this, "校区修改成功",
							Toast.LENGTH_SHORT).show();
					setResult(1);
					finish();
				}
			}
		});

	}
}
