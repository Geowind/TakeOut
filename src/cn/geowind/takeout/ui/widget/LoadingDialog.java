package cn.geowind.takeout.ui.widget;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.widget.TextView;
import cn.geowind.takeout.R;

/**
 * 自定义的ProgressDialog
 * 
 * @author 朱霜
 * @school University of South China
 * @date 2014.03
 */
public class LoadingDialog extends Dialog {

	/**
	 * 
	 * @param context
	 *            必须传所在Activity这个Context的引用, 否则会抛异常
	 *            android.view.WindowManager$BadTokenException: Unable to add
	 *            window -- token null is not for an application
	 * @param message
	 *            显示的文本信息
	 */
	public LoadingDialog(Context context, String message) {
		super(context, R.style.LoadingDialogStyle);
		setContentView(R.layout.dialog_loading);
		getWindow().setGravity(Gravity.CENTER);
		setCancelable(true);
		((TextView) findViewById(R.id.dialog_loading)).setText(message);
	}

	public LoadingDialog(Context context, int theme) {
		super(context, theme);
	}

	/**
	 * 设置对话框文本信息
	 * 
	 * @param message
	 */
	public void setMessage(String message) {
		((TextView) findViewById(R.id.dialog_loading)).setText(message);
	}

	public void setCancelable(boolean flag) {
		super.setCancelable(flag);
	}

}
