package com.azusasoft.facehubcloudsdk.views.viewUtils;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.webkit.WebView;

import com.azusasoft.facehubcloudsdk.api.utils.LogX;

/**
 * Created by SETA on 2015/8/27.
 */
public class WebGifViewFC extends WebView {
    private Context context;
    private String gifPath="";
    private boolean showAsCircle =false;

    private String imageBgColor="transparent";
//    private String imageBgColor="white";
    private boolean byWidth=true;
    private int width = 0;
    private int height = 0;
    private String path;

    public WebGifViewFC(Context context) {
        super(context);
        initView(context);
    }

    public WebGifViewFC(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public WebGifViewFC(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public WebGifViewFC(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    private void initView(Context context){
        this.context = context;
        getSettings().setDefaultTextEncodingName("UTF-8");
        String html = "<html style=\"background-color:transparent;margin:0px\">\n" +
                "<body style=\"background-color:transparent;margin:0px\">\n" +
                "</body>\n" +
                "</html>";
        setBackgroundColor(Color.TRANSPARENT);
        loadDataWithBaseURL(null,html,"text/html","UTF-8",null);
    }

    public void setSize(int width,int height){
        this.width = width;
        this.height = height;
    }

    public void setByWidth(boolean byWidth){
        this.byWidth = byWidth;
    }

    public void setShowAsCircle(boolean showAsCircle){
        this.showAsCircle = showAsCircle;
    }
    public void setGifResource(int resourceId){
        String gifName = getResources().getResourceEntryName(resourceId);
        String path="file:///android_res/drawable/"+gifName;
//        fastLog("Gif resource path : " + path);
        setGifPath(path);
    }
    public void setGifRaw(int rawId){
        String gifName = getResources().getResourceEntryName(rawId);
        String path="file:///android_res/raw/"+gifName;
//        fastLog("Gif raw path : " + path);
        setGifPath(path);
    }
    public void setGifAssets(String assetsName){
//        String gifName = getResources().getResourceEntryName(assetsId);
        String path="file:///android_asset/"+assetsName;
//        fastLog("Gif assets path : " + path);
        setGifPath(path);
    }
    public void setHttpPath(String path){
        setGifPath(path);
    }
    public void setGifPath(String path){
        this.path = path;
        String html;
        String borderRadius="10000em";
        if(!showAsCircle){
            borderRadius = "0";
        }else {
            this.height = this.width;
        }

        String css = "body{\n" +
                "    -webkit-touch-callout: none;\n" +
                "    -webkit-user-select: none;\n" +
                "    -khtml-user-select: none;\n" +
                "    -moz-user-select: none;\n" +
                "    -ms-user-select: none;\n" +
                "    user-select: none;\n" +
                "}";

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        double density = metrics.scaledDensity;
        int w = (int) (width/density);
        int h = (int) (height/density);
        LogX.d("emo", "w " + w + " , h : " + h);
        html = "<html style='background-color:transparent'>" +
                "<style>" + css + "</style>" +
                "<body style=\"margin:0px;padding:0px;background-color:transparent;\">" +
                "    <table style='display:block;background-color:transparent;margin:0px;border-collapse:collapse;'>" +
//                "    <table style='width:100%;height:100%;display:block;margin:0;'>\n" +
                "        <tbody>" +
                "            <tr>" +
                "                <td style='background-color:"+imageBgColor+";width:"+w+"px;height:"+h+"px;text-align:center;border-radius:"+borderRadius+";overflow:hidden;padding:0;'>" +
                "                        <img src='"+path+"' style='margin:0;max-height:100%;max-width:100%;background-color:transparent'/>" +
                "                </td>" +
                "            </tr>" +
                "        </tbody>" +
                "    </table>" +
                "</body>" +
                "</html>";

        getSettings().setDefaultTextEncodingName("UTF-8");
//        gifWebView.loadUrl("http://img.t.sinajs.cn/t4/appstyle/expression/ext/normal/8f/qq_org.gif");
        setBackgroundColor(Color.TRANSPARENT);
//        gifWebView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
        loadDataWithBaseURL(null,html,"text/html","UTF-8",null);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        this.width = r-l;
        this.height = b-t;
        setGifPath(path);
        LogX.d("emo","onLayout path : " + path);
        super.onLayout(changed, l, t, r, b);
    }
}
