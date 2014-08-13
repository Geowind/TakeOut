package cn.geowind.takeout.entity;

/**
 * 订单实体类
 * 
 * @author 朱霜
 * @school University of South China
 * @date 2013.11
 */
public class Order {
	/**
	 * 服务器端数据库中对应表中的列名
	 */

	public static final String CLASS_NAME = "Order";
	public static final String OBJECT_ID = "objectId";
	public static final String OWNER = "owner";
	public static final String TELEPHONE = "telephone";
	public static final String DATA = "data";
	public static final String RESTAURANT_ID = "restaurantId";
	public static final String RESTAURANT_NAME = "restaurantName";
	public static final String TOTAL_PRICE = "totalPrice";
	public static final String REGISTRATION_ID = "registrationId";
	public static final String RST_REGISTRATIONID = "rstRegistrationId";
	public static final String LOCALE = "locale";
	public static final String DORMITORY = "dormitory";
	public static final String BEDROOM = "bedroom";
	public static final String OTHERS = "others";
	public static final String TIME = "time";
	public static final String IS_DEAL = "isDeal";
	public static final String IS_DONE = "isDone";
	public static final String COMMENT_ID = "commentId";

	String objectId;
	/**
	 * 下此订单的用户的id
	 */
	String owner;
	/**
	 * 下单者的手机号码，方便商家联系
	 */
	String telephone;
	/**
	 * 订单的详细json数据
	 */
	String data;
	/**
	 * 订单的外卖店ID
	 */
	String restaurantId;
	/**
	 * 订单的外卖店名
	 */
	String restaurantName;
	/**
	 * 订单总价格
	 */
	double totalPrice;
	/**
	 * 下单用户的registrationId，用于商家推送给用户
	 */
	String registrationId;
	/**
	 * 商家的registrationId,用于撤销订单、催单的时候推送
	 */
	String rstRegistrationId;
	/**
	 * 送餐的校区
	 */
	String locale;
	/**
	 * 送餐的宿舍楼
	 */
	String dormitory;
	/**
	 * 送餐的寝室号
	 */
	String bedroom;
	/**
	 * 订单额外的需求，比如多加点饭
	 */
	String others;
	/**
	 * long类型的下单时间
	 */
	String time;
	/**
	 * 商家是否受理订单
	 */
	boolean isDeal;
	/**
	 * 交易完成
	 */
	boolean isDone;
	/**
	 * 评论的Id
	 */
	String commentId;
}
