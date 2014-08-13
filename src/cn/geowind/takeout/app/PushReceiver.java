package cn.geowind.takeout.app;

import cn.geowind.takeout.ui.OrderActivity;
import cn.jpush.android.api.JPushInterface;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

/**
 * 自定义Receiver用来处理接收到的推送消息
 * 
 * @author 朱霜
 * @school University of South China
 * @date 2014.03
 */
public class PushReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action.equals(JPushInterface.ACTION_NOTIFICATION_RECEIVED)) {
			/* 发送局部广播，保证安全性且高效 */
			LocalBroadcastManager.getInstance(context).sendBroadcast(
					new Intent(App.ACTION_ORDER_HANDLED));
			/* 收到新订单回复就在把notifyOrder置为true,如果为true则显示红点 */
			context.getSharedPreferences(Constants.PREFS_ACCOUNT,
					Context.MODE_PRIVATE).edit()
					.putBoolean(Constants.ACCOUNT_NOTIFY_ORDER, true).apply();
		} else if (action.equals(JPushInterface.ACTION_NOTIFICATION_OPENED)) {
			Intent i = new Intent(context, OrderActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(i);
		}
	}

}
