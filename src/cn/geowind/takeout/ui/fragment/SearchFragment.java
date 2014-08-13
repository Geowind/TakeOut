package cn.geowind.takeout.ui.fragment;

import java.util.List;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import cn.geowind.takeout.R;
import cn.geowind.takeout.app.App;
import cn.geowind.takeout.app.App.Event;
import cn.geowind.takeout.entity.Food;
import cn.geowind.takeout.ui.CategorySearchActivity;
import cn.geowind.takeout.ui.FoodDetailActivity;
import cn.geowind.takeout.ui.MainActivity;
import cn.geowind.takeout.ui.SettingsActivity.Key;
import cn.geowind.takeout.ui.adapter.CategoryDetailAdapter;
import cn.geowind.takeout.ui.adapter.SearchAdapter;
import cn.geowind.takeout.ui.adapter.SimpleCategoryAdapter;
import cn.geowind.takeout.ui.widget.LoadingDialog;
import cn.geowind.takeout.ui.widget.SlidingPaneListView;
import cn.geowind.takeout.util.ToastUtil;
import cn.geowind.takeout.util.Utils;

import com.avos.avoscloud.AVAnalytics;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.CountCallback;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.LogUtil.log;

/**
 * 搜索Fragment
 * 
 * @author 朱霜
 * @school University of South China
 * @date 2013.09
 * @see MainActivity
 */
public class SearchFragment extends Fragment implements OnClickListener,
		OnScrollListener, OnItemSelectedListener, OnEditorActionListener {
	private FragmentActivity activity;
	private Resources res;
	private ActionBar actionBar;
	private View customActionBar;
	private TextView searchTitle;
	private EditText searchEdt;
	private ImageButton searchBtn;
	private TextWatcher textWatcher;
	private LoadingDialog mDialog;

	private LinearLayout searchTipsView;
	private ListView listView;
	private SlidingPaneListView categoryList;
	private ListView categoryDetailList;
	private CategoryDetailAdapter categoryDetailAdapter;

	private SearchAdapter mAdapter;
	private View listViewHeader;
	private View listViewFooter;
	private Spinner orderBySpinner;
	private TextView headerResultSize;
	private ProgressBar footerProgressBar;
	private TextView footerText;

	private List<AVObject> mList;
	private AVQuery<AVObject> mQuery;
	private String oldQueryString = "";
	private int resultSize;
	/**
	 * 保存上一次查询的resultSize，方便再次切换到Search Fragment的时候能够判断是否自动加载
	 */
	private int oldResultSize;
	private int lastItemIndex;
	private int queryOrderBy;
	private int categorySelectedPosition = 0;

	private String[] categorys = { "饭菜", "粉面", "主食", "快餐", "小吃", "饮料", "其他" };
	private String details[][] = new String[][] {
			new String[] { "炒菜", "煲仔饭", "炒饭", "盖浇饭", "烩饭" },
			new String[] { "粉", "拌面", "炒面", "汤面" },
			new String[] { "饼", "水饺", "粥", "馄饨" },
			new String[] { "汉堡", "鸡排", "西式", "小食", "披萨" },
			new String[] { "烧烤" }, new String[] { "冰品", "奶茶", "牛奶" },
			new String[] { "其他" } };
	private static final int ORDER_BY_PRICE = 0;
	private static final int ORDER_BY_HOT = 1;
	private static final int LIMIT_SIZE = 10;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		customActionBar = inflater.inflate(R.layout.ab_search_fragment, null,
				false);
		View view = inflater
				.inflate(R.layout.fragment_search, container, false);

		searchTipsView = (LinearLayout) view
				.findViewById(R.id.search_tips_view);
		categoryList = (SlidingPaneListView) searchTipsView
				.findViewById(R.id.category_list);
		categoryDetailList = (ListView) searchTipsView
				.findViewById(R.id.category_detail_list);

		listViewHeader = inflater.inflate(R.layout.search_list_header, null);
		listViewFooter = inflater.inflate(R.layout.list_footer_load_more, null);
		listView = (ListView) view.findViewById(R.id.listview);

		orderBySpinner = (Spinner) listViewHeader
				.findViewById(R.id.header_order_by);
		headerResultSize = (TextView) listViewHeader
				.findViewById(R.id.header_result_size);
		footerProgressBar = (ProgressBar) listViewFooter
				.findViewById(R.id.list_footer_progressbar);
		footerText = (TextView) listViewFooter
				.findViewById(R.id.list_footer_text);

		listView.addFooterView(listViewFooter, null, false);
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		activity = getActivity();
		res = activity.getResources();
		mDialog = new LoadingDialog(activity,
				res.getString(R.string.trying_to_loading));
		actionBar = MainActivity.getMainActionBar();
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setCustomView(customActionBar);

		searchTitle = (TextView) customActionBar
				.findViewById(R.id.search_title);
		searchTitle.setCompoundDrawablePadding(0);
		searchEdt = ((EditText) customActionBar
				.findViewById(R.id.action_search_edt));
		searchBtn = (ImageButton) customActionBar
				.findViewById(R.id.action_search_btn);
		searchEdt.requestFocus(View.FOCUS_RIGHT);
		searchEdt.setOnEditorActionListener(this);

		listView.setOnScrollListener(this);

		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				activity, R.array.order_by_array,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		orderBySpinner.setAdapter(adapter);
		orderBySpinner.setOnItemSelectedListener(this);

		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(activity);
		String orderPref = sharedPref.getString(Key.KEY_SEARCH_ORDER, "");
		String[] order = res.getStringArray(R.array.order_by_value_array);
		if (order[0].equals(orderPref)) {
			orderBySpinner.setSelection(0);
		} else if (order[1].equals(orderPref)) {
			orderBySpinner.setSelection(1);
		}

		queryOrderBy = orderBySpinner.getSelectedItemPosition();

		if (Utils.isNetworkConnected(activity)) {
			if (!oldQueryString.equals("")) {
				queryFood(oldQueryString, true);
				searchEdt.setText(oldQueryString);
				searchEdt.setSelection(oldQueryString.length());
				resultSize = oldResultSize;
			}
		} else {
			ToastUtil.toast(activity, ToastUtil.NO_NETWORK);
		}

		if (searchEdt.getText().toString().equals("")) {
			searchTipsView.setVisibility(View.VISIBLE);
			renderCategoryList();
		} else {
			searchTipsView.setVisibility(View.GONE);
		}

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent(activity, FoodDetailActivity.class);
				intent.putExtra(Food.CLASS_NAME, Food
						.parseFood((AVObject) parent
								.getItemAtPosition(position)));
				startActivity(intent);
				activity.overridePendingTransition(R.anim.in_from_right,
						R.anim.out_to_left);
			}
		});

		/**
		 * 监测搜索框字数的变化
		 */
		textWatcher = new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				if (!android.text.TextUtils.isEmpty(s)) {
					searchEdt.setCompoundDrawablesWithIntrinsicBounds(0, 0,
							R.drawable.ic_search_clear, 0);
					searchTitle.setCompoundDrawablesWithIntrinsicBounds(
							R.drawable.ic_arrow_left, 0, 0, 0);

				} else {
					searchEdt.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0,
							0);
				}
			}
		};

		searchEdt.addTextChangedListener(textWatcher);
		searchBtn.setOnClickListener(this);
		searchTitle.setOnClickListener(this);
		searchEdt.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					int x = (int) event.getX();
					if (x > searchEdt.getWidth() - 38
							&& !android.text.TextUtils.isEmpty(searchEdt
									.getText())) {
						searchEdt.setText("");
					}
				}
				return false;
			}
		});

	}

	protected void renderCategoryList() {
		final SimpleCategoryAdapter sca = new SimpleCategoryAdapter(activity,
				categorys);
		sca.setSelectedPosition(categorySelectedPosition);
		categoryList.setAdapter(sca);
		categoryDetailAdapter = new CategoryDetailAdapter(activity);
		categoryDetailAdapter
				.setCategoryClickPosition(categorySelectedPosition);
		categoryDetailList.setAdapter(categoryDetailAdapter);
		categoryList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				categorySelectedPosition = position;
				sca.setSelectedPosition(position);
				sca.notifyDataSetChanged();
				categoryDetailAdapter.setCategoryClickPosition(position);
				categoryDetailAdapter.notifyDataSetChanged();
			}
		});
		categoryDetailList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				AVAnalytics.onEvent(activity, App.Event.CATEGORY_SEARCH);
				String categoryQuery = details[categorySelectedPosition][position];
				Intent intent = new Intent(activity,
						CategorySearchActivity.class);
				System.out.println("categoryQuery" + categoryQuery);
				intent.putExtra(Food.CATEGORY, categoryQuery);
				startActivity(intent);
				activity.overridePendingTransition(R.anim.in_from_right,
						R.anim.out_to_left);
			}
		});
	}

	/**
	 * 
	 * @param queryString
	 * @param isOldQuery
	 *            true表示为旧关键字查询，false表示新关键字查询(
	 *            针对于从MainActivity内的其他Fragment切换过来的情况)
	 */
	protected void queryFood(String queryString, boolean isOldQuery) {
		mDialog.show();
		if (listView.getHeaderViewsCount() == 0) {
			listView.addHeaderView(listViewHeader, null, false);
		}
		mQuery = AVQuery.getQuery(Food.CLASS_NAME);
		mQuery.whereContains(Food.NAME, queryString);
		mQuery.setLimit(LIMIT_SIZE);
		if (ORDER_BY_PRICE == queryOrderBy) {
			mQuery.orderByAscending(Food.PRICE);
		} else if (ORDER_BY_HOT == queryOrderBy) {
			mQuery.orderByDescending(Food.HOT);
		}
		mQuery.findInBackground(new FindCallback<AVObject>() {

			@Override
			public void done(List<AVObject> list, AVException e) {
				if (e == null) {
					mList = list;
					if (mList.size() == 0) {
						listView.removeHeaderView(listViewHeader);
						footerText.setText(res
								.getString(R.string.search_no_result));
					} else {
						footerText.setText(res.getString(R.string.no_more));
					}
					mAdapter = new SearchAdapter(activity, mList);
					listView.setAdapter(mAdapter);
					mDialog.dismiss();
				} else {
					log.e(getTag(), "查询失败", e);
					mDialog.dismiss();
					Toast.makeText(activity, R.string.loading_fail,
							Toast.LENGTH_SHORT).show();
				}
			}
		});
		if (!isOldQuery) {
			mQuery.countInBackground(new CountCallback() {

				@Override
				public void done(int count, AVException e) {
					if (e == null) {
						resultSize = count;
						String s1 = res.getString(R.string.search_result1);
						String s2 = res.getString(R.string.search_result2);
						String text = s1 + resultSize + s2;
						headerResultSize.setText(Utils.hightLight(text,
								"#DD4814", s1.length(),
								text.length() - s2.length()));
					} else {
						log.e(getTag(), "查询失败", e);
					}
				}
			});
		}

	}

	private void loadMoreData() {
		mQuery = AVQuery.getQuery(Food.CLASS_NAME);
		mQuery.whereContains(Food.NAME, oldQueryString);
		mQuery.setSkip(mList.size());
		mQuery.setLimit(LIMIT_SIZE);
		/**
		 * 判断是以哪种方式排序
		 */
		if (ORDER_BY_PRICE == queryOrderBy) {
			mQuery.orderByAscending(Food.PRICE);
		} else if (ORDER_BY_HOT == queryOrderBy) {
			mQuery.orderByDescending(Food.HOT);
		}
		mQuery.findInBackground(new FindCallback<AVObject>() {

			@Override
			public void done(List<AVObject> list, AVException e) {
				if (e == null) {
					mList.addAll(list);
					mAdapter.notifyDataSetChanged();
				} else {
					log.e(getTag(), "查询失败", e);
				}
			}
		});
	}

	/**
	 * 处理点击搜索按钮的事件,在OnClick和OnEditAction方法中都调用了此方法
	 */
	private void performSearchClick() {
		Utils.hideInputMethod(activity);
		if (!Utils.isNetworkConnected(activity)) {
			ToastUtil.toast(activity, ToastUtil.NO_NETWORK);
			return;
		}
		AVAnalytics.onEvent(activity, Event.SEARCH);
		String queryString = searchEdt.getText().toString();
		if (queryString.equals("")) {
			return;
		}
		searchTipsView.setVisibility(View.GONE);
		listView.setVisibility(View.VISIBLE);
		queryFood(queryString, false);
		oldQueryString = queryString;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.action_search_btn:
			performSearchClick();
			break;
		case R.id.search_title:
			searchEdt.setText("");
			searchTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
			if (mList != null) {
				mList.clear();
			}
			listView.setVisibility(View.GONE);
			searchTipsView.setVisibility(View.VISIBLE);
			renderCategoryList();
			Utils.hideInputMethod(activity);
			break;
		}

	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		oldQueryString = searchEdt.getText().toString();
		oldResultSize = resultSize;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (lastItemIndex == resultSize) {
			footerText.setText(res.getString(R.string.no_more));
			footerProgressBar.setVisibility(View.GONE);
			return;
		}
		/**
		 * 因为加了一个HeaderView，所以这里adapter.getCount()后不需要减一
		 * 
		 */
		if (lastItemIndex == mAdapter.getCount() // - 1
				&& scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
			footerText.setText(res.getString(R.string.loading_more));
			footerProgressBar.setVisibility(View.VISIBLE);
			loadMoreData();
		} else {
			footerText.setText(res.getString(R.string.load_more));
			footerProgressBar.setVisibility(View.GONE);
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		/**
		 * ListView 的FooterView也会算到visibleItemCount中去，所以要再减去一
		 */
		lastItemIndex = firstVisibleItem + visibleItemCount - 1 - 1;
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		queryOrderBy = orderBySpinner.getSelectedItemPosition();
		queryFood(oldQueryString, false);
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (actionId == EditorInfo.IME_ACTION_SEARCH) {
			performSearchClick();
		}
		return false;
	}

}
