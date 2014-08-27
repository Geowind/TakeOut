package cn.geowind.takeout.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.view.inputmethod.InputMethodManager;

/**
 * 工具类
 * 
 * @author 朱霜
 * @school University of South China
 * @date 2013.09
 */
public class Utils {
	public static final String PICTURE_NOT_AVAILABLE = "http://pager.u.qiniudn.com/picture_not_available.jpg";

	/**
	 * 输入完毕之后按下搜索按钮自动隐藏输入法，增加用户体验
	 */
	public static void hideInputMethod(Activity activity) {
		((InputMethodManager) activity
				.getSystemService(Context.INPUT_METHOD_SERVICE))
				.hideSoftInputFromWindow(activity.getCurrentFocus()
						.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	}

	/**
	 * 将long类型的时间转换成日期类型
	 * 
	 * @param time
	 * @return
	 */
	public static String convertLongTimeToDate(long time) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
				Locale.CHINA);
		return sdf.format(new Date(time));
	}

	/**
	 * 检查当前是否联网
	 * 
	 * @return
	 */
	public static boolean isNetworkConnected(Context context) {
		ConnectivityManager connMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		return (networkInfo != null && networkInfo.isConnected());
	}

	/**
	 * 判断是否在wifi状态下
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isWifiConnected(Context context) {
		ConnectivityManager connMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connMgr.getActiveNetworkInfo();
		/* 这里一定要判断一下info 是否等于null*/
		return (info != null && ConnectivityManager.TYPE_WIFI == info.getType());
	}

	/**
	 * 高亮字符串中特定字段
	 * 
	 * @param text
	 * @param color
	 * @param start
	 * @param end
	 * @return
	 */
	public static SpannableString hightLight(String text, String color,
			int start, int end) {
		SpannableString span = new SpannableString(text);
		span.setSpan(new ForegroundColorSpan(Color.parseColor(color)), start,
				end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
		return span;
	}

	public static SpannableString hightLight(String text, int color, int start,
			int end) {
		SpannableString span = new SpannableString(text);
		span.setSpan(new ForegroundColorSpan(color), start, end,
				SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
		return span;
	}

	/**
	 * 给字符串特定字段加下划线
	 * 
	 * @param deleteValue
	 * @param start
	 * @param end
	 * @return
	 */
	public static SpannableString strokeStyle(String deleteValue, int start,
			int end) {
		SpannableString span = new SpannableString(deleteValue);
		span.setSpan(new StrikethroughSpan(), start, end,
				SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
		return span;
	}

	/**
	 * 用正则表达式验证邮箱是否正确
	 * 
	 * @param email
	 * @return
	 */
	public static boolean checkEmail(String email) {
		final String REGEXP = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
		Pattern p = Pattern.compile(REGEXP);
		Matcher m = p.matcher(email);
		return m.matches();
	}

	/**
	 * 用正则表达式验证邮箱是否正确
	 * 
	 * @param number
	 * @return
	 */
	public static boolean checkMobile(String number) {
		final String REGEXP = "^((13[0-9])|(15[^4,\\D])|(18[0-9]))\\d{8}$";
		Pattern p = Pattern.compile(REGEXP);
		Matcher m = p.matcher(number);
		return m.matches();
	}

	/**
	 * 检测字符串是否符合要求，不能包含特殊字符
	 * 
	 * @param str
	 * @return 符合要求返回{@code true}，否则返回{@code false}
	 */
	public static boolean checkString(String str) {
		final String REGEXP = "^[\u4E00-\u9FA5A-Za-z0-9_]+$";
		Pattern p = Pattern.compile(REGEXP);
		Matcher m = p.matcher(str);
		System.out.println("regexp:" + str.matches(REGEXP));
		return m.matches();
	}

	/**
	 * 递归调用删除缓存文件
	 * 
	 * @param dir
	 */
	public static boolean clearCacheFolder(File dir) {
		System.out.println(dir.getPath());
		if (dir != null && dir.isDirectory()) {
			try {
				for (File child : dir.listFiles()) {
					// if (child.isDirectory()) {
					clearCacheFolder(child);
					// } else {
					// child.delete();
					// }
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		} else {
			return dir.delete();
		}
	}

	/**
	 * 简单的图片加载方法
	 * 
	 * @param urlStr
	 * @return
	 * @throws IOException
	 */
	public static Bitmap getBitmap(String urlStr) throws IOException {
		Bitmap bitmap;
		URL url = new URL(urlStr);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setReadTimeout(5 * 1000);
		conn.setDoInput(true);
		conn.connect();
		InputStream is = conn.getInputStream();
		// byte[] image_byte = StreamUtil.readInputStream(is);
		bitmap = BitmapFactory.decodeStream(is);
		is.close();
		return bitmap;
	}
}
