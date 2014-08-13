package cn.geowind.takeout.ui.fragment;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import cn.geowind.takeout.R;
import cn.geowind.takeout.entity.Food;
import cn.geowind.takeout.ui.FoodDetailActivity;
import cn.geowind.takeout.ui.adapter.RecommendGridAdapter;
import cn.geowind.takeout.util.TimeUtil;
import cn.geowind.takeout.util.Utils;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVQuery.CachePolicy;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.LogUtil.log;

/**
 * 推荐页的Fragment
 * 
 * @author 朱霜
 * @school University of South China
 * @date 2014.03
 */
public class RecommendFragment extends BaseFragment {
	private FragmentActivity activity;
	private AVQuery<AVObject> mQuery;
	private GridView mGridView;
	private RecommendGridAdapter mGridAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activity = getActivity();
		mQuery = AVQuery.getQuery(Food.CLASS_NAME);
		mQuery.setCachePolicy(CachePolicy.CACHE_ELSE_NETWORK);
		mQuery.setMaxCacheAge(TimeUtil.RECOMMEND_FRAGMENT);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_recommend, container,
				false);
		mGridView = (GridView) view.findViewById(R.id.gridview);
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if (mQuery.hasCachedResult() || Utils.isNetworkConnected(activity)) {
			mQuery.whereEqualTo(Food.RECOMMEND, true);
			mQuery.findInBackground(new FindCallback<AVObject>() {

				@Override
				public void done(final List<AVObject> list, AVException e) {
					if (e == null) {
						mGridAdapter = new RecommendGridAdapter(activity, list);
						mGridView.setAdapter(mGridAdapter);
						mGridView
								.setOnItemClickListener(new OnItemClickListener() {

									@Override
									public void onItemClick(
											AdapterView<?> parent, View view,
											int position, long id) {
										Intent intent = new Intent(activity,
												FoodDetailActivity.class);
										intent.putExtra(Food.CLASS_NAME, Food
												.parseFood(list.get(position)));
										startActivity(intent);
									}
								});
					} else {
						log.e(getTag(), "加载推荐列表异常", e);
					}
				}
			});
		}
	}
}
