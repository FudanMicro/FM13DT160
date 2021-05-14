//
//  NFCHelper.h
//  FMTemperature
//
//  Created by lubozhi on 2018/7/9.
//  Copyright © 2018年 复旦微电子集团股份科技有限公司. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import <CoreNFC/CoreNFC.h>

typedef NS_ENUM(NSUInteger, LoggingStatus) {
    STATUS_WAITING      = 0,        //等待测温
    STATUS_LOGGING      = 1,        //正在测温
    STATUS_ERROR        = 2,        //测温异常
    STATUS_FINISH       = 3         //测温完成
};

//温度详细信息
@interface TempDetail:NSObject
//序号
@property (nonatomic, copy) NSString *tempID;
//hex温度
@property (nonatomic, copy) NSString *hexTemp;
//十进制温度
@property (nonatomic, copy) NSString *decimalTemp;
//测温时间
@property (nonatomic, copy) NSString *opTime;
@end

//定时测温结果
@interface LoggingMsg : NSObject
//操作结果，YES成功，NO失败
@property (nonatomic, assign) BOOL isSuccess;
@property (nonatomic, assign) LoggingStatus opStatus;
//标签类型:14443,15693
@property (nonatomic, copy) NSString *tagType;
//开启测温时间
@property (nonatomic, assign) NSInteger startTime;
//标签UUID
@property (nonatomic, copy) NSString *uid;
//测温总次数
@property (nonatomic, assign) NSInteger totalCount;
//实际测温次数
@property (nonatomic, assign) NSInteger recordedCount;
//测温延迟时间
@property (nonatomic, assign) NSInteger delayMinutes;
//测温间隔
@property (nonatomic, assign) NSInteger intervalSeconds;
//记录最大温度数据
@property (nonatomic, assign) CGFloat recordedMinimum;
//最小记录温度数据
@property (nonatomic, assign) CGFloat recordedMaximum;
//超下限次数
@property (nonatomic, assign) NSInteger overLowCount;
//超上限次数
@property (nonatomic, assign) NSInteger overHighCount;
//最小温度有效值
@property (nonatomic, assign) CGFloat validMinimum;
//最大温度有效值
@property (nonatomic, assign) CGFloat validMaximum;
//温度数据
@property (nonatomic, strong) NSMutableArray *temperaturesArray;
//报错信息，当isSuccess为false时使用
@property (nonatomic, copy) NSString *message;
@end

//即时测温结果
@interface MeasureMsg : NSObject
//操作结果，YES成功，NO失败
@property (nonatomic, assign) BOOL isSuccess;
//标签类型:14443,15693
@property (nonatomic, copy) NSString *tagType;
//标签UUID
@property (nonatomic, copy) NSString *uid;
//场强
@property (nonatomic, copy) NSString *fieldValue;
//温度
@property (nonatomic, copy) NSString *tempValue;
//电压
@property (nonatomic, copy) NSString *voltageValue;
//是否唤醒，YES，已唤醒，NO，休眠
@property (nonatomic, assign) BOOL isWakeup;
//报错信息，当isSuccess为false时使用
@property (nonatomic, copy) NSString *message;
@end

@interface NFCTagHelper: NSObject

//获取SDK版本号
+ (NSString *)getLibVersion;

+ (instancetype)shareInstance;

//获取UID
- (void)getTagUID:(void (^)(MeasureMsg *resultData))onComplete;
//获取基础测量数据
- (void)getBasicData:(void (^)(MeasureMsg *resultData))onComplete;
//获取是否处于唤醒状态
- (void)checkWakeUp:(void (^)(MeasureMsg *resultData))onComplete;
//休眠
- (void)doSleep:(void (^)(MeasureMsg *resultData))onComplete;
//超高频初始化
- (void)initUHF:(void (^)(MeasureMsg *resultData))onComplete;
//唤醒
- (void)doWakeup:(void (^)(MeasureMsg *resultData))onComplete;
//打开LED灯
- (void)turnOnLED:(void (^)(MeasureMsg *resultData))onComplete;
//关闭LED灯
- (void)turnOffLED:(void (^)(MeasureMsg *resultData))onComplete;
//发送自定义指令
- (void)sendInstruct:(NSString *)instruction onComplete:(void (^)(MeasureMsg *resultData))onComplete;

//开启定时测温
- (void)startLogging:(NSInteger)delayMinutes intervalSeconds:(NSInteger)intervalSeconds loggingCount:(NSInteger)loggingCount minTemperature:(NSInteger)minTemperature maxTemperature:(NSInteger)maxTemperature onComplete:(void (^)(MeasureMsg *resultData))onComplete;
//停止定时测温
- (void)stopLogging:(void (^)(MeasureMsg *resultData))onComplete;
//获取定时测温结果
- (void)getLoggingResult:(void (^)(LoggingMsg *resultData))onComplete;

@end
