package cn.geowind.takeout.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;
import cn.geowind.takeout.R;
import cn.geowind.takeout.entity.User;
import cn.geowind.takeout.util.ToastUtil;
import cn.geowind.takeout.util.Utils;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.SaveCallback;

/**
 * 改变宿舍楼和寝室号的Activity
 * 
 * @author 朱霜
 * @school University of South China
 * @date 2014.04
 */
public class ChangeDormitoryActivity extends BaseActivity implements
		OnClickListener {
	private ActionBar mActionBar;
	private EditText mDormitoryEdt;
	private AVUser mUser;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_change_dormitory);
		mActionBar = getSupportActionBar();
		mActionBar.setTitle("修改宿舍楼");
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setDisplayShowHomeEnabled(false);

		mDormitoryEdt = (EditText) findViewById(R.id.change_dormitory_edt);
		findViewById(R.id.ok_btn).setOnClickListener(this);

		mUser = AVUser.getCurrentUser();
		mDormitoryEdt.setText(mUser.getString(User.DORMITORY));
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
		String dormitory = mDormitoryEdt.getText().toString().trim();
		if ("".equals(dormitory) || !Utils.checkString(dormitory)) {
			Toast.makeText(this, "宿舍楼不符合要求", Toast.LENGTH_SHORT).show();
			return;
		}
		mUser.put(User.DORMITORY, dormitory);
		mUser.saveInBackground(new SaveCallback() {

			@Override
			public void done(AVException e) {
				if (e == null) {
					Toast.makeText(ChangeDormitoryActivity.this, "修改成功",
							Toast.LENGTH_SHORT).show();
					setResult(2);
					finish();
				} else {
					e.printStackTrace();
				}
			}
		});
	}
}
