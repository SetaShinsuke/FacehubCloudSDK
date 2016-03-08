//
//  FacehubCloudApi.h
//  FacehubCloudSDK
//
//  Created by satori on 16/1/18.
//  Copyright © 2016年 satori. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "FacehubCloudBanner.h"
#import "FacehubCloudPackage.h"
#import "FacehubCloudList.h"



typedef enum FCApiErrCode{
    FCApiErrCode_Success = 0,
    FCApiErrCode_InvalidAppid = -1,
    FCApiErrCode_InvalidUserid = -2,
    FCApiErrCode_InvalidToken = -3,
    FCApiErrCode_Timeout = -4,
    FCApiErrCode_NoNetwork = -5,
    FCApiErrCode_InvalidParam = -6,
    FCApiErrCode_UpdateDatabaseFailed = -7,
    FCApiErrCode_FileSaveFailed = -8,
    FCApiErrCode_ServerDataErr = -9
    /*
     more..
     */
}FCApiErrCode;

typedef void (^fca_completion_block_t) (int errCode);
typedef void (^fca_get_banners_block_t) (int errCode,NSArray<FacehubCloudBanner *> *banners);
typedef void (^fca_get_package_tags_block_t) (int errCode,NSArray<NSString *> *tags);
typedef void (^fca_get_packages_block_t) (int errCode,NSArray<NSString *> *packages);
typedef void (^fca_get_packages_by_section_block_t) (int errCode,NSString* section,NSArray<NSString *> *packages);
typedef void (^fca_get_package_detail_block_t) (int errCode,FacehubCloudPackage *package);
typedef void (^fca_download_emoticon_block_t) (int errCode,FacehubCloudEmoticon *emoticon);
typedef void (^ec_new_user_list_block_t) (int errCode,FacehubCloudList *new_userlist);

//用userlistid作为用户分组的键值索引
typedef void (^fca_get_user_list_block_t) (int errCode,NSDictionary<NSString*,FacehubCloudList *> *dic_userList);

@interface FacehubCloudApi : NSObject
/*
 功能:
 初始化appid
 参数:
 appid 开发者id
 返回:
 void 无返回
 */
+(void)setAppID:(NSString *)appID;

/*
 功能:
 设置当前有效的用户token
 参数:
 token 数据请求令牌
 返回:
 void  无返回
 */
+(void)setUserToken:(NSString *)token;

#pragma mark - 用户
/*
 功能:
 切换当前用户
 参数:
 userid 用户唯一ID
 token  数据请求令牌
 completionHandler 结果异步回调，线程不安全 根据实际使用情况可调整为主线程返回
 返回:
 void 无返回
 */
+(void)setCurrentUserID:(NSString *)userID token:(NSString *)token completionHandler:(fca_completion_block_t)block;

#pragma mark - 商店
/*
 功能:
 从服务器获取Banner信息
 参数:
 completionHandler 结果异步回调，线程不安全 根据实际使用情况可调整为主线程返回
 返回:
 void 无返回
 */
+(void)getBannersCompletionHandler:(fca_get_banners_block_t)block;

/*
 功能:
 从服务器获取Tags,可自定义参数，参数格式为REST请求参数
 参数:
 param:自定义参数，eg:@“type=section”
 completionHandler 结果异步回调，线程不安全 根据实际使用情况可调整为主线程返回
 返回:
 void 无返回
 思路:
 标准的可变参数函数设计本质还是字符串格式化输入，调用此函数的开发者根据OpenAPI的文档自行补充参数
 我们提供常用的数据获取函数，本函数主要针对SDK版本落后于OpenAPI版本，函数参数类型拓展等其它函数无法满足开发者需求的情况
 */
+(void)getPackageTagsByParam:(NSString *)param completionHandler:(fca_get_package_tags_block_t)block;


/*
 功能:
 从服务器获取Section类型的Tags
 参数:
 completionHandler 结果异步回调，线程不安全 根据实际使用情况可调整为主线程返回
 返回:
 void 无返回
 */
+(void)getPackageTagsBySectionTypeCompletionHandler:(fca_get_package_tags_block_t)block;

/*
 功能:
 从服务器获取表情包列表
 参数:
 param:自定义参数，eg:@"type=Section1"  @"section=Section1&page=2&limit=10"
 completionHandler 结果异步回调，线程不安全 根据实际使用情况可调整为主线程返回
 返回:
 void 无返回
 */
+(void)getPackagesByParam:(NSString *)param completionHandler:(fca_get_packages_block_t)block;


/*
 功能:
 从服务器获取表情包列表
 参数:
 section:目标分区名
 page:分页数，该分页第几页  >=0
 limit:当前分页package最大回传数 >=1
 completionHandler 结果异步回调，线程不安全 根据实际使用情况可调整为主线程返回
 返回
 void 无返回
 */
+(void)getPackagesBySection:(NSString *)section page:(NSInteger)page limit:(NSInteger)limit completionHandler:(fca_get_packages_by_section_block_t)block;


/*
 功能:
 获取指定ID的package详细信息
 参数:
 packageid 表情包id
 completionHandler 结果异步回调，线程不安全 根据实际使用情况可调整为主线程返回
 返回:
 void 无返回
 */
+(void)getPackageDetailByPackageID:(NSString *)packageID completionHandler:(fca_get_package_detail_block_t)block;

/*
 功能:
 收藏表情到指定分组
 参数:
 emoticonid 表情唯一标识
 userlistid 用户分组标识
 CompletionHandler 结果异步回调，线程不安全 根据实际使用情况可调整为主线程返回
 返回:
 void 无返回
 */
+(void)collectEmoticonByID:(NSString *)emoticonID toUserListByID:(NSString*)userListID completionHandler:(fca_completion_block_t)block;

/*
 功能:
 收藏表情包，默认为表情包新建用户分组
 参数:
 packageid 表情包唯一标识
 CompletionHandler 结果异步回调，线程不安全 根据实际使用情况可调整为主线程返回
 返回:
 void 无返回
 */
+(void)collectEmoticonPackageByID:(NSString *)packageID completionHandler:(fca_completion_block_t)block;

/*
 功能:
 收藏表情包到指定分组，将表情包表情全部添加到指定分组
 参数:
 packageid 表情包唯一标识
 userlistid 用户分组标识
 CompletionHandler 结果异步回调，线程不安全 根据实际使用情况可调整为主线程返回
 返回:
 void 无返回
 */
+(void)collectEmoticonPackageByID:(NSString *)packageID toUserListByID:(NSString *)userListID completionHandler:(fca_completion_block_t)block;

#pragma mark 表情资源请求

/*
 功能:
 通过表情唯一标识向服务器请求表情资源
 参数:
 emoticonid 表情包唯一标识
 CompletionHandler 结果异步回调，线程不安全 根据实际使用情况可调整为主线程返回
 返回:
 void 无返回
 */
+(void)getEmoticonByID:(NSString *)emoticonID completionHandler:(fca_download_emoticon_block_t)block;


#pragma mark 本地表情管理
/*
 功能:
 检查本地是否已收藏该表情 考虑运营时数据库在内存中，单字段遍历时间消耗不大，设计为同步执行查询
 参数:
 emoticonid 表情唯一标识
 返回:
 bool  是否存在
 */
+(BOOL)existEmoticonOfID:(NSString*)emoticonID;

/*
 功能:
 获取用户的分组
 参数:
 CompletionHandler 结果异步回调，线程不安全 根据实际使用情况可调整为主线程返回
 返回:
 void 无返回
 */
+(void)getUserListCompletionHandler:(fca_get_user_list_block_t)block;

/*
 功能:
 从指定分组批量删除表情
 参数:
 emoticons  要删除表情的表情ID数组
 userlistid 指定的用户分组
 返回:
 BOOL 是否成功，若一部分成功，一部分不成功依然会返回true
 */
+(BOOL)removeEmoticonsByEmoticonsIDArray:(NSArray<NSString *> *)emoticonsIDArray fromUserListByID:(NSString *)userListID;

/*
 功能:
 从指定分组删除单张表情
 参数:
 emoticon    要删除的表情ID
 userlistid  指定的分组
 返回:
 BOOL 是否成功
 */
+(BOOL)removeEmoticonByID:(NSString *)emoticonID fromUserListByID:(NSString *)userListID;

/*
 功能:
 新建分组
 参数:
 name 分组名
 completionHandler AFNetwork为主线程回调，线程安全
 返回:
 void 无返回
 */
+(void)createUserListByName:(NSString *)name completionHandler:(ec_new_user_list_block_t)block;;

/*
 功能:
 重命名分组
 参数:
 userListID 要重命名的列表id
 name 重命名的名字
 userlistid 分组id
 返回:
 NSString 新建的分组id
 */
+(void)renameUserListByID:(NSString *)userListID withName:(NSString *)name completionHandler:(fca_completion_block_t)block;;

/*
 功能:
 删除分组
 参数:
 userlistid  分组id
 返回:
 Bool 是否成功
 */
+(BOOL)removeUserListByID:(NSString *)userListID;

/*
 功能:
 将表情从一个分组移动到另一个分组
 参数:
 emoticonid 要移动的表情ID
 fid 移出分组id
 tid 移入分组id
 completionHandler AFNetwork为主线程回调，线程安全
 返回:
 void 无返回
 */
+(void)moveEmoticonByID:(NSString *)emoticonID fromUserListByID:(NSString *)sourceID toUserListByID:(NSString*)targetID completionHandler:(fca_completion_block_t)block;
@end
