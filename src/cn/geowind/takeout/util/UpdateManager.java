package cn.geowind.takeout.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import cn.geowind.takeout.R;
import cn.geowind.takeout.entity.Update;

/**
 * App版本更新管理类
 * 
 * @author 朱霜
 * @school University of South China
 * @date 2013.10
 * @see Update
 */
public class UpdateManager {
	private Context context;

	private static final int CHECKED_NEW_VERSION = 0x01; // 检测到新版本
	private static final int NOT_CHECKED_NEW_VERSION = 0x02;// 没有检测到新版本
	private static final int DOWNLOAD = 0x03; // 下载新版本
	private static final int DOWNLOAD_FROM_BROWSER = 0x04; // 去浏览器下载新版本
	private static final int DOWNLOAD_FINISH = 0x05;// 新版本下载在完成

	private Update update;
	private UpdateHandler handler;

	public UpdateManager(Context context) {
		super();
		this.context = context;
		handler = new UpdateHandler(context);
	}

	/**
	 * 检查App是否有新版本
	 * 
	 * @param auto
	 *            true为自动检查更新,false为用户手动检查更新
	 */
	public void checkUpdate(final boolean auto) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				String json = HttpUtils.getUpdateInfo();
				/* 如果解析的时候有异常，返回空字符串，并且是自动检测更新，则弹出Toast提示没有最新版。 */
				if ("".equals(json) && !auto) {
					handler.sendEmptyMessage(NOT_CHECKED_NEW_VERSION);
				}
				update = fromJson(json);
				if (update.versionCode > getAppVersionCode()) {
					Message msg = new Message();
					msg.obj = update;
					msg.what = CHECKED_NEW_VERSION;
					handler.sendMessage(msg);
				} else {
					if (!auto) {
						handler.sendEmptyMessage(NOT_CHECKED_NEW_VERSION);
					}
				}
			}
		}).start();

	}

	/**
	 * 下载新版的APK
	 * 
	 * @param url
	 * @return
	 */
	public boolean downloadApk(String url) {
		return false;
	}

	protected PackageInfo getPackageInfo() {
		PackageInfo pi = null;
		try {
			pi = context.getPackageManager().getPackageInfo(
					"cn.geowind.takeout", 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return pi;
	}

	/**
	 * 获取当前App的版本名称
	 * 
	 * @param context
	 * @return
	 */
	public String getAppVersionName() {
		return getPackageInfo().versionName;
	}

	/**
	 * 获取当前App的版本号
	 * 
	 * @return
	 */
	public int getAppVersionCode() {
		return getPackageInfo().versionCode;
	}

	public String getDiskCachePath() {
		final String cachePath;
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			cachePath = context.getExternalCacheDir().getPath();
		} else {
			cachePath = context.getCacheDir().getPath();
		}
		return cachePath;
	}

	public void showDialog() {

	}

	static class UpdateHandler extends Handler {
		private WeakReference<Context> c;
		private Context context;
		private LayoutInflater inflater;
		private Update update;

		public UpdateHandler(Context context) {
			c = new WeakReference<Context>(context);
			inflater = LayoutInflater.from(context);

		}

		public UpdateHandler(Looper looper, Context context) {
			super(looper);
			c = new WeakReference<Context>(context);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			context = c.get();
			if (context == null) {
				return;
			}
			switch (msg.what) {
			case CHECKED_NEW_VERSION:
				update = (Update) msg.obj;
				AlertDialog.Builder builder = new Builder(context);
				TextView title = (TextView) inflater.inflate(
						R.layout.dialog_custom_title, null);
				title.setText(R.string.checked_update);
				ViewGroup view = (ViewGroup) inflater.inflate(
						R.layout.dialog_update, null);
				((TextView) view.findViewById(R.id.update_version))
						.setText("版本号：" + update.versionName);
				((TextView) view.findViewById(R.id.update_size)).setText("大小："
						+ update.appSize);
				((TextView) view.findViewById(R.id.update_time)).setText("时间："
						+ update.updateTime);
				builder.setCustomTitle(title)
						.setView(view)
						.setPositiveButton(R.string.download_from_market,
								new OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										Intent intent = new Intent(
												Intent.ACTION_VIEW,
												Uri.parse("market://search?q=pname:cn.geowind.takeout"));
										context.startActivity(intent);
									}
								})
						.setNeutralButton(R.string.download_from_browser,
								new OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										Intent downloadIntent = new Intent(
												Intent.ACTION_VIEW, Uri
														.parse(update.appUrl));
										context.startActivity(downloadIntent);
									}
								})
						.setNegativeButton(R.string.temporarily_not_update,
								new OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
									}
								});
				builder.create().show();
				break;
			case NOT_CHECKED_NEW_VERSION:
				Toast.makeText(context, R.string.not_has_new_version,
						Toast.LENGTH_LONG).show();
				break;
			case DOWNLOAD_FROM_BROWSER:
				break;
			case DOWNLOAD:
				final NotificationManager nm = ((NotificationManager) context
						.getSystemService(Context.NOTIFICATION_SERVICE));
				final NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(
						context);
				final int id = 0;
				nBuilder.setSmallIcon(R.drawable.ic_call)
						.setContentTitle("正在下载更新版本")
						.setContentText("外卖小助手1.1版本");
				new Thread(new Runnable() {

					@Override
					public void run() {
						UpdateManager um = new UpdateManager(context);
						String path = um.getDiskCachePath() + File.separator
								+ "update";
						File file = new File(path);
						if (!file.exists()) {
							file.mkdir();
						}
						try {
							URL url = new URL(update.appUrl);
							HttpURLConnection conn = (HttpURLConnection) url
									.openConnection();
							conn.connect();
							// apkLength = conn.getContentLength();
							InputStream is = conn.getInputStream();
							File apkFile = new File(file, update.versionName);
							FileOutputStream fos = new FileOutputStream(apkFile);
							byte[] buff = new byte[1024];
							int len;
							do {
								len = is.read(buff);
								nBuilder.setProgress(100, len, false);
								nm.notify(id, nBuilder.build());
								fos.write(buff, 0, len);
							} while (len <= 0);
							is.close();
							fos.close();
						} catch (MalformedURLException e1) {
							e1.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
						nBuilder.setContentText("下载完成")
								.setProgress(0, 0, false);
						nm.notify(id, nBuilder.build());
					}
				}).start();

				break;
			case DOWNLOAD_FINISH:

				break;
			}
		}
	}

	public Update fromJson(String jsonString) {
		Update update = new Update();
		try {
			JSONObject json = new JSONObject(jsonString);
			update.versionCode = json.getInt("version");
			update.versionName = json.getString("name");
			update.updateTime = json.getString("time");
			update.appUrl = json.getString("url");
			update.appSize = json.getString("size");
			update.updateDetails = json.getString("detail");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return update;
	}
}
