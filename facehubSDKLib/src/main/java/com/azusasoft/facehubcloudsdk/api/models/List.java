package com.azusasoft.facehubcloudsdk.api.models;

import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.azusasoft.facehubcloudsdk.api.ResultHandlerInterface;
import com.azusasoft.facehubcloudsdk.api.utils.LogX;
import com.azusasoft.facehubcloudsdk.api.utils.UtilMethods;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

import static com.azusasoft.facehubcloudsdk.api.utils.UtilMethods.isJsonWithKey;

/**
 * Created by SETA on 2016/3/8.
 * 表情的集合
 */
public class List {
    private String id;
    private String name;
    private ArrayList<Emoticon> emoticons = new ArrayList<>();
    private Emoticon cover; //可能为空

    /**
     * {@link List}工厂方法
     * 注意!关于emoticons的具体处理在子类中进行( {@link UserList} 与 {@link EmoPackage} )
     *
     * @param jsonObject
     * @return
     * @throws JSONException
     */
    public List updateField(JSONObject jsonObject) throws JSONException {
        this.setId(jsonObject.getString("id"));
        this.setName(jsonObject.getString("name"));
        if (isJsonWithKey(jsonObject, "cover")
                && isJsonWithKey(jsonObject, "cover_detail")) { //有封面字段
            JSONObject coverDetailJson = jsonObject.getJSONObject("cover_detail");
//            Emoticon coverEmoticon = FacehubApi.getApi().getEmoticonContainer().getUniqueEmoticonById(coverDetailJson.getString("id"));

            Emoticon coverImage = FacehubApi.getApi().getEmoticonContainer()
                    .getUniqueEmoticonById(coverDetailJson.getString("id"));
            coverImage.updateField(coverDetailJson);
            if (getCover()==null //原封面空
                    || getCover().getFilePath(Image.Size.FULL) == null //原封面没有下载
                    || !getCover().getId().equals(coverDetailJson.getString("id")) ) { //新封面与原封面不同
                setCover(coverImage);
            }else { //原有封面与新封面相同
                getCover().updateField(coverImage);
            }

        } else { //没有封面字段，则根据是否已有封面来决定是否更新
            if(getCover()!=null && getCover().getFilePath(Image.Size.FULL)!=null) {
                setCover(null);
            }
        }

        //表情内容
        if( isJsonWithKey(jsonObject,"contents") ) {
            ArrayList<Emoticon> emoticonsTmp = new ArrayList<>();
            JSONArray jsonArray = jsonObject.getJSONArray("contents");
            EmoticonContainer emoticonContainer = FacehubApi.getApi().getEmoticonContainer();
            for(int i=0;i<jsonArray.length();i++){
                String emoId = jsonArray.getString(i);
                Emoticon emoticon = emoticonContainer.getUniqueEmoticonById(emoId);
                LogX.fastLog("emoticon.id == emoId ?? " + emoId.equals(emoticon.getId()));
                if( !emoId.equals(emoticon.getId()) ){
                    emoticon = emoticonContainer.getUniqueEmoticonById(emoId);
                    LogX.fastLog("emoticon.id == emoId again ?? " + emoId.equals(emoticon.getId()));
                }
                emoticonsTmp.add(emoticon);
            }
            setEmoticons(emoticonsTmp);
        }

        if( isJsonWithKey(jsonObject,"contents_details") ) { //有"contents_details"字段
            JSONObject emoDetailsJson = jsonObject.getJSONObject("contents_details");
            for (Emoticon emoticon:getEmoticons()){
                emoticon.updateField(emoDetailsJson.getJSONObject(emoticon.getId()));
            }
        }
        return this;
    }

//    @Override
//    public String toString() {
//        return "\n[List] : " + "\nid : " + id
//                + "\nname : " + name
//                + "\nemoticons : " + emoticons
//                + "\ncover : " + cover
//                ;
//    }

    public String getId() {
        return id;
    }

    protected void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    protected void setName(String name) {
        this.name = name;
    }

    public ArrayList<Emoticon> getEmoticons() {
        return emoticons;
    }

    protected void setEmoticons(ArrayList<Emoticon> emoticons) {
        this.emoticons = emoticons;
    }
    public Emoticon getEmoticonById(String id){
        Emoticon emoticon=null;
        for(Emoticon emoticon1 : emoticons)
            if (emoticon1.getId().equals(id)) {
                emoticon=emoticon1;
                break;
            }
        return  emoticon;
    }
    public Emoticon getCover() {
        if (cover != null) {
            return cover;
        }
        if (getEmoticons().size() > 0) {
            return getEmoticons().get(0);
        }
        return null;
    }

    protected void setCover(Emoticon cover) {
        this.cover = cover;
    }

    /**
     * 下载列表封面;
     *
     * @param size 要下载的尺寸;
     * @param resultHandlerInterface 封面下载回调,返回一个下载好的文件{@link File}对象;
     */
    public void downloadCover(final Image.Size size, final ResultHandlerInterface resultHandlerInterface) {
        final Emoticon cover = getCover();
        if (cover != null ){
            if(cover.getFilePath(size) == null) {
                cover.download2Cache(size, resultHandlerInterface);
            }else {
                resultHandlerInterface.onResponse(cover);
            }
        }else {
            resultHandlerInterface.onResponse(new Exception("封面下载出错 : cover为空"));
        }
    }
}
