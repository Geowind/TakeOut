package cn.geowind.takeout.ui;

import java.util.List;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import cn.geowind.takeout.R;
import cn.geowind.takeout.entity.User;
import cn.geowind.takeout.ui.widget.LoadingDialog;
import cn.geowind.takeout.util.ToastUtil;
import cn.geowind.takeout.util.Utils;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.SaveCallback;

/**
 * 修改昵称，性别的Activity
 * 
 * @author 朱霜
 * @school University of South China
 * @date 2014.04
 */
public class ChangeBasicInfoActivity extends BaseActivity implements
		OnClickListener {
	private ActionBar mActionBar;
	private EditText mNicknameEdt;
	private Button mOkBtn;

	private ViewGroup mLayoutMale;
	private ViewGroup mLayoutFemale;

	private LoadingDialog mDialog;
	private AVUser mUser;
	private String mGender;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_change_basic_info);
		mActionBar = getSupportActionBar();
		mActionBar.setTitle("修改昵称或性别");
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setDisplayShowHomeEnabled(false);

		mNicknameEdt = (EditText) findViewById(R.id.change_nickname_edt);
		mOkBtn = (Button) findViewById(R.id.ok_btn);

		mLayoutMale = (ViewGroup) findViewById(R.id.layout_male);
		mLayoutFemale = (ViewGroup) findViewById(R.id.layout_female);

		mUser = AVUser.getCurrentUser();
		mNicknameEdt.setText(mUser.getString(User.NICKNAME));
		mGender = mUser.getString(User.GENDER);
		if ("男".equals(mGender)) {
			mLayoutMale.setBackgroundResource(R.drawable.transparent_30);
			mLayoutFemale.setBackgroundResource(R.color.transparent);
		} else {
			mLayoutFemale.setBackgroundResource(R.drawable.transparent_30);
			mLayoutMale.setBackgroundResource(R.color.transparent);
		}

		mOkBtn.setOnClickListener(this);
		mLayoutMale.setOnClickListener(this);
		mLayoutFemale.setOnClickListener(this);

		mDialog = new LoadingDialog(this, "正在保存");
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
		int id = v.getId();
		if (id == R.id.layout_male) {
			mGender = "男";
			mLayoutMale.setBackgroundResource(R.drawable.transparent_30);
			mLayoutFemale.setBackgroundResource(R.color.transparent);
		} else if (id == R.id.layout_female) {
			mGender = "女";
			mLayoutFemale.setBackgroundResource(R.drawable.transparent_30);
			mLayoutMale.setBackgroundResource(R.color.transparent);
		} else if (id == R.id.ok_btn) {
			final String nickname = mNicknameEdt.getText().toString().trim();
			if ("".equals(nickname) || !Utils.checkString(nickname)) {
				Toast.makeText(this, "昵称不符合要求", Toast.LENGTH_SHORT).show();
				return;
			}
			mDialog.show();
			/**
			 * 先判断昵称是否已被占用
			 */
			AVQuery<AVUser> query = AVUser.getQuery();
			query.whereEqualTo(User.NICKNAME, nickname);
			query.findInBackground(new FindCallback<AVUser>() {

				@Override
				public void done(List<AVUser> list, AVException e) {
					if (e == null && list.size() == 0) {
						mUser.put(User.NICKNAME, nickname);
						mUser.put(User.GENDER, mGender);
						mUser.saveInBackground(new SaveCallback() {

							@Override
							public void done(AVException e) {
								mDialog.dismiss();
								if (e == null) {
									Toast.makeText(
											ChangeBasicInfoActivity.this,
											"修改成功", Toast.LENGTH_SHORT).show();
									setResult(0);
									finish();
								} else {
									e.printStackTrace();
								}
							}
						});
					} else {
						mDialog.dismiss();
						Toast.makeText(ChangeBasicInfoActivity.this,
								"对不起,昵称已被占用", Toast.LENGTH_SHORT).show();
					}
				}
			});

		}
	}
}
