package cn.geowind.takeout.ui;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.widget.TextView;
import cn.geowind.takeout.R;
import cn.geowind.takeout.entity.Food;
import cn.geowind.takeout.entity.Order;
import cn.geowind.takeout.ui.widget.LoadingDialog;
import cn.geowind.takeout.util.Utils;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;

/**
 * 订单详情页Activity
 * 
 * @author 朱霜
 * @school University of South China
 * @date 2014.03
 */
public class OrderDetailActivity extends BaseActivity {
	private ActionBar mActionBar;
	private TextView restaurant;
	private TextView totalPrice;
	private TextView foodNames;
	private TextView foodPrices;
	private TextView foodAmounts;
	private TextView others;
	private TextView address;

	private LoadingDialog mDialog;
	private AVQuery<AVObject> mQuery;
	private AVObject mOrder;
	private String commentId;
	private static final String TITLE = "订单详情";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_order_detail);
		mActionBar = getSupportActionBar();
		mActionBar.setTitle(TITLE);
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setDisplayShowHomeEnabled(false);

		commentId = getIntent().getStringExtra(Order.COMMENT_ID);

		restaurant = (TextView) findViewById(R.id.restaurant);
		totalPrice = (TextView) findViewById(R.id.total_price);
		foodNames = (TextView) findViewById(R.id.order_data_food);
		foodPrices = (TextView) findViewById(R.id.order_data_price);
		foodAmounts = (TextView) findViewById(R.id.order_data_amount);
		others = (TextView) findViewById(R.id.order_others);
		address = (TextView) findViewById(R.id.order_address);

		mDialog = new LoadingDialog(this, getResources().getString(
				R.string.trying_to_loading));
		mDialog.show();
		mQuery = AVQuery.getQuery(Order.CLASS_NAME);
		mQuery.whereEqualTo(Order.COMMENT_ID, commentId);
		mQuery.findInBackground(new FindCallback<AVObject>() {

			@Override
			public void done(List<AVObject> list, AVException e) {
				if (e == null) {
					mOrder = list.get(0);
					mDialog.dismiss();
					restaurant.setText(mOrder.getString(Order.RESTAURANT_NAME));
					totalPrice.setText("￥"
							+ mOrder.getDouble(Order.TOTAL_PRICE));
					StringBuilder strFood = new StringBuilder();
					StringBuilder strPrice = new StringBuilder();
					StringBuilder strAmount = new StringBuilder();
					try {
						JSONArray array = new JSONArray(mOrder
								.getString(Order.DATA));
						int len = array.length();
						JSONObject jo;
						for (int i = 0; i < len - 1; i++) {
							jo = array.getJSONObject(i);
							strFood.append(jo.getString(Food.NAME) + "\n");
							strPrice.append("￥" + jo.getDouble(Food.PRICE)
									+ "\n");
							strAmount.append(jo.getInt(Food.AMOUNT) + "份\n");
						}
						/**
						 * 最后一行不需要换行符
						 */
						jo = array.getJSONObject(len - 1);
						strFood.append(jo.getString(Food.NAME));
						strPrice.append("￥" + jo.getDouble(Food.PRICE));
						strAmount.append(jo.getInt(Food.AMOUNT) + "份");
					} catch (JSONException je) {
						je.printStackTrace();
					}
					foodNames.setText(strFood);
					foodPrices.setText(strPrice);
					foodAmounts.setText(strAmount);
					String strOthers = "其他需求：" + mOrder.getString(Order.OTHERS);
					others.setText(Utils.hightLight(strOthers, "#DD4814", 0, 4));
					String strAddress = "送餐地址："
							+ mOrder.getString(Order.LOCALE)
							+ mOrder.getString(Order.DORMITORY) + "栋***";
					address.setText(Utils.hightLight(strAddress, "#DD4814", 0,
							4));
				} else {
					mDialog.dismiss();
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
}
