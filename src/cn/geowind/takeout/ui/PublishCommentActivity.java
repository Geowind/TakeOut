package cn.geowind.takeout.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;
import cn.geowind.takeout.R;
import cn.geowind.takeout.entity.Comment;
import cn.geowind.takeout.entity.Order;
import cn.geowind.takeout.entity.User;
import cn.geowind.takeout.ui.widget.LoadingDialog;
import cn.geowind.takeout.util.ToastUtil;
import cn.geowind.takeout.util.Utils;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.SaveCallback;

/**
 * 用户发表评论Activity
 * 
 * @author 朱霜
 * @school University of South China
 * @date 2014.03
 */
public class PublishCommentActivity extends BaseActivity {
	private ActionBar mActionBar;
	private EditText mCommentEdt;
	private LoadingDialog mDialog;
	private Intent mIntent;
	private String orderId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_publish_comment);
		mActionBar = getSupportActionBar();
		mActionBar.setTitle("发表点评");
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setDisplayShowHomeEnabled(false);

		mIntent = getIntent();
		orderId = mIntent.getStringExtra(Order.OBJECT_ID);
		mCommentEdt = (EditText) findViewById(R.id.comment_edt);
		mDialog = new LoadingDialog(this, "正在提交评论");
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			finish();
		} else if (id == R.id.action_publish) {
			mDialog.show();
			String content = mCommentEdt.getText().toString().trim();
			if (content.length() == 0) {
				Toast.makeText(this, R.string.comment_can_not_empty,
						Toast.LENGTH_SHORT).show();
				mDialog.dismiss();
				return false;
			}
			if (!Utils.isNetworkConnected(this)) {
				ToastUtil.toast(this, ToastUtil.NO_NETWORK);
				mDialog.dismiss();
				return false;
			}
			AVUser user = AVUser.getCurrentUser();
			final AVObject comment = new AVObject(Comment.CLASS_NAME);
			comment.put(Comment.CONTENT, content);
			comment.put(Comment.AUTHOR, user.getString(User.NICKNAME));
			comment.put(Comment.AUTHOR_ID, user.getObjectId());
			comment.put(Comment.AUTHOR_GENDER, user.getString(User.GENDER));
			comment.put(Comment.AUTHOR_LOCALE, user.getString(User.LOCALE));
			comment.put(Comment.RESTAURANT,
					mIntent.getStringExtra(Order.RESTAURANT_NAME));
			comment.put(Comment.RESTAURANT_ID,
					mIntent.getStringExtra(Order.RESTAURANT_ID));
			comment.setFetchWhenSave(true);
			comment.saveInBackground(new SaveCallback() {

				@Override
				public void done(AVException e) {
					if (e == null) {
						AVObject obj = new AVObject(Order.CLASS_NAME);
						obj.setObjectId(orderId);
						obj.put(Order.COMMENT_ID, comment.getObjectId());
						obj.saveInBackground(new SaveCallback() {

							@Override
							public void done(AVException e) {
								if (e == null) {
									mDialog.dismiss();
									Toast.makeText(PublishCommentActivity.this,
											R.string.comment_submit_success,
											Toast.LENGTH_SHORT).show();
									setResult(2);
									finish();
								} else {
									mDialog.dismiss();
									e.printStackTrace();
								}
							}
						});
					} else {
						mDialog.dismiss();
						e.printStackTrace();
						Toast.makeText(PublishCommentActivity.this,
								R.string.comment_submit_failure,
								Toast.LENGTH_SHORT).show();
					}
				}
			});
			Utils.hideInputMethod(this);
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.action_publish_comment, menu);
		return super.onCreateOptionsMenu(menu);
	}

}
