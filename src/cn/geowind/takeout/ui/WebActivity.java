package cn.geowind.takeout.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import cn.geowind.takeout.R;
import cn.geowind.takeout.entity.Pager;
import cn.geowind.takeout.util.ToastUtil;
import cn.geowind.takeout.util.Utils;

/**
 * 网页页面。用来显示一些HTML页面
 * 
 * @author 朱霜
 * @school University of South China
 * @date 2013.10
 */
public class WebActivity extends BaseActivity implements OnKeyListener {
	private ActionBar mActionBar;
	private WebView mWebView;
	private ProgressBar mProgressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_webview);

		Intent intent = getIntent();
		mActionBar = getSupportActionBar();
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setDisplayShowHomeEnabled(false);
		mActionBar.setTitle(intent.getStringExtra(Pager.TITLE));

		if (!Utils.isNetworkConnected(this)) {
			ToastUtil.toast(this, ToastUtil.NO_NETWORK);
			return;
		}
		mWebView = (WebView) findViewById(R.id.webview);
		mWebView.setOnKeyListener(this);
		mProgressBar = (ProgressBar) findViewById(R.id.progress_horizontal);
		WebSettings settings = mWebView.getSettings();
		settings.setJavaScriptEnabled(true);
		settings.setUserAgentString("Mozilla/5.0 (Windows; U; Windows NT 5.2) AppleWebKit/525.13 (KHTML, like Gecko) Chrome/0.2.149.27 Safari/525.13");
		mWebView.setWebViewClient(new WebViewClient() {

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				mProgressBar.setVisibility(View.VISIBLE);
				view.loadUrl(url);
				return true;
			}

		});
		mWebView.setWebChromeClient(new WebChromeClient() {

			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				super.onProgressChanged(view, newProgress);
				if (newProgress == 0) {
					mProgressBar.setVisibility(View.VISIBLE);
				}
				if (newProgress == 100) {
					mActionBar.setTitle(view.getTitle());
					mProgressBar.setVisibility(View.GONE);
				}
				mProgressBar.setProgress(newProgress);
				mProgressBar.postInvalidate();
			}

		});
		mWebView.loadUrl(intent.getStringExtra(Pager.DATA));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		finish();
		return true;
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			if (keyCode == KeyEvent.KEYCODE_BACK && mWebView.canGoBack()) {
				mWebView.goBack(); // 后退
				return true; // 已处理
			}
		}
		return false;
	}
}
