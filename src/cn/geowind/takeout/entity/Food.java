package cn.geowind.takeout.entity;

import org.json.JSONException;
import org.json.JSONObject;

import com.avos.avoscloud.AVObject;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 每一样菜的实体类
 * 
 * @author 朱霜
 * @school University of South China
 * @date 2013.09
 */
public class Food implements Parcelable {

	/**
	 * 服务器端数据库中对应表中的列名
	 */
	public static final String CLASS_NAME = "Food";
	public static final String OBJECT_ID = "objectId";
	public static final String NAME = "name";
	public static final String IMG_URL = "img";
	public static final String PRICE = "price";
	public static final String OLD_PRICE = "oldPrice";
	public static final String RANK = "rank";
	public static final String SPECIALTY = "specialty";
	public static final String DESCRIPTION = "description";
	public static final String CATEGORY = "category";
	public static final String FAVORITES = "favorites";
	public static final String RESTURANT_ID = "restaurantId";
	public static final String RESTURANT = "restaurant";
	public static final String RECOMMEND = "recommend";
	public static final String HOT = "hot";
	public static final String AMOUNT = "amount";
	public static final String TIME = "time";

	public String objectId;
	public String name;
	public String img;
	public double price;
	public double oldPrice;
	public String rank;
	public boolean specialty;
	public String description;
	public String category;
	public int favorites;
	public String restaurantId;
	public String restaurant;
	/**
	 * 是否为推荐菜
	 */
	public boolean recommend;
	/**
	 * 这样菜被点过多少次
	 */
	public int hot;

	/**
	 * 加入订单或者收藏的时间，主要方便按时间排序，服务器数据库没有此字段
	 */
	public long time;
	/**
	 * 特殊属性，用在订单，表示有多少样这样的菜,其他地方不使用此属性,服务器数据库没有此字段
	 */
	public int amount;

	public static final Creator<Food> CREATOR = new Creator<Food>() {

		@Override
		public Food[] newArray(int size) {
			return new Food[size];
		}

		@Override
		public Food createFromParcel(Parcel source) {
			Food food = new Food();
			food.objectId = source.readString();
			food.name = source.readString();
			food.img = source.readString();
			food.price = source.readDouble();
			food.oldPrice = source.readDouble();
			food.rank = source.readString();
			/**
			 * Parcelable 没有暂时没有提供 readBoolean之类的方法，但可以通过这样的方式实现
			 */
			food.specialty = source.readByte() != 0;
			food.description = source.readString();
			food.category = source.readString();
			food.favorites = source.readInt();
			food.restaurantId = source.readString();
			food.restaurant = source.readString();
			food.recommend = source.readByte() != 0;
			food.hot = source.readInt();
			return food;
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	/**
	 * {@link http://stackoverflow
	 * .com/questions/6201311/how-to-read-write-a-boolean
	 * -when-implementing-the-parcelable-interface}
	 */
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(objectId);
		dest.writeString(name);
		dest.writeString(img);
		dest.writeDouble((Double) price);
		dest.writeDouble(oldPrice);
		dest.writeString(rank);
		/**
		 * Parcelable借口暂时没有提供writeBoolean()这样的方法，但可以通过这样的方式实现 link:
		 */
		dest.writeByte((byte) (specialty ? 1 : 0));
		dest.writeString(description);
		dest.writeString(category);
		dest.writeInt(favorites);
		dest.writeString(restaurantId);
		dest.writeString(restaurant);
		dest.writeByte((byte) (recommend ? 1 : 0));
		dest.writeInt(hot);
	}

	@Override
	public String toString() {
		return "Food [objectId=" + objectId + ", name=" + name + ", img=" + img
				+ ", price=" + price + ", oldPrice=" + oldPrice + ", rank="
				+ rank + ", specilty=" + specialty + ", description="
				+ description + ", category=" + category + ", favorites="
				+ favorites + ", restaurantId=" + restaurantId
				+ ", restaurant=" + restaurant + ", time=" + time + ", amount="
				+ amount + "]";
	}

	/**
	 * 从AVObject解析成实体类，替代子类化AVObject的方式
	 * 
	 * @param obj
	 * @return
	 */
	public static Food parseFood(AVObject obj) {
		Food food = new Food();
		food.objectId = obj.getObjectId();
		food.name = obj.getString(NAME);
		food.img = obj.getString(IMG_URL);
		food.price = obj.getDouble(PRICE);
		food.oldPrice = obj.getDouble(OLD_PRICE);
		food.rank = obj.getString(RANK);
		food.specialty = obj.getBoolean(SPECIALTY);
		food.description = obj.getString(DESCRIPTION);
		food.category = obj.getString(CATEGORY);
		food.favorites = obj.getInt(FAVORITES);
		food.restaurantId = obj.getString(RESTURANT_ID);
		food.restaurant = obj.getString(RESTURANT);
		food.recommend = obj.getBoolean(RECOMMEND);
		food.hot = obj.getInt(HOT);
		return food;
	}

	public static String toJson(Food food) throws JSONException {
		return toJSONObject(food).toString();
	}

	public static JSONObject toJSONObject(Food food) throws JSONException {
		JSONObject obj = new JSONObject();
		obj.put(OBJECT_ID, food.objectId);
		obj.put(NAME, food.name);
		obj.put(IMG_URL, food.img);
		obj.put(PRICE, food.price);
		obj.put(FAVORITES, food.favorites);
		obj.put(RESTURANT, food.restaurant);
		obj.put(RESTURANT_ID, food.restaurantId);
		obj.put(TIME, food.time);
		return obj;
	}

	public static Food fromJson(String jsonString) throws JSONException {
		JSONObject obj = new JSONObject(jsonString);
		Food food = new Food();
		food.objectId = obj.getString(OBJECT_ID);
		food.name = obj.getString(NAME);
		food.img = obj.getString(IMG_URL);
		food.price = obj.getDouble(PRICE);
		food.favorites = obj.getInt(FAVORITES);
		food.restaurant = obj.getString(RESTURANT);
		food.restaurantId = obj.getString(RESTURANT_ID);
		food.time = obj.getLong(TIME);
		return food;
	}

	/**
	 * 将订单中所需要的Food信息序列化成json字符串
	 * 
	 * @param food
	 * @return
	 * @throws JSONException
	 */
	public static String toOrderJson(Food food) throws JSONException {
		JSONObject obj = new JSONObject();
		/**
		 * 必须要有objectId
		 */
		obj.put(OBJECT_ID, food.objectId);
		obj.put(NAME, food.name);
		obj.put(PRICE, food.price);
		obj.put(AMOUNT, food.amount);
		/* 外卖小助手1.4新增 */
		obj.put(DESCRIPTION, food.description == null?"":food.description);
		obj.put(TIME, food.time);
		return obj.toString();
	}

	public static Food fromOrderJson(String jsonString) throws JSONException {
		System.out.println(jsonString);
		Food food = new Food();
		JSONObject obj = new JSONObject(jsonString);
		food.objectId = obj.getString(OBJECT_ID);
		food.name = obj.getString(NAME);
		food.price = obj.getDouble(PRICE);
		food.amount = obj.getInt(AMOUNT);
		/* 外卖小助手1.4新增 */
		food.description = obj.getString(DESCRIPTION);
		food.time = obj.getLong(TIME);
		return food;
	}
}
