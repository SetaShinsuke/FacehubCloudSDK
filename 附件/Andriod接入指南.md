##Android接入步骤

###接入之前

- 申请app_id

###开始接入
1. 在gradle中加入facehubSDKLib1.0.0-release.aar
2. 在你的application onCreate时调用初始化：

    ~~~java
    FacehubApi.init(getApplicationContext()); //初始化
    FacehubApi.initViews(getApplicationContext()); //初始化api中的views
    FacehubApi.getApi().setAppId(APP_ID); //设置应用ID
    ~~~
3. 在你的登陆回调中加入：
    
    ~~~java
    FacehubApi.getApi().setCurrentUser(USER_ID, AUTH_TOKEN, new ResultHandlerInterface() {
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
    
    ```java
     mEmoticonKeyboard.setEmoticonSendListener(new EmoticonSendListener() {
                @Override
                public void onSend(Emoticon emoticon) {
                    String id = emoticon.getId();
                    String filePath = emoticon.getFilePath(Image.Size.FULL);
                    //send and display the emoticon
                }
            });
    ```

显示和隐藏键盘：

    ```java
    mEmoticonKeyboard.hide();
    
    mEmoticonKeyboard.show();
    ```


####关于账户
- 您在创建您的app用户时应当同时创建对应的面馆云账户，并且在您的服务器上保存此账户信息
- 您的用户在登录时应该返回对应的面馆云账户，并且把此账户设置给sdk，sdk会同步对应表情数据，此操作为异步操作，请监听回调并且更新UI。sdk会保存此用户，并且在之后打开应用时自动恢复。
- *如果您的app允许用户多设备同时登陆，那么请手动的调用sdk的设置账户函数，该函数会重新同步数据，保证数据一致性。（比如每次打开app后）已经同步过的表情不会重复下载，不需要担心流量问题。



--------------------------------------------------------------------------
                        文档补充
--------------------------------------------------------------------------

1.主题色设置：
        调用 FacehubApi.getApi.setThemeColor(String colorString )；
        colorString 一个表示颜色RGB的字符串，例如"#f33847";

2.键盘说明：
    目前表情键盘仅支持宽度为全屏宽度，如有其它需求或问题，请联系我们。