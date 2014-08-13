package cn.geowind.takeout.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import cn.geowind.takeout.R;
import cn.geowind.takeout.ui.widget.LoadingDialog;
import cn.geowind.takeout.util.ToastUtil;
import cn.geowind.takeout.util.Utils;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.UpdatePasswordCallback;

/**
 * 修改密码的Activity
 * 
 * @author 朱霜
 * @school University of South China
 * @date 2014.04
 */
public class ChangePasswordActivity extends BaseActivity implements
		OnClickListener {
	private ActionBar mActionBar;
	private EditText mOldEdt;
	private EditText mNewEdt;
	private EditText mNewAgainEdt;
	private Button mOkBtn;
	private LoadingDialog mDialog;
	private Animation shake;
	private AVUser mUser;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_change_password);
		mActionBar = getSupportActionBar();
		mActionBar.setTitle("修改密码");
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setDisplayShowHomeEnabled(false);

		mOldEdt = (EditText) findViewById(R.id.old_password_edt);
		mNewEdt = (EditText) findViewById(R.id.new_password_edt);
		mNewAgainEdt = (EditText) findViewById(R.id.new_password_again_edt);
		mOkBtn = (Button) findViewById(R.id.ok_btn);
		mOkBtn.setOnClickListener(this);
		mUser = AVUser.getCurrentUser();

		mDialog = new LoadingDialog(this, getResources().getString(
				R.string.trying_to_handing));
		shake = AnimationUtils.loadAnimation(this, R.anim.shake);
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
		final String oldPsw = mOldEdt.getText().toString().trim();
		final String newPsw = mNewEdt.getText().toString().trim();
		final String newPswAgain = mNewAgainEdt.getText().toString().trim();
		if ("".equals(oldPsw) || "".equals(newPsw) || "".equals(newPswAgain)) {
			Toast.makeText(this, "密码不能为空", Toast.LENGTH_SHORT).show();
			mOkBtn.startAnimation(shake);
			return;
		}
		if (!newPsw.equals(newPswAgain)) {
			Toast.makeText(this, "亲，请输入相同的新密码", Toast.LENGTH_SHORT).show();
			mOkBtn.startAnimation(shake);
			return;
		}
		mDialog.show();
		mUser.updatePasswordInBackground(oldPsw, newPsw,
				new UpdatePasswordCallback() {

					@Override
					public void done(AVException e) {
						mDialog.dismiss();
						if (e == null) {
							/* 先注销登录 */
							AVUser.logOut();
							startActivity(new Intent(
									ChangePasswordActivity.this,
									LoginActivity.class));
							Toast.makeText(ChangePasswordActivity.this,
									"修改成功，重新用新密码登录吧", Toast.LENGTH_LONG).show();
							finish();
						} else {
							e.printStackTrace();
							/* 不知道是什么错误代码 */
							System.out.println("error code:" + e.getCode());
							if (0 == e.getCode()) {
								Toast.makeText(ChangePasswordActivity.this,
										"亲，旧密码不对哦。", Toast.LENGTH_LONG).show();
							} else {
								Toast.makeText(ChangePasswordActivity.this,
										"出了点问题，等下再试试", Toast.LENGTH_LONG)
										.show();
							}
							mOkBtn.startAnimation(shake);
						}
					}
				});
	}
}
