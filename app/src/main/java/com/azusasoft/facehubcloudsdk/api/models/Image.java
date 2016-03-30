package com.azusasoft.facehubcloudsdk.api.models;

import android.content.Context;

import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.azusasoft.facehubcloudsdk.api.ResultHandlerInterface;
import com.azusasoft.facehubcloudsdk.api.utils.DownloadService;
import com.azusasoft.facehubcloudsdk.api.utils.LogX;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by SETA on 2016/3/8.
 */
public class Image {
    public Image() {
    }

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

    public Image imageFactoryByJson(JSONObject jsonObject) throws JSONException{
        this.setId( jsonObject.getString("id") )
                .setFsize( jsonObject.getInt("fsize") )
                .setHeight( jsonObject.getInt("height") )
                .setWidth( jsonObject.getInt("width") )
                .setFormat( jsonObject.getString("format"))
                .setFileUrl( jsonObject );
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

    public void download(final Size size, final ResultHandlerInterface resultHandlerInterface){
        String url = getFileUrl(size);
        if(url==null){
            LogX.e("Image url null !!");
            resultHandlerInterface.onError(new Exception("Image url null !!"));
            return;
        }
        Context context = FacehubApi.getAppContext();
        File dir = context.getExternalCacheDir();
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
}
