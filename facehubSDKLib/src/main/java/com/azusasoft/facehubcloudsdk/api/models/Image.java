package com.azusasoft.facehubcloudsdk.api.models;

import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.azusasoft.facehubcloudsdk.api.ResultHandlerInterface;
import com.azusasoft.facehubcloudsdk.api.utils.DownloadService;
import com.azusasoft.facehubcloudsdk.api.utils.UtilMethods;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;

import static com.azusasoft.facehubcloudsdk.api.utils.UtilMethods.getNewer;

/**
 * Created by SETA on 2016/3/8.
 * 图片类
 */
public class Image {

    //只用来记录下载到cache目录是否失败
    public enum DownloadStatus{
        none,downloading,fail
    }
    private DownloadStatus downloadStatus = DownloadStatus.none;

    //数据库内id
    private Long dbId;

    protected Image() {
    }

    public Image(String id){
        setId(id);
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
    private int fsize=0, height=0, width=0;
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

//    public Image(JSONObject jsonObject) throws JSONException , FacehubSDKException{
//            updateField(jsonObject);
//    }

    public Image updateField(JSONObject jsonObject) throws JSONException , FacehubSDKException{
        Image tmpImage = new Image(jsonObject.getString("id"));
        tmpImage.setId( jsonObject.getString("id") )
                .setFsize( jsonObject.getInt("fsize") )
                .setHeight( jsonObject.getInt("height") )
                .setWidth( jsonObject.getInt("width") )
                .setFormat( jsonObject.getString("format"))
                .setFileUrl( jsonObject );
        this.updateField(tmpImage);
        FacehubApi.getApi().getImageContainer().put(getId(),this);
        return this;
    }

    public Image updateField( Image image) throws FacehubSDKException{
        if(image.getId()==null){
            return this;
        }
        if(!image.getId().equals(getId())){
            throw new FacehubSDKException("Image Id not match when updating fields !");
        }
        //Id相同，根据是否有path选择更新
//        setId(image.getId());
        setDbId((Long) getNewer(getDbId(),image.getDbId()));
        setFilePath(Size.FULL, (String) getNewer(getFullPath(),image.getFullPath()) );
        setFilePath(Size.MEDIUM, (String) getNewer(getThumbPath(),image.getThumbPath()));
        setFileUrl(Size.FULL, (String) getNewer(getFileUrl(Size.FULL),image.getFileUrl(Size.FULL)));
        setFileUrl(Size.MEDIUM, (String) getNewer(getFileUrl(Size.MEDIUM),image.getFileUrl(Size.MEDIUM)));
        setFormat((String) getNewer(getFormat()+"",image.getFormat()+""));
        setHeight(image.getHeight());
        setWidth(image.getWidth());
        setFsize(image.getFsize());
        FacehubApi.getApi().getImageContainer().put(getId(),this);
        return this;
    }

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

//    private String getFilePath(Size size){
//        switch (size){
//            case FULL:
//                return fullPath;
//            case MEDIUM:
//                return mediumPath;
//            default:
//                return null;
//        }
//    }

    public String getFullPath(){
        return fullPath;
    }

    public String getThumbPath(){
        return mediumPath;
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
     * @param size 图片尺寸;
     * @param resultHandlerInterface 下载回调，返回下载好的{@link File}对象;
     */
    private void download2Cache(final Size size, final ResultHandlerInterface resultHandlerInterface){
        downloadStatus = DownloadStatus.downloading;
        String url = getFileUrl(size);
        File dir = DownloadService.getCacheDir();
        final String path = "/" + getId() + size.toString().toLowerCase() + getFormat().toString().toLowerCase();
        DownloadService.download(url, dir, path, new ResultHandlerInterface() {
            @Override
            public void onResponse(Object response) {
                setFilePath(size, ((File)response).getAbsolutePath());
                downloadStatus = DownloadStatus.none;
                resultHandlerInterface.onResponse(response);
            }

            @Override
            public void onError(Exception e) {
                downloadStatus = DownloadStatus.fail;
                resultHandlerInterface.onError(e);
            }
        });
    }
    /**
     * 下载【缩略图】到cache目录;
     * @param resultHandlerInterface 下载回调，返回下载好的{@link File}对象;
     */
    public void downloadThumb2Cache(ResultHandlerInterface resultHandlerInterface){
        download2Cache(Size.MEDIUM,resultHandlerInterface);
    }
    /**
     * 下载【原图】到cache目录;
     * @param resultHandlerInterface 下载回调，返回下载好的{@link File}对象;
     */
    public void downloadFull2Cache(ResultHandlerInterface resultHandlerInterface){
        download2Cache(Size.FULL,resultHandlerInterface);
    }
    /**
     * 下载【缩略图】和【原图】到cache目录;
     * @param resultHandlerInterface 下载回调，返回下载好的{@link File}对象;
     */
    public void download2Cache(final ResultHandlerInterface resultHandlerInterface){
        downloadThumb2Cache(new ResultHandlerInterface() {
            @Override
            public void onResponse(Object response) {
                downloadFull2Cache(resultHandlerInterface);
            }

            @Override
            public void onError(Exception e) {
                resultHandlerInterface.onError(new Exception("下载图片缩略图出错 : " + e));
            }
        });
    }


    public DownloadStatus getDownloadStatus(){
        return this.downloadStatus;
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
