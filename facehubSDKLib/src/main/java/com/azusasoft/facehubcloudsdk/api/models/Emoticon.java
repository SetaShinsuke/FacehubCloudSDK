package com.azusasoft.facehubcloudsdk.api.models;

import android.content.Context;

import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.azusasoft.facehubcloudsdk.api.ResultHandlerInterface;
import com.azusasoft.facehubcloudsdk.api.models.events.EmoticonCollectEvent;
import com.azusasoft.facehubcloudsdk.api.utils.CodeTimer;
import com.azusasoft.facehubcloudsdk.api.utils.DownloadService;
import com.azusasoft.facehubcloudsdk.api.utils.LogX;
import com.azusasoft.facehubcloudsdk.api.utils.UtilMethods;
import com.azusasoft.facehubcloudsdk.api.utils.threadUtils.ThreadPoolManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import de.greenrobot.event.EventBus;

import static com.azusasoft.facehubcloudsdk.api.utils.UtilMethods.getNewer;

/**
 * Created by SETA on 2016/3/8.
 * 用来表示一个表情对象
 */
public class Emoticon extends Image {
    private boolean isCollected = false;
    private String description;
    private boolean local = false;

    public Emoticon() {

    }

    public Emoticon(String id){
        setId(id);
    }

    public boolean isCollected(){
        CodeTimer codeTimer = new CodeTimer();
        codeTimer.start("表情是否收藏");
        boolean flag = FacehubApi.getApi().isEmoticonCollected(getId());
        codeTimer.end("表情是否收藏");
        return flag;
    }

    /**
     * 收藏表情
     *
     * @param listId 要收藏到的列表id;
     * @param resultHandlerInterface 收藏结果回调，返回{@link Emoticon}对象;
     */
    public void collect(String listId , final ResultHandlerInterface resultHandlerInterface){
        final Emoticon self = this;
        FacehubApi.getApi().collectEmoById(getId(), listId, new ResultHandlerInterface() {
            @Override
            public void onResponse(Object response) {
                String cachePath = getFullPath();
                String filePath = getFileStoragePath(Size.FULL);
                try {
                    UtilMethods.copyFile(cachePath, filePath);
                    setFilePath(Size.FULL, filePath);
                    save2Db();
                    EmoticonCollectEvent event = new EmoticonCollectEvent();
                    EventBus.getDefault().post(event);
                } catch (IOException e) {
                    e.printStackTrace();
                    resultHandlerInterface.onError(e);
                    return;
                }
                resultHandlerInterface.onResponse( self );
            }

            @Override
            public void onError(Exception e) {
                resultHandlerInterface.onError(e);
            }
        });
    }

    // 在主线程:耗时; 在后台线程:界面错乱
    public boolean save2Db() {
//        final Emoticon emoticon = this;
//        ThreadPoolManager.getDbThreadPool().submit(new Runnable() {
//            @Override
//            public void run() {
//                boolean flag = EmoticonDAO.save2DBWithClose(emoticon);
//                LogX.fastLog("保存到数据库 : " + flag);
//            }
//        });
//        return true;

        return EmoticonDAO.save2DB(this);
    }

    /**
     * 保存表情到数据库
     *
     * @return 保存是否成功.
     */


    @Override
    public String getId() {
        return super.getId();
    }

    @Override
    protected Image setId(String id) {
        return super.setId(id);
    }

    @Override
    public int getFsize() {
        return super.getFsize();
    }

    @Override
    protected Image setFsize(int size) {
        return super.setFsize(size);
    }

    @Override
    public int getHeight() {
        return super.getHeight();
    }

    @Override
    protected Image setHeight(int height) {
        return super.setHeight(height);
    }

    @Override
    public int getWidth() {
        return super.getWidth();
    }

    @Override
    protected Image setWidth(int width) {
        return super.setWidth(width);
    }

    @Override
    public Format getFormat() {
        return super.getFormat();
    }

    @Override
    protected Image setFormat(String format) {
        return super.setFormat(format);
    }

    @Override
    public String getFileUrl(Size imgSize) {
        return super.getFileUrl(imgSize);
    }

    @Override
    protected Image setFileUrl(JSONObject obj) {
        return super.setFileUrl(obj);
    }

    @Override
    public boolean hasFile(Size size) {
        return super.hasFile(size);
    }

    @Override
    protected void setFilePath(Size size, String path) {
        super.setFilePath(size, path);
    }

    @Override
    public String toString() {
        return "\n[Emoticon] : " + "\nid : " + getId()
                + "\nfsize : " + getFsize()
                +"\nheight : " + getHeight()
                +"\nwidth : " + getWidth()
                +"\nformat : " + getFormat()
                +"\nmediumUrl : " + getFileUrl(Size.MEDIUM)
                +"\nfullUrl : " + getFileUrl(Size.FULL);
    }


    public Emoticon updateField(Emoticon emoticon) throws FacehubSDKException{
        super.updateField(emoticon);
        if(emoticon.getId()==null){
            return this;
        }
        if(!emoticon.getId().equals(getId())){
            throw new FacehubSDKException(new Exception("Emoticon updateField Error : id not match !! "));
        }
        //Id相同，根据是否有path选择更新
        setDbId((Long) getNewer(getDbId(),emoticon.getDbId()));
        setFilePath(Size.FULL, (String) getNewer(getFullPath(),emoticon.getFullPath()));
        setFilePath(Size.MEDIUM, (String) getNewer(getThumbPath(),emoticon.getThumbPath()));
        setFileUrl(Size.FULL, (String) getNewer(getFileUrl(Size.FULL),emoticon.getFileUrl(Size.FULL)));
        setFileUrl(Size.MEDIUM, (String) getNewer(getFileUrl(Size.MEDIUM),emoticon.getFileUrl(Size.MEDIUM)));
        setFormat((String) getNewer(getFormat()+"",emoticon.getFormat()+""));
        setDescription((String)getNewer(getDescription(),emoticon.getDescription()));
        setHeight(emoticon.getHeight());
        setWidth(emoticon.getWidth());
        setFsize(emoticon.getFsize());
        FacehubApi.getApi().getEmoticonContainer().put(getId(),this);
        return this;
    }

    public Emoticon updateField(JSONObject jsonObject) throws JSONException , FacehubSDKException {
        super.updateField(jsonObject);
        Emoticon tmpEmoticon = new Emoticon();
        tmpEmoticon.setId( jsonObject.getString("id") )
                .setFsize( jsonObject.getInt("fsize") )
                .setHeight( jsonObject.getInt("height") )
                .setWidth( jsonObject.getInt("width") )
                .setFormat( jsonObject.getString("format"))
                .setFileUrl( jsonObject );
        if(UtilMethods.isJsonWithKey(jsonObject,"description")){
            tmpEmoticon.setDescription(jsonObject.getString("description"));
        }
        this.updateField(tmpEmoticon);
        FacehubApi.getApi().getEmoticonContainer().put(getId(),this);
        return this;
    }

    /**
     * 下载表情到file目录
     *
     * @param size 尺寸
     * @param saveNow 下载完成后是否立即保存到数据库,true立即保存，false另外进行批量保存或不保存;
     * @param resultHandlerInterface 返回一个下载好的文件{@link File}对象;
     */
    private void download2File(final Size size, final boolean saveNow , final ResultHandlerInterface resultHandlerInterface) {

        File cacheFile = new File(getCacheStoragePath(size));
        File dataFile  = new File(getFileStoragePath(size));
        if(cacheFile.exists()){ //cache目录里有文件，则进行复制
            try {
                UtilMethods.copyFile(cacheFile,dataFile);
                setFilePath(size,dataFile.getAbsolutePath());
                resultHandlerInterface.onResponse(dataFile);
                if(saveNow) {
                    save2Db();
                }
            } catch (IOException e) {
                e.printStackTrace();
                resultHandlerInterface.onError(e);
            }

        }else{
            String url = getFileUrl(size);
            File dir = DownloadService.getFileDir();
            final String path = "/" + getId() + size.toString().toLowerCase() + getFormat().toString().toLowerCase();
            DownloadService.download(url, dir, path, new ResultHandlerInterface() {
                @Override
                public void onResponse(Object response) {
                    setFilePath(size, ((File) response).getAbsolutePath());
                    if(saveNow) {
                        save2Db();
                    }
                    resultHandlerInterface.onResponse(response);
                }

                @Override
                public void onError(Exception e) {
                    resultHandlerInterface.onError(e);
                }
            });
        }
    }

    /**
     * 下载【缩略图】到File目录
     * @param saveNow 是否立即保存到数据库
     * @param resultHandlerInterface 返回一个下载好的文件{@link File}对象;
     */
    public void downloadThumb2File(boolean saveNow , ResultHandlerInterface resultHandlerInterface){
        download2File(Size.MEDIUM , saveNow , resultHandlerInterface);
    }

    /**
     * 下载【原图】到File目录
     * @param saveNow 是否立即保存到数据库
     * @param resultHandlerInterface 返回一个下载好的文件{@link File}对象;
     */
    public void downloadFull2File(boolean saveNow , ResultHandlerInterface resultHandlerInterface){
        download2File(Size.FULL , saveNow , resultHandlerInterface);
    }

    /**
     * 下载【缩略图】和【原图】到File目录
     * @param saveNow 是否立即保存到数据库
     * @param resultHandlerInterface 返回一个下载好的文件{@link File}对象;
     */
    public void download2File(final boolean saveNow , final ResultHandlerInterface resultHandlerInterface){
        downloadThumb2File(false , new ResultHandlerInterface() {
            @Override
            public void onResponse(Object response) {
                //缩略图下载成功，继续下载全图
                downloadFull2File(saveNow,resultHandlerInterface);
            }

            @Override
            public void onError(Exception e) {
                resultHandlerInterface.onError(new Exception("缩略图下载出错 : " + e));
            }
        });
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isLocal() {
        return local;
    }

    public void setLocal(boolean local) {
        this.local = local;
    }
}
