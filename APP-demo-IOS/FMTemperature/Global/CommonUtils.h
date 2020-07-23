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

//定时测温配置信息
@interface LoggingInfo : NSObject
//延迟测温时间，单位 分
@property (nonatomic,copy) NSString *delayMinutes;
//测温间隔时间，单位 秒
@property (nonatomic,copy) NSString *intervalSeconds;
//测温次数
@property (nonatomic,copy) NSString *loggingCount;
//最小测温范围
@property (nonatomic,copy) NSString *minTemperature;
//最大测温范围
@property (nonatomic,copy) NSString *maxTemperature;
@end

//定时测温结果
@interface LoggingMsg : NSObject
@property (nonatomic, assign) LoggingStatus opStatus;
//标签类型:14443,15693
@property (nonatomic, copy) NSString *tagType;
//操作类型
@property (nonatomic, assign) NSInteger opType;
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
@end

//即时测温结果
@interface MeasureMsg : NSObject
//操作结果，YES成功，NO失败
@property (nonatomic, assign) BOOL isSuccess;
//标签类型:14443,15693
@property (nonatomic, copy) NSString *tagType;
//操作类型
@property (nonatomic, assign) NSInteger opType;
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
@end

@interface CommonUtils : NSObject

//UIAlertController提示
//确定，单按钮
+ (void)showError:(NSString *)errText controller:(UIViewController *)controller onClick:(void (^)(UIAlertAction *action))block;

//是，否，双按钮
+ (void)showErrorWithTwoBtn:(NSString *)errText controller:(UIViewController *)controller yesTitle:(NSString *)yesTitle onYESClick:(void (^)(UIAlertAction *action))yesBlock noTitle:(NSString *)noTitle onNOClick:(void (^)(UIAlertAction *action))noBlock;

//将HexString转化为NSData
+ (NSMutableData *)dataWithHexString:(NSString *)hexString;
//将NSData转化为HexString
+ (NSString *)hexStringWithData:(NSData *)data;

//十进制转二进制
+ (NSString *)convertBinaryFromDecimal:(NSString *)decimal;
//二进制转十进制
+ (NSString *)convertDecimalFromBinary:(NSString *)binary;

//十六进制转十进制
+ (NSInteger)getDecimalByHex:(NSString *)hexString;
//十进制转十六进制
+ (NSString *)getHexByDecimal:(NSInteger)decimal digit:(NSInteger)digit;

//大端与小端互转，仅限两字节
+(NSString *)revert2BytesHexString:(NSString *)hexSrc;
//将16进制char转换为NSString
+(NSString *)convertDecimalFromHexChar:(unichar)hexChar;
//获取当前时间戳，单位秒
+(NSString *)getNowTimeTimestamp;
//将某个时间转化成 时间戳
+(NSInteger)timeSwitchTimestamp:(NSString *)formatTime andFormatter:(NSString *)format;
//将某个时间戳转化成 时间
+(NSString *)timestampSwitchTime:(NSInteger)timestamp andFormatter:(NSString *)format;
//读取定时测温配置
+(LoggingInfo *)getLoggingInfo;
//保存定时测温配置
+(void)saveLoggingInfo:(LoggingInfo*)myInfo;
//获取array中与text相等的元素index
+(NSInteger)getIndexFromArray:(NSArray *)array text:(NSString *)text;
@end
