package com.azusasoft.facehubcloudsdk.activities;

import android.content.Context;
import android.content.res.Configuration;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.azusasoft.facehubcloudsdk.R;
import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.azusasoft.facehubcloudsdk.api.ResultHandlerInterface;
import com.azusasoft.facehubcloudsdk.api.models.Emoticon;
import com.azusasoft.facehubcloudsdk.api.models.Image;
import com.azusasoft.facehubcloudsdk.api.utils.LogX;
import com.azusasoft.facehubcloudsdk.views.EmoticonKeyboardView;
import com.azusasoft.facehubcloudsdk.views.EmoticonSendListener;

import static com.azusasoft.facehubcloudsdk.api.utils.LogX.fastLog;

/**
 * Created by SETA on 2016/3/16.
 */
public class KeyboardActivity extends AppCompatActivity {
    private Context context;
    private EmoticonKeyboardView emoticonKeyboardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keyboard);
        this.context = this;
        emoticonKeyboardView = (EmoticonKeyboardView) findViewById(R.id.emo_keyboard);
        emoticonKeyboardView.initKeyboard();
        emoticonKeyboardView.setEmoticonSendListener(new EmoticonSendListener() {
            @Override
            public void onSend(Emoticon emoticon) {
                fastLog("发送表情 : " + emoticon.getFilePath(Image.Size.FULL));
            }
        });
    }

    /**
     * 屏幕旋转时调用此方法
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //newConfig.orientation获得当前屏幕状态是横向或者竖向
        //Configuration.ORIENTATION_PORTRAIT 表示竖向
        //Configuration.ORIENTATION_LANDSCAPE 表示横屏
//        fastLog("onConfigurationChanged!");
        if(newConfig.orientation==Configuration.ORIENTATION_PORTRAIT){
            emoticonKeyboardView.onScreenWidthChange();
            fastLog("旋转 : 竖屏");
        }
        if(newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE){
            emoticonKeyboardView.onScreenWidthChange();
            fastLog("旋转 : 横屏");
        }
    }

}
