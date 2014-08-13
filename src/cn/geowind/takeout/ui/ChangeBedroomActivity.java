package cn.geowind.takeout.ui;

import cn.geowind.takeout.R;
import cn.geowind.takeout.entity.User;
import cn.geowind.takeout.util.ToastUtil;
import cn.geowind.takeout.util.Utils;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.SaveCallback;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

/**
 * 修改寝室号的Activity
 * 
 * @author 朱霜
 * @school University of South China
 * @date 2014.04
 */
public class ChangeBedroomActivity extends BaseActivity implements
		OnClickListener {

	private ActionBar mActionBar;
	private EditText mBedroomEdt;
	private AVUser mUser;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_change_bedroom);
		mActionBar = getSupportActionBar();
		mActionBar.setTitle("修改寝室号");
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setDisplayShowHomeEnabled(false);

		mBedroomEdt = (EditText) findViewById(R.id.change_bedroom_edt);
		findViewById(R.id.ok_btn).setOnClickListener(this);

		mUser = AVUser.getCurrentUser();
		mBedroomEdt.setText(mUser.getString(User.BEDROOM));
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
		String bedroom = mBedroomEdt.getText().toString().trim();
		if ("".equals(bedroom)) {
			Toast.makeText(this, "寝室号不能为空", Toast.LENGTH_SHORT).show();
			return;
		}
		mUser.put(User.BEDROOM, bedroom);
		mUser.saveInBackground(new SaveCallback() {

			@Override
			public void done(AVException e) {
				if (e == null) {
					Toast.makeText(ChangeBedroomActivity.this, "修改成功",
							Toast.LENGTH_SHORT).show();
					setResult(3);
					finish();
				} else {
					e.printStackTrace();
				}
			}
		});
	}
}
