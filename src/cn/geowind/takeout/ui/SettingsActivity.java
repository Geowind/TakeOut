package cn.geowind.takeout.ui;

import android.content.Context;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import cn.geowind.takeout.R;
import cn.geowind.takeout.util.ToastUtil;
import cn.geowind.takeout.util.UpdateManager;
import cn.geowind.takeout.util.Utils;

import com.avos.avoscloud.AVQuery;

/**
 * 设置界面。V7包中的ActionBarActivity没有相应的Preference版，所以设置界面没有设返回按钮
 * 
 * @author 朱霜
 * @school University of South China
 * @date 2013.09
 */
public class SettingsActivity extends PreferenceActivity {
	private Context mContext;
	private Preference clearCache;
	private ListPreference searchOrder;
	private Preference checkUpdate;

	@Override
	protected void onResume() {
		super.onResume();
		// AVAnalytics.onResume(this);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		mContext = this;
		clearCache = findPreference("cache");
		searchOrder = (ListPreference) findPreference(Key.KEY_SEARCH_ORDER);
		checkUpdate = (Preference) findPreference(Key.KEY_CHECK_UPDATE);
		clearCache
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {
						AVQuery.clearAllCachedResults();
						Utils.clearCacheFolder(mContext.getCacheDir());
//						getSharedPreferences("", mode)
						ToastUtil.toast(SettingsActivity.this,
								getString(R.string.clear_cache_success), 0);
						return false;
					}
				});
		searchOrder
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

					@Override
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						if (((String) newValue).equals(Key.KEY_SEARCH_ORDER)) {
							searchOrder
									.setSummary(getString(R.string.order_by_price));
						} else {
							searchOrder
									.setSummary(getString(R.string.order_by_hot));
						}
						/**
						 * ListPreference的值必须返回true
						 */
						return true;
					}
				});
		checkUpdate
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {
						UpdateManager um = new UpdateManager(
								SettingsActivity.this);
						um.checkUpdate(false);
						return true;
					}
				});
	}

	@Override
	protected void onPause() {
		super.onPause();
		// AVAnalytics.onPause(this);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
	}

	/**
	 * 静态内部类，对应preference里面的key
	 */
	public static class Key {
		public static final String KEY_SEARCH_ORDER = "search_order";
		public static final String KEY_CACHE = "cache";
		public static final String KEY_AUTO_CHECK_UPDATE = "auto_check_update";
		public static final String KEY_CHECK_UPDATE = "check_update";
	}
}
