package cn.geowind.takeout.util;

/**
 * 统一管理缓存时间的工具类
 * 
 * @author 朱霜
 * @school University of South China
 * @date 2013.10
 */
public final class TimeUtil {
	public static final int MINUTE = 60000;// 60 * 1000
	public static final int HOUR = 3600000;// 60 * 60 * 1000
	public static final int DAY = 86400000;// 24 * 60 * 60 * 1000

	public static final int VIEWPAGER_COUNT = HOUR;
	public static final int VIEWPAGER_ADAPTER = HOUR;
	public static final int RECOMMEND_FRAGMENT = 3 * HOUR;
	public static final int RESTAURANT_MENU = HOUR / 2;
	public static final int TAKEOUT_FRAGMENT = HOUR;
	
	
}
