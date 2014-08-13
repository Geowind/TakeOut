package cn.geowind.takeout.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import cn.geowind.takeout.R;
import cn.geowind.takeout.app.App;
import cn.geowind.takeout.app.Constants;
import cn.geowind.takeout.entity.Comment;
import cn.geowind.takeout.entity.Food;
import cn.geowind.takeout.entity.Order;
import cn.geowind.takeout.entity.Restaurant;
import cn.geowind.takeout.entity.User;
import cn.geowind.takeout.ui.widget.AmountView;
import cn.geowind.takeout.ui.widget.AmountView.OnSelectListener;
import cn.geowind.takeout.ui.widget.LoadingDialog;
import cn.geowind.takeout.util.PushUtils;
import cn.geowind.takeout.util.PushUtils.PushCallback;
import cn.geowind.takeout.util.PushUtils.PushType;
import cn.geowind.takeout.util.ToastUtil;
import cn.geowind.takeout.util.Utils;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.avos.avoscloud.AVAnalytics;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.SaveCallback;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

/**
 * 菜的详情页</br> </br>
 * 加入订单：以该菜的id作为键，详细信息（包括菜名，价格，份数，店名，店id）转换成json格式的字符串作为值保存到SharedPreferences中；
 * 进入这个Activity时先判断SharedPreferences中是否contain这个菜的id
 * ，如果contain则显示查看订单字样，否则显示加入订单；
 * 点击加入订单按钮之后，弹出SlidingUpPanelLayout，里面用ListView显示详细的订单信息 ，可以删除某样菜；
 * 按去保存订单按钮后进入订单管理Activity,然后去下订单。
 * 
 * @author 朱霜
 * @school University of South China
 * @date 2013.09/2014.01/2014.05/2014.06
 */
public class FoodDetailActivity extends BaseActivity implements
		View.OnClickListener, OnItemLongClickListener {
	/* @Food */
	private NetworkImageView image;
	private TextView name;
	private TextView price;
	private TextView oldPrice;
	private TextView hot;
	private TextView favorites;
	private TextView description;
	private TextView copyRight;
	/* Action */
	private AmountView detailAmount;
	private TextView detailShopcartTv;
	private ImageView shopcartImg;
	private ImageView favoriteImg;

	/* Order */
	private View orderFooter;
	private TextView mTotalPrice;
	private TextView mAddress;
	private EditText mOthers;

	private LoadingDialog mDialog;
	private SlidingUpPanelLayout mSlidingUpPanelLayout;
	private PopupWindow mTipsPopup;

	/**
	 * 订单ListView
	 */
	private ListView foodOrderList;
	private LayoutInflater mInflater;
	private SimpleOrderAdapter mAdapter;
	/**
	 * 订单保存在List集合中
	 */
	private List<Food> mList;
	/**
	 * 被收藏的Food的信息以json的格式保存起来
	 */
	private SharedPreferences prefsFavorite;
	/**
	 * 被加入订单的Food的信息以json的格式保存起来
	 */
	private SharedPreferences prefsOrder;
	/**
	 * 保存Restaurant的一些重要信息，比如RegistrationId,营业时间，营业状态
	 */

	/**
	 * 用于更新服务器端这道菜数据，比如更新收藏数等
	 */
	private AVObject mFoodObj;
	private AVUser mUser;
	private Resources mRes;
	private Context mContext;
	private Intent mIntent;
	/**
	 * 当前这道菜，是从Intent中获取的Parcelable对象
	 */
	private Food mFood;
	/**
	 * 获取缓存好了的Restaurant
	 */
	private Restaurant mRestaurant;
	private boolean isFavorite;
	private boolean oldIsFavorite;
	/**
	 * 长按时选中的Food的position
	 */
	private int selectedFoodPosition;
	/**
	 * 是否加入订单
	 */
	private boolean isOrder;
	/**
	 * 保存订单之后根据此状态然后在onStop方法中清空List
	 */
	private boolean shouldClearList = false;
	private SimpleDateFormat mDateFormat;

	private final String ADD_TO_ORDER = "加入订单";
	private final String VIEW_ORDER = "查看订单";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_food_detail);
		getSupportActionBar().hide();
		mDialog = new LoadingDialog(this, getResources().getString(
				R.string.trying_to_handing));
		mDialog.setCancelable(false);

		mContext = this;
		mRes = getResources();
		mIntent = getIntent();
		mFood = mIntent.getParcelableExtra(Food.CLASS_NAME);
		findViewByIds();
		name.setText(mFood.name);
		ImageLoader imageLoader = new ImageLoader(App.requestQueue,
				App.bitmapLruCache);
		if (mFood.img == null) {
			image.setImageUrl(Utils.PICTURE_NOT_AVAILABLE, imageLoader);
		} else {
			image.setErrorImageResId(R.drawable.picture_not_available);
			image.setImageUrl(mFood.img, imageLoader);
		}
		price.setText("￥" + mFood.price);
		if (mFood.oldPrice != 0) {
			oldPrice = (TextView) findViewById(R.id.old_price);
			String op = "原价:￥" + mFood.oldPrice;
			oldPrice.setText(Utils.strokeStyle(op, 3, op.length()));
		}
		String sHot = "已售" + mFood.hot + "份";
		hot.setText(Utils.hightLight(sHot, "#DD4814", 2, sHot.length() - 1));
		String sFavorite = mFood.favorites
				+ getString(R.string.food_detail_favorites_text);
		favorites.setText(Utils.hightLight(sFavorite, "#E26337", 0,
				sFavorite.length() - 3));
		description.setText(mFood.description);
		String sCopyRight = String.format(
				getResources().getString(R.string.food_detail_copyright),
				mFood.restaurant);
		copyRight.setText(Utils.hightLight(sCopyRight, "#E26337", 6,
				sCopyRight.length() - 3));

		mInflater = getLayoutInflater();
		prefsFavorite = getSharedPreferences(Constants.PREFS_FAVORITE_FOOD,
				Context.MODE_PRIVATE);
		oldIsFavorite = isFavorite = prefsFavorite.contains(mFood.objectId);
		if (oldIsFavorite) {
			favoriteImg.setImageDrawable(mRes
					.getDrawable(R.drawable.ic_favorite_star_pressed));
		}
		mSlidingUpPanelLayout.setEnableDragViewTouchEvents(true);
		mSlidingUpPanelLayout.setAnchorPoint(0.6f);
		setListeners();

		// /* 获取当前这道菜的Restaurant的信息，一般是从缓存中获取的 */
		// AVQuery<AVObject> query = AVQuery.getQuery(Restaurant.CLASS_NAME);
		// query.setCachePolicy(AVQuery.CachePolicy.CACHE_ELSE_NETWORK);
		// query.getInBackground(mFood.restaurantId, new GetCallback<AVObject>()
		// {
		//
		// @Override
		// public void done(AVObject obj, AVException e) {
		// if (e == null) {
		// mRestaurant = Restaurant.parseRestaurant(obj);
		// } else {
		// e.printStackTrace();
		// }
		// }
		// });

		/* 从SharedPreferences中获取当前这道菜的Restaurant的信息 */
		mRestaurant = Restaurant.fromJson(getSharedPreferences(
				Constants.PREFS_RESTAURANT, Context.MODE_PRIVATE).getString(
				mFood.restaurantId, ""));
		renderListView();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mUser = AVUser.getCurrentUser();
		/* init footer */
		mAddress.setText(mUser == null ? "暂无" : mUser.getString(User.LOCALE)
				+ mUser.getString(User.DORMITORY) + "-"
				+ mUser.getString(User.BEDROOM));
	}

	/**
	 * 初始化订单ListView
	 */
	protected void renderListView() {
		View headerView = mInflater.inflate(R.layout.food_order_list_header,
				null, false);
		((TextView) headerView.findViewById(R.id.food_order_list_header_title))
				.setText(mFood.restaurant);
		((TextView) headerView.findViewById(R.id.food_order_list_header_area))
				.setText("[" + mRestaurant.area + "]");

		/* FooterView提示用户长按某一项可以删除该样菜 */
		orderFooter = mInflater.inflate(R.layout.food_order_list_footer, null,
				false);
		orderFooter.findViewById(R.id.food_order_tips).setOnClickListener(this);
		foodOrderList.addHeaderView(headerView, null, false);
		foodOrderList.setEmptyView(findViewById(R.id.empty));
		foodOrderList.addFooterView(orderFooter, null, false);
		/* Order relative view findViewById() & setLisener */
		mTotalPrice = (TextView) orderFooter
				.findViewById(R.id.order_total_price);
		mAddress = (TextView) orderFooter.findViewById(R.id.order_address);
		mOthers = (EditText) orderFooter.findViewById(R.id.order_others);
		orderFooter.findViewById(R.id.order_more_rice).setOnClickListener(this);
		orderFooter.findViewById(R.id.order_less_pepper).setOnClickListener(
				this);
		orderFooter.findViewById(R.id.order_no_change).setOnClickListener(this);
		orderFooter.findViewById(R.id.order_commit).setOnClickListener(this);

		prefsOrder = getSharedPreferences(mFood.restaurantId,
				Context.MODE_PRIVATE);
		isOrder = prefsOrder.contains(mFood.objectId);
		if (isOrder) {
			detailShopcartTv.setText(VIEW_ORDER);
			shopcartImg.setImageDrawable(mRes
					.getDrawable(R.drawable.ic_shopcart_selected));
		}

		Map<String, ?> map = (Map<String, ?>) prefsOrder.getAll();
		try {
			mList = convertMapToList(map);
			mAdapter = new SimpleOrderAdapter();
			foodOrderList.setAdapter(mAdapter);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		/* 注册上下文菜单 */
		registerForContextMenu(foodOrderList);
		foodOrderList.setOnItemLongClickListener(this);
		mOthers.setLongClickable(false);

		mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
	}

	/**
	 * 计算订单的总金额。
	 * 
	 * @return
	 */
	private double getTotalPrice() {
		double total = 0;
		int size = mList.size();
		for (int i = 0; i < size; i++) {
			Food f = mList.get(i);
			total = total + f.price * mAdapter.getCurrentAmount(i);
		}
		return total;
	}

	/**
	 * 更新总金额，每次更新一下菜的份数或者增加、删除一样菜的时候都要调用这个方法
	 */
	private void updateTotalPrice() {
		mTotalPrice.setText("￥" + getTotalPrice());
	}

	/**
	 * 弹出提示PopupWindow
	 */
	protected void showTips(View anchor) {
		View v = mInflater.inflate(R.layout.pop_food_order_tips, null, false);
		if (mTipsPopup == null) {
			/* 初始化mTipsPopup */
			mTipsPopup = new PopupWindow(v, LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT, true);
			mTipsPopup.setTouchable(true);
			mTipsPopup.setOutsideTouchable(true);
			mTipsPopup.setBackgroundDrawable(new ColorDrawable());
		}
		mTipsPopup.showAsDropDown(anchor, v.getWidth() / 2 - anchor.getWidth()
				/ 2, 5);
		AVAnalytics.onEvent(mContext, App.Event.BOWL);
	}

	/**
	 * 将SharedPreference里面的键值对转换成{@code List<Food>},并按加入订单的时间从大到小排序
	 * 
	 * @param map
	 * @return
	 * @throws JSONException
	 */
	private List<Food> convertMapToList(Map<String, ?> map)
			throws JSONException {
		List<Food> list = new ArrayList<Food>();
		for (Entry<String, ?> entry : map.entrySet()) {
			list.add(Food.fromOrderJson(entry.getValue().toString()));
		}
		sort(list);
		return list;
	}

	/**
	 * 按加入订单的时间从先后顺序排序
	 * 
	 * @param list
	 */
	protected void sort(List<Food> list) {
		Collections.sort(list, new Comparator<Food>() {

			@Override
			public int compare(Food lhs, Food rhs) {
				return (int) (lhs.time - rhs.time);
			}
		});
	}

	private void findViewByIds() {
		name = (TextView) findViewById(R.id.food_name);
		if (mFood.specialty) {
			findViewById(R.id.food_specialty).setVisibility(View.VISIBLE);
		} else {
			findViewById(R.id.food_specialty).setVisibility(View.GONE);
		}
		image = (NetworkImageView) findViewById(R.id.img);
		price = (TextView) findViewById(R.id.price);
		hot = (TextView) findViewById(R.id.hot);
		favorites = (TextView) findViewById(R.id.favorites);
		description = (TextView) findViewById(R.id.description);
		copyRight = (TextView) findViewById(R.id.copyright);
		shopcartImg = (ImageView) findViewById(R.id.detail_shopcart_img);
		favoriteImg = (ImageView) findViewById(R.id.detail_favorite_img);
		detailAmount = (AmountView) findViewById(R.id.detail_amount);
		detailShopcartTv = (TextView) findViewById(R.id.detail_shopcart_tv);
		mSlidingUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_up_panel_layout);
		foodOrderList = (ListView) findViewById(R.id.listview);
	}

	private void setListeners() {
		findViewById(R.id.goto_restaurant).setOnClickListener(this);
		findViewById(R.id.view_comment).setOnClickListener(this);
		findViewById(R.id.detail_back).setOnClickListener(this);
		findViewById(R.id.detail_favorite).setOnClickListener(this);
		findViewById(R.id.detail_shopcart).setOnClickListener(this);
		findViewById(R.id.detail_share).setOnClickListener(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		/* 保存订单之后跳转到OrderActivity,同时清空 List<Food> */
		if (mList != null && mList.size() > 0 && shouldClearList) {
			mAdapter.clearAllItem();
			mAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onClick(final View v) {
		StringBuilder sb;
		switch (v.getId()) {
		case R.id.detail_back:
			finish();
			break;
		case R.id.detail_shopcart:
			if (isOrder) {
				if (mSlidingUpPanelLayout.isExpanded()) {
					mSlidingUpPanelLayout.collapsePane();
				} else {
					mSlidingUpPanelLayout.expandPane();
				}
			} else {
				if (mSlidingUpPanelLayout.isExpanded()) {
					// do noting...
				} else {
					mSlidingUpPanelLayout.expandPane();
				}
				try {
					mFood.amount = detailAmount.getAmount();
					mFood.time = System.currentTimeMillis();
					/* 更新ListView */
					mList.add(mFood);
					sort(mList);
					/* 清空AmountList */
					mAdapter.clearAmount();
					mAdapter.notifyDataSetChanged();
					isOrder = true;
					detailShopcartTv.setText(VIEW_ORDER);
					shopcartImg.setImageDrawable(mRes
							.getDrawable(R.drawable.ic_shopcart_selected));
					/* 将菜品信息加入保存到SharedPreferences中 */
					String orderJson = Food.toOrderJson(mFood);
					Editor prefsOrderEditor = prefsOrder.edit();
					prefsOrderEditor.putString(mFood.objectId, orderJson);
					prefsOrderEditor.apply();
					Toast.makeText(this,
							getString(R.string.add_to_order_success),
							Toast.LENGTH_SHORT).show();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			break;
		case R.id.order_commit:
			v.setClickable(false);
			mDialog.show();
			if (!prepareOrder()) {
				v.setClickable(true);
				mDialog.dismiss();
				return;
			}
			/* 申明一个订单对象 */
			final AVObject order = new AVObject(Order.CLASS_NAME);
			order.put(Order.OWNER, mUser.getObjectId());
			order.put(Order.TELEPHONE, mUser.getUsername());
			order.put(Order.RESTAURANT_ID, mFood.restaurantId);
			order.put(Order.RESTAURANT_NAME, mFood.restaurant);
			order.put(Order.REGISTRATION_ID,
					mUser.getString(Order.REGISTRATION_ID));
			order.put(Order.TOTAL_PRICE, getTotalPrice());
			order.put(Order.LOCALE, mUser.getString(User.LOCALE));
			order.put(Order.DORMITORY, mUser.getString(User.DORMITORY));
			order.put(Order.BEDROOM, mUser.getString(User.BEDROOM));
			order.put(Order.OTHERS, mOthers.getText().toString());
			JSONArray array = new JSONArray();
			try {
				int size = mList.size();
				for (int i = 0; i < size; i++) {
					Food f = mList.get(i);
					JSONObject obj = new JSONObject();
					obj.put(Food.NAME, f.name);
					obj.put(Food.PRICE, f.price);
					obj.put(Food.AMOUNT, mAdapter.getCurrentAmount(i));
					array.put(i, obj);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			order.put(Order.DATA, array.toString());
			order.put(Order.TIME, mDateFormat.format(new Date()));
			/* 商家的registrationId，也要保存到服务器上 */
			order.put(Order.RST_REGISTRATIONID, mRestaurant.registrationId);
			PushUtils.push(PushType.ORDER, mRestaurant.registrationId,
					new PushCallback() {

						@Override
						public void onSuccess(boolean flag) {
							if (flag) {
								/* 推送成功 */
								order.saveInBackground(new SaveCallback() {

									@Override
									public void done(AVException e) {
										if (e == null) {
											isOrder = false;
											detailShopcartTv
													.setText(ADD_TO_ORDER);
											shopcartImg.setImageDrawable(mRes
													.getDrawable(R.drawable.ic_shopcart_normal));
											shouldClearList = true;
											prefsOrder.edit().clear().apply();
											batchUpdateFoodHot();
											Toast.makeText(mContext,
													R.string.order_successful,
													Toast.LENGTH_LONG).show();
											mDialog.dismiss();
											finish();
											startActivity(new Intent(mContext,
													OrderActivity.class));
										} else {
											Toast.makeText(mContext,
													"网络出了点问题，订单稍后在提交吧。",
													Toast.LENGTH_SHORT).show();
											e.printStackTrace();
											mDialog.dismiss();
											v.setClickable(true);
											Log.e("FoodDetail", "订单保存失败");
										}
									}
								});
							} else {
								Toast.makeText(mContext, "网络出了点问题，订单稍后在提交吧。",
										Toast.LENGTH_SHORT).show();
								mDialog.dismiss();
								v.setClickable(true);
								Log.e("FoodDetail", "推送失败");
							}
						}
					});
			break;
		case R.id.order_more_rice:
			sb = new StringBuilder(mOthers.getText().toString());
			mOthers.setText(sb.append(((TextView) v).getText().toString())
					.append(","));
			mOthers.setSelection(sb.length());
			v.setClickable(false);
			break;
		case R.id.order_less_pepper:
			sb = new StringBuilder(mOthers.getText().toString());
			mOthers.setText(sb.append(((TextView) v).getText().toString())
					.append(","));
			mOthers.setSelection(sb.length());
			v.setClickable(false);
			break;
		case R.id.order_no_change:
			sb = new StringBuilder(mOthers.getText().toString());
			mOthers.setText(sb.append(((TextView) v).getText().toString())
					.append(","));
			mOthers.setSelection(sb.length());
			v.setClickable(false);
			break;
		case R.id.food_order_tips:
			showTips(v);
			break;
		case R.id.view_comment:
			Intent intent = new Intent(this, RestaurantCommentsActivity.class);
			intent.putExtra(Comment.RESTAURANT, mFood.restaurant);
			intent.putExtra(Comment.RESTAURANT_ID, mFood.restaurantId);
			startActivity(intent);
			break;
		case R.id.detail_share:
			String shareContent = "我在《外卖小助手》上发现了一道美食。分享吃货们。";
			Intent shareIntent = new Intent(Intent.ACTION_SEND);
			shareIntent.putExtra(Intent.EXTRA_TEXT, shareContent);
			shareIntent.setType("text/plain");
			startActivity(Intent.createChooser(shareIntent, "分享给吃货们..."));
			break;
		case R.id.detail_favorite:
			isFavorite = !isFavorite;
			if (Utils.isNetworkConnected(this)) {
				if (mFoodObj == null) {
					mFoodObj = new AVObject(Food.CLASS_NAME);
					mFoodObj.setObjectId(mFood.objectId);
				}
				if (isFavorite) {
					favoriteImg.setImageDrawable(mRes
							.getDrawable(R.drawable.ic_favorite_star_pressed));
					mFoodObj.increment(Food.FAVORITES, 1);
					ToastUtil.toast(this, ToastUtil.FAVORITE_SUCCESS);
				} else {
					favoriteImg.setImageDrawable(mRes
							.getDrawable(R.drawable.ic_favorite_star_normal));
					mFoodObj.increment(Food.FAVORITES, -1);
					ToastUtil.toast(this, ToastUtil.CANCEL_FAVORITE_SUCCESS);
				}
				mFoodObj.setFetchWhenSave(true);// Caution!!!
				mFoodObj.saveInBackground(new SaveCallback() {

					@Override
					public void done(AVException e) {
						if (e == null) {
							mFood.favorites = mFoodObj.getInt(Food.FAVORITES);
							String text = mFood.favorites
									+ getString(R.string.food_detail_favorites_text);
							favorites.setText(Utils.hightLight(text, "#F3822B",
									0, text.length() - 3));
						} else {
							e.printStackTrace();
						}
					}
				});
			} else {
				ToastUtil.toast(this,
						getString(R.string.no_network_canot_favortie), 0);
			}
			break;
		case R.id.goto_restaurant:
			intent = new Intent(this, RestaurantActivity.class);
			intent.putExtra(Restaurant.NAME, mFood.restaurant);
			intent.putExtra(Restaurant.OBJECT_ID, mFood.restaurantId);
			startActivity(intent);
			break;
		}
	}

	/**
	 * 批量更新Food 的 hot值
	 */
	private void batchUpdateFoodHot() {
		List<AVObject> batch = new ArrayList<AVObject>();
		int size = mList.size();
		for (int i = 0; i < size; i++) {
			Food food = mList.get(i);
			AVObject obj = new AVObject(Food.CLASS_NAME);
			obj.setObjectId(food.objectId);
			obj.increment(Food.HOT, mAdapter.getCurrentAmount(i));
			batch.add(i, obj);
		}
		/* 批量更新API */
		AVObject.saveAllInBackground(batch);
	}

	/**
	 * 下单之前的准备工作，判断是否联网、是否登录、商家是否正在营业中
	 * 
	 * @return <code>true</code> 表示可以下单 <br/>
	 *         <code>false</code> 表示不满足条件不能下单
	 */
	private boolean prepareOrder() {
		if (!Utils.isNetworkConnected(this)) {
			ToastUtil.toast(this, ToastUtil.NO_NETWORK);
			return false;
		}
		/* 先判断有没有登录 */
		if (mUser == null) {
			Toast.makeText(this, R.string.login_to_order, Toast.LENGTH_LONG)
					.show();
			startActivity(new Intent(this, LoginActivity.class));
			return false;
		}
		/* 判断是否在营业时间 */
		switch (mRestaurant.getStatus()) {
		case OK:
			break;
		case TOO_EARLY:
			Toast.makeText(mContext, R.string.business_too_early,
					Toast.LENGTH_LONG).show();
			return false;
		case TOO_LATER:
			Toast.makeText(mContext, R.string.business_too_later,
					Toast.LENGTH_LONG).show();
			return false;
		case STOP:
			Toast.makeText(mContext, R.string.business_stop, Toast.LENGTH_LONG)
					.show();
			return false;
		}
		return true;
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		((Vibrator) getSystemService(Context.VIBRATOR_SERVICE)).vibrate(100);
		selectedFoodPosition = position - 1;
		return false;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		getMenuInflater().inflate(R.menu.menu_simple_food_order, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_delete_food:
			/* 获取所删除的Food的id */
			String id = mList.get(selectedFoodPosition).objectId;
			prefsOrder.edit().remove(id).apply();
			mAdapter.removeItem(selectedFoodPosition);
			mAdapter.notifyDataSetChanged();
			/* 判断所删除的是否为当前页面的菜 */
			if (mFood.objectId.equals(id)) {
				isOrder = false;
				detailShopcartTv.setText(ADD_TO_ORDER);
				shopcartImg.setImageDrawable(mRes
						.getDrawable(R.drawable.ic_shopcart_normal));
			}
			break;
		case R.id.menu_delete_all_food:
			mAdapter.clearAllItem();
			mAdapter.notifyDataSetChanged();
			isOrder = false;
			detailShopcartTv.setText(ADD_TO_ORDER);
			shopcartImg.setImageDrawable(mRes
					.getDrawable(R.drawable.ic_shopcart_normal));
			prefsOrder.edit().clear().apply();
			break;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void finish() {
		/* 调用finish()方法的时候清空amountList */
		if (mAdapter != null) {
			mAdapter.clearAmount();
		}
		if (oldIsFavorite != isFavorite) {
			Editor favoriteFoodEditor = prefsFavorite.edit();
			if (isFavorite) {
				mFood.time = System.currentTimeMillis();
				try {
					String json = Food.toJson(mFood);
					favoriteFoodEditor.putString(mFood.objectId, json);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				favoriteFoodEditor.remove(mFood.objectId);
			}
			favoriteFoodEditor.apply();
		}
		super.finish();
	}

	/**
	 * 订单列表的Adapter
	 * 
	 */
	public class SimpleOrderAdapter extends BaseAdapter implements
			OnSelectListener {
		/**
		 * 维护一个List用来存储订单中每样菜的份数，每次点击AmountView都要更新List里面的值
		 */
		private List<Integer> amountList = new ArrayList<Integer>();

		@Override
		public int getCount() {
			return mList.size();
		}

		/**
		 * 当删除全部项时清空mList和amountList
		 */
		public void clearAllItem() {
			mList.clear();
			amountList.clear();
		}

		public void clearAmount() {
			amountList.clear();
		}

		/**
		 * 当删除某一项时同时删除mList和amountList中的对应的这一项
		 * 
		 * @param position
		 */
		public void removeItem(int position) {
			mList.remove(position);
			amountList.clear();
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
						R.layout.food_order_simple_list_item, null, false);
				holder.name = (TextView) convertView
						.findViewById(R.id.food_name);
				holder.price = (TextView) convertView.findViewById(R.id.price);
				holder.amount = (AmountView) convertView
						.findViewById(R.id.food_amount);
				holder.description = (TextView) convertView
						.findViewById(R.id.food_description);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			Food food = mList.get(position);
			holder.name.setText(food.name);
			holder.price.setText("￥" + food.price);
			/*
			 * 因为每次都会clear掉amountList,所以仅当amountList.size() !=
			 * mList.size()里面没有数据的时候才初始化list的值，因为每次调用notifyDataSetChange()
			 * 方法之后都会重新调用getView()方法
			 */
			if (amountList.size() != mList.size()) {
				amountList.add(position, food.amount);
			}
			/* 第一次加入订单的时候不是从Preferences里面读取的，所以要判断food.description 是否等于null */
			if ("".equals(food.description) || null == food.description) {
				holder.description.setVisibility(View.GONE);
				System.out.println("description:" + position + " gone");
			} else {
				holder.description.setVisibility(View.VISIBLE);
				holder.description.setText(food.description);
				/* 为了在ListView中使TextView显示跑马灯效果，需要添加这行代码 */
				holder.description.setSelected(true);
				System.out.println("description:" + position + " visible");
			}
			holder.amount.setAmount(food.amount);
			/* 把position设置为AmoutView的Tag，方便知道在onClick方法中得知当前点击的是哪一项的AmountView */
			holder.amount.setTag(position);
			holder.amount.setOnSelectListener(this);

			/* 执行到最后一次getView()方法之后，更新总金额 */
			if (position == mList.size() - 1) {
				updateTotalPrice();
			}
			return convertView;
		}

		/**
		 * 获取当前项AmountView的值
		 * 
		 * @param position
		 * @return
		 */
		public int getCurrentAmount(int position) {
			return amountList.get(position);
		}

		@Override
		public void onSelect(View view, int amount) {
			int p = (Integer) view.getTag();
			amountList.set(p, amount);
			/* 菜的份数有变动都要更新总金额 */
			updateTotalPrice();
		}

		class ViewHolder {
			TextView name;
			TextView price;
			AmountView amount;
			TextView description;
		}
	}
}