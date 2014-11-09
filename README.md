#TakeOut             

![](https://github.com/Folyd/TakeOut/blob/master/res/drawable-xxhdpi/logo.png)


##简介
  外卖小助手是南华大学经纬度团队从2013年开始研发的一款针对高校的外卖叫餐App。
  
  2014年3月25号在南华大学正式上线推广试运营，试运营达3个月之久，后期因美团外卖、饿了么这些专业的外卖平台进入校园，导致竞争过于激烈，学生团队力不从心，2014年暑假之后，外卖小助手停止运营。
  
  
  
##项目

####第三方依赖库
外卖小助手依赖如下几个第三方库：

- **[Android-ViewPagerIndicator](https://github.com/JakeWharton/Android-ViewPagerIndicator)**
- **android-support-v7-appcompat** （$ANDROID_SDK_HOME/extras/android/support/v7/appcompat）
- **[SlidingUpPanel](https://github.com/Folyd/SlidingUpPanel)**

请将如上三个第三方库 `git clone` 到 **../** 目录下面。
可以参考 **project.properties** 文件：

    target=android-14
    android.library.reference.1=../SlidingUpPanel
    android.library.reference.2=../Android-ViewPagerIndicator/library
    android.library.reference.3=../android-support-v7-appcompat



####依赖jar包
- **AVOS Cloud 相关** (外卖小助手后端云服务sdk)
   - android-async-http-1.4.4-fix.jar
   - AndroidSDKComponent.jar
   - avoscloud-v2.5.9.jar
   - avospush-v2.5.9.jar
   - avosstatistics-v2.5.7.jar
   - fastjson.jar
- **jpush-sdk-release1.6.1.jar**(订单推送所用到的极光推送sdk)
- **volley.jar** (图片异步加载、内存缓存)
- **android-supprot-v4.jar**  （Android官方v4兼容库）



更多详细文档可以参考 **[documentation.doc](https://github.com/Folyd/TakeOut/blob/master/documentation.doc)**





```
Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
```
