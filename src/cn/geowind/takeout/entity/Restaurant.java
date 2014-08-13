package cn.geowind.takeout.entity;

import java.util.Calendar;

import com.alibaba.fastjson.JSON;
import com.avos.avoscloud.AVObject;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 外卖店实体类
 * 
 * @author 朱霜
 * @school University of South China
 * @date 2013.09
 */
public class Restaurant implements Parcelable {

	/* 服务器端数据库中对应表中的字段 */
	public static final String CLASS_NAME = "Restaurant";
	public static final String OBJECT_ID = "objectId";
	public static final String NAME = "name";
	public static final String IMG_URL = "img";
	public static final String TEL1 = "tel1";
	public static final String TEL2 = "tel2";
	public static final String LOCALE = "locale";
	public static final String ADDRESS = "address";
	public static final String BUSINESS_TIME = "businessTime";
	public static final String IS_RUNNING = "isRunning";
	public static final String KEYWORDS = "keywords";
	public static final String DETAILS = "details";
	public static final String OTHERS = "others";
	/**
	 * 菜的价位字符串
	 */
	public static final String KIND = "kind";
	public static final String USRER_ID = "userId";
	public static final String AREA = "area";
	/**
	 * 特殊字段，推送专用
	 */
	public static final String REGISTRATION_ID = "registrationId";
	/**
	 * 注册码，防着对商家的恶意注册，并且方便在TakeOutFragment中对商家进行字典序排序
	 */
	public static final String REGISTER_CODE = "registerCode";

	public String objectId;
	public String name;
	public String img;
	public String tel1;
	public String tel2;
	public String locale;
	public String address;
	public String businessTime;
	public boolean isRunning;
	public String keywords;
	public String details;
	public String others;
	public String kind;
	public String userId;
	public String area;
	public String registrationId;
	public String registerCode;

	/**
	 * Restaurant的营业状态
	 * 
	 * @date 2014.06
	 */
	public enum Status {
		/**
		 * 正常营业中
		 */
		OK,
		/**
		 * 营业时间还未到
		 */
		TOO_EARLY,
		/**
		 * 营业时间已经过了
		 */
		TOO_LATER,
		/**
		 * 已经暂停营业
		 */
		STOP;
	}

	public static final Creator<Restaurant> CREATOR = new Creator<Restaurant>() {
		@Override
		public Restaurant[] newArray(int size) {
			return new Restaurant[size];
		}

		@Override
		public Restaurant createFromParcel(Parcel source) {
			Restaurant r = new Restaurant();
			r.objectId = source.readString();
			r.name = source.readString();
			r.img = source.readString();
			r.tel1 = source.readString();
			r.tel2 = source.readString();
			r.locale = source.readString();
			r.address = source.readString();
			r.businessTime = source.readString();
			r.isRunning = source.readByte() != 0;
			r.keywords = source.readString();
			r.details = source.readString();
			r.others = source.readString();
			r.kind = source.readString();
			r.userId = source.readString();
			r.area = source.readString();
			r.registrationId = source.readString();
			r.registerCode = source.readString();
			return r;
		}
	};

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(objectId);
		dest.writeString(name);
		dest.writeString(img);
		dest.writeString(tel1);
		dest.writeString(tel2);
		dest.writeString(locale);
		dest.writeString(address);
		dest.writeString(businessTime);
		dest.writeByte((byte) (isRunning ? 1 : 0));
		dest.writeString(keywords);
		dest.writeString(details);
		dest.writeString(others);
		dest.writeString(kind);
		dest.writeString(userId);
		dest.writeString(area);
		dest.writeString(registrationId);
		dest.writeString(registerCode);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	/**
	 * 把AVObject对象解析成Restaurant
	 * 
	 * @param obj
	 * @return
	 */
	public static Restaurant parseRestaurant(AVObject obj) {
		Restaurant r = new Restaurant();
		r.objectId = obj.getObjectId();
		r.name = obj.getString(NAME);
		r.img = obj.getString(IMG_URL);
		r.tel1 = obj.getString(TEL1);
		r.tel2 = obj.getString(TEL2);
		r.locale = obj.getString(LOCALE);
		r.address = obj.getString(ADDRESS);
		r.businessTime = obj.getString(BUSINESS_TIME);
		r.isRunning = obj.getBoolean(IS_RUNNING);
		r.keywords = obj.getString(KEYWORDS);
		r.details = obj.getString(DETAILS);
		r.others = obj.getString(OTHERS);
		r.kind = obj.getString(KIND);
		r.userId = obj.getString(USRER_ID);
		r.area = obj.getString(AREA);
		r.registrationId = obj.getString(REGISTRATION_ID);
		r.registerCode = obj.getString(REGISTER_CODE);
		return r;
	}

	/**
	 * 获取当前Restaurant的营业状态
	 * 
	 * @param restaurant
	 *            当前的Restaurant对象
	 * @return {@code OK}:表示正在营业 <br/>
	 *         {@code TOO_EARLY}:还没到营业时 <br/>
	 *         {@code TOO_LATER}:营业时间已过<br/>
	 *         {@code STOP}:表示已暂停营业<br/>
	 */
	public Status getStatus() {
		try {
			if (isRunning == false) {
				return Status.STOP;
			}
			String[] time = businessTime.split("-");
			String[] am = time[0].split(":");
			String[] pm = time[1].split(":");

			System.out.println("am[0]:" + am[0]);
			Calendar t = Calendar.getInstance();
			t.set(Calendar.HOUR_OF_DAY, Integer.parseInt(am[0]));
			t.set(Calendar.MINUTE, Integer.parseInt(am[1]));
			Calendar currentTime = Calendar.getInstance();
			if (currentTime.compareTo(t) == -1) {
				return Status.TOO_EARLY;
			}
			t.set(Calendar.HOUR_OF_DAY, Integer.parseInt(pm[0]));
			t.set(Calendar.MINUTE, Integer.parseInt(pm[1]));
			if (currentTime.compareTo(t) == 1) {
				return Status.TOO_LATER;
			}
		} catch (Exception e) {
			/* 如果发生了异常，则返回 STATUS_STOP */
			e.printStackTrace();
			return Status.STOP;
		}
		return Status.OK;
	}

	/**
	 * 2014.06
	 * 
	 * @param restaurant
	 * @return
	 */
	public static String toJson(Restaurant restaurant) {
		return JSON.toJSONString(restaurant);
	}

	/**
	 * 2014.06
	 * 
	 * @param jsonString
	 * @return
	 */
	public static Restaurant fromJson(String jsonString) {
		Restaurant restaurant = JSON.parseObject(jsonString, Restaurant.class);
		return restaurant;
	}
}
