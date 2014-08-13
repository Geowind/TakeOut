package cn.geowind.takeout.ui;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import cn.geowind.takeout.R;
import cn.geowind.takeout.app.Constants;
import cn.geowind.takeout.entity.Pager;
import cn.geowind.takeout.entity.User;
import cn.geowind.takeout.ui.widget.LoadingDialog;
import cn.geowind.takeout.util.TimeUtil;
import cn.geowind.takeout.util.ToastUtil;
import cn.geowind.takeout.util.Utils;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.SignUpCallback;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

/**
 * 注册页面
 * 
 * @author 朱霜
 * @school University of South China
 * @date 2013.11/2014.04
 */
public class RegisterActivity extends BaseActivity implements OnClickListener,
		OnEditorActionListener {
	private Context mContext;
	private ActionBar mActionBar;
	private ViewGroup mLayoutProfile;
	private ViewGroup mLayoutMobile;
	private ViewGroup mLayoutVerifyRegister;

	private Spinner localeSpr;
	private EditText dormitoryEdt;
	private EditText bedroomEdt;
	private Button nextToMoblieBtn;

	private EditText accountEdt;
	private Button nextToSignupBtn;

	private TextView tipsVerifyPhone;
	private EditText verifyEdt;
	private EditText passwordEdt;
	private CheckBox mCheckbox;
	private Button registerBtn;

	private ContentResolver mContentResolver;
	private ContentObserver mObserver;

	private Animation shake;
	private LoadingDialog mDialog;

	private String strLocale;
	private String strDormitory;
	private String strBedroom;

	private static final String TRY_TO_RIGSTER = "正在注册中";
	private static final String TRY_TO_SEND_SMS = "正在处理中";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

		mContext = this;
		mActionBar = getSupportActionBar();
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setDisplayShowHomeEnabled(false);
		mActionBar.setTitle(R.string.register_account);

		mDialog = new LoadingDialog(this, TRY_TO_RIGSTER);
		mDialog.setCancelable(false);
		init();

		mContentResolver = getContentResolver();
		mObserver = new SmsReceiver(new Handler());
		mContentResolver.registerContentObserver(Uri.parse("content://sms/"),
				true, mObserver);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mContentResolver.unregisterContentObserver(mObserver);
	}

	private void init() {
		localeSpr = (Spinner) findViewById(R.id.locale_spr);
		dormitoryEdt = (EditText) findViewById(R.id.dormitory_edt);
		bedroomEdt = (EditText) findViewById(R.id.bedroom_edt);
		nextToMoblieBtn = (Button) findViewById(R.id.next_to_moblie_btn);

		accountEdt = (EditText) findViewById(R.id.account_edt);
		nextToSignupBtn = (Button) findViewById(R.id.next_to_signup_btn);

		tipsVerifyPhone = (TextView) findViewById(R.id.tips_verify_phone);
		verifyEdt = (EditText) findViewById(R.id.verify_edt);
		passwordEdt = (EditText) findViewById(R.id.password_edt);
		mCheckbox = (CheckBox) findViewById(R.id.check_protocol);
		registerBtn = (Button) findViewById(R.id.register_btn);

		mLayoutProfile = (ViewGroup) findViewById(R.id.layout_profile);
		mLayoutMobile = (ViewGroup) findViewById(R.id.layout_mobile);
		mLayoutVerifyRegister = (ViewGroup) findViewById(R.id.layout_verify_register);

		nextToMoblieBtn.setOnClickListener(this);
		verifyEdt.setOnClickListener(this);
		nextToSignupBtn.setOnClickListener(this);
		registerBtn.setOnClickListener(this);
		passwordEdt.setOnEditorActionListener(this);
		findViewById(R.id.protocol).setOnClickListener(this);

		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.school_locale, R.layout.style_spinner_item);
		adapter.setDropDownViewResource(R.layout.stlye_spinner_drop_item);
		localeSpr.setAdapter(adapter);
		shake = AnimationUtils.loadAnimation(this, R.anim.shake);
	}

	@Override
	public void onClick(View v) {
		if (!Utils.isNetworkConnected(this)) {
			ToastUtil.toast(this, ToastUtil.NO_NETWORK);
			return;
		}
		int id = v.getId();
		if (id == R.id.register_btn) {
			performSignUp();
		} else if (id == R.id.next_to_moblie_btn) {
			strLocale = localeSpr.getSelectedItem().toString().trim();
			strDormitory = dormitoryEdt.getText().toString().trim();
			strBedroom = bedroomEdt.getText().toString().trim();
			if (strDormitory.equals("") || !Utils.checkString(strDormitory)) {
				Toast.makeText(this, "宿舍楼不符合要求", Toast.LENGTH_SHORT).show();
				nextToMoblieBtn.startAnimation(shake);
				return;
			} else if (strBedroom.equals("")) {
				Toast.makeText(this, "寝室号不能为空", Toast.LENGTH_SHORT).show();
				nextToMoblieBtn.startAnimation(shake);
				return;
			}
			mLayoutProfile.setVisibility(View.GONE);
			mLayoutMobile.setVisibility(View.VISIBLE);
			mLayoutMobile.requestFocus();
		} else if (id == R.id.next_to_signup_btn) {
			final String strAccount = accountEdt.getText().toString();
			if (!Utils.checkMobile(strAccount)) {
				Toast.makeText(mContext, "手机号不正确", Toast.LENGTH_SHORT).show();
				nextToSignupBtn.startAnimation(shake);
				return;
			}
			mDialog.setMessage(TRY_TO_SEND_SMS);
			mDialog.show();
			AVQuery<AVUser> query = AVUser.getQuery();
			query.whereEqualTo(User.USERNAME, strAccount);
			query.findInBackground(new FindCallback<AVUser>() {

				@Override
				public void done(List<AVUser> list, AVException e) {
					if (e == null && list.size() == 0) {
						/**
						 * _User表中不存在该手机号才允许发送验证码
						 * dialog在sendVerifyCode（）方法中dismiss()的
						 */
						sendVerifyCode(strAccount);
					} else {
						mDialog.dismiss();
						e.printStackTrace();
						nextToSignupBtn.startAnimation(shake);
						Toast.makeText(mContext, "很抱歉,该手机号已被注册",
								Toast.LENGTH_SHORT).show();
					}
				}
			});

		} else if (id == R.id.protocol) {
			Intent intent = new Intent(this, WebActivity.class);
			intent.putExtra(Pager.TITLE, "用户注册协议");
			intent.putExtra(Pager.DATA, "file:///android_asset/protocol.html");
			startActivity(intent);
		}
	}

	/**
	 * 获取短信验证码
	 * 
	 * @param strAccount
	 */
	private void sendVerifyCode(String strAccount) {
		String url = Constants.API_IP + Constants.API_TYPE_SMS
				+ Constants.API_QUESTION_MARK + Constants.API_ACTION_TEL
				+ strAccount;
		AsyncHttpClient client = new AsyncHttpClient();
		client.get(url, new JsonHttpResponseHandler() {

			@Override
			public void onFailure(int statusCode, Throwable e,
					JSONObject errorResponse) {
				mDialog.dismiss();
				super.onFailure(statusCode, e, errorResponse);
				Toast.makeText(mContext, R.string.get_registercode_fail,
						Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onSuccess(int statusCode, JSONObject response) {
				mDialog.dismiss();
				super.onSuccess(statusCode, response);
				try {
					if (response.getString("code").equals("200")) {
						Toast.makeText(mContext,
								R.string.send_registercode_success,
								Toast.LENGTH_SHORT).show();
						/**
						 * 隐藏上半部分，显示下半部分
						 */
						mLayoutMobile.setVisibility(View.GONE);
						mLayoutVerifyRegister.setVisibility(View.VISIBLE);
						tipsVerifyPhone
								.setText(getString(R.string.tips_send_verify_code)
										+ accountEdt.getText().toString());
					}
					System.out.println("sendSmsresult:" + response.toString());
				} catch (JSONException e) {
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

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (actionId == EditorInfo.IME_ACTION_DONE) {
			performSignUp();
		}
		return false;
	}

	/**
	 * 处理点击注册按钮和点击输入法的完成按钮的事件,在OnClick和OnEditAction方法中都调用了此方法
	 */
	private void performSignUp() {
		final String strAccount = accountEdt.getText().toString();
		final String strVerifyCode = verifyEdt.getText().toString().trim();
		final String strPassword = passwordEdt.getText().toString().trim();
		if (strAccount.equals("")) {
			Toast.makeText(this, "手机号不能为空", Toast.LENGTH_SHORT).show();
			registerBtn.startAnimation(shake);
			return;
		} else if (!Utils.checkMobile(strAccount)) {
			Toast.makeText(this, "手机号不正确", Toast.LENGTH_SHORT).show();
			registerBtn.startAnimation(shake);
			return;
		} else if (strVerifyCode.equals("")) {
			Toast.makeText(this, "验证码不能为空", Toast.LENGTH_SHORT).show();
			registerBtn.startAnimation(shake);
			return;
		} else if (strPassword.equals("")) {
			Toast.makeText(this, "密码不能为空", Toast.LENGTH_SHORT).show();
			registerBtn.startAnimation(shake);
			return;
		} else if (!mCheckbox.isChecked()) {
			Toast.makeText(this, "请同意用户注册协议", Toast.LENGTH_SHORT).show();
			mCheckbox.startAnimation(shake);
			return;
		}

		mDialog.setMessage(TRY_TO_RIGSTER);
		mDialog.show();
		AVQuery<AVObject> query = AVQuery.getQuery("Verify");
		query.whereEqualTo("tel", strAccount);
		query.findInBackground(new FindCallback<AVObject>() {

			@Override
			public void done(List<AVObject> l, AVException e) {
				/**
				 * 当Verify表中存在这个手机号时，表示存在该验证码
				 */
				if (e == null && l != null && l.size() == 1) {
					AVObject o = l.get(0);
					if (!strVerifyCode.equals(o.getString("verifyCode"))) {
						mDialog.dismiss();
						/**
						 * 短信验证码不正确
						 */
						Toast.makeText(mContext, "很抱歉,短信验证码不正确",
								Toast.LENGTH_SHORT).show();
						return;
					}
					long time = o.getLong("time");
					if (System.currentTimeMillis() - time > TimeUtil.MINUTE * 10) {
						mDialog.dismiss();
						/**
						 * 验证码已过时
						 */
						Toast.makeText(mContext, "很抱歉,验证码已过时",
								Toast.LENGTH_SHORT).show();
						return;
					}
					AVUser user = new AVUser();
					user.setUsername(strAccount);
					user.setPassword(strPassword);
					user.put(User.NICKNAME,
							"User" + strAccount.substring(5, 10));
					user.put(User.GENDER, "男");
					user.put(User.LOCALE, strLocale);
					user.put(User.DORMITORY, strDormitory);
					user.put(User.BEDROOM, strBedroom);
					user.signUpInBackground(new SignUpCallback() {

						@Override
						public void done(AVException e) {
							if (e == null) {
								Toast.makeText(mContext, "注册成功",
										Toast.LENGTH_SHORT).show();
								/**
								 * 把用户名保存到手机里面
								 */
								getSharedPreferences(Constants.PREFS_ACCOUNT,
										Context.MODE_PRIVATE)
										.edit()
										.putString(Constants.ACCOUNT_ACCOUNT,
												strAccount).commit();
								mDialog.dismiss();
								finish();
								overridePendingTransition(R.anim.in_from_top,
										R.anim.out_to_bottom);
							} else {
								mDialog.dismiss();
								e.printStackTrace();
								int errCode = e.getCode();
								if (errCode == 202) {
									Toast.makeText(mContext, "注册失败,手机号已被注册",
											Toast.LENGTH_SHORT).show();
									return;
								}
								/*
								 * else if (errCode == 137) {
								 * Toast.makeText(mContext, "注册失败,昵称已被注册",
								 * Toast.LENGTH_SHORT).show(); }
								 */
								registerBtn.startAnimation(shake);
							}
						}
					});
				} else {
					e.printStackTrace();
					mDialog.dismiss();
					Toast.makeText(mContext, "对不起，验证码不对哦。", Toast.LENGTH_SHORT)
							.show();
				}
			}
		});
	}

	/**
	 * 监听手机短信数据库的变化,自动填充短信验证码
	 * 
	 * @author 朱霜
	 * @school University of South China
	 * @date 2014.04
	 */
	class SmsReceiver extends ContentObserver {
		private static final String SMS_URI_INBOX = "content://sms/inbox";

		public SmsReceiver(Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			String body = "";

			Cursor cursor = getContentResolver().query(
					Uri.parse(SMS_URI_INBOX),
					new String[] { "address", "body", "read", "date" }, null,
					null, "date desc");
			if (null != cursor) {
				if (cursor.moveToFirst()) {
					body = cursor.getString(cursor.getColumnIndex("body"));
					if (body.contains("外卖小助手") && body.contains("验证码")) {
						int start = body.indexOf("是") + 1;
						int end = body.indexOf("。验");
						String code = body.substring(start, end);
						verifyEdt.setText(code);
					}
				}
			}
		}

	}

}
