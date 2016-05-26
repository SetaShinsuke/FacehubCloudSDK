package com.azusasoft.facehubcloudsdk.api.models;

//import com.azusasoft.facehubcloudsdk.api.CollectProgressListener;

import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.azusasoft.facehubcloudsdk.api.ProgressInterface;
import com.azusasoft.facehubcloudsdk.api.ResultHandlerInterface;
import com.azusasoft.facehubcloudsdk.api.models.events.DownloadProgressEvent;
import com.azusasoft.facehubcloudsdk.api.models.events.PackageCollectEvent;
import com.azusasoft.facehubcloudsdk.api.utils.Constants;
import com.azusasoft.facehubcloudsdk.api.utils.LogX;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

import static com.azusasoft.facehubcloudsdk.api.utils.LogX.fastLog;
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
            Image bkgImage = new Image(jsonObject.getJSONObject("background_detail"));
            this.setBackground(bkgImage);
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
     *
     * @param size                   要下载的尺寸;
     * @param resultHandlerInterface 封面下载回调,返回一个下载好的文件{@link java.io.File}对象;
     */
    @Override
    public void downloadCover(Image.Size size, ResultHandlerInterface resultHandlerInterface) {
        super.downloadCover(size, resultHandlerInterface);
    }

    /**
     * 下载背景;
     *
     * @param size                   要下载的尺寸;
     * @param resultHandlerInterface 封面下载回调,返回一个下载好的文件{@link java.io.File}对象;
     */
    public void downloadBackground(Image.Size size, ResultHandlerInterface resultHandlerInterface) {
        if (getBackground() == null) {
            return;
        }
        Image background = getBackground();
        background.download2Cache(size, resultHandlerInterface);
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

    //重构
//    public void collect_old(ResultHandlerInterface resultHandlerInterface) {
//        setIsCollecting(true);
//        ArrayList<Emoticon> allEmoticonAndCover = new ArrayList<>(getEmoticons());
//        if (getCover() != null) {
//            allEmoticonAndCover.add(getCover());
//        }
//        totalCount = allEmoticonAndCover.size();
//        success = 0;
//        fail = 0;
//        emoticons2Download.clear();
//        failEmoticons.clear();
//        tmpPercent = 0;
//        percent = 0;
//        for (int i = 0; i < allEmoticonAndCover.size(); i++) {
//            Emoticon emoticon = allEmoticonAndCover.get(i);
//            if (emoticon.getFilePath(Image.Size.FULL) != null
//                    && (new File(emoticon.getFilePath(Image.Size.FULL))).exists()) { //复制文件
//                try {
//                    UtilMethods.copyFile(emoticon.getFilePath(Image.Size.FULL), emoticon.getFileStoragePath(Image.Size.FULL));
//                    emoticon.save2Db();
//                    success++;
//                    tmpPercent = success * 1f / totalCount;
//                    percent = tmpPercent;
////                    fastLog("复制进度 : " + percent*100 + " %");
//                    DownloadProgressEvent event = new DownloadProgressEvent(getId());
//                    event.percentage = percent * 100;
//                    EventBus.getDefault().post(event);
//                } catch (IOException e) { //复制失败，加入下载队列
//                    LogX.e("Error collecting emoticon : " + e);
//                    emoticons2Download.add(emoticon);
//                }
//            } else { //没有本地文件，加入下载队列
//                emoticons2Download.add(emoticon);
//            }
//        }
//        if (emoticons2Download.size() == 0) {
//            doCollect(resultHandlerInterface);
//            return;
//        }
//        downloadEach(emoticons2Download, resultHandlerInterface);
//    }

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
//                            for(Emoticon emoticon :pkg.getEmoticons()) {
//                                Emoticon emo1= userList.getEmoticonById(emoticon.getId());
//                                emo1.updateField(emoticon);
//                            }
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
                        collectResultHandler.onError(e);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                setIsCollecting(false);
                collectResultHandler.onError(e);
            }


        });


    }

    private int retryTimes = 0;

//    private void downloadEach(final ArrayList<Emoticon> emoticons, final ResultHandlerInterface resultHandlerInterface) {
//        success = 0;
//        fail = 0;
//        emoticons2Download = new ArrayList<>(emoticons);
//        failEmoticons.clear();
//        for (int i = 0; i < emoticons2Download.size(); i++) {
//            final Emoticon emoticon = emoticons2Download.get(i);
//
//            emoticon.download2File(Image.Size.FULL, false, new ResultHandlerInterface() {
//                @Override
//                public void onResponse(Object response) {
//                    success++;
//                    percent = tmpPercent + (success * 1f / totalCount);
////                    fastLog("下载收藏进度 : " + percent*100 + " %" + " || success : " + success);
//                    DownloadProgressEvent event = new DownloadProgressEvent(getId());
//                    event.percentage = percent * 100;
//                    EventBus.getDefault().post(event);
////                    emoticon.save2Db();
//                    onFinish();
//                }
//
//                @Override
//                public void onError(Exception e) {
//                    fail++;
//                    failEmoticons.add(emoticon);
//                    onFinish();
//                }
//
//                private void onFinish() {
//                    if (success + fail != emoticons2Download.size()) {
//                        return; //仍在下载中
//                    }
//                    if (fail == 0) { //全部下载完成
//                        doCollect(resultHandlerInterface);
//                    } else if (retryTimes < 3) { //重试次数两次
//                        retryTimes++;
//                        downloadEach(failEmoticons, resultHandlerInterface);
//                    } else {
//                        setIsCollecting(false);
//                        onError(new Exception("下载出错,失败个数 : " + failEmoticons.size()));
//                        PackageCollectEvent event = new PackageCollectEvent(getId());
//                        EventBus.getDefault().post(event);
//                        setIsCollecting(false);
//                    }
//                }
//            });
//        }
//    }

//    private void doCollect(final ResultHandlerInterface resultHandlerInterface) {
//        FacehubApi.getApi().collectEmoPackageById(getId(), new ResultHandlerInterface() {
//            @Override
//            public void onResponse(Object response) {
//                setIsCollecting(false);
//                EmoticonDAO.saveInTx(getEmoticons());
//                getCover().save2Db();
//                PackageCollectEvent event = new PackageCollectEvent(getId());
//                EventBus.getDefault().post(event);
//                LogX.fastLog("收藏完成 . ");
//                resultHandlerInterface.onResponse(response);
//            }
//
//            @Override
//            public void onError(Exception e) {
//                setIsCollecting(false);
//                resultHandlerInterface.onError(e);
//                PackageCollectEvent event = new PackageCollectEvent(getId());
//                EventBus.getDefault().post(event);
//            }
//        });
//    }

    /**
     * 判断此包是否已被收藏过;
     *
     * @return 是否已收藏;
     */
    public boolean isCollected() {
        boolean flag = UserListDAO.findByForkFrom(getId(), true) != null;
//        fastLog("Is emoticon collected ? " + flag);
        return flag;
    }

    /**
     * 判断这个包是否在收藏中;
     *
     * @return 是否收藏中;
     */
    public boolean isCollecting() {
//        fastLog(getClass().getName() + " || isCollecting ? : " + isCollecting);
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

}
