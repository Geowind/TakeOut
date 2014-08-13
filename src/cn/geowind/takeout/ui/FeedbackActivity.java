package cn.geowind.takeout.ui;

import java.util.List;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import cn.geowind.takeout.R;
import cn.geowind.takeout.entity.User;
import cn.geowind.takeout.ui.widget.LoadingDialog;
import cn.geowind.takeout.util.ToastUtil;
import cn.geowind.takeout.util.Utils;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVQuery.CachePolicy;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.SaveCallback;

/**
 * 反馈意见Activity
 * 
 * @author 朱霜
 * @team Geowind
 * @school University of South China
 * @date 2013.09/2014.03
 */
public class FeedbackActivity extends BaseActivity implements OnClickListener {
	private Resources res;
	private ActionBar mActionBar;
	private LayoutInflater mInflater;
	private ListView mListView;
	private View mHeaderView;
	private FeedbackAdapter mAdapter;
	private LoadingDialog mDialog;

	private List<AVObject> mList;
	private AVQuery<AVObject> mQuery;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_feedback);
		res = getResources();
		mActionBar = getSupportActionBar();
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setDisplayShowHomeEnabled(false);
		mActionBar.setTitle(R.string.action_feedback);

		mInflater = getLayoutInflater();
		mListView = (ListView) findViewById(R.id.listview);
		mHeaderView = mInflater.inflate(R.layout.feedback_header_view, null,
				false);
		mHeaderView.findViewById(R.id.feedback_header_commit)
				.setOnClickListener(this);
		mListView.addHeaderView(mHeaderView, null, false);

		if (!Utils.isNetworkConnected(this)) {
			ToastUtil.toast(this, ToastUtil.NO_NETWORK);
			return;
		}
		mDialog = new LoadingDialog(this,
				res.getString(R.string.trying_to_loading));
		mDialog.show();

		mQuery = AVQuery.getQuery(FeedBack.CLASS_NAME);
		mQuery.setCachePolicy(CachePolicy.NETWORK_ELSE_CACHE);
		mQuery.orderByDescending("createdAt");
		mQuery.findInBackground(new FindCallback<AVObject>() {

			@Override
			public void done(List<AVObject> list, AVException e) {
				if (e == null) {
					mList = list;
					mDialog.dismiss();
					mAdapter = new FeedbackAdapter();
					mListView.setAdapter(mAdapter);
				} else {
					mDialog.dismiss();
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		finish();
		return true;
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.feedback_header_commit) {
			Utils.hideInputMethod(this);
			if (!Utils.isNetworkConnected(this)) {
				ToastUtil.toast(this, ToastUtil.NO_NETWORK);
				return;
			}
			final EditText commentBox = (EditText) mHeaderView
					.findViewById(R.id.feedback_header_edt);
			String content = commentBox.getText().toString().trim();
			if (content.equals("")) {
				ToastUtil.toast(this,
						res.getString(R.string.feedback_content_no_empty), 0);
				return;
			}
			if (content.length() < 15) {
				ToastUtil.toast(this,
						res.getString(R.string.feedback_content_too_short), 0);
				return;
			}

			AVUser user = AVUser.getCurrentUser();
			if (user == null) {
				Toast.makeText(this, R.string.not_login, Toast.LENGTH_SHORT)
						.show();
				Intent intent = new Intent(this, LoginActivity.class);
				startActivity(intent);
				return;
			}
			mDialog.show();
			final AVObject feedback = new AVObject(FeedBack.CLASS_NAME);
			feedback.put(FeedBack.CONTENT, content);
			feedback.put(FeedBack.PHONE, user.getUsername());
			feedback.put(FeedBack.AUTHOR, user.getString(User.NICKNAME));
			feedback.put(FeedBack.GENDER, user.getString(User.GENDER));
			feedback.saveInBackground(new SaveCallback() {

				@Override
				public void done(AVException e) {
					if (e == null) {
						mList.add(0, feedback);
						mAdapter.notifyDataSetChanged();
						commentBox.setText("");
						ToastUtil.toast(
								FeedbackActivity.this,
								res.getString(R.string.feedback_submit_success),
								0);
						mDialog.dismiss();
					} else {
						mDialog.dismiss();
						ToastUtil.toast(FeedbackActivity.this, "网络出了点问题，等下再试吧",
								0);
						e.printStackTrace();
					}
				}
			});
		}
	}

	/**
	 * 反馈实体类
	 * 
	 */
	class FeedBack {
		static final String CLASS_NAME = "FeedBack";
		static final String CONTENT = "content";
		static final String AUTHOR = "author";
		static final String GENDER = "gender";
		static final String PHONE = "phone";
		static final String REPLY = "reply";

		String content;
		String author;
		String gender;
		String phone;
		String reply;
	}

	class FeedbackAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mList.size();
		}

		@Override
		public Object getItem(int position) {
			return mList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.feedback_list_item,
						null, false);
				holder.author = (TextView) convertView
						.findViewById(R.id.feedback_author);
				holder.gender = (ImageView) convertView
						.findViewById(R.id.feedback_gender);
				holder.content = (TextView) convertView
						.findViewById(R.id.feedback_content);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			AVObject obj = mList.get(position);
			holder.author.setText(obj.getString(FeedBack.AUTHOR));
			holder.content.setText(obj.getString(FeedBack.CONTENT));
			if (obj.getString(FeedBack.GENDER).equals("男")) {
				holder.gender.setImageDrawable(res
						.getDrawable(R.drawable.ic_boy));
			} else {
				holder.gender.setImageDrawable(res
						.getDrawable(R.drawable.ic_girl));
			}
			String reply = obj.getString(FeedBack.REPLY);
			holder.reply = (TextView) convertView
					.findViewById(R.id.feedback_reply);
			if (reply != null) {
				holder.reply.setVisibility(View.VISIBLE);
				String text = "团队回复：" + reply;
				holder.reply.setText(Utils.hightLight(text, "#DD4814", 0, 4));
			} else {
				holder.reply.setVisibility(View.GONE);
			}
			return convertView;
		}

		class ViewHolder {
			TextView author;
			ImageView gender;
			TextView content;
			TextView reply;
			TextView time;
		}
	}
}
