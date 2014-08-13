package cn.geowind.takeout.util;

import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * 推送工具类
 * 
 * @author 朱霜
 * @school University of South China
 * @date 2014.02
 */
public class PushUtils {
	public static final int MAX = Integer.MAX_VALUE;
	public static final int MIN = (int) MAX / 2;
	private static final String APP_KEY = "2cd2339ce564e4fdb120274d";
	private static final String MASTER_SECRET = "08bf94dfa868398c521d5da6";
	private static final String PUSH_TEXT = "您有新的订单。";
	private static final String UNDO_ORDER_TEXT = "用户撤销了订单。";
	private static final String URGE_ORDER_TEXT = "用户催单了。";

	/**
	 * 推送类别枚举
	 */
	public static enum PushType {
		/**
		 * 推送订单
		 */
		ORDER,
		/**
		 * 撤销订单
		 */
		UNDO,
		/**
		 * 催订单
		 */
		URGE
	}

	/**
	 * 推送
	 * 
	 * @param type
	 *            推送类型。
	 * @param registrationId
	 *            接受者的registrationId
	 * @param pushCallback
	 *            推送是否成功的回调
	 * 
	 * @see PushType
	 */
	public static void push(PushType type, String registrationId,
			final PushCallback pushCallback) {
		RequestParams p = new RequestParams();
		String sendNo = String.valueOf(getRandomSendNo());
		String receiver_type = "5";
		String input = String.valueOf(sendNo) + receiver_type + registrationId
				+ MASTER_SECRET;
		String verification_code = StringUtils.toMD5(input);
		String msg_type = "1";
		JSONObject obj = new JSONObject();
		try {
			if (type == PushType.ORDER) {
				obj.put("n_content", PUSH_TEXT);
				obj.put("n_extras", new JSONObject().put("id", registrationId)
						.put("type", "order"));
				System.out.println(obj.toString(4));
			} else if (type == PushType.UNDO) {
				obj.put("n_content", UNDO_ORDER_TEXT);
				obj.put("n_extras", new JSONObject().put("id", registrationId)
						.put("type", "undo"));
				System.out.println(obj.toString(4));
			} else if (type == PushType.URGE) {
				obj.put("n_content", URGE_ORDER_TEXT);
				obj.put("n_extras", new JSONObject().put("id", registrationId)
						.put("type", "urge"));
				System.out.println(obj.toString(4));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		String msg_content = obj.toString();
		String platform = "android";

		p.put("sendno", sendNo);
		p.put("app_key", APP_KEY);
		p.put("receiver_type", receiver_type);
		p.put("receiver_value", registrationId);
		p.put("verification_code", verification_code);
		p.put("msg_type", msg_type);
		p.put("msg_content", msg_content);
		p.put("platform", platform);
		AsyncHttpClient client = new AsyncHttpClient();
		client.setTimeout(5000);
		client.addHeader("Content-Type", "application/x-www-form-urlencoded");
		client.post("http://api.jpush.cn:8800/v2/push", p,
				new JsonHttpResponseHandler() {

					@Override
					public void onFailure(String responseBody, Throwable error) {
						super.onFailure(responseBody, error);
						pushCallback.onSuccess(false);
					}

					@Override
					public void onFailure(Throwable e, JSONObject errorResponse) {
						super.onFailure(e, errorResponse);
						pushCallback.onSuccess(false);
					}

					@Override
					public void onSuccess(JSONObject response) {
						super.onSuccess(response);
						try {
							if (response.getInt("errcode") == 0) {
								pushCallback.onSuccess(true);
							} else {
								pushCallback.onSuccess(false);
							}
							System.out.println(response.toString(4));
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}

				});
	}

	@Deprecated
	public static void push(String registrationId,
			final PushCallback pushCallback) {
		RequestParams p = new RequestParams();
		String sendNo = String.valueOf(getRandomSendNo());
		String receiver_type = "5";
		String input = String.valueOf(sendNo) + receiver_type + registrationId
				+ MASTER_SECRET;
		String verification_code = StringUtils.toMD5(input);
		String msg_type = "1";
		JSONObject obj = new JSONObject();
		try {
			obj.put("n_content", PUSH_TEXT);
			obj.put("n_extras", new JSONObject().put("id", registrationId));
			System.out.println(obj.toString(4));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		String msg_content = obj.toString();
		String platform = "android";

		p.put("sendno", sendNo);
		p.put("app_key", APP_KEY);
		p.put("receiver_type", receiver_type);
		p.put("receiver_value", registrationId);
		p.put("verification_code", verification_code);
		p.put("msg_type", msg_type);
		p.put("msg_content", msg_content);
		p.put("platform", platform);
		AsyncHttpClient client = new AsyncHttpClient();
		client.setTimeout(5000);
		client.addHeader("Content-Type", "application/x-www-form-urlencoded");
		client.post("http://api.jpush.cn:8800/v2/push", p,
				new JsonHttpResponseHandler() {

					@Override
					public void onFailure(String responseBody, Throwable error) {
						super.onFailure(responseBody, error);
						pushCallback.onSuccess(false);
					}

					@Override
					public void onFailure(Throwable e, JSONObject errorResponse) {
						super.onFailure(e, errorResponse);
						pushCallback.onSuccess(false);
					}

					@Override
					public void onSuccess(JSONObject response) {
						super.onSuccess(response);
						try {
							if (response.getInt("errcode") == 0) {
								pushCallback.onSuccess(true);
							} else {
								pushCallback.onSuccess(false);
							}
							System.out.println(response.toString(4));
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}

				});
	}

	/**
	 * 保持 sendNo 的唯一性是有必要的 It is very important to keep sendNo unique.
	 * 
	 * @return sendNo
	 */
	public static int getRandomSendNo() {
		return (int) (MIN + Math.random() * (MAX - MIN));
	}

	/**
	 * 推送回调接口
	 * 
	 * @author 朱霜
	 * @school University of South China
	 * @date 2014.03
	 */
	public interface PushCallback {
		public void onSuccess(boolean flag);
	}
}
