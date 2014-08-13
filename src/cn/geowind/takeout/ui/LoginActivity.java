package cn.geowind.takeout.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import cn.geowind.takeout.R;
import cn.geowind.takeout.app.Constants;
import cn.geowind.takeout.entity.User;
import cn.geowind.takeout.ui.widget.LoadingDialog;
import cn.geowind.takeout.util.ToastUtil;
import cn.geowind.takeout.util.Utils;
import cn.jpush.android.api.JPushInterface;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.LogInCallback;
import com.avos.avoscloud.SaveCallback;

/**
 * 登陆界面
 * 
 * @author 朱霜
 * @school University of South China
 * @date 2013.11
 */
public class LoginActivity extends Activity implements OnClickListener,
		OnEditorActionListener {
	private Context mContext;
	private EditText account;
	private EditText password;
	private Button login;
	private TextView forgotPassword;
	private TextView signUp;
	private Animation shake;
	private LoadingDialog mDialog;
	private SharedPreferences mPrefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPrefs = getSharedPreferences(Constants.PREFS_ACCOUNT, MODE_PRIVATE);
		/**
		 * 如果用户还没有登录过则直接跳转到注册界面。免得用户还要找注册按钮。
		 */
		if (mPrefs.getBoolean(Constants.ACCOUNT_LOGINED, false) == false) {
			startActivity(new Intent(this, RegisterActivity.class));
		}
		setContentView(R.layout.activity_login);
		mContext = this;
		init();
	}

	private void init() {
		account = (EditText) findViewById(R.id.account_edt);
		password = (EditText) findViewById(R.id.password_edt);
		login = (Button) findViewById(R.id.login_btn);
		forgotPassword = (TextView) findViewById(R.id.forgot_password_btn);
		signUp = (TextView) findViewById(R.id.sing_up_btn);

		password.setOnEditorActionListener(this);
		login.setOnClickListener(this);
		forgotPassword.setOnClickListener(this);
		signUp.setOnClickListener(this);

		shake = AnimationUtils.loadAnimation(this, R.anim.shake);
		mDialog = new LoadingDialog(this, "正在登录");
		mDialog.setCancelable(false);
	}

	@Override
	protected void onResume() {
		super.onResume();
		account.setText(mPrefs.getString(Constants.ACCOUNT_ACCOUNT, ""));
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.login_btn) {
			performLogin();
		} else if (id == R.id.sing_up_btn) {
			Intent intent = new Intent(this, RegisterActivity.class);
			startActivity(intent);
			overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);
		} else if (id == R.id.forgot_password_btn) {
			Intent intent = new Intent(this, ForgotPasswordActivity.class);
			startActivity(intent);
			overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);
		}
	}

	/**
	 * 处理点击登录按钮或输入法中的完成按钮的事件,在OnClick和OnEditAction方法中都调用了此方法
	 */
	private void performLogin() {
		String strAccount = account.getText().toString();
		String strPassword = password.getText().toString();
		if (strAccount.equals("") || strPassword.equals("")) {
			Toast.makeText(this, "帐号或密码不能为空", Toast.LENGTH_SHORT).show();
			login.startAnimation(shake);
			return;
		}
		performLogin(strAccount, strPassword);
	}

	/**
	 * 执行登录操作
	 * 
	 * @param account
	 * @param password
	 */
	private void performLogin(String account, String password) {
		mDialog.show();
		if (!Utils.isNetworkConnected(this)) {
			ToastUtil.toast(this, ToastUtil.NO_NETWORK);
			mDialog.dismiss();
			return;
		}
		AVUser.logInInBackground(account, password,
				new LogInCallback<AVUser>() {

					@Override
					public void done(AVUser user, AVException e) {
						/**
						 * 要判断一下是否为商家账号
						 */
						if (user != null
								&& user.getBoolean("restaurant") == false) {
							/**
							 * 保存JPush的registrationId
							 */
							user.put(User.REGISTRATION_ID,
									JPushInterface.getRegistrationID(mContext));
							user.saveInBackground(new SaveCallback() {

								@Override
								public void done(AVException e) {
									mDialog.dismiss();
									if (e == null) {
										Toast.makeText(
												mContext,
												getString(R.string.login_success),
												Toast.LENGTH_SHORT).show();
										/**
										 * 更改account
										 * SharedPreferences中logined属性为true
										 * apply()方法为2.3之后增加的，可以异步保存
										 */
										mPrefs.edit()
												.putBoolean(
														Constants.ACCOUNT_LOGINED,
														true).apply();
										finish();
										overridePendingTransition(
												R.anim.in_from_top,
												R.anim.out_to_bottom);
									} else {
										e.printStackTrace();
										Toast.makeText(mContext,
												"网络出了点问题，等下再试试吧",
												Toast.LENGTH_SHORT).show();
									}
								}
							});
						} else {
							mDialog.dismiss();
							Toast.makeText(mContext,
									getString(R.string.login_error),
									Toast.LENGTH_SHORT).show();
							login.startAnimation(shake);
						}
					}
				});
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (actionId == EditorInfo.IME_ACTION_DONE) {
			performLogin();
		}
		return false;
	}
}
