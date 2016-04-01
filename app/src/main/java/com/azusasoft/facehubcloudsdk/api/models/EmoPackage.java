package com.azusasoft.facehubcloudsdk.api.models;

//import com.azusasoft.facehubcloudsdk.api.CollectProgressListener;
import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.azusasoft.facehubcloudsdk.api.ResultHandlerInterface;
import com.azusasoft.facehubcloudsdk.api.utils.LogX;
import com.azusasoft.facehubcloudsdk.api.utils.UtilMethods;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static com.azusasoft.facehubcloudsdk.api.utils.LogX.fastLog;
import static com.azusasoft.facehubcloudsdk.api.utils.UtilMethods.isJsonWithKey;

/**
 * Created by SETA on 2016/3/8.
 */
public class EmoPackage extends List {

    public enum DownloadStatus{
        NONE,DOWNLOADING,FAIL,SUCCESS
    }

    private String description;
    private String subTitle;
    private Image background; //TODO:可能为空!!
    private String authorName;
    private DownloadStatus downloadStatus = DownloadStatus.NONE;

    /**
     * {@link EmoPackage}工厂方法
     * 注意!方法执行后的 {@link EmoPackage} 中的 {@link #emoticons} 可能只包含 {@link Emoticon#id} 这一个属性
     *
     * @param jsonObject 待处理的Json
     * @return {@link EmoPackage}对象
     * @throws JSONException
     */
    public EmoPackage emoPackageFactoryByJson(JSONObject jsonObject) throws JSONException{
        super.listFactoryByJson( jsonObject );
        this.setDescription(jsonObject.getString("description"));
        this.setSubTitle(jsonObject.getString("sub_title"));
        this.setAuthorName(jsonObject.getJSONObject("author").getString("name"));
        if( isJsonWithKey(jsonObject, "background") && isJsonWithKey(jsonObject,"background_detail") ){
            Image bkgImage = new Image();
            this.setBackground(
                    bkgImage.imageFactoryByJson(jsonObject.getJSONObject("background_detail") , false));
        }else {
            setBackground( null );
        }
        //emoticons
        if( isJsonWithKey(jsonObject,"contents") ) {
            ArrayList<Emoticon> emoticons = new ArrayList<>();
            JSONArray jsonArray = jsonObject.getJSONArray("contents");
            for(int i=0;i<jsonArray.length();i++){
                String emoId = jsonArray.getString(i);
                Emoticon emoticon = new Emoticon();
                emoticon.setId( emoId );
                emoticons.add(emoticon);
            }
            setEmoticons(emoticons);
        }
        //如果有emoticons的详情，则直接设置进去
        if( isJsonWithKey(jsonObject,"contents_details") ){
            ArrayList<Emoticon> emoticons = getEmoticons();
            JSONObject emoDetailsJson = jsonObject.getJSONObject("contents_details");
            for (Emoticon emoticon:emoticons){
                emoticon.emoticonFactoryByJson( emoDetailsJson.getJSONObject(emoticon.getId()) , false );
            }
        }
        return this;
    }

    @Override
    public String toString() {
        return "\n[EmoPackage] : " + "\nid : " + getId()
                +"\nname : " + getName()
                +"\ndescription : " + description
                +"\nsubTitle : " + subTitle
                +"\nauthor name : " + authorName
                +"\ncover : " + getCover()
                +"\nbackground : " + background
                +"\nemoticons : " + getEmoticons() ;
    }

    public DownloadStatus getDownloadStatus() {
        return downloadStatus;
    }

    public void setDownloadStatus(DownloadStatus downloadStatus) {
        this.downloadStatus = downloadStatus;
    }

    @Override
    public String getId() {
        return super.getId();
    }

    @Override
    public void setId(String id) {
        super.setId(id);
    }

    @Override
    public String getName() {
        return super.getName();
    }

    @Override
    public void setName(String name) {
        super.setName(name);
    }

    @Override
    public Image getCover() {
        return super.getCover();
    }

    @Override
    public void setCover(Image cover) {
        super.setCover(cover);
    }

    @Override
    public ArrayList<Emoticon> getEmoticons() {
        return super.getEmoticons();
    }

    @Override
    public void setEmoticons(ArrayList<Emoticon> emoticons) {
        super.setEmoticons(emoticons);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public Image getBackground() {
        return background;
    }

    public void setBackground(Image background) {
        this.background = background;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    @Override
    public void downloadCover(Image.Size size, ResultHandlerInterface resultHandlerInterface) {
        super.downloadCover(size, resultHandlerInterface);
    }

    public void downloadBackground(Image.Size size,ResultHandlerInterface  resultHandlerInterface){
        if(getBackground()==null){
            return;
        }
        Image background = getBackground();
        background.download2Cache(size, resultHandlerInterface);
    }

    public float getPercent(){
        return this.percent;
    }

    private int totalCount=0;
    private int success = 0;
    private int fail = 0;
    private float percent = 0f;
    private float tmpPercent = 0f;
    private ArrayList<Emoticon> emoticons2Download = new ArrayList<>();
    private ArrayList<Emoticon> failEmoticons = new ArrayList<>();
    public void collect(ResultHandlerInterface resultHandlerInterface){
        totalCount = getEmoticons().size();
        success = 0;
        fail = 0;
        emoticons2Download.clear();
        failEmoticons.clear();
        tmpPercent = 0;
        percent = 0;
        for(int i=0;i<getEmoticons().size();i++){
            Emoticon emoticon = getEmoticons().get(i);
            if(emoticon.getFilePath(Image.Size.FULL)!=null
                    && (new File(emoticon.getFilePath(Image.Size.FULL))).exists() ){ //复制文件
                try {
                    UtilMethods.copyFile(emoticon.getFilePath(Image.Size.FULL), emoticon.getFileStoragePath(Image.Size.FULL));
                    emoticon.save2Db();
                    success ++;
                    tmpPercent = success*1f/totalCount;
                    percent = tmpPercent;
                    fastLog("复制进度 : " + percent*100 + " %");
                } catch (IOException e) { //复制失败，加入下载队列
                    LogX.e("Error collecting emoticon : " + e);
                    emoticons2Download.add(emoticon);
                }
            }else { //没有本地文件，加入下载队列
                emoticons2Download.add(emoticon);
            }
        }
        if(emoticons2Download.size()==0){
            doCollect(resultHandlerInterface);
            return;
        }
        downloadEach(emoticons2Download,resultHandlerInterface);
    }
    private int retryTimes = 0;
    private void downloadEach(final ArrayList<Emoticon> emoticons, final ResultHandlerInterface resultHandlerInterface){
        success = 0;
        fail = 0;
        emoticons2Download = new ArrayList<>(emoticons);
        failEmoticons.clear();
        for (int i=0;i<emoticons2Download.size();i++){
            final Emoticon emoticon = emoticons2Download.get(i);
            emoticon.download2File(Image.Size.FULL, new ResultHandlerInterface() {
                @Override
                public void onResponse(Object response) {
                    success++;
                    percent = tmpPercent + (success*1f/totalCount);
                    fastLog("下载收藏进度 : " + percent*100 + " %" + " || success : " + success);
                    emoticon.save2Db();
                    onFinish();
                }

                @Override
                public void onError(Exception e) {
                    fail++;
                    failEmoticons.add(emoticon);
                    onFinish();
                }

                private void onFinish(){
                    if (success + fail != emoticons2Download.size()) {
                        return; //仍在下载中
                    }
                    if (fail == 0) { //全部下载完成
                        doCollect(resultHandlerInterface);
                    } else if (retryTimes < 3) { //重试次数两次
                        retryTimes++;
                        downloadEach(failEmoticons, resultHandlerInterface);
                    } else {
                        onError(new Exception("下载出错,失败个数 : " + failEmoticons.size()));
                    }
                }
            });
        }
    }

    private void doCollect(ResultHandlerInterface resultHandlerInterface){
        FacehubApi.getApi().collectEmoPackageById(getId(),resultHandlerInterface);
    }

}
