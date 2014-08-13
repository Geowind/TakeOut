package cn.geowind.takeout.ui.fragment;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import cn.geowind.takeout.R;
import cn.geowind.takeout.app.App.Event;
import cn.geowind.takeout.entity.Food;
import cn.geowind.takeout.ui.FoodDetailActivity;
import cn.geowind.takeout.util.Utils;

import com.avos.avoscloud.AVAnalytics;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.CountCallback;
import com.avos.avoscloud.FindCallback;

/**
 * 摇一摇的Fragment
 * 
 * @author 朱霜
 * @school University of South China
 * @date 2014.03
 */
public class ShakeFoodFragment extends BaseFragment implements
		SensorEventListener, OnClickListener {
	private Activity mActivity;
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private Vibrator mVibrator;
	private TextView shakeTips;
	private View whichFood;
	private TextView whichFoodName;
	private TextView whichFoodPrice;
	private TextView whichRestaurant;
	private SoundPool mSoundPool;

	private AVQuery<AVObject> mQuery;
	private AVObject mObj;
	private SparseIntArray mSoundMap;
	private boolean isVisible = false;
	/**
	 * 目前服务器上Food表的数量
	 */
	private int foodSize;
	private static final int SENSOR_VALUE = 15;
	private static final int SHAKE_READY = 1;
	private static final int SHAKE_SUCCESS = 2;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActivity = getActivity();
		mSensorManager = (SensorManager) mActivity
				.getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mVibrator = (Vibrator) mActivity
				.getSystemService(Context.VIBRATOR_SERVICE);

		mSoundPool = new SoundPool(3, AudioManager.STREAM_MUSIC, 0);
		mSoundMap = new SparseIntArray();
		mSoundMap.put(SHAKE_READY,
				mSoundPool.load(mActivity, R.raw.shake_ready, 1));
		mSoundMap.put(SHAKE_SUCCESS,
				mSoundPool.load(mActivity, R.raw.shake_success, 1));
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_shake_food, container,
				false);
		shakeTips = (TextView) view.findViewById(R.id.shake_tips);
		whichFood = view.findViewById(R.id.which_food);
		whichFoodName = (TextView) view.findViewById(R.id.which_food_name);
		whichFoodPrice = (TextView) view.findViewById(R.id.which_food_price);
		whichRestaurant = (TextView) view.findViewById(R.id.which_restaurant);
		whichFood.setOnClickListener(this);
		return view;
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		isVisible = isVisibleToUser;
		if (mSensorManager == null) {
			return;
		}
		if (isVisibleToUser) {
			mSensorManager.registerListener(this, mAccelerometer,
					SensorManager.SENSOR_DELAY_NORMAL);
		} else {
			mSensorManager.unregisterListener(this);
		}
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mQuery = AVQuery.getQuery(Food.CLASS_NAME);
		mQuery.countInBackground(new CountCallback() {

			@Override
			public void done(int count, AVException e) {
				if (e == null) {
					foodSize = count;
				} else {
					foodSize = 100;
				}
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		/* 仅当这个Fragment为可见状态的时候才注册监听 */
		if (isVisible) {
			mSensorManager.registerListener(this, mAccelerometer,
					SensorManager.SENSOR_DELAY_NORMAL);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(this);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		float[] values = event.values;
		if (values[0] >= SENSOR_VALUE || values[1] >= SENSOR_VALUE
				|| values[2] >= SENSOR_VALUE) {
			mSoundPool.play(mSoundMap.get(SHAKE_READY), 1, 1, 0, 0, 1);
			whichFood.setVisibility(View.INVISIBLE);
			if (!Utils.isNetworkConnected(mActivity)) {
				shakeTips.setText(R.string.shake_fail);
				return;
			}
			AVAnalytics.onEvent(mActivity, Event.SHAKE_FOOD);
			shakeTips.setText(R.string.trying_to_searching);
			mQuery.setLimit(1);
			mQuery.setSkip((int) (Math.random() * foodSize));
			mQuery.findInBackground(new FindCallback<AVObject>() {

				@Override
				public void done(List<AVObject> list, AVException e) {
					if (e == null) {
						mVibrator.vibrate(300);
						whichFood.setVisibility(View.VISIBLE);
						mObj = list.get(0);
						whichFoodName.setText(mObj.getString(Food.NAME));
						whichFoodPrice.setText("￥" + mObj.getDouble(Food.PRICE));
						whichRestaurant.setText(mObj.getString(Food.RESTURANT));
						mSoundPool.play(mSoundMap.get(SHAKE_SUCCESS), 1, 1, 0,
								0, 1);
					} else {
						shakeTips.setText(R.string.shake_fail);
						e.printStackTrace();
					}
				}
			});
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent(mActivity, FoodDetailActivity.class);
		intent.putExtra(Food.CLASS_NAME, Food.parseFood(mObj));
		startActivity(intent);
	}

}
