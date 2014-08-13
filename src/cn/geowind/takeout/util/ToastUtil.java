package cn.geowind.takeout.util;

import android.content.Context;
import android.widget.Toast;
import cn.geowind.takeout.R;

/**
 * Toast 同意管理类（设计不合理）
 * 
 * @author 朱霜
 * @school University of South China
 * @date 2013.10
 */
public class ToastUtil {
	public static final int NO_NETWORK = 0;
	public static final int FAVORITE_SUCCESS = 1;
	public static final int CANCEL_FAVORITE_SUCCESS = 2;
	public static final int FEEDBACK_CONTENT_NO_EMPTY = 3;
	public static final int FEEDBACK_CONTENT_TOO_SHORT = 4;
	public static final int FEEDBACK_PHONE_NO_EMPTY = 5;
	public static final int FEEDBACK_PHONE_TOO_SHORT = 6;

	public static void toast(Context context, int type) {
		String message = "";
		int time = 0;
		switch (type) {
		case NO_NETWORK:
			message = context.getResources().getString(R.string.no_network);
			time = 1;
			break;
		case FAVORITE_SUCCESS:
			message = context.getResources().getString(
					R.string.favorite_success);
			break;
		case CANCEL_FAVORITE_SUCCESS:
			message = context.getResources().getString(
					R.string.cancel_favorite_success);
			break;
		}
		toast(context, message, time);
	}

	public static void toast(Context context, String message, int time) {
		Toast.makeText(context, message, time).show();
	}
}
