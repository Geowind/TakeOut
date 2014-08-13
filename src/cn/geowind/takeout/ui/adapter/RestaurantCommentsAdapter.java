package cn.geowind.takeout.ui.adapter;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import cn.geowind.takeout.R;
import cn.geowind.takeout.entity.Comment;
import cn.geowind.takeout.ui.RestaurantCommentsActivity;
import cn.geowind.takeout.util.Utils;

import com.avos.avoscloud.AVObject;

/**
 * 评论Adapter
 * 
 * @author 朱霜
 * @school University of South China
 * @date 2013.11
 * @see RestaurantCommentsActivity
 */
public class RestaurantCommentsAdapter extends BaseAdapter {
	private Context context;
	private List<AVObject> list;
	private Resources res;
	private SimpleDateFormat mDateFormat;

	public RestaurantCommentsAdapter(Context context, List<AVObject> list) {
		super();
		this.context = context;
		this.list = list;
		res = context.getResources();
		mDateFormat = new SimpleDateFormat("MM-dd HH:mm", Locale.CHINA);
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		AVObject comment = list.get(position);
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = View.inflate(context, R.layout.comment_list_item,
					null);
			holder.authorLocale = (TextView) convertView
					.findViewById(R.id.comment_author_locale);
			holder.author = (TextView) convertView
					.findViewById(R.id.comment_author);
			holder.gender = (ImageView) convertView
					.findViewById(R.id.comment_author_gender);
			holder.time = (TextView) convertView
					.findViewById(R.id.comment_time);
			holder.content = (TextView) convertView
					.findViewById(R.id.comment_content);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.authorLocale.setText("["
				+ comment.getString(Comment.AUTHOR_LOCALE) + "]");
		holder.author.setText(comment.getString(Comment.AUTHOR));
		holder.content.setText(comment.getString(Comment.CONTENT));
		holder.time
				.setText("发表于 " + mDateFormat.format(comment.getCreatedAt()));
		if (comment.getString(Comment.AUTHOR_GENDER).equals("女")) {
			holder.gender.setImageDrawable(res.getDrawable(R.drawable.ic_girl));
		} else {
			holder.gender.setImageDrawable(res.getDrawable(R.drawable.ic_boy));
		}
		holder.reply = (TextView) convertView.findViewById(R.id.comment_reply);
		String reply = comment.getString(Comment.REPLY);
		if (reply != null) {
			holder.reply.setVisibility(View.VISIBLE);
			String text = "商家回复：" + reply;
			holder.reply.setText(Utils.hightLight(text, "#DD4814", 0, 4));
		} else {
			holder.reply.setVisibility(View.GONE);
		}
		return convertView;
	}

	static class ViewHolder {
		TextView authorLocale;
		TextView author;
		ImageView gender;
		TextView time;
		TextView content;
		TextView reply;
	}

}
