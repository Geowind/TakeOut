package cn.geowind.takeout.entity;

/**
 * 评论实体类
 * 
 * @author 朱霜
 * @school University of South China
 * @date 2013.11
 */
public class Comment {

	/**
	 * 服务器端数据库中对应表中的列名
	 */
	public static final String CLASS_NAME = "Comment";
	public static final String OBJECT_ID = "object_id";
	public static final String CONTENT = "content";
	public static final String AUTHOR = "author";
	public static final String AUTHOR_ID = "authorId";
	public static final String AUTHOR_LOCALE = "authorLocale";
	public static final String AUTHOR_GENDER = "authorGender";
	public static final String RESTAURANT = "restaurant";
	public static final String RESTAURANT_ID = "restaurantId";
	public static final String REPLY = "reply";

	public String objectId;
	public String content;
	public String author;
	public String authorId;
	public String authorLocale;
	public String authorGender;
	public String restaurant;
	public String restaurantId;
	/**
	 * 商家的回复
	 */
	public String reply;
}
