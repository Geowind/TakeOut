package cn.geowind.takeout.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;
import cn.geowind.takeout.R;
import cn.geowind.takeout.util.ToastUtil;
import cn.geowind.takeout.util.Utils;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.SaveCallback;

public class ChangeEmailActivity extends BaseActivity implements
		OnClickListener {
	private ActionBar mActionBar;
	private EditText mEmailEdt;
	private AVUser mUser;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_change_email);
		mActionBar = getSupportActionBar();
		mActionBar.setTitle("修改邮箱");
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setDisplayShowHomeEnabled(false);

		mEmailEdt = (EditText) findViewById(R.id.change_email_edt);
		findViewById(R.id.ok_btn).setOnClickListener(this);

		mUser = AVUser.getCurrentUser();
		mEmailEdt.setText(mUser.getEmail());
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
		String email = mEmailEdt.getText().toString().trim();
		if ("".equals(email) || !Utils.checkEmail(email)) {
			Toast.makeText(this, "邮箱不符合要求", Toast.LENGTH_SHORT).show();
			return;
		}
		mUser.setEmail(email);
		mUser.saveInBackground(new SaveCallback() {

			@Override
			public void done(AVException e) {
				if (e == null) {
					Toast.makeText(ChangeEmailActivity.this, "修改成功",
							Toast.LENGTH_SHORT).show();
					setResult(4);
					finish();
				} else {
					e.printStackTrace();
				}
			}
		});
	}
}
