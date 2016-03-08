package com.azusasoft.facehubcloudsdk.api;

import com.azusasoft.facehubcloudsdk.api.model.User;

import java.util.ArrayList;

/**
 * Created by SETA on 2016/3/8.
 */
public class FacehubApi {
    protected final static String HOST = "http://10.0.0.37:9292";  //内网
//    public final static String HOST = "http://115.28.208.104:9292";  //外网

    private static FacehubApi api;
    private String appId = "test-app-id";
    private User user;

    private FacehubApi() {
    }

    public static FacehubApi getApi() {
        if (api == null) {
            api = new FacehubApi();
        }
        return api;
    }

    /**
     * 初始化appId( 可在AndroidManifest.xml 中设置)
     *
     * @param appId 开发者id.
     */
    public void setAppId(String appId) {
        this.appId = appId;
    }

    /**
     * 设置当前有效的用户token
     *
     * @param token 数据请求令牌.
     */
    public void setUserToken(String token) {

    }

    /**
     * 切换当前用户
     *
     * @param userId                 用户唯一id.
     * @param token                  数据请求令牌.
     * @param resultHandlerInterface 结果回调.
     */
    public void setCurrentUserId(String userId, String token, ResultHandlerInterface resultHandlerInterface) {

    }

    //region 表情商店

    /**
     * 从服务器获取Banner信息
     *
     * @param resultHandlerInterface 结果回调.
     */
    public void getBanners(ResultHandlerInterface resultHandlerInterface) {

    }

    /**
     * 从服务器获取Tags，可自定义参数，参数格式为REST请求参数
     *
     * @param param 自定义参数，eg: param = "type=section";
     */
    public void getPackageTagsByParam(String param) {

    }

    /**
     * 从服务器获取Section类型的Tags
     *
     * @param resultHandlerInterface 结果回调
     */
    public void getPackageTagsBySection(ResultHandlerInterface resultHandlerInterface) {

    }

    /**
     * 从服务器获取表情包列表
     *
     * @param param                  自定义参数，eg: param = "type=Section1" ; param = "section=Section1&page=2;&limit=10";
     * @param resultHandlerInterface 结果回调
     */
    public void getPackagesByParam(String param, ResultHandlerInterface resultHandlerInterface) {

    }

    /**
     * 从服务器获取表情包列表
     *
     * @param sectionName            目标分区名
     * @param page                   分页数，该分页第几页  >=0
     * @param limit                  limit:当前分页package最大回传数 >=1
     * @param resultHandlerInterface completionHandler 结果回调
     */
    public void getPackagesBySection(String sectionName, int page, int limit, ResultHandlerInterface resultHandlerInterface) {

    }

    /**
     * 获取指定ID的package详细信息
     *
     * @param packageId              表情包id
     * @param resultHandlerInterface 结果回调
     */
    public void getPackageDetailById(String packageId, ResultHandlerInterface resultHandlerInterface) {

    }

    /**
     * 收藏表情到指定分组
     *
     * @param emoticonId             表情唯一标识表情唯一标识
     * @param toUserListId           用户分组标识
     * @param resultHandlerInterface 结果回调
     */
    public void collectEmoById(String emoticonId, String toUserListId, ResultHandlerInterface resultHandlerInterface) {

    }

    /**
     * 收藏表情包，默认为表情包新建用户分组
     *
     * @param packageId              表情包唯一标识
     * @param resultHandlerInterface 结果回调
     */
    public void collectEmoPackageById(String packageId, ResultHandlerInterface resultHandlerInterface) {

    }

    /**
     * 收藏表情包到指定分组，将表情包表情全部添加到指定分组
     *
     * @param packageId              表情包唯一标识
     * @param toUserListId           用户分组标识
     * @param resultHandlerInterface 结果回调
     */
    public void collectEmoPackageById(String packageId, String toUserListId, ResultHandlerInterface resultHandlerInterface) {

    }
    //endregion

    //region 表情资源请求

    /**
     * 通过表情唯一标识向服务器请求表情资源
     *
     * @param emoticonId             表情包唯一标识
     * @param resultHandlerInterface 结果回调
     */
    public void getEmoticonById(String emoticonId, ResultHandlerInterface resultHandlerInterface) {

    }
    //endregion

    /**
     * 检查本地是否已收藏该表情
     *
     * @param emoticonId 表情唯一标识
     * @return 是否已收藏
     */
    public boolean isEmoticonCollected(String emoticonId) {
        return true;
    }

    /**
     * 获取用户的分组
     *
     * @param resultHandlerInterface 结果回调
     */
    public void getUserList(ResultHandlerInterface resultHandlerInterface) {

    }

    /**
     * 从指定分组批量删除表情
     *
     * @param emoticonIds 要删除表情的表情ID数组
     * @param userListId  指定的用户表情分组
     * @return 是否删除成功，若一部分成功，一部分不成功依然会返回true
     */
    public boolean removeEmoticonsByIds(ArrayList<String> emoticonIds, String userListId) {
        return true;
    }

    /**
     * 从指定分组删除单张表情
     *
     * @param emoticonId 要删除的表情ID
     * @param userListId 指定的分组
     * @return 是否删除成功
     */
    public boolean removeEmoticonById(String emoticonId, String userListId) {
        return true;
    }

    /**
     * 新建分组
     *
     * @param listName               分组名
     * @param resultHandlerInterface 结果回调
     */
    public void createUserListByName(String listName, ResultHandlerInterface resultHandlerInterface) {

    }

    /**
     * 重命名分组
     *
     * @param userListId             要重命名的列表id
     * @param name                   重命名的名字
     * @param resultHandlerInterface 结果回调
     */
    public void renameUserListById(String userListId, String name, ResultHandlerInterface resultHandlerInterface) {

    }

    /**
     * 删除分组
     *
     * @param userListId 分组id
     * @return 是否删除成功
     */
    public boolean removeUserListById(String userListId) {
        return true;
    }

    /**
     * 将表情从一个分组移动到另一个分组
     *
     * @param emoticonId             要移动的表情ID
     * @param fromId                 移出分组id
     * @param toId                   移入分组id
     * @param resultHandlerInterface 结果回调
     */
    public void moveEmoticonById(String emoticonId, String fromId, String toId, ResultHandlerInterface resultHandlerInterface) {

    }
}
