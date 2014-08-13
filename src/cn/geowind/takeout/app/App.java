package cn.geowind.takeout.app;

import android.app.Application;
import cn.geowind.takeout.ui.widget.BitmapLruCache;
import cn.jpush.android.api.JPushInterface;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.avos.avoscloud.AVAnalytics;
import com.avos.avoscloud.AVOSCloud;

/**
 * 总共用到云平台：七牛（图片服务器），AVOS Cloud（后端服务器，数据服务器），云测（测试云平台）
 * 
 * @author 朱霜
 * @school University of South China
 * @date 2013.09
 */
public class App extends Application {
	/**
	 * 接受商家通知的Action
	 */
	public static final String ACTION_ORDER_HANDLED = "order_handled";

	/**
	 * Index Fragment 里面ViewPager当前显示的索引
	 */
	public static int currentPagerItem = 0;
	/**
	 * 整个App的图片内存缓存类
	 */
	public static BitmapLruCache bitmapLruCache;
	/**
	 * 整个App内Volley相关类要用到的RequestQueue
	 */
	public static RequestQueue requestQueue;

	@Override
	public void onCreate() {
		super.onCreate();
		/* AVOS Cloud 初始化 */
		AVOSCloud.setNetworkTimeout(3000);
		AVOSCloud.useAVCloudCN();
		AVOSCloud.initialize(this,
				"uy5pt942okfgyvylm4lrgtl3qly1fj003z2hih9qkb5qvwzv",
				"jt5tdivgyywgci16516rszztw3vrzwx5p6wconetz7uhtidz");
		AVAnalytics.enableCrashReport(this, true);
		requestQueue = Volley.newRequestQueue(this);
		bitmapLruCache = new BitmapLruCache();

		// JPush推送初始化
		JPushInterface.init(this);

//		AVInstallation.getCurrentInstallation().saveInBackground();
	}

	/**
	 * 统计专用字段
	 */
	public static final class Event {
		/**
		 * 记录Pager的点击次数
		 */
		public static final String PAGER = "Pager";
		/**
		 * 记录摇一摇的次数
		 */
		public static final String SHAKE_FOOD = "ShakeFood";
		/**
		 * 记录关键字搜索的次数
		 */
		public static final String SEARCH = "Search";
		/**
		 * 记录类别搜索的次数
		 */
		public static final String CATEGORY_SEARCH = "CategorySearch";
		/**
		 * 记录用户点击Drawer的次数
		 */
		public static final String DRAWER = "Drawer";
		/**
		 * 记录用户打商家电话的次数
		 */
		public static final String CALL = "Call";
		/**
		 * 记录用户点击碗的次数
		 */
		public static final String BOWL = "Bowl";
	}
}
