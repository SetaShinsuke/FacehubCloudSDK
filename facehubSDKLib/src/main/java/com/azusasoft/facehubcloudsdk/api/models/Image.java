package com.azusasoft.facehubcloudsdk.api.models;

import com.azusasoft.facehubcloudsdk.api.ResultHandlerInterface;
import com.azusasoft.facehubcloudsdk.api.utils.DownloadService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;

/**
 * Created by SETA on 2016/3/8.
 * 图片类
 */
public class Image {
    //数据库内id
    private Long dbId;
    public Image() {
    }


    //type : "" , "emoticon"
//    private String type = "";
//    public String getType() {
//        return type;
//    }
//
//    public void setType(String type) {
//        this.type = type;
//    }

    public enum Size{
        MEDIUM,FULL
    }
    public enum Format{
        JPG,BMP,PNG,GIF
    }

    private String id;
    private int fsize, height, width;
    private Format format = Format.JPG;
    private transient HashMap<Size,String> fileUrl = new HashMap<>();
    private String fullPath,mediumPath;

//    @Override
//    public String toString() {
//        return "\n[Image] : " + "\nid : " + id
//                + "\nfsize : " + fsize
//                +"\nheight : " + height
//                +"\nwidth : " + width
//                +"\nformat : " + format
//                +"\nfileUrl : " + fileUrl;
//    }

    public Image(JSONObject jsonObject) throws JSONException{
            updateField(jsonObject);
//        if(doSave2DB) {
//            save2Db();
//        }

    }
    public Image updateField(JSONObject jsonObject) throws JSONException{
        return this.setId( jsonObject.getString("id") )
                .setFsize( jsonObject.getInt("fsize") )
                .setHeight( jsonObject.getInt("height") )
                .setWidth( jsonObject.getInt("width") )
                .setFormat( jsonObject.getString("format"))
                .setFileUrl( jsonObject );
    }
    public Image updateField(Image imgWithContent){
        if(imgWithContent.getId()!=null && imgWithContent.getId().equals(getId())){
            setDbId(imgWithContent.getDbId());
            setFilePath(Size.FULL   ,imgWithContent.getFilePath(Size.FULL));
            setFilePath(Size.MEDIUM ,imgWithContent.getFilePath(Size.MEDIUM));
            setFileUrl(Size.FULL    ,imgWithContent.getFileUrl(Size.FULL));
            setFileUrl(Size.MEDIUM  ,imgWithContent.getFileUrl(Size.MEDIUM));
            setFormat(imgWithContent.getFormat().toString());
            setHeight(imgWithContent.getHeight());
            setWidth(imgWithContent.getWidth());
            setFsize(imgWithContent.getFsize());
        }
        return this;
    }

//    public boolean save2Db(){
//        return EmoticonDAO.save2DBWithClose(this);
//    }

    protected Image setId(String id) {
        this.id = id;
        return this;
    }

    public String getId() {
        return id;
    }

    protected Image setFsize(int size){
        this.fsize = size;
        return this;
    }
    public int getFsize(){
        return this.fsize;
    }  public int getHeight() {
        return height;
    }
    protected Image setHeight(int height) {
        this.height = height;
        return this;
    }
    public int getWidth() {
        return width;
    }
    protected Image setWidth(int width) {
        this.width = width;
        return this;
    }
    public Format getFormat() {
        return format;
    }

    protected Image setFormat(String format) {
        format = format.toLowerCase();
        if(format.equals("jpg")||format.equals("jpeg")){
            this.format = Format.JPG;
        }else if(format.equals("png")){
            this.format = Format.PNG;
        }else if(format.equals("bmp")){
            this.format = Format.BMP;
        }else if(format.equals("gif")){
            this.format = Format.GIF;
        }
        return this;
    }
    public Long getDbId() {
        return dbId;
    }

    protected void setDbId(Long dbId) {
        this.dbId = dbId;
    }

    public String getFileUrl(Size imgSize) {
        return fileUrl.get(imgSize);
    }

    public Emoticon toEmoticon(){
        Emoticon emoticon = new Emoticon();
        emoticon.setDbId(this.getDbId());
        emoticon.setFilePath(Size.FULL,this.getFilePath(Size.FULL));
        emoticon.setFilePath(Size.MEDIUM,this.getFilePath(Size.MEDIUM));
        emoticon.setFormat(getFormat().toString())
                .setFsize(getFsize())
                .setHeight(getHeight())
                .setWidth(getWidth())
                .setId(getId());
        return emoticon;
    }
    protected Image setFileUrl(Size imgSize,String fileUrl) {
        this.fileUrl.put(imgSize, fileUrl);
        return this;
    }
    protected Image setFileUrl(JSONObject obj){
        try {
            this.setFileUrl(Size.MEDIUM, obj.getString("medium_url"));
        } catch (JSONException e) {
            //e.printStackTrace();
        }
        try {
            this.setFileUrl(Size.FULL, obj.getString("full_url"));
        } catch (JSONException e) {
            //e.printStackTrace();
        }
        return this;
    }

    protected void setFilePath(Size size,String path){
        switch (size){
            case FULL:
                this.fullPath = path;
                break;
            case MEDIUM:
                this.mediumPath = path;
                break;
            default:
                break;
        }
    }

    public String getFilePath(Size size){
        switch (size){
            case FULL:
                return fullPath;
            case MEDIUM:
                return mediumPath;
            default:
                return null;
        }
    }

    public boolean hasFile(Size size) {
        switch (size) {
            case FULL:
                return fullPath!=null;
            case MEDIUM:
                return mediumPath!=null;
            default:
                return false;
        }
    }

    protected String getCacheStoragePath(Size size){
        File dir = DownloadService.getCacheDir();
        if(dir==null){
            return null;
        }
        return dir.getAbsolutePath()
                .concat("/" + getId() + size.toString().toLowerCase() + getFormat().toString().toLowerCase() );
    }

    protected String getFileStoragePath(Size size){
        File dir = DownloadService.getFileDir();
        if(dir==null){
            return null;
        }
        return dir.getAbsolutePath()
                .concat("/" + getId() + size.toString().toLowerCase() + getFormat().toString().toLowerCase() );
    }

    /**
     * 下载图片到cache目录;
     *
     * @param size 图片尺寸;
     * @param resultHandlerInterface 下载回调，返回下载好的{@link File}对象;
     */
    public void download2Cache(final Size size, final ResultHandlerInterface resultHandlerInterface){
        String url = getFileUrl(size);
        File dir = DownloadService.getCacheDir();
        final String path = "/" + getId() + size.toString().toLowerCase() + getFormat().toString().toLowerCase();
        DownloadService.download(url, dir, path, new ResultHandlerInterface() {
            @Override
            public void onResponse(Object response) {
                setFilePath(size, ((File)response).getAbsolutePath());
                resultHandlerInterface.onResponse(response);
            }

            @Override
            public void onError(Exception e) {
                resultHandlerInterface.onError(e);
            }
        });
    }

//    public void download2File(final Size size, final ResultHandlerInterface resultHandlerInterface){
//        String url = getFileUrl(size);
//        Context context = FacehubApi.getAppContext();
//        File dir = context.getExternalFilesDir(null);
//        String type = "image";
//        try {
//            String fullClassName = getClass()+"";
//            type = fullClassName.substring(fullClassName.lastIndexOf('.') + 1).toLowerCase();
//        }catch (Exception e){
//            LogX.e("" + e);
//            if(this instanceof Emoticon){
//                type = "emoticon";
//            }
//        }
//        final String path = "/" + type + getId() + size.toString().toLowerCase() + getFormat().toString().toLowerCase();
//        DownloadService.download(url, dir, path, new ResultHandlerInterface() {
//            @Override
//            public void onResponse(Object response) {
//                setFilePath(size, ((File)response).getAbsolutePath());
//                save2Db();
//                resultHandlerInterface.onResponse(response);
//            }
//
//            @Override
//            public void onError(Exception e) {
//                resultHandlerInterface.onError(e);
//            }
//        });
//    }
}
