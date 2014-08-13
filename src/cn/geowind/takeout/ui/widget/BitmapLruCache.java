package cn.geowind.takeout.ui.widget;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.toolbox.ImageLoader.ImageCache;
import com.android.volley.toolbox.Volley;

/**
 * 开源项目Volley处理图片缓存时要用到的BitmapLruCache
 * 
 * @author 朱霜
 * @school University of South China
 * @date 2013.09
 * @see Volley
 */
public class BitmapLruCache implements ImageCache {

	private LruCache<String, Bitmap> mCache;

	public BitmapLruCache() {
		/**
		 * 最大内存缓存为10M
		 */
		int maxSize = 10 * 1024 * 1024;
		mCache = new LruCache<String, Bitmap>(maxSize) {
			@Override
			protected int sizeOf(String key, Bitmap value) {
				return value.getRowBytes() * value.getHeight();
			}
		};
	}

	@Override
	public Bitmap getBitmap(String url) {
		return mCache.get(url);
	}

	@Override
	public void putBitmap(String url, Bitmap bitmap) {
		mCache.put(url, bitmap);
	}

}