package cn.geowind.takeout.ui.fragment;

import java.util.Calendar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.geowind.takeout.R;
import cn.geowind.takeout.app.Constants;
import cn.geowind.takeout.entity.User;
import cn.geowind.takeout.ui.ChangeBasicInfoActivity;
import cn.geowind.takeout.ui.ChangeBedroomActivity;
import cn.geowind.takeout.ui.ChangeDormitoryActivity;
import cn.geowind.takeout.ui.ChangeEmailActivity;
import cn.geowind.takeout.ui.ChangePasswordActivity;
import cn.geowind.takeout.ui.CommentsActivity;
import cn.geowind.takeout.ui.FavoriteActivity;
import cn.geowind.takeout.ui.LoginActivity;
import cn.geowind.takeout.ui.MainActivity;
import cn.geowind.takeout.ui.OrderActivity;
import cn.geowind.takeout.ui.SelectLocaleActivity;

import com.avos.avoscloud.AVUser;

/**
 * 用户个人资料
 * 
 * @author 朱霜
 * @school University of South China
 * @date 2013.11
 */
public class ProfileFragment extends BaseFragment implements OnClickListener {
	private FragmentActivity mActivity;
	private ActionBar mActionBar;
	private AVUser mUser;

	private RelativeLayout mFrame;
	private TextView mNickname;
	private ImageView mGender;

	private TextView mOrdersTv;

	private TextView mTelephone;
	private TextView mLocale;
	private TextView mDormitory;
	private TextView mBedroom;
	private TextView mEmail;
	private Button mChangePasswordBtn;
	private Button mLogoutOrToLoginBtn;

	private SharedPreferences mPrefsAccount;

	private static final int REQUEST_CODE_USER = 0;
	private static final int REQUEST_CODE_LOCALE = 1;
	private static final int REQUEST_CODE_DORMITORY = 2;
	private static final int REQUEST_CODE_BEDROOM = 3;
	private static final int REQUEST_CODE_EMAIL = 4;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_profile, container,
				false);
		mActivity = (ActionBarActivity) getActivity();
		mActionBar = MainActivity.getMainActionBar();
		mActionBar.setDisplayShowCustomEnabled(false);
		mActionBar.setTitle(R.string.actionbar_title_profile);

		mFrame = (RelativeLayout) view.findViewById(R.id.frame);
		mNickname = (TextView) view.findViewById(R.id.profile_nickname);
		mGender = (ImageView) view.findViewById(R.id.profile_gender);
		mTelephone = (TextView) view.findViewById(R.id.profile_telephone);
		mLocale = (TextView) view.findViewById(R.id.profile_locale_tv);
		mDormitory = (TextView) view.findViewById(R.id.profile_dormitory_tv);
		mBedroom = (TextView) view.findViewById(R.id.profile_bedroom_tv);
		mEmail = (TextView) view.findViewById(R.id.profile_email_tv);
		mLogoutOrToLoginBtn = (Button) view
				.findViewById(R.id.profile_logout_or_to_login);
		mChangePasswordBtn = (Button) view
				.findViewById(R.id.profile_change_password);

		view.findViewById(R.id.frame).setOnClickListener(this);
		view.findViewById(R.id.profile_orders).setOnClickListener(this);
		view.findViewById(R.id.profile_comments).setOnClickListener(this);
		view.findViewById(R.id.profile_favorites).setOnClickListener(this);
		view.findViewById(R.id.profile_locale).setOnClickListener(this);
		view.findViewById(R.id.profile_dormitory).setOnClickListener(this);
		view.findViewById(R.id.profile_bedroom).setOnClickListener(this);
		view.findViewById(R.id.profile_email).setOnClickListener(this);

		mOrdersTv = (TextView) view.findViewById(R.id.profile_orders_tv);

		int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
		if (hour > 5 && hour < 15) {
			mFrame.setBackgroundResource(R.drawable.sunrise);
		} else {
			mFrame.setBackgroundResource(R.drawable.dusk);
		}
		mLogoutOrToLoginBtn.setOnClickListener(this);

		mPrefsAccount = mActivity.getSharedPreferences(Constants.PREFS_ACCOUNT,
				Context.MODE_PRIVATE);
		return view;
	}

	/**
	 * 当前账号已登录时调用此方法
	 */
	protected void refreshUiWhenLogin() {
		mGender.setVisibility(View.VISIBLE);
		mTelephone.setVisibility(View.VISIBLE);
		mNickname.setText(mUser.getString(User.NICKNAME));
		if ("男".equals(mUser.getString(User.GENDER))) {
			mGender.setBackgroundResource(R.drawable.ic_gender_male);
		} else {
			mGender.setBackgroundResource(R.drawable.ic_gender_female);
		}
		mTelephone.setText(mUser.getUsername());
		mLocale.setText(mUser.getString(User.LOCALE));
		mDormitory.setText(mUser.getString(User.DORMITORY));
		mBedroom.setText(mUser.getString(User.BEDROOM));
		if (mUser.getEmail() == null) {
			mEmail.setText("绑定邮箱用于找回密码");
		} else {
			mEmail.setText(mUser.getEmail());
		}

		mChangePasswordBtn.setVisibility(View.VISIBLE);
		mChangePasswordBtn.setOnClickListener(this);
		mLogoutOrToLoginBtn.setText(R.string.log_out_account);

		mOrdersTv.setCompoundDrawablePadding(0);
		/**
		 * 收到新订单回复就在receiver里面把notifyOrder置为true,如果为true则显示红点
		 */
		if (mPrefsAccount.getBoolean(Constants.ACCOUNT_NOTIFY_ORDER, true) == true) {
			mOrdersTv.setCompoundDrawablesWithIntrinsicBounds(0, 0,
					R.drawable.shap_notify, 0);
		} else {
			mOrdersTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
		}
	}

	/**
	 * 当前账号未登录时调用此方法刷新UI界面
	 */
	protected void refreshUiWhenLogout() {
		mNickname.setText(R.string.not_login);
		mGender.setVisibility(View.INVISIBLE);
		mTelephone.setVisibility(View.INVISIBLE);
		mLocale.setText(R.string.not_available);
		mBedroom.setText(R.string.not_available);
		mDormitory.setText(R.string.not_available);
		mEmail.setText(R.string.unbundled);
		mChangePasswordBtn.setVisibility(View.GONE);
		mLogoutOrToLoginBtn.setText(R.string.to_login);
	}

	/**
	 * 跳转到登录页面
	 */
	protected void gotoLoginActivity() {
		startActivity(new Intent(mActivity, LoginActivity.class));
	}

	@Override
	public void onResume() {
		super.onResume();
		/**
		 * 获取当前用户
		 */
		mUser = AVUser.getCurrentUser();
		if (mUser == null) {
			refreshUiWhenLogout();
		} else {
			refreshUiWhenLogin();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		/**
		 * 刷新UI
		 */
		refreshUiWhenLogin();
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.frame:
			if (mUser == null) {
				Toast.makeText(mActivity, R.string.not_login,
						Toast.LENGTH_SHORT).show();
				gotoLoginActivity();
			} else {
				startActivityForResult(new Intent(mActivity,
						ChangeBasicInfoActivity.class), REQUEST_CODE_USER);
			}
			break;
		case R.id.profile_orders:
			if (mUser == null) {
				Toast.makeText(mActivity, R.string.not_login,
						Toast.LENGTH_SHORT).show();
				gotoLoginActivity();
			} else {
				/**
				 * 点了订单 之后，红点应该消失
				 */
				mPrefsAccount.edit()
						.putBoolean(Constants.ACCOUNT_NOTIFY_ORDER, false)
						.apply();
				startActivity(new Intent(mActivity, OrderActivity.class));
			}
			break;
		case R.id.profile_favorites:
			startActivity(new Intent(mActivity, FavoriteActivity.class));
			break;
		case R.id.profile_comments:
			if (mUser == null) {
				Toast.makeText(mActivity, R.string.not_login,
						Toast.LENGTH_SHORT).show();
				gotoLoginActivity();
			} else {
				Intent intent = new Intent(mActivity, CommentsActivity.class);
				intent.putExtra("id", mUser.getObjectId());
				startActivity(intent);
			}
			break;

		case R.id.profile_locale:
			if (mUser == null) {
				Toast.makeText(mActivity, R.string.not_login,
						Toast.LENGTH_SHORT).show();
				gotoLoginActivity();
			} else {
				startActivityForResult(new Intent(mActivity,
						SelectLocaleActivity.class), REQUEST_CODE_LOCALE);
			}
			break;
		case R.id.profile_dormitory:
			if (mUser == null) {
				Toast.makeText(mActivity, R.string.not_login,
						Toast.LENGTH_SHORT).show();
				gotoLoginActivity();
			} else {
				startActivityForResult(new Intent(mActivity,
						ChangeDormitoryActivity.class), REQUEST_CODE_DORMITORY);
			}
			break;
		case R.id.profile_bedroom:
			if (mUser == null) {
				Toast.makeText(mActivity, R.string.not_login,
						Toast.LENGTH_SHORT).show();
				gotoLoginActivity();
			} else {
				startActivityForResult(new Intent(mActivity,
						ChangeBedroomActivity.class), REQUEST_CODE_BEDROOM);
			}
			break;
		case R.id.profile_email:
			if (mUser == null) {
				Toast.makeText(mActivity, R.string.not_login,
						Toast.LENGTH_SHORT).show();
				gotoLoginActivity();
			} else {
				startActivityForResult(new Intent(mActivity,
						ChangeEmailActivity.class), REQUEST_CODE_EMAIL);
			}
			break;
		case R.id.profile_logout_or_to_login:
			if (mUser == null) {
				gotoLoginActivity();
			} else {
				/**
				 * 注销之前先把用户名保存到手机里面
				 */
				mPrefsAccount
						.edit()
						.putString(Constants.PREFS_ACCOUNT, mUser.getUsername())
						.apply();
				/**
				 * 注销账号
				 */
				AVUser.logOut();
				mUser = null;
				refreshUiWhenLogout();
			}
			break;
		case R.id.profile_change_password:
			startActivity(new Intent(mActivity, ChangePasswordActivity.class));
		}
	}
}
