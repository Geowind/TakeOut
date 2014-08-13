package cn.geowind.takeout.ui;

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
import cn.geowind.takeout.util.Utils;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.RequestPasswordResetCallback;

/**
 * 找回密码Activity
 * 
 * @author 朱霜
 * @school University of South China
 * @date
 */
public class ForgotPasswordActivity extends BaseActivity implements
		OnClickListener {
	private ActionBar mActionBar;
	private EditText emailEdt;
	private Button resetBtn;
	private Animation shake;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_forgot_password);

		mActionBar = getSupportActionBar();
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setDisplayShowHomeEnabled(false);
		mActionBar.setTitle(R.string.change_new_password);

		emailEdt = (EditText) findViewById(R.id.email_edt);
		resetBtn = (Button) findViewById(R.id.reset_password_btn);

		resetBtn.setOnClickListener(this);

		shake = AnimationUtils.loadAnimation(this, R.anim.shake);
	}

	@Override
	public void onClick(View v) {
		String strEmail = emailEdt.getText().toString();
		if (strEmail.equals("")) {
			emailEdt.startAnimation(shake);
			Toast.makeText(this,
					getResources().getString(R.string.email_empty),
					Toast.LENGTH_SHORT).show();
			return;
		}
		if (!Utils.checkEmail(strEmail)) {
			Toast.makeText(this, "邮箱不合法", Toast.LENGTH_SHORT).show();
			return;
		}
		AVUser.requestPasswordResetInBackground(strEmail,
				new RequestPasswordResetCallback() {

					@Override
					public void done(AVException e) {
						if (e == null) {
							Toast.makeText(getApplicationContext(),
									R.string.send_email, Toast.LENGTH_SHORT)
									.show();
							finish();
						} else {
							Toast.makeText(getApplicationContext(),
									R.string.send_email_failure,
									Toast.LENGTH_SHORT).show();
							e.printStackTrace();
						}
					}
				});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		finish();
		return super.onOptionsItemSelected(item);
	}
}
