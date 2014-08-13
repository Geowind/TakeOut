package cn.geowind.takeout.ui;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import cn.geowind.takeout.R;
import cn.geowind.takeout.entity.Comment;
import cn.geowind.takeout.entity.Order;
import cn.geowind.takeout.ui.widget.LoadingDialog;
import cn.geowind.takeout.util.ToastUtil;
import cn.geowind.takeout.util.Utils;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;

/**
 * 用户的评论管理Activity
 * 
 * @author 朱霜
 * @school University of South China
 * @date 2014.02
 */
public class CommentsActivity extends BaseActivity implements
		OnItemClickListener {
	private ActionBar mActionBar;
	private ListView mListView;
	private TextView mEmptyView;
	private LayoutInflater mInflater;
	private SimpleDateFormat mDateFormat;
	private CommentsAdapter mAdapter;
	private LoadingDialog mDialog;

	private List<AVObject> mList;
	private AVQuery<AVObject> mQuery;

	private static final String TITLE = "我发表的评论";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_comments);

		mActionBar = getSupportActionBar();
		mActionBar.setTitle(TITLE);
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setDisplayShowHomeEnabled(false);

		mInflater = getLayoutInflater();
		mListView = (ListView) findViewById(R.id.listview);
		mEmptyView = (TextView) findViewById(R.id.empty);
		mListView.setEmptyView(mEmptyView);
		mListView.setOnItemClickListener(this);
		mDialog = new LoadingDialog(this, getResources().getString(
				R.string.trying_to_loading));

		mDateFormat = new SimpleDateFormat("MM-dd HH:mm", Locale.CHINA);

		if (Utils.isNetworkConnected(this)) {
			mDialog.show();
			mQuery = AVQuery.getQuery(Comment.CLASS_NAME);
			mQuery.whereEqualTo(Comment.AUTHOR_ID,
					getIntent().getStringExtra("id"));
			mQuery.orderByDescending("createdAt");
			mQuery.findInBackground(new FindCallback<AVObject>() {

				@Override
				public void done(List<AVObject> list, AVException e) {
					if (e == null) {
						mDialog.dismiss();
						mList = list;
						mAdapter = new CommentsAdapter();
						mListView.setAdapter(mAdapter);
					} else {
						mDialog.dismiss();
						Toast.makeText(CommentsActivity.this,
								R.string.loading_fail, Toast.LENGTH_SHORT)
								.show();
						e.printStackTrace();
					}
				}
			});
		} else {
			ToastUtil.toast(this, ToastUtil.NO_NETWORK);
		}
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
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Intent intent = new Intent(this, OrderDetailActivity.class);
		intent.putExtra(Order.COMMENT_ID, mList.get(position).getObjectId());
		startActivity(intent);
	}

	class CommentsAdapter extends BaseAdapter {

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
				convertView = mInflater.inflate(
						R.layout.profile_comments_list_item, null, false);
				holder.restaurant = (TextView) convertView
						.findViewById(R.id.restaurant);
				holder.content = (TextView) convertView
						.findViewById(R.id.comment_content);
				holder.time = (TextView) convertView
						.findViewById(R.id.comment_time);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			AVObject obj = mList.get(position);
			holder.restaurant.setText(obj.getString(Comment.RESTAURANT));
			holder.content.setText(obj.getString(Comment.CONTENT));
			String reply = obj.getString(Comment.REPLY);
			if (reply != null) {
				holder.reply = (TextView) convertView
						.findViewById(R.id.comment_reply);
				String text = "商家回复：" + reply;
				holder.reply.setText(Utils.hightLight(text, "#DD4814", 0, 4));
			} else {
				convertView.findViewById(R.id.comment_reply).setVisibility(
						View.GONE);
			}
			holder.time.setText("发表于" + mDateFormat.format(obj.getCreatedAt()));
			return convertView;
		}

		class ViewHolder {
			TextView restaurant;
			TextView content;
			TextView reply;
			TextView time;
		}

	}

}