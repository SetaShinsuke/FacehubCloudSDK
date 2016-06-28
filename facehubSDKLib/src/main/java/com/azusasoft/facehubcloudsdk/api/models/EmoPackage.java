package com.azusasoft.facehubcloudsdk.api.models;

//import com.azusasoft.facehubcloudsdk.api.CollectProgressListener;

import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.azusasoft.facehubcloudsdk.api.ProgressInterface;
import com.azusasoft.facehubcloudsdk.api.ResultHandlerInterface;
import com.azusasoft.facehubcloudsdk.api.models.events.DownloadProgressEvent;
import com.azusasoft.facehubcloudsdk.api.models.events.PackageCollectEvent;
import com.azusasoft.facehubcloudsdk.api.utils.Constants;
import com.azusasoft.facehubcloudsdk.api.utils.LogX;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

import static com.azusasoft.facehubcloudsdk.api.utils.UtilMethods.isJsonWithKey;

/**
 * Created by SETA on 2016/3/8.
 * 表情包
 */
public class EmoPackage extends List {

//    public enum CollectStatus {
//        NONE, COLLECTING,SUCCESS
//    }

    private String description;
    private String subTitle;
    private Image background; //可能为空!!
    //    private CollectStatus collectStatus = CollectStatus.NONE;
    private boolean isCollecting = false;

    private Author author;
    private String copyright;

    protected EmoPackage(){

    }


    /**
     * {@link EmoPackage}工厂方法
     * 注意!方法执行后的 {@link EmoPackage} 中的 {@link #emoticons} 可能只包含 {@link Emoticon#id} 这一个属性
     *
     * @param jsonObject 待处理的Json
     * @return {@link EmoPackage}对象
     * @throws JSONException
     */
    @Override
    public EmoPackage updateField(JSONObject jsonObject) throws JSONException {
        super.updateField(jsonObject);
        this.setDescription(jsonObject.getString("description"));
        this.setSubTitle(jsonObject.getString("sub_title"));
//        this.setAuthorName(jsonObject.getJSONObject("author").getString("name"));
        if (isJsonWithKey(jsonObject, "background") && isJsonWithKey(jsonObject, "background_detail")) {
            try {
                Image bkgImage = new Image(jsonObject.getJSONObject("background_detail").getString("id"));
                bkgImage.updateField(jsonObject.getJSONObject("background_detail"));
                this.setBackground(bkgImage);
            }catch (FacehubSDKException e){
                LogX.e("包背景解析出错 : " + e);
            }
        } else {
            setBackground(null);
        }

        if (isJsonWithKey(jsonObject, "author")) {
            JSONObject authorJson = jsonObject.getJSONObject("author");
            Author author = FacehubApi.getApi().getAuthorContainer().getUniqueAuthorByName(authorJson.getString("name"));
            //解析作者字段
            if (isJsonWithKey(jsonObject.getJSONObject("author"), "avatar")) {
                Image authorAvatar = new Image(getName() + "author");
                authorAvatar.setFileUrl(Image.Size.FULL, authorJson.getString("avatar"));
                author.setAvatar(authorAvatar);
            }
            if(isJsonWithKey(authorJson,"banner")){
                Image authorBanner = new Image(getName() + "banner");
                authorBanner.setFileUrl(Image.Size.FULL, authorJson.getString("banner"));
                author.setAuthorBanner(authorBanner);
            }
            if(isJsonWithKey(authorJson,"description")){
                author.setDescription(authorJson.getString("description"));
            }
            setAuthor(author);
        }

        //copyright字段
        if(isJsonWithKey(jsonObject,"copyright")){
            setCopyright(jsonObject.getString("copyright"));
        }

        //emoticons
//        if (isJsonWithKey(jsonObject, "contents")) {
//            ArrayList<Emoticon> emoticons = new ArrayList<>();
//            JSONArray jsonArray = jsonObject.getJSONArray("contents");
//            for (int i = 0; i < jsonArray.length(); i++) {
//                String emoId = jsonArray.getString(i);
//                Emoticon emoticon = FacehubApi.getApi().getEmoticonContainer().getUniqueEmoticonById(emoId);
//                emoticons.add(emoticon);
//            }
//            setEmoticons(emoticons);
//        }
//        //如果有emoticons的详情，则直接设置进去
//        if (isJsonWithKey(jsonObject, "contents_details")) {
//            JSONObject emoDetailsJson = jsonObject.getJSONObject("contents_details");
//            for (Emoticon emoticon : getEmoticons()) {
//                emoticon.updateField(emoDetailsJson.getJSONObject(emoticon.getId()));
//            }
//        }
        return this;
    }

//    @Override
//    public String toString() {
//        return "\n[EmoPackage] : " + "\nid : " + getId()
//                +"\nname : " + getName()
//                +"\ndescription : " + description
//                +"\nsubTitle : " + subTitle
//                +"\nauthor name : " + authorName
//                +"\ncover : " + getCover()
//                +"\nbackground : " + background
//                +"\nemoticons : " + getEmoticons() ;
//    }

//    public CollectStatus getCollectStatus() {
//        return collectStatus;
//    }
//
//    public void setCollectStatus(CollectStatus collectStatus) {
//        this.collectStatus = collectStatus;
//    }

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
    public Emoticon getCover() {
        return super.getCover();
    }

    @Override
    public void setCover(Emoticon cover) {
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
        if(author==null){
            return null;
        }
        return author.getName();
    }


    public Image getAuthorAvatar() {
        if(author==null){
            return null;
        }
        return this.author.getAvatar();
    }

    public void downloadAuthorAvatar(ResultHandlerInterface resultHandlerInterface) {
        if (author != null) {
            author.downloadAvatar(resultHandlerInterface);
        }
    }

    public void downloadAuhtorBanner(ResultHandlerInterface resultHandlerInterface){
        if(author!=null){
            author.downloadAuthorBanner(resultHandlerInterface);
        }
    }

    /**
     * 下载封面;
     * @param resultHandlerInterface 封面下载回调,返回一个下载好的文件{@link java.io.File}对象;
     */
    @Override
    public void downloadCover(ResultHandlerInterface resultHandlerInterface) {
        super.downloadCover( resultHandlerInterface);
    }

    /**
     * 下载背景;
     * @param resultHandlerInterface 封面下载回调,返回一个下载好的文件{@link java.io.File}对象;
     */
    public void downloadBackground(ResultHandlerInterface resultHandlerInterface) {
        if (getBackground() == null) {
            return;
        }
        Image background = getBackground();
        background.downloadFull2Cache(resultHandlerInterface);
    }

    /**
     * 收藏的进度
     *
     * @return 收藏进度的百分比;
     */
    public float getPercent() {
        return this.percent;
    }

    private int totalCount = 0;
    private int success = 0;
    private int fail = 0;
    private float percent = 0f;
    private float tmpPercent = 0f;
    private ArrayList<Emoticon> emoticons2Download = new ArrayList<>();
    private ArrayList<Emoticon> failEmoticons = new ArrayList<>();

    /**
     * 拉取详细并且收藏.
     *
     * @param collectResultHandler 收藏结果回调,返回对应保存过后的{@link UserList};
     */
    public void collect(final ResultHandlerInterface collectResultHandler) {
        setIsCollecting(true);
        percent = 0f;
        //开始收藏时，设置为true(收藏中);
        // 全部收藏下载成功时false，或详情拉取失败时false
        FacehubApi.getApi().getPackageDetailById(getId(), new ResultHandlerInterface() {
            @Override
            public void onResponse(Object response) {
                final EmoPackage pkg = (EmoPackage) response;
                FacehubApi.getApi().collectEmoPackageById(getId(), new ResultHandlerInterface() {
                    @Override
                    public void onResponse(Object response) {
                        if (response instanceof UserList) {
                            final UserList userList = (UserList) response;
                            userList.download(new ResultHandlerInterface() {
                                @Override
                                public void onResponse(Object response) {
                                    setIsCollecting(false);
                                    PackageCollectEvent event = new PackageCollectEvent(getId());
                                    EventBus.getDefault().post(event);
                                    collectResultHandler.onResponse(userList);
                                    percent = 100f;
                                }

                                @Override
                                public void onError(Exception e) {
                                    setIsCollecting(false);
                                    PackageCollectEvent event = new PackageCollectEvent(getId());
                                    EventBus.getDefault().post(event);
                                    collectResultHandler.onError(e);
                                    percent = 100f;
                                }
                            }, new ProgressInterface() {
                                @Override
                                public void onProgress(double process) {
                                    DownloadProgressEvent event = new DownloadProgressEvent(getId());
                                    event.percentage = (float) process;
                                    EventBus.getDefault().post(event);
                                    percent = (float) process;
                                    LogX.i(Constants.PROGRESS, "收藏包-个人列表下载进度 : " + process + " %");
                                }
                            });
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        setIsCollecting(false);
                        PackageCollectEvent event = new PackageCollectEvent(getId());
                        EventBus.getDefault().post(event);
                        collectResultHandler.onError(e);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                setIsCollecting(false);
                PackageCollectEvent event = new PackageCollectEvent(getId());
                EventBus.getDefault().post(event);
                collectResultHandler.onError(e);
            }
        });
    }

    /**
     * 判断此包是否已被收藏过;
     *
     * @return 是否已收藏;
     */
    public boolean isCollected() {
        if(getId()==null){
            return false;
        }
        for(UserList userList:FacehubApi.getApi().getUser().getUserLists()){
            if( getId().equals(userList.getForkFromId()) ){
                return true;
            }
        }
        return false;
//        return UserListDAO.findByForkFrom(getId(), true) != null;
    }

    /**
     * 判断这个包是否在收藏中;
     *
     * @return 是否收藏中;
     */
    public boolean isCollecting() {
        return isCollecting;
    }


    void setIsCollecting(boolean isCollecting) {
        this.isCollecting = isCollecting;
    }

    private void setAuthor(Author author) {
        this.author = author;
    }
    public Author getAuthor() {
        return author;
    }

    public String getCopyright() {
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }
}
