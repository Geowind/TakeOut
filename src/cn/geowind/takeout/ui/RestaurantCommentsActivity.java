package cn.geowind.takeout.ui;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import cn.geowind.takeout.R;
import cn.geowind.takeout.entity.Comment;
import cn.geowind.takeout.entity.Order;
import cn.geowind.takeout.ui.adapter.RestaurantCommentsAdapter;
import cn.geowind.takeout.ui.fragment.SearchFragment;
import cn.geowind.takeout.ui.widget.LoadingDialog;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.CountCallback;
import com.avos.avoscloud.FindCallback;

/**
 * 外卖店的评论列表
 * 
 * @author 朱霜
 * @school University of South China
 * @date 2013.11
 */
public class RestaurantCommentsActivity extends BaseActivity implements
		OnItemClickListener, OnScrollListener {
	private Resources mResources;
	private Context mContext = this;
	private ActionBar mActionBar;
	private LoadingDialog mDialog;
	private ListView mListView;
	private RestaurantCommentsAdapter mAdapter;
	private AVQuery<AVObject> mQuery;
	private Intent mIntent;
	private List<AVObject> mList;
	private String restaurantId;

	private TextView footerText;
	private ProgressBar footerProgressBar;
	private int resultSize;
	/**
	 * ListView最后一项的索引
	 */
	private int lastItemIndex;
	private static final int LIMIT_SIZE = 20;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_restaurant_comments);

		mIntent = getIntent();
		restaurantId = mIntent.getStringExtra(Comment.RESTAURANT_ID);

		mActionBar = getSupportActionBar();
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setDisplayShowHomeEnabled(false);
		mActionBar.setTitle(mIntent.getStringExtra(Comment.RESTAURANT));

		mListView = (ListView) findViewById(R.id.listview);
		mListView.setEmptyView(findViewById(R.id.empty));

		mDialog = new LoadingDialog(this, getResources().getString(
				R.string.trying_to_loading));
		mDialog.show();

		mQuery = AVQuery.getQuery(Comment.CLASS_NAME);
		mQuery.whereEqualTo(Comment.RESTAURANT_ID, restaurantId);
		mQuery.orderByDescending("createdAt");
		mQuery.setLimit(LIMIT_SIZE);
		mQuery.findInBackground(new FindCallback<AVObject>() {

			@Override
			public void done(List<AVObject> list, AVException e) {
				if (e == null) {
					mList = list;
					// Collections.reverse(mList);
					mAdapter = new RestaurantCommentsAdapter(mContext, mList);
					mListView.setAdapter(mAdapter);
					mDialog.dismiss();
				} else {
					e.printStackTrace();
					mDialog.dismiss();
				}
			}

		});
		mQuery.countInBackground(new CountCallback() {

			@Override
			public void done(int size, AVException e) {
				if (e == null) {
					resultSize = size;
					mActionBar.setSubtitle("共有" + resultSize + "条评论");
				} else {
					e.printStackTrace();
				}
			}
		});
		View footerView = getLayoutInflater().inflate(
				R.layout.list_footer_load_more, null, false);
		footerText = (TextView) footerView.findViewById(R.id.list_footer_text);
		footerProgressBar = (ProgressBar) footerView
				.findViewById(R.id.list_footer_progressbar);
		mListView.addFooterView(footerView, null, false);
		mListView.setOnItemClickListener(this);
		mListView.setOnScrollListener(this);
		mResources = getResources();
	}

	/**
	 * 加载更多数据
	 */
	private void loadMoreData() {
		mQuery.setSkip(mList.size());
		mQuery.findInBackground(new FindCallback<AVObject>() {

			@Override
			public void done(List<AVObject> list, AVException e) {
				if (e == null) {
					mList.addAll(list);
					mAdapter.notifyDataSetChanged();
				} else {
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
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Intent intent = new Intent(this, OrderDetailActivity.class);
		intent.putExtra(Order.COMMENT_ID, mList.get(position).getObjectId());
		startActivity(intent);
	}

	/**
	 * @see SearchFragment#onScrollStateChanged(AbsListView, int)
	 */
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (lastItemIndex == resultSize) {
			footerText.setText(mResources.getString(R.string.no_more));
			footerProgressBar.setVisibility(View.GONE);
			return;
		}
		/**
		 * 因为没有加HeaderView，所以这里adapter.getCount()后需要减一
		 */
		if (lastItemIndex == mAdapter.getCount() 
				&& scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
			footerText.setText(mResources.getString(R.string.loading_more));
			footerProgressBar.setVisibility(View.VISIBLE);
			loadMoreData();
		} else {
			footerText.setText(mResources.getString(R.string.load_more));
			footerProgressBar.setVisibility(View.GONE);
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		lastItemIndex = firstVisibleItem + visibleItemCount - 1;
	}
}
