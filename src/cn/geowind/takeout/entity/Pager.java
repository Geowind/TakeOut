package cn.geowind.takeout.entity;

import com.android.volley.toolbox.NetworkImageView;

/**
 * Recommend Fragment 里面ViewPager每一项对应的实体类
 * 
 * @author 朱霜
 * @school University of South China
 * @date 2013.10
 */
public class Pager {
	/**
	 * Pager 的属性
	 */
	public NetworkImageView pager;
	public String position;
	public String img;
	public String data;
	public String title;
	public String type;
	public String others;

	/**
	 * 对应服务器端数据库中相应表的列名
	 */
	public static final String CLASS_NAME = "Pager";
	/**
	 * 图片
	 */
	public static final String IMG_URL = "img";
	/**
	 * 类型
	 */
	public static final String TYPE = "type";
	/**
	 * 标题
	 */
	public static final String TITLE = "title";
	/**
	 * 数据{网址url,Restaurant Id,Food Id}
	 */
	public static final String DATA = "data";
	/**
	 * 其他东西{Food Price,预留作用}
	 */
	public static final String OTHERS = "others";
	/**
	 * 显示的位置
	 */
	public static final String POSITION = "position";

	/**
	 * Pager的类型，共四种
	 */
	@Deprecated
	public static final String TYPE_FOOD = "food";
	public static final String TYPE_RESTAURANT = "restaurant";
	public static final String TYPE_TEXT = "text";
	public static final String TYPE_HTML = "html";
}
