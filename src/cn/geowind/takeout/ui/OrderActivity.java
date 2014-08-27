package cn.geowind.takeout.ui;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.PopupMenu.OnMenuItemClickListener;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import cn.geowind.takeout.R;
import cn.geowind.takeout.app.App;
import cn.geowind.takeout.entity.Food;
import cn.geowind.takeout.entity.Order;
import cn.geowind.takeout.entity.Restaurant;
import cn.geowind.takeout.ui.widget.LoadingDialog;
import cn.geowind.takeout.util.PushUtils;
import cn.geowind.takeout.util.PushUtils.PushCallback;
import cn.geowind.takeout.util.PushUtils.PushType;
import cn.geowind.takeout.util.TimeUtil;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVQuery.CachePolicy;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.DeleteCallback;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.SaveCallback;

/**
 * 订单中心Activity
 * 
 * @author 朱霜
 * @school University of South China
 * @date 2014.02
 */
public class OrderActivity extends BaseActivity implements OnItemClickListener {
	private Context mContext;
	private ActionBar mActionBar;
	private ListView mListView;
	private AVQuery<AVObject> mQuery;
	private List<AVObject> mList;
	private LayoutInflater mInflater;
	private OrderAdapter mAdapter;
	private SimpleDateFormat mDateFormat;
	private LoadingDialog mDialog;
	private LocalBroadcastManager mLbm;
	private OrderReceiver mReceiver;

	private static final String TITLE = "我的订单";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_order);
		mContext = this;
		mActionBar = getSupportActionBar();
		mActionBar.setTitle(TITLE);
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setDisplayShowHomeEnabled(false);

		mInflater = getLayoutInflater();
		mListView = (ListView) findViewById(R.id.listview);
		mListView.addHeaderView(
				mInflater.inflate(R.layout.order_list_header, null, false),
				null, false);
		mListView.setEmptyView(findViewById(R.id.empty));
		mListView.setOnItemClickListener(this);
		mDialog = new LoadingDialog(this, getResources().getString(
				R.string.trying_to_loading));
		// if (Utils.isNetworkConnected(this)) {
		refreshData();
		// } else {
		// ToastUtil.toast(this, ToastUtil.NO_NETWORK);
		// }

		mDateFormat = new SimpleDateFormat("MM-dd HH:mm", Locale.CHINA);
		mReceiver = new OrderReceiver();
		/* 用LocalBroadcastManager，更安全且更高效 */
		mLbm = LocalBroadcastManager.getInstance(this);
		mLbm.registerReceiver(mReceiver, new IntentFilter(
				App.ACTION_ORDER_HANDLED));
		// registerReceiver(mReceiver, new
		// IntentFilter(App.ACTION_ORDER_HANDLED));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mLbm.unregisterReceiver(mReceiver);
	}

	/**
	 * 从服务器上获取最新的数据
	 */
	protected void refreshData() {
		mDialog.show();
		Calendar time = Calendar.getInstance();
		time.setTime(new Date());
		time.add(Calendar.DATE, -6);
		mQuery = AVQuery.getQuery(Order.CLASS_NAME);
		/* 订单缓存一个星期的数据。先从网上获取最新的数据，如果获取失败了，再从缓存中获取。 */
		mQuery.setCachePolicy(CachePolicy.NETWORK_ELSE_CACHE);
		mQuery.setMaxCacheAge(7 * TimeUtil.DAY);
		mQuery.whereEqualTo(Order.OWNER, AVUser.getCurrentUser().getObjectId());
		mQuery.whereGreaterThan("createdAt", time.getTime());
		mQuery.orderByDescending("createdAt");
		mQuery.findInBackground(new FindCallback<AVObject>() {

			@Override
			public void done(List<AVObject> list, AVException e) {
				if (e == null) {
					mDialog.dismiss();
					mList = list;
					mAdapter = new OrderAdapter();
					mListView.setAdapter(mAdapter);
				} else {
					mDialog.dismiss();
					Toast.makeText(mContext, R.string.loading_fail,
							Toast.LENGTH_SHORT).show();
					e.printStackTrace();
				}
			}
		});
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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 1 && resultCode == 2) {
			refreshData();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		int p = position - 1;
		Intent intent = new Intent(this, RestaurantActivity.class);
		intent.putExtra(Restaurant.OBJECT_ID,
				mList.get(p).getString(Order.RESTAURANT_ID));
		intent.putExtra(Restaurant.NAME,
				mList.get(p).getString(Order.RESTAURANT_NAME));
		startActivity(intent);
	}

	/**
	 * 订单中心页面的Adapter
	 */
	class OrderAdapter extends BaseAdapter implements OnClickListener {
		private static final String STATUS_ORDERED = "已下单，等待商家受理";
		private static final String STATUS_DEALED = "商家已受理订单";
		private static final String STATUS_SUCCESS = "交易成功";

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
				convertView = mInflater.inflate(R.layout.order_list_item, null,
						false);
				holder.restaurant = (TextView) convertView
						.findViewById(R.id.restaurant);
				holder.totalPrice = (TextView) convertView
						.findViewById(R.id.total_price);
				holder.food = (TextView) convertView
						.findViewById(R.id.order_data_food);
				holder.price = (TextView) convertView
						.findViewById(R.id.order_data_price);
				holder.amount = (TextView) convertView
						.findViewById(R.id.order_data_amount);
				holder.others = (TextView) convertView
						.findViewById(R.id.order_others);
				holder.time = (TextView) convertView
						.findViewById(R.id.order_time);
				holder.status = (TextView) convertView
						.findViewById(R.id.order_status);
				holder.action = (Button) convertView
						.findViewById(R.id.order_action);
				holder.moreAction = (ImageView) convertView
						.findViewById(R.id.order_more_action);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			AVObject obj = mList.get(position);
			holder.restaurant.setText(obj.getString(Order.RESTAURANT_NAME));
			holder.totalPrice.setText("￥" + obj.getDouble(Order.TOTAL_PRICE));
			holder.time.setText("订单提交于"
					+ mDateFormat.format(obj.getCreatedAt()));
			StringBuilder strFood = new StringBuilder();
			StringBuilder strPrice = new StringBuilder();
			StringBuilder strAmount = new StringBuilder();
			try {
				JSONArray array = new JSONArray(obj.getString(Order.DATA));
				int len = array.length();
				JSONObject jo;
				for (int i = 0; i < len - 1; i++) {
					jo = array.getJSONObject(i);
					strFood.append(jo.getString(Food.NAME) + "\n");
					strPrice.append("￥" + jo.getDouble(Food.PRICE) + "\n");
					strAmount.append(jo.getInt(Food.AMOUNT) + "份\n");
				}
				/* 最后一行不需要换行符 */
				jo = array.getJSONObject(len - 1);
				strFood.append(jo.getString(Food.NAME));
				strPrice.append("￥" + jo.getDouble(Food.PRICE));
				strAmount.append(jo.getInt(Food.AMOUNT) + "份");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			holder.food.setText(strFood);
			holder.price.setText(strPrice);
			holder.amount.setText(strAmount);
			String others = obj.getString(Order.OTHERS);
			if ("".equals(others)) {
				holder.others.setVisibility(View.GONE);
			} else {
				holder.others.setVisibility(View.VISIBLE);
				holder.others.setText("额外需求：" + others);
			}

			if (obj.getBoolean(Order.IS_DEAL) == false) {
				holder.status.setText(STATUS_ORDERED);
				holder.action.setVisibility(View.INVISIBLE);
				/* 显示更多操作的按钮 */
				holder.moreAction.setVisibility(View.VISIBLE);
				holder.moreAction.setTag(position);
				holder.moreAction.setOnClickListener(this);
			} else {
				holder.status.setText(STATUS_DEALED);
				holder.action.setVisibility(View.VISIBLE);
				holder.action.setTag(position);
				holder.action.setOnClickListener(this);
				/* 隐藏更多操作的按钮 */
				holder.moreAction.setVisibility(View.GONE);

				if (obj.getBoolean(Order.IS_DONE) == false) {
					holder.action.setText(R.string.order_confirm_received);
				} else {
					if (obj.getString(Order.COMMENT_ID) == null
							&& System.currentTimeMillis()
									- obj.getCreatedAt().getTime() < TimeUtil.DAY) {
						holder.action.setText(R.string.order_to_comment);
					} else {
						holder.action.setVisibility(View.GONE);
						holder.status.setText(STATUS_SUCCESS);
					}
				}
			}
			return convertView;
		}

		@Override
		public void onClick(View v) {
			int id = v.getId();
			final int position = (Integer) v.getTag();
			final AVObject obj = mList.get(position);
			switch (id) {
			case R.id.order_action:
				if (obj.getBoolean(Order.IS_DONE)) {
					/* 商家已受理，且用户收到了[点评一下] */
					((Button) v).setText(R.string.order_to_comment);
					Intent intent = new Intent(mContext,
							PublishCommentActivity.class);
					intent.putExtra(Order.OBJECT_ID, obj.getObjectId());
					intent.putExtra(Order.RESTAURANT_NAME,
							obj.getString(Order.RESTAURANT_NAME));
					intent.putExtra(Order.RESTAURANT_ID,
							obj.getString(Order.RESTAURANT_ID));
					startActivityForResult(intent, 1);
					overridePendingTransition(R.anim.in_from_right,
							R.anim.out_to_left);
				} else {
					/* 商家已受理，但用户还没有收到[确认收到] */
					((Button) v).setText(R.string.order_confirm_received);
					obj.put(Order.IS_DONE, true);
					obj.saveInBackground(new SaveCallback() {

						@Override
						public void done(AVException e) {
							if (e == null) {
								notifyDataSetChanged();
							} else {
								e.printStackTrace();
							}
						}
					});
				}
				break;
			case R.id.order_more_action:
				PopupMenu pop = new PopupMenu(mContext, v);
				pop.getMenuInflater().inflate(R.menu.menu_order_more_action,
						pop.getMenu());
				pop.show();
				pop.setOnMenuItemClickListener(new OnMenuItemClickListener() {

					@Override
					public boolean onMenuItemClick(MenuItem menu) {
						mDialog.setMessage("正在处理中");
						mDialog.show();
						int id = menu.getItemId();
						if (id == R.id.menu_undo_order) {
							/**
							 * 少于5分钟，不允许撤销订单
							 */
							if (System.currentTimeMillis()
									- obj.getCreatedAt().getTime() <= 5 * TimeUtil.MINUTE) {
								mDialog.dismiss();
								Toast.makeText(mContext, "亲，下单5分钟之后才可以撤销订单哦",
										Toast.LENGTH_LONG).show();
								return false;
							}
							mDialog.setCancelable(false);
							obj.deleteInBackground(new DeleteCallback() {

								@Override
								public void done(AVException e) {
									mDialog.dismiss();
									if (e == null) {
										mList.remove(position);
										mAdapter.notifyDataSetChanged();
										PushUtils.push(
												PushType.UNDO,
												obj.getString(Order.RST_REGISTRATIONID),
												new PushCallback() {

													@Override
													public void onSuccess(
															boolean flag) {
													}
												});
										Toast.makeText(mContext, "成功撤销订单",
												Toast.LENGTH_LONG).show();
									} else {
										e.printStackTrace();
									}
								}
							});
						} else if (id == R.id.menu_prompt_order) {
							/**
							 * 少于5分钟，不允许撤销订单
							 */
							if (System.currentTimeMillis()
									- obj.getCreatedAt().getTime() <= 5 * TimeUtil.MINUTE) {
								mDialog.dismiss();
								Toast.makeText(mContext, "亲，您刚刚才下的单，等下再催啦。",
										Toast.LENGTH_LONG).show();
								return false;
							}
							PushUtils.push(PushType.URGE,
									obj.getString(Order.RST_REGISTRATIONID),
									new PushCallback() {

										@Override
										public void onSuccess(boolean flag) {
											mDialog.dismiss();
											if (flag) {
												Toast.makeText(mContext,
														"好啦好啦，商家收到你的催单了。",
														Toast.LENGTH_LONG)
														.show();
											}
										}
									});
						}
						return false;
					}
				});
				break;
			}
		}

		class ViewHolder {
			TextView restaurant;
			TextView totalPrice;
			TextView food;
			TextView price;
			TextView amount;
			TextView others;
			TextView time;
			TextView status;
			Button action;
			ImageView moreAction;
		}

	}

	class OrderReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(App.ACTION_ORDER_HANDLED)) {
				refreshData();
			}
		}
	}

}
