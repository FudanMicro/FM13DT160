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

@interface Commons : NSObject

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
