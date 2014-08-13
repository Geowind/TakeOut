package cn.geowind.takeout.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import cn.geowind.takeout.R;
import cn.geowind.takeout.ui.BaseActivity;

/**
 * 方便统计，定义一个BaseFragment作为所有Fragment的父类。类似 {@link BaseActivity}
 * 
 * @author 朱霜
 * @school University of South China
 * @date 2014.01
 */
public class BaseFragment extends Fragment {

	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	public void onStart() {
		super.onStart();
	}

	@Override
	public void startActivity(Intent intent) {
		super.startActivity(intent);
		getActivity().overridePendingTransition(R.anim.in_from_right,
				R.anim.out_to_left);
	}

	@Override
	public void startActivityForResult(Intent intent, int requestCode) {
		super.startActivityForResult(intent, requestCode);
		getActivity().overridePendingTransition(R.anim.in_from_right,
				R.anim.out_to_left);
	}
}
