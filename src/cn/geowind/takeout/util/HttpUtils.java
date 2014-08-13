package cn.geowind.takeout.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 网络工具类，发送POST或GET请求
 * 
 * @author 朱霜
 * @school University of South China
 * @date 2013.09
 */
public class HttpUtils {

	/**
	 * 如果有异常，则返回 ""
	 * 
	 * @return
	 */
	public static String getUpdateInfo() {
		String updateUrl = "http://pager.u.qiniudn.com/update.txt";
		String info = "";
		try {
			info = get(updateUrl);
		} catch (IOException e) {
			e.printStackTrace();
			/* 如果有异常，则返回 "" */
			return info;
		}

		return info;
	}

	private static String get(String updateUrl) throws IOException {
		byte[] data = null;
		URL url = new URL(updateUrl);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setReadTimeout(3000);
		if (conn.getResponseCode() == 200) {
			InputStream is = conn.getInputStream();
			data = readInputStream(is);
		}
		return new String(data, "utf-8");
	}

	private static byte[] readInputStream(InputStream is) throws IOException {
		byte[] data = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = -1;
		while ((len = is.read(buffer)) != -1) {
			baos.write(buffer, 0, len);
		}
		is.close();
		baos.flush();
		baos.close();
		data = baos.toByteArray();
		return data;
	}

}
