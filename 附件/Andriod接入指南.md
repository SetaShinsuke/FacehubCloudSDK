##Android接入步骤

###接入之前

- 申请app_id
- 服务器

###开始接入
1. 在gradle中加入facehubSDKLib1.0.0-release.aar

~~~
//将facehubSDKLib1.0.0-release.aar 复制到app/libs下
//修改project的build.gradle
allprojects {
   repositories {
      jcenter()
      flatDir {
        dirs 'libs'
      }
   }
}
//修改Module:app的build.gradle
dependencies {
    compile(name:'facehubSDKLib-1.0.0-release', ext:'aar')
      
    compile 'com.android.support:appcompat-v7:23.2.1'
    compile 'com.android.support:recyclerview-v7:23.2.1'
    compile 'com.loopj.android:android-async-http:1.4.9'
    compile 'de.greenrobot:eventbus:2.4.0'
    compile 'com.nostra13.universalimageloader:universal-image-loader:1.9.5'
    compile 'com.android.support:design:23.1.1'
 }
~~~

2. 在你的application onCreate时调用初始化：

~~~java
    FacehubApi.init(getApplicationContext()); //初始化
    FacehubApi.initViews(getApplicationContext()); //初始化api中的views
    FacehubApi.getApi().setAppId(APP_ID); //设置应用ID
    //FacehubApi.getApi.setThemeColor(colorString );设置主色调，默认为面馆红(#f33847)//colorString 一个表示颜色RGB的字符串，例如"#f33847";
~~~
3. 在你的登陆回调中加入：
    
~~~java
    FacehubApi.getApi().login(USER_ID, AUTH_TOKEN, new ResultHandlerInterface() {
                @Override
                public void onResponse(Object response) {
                    //all done
                }
                @Override
                public void onError(Exception e) {
                    //some thing errror, please retry or show
                    //message to user
                }
    }, new ProgressInterface() {
                                @Override
                                public void onProgress(double process) {
                                    //update your progress bar
                                }
        });
~~~
4. 在你的聊天界面中加入：

~~~xml
    <!--通常放置在你的聊天输入框下方  -->
     <com.azusasoft.facehubcloudsdk.views.EmoticonKeyboardView
                    android:id="@+id/chatting_keyboard"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content">
    </com.azusasoft.facehubcloudsdk.views.EmoticonKeyboardView>
~~~
初始化View：

~~~java
    EmoticonKeyboardView mEmoticonKeyboard = findViewById(R.id.chatting_keyboard);
    mEmoticonKeyboard.initKeyboard();
~~~

增加监听器：
    
~~~java
     mEmoticonKeyboard.setEmoticonSendListener(new EmoticonSendListener() {
                @Override
                public void onSend(Emoticon emoticon) {
                    String id = emoticon.getId();
                    String filePath = emoticon.getFilePath(Image.Size.FULL);
                    //send and display the emoticon
                }
            });
~~~

显示和隐藏键盘：

 ~~~java
    mEmoticonKeyboard.hide();
    
    mEmoticonKeyboard.show();
 ~~~
（目前表情键盘仅支持宽度为全屏宽度，如有其它需求或问题，请联系我们。）

####关于账户
- 您在创建您的app用户时应当同时创建对应的面馆云账户，并且在您的服务器上保存此账户信息
- 您的用户在登录时您的服务器时应该返回对应的面馆云账户，并且调用login，sdk会同步对应表情数据，此操作为异步操作，请监听回调并且更新UI。sdk会保存此用户，并且在之后打开应用时自动恢复。


