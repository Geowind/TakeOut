package cn.geowind.takeout.entity;

import cn.geowind.takeout.util.UpdateManager;

/**
 * 更新实体类
 * 
 * @author 朱霜
 * @school University of South China
 * @date 2013.10
 * @see UpdateManager
 */
public class Update {

	public int versionCode;
	public String versionName;
	public String updateTime;
	public String appSize;
	public String appUrl;
	public String updateDetails;

	@Override
	public String toString() {
		return "Update [versionCode=" + versionCode + ", versionName="
				+ versionName + ", updateTime=" + updateTime + ", appSize="
				+ appSize + ", appUrl=" + appUrl + "]";
	}

}
