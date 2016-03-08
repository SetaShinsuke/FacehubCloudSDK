//
//  FacehubCloudApi.m
//  FacehubCloudSDK
//
//  Created by satori on 16/1/18.
//  Copyright © 2016年 satori. All rights reserved.
//

#import "FacehubCloudApi.h"
#import "FacehubCloudGlobal.h"
#import "AFHTTPSessionManager.h"
#import "FacehubCloudDB.h"
#import "FacehubCloudParameter.h"
#import "FacehubCloudList.h"
#import "FacehubCloudEmoticon.h"

#define  NEED_DEBUG_DETAIL false

@implementation FacehubCloudApi
static int timeoutSeconds=15;
//关于重试，如果有两个线程都调用了里面的接口，那么就会多发很多重试请求，因此不能这样做。但只有get类请求才会互相冲突，其他类请求一般都是不会同时进行的，所以在所有非get请求的函数里调用重试比较好。
//只有两个例外，那就是获取所有的列表资源的那个接口需要接重试
#pragma mark - 对外接口
//设置appID
+(void)setAppID:(NSString *)appID
{
    [FacehubCloudParameter setAppID:appID];
}

//设置token
+(void)setUserToken:(NSString *)token
{
    [FacehubCloudParameter setUserToken:token];
}

#pragma mark - 用户
//切换用户
+(void)setCurrentUserID:(NSString *)userID token:(NSString *)token completionHandler:(fca_completion_block_t)block
{
    [FacehubCloudParameter setUserID:userID];
    [FacehubCloudParameter setUserToken:token];
    block(FCApiErrCode_Success);
}

#pragma mark - 商店
//获取banner
+(void)getBannersCompletionHandler:(fca_get_banners_block_t)block
{
    NSString *url=[NSString stringWithFormat:@"%@/api/v1/recommends/last",HOST];
    NSDictionary *parameterDic=[FacehubCloudParameter networkParameterDic];
    
    AFHTTPSessionManager *manager=[self getSessionManager];
    [manager GET:url
      parameters:parameterDic
        progress:nil
         success:^(NSURLSessionDataTask * _Nonnull task, id  _Nullable responseObject) {
             if(NEED_DEBUG_DETAIL)NSLog(@"success %@",responseObject);
             
             NSMutableArray *bannersArray=[NSMutableArray array];
             if(IS_NOT_NULL([responseObject objectForKey:@"recommends"])){
                 for(NSInteger i=0;i<[responseObject[@"recommends"] count];++i){
                     NSDictionary *bannerDic=responseObject[@"recommends"][i];
                     FacehubCloudBanner *banner=[[FacehubCloudBanner alloc]initWithDic:bannerDic];
                     if(banner){
                         [bannersArray addObject:banner];
                     }
                 }
                 if([bannersArray count]>0){
                     block(FCApiErrCode_Success,bannersArray);
                 }else{
                     block(FCApiErrCode_ServerDataErr,nil);
                 }
             }else{
                 block(FCApiErrCode_ServerDataErr,nil);
             }
         }
         failure:^(NSURLSessionDataTask * _Nullable task, NSError * _Nonnull error) {
             if(NEED_DEBUG_DETAIL)NSLog(@"请求发生了错误：%@",error.localizedDescription);
             if(error.code == NSURLErrorTimedOut){
                 block(FCApiErrCode_Timeout,nil);
             }else{
                 block(FCApiErrCode_NoNetwork,nil);
             }
         }];
}

//根据自定义参数（包括属性，等等）来获取一系列包的标签
+(void)getPackageTagsByParam:(NSString *)param completionHandler:(fca_get_package_tags_block_t)block
{
    NSString *url=[NSString stringWithFormat:@"%@/api/v1/package_tags?%@",HOST,param];
    
    NSDictionary *parameterDic=[FacehubCloudParameter networkParameterDic];
    
    AFHTTPSessionManager *manager=[self getSessionManager];
    [manager GET:url
      parameters:parameterDic
        progress:nil
         success:^(NSURLSessionDataTask * _Nonnull task, id  _Nullable responseObject) {
             if(NEED_DEBUG_DETAIL)NSLog(@"success %@",responseObject);
             
             //没有一个好方法去获得dic的那个字段,因此只能这样获取
             NSArray *valueArray=[responseObject allValues];
             if([valueArray count]==1){
                 NSArray *tags=valueArray[0];
                 block(FCApiErrCode_Success,tags);
             }else{
                 block(FCApiErrCode_InvalidParam,nil);
             }
         }
         failure:^(NSURLSessionDataTask * _Nullable task, NSError * _Nonnull error) {
             if(NEED_DEBUG_DETAIL)NSLog(@"请求发生了错误：%@",error.localizedDescription);
             if(error.code == NSURLErrorTimedOut){
                 block(FCApiErrCode_Timeout,nil);
             }else{
                 block(FCApiErrCode_NoNetwork,nil);
             }
         }];
}

//根据分区（属性）来获取一系列包的标签
+(void)getPackageTagsBySectionTypeCompletionHandler:(fca_get_package_tags_block_t)block
{
    NSString *url=[NSString stringWithFormat:@"%@/api/v1/package_tags",HOST];
    
    NSMutableDictionary *parameterDic=[FacehubCloudParameter networkParameterDic];
    [parameterDic setObject:@"section" forKey:@"type"];
    
    AFHTTPSessionManager *manager=[self getSessionManager];
    [manager GET:url
      parameters:parameterDic
        progress:nil
         success:^(NSURLSessionDataTask * _Nonnull task, id  _Nullable responseObject) {
             if(NEED_DEBUG_DETAIL)NSLog(@"success %@",responseObject);
             
             if(IS_NOT_NULL([responseObject objectForKey:@"section"])){
                 NSArray *tags=responseObject[@"section"];
                 block(FCApiErrCode_Success,tags);
             }else{
                 block(FCApiErrCode_ServerDataErr,nil);
             }
         }
         failure:^(NSURLSessionDataTask * _Nullable task, NSError * _Nonnull error) {
             if(NEED_DEBUG_DETAIL)NSLog(@"请求发生了错误：%@",error.localizedDescription);
             if(error.code == NSURLErrorTimedOut){
                 block(FCApiErrCode_Timeout,nil);
             }else{
                 block(FCApiErrCode_NoNetwork,nil);
             }
         }];
}

//根据自定义参数（包括标签，页码，每页个数，等等）来获取一系列包
+(void)getPackagesByParam:(NSString *)param completionHandler:(fca_get_packages_block_t)block
{
    NSString *url=[NSString stringWithFormat:@"%@/api/v1/packages?%@",HOST,param];
    
    NSDictionary *parameterDic=[FacehubCloudParameter networkParameterDic];
    
    AFHTTPSessionManager *manager=[self getSessionManager];
    [manager GET:url
      parameters:parameterDic
        progress:nil
         success:^(NSURLSessionDataTask * _Nonnull task, id  _Nullable responseObject) {
             if(NEED_DEBUG_DETAIL)NSLog(@"success %@",responseObject);
             
             NSMutableArray *packagesIDArray=[NSMutableArray array];
             if(IS_NOT_NULL([responseObject objectForKey:@"packages"])){
                 for(NSInteger i=0;i<[responseObject[@"packages"] count];++i){
                     if(IS_NOT_NULL([responseObject[@"packages"][i] objectForKey:@"id"])){
                         NSString *packageID=responseObject[@"packages"][i][@"id"];
                         [packagesIDArray addObject:packageID];
                     }
                 }
                 if([packagesIDArray count]==0){
                     block(FCApiErrCode_ServerDataErr,nil);
                 }else{
                     block(FCApiErrCode_Success,packagesIDArray);
                 }
             }else{
                 block(FCApiErrCode_ServerDataErr,nil);
             }
         }
         failure:^(NSURLSessionDataTask * _Nullable task, NSError * _Nonnull error) {
             if(NEED_DEBUG_DETAIL)NSLog(@"请求发生了错误：%@",error.localizedDescription);
             if(error.code == NSURLErrorTimedOut){
                 block(FCApiErrCode_Timeout,nil);
             }else{
                 block(FCApiErrCode_NoNetwork,nil);
             }
         }];
}

//根据指定条件来获取一系列包
+(void)getPackagesBySection:(NSString *)section page:(NSInteger)page limit:(NSInteger)limit completionHandler:(fca_get_packages_by_section_block_t)block
{
    NSString *url=[NSString stringWithFormat:@"%@/api/v1/packages",HOST];
    
    NSMutableDictionary *parameterDic=[FacehubCloudParameter networkParameterDic];
    [parameterDic setObject:section forKey:@"section"];
    [parameterDic setObject:[NSNumber numberWithInteger:page] forKey:@"page"];
    [parameterDic setObject:[NSNumber numberWithInteger:limit] forKey:@"limit"];
    
    AFHTTPSessionManager *manager=[self getSessionManager];
    [manager GET:url
      parameters:parameterDic
        progress:nil
         success:^(NSURLSessionDataTask * _Nonnull task, id  _Nullable responseObject) {
             if(NEED_DEBUG_DETAIL)NSLog(@"success %@",responseObject);
             
             NSMutableArray *packagesIDArray=[NSMutableArray array];
             if(IS_NOT_NULL([responseObject objectForKey:@"packages"])){
                 for(NSInteger i=0;i<[responseObject[@"packages"] count];++i){
                     if(IS_NOT_NULL([responseObject[@"packages"][i] objectForKey:@"id"])){
                         NSString *packageID=responseObject[@"packages"][i][@"id"];
                         [packagesIDArray addObject:packageID];
                     }
                 }
                 if([packagesIDArray count]==0){
                     block(FCApiErrCode_ServerDataErr,section,nil);
                 }else{
                     block(FCApiErrCode_Success,section,packagesIDArray);
                 }
             }else{
                 block(FCApiErrCode_ServerDataErr,section,nil);
             }
         }
         failure:^(NSURLSessionDataTask * _Nullable task, NSError * _Nonnull error) {
             if(NEED_DEBUG_DETAIL)NSLog(@"请求发生了错误：%@",error.localizedDescription);
             if(error.code == NSURLErrorTimedOut){
                 block(FCApiErrCode_Timeout,section,nil);
             }else{
                 block(FCApiErrCode_NoNetwork,section,nil);
             }
         }];
}

//获取表情包详情
+(void)getPackageDetailByPackageID:(NSString *)packageID completionHandler:(fca_get_package_detail_block_t)block
{
    NSString *url=[NSString stringWithFormat:@"%@/api/v1/packages/%@",HOST,packageID];
    
    NSMutableDictionary *parameterDic=[FacehubCloudParameter networkParameterDic];
    
    AFHTTPSessionManager *manager=[self getSessionManager];
    [manager GET:url
      parameters:parameterDic
        progress:nil
         success:^(NSURLSessionDataTask * _Nonnull task, id  _Nullable responseObject) {
             if(NEED_DEBUG_DETAIL)NSLog(@"success %@",responseObject);
             
             if(IS_NOT_NULL([responseObject objectForKey:@"package"])){
                 FacehubCloudPackage *package=[[FacehubCloudPackage alloc]initWithDic:responseObject[@"package"]];
                 if(package){
                     block(FCApiErrCode_Success,package);
                 }else{
                     block(FCApiErrCode_ServerDataErr,nil);
                 }
             }else{
                 block(FCApiErrCode_ServerDataErr,nil);
             }
         }
         failure:^(NSURLSessionDataTask * _Nullable task, NSError * _Nonnull error) {
             if(NEED_DEBUG_DETAIL)NSLog(@"请求发生了错误：%@",error.localizedDescription);
             if(error.code == NSURLErrorTimedOut){
                 block(FCApiErrCode_Timeout,nil);
             }else{
                 block(FCApiErrCode_NoNetwork,nil);
             }
         }];
}

//收藏单个表情到指定分组
+(void)collectEmoticonByID:(NSString *)emoticonID toUserListByID:(NSString *)userListID completionHandler:(fca_completion_block_t)block
{
    [self retryAllFailedExcuteWithCompletionHandler:^(int errCode) {
        NSString *userID=[FacehubCloudParameter currentUserID];
        NSArray *contentsArray=@[emoticonID];
        NSString *url=[NSString stringWithFormat:@"%@/api/v1/users/%@/lists/%@",HOST,userID,userListID];
        
        NSMutableDictionary *parameterDic=[FacehubCloudParameter networkParameterDic];
        [parameterDic setObject:contentsArray forKey:@"contents"];
        [parameterDic setObject:@"add" forKey:@"action"];
        [parameterDic setObject:@"1" forKey:@"emoticon_details"];
        
        AFHTTPSessionManager *manager=[self getSessionManager];
        [manager POST:url
           parameters:parameterDic
             progress:nil
              success:^(NSURLSessionDataTask * _Nonnull task, id  _Nullable responseObject) {
                  if(NEED_DEBUG_DETAIL)NSLog(@"success %@",responseObject);
                  
                  if(IS_NOT_NULL([responseObject objectForKey:@"list"])&&IS_NOT_NULL([responseObject objectForKey:@"emoticons"])){
                      FacehubCloudList *list=[[FacehubCloudList alloc]initWithNetDic:responseObject[@"list"]];
                      NSDictionary *emoticonsDic=[responseObject objectForKey:@"emoticons"];
                      FacehubCloudEmoticon *emoticon=[[FacehubCloudEmoticon alloc]initWithNetDic:emoticonsDic[emoticonID]];
                      if(list && emoticon){
                          [[FacehubCloudDB defaultDB]collectEmoticon:emoticon
                                                              toList:list
                                                   completionHandler:^(bool success) {
                                                       if(success){
                                                           block(FCApiErrCode_Success);
                                                       }else{
                                                           block(FCApiErrCode_UpdateDatabaseFailed);
                                                       }
                                                   }];
                      }else{
                          block(FCApiErrCode_ServerDataErr);
                      }
                  }else{
                      block(FCApiErrCode_ServerDataErr);
                  }
              }
              failure:^(NSURLSessionDataTask * _Nullable task, NSError * _Nonnull error) {
                  if(NEED_DEBUG_DETAIL)NSLog(@"请求发生了错误：%@",error.localizedDescription);
                  if(error.code == NSURLErrorTimedOut){
                      block(FCApiErrCode_Timeout);
                  }else{
                      block(FCApiErrCode_NoNetwork);
                  }
              }];
    }];
}

//收藏表情包到新列表
+(void)collectEmoticonPackageByID:(NSString *)packageID completionHandler:(fca_completion_block_t)block
{
    [self retryAllFailedExcuteWithCompletionHandler:^(int errCode) {
        NSString *userID=[FacehubCloudParameter currentUserID];
        NSString *url=[NSString stringWithFormat:@"%@/api/v1/users/%@/lists/batch",HOST,userID];
        
        NSMutableDictionary *parameterDic=[FacehubCloudParameter networkParameterDic];
        [parameterDic setObject:packageID forKey:@"list_id"];
        [parameterDic setObject:@"" forKey:@"dest_id"];
        [parameterDic setObject:@"1" forKey:@"emoticon_details"];
        
        AFHTTPSessionManager *manager=[self getSessionManager];
        [manager POST:url
           parameters:parameterDic
             progress:nil
              success:^(NSURLSessionDataTask * _Nonnull task, id  _Nullable responseObject) {
                  if(NEED_DEBUG_DETAIL)NSLog(@"success %@",responseObject);
                  
                  if(IS_NOT_NULL([responseObject objectForKey:@"emoticon"])&&IS_NOT_NULL([responseObject objectForKey:@"list"])){
                      FacehubCloudList *list=[[FacehubCloudList alloc]initWithNetDic:responseObject[@"list"]];
                      if(list){
                          NSMutableArray *emoticons=[NSMutableArray array];
                          for(NSString *emoticonID in responseObject[@"emoticon"]){
                              if(IS_NOT_NULL([responseObject[@"emoticon"] objectForKey:emoticonID])){
                                  NSDictionary *emoticonDic=responseObject[@"emoticon"][emoticonID];
                                  FacehubCloudEmoticon *emoticon=[[FacehubCloudEmoticon alloc]initWithNetDic:emoticonDic];
                                  if(emoticon){
                                      [emoticons addObject:emoticon];
                                  }
                              }
                          }
                          BOOL serverErr=NO;
                          if([emoticons count]!=[responseObject[@"emoticon"] count]){
                              serverErr=YES;
                          }
                          [[FacehubCloudDB defaultDB]collectEmoticonPackageWithEmoticons:emoticons
                                                                               toNewList:list
                                                                       completionHandler:^(bool success) {
                                                                           if(success){
                                                                               if(serverErr){
                                                                                   block(FCApiErrCode_ServerDataErr);
                                                                               }else{
                                                                                   block(FCApiErrCode_Success);
                                                                               }
                                                                           }else{
                                                                               block(FCApiErrCode_UpdateDatabaseFailed);
                                                                           }
                                                                       }];
                      }else{
                          block(FCApiErrCode_ServerDataErr);
                      }
                  }else{
                      block(FCApiErrCode_ServerDataErr);
                  }
              }
              failure:^(NSURLSessionDataTask * _Nullable task, NSError * _Nonnull error) {
                  if(NEED_DEBUG_DETAIL)NSLog(@"请求发生了错误：%@",error.localizedDescription);
                  if(error.code == NSURLErrorTimedOut){
                      block(FCApiErrCode_Timeout);
                  }else{
                      block(FCApiErrCode_NoNetwork);
                  }
              }];
    }];
}

//收藏表情包到已存在的列表
+(void)collectEmoticonPackageByID:(NSString *)packageID toUserListByID:(NSString *)userListID completionHandler:(fca_completion_block_t)block
{
    [self retryAllFailedExcuteWithCompletionHandler:^(int errCode) {
        NSString *userID=[FacehubCloudParameter currentUserID];
        NSString *url=[NSString stringWithFormat:@"%@/api/v1/users/%@/lists/batch",HOST,userID];
        
        NSMutableDictionary *parameterDic=[FacehubCloudParameter networkParameterDic];
        [parameterDic setObject:packageID forKey:@"list_id"];
        [parameterDic setObject:userListID forKey:@"dest_id"];
        [parameterDic setObject:@"1" forKey:@"emoticon_details"];
        
        AFHTTPSessionManager *manager=[self getSessionManager];
        [manager POST:url
           parameters:parameterDic
             progress:nil
              success:^(NSURLSessionDataTask * _Nonnull task, id  _Nullable responseObject) {
                  if(NEED_DEBUG_DETAIL)NSLog(@"success %@",responseObject);
                  
                  if(IS_NOT_NULL([responseObject objectForKey:@"emoticon"])&&IS_NOT_NULL([responseObject objectForKey:@"list"])){
                      FacehubCloudList *list=[[FacehubCloudList alloc]initWithNetDic:responseObject[@"list"]];
                      if(list){
                          NSMutableArray *emoticons=[NSMutableArray array];
                          for(NSString *emoticonID in responseObject[@"emoticon"]){
                              if(IS_NOT_NULL([responseObject[@"emoticon"] objectForKey:emoticonID])){
                                  NSDictionary *emoticonDic=responseObject[@"emoticon"][emoticonID];
                                  FacehubCloudEmoticon *emoticon=[[FacehubCloudEmoticon alloc]initWithNetDic:emoticonDic];
                                  if(emoticon){
                                      [emoticons addObject:emoticon];
                                  }
                              }
                          }
                          BOOL serverErr=NO;
                          if([emoticons count]!=[responseObject[@"emoticon"] count]){
                              serverErr=YES;
                          }
                          [[FacehubCloudDB defaultDB]collectEmoticonPackageWithEmoticons:emoticons
                                                                             toExistList:list
                                                                       completionHandler:^(bool success) {
                                                                           if(success){
                                                                               if(serverErr){
                                                                                   block(FCApiErrCode_ServerDataErr);
                                                                               }else{
                                                                                   block(FCApiErrCode_Success);
                                                                               }
                                                                               
                                                                           }else{
                                                                               block(FCApiErrCode_UpdateDatabaseFailed);
                                                                           }
                                                                       }];
                      }else{
                          block(FCApiErrCode_ServerDataErr);
                      }
                      
                  }else{
                      block(FCApiErrCode_ServerDataErr);
                  }
              }
              failure:^(NSURLSessionDataTask * _Nullable task, NSError * _Nonnull error) {
                  if(NEED_DEBUG_DETAIL)NSLog(@"请求发生了错误：%@",error.localizedDescription);
                  if(error.code == NSURLErrorTimedOut){
                      block(FCApiErrCode_Timeout);
                  }else{
                      block(FCApiErrCode_NoNetwork);
                  }
              }];
    }];
}

#pragma mark - 表情资源请求
//获取单个表情（含下载）
+(void)getEmoticonByID:(NSString *)emoticonID completionHandler:(fca_download_emoticon_block_t)block
{
    NSString *url=[NSString stringWithFormat:@"%@/api/v1/emoticons/%@",HOST,emoticonID];
    
    NSDictionary *parameterDic=[FacehubCloudParameter networkParameterDic];
    
    AFHTTPSessionManager *manager=[self getSessionManager];
    [manager GET:url
      parameters:parameterDic
        progress:nil
         success:^(NSURLSessionDataTask * _Nonnull task, id  _Nullable responseObject) {
             if(NEED_DEBUG_DETAIL)NSLog(@"success %@",responseObject);
             
             if(IS_NOT_NULL([responseObject objectForKey:@"emoticon"])){
                 __block FacehubCloudEmoticon *emoticon=[[FacehubCloudEmoticon alloc]initWithNetDic:responseObject[@"emoticon"]];
                 if(emoticon){
                     NSURL* _url = [NSURL URLWithString:[emoticon fullURL]];
                     NSData *data=[NSData dataWithContentsOfURL:_url];
                     if(!data){
                         block(FCApiErrCode_NoNetwork,nil);
                     }else{
                         NSString *emoticonName=[NSString stringWithFormat:@"%@",emoticonID];
                         NSString *emoticonPath=[self getEmoticonPathWithName:emoticonName];
                         BOOL result=[data writeToFile:emoticonPath atomically:YES];
                         if(!result){
                             block(FCApiErrCode_FileSaveFailed,nil);
                         }
                         [[FacehubCloudDB defaultDB]insertEmoticon:emoticon
                                                 completionHandler:^(bool success) {
                                                     if(success){
                                                         block(FCApiErrCode_Success,emoticon);
                                                     }else{
                                                         block(FCApiErrCode_UpdateDatabaseFailed,nil);
                                                     }
                                                 }];
                     }
                 }else{
                     block(FCApiErrCode_ServerDataErr,nil);
                 }
             }else{
                 block(FCApiErrCode_ServerDataErr,nil);
             }
             
         }
         failure:^(NSURLSessionDataTask * _Nullable task, NSError * _Nonnull error) {
             if(NEED_DEBUG_DETAIL)NSLog(@"请求发生了错误：%@",error.localizedDescription);
             if(error.code == NSURLErrorTimedOut){
                 block(FCApiErrCode_Timeout,nil);
             }else{
                 block(FCApiErrCode_NoNetwork,nil);
             }
         }];
}

#pragma mark 本地表情管理
//检查是否存在表情文件
+(BOOL)existEmoticonOfID:(NSString *)emoticonID
{
    NSString *emoticonName=[NSString stringWithFormat:@"%@",emoticonID];
    NSString *emoticonPath=[self getEmoticonPathWithName:emoticonName];
    NSData *data=[NSData dataWithContentsOfFile:emoticonPath];
    if(data){
        return YES;
    }else{
        return NO;
    }
}

//获取用户分组
+(void)getUserListCompletionHandler:(fca_get_user_list_block_t)block
{
    NSString *userID=[FacehubCloudParameter currentUserID];
    NSString *url=[NSString stringWithFormat:@"%@/api/v1/users/%@/lists",HOST,userID];
    
    NSDictionary *parameterDic=[FacehubCloudParameter networkParameterDic];
    
    [self retryAllFailedExcuteWithCompletionHandler:^(int errCode) {
        if(errCode == FCApiErrCode_Success){
            AFHTTPSessionManager *manager=[self getSessionManager];
            [manager GET:url
              parameters:parameterDic
                progress:nil
                 success:^(NSURLSessionDataTask * _Nonnull task, id  _Nullable responseObject) {
                     if(NEED_DEBUG_DETAIL)NSLog(@"success %@",responseObject);
                     
                     __block NSMutableDictionary *dic_userLists=[NSMutableDictionary dictionary];
                     if(IS_NOT_NULL([responseObject objectForKey:@"lists"])){
                         for(NSInteger i=0;i<[responseObject[@"lists"] count];++i){
                             NSDictionary *listDic=responseObject[@"lists"][i];
                             FacehubCloudList *list=[[FacehubCloudList alloc]initWithNetDic:listDic];
                             if(list){
                                 [dic_userLists setObject:list forKey:[list listID]];
                             }
                         }
                         
                         BOOL serverErr=NO;
                         if([responseObject[@"lists"] count]!=[dic_userLists count]){
                             serverErr=YES;
                         }
                         [[FacehubCloudDB defaultDB]insertListsByUserListsDic:dic_userLists
                                                            completionHandler:^(bool success) {
                                                                if(success){
                                                                    if(serverErr){
                                                                        block(FCApiErrCode_ServerDataErr,dic_userLists);
                                                                    }else{
                                                                        block(FCApiErrCode_Success,dic_userLists);
                                                                    }
                                                                }else{
                                                                    block(FCApiErrCode_UpdateDatabaseFailed,dic_userLists);
                                                                }
                                                            }];
                     }else{
                         block(FCApiErrCode_ServerDataErr,nil);
                     }
                 }
                 failure:^(NSURLSessionDataTask * _Nullable task, NSError * _Nonnull error) {
                     if(NEED_DEBUG_DETAIL)NSLog(@"请求发生了错误：%@",error.localizedDescription);
                     if(error.code == NSURLErrorTimedOut){
                         [self getUserListFromLocalWithErrCode:FCApiErrCode_Timeout completionHandler:block];
                     }else{
                         [self getUserListFromLocalWithErrCode:FCApiErrCode_NoNetwork completionHandler:block];
                     }
                 }];
        }else{
            [self getUserListFromLocalWithErrCode:errCode completionHandler:block];
        }
    }];
}

//删除多张表情
+(BOOL)removeEmoticonsByEmoticonsIDArray:(NSArray<NSString *> *)emoticonsIDArray fromUserListByID:(NSString *)userListID
{
    //先写入数据库
    __block BOOL result=NO;
    
    [[FacehubCloudDB defaultDB]removeEmoticonsByContents:emoticonsIDArray
                                              fromListID:userListID
                                       completionHandler:^(bool success) {
                                           result=success;
                                       }];
    
    [self retryAllFailedExcuteWithCompletionHandler:^(int errCode) {
        //再发送请求，如果失败写入重试数据库
        NSString *userID=[FacehubCloudParameter currentUserID];
        NSString *url=[NSString stringWithFormat:@"%@/api/v1/users/%@/lists/%@",HOST,userID,userListID];
        
        NSMutableDictionary *parameterDic=[FacehubCloudParameter networkParameterDic];
        [parameterDic setObject:emoticonsIDArray forKey:@"contents"];
        [parameterDic setObject:@"remove" forKey:@"action"];
        
        AFHTTPSessionManager *manager=[self getSessionManager];
        [manager POST:url
           parameters:parameterDic
             progress:nil
              success:^(NSURLSessionDataTask * _Nonnull task, id  _Nullable responseObject) {
                  if(NEED_DEBUG_DETAIL)NSLog(@"success %@",responseObject);
              }
              failure:^(NSURLSessionDataTask * _Nullable task, NSError * _Nonnull error) {
                  if(NEED_DEBUG_DETAIL)NSLog(@"请求发生了错误：%@",error.localizedDescription);
                  NSString *content=[emoticonsIDArray componentsJoinedByString:@","];
                  [[FacehubCloudDB defaultDB]insertIntoRetryTableWithType:@"removeEmoticon" content:content listID:userListID];
              }];
    }];
    
    return result;
}

//删除单张表情
+(BOOL)removeEmoticonByID:(NSString *)emoticonID fromUserListByID:(NSString *)userListID
{
    //先写入数据库
    __block BOOL result=NO;
    
    NSArray *emoticonsIDArray=@[emoticonID];
    [[FacehubCloudDB defaultDB]removeEmoticonsByContents:emoticonsIDArray
                                              fromListID:userListID
                                       completionHandler:^(bool success) {
                                           result=success;
                                       }];
    
    //再发送请求，如果失败写入重试数据库
    NSString *userID=[FacehubCloudParameter currentUserID];
    NSString *url=[NSString stringWithFormat:@"%@/api/v1/users/%@/lists/%@",HOST,userID,userListID];
    
    NSMutableDictionary *parameterDic=[FacehubCloudParameter networkParameterDic];
    [parameterDic setObject:emoticonsIDArray forKey:@"contents"];
    [parameterDic setObject:@"remove" forKey:@"action"];
    
    [self retryAllFailedExcuteWithCompletionHandler:^(int errCode) {
        AFHTTPSessionManager *manager=[self getSessionManager];
        [manager POST:url
           parameters:parameterDic
             progress:nil
              success:^(NSURLSessionDataTask * _Nonnull task, id  _Nullable responseObject) {
                  if(NEED_DEBUG_DETAIL)NSLog(@"success %@",responseObject);
              }
              failure:^(NSURLSessionDataTask * _Nullable task, NSError * _Nonnull error) {
                  if(NEED_DEBUG_DETAIL)NSLog(@"请求发生了错误：%@",error.localizedDescription);
                  [[FacehubCloudDB defaultDB]insertIntoRetryTableWithType:@"removeEmoticon" content:emoticonID listID:userListID];
              }];
    }];
    
    return result;
}

//新建列表
+(void)createUserListByName:(NSString *)name completionHandler:(ec_new_user_list_block_t)block
{
    NSString *userID=[FacehubCloudParameter currentUserID];
    NSString *url=[NSString stringWithFormat:@"%@/api/v1/users/%@/lists",HOST,userID];
    
    NSMutableDictionary *parameterDic=[FacehubCloudParameter networkParameterDic];
    [parameterDic setObject:name forKey:@"name"];
    
    [self retryAllFailedExcuteWithCompletionHandler:^(int errCode) {
        AFHTTPSessionManager *manager=[self getSessionManager];
        [manager POST:url
           parameters:parameterDic
             progress:nil
              success:^(NSURLSessionDataTask * _Nonnull task, id  _Nullable responseObject) {
                  if(NEED_DEBUG_DETAIL)NSLog(@"success %@",responseObject);
                  
                  if(IS_NOT_NULL([responseObject objectForKey:@"list"])){
                      __block FacehubCloudList *list=[[FacehubCloudList alloc]initWithNetDic:responseObject[@"list"]];
                      if(list){
                          [[FacehubCloudDB defaultDB]createEmptyList:list
                                                   completionHandler:^(bool success) {
                                                       if(success){
                                                           block(FCApiErrCode_Success,list);
                                                       }else{
                                                           block(FCApiErrCode_UpdateDatabaseFailed,list);
                                                       }
                                                   }];
                      }else{
                          block(FCApiErrCode_ServerDataErr,nil);
                      }
                  }else{
                      block(FCApiErrCode_ServerDataErr,nil);
                  }
              }
              failure:^(NSURLSessionDataTask * _Nullable task, NSError * _Nonnull error) {
                  if(NEED_DEBUG_DETAIL)NSLog(@"请求发生了错误：%@",error.localizedDescription);
                  if(error.code == NSURLErrorTimedOut){
                      block(FCApiErrCode_Timeout,nil);
                  }else{
                      block(FCApiErrCode_NoNetwork,nil);
                  }
              }];
    }];
}

//重命名列表
+(void)renameUserListByID:(NSString *)userListID withName:(NSString *)name completionHandler:(fca_completion_block_t)block
{
    [self retryAllFailedExcuteWithCompletionHandler:^(int errCode) {
        NSString *userID=[FacehubCloudParameter currentUserID];
        NSString *url=[NSString stringWithFormat:@"%@/api/v1/users/%@/lists/%@",HOST,userID,name];
        
        NSMutableDictionary *parameterDic=[FacehubCloudParameter networkParameterDic];
        [parameterDic setObject:name forKey:@"name"];
        [parameterDic setObject:@"rename" forKey:@"action"];
        
        AFHTTPSessionManager *manager=[self getSessionManager];
        [manager POST:url
           parameters:parameterDic
             progress:nil
              success:^(NSURLSessionDataTask * _Nonnull task, id  _Nullable responseObject) {
                  if(NEED_DEBUG_DETAIL)NSLog(@"success %@",responseObject);
                  
                  if(IS_NOT_NULL([responseObject objectForKey:@"list"])){
                      FacehubCloudList *list=[[FacehubCloudList alloc]initWithNetDic:responseObject[@"list"]];
                      if(list){
                          [[FacehubCloudDB defaultDB]updateNameOfList:list
                                                    completionHandler:^(bool success) {
                                                        if(success){
                                                            block(FCApiErrCode_Success);
                                                        }else{
                                                            block(FCApiErrCode_UpdateDatabaseFailed);
                                                        }
                                                    }];
                      }else{
                          block(FCApiErrCode_ServerDataErr);
                      }
                  }else{
                      block(FCApiErrCode_ServerDataErr);
                  }
              }
              failure:^(NSURLSessionDataTask * _Nullable task, NSError * _Nonnull error) {
                  if(NEED_DEBUG_DETAIL)NSLog(@"请求发生了错误：%@",error.localizedDescription);
                  if(error.code == NSURLErrorTimedOut){
                      block(FCApiErrCode_Timeout);
                  }else{
                      block(FCApiErrCode_NoNetwork);
                  }
              }];
    }];
}

//删除列表
+(BOOL)removeUserListByID:(NSString *)userListID
{
    //先写入数据库
    __block BOOL result=NO;

    [[FacehubCloudDB defaultDB]removeListByID:userListID
                            completionHandler:^(bool success) {
                                result=success;
                            }];
    
    [self retryAllFailedExcuteWithCompletionHandler:^(int errCode) {
        //再发送请求，如果失败则写入重试数据库
        NSString *userID=[FacehubCloudParameter currentUserID];
        NSString *url=[NSString stringWithFormat:@"%@/api/v1/users/%@/lists",HOST,userID];
        
        NSDictionary *parameterDic=[FacehubCloudParameter networkParameterDic];
        
        AFHTTPSessionManager *manager=[self getSessionManager];
        [manager DELETE:url
             parameters:parameterDic
                success:^(NSURLSessionDataTask * _Nonnull task, id  _Nullable responseObject) {
                    if(NEED_DEBUG_DETAIL)NSLog(@"success %@",responseObject);
                }
                failure:^(NSURLSessionDataTask * _Nullable task, NSError * _Nonnull error) {
                    if(NEED_DEBUG_DETAIL)NSLog(@"请求发生了错误：%@",error.localizedDescription);
                    [[FacehubCloudDB defaultDB]insertIntoRetryTableWithType:@"removeList" content:userListID listID:userListID];
                }];
    }];

    return result;
}

//移动表情到另一个列表
+(void)moveEmoticonByID:(NSString *)emoticonID fromUserListByID:(NSString *)sourceID toUserListByID:(NSString *)targetID completionHandler:(fca_completion_block_t)block
{
    //先添加，如果添加成功，那么在数据库里删除，如果添加失败，那么就返回失败。
    [self collectEmoticonByID:emoticonID toUserListByID:targetID completionHandler:^(int errCode) {
        block(errCode);
        if(errCode == FCApiErrCode_Success){
            [self removeEmoticonByID:emoticonID fromUserListByID:sourceID];
        }
    }];
}

//创建用户
//2016.1.29 弃用，不提供这样的接口。移除原因:采用token的形式来验证用户，注册用户应当在开发者服务器进行  故移除
//+(void)createUserByAppID:(NSString *)appID completionHandler:(ec_regist_user_block_t)block
//{
//    NSString *url=[NSString stringWithFormat:@"%@/api/v1/users/",HOST];
//    
//    NSDictionary *parameterDic=@{@"app_id":appID};
//    AFHTTPSessionManager *manager=[self getSessionManager];
//    [manager POST:url
//       parameters:parameterDic
//          success:^(NSURLSessionDataTask *task, id responseObject) {
//              if(NEED_DEBUG_DETAIL)NSLog(@"success %@",responseObject);
//              
//              //生成user对象，存入数据库，然后自动登录
//              NSDictionary *userDic=responseObject[@"user"];
//              FacehubCloudUser *user=[[FacehubCloudUser alloc]initWithDic:userDic];
//              [[FacehubCloudDB defaultDB]insertUser:user];
//              [FacehubCloudParameter loginUser:user];
//              
//              block(ECModelErrCode_Success,[user userID]);
//          }
//          failure:^(NSURLSessionDataTask *task, NSError *error) {
//              if(NEED_DEBUG_DETAIL)NSLog(@"failed %@",error);
//          }];
//}

////替换列表内容（改变列表内表情顺序用）
//+(void)replaceByContents:(NSArray *)contentsArray ToList:(FacehubCloudList *)list
//{
//    __block FacehubCloudUser *user=[FacehubCloudParameter currentUser];
//    
//    NSString *url=[NSString stringWithFormat:@"%@/api/v1/users/%@/lists/%@",HOST,[user userID],[list listID]];
//    
//    NSDictionary *parameterDic=@{@"contents":contentsArray,@"action":@"replace"};
//    
//    AFHTTPSessionManager *manager=[self getSessionManager];
//    [manager POST:url
//       parameters:parameterDic
//          success:^(NSURLSessionDataTask *task, id responseObject) {
//              if(NEED_DEBUG_DETAIL)NSLog(@"success %@",responseObject);
//              
//              //更新list对象(替换contents顺序和修改时间)，存入数据库
//              NSDictionary *listDic=[NSMutableDictionary dictionaryWithDictionary:responseObject[@"list"]];
//              [list updateContentsWithDic:listDic];
//              [[FacehubCloudDB defaultDB]updateList:list ByReplaceContentsArray:contentsArray];
//              [[FacehubCloudDB defaultDB]updateListTime:list];
//              
//              //更新用户信息(修改时间),同时写入数据库
//              NSDictionary *userDic=responseObject[@"user"];
//              [user updateWithDic:userDic];
//              [[FacehubCloudDB defaultDB]updateUserTime:user];
//          }
//          failure:^(NSURLSessionDataTask *task, NSError *error) {
//              if(NEED_DEBUG_DETAIL)NSLog(@"failed %@",error);
//          }];
//}
//

#pragma mark - 公用函数

+(AFHTTPSessionManager *)getSessionManager
{
    AFHTTPSessionManager *manager=[AFHTTPSessionManager manager];
    manager.responseSerializer=[AFJSONResponseSerializer serializer];
    manager.requestSerializer=[AFJSONRequestSerializer serializer];
    manager.requestSerializer.timeoutInterval=timeoutSeconds;
    return manager;
}

+(NSString*)getEmoticonPathWithName:(NSString*)emoticonName
{
    NSString *emoticonDocumentPath = [self getDocumentPathByDocumentName:@"Emoticons"];
    
    [self createIfNotExistDocumentPath:emoticonDocumentPath];
    
    NSString *emoticonPath=[NSString stringWithFormat:@"%@/%@", emoticonDocumentPath, emoticonName];
    return emoticonPath;
}

//todo 检测是否能正常运行，以及创建失败后是否能正常返回错误
+(NSString *)getDocumentPathByDocumentName:(NSString *)documentName
{
    NSString *docPath=[FacehubCloudParameter docPath];
    NSString *documentsPath=[docPath stringByAppendingPathComponent:documentName];
    
    [self createIfNotExistDocumentPath:documentsPath];
    
    return documentsPath;
}

+(void)createIfNotExistDocumentPath:(NSString *)path
{
    NSFileManager *fileManager = [NSFileManager defaultManager];
    if(![fileManager fileExistsAtPath:path]){
        [fileManager createDirectoryAtPath:path
               withIntermediateDirectories:YES
                                attributes:nil
                                     error:nil];
        
        //設定這個URL指向的資料為不備份到iCloud
        NSError *error;
        [[NSURL fileURLWithPath:path] setResourceValue: [NSNumber numberWithBool: YES]
                                                forKey: NSURLIsExcludedFromBackupKey
                                                 error: &error];
        if(error){
            if(NEED_DEBUG_DETAIL)NSLog(@"创建路径%@失败，错误信息：%@",path,error);
        }
    }
}

//重试函数
+(void)retryAllFailedExcuteWithCompletionHandler:(fca_completion_block_t)block
{
    NSMutableArray *excuteArray=[[FacehubCloudDB defaultDB]getAllRetryExcuteArray];
    if([excuteArray count]>0){
        [self retryByExcuteArray:excuteArray completionHandler:block];
    }else{
        block(FCApiErrCode_Success);
    }
}

+(void)retryByExcuteArray:(NSMutableArray *)excuteArray completionHandler:(fca_completion_block_t)block
{
    NSDictionary *dic=excuteArray[0];
    NSString *type=dic[@"type"];
    NSString *content=dic[@"content"];
    NSString *listID=dic[@"listID"];
    if([type isEqualToString:@"removeEmoticon"]){
        NSArray *emoticonIDArray=[content componentsSeparatedByString:@","];
        [self retryRemoveEmoticonsByEmoticonsIDArray:emoticonIDArray
                                    fromUserListByID:listID
                                   completionHandler:^(int errCode) {
                                       if(errCode==FCApiErrCode_Success){
                                           if([excuteArray count]>1){
                                               [excuteArray removeObjectAtIndex:0];
                                               [self retryByExcuteArray:excuteArray completionHandler:block];
                                           }else{
                                               block(FCApiErrCode_Success);
                                           }
                                       }else{
                                           block(FCApiErrCode_NoNetwork);
                                       }
                                   }];
    }else{
        [self retryRemoveListByID:listID
                completionHandler:^(int errCode) {
                    if(errCode==FCApiErrCode_Success){
                        if([excuteArray count]>1){
                            [excuteArray removeObjectAtIndex:0];
                            [self retryByExcuteArray:excuteArray completionHandler:block];
                        }else{
                            block(FCApiErrCode_Success);
                        }
                    }else{
                        block(FCApiErrCode_NoNetwork);
                    }
                }];
    }
}

//重试删除表情
+(void)retryRemoveEmoticonsByEmoticonsIDArray:(NSArray *)emoticonsIDArray fromUserListByID:(NSString *)userListID completionHandler:(fca_completion_block_t)block
{
    NSString *userID=[FacehubCloudParameter currentUserID];
    NSString *url=[NSString stringWithFormat:@"%@/api/v1/users/%@/lists/%@",HOST,userID,userListID];
    
    NSMutableDictionary *parameterDic=[FacehubCloudParameter networkParameterDic];
    [parameterDic setObject:emoticonsIDArray forKey:@"contents"];
    [parameterDic setObject:@"remove" forKey:@"action"];
    
    AFHTTPSessionManager *manager=[self getSessionManager];
    [manager POST:url
       parameters:parameterDic
         progress:nil
          success:^(NSURLSessionDataTask * _Nonnull task, id  _Nullable responseObject) {
              if(NEED_DEBUG_DETAIL)NSLog(@"success %@",responseObject);
              block(FCApiErrCode_Success);
          }
          failure:^(NSURLSessionDataTask * _Nullable task, NSError * _Nonnull error) {
              if(NEED_DEBUG_DETAIL)NSLog(@"请求发生了错误：%@",error.localizedDescription);
              block(FCApiErrCode_NoNetwork);
          }];
}
//重试删除列表
+(void)retryRemoveListByID:(NSString *)listID completionHandler:(fca_completion_block_t)block
{
    NSString *userID=[FacehubCloudParameter currentUserID];
    NSString *url=[NSString stringWithFormat:@"%@/api/v1/users/%@/lists",HOST,userID];
    
    NSDictionary *parameterDic=[FacehubCloudParameter networkParameterDic];
    
    AFHTTPSessionManager *manager=[self getSessionManager];
    [manager DELETE:url
         parameters:parameterDic
            success:^(NSURLSessionDataTask *task, id responseObject) {
                if(NEED_DEBUG_DETAIL)NSLog(@"success %@",responseObject);
                block(FCApiErrCode_Success);
            }
            failure:^(NSURLSessionDataTask *task, NSError *error) {
                if(NEED_DEBUG_DETAIL)NSLog(@"请求发生了错误：%@",error.localizedDescription);
                block(FCApiErrCode_NoNetwork);
            }];
}

//从本地拿lists
+(void)getUserListFromLocalWithErrCode:(FCApiErrCode)errCode completionHandler:(fca_get_user_list_block_t)block
{
    NSDictionary *dic_userLists=[[FacehubCloudDB defaultDB]getAllListsOfCurrentUser];
    block(errCode,dic_userLists);
}
@end
