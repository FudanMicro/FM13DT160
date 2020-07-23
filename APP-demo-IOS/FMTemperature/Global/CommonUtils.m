#import "CommonUtils.h"

@interface CommonUtils()

@end

@implementation CommonUtils

//确定，单按钮
+ (void)showError:(NSString *)errText controller:(UIViewController *)controller onClick:(void (^)(UIAlertAction *action))block
{
    UIAlertController *alertController = [UIAlertController alertControllerWithTitle:@"提示" message:errText preferredStyle:UIAlertControllerStyleAlert];
    UIAlertAction *okAction = [UIAlertAction actionWithTitle:@"确定" style:UIAlertActionStyleDefault handler:block];
    [alertController addAction:okAction];
    [controller presentViewController:alertController animated:YES completion:nil];
}

//是，否，双按钮
+ (void)showErrorWithTwoBtn:(NSString *)errText controller:(UIViewController *)controller yesTitle:(NSString *)yesTitle onYESClick:(void (^)(UIAlertAction *action))yesBlock noTitle:(NSString *)noTitle onNOClick:(void (^)(UIAlertAction *action))noBlock
{
    UIAlertController *alertController = [UIAlertController alertControllerWithTitle:@"提示" message:errText preferredStyle:UIAlertControllerStyleAlert];
    UIAlertAction *yesAction = [UIAlertAction actionWithTitle:yesTitle style:UIAlertActionStyleDefault handler:yesBlock];
    UIAlertAction *noAction = [UIAlertAction actionWithTitle:noTitle style:UIAlertActionStyleDefault handler:noBlock];
    [alertController addAction:yesAction];
    [alertController addAction:noAction];
    [controller presentViewController:alertController animated:YES completion:nil];
}

//将HexString转化为NSData
+ (NSMutableData *)dataWithHexString:(NSString *)hexString
{
    
    if (!hexString || [hexString length] == 0) {
        return nil;
    }
    
    NSMutableData *hexData = [[NSMutableData alloc] initWithCapacity:8];
    NSRange range;
    if ([hexString length] % 2 == 0) {
        range = NSMakeRange(0, 2);
    } else {
        range = NSMakeRange(0, 1);
    }
    for (NSInteger i = range.location; i < [hexString length]; i += 2) {
        unsigned int anInt;
        NSString *hexCharStr = [hexString substringWithRange:range];
        NSScanner *scanner = [[NSScanner alloc] initWithString:hexCharStr];
        
        [scanner scanHexInt:&anInt];
        NSData *entity = [[NSData alloc] initWithBytes:&anInt length:1];
        [hexData appendData:entity];
        
        range.location += range.length;
        range.length = 2;
    }
    
    //   FMLog(@"hexdata: %@", hexData);
    return hexData;
    
}

//将NSData转化为HexString
+ (NSString *)hexStringWithData:(NSData *)data
{
    if (!data || [data length] == 0) {
        return @"";
    }
    NSMutableString *string = [[NSMutableString alloc] initWithCapacity:[data length]];
    
    [data enumerateByteRangesUsingBlock:^(const void *bytes, NSRange byteRange, BOOL *stop) {
        unsigned char *dataBytes = (unsigned char*)bytes;
        for (NSInteger i = 0; i < byteRange.length; i++) {
            NSString *hexStr = [NSString stringWithFormat:@"%x", (dataBytes[i]) & 0xff];
            if ([hexStr length] == 2) {
                [string appendString:hexStr];
            } else {
                [string appendFormat:@"0%@", hexStr];
            }
        }
    }];
    
    return [string lowercaseString];
}

//十进制转二进制
+ (NSString *)convertBinaryFromDecimal:(NSString *)decimal
{
    NSInteger num = [decimal intValue];
    NSInteger remainder = 0;      //余数
    NSInteger divisor = 0;        //除数
    
    NSString * prepare = @"";
    
    while (true){
        
        remainder = num%2;
        divisor = num/2;
        num = divisor;
        prepare = [prepare stringByAppendingFormat:@"%ld",remainder];
        
        if (divisor == 0){
            
            break;
        }
    }
    
    NSString * result = @"";
    
    for (NSInteger i = prepare.length - 1; i >= 0; i --){
        
        result = [result stringByAppendingFormat:@"%@",
                  [prepare substringWithRange:NSMakeRange(i , 1)]];
    }
    
    return result;
}

//二进制转十进制
+ (NSString *)convertDecimalFromBinary:(NSString *)binary
{
    NSInteger ll = 0 ;
    NSInteger  temp = 0 ;
    for (NSInteger i = 0; i < binary.length; i ++){
        
        temp = [[binary substringWithRange:NSMakeRange(i, 1)] intValue];
        temp = temp * powf(2, binary.length - i - 1);
        ll += temp;
    }
    
    NSString * result = [NSString stringWithFormat:@"%ld",ll];
    
    return result;
}

//十进制转十六进制
+ (NSString *)getHexByDecimal:(NSInteger)decimal digit:(NSInteger)digit {
    NSString *hex =@"";
    NSString *letter;
    NSInteger number;
    BOOL isFinished = NO;
    while (!isFinished)
    {
        number = decimal % 16;
        decimal = decimal / 16;
        switch (number) {
                
            case 10:
                letter =@"a"; break;
            case 11:
                letter =@"b"; break;
            case 12:
                letter =@"c"; break;
            case 13:
                letter =@"d"; break;
            case 14:
                letter =@"e"; break;
            case 15:
                letter =@"f"; break;
            default:
                letter = [NSString stringWithFormat:@"%ld", number];
        }
        hex = [letter stringByAppendingString:hex];
        if (decimal == 0) {
            isFinished = YES;
        }
    }
    NSInteger fillCount = digit - hex.length;
    if(fillCount>0){
        for(int i=0;i<fillCount;i++){
            hex = [NSString stringWithFormat:@"0%@", hex];
        }
    }
    return hex;
}

//十六进制转十进制
+ (NSInteger)getDecimalByHex:(NSString *)hexString{
    const char *hexChar = [hexString cStringUsingEncoding:NSUTF8StringEncoding];
    int hexNumber;
    sscanf(hexChar, "%x", &hexNumber);
    return (NSInteger)hexNumber;
}

//大端与小端互转，仅限两字节
+(NSString *)revert2BytesHexString:(NSString *)hexSrc
{
    if(hexSrc.length == 4){
        return [NSString stringWithFormat:@"%@%@", [hexSrc substringWithRange:NSMakeRange(2,2)], [hexSrc substringWithRange:NSMakeRange(0,2)]];
    }
    else{
        return hexSrc;
    }
}

//将16进制char转换为NSString
+(NSString *)convertDecimalFromHexChar:(unichar)hexChar
{
    switch (hexChar) {
        case '0':
            return @"0";
        case '1':
            return @"1";
        case '2':
            return @"2";
        case '3':
            return @"3";
        case '4':
            return @"4";
        case '5':
            return @"5";
        case '6':
            return @"6";
        case '7':
            return @"7";
        case '8':
            return @"8";
        case '9':
            return @"9";
        case 'a':
            return @"10";
        case 'b':
            return @"11";
        case 'c':
            return @"12";
        case 'd':
            return @"13";
        case 'e':
            return @"14";
        case 'f':
            return @"15";
        default:
            return @"0";
    }
}

//获取当前时间戳，单位秒
+(NSString *)getNowTimeTimestamp
{
    NSDateFormatter *formatter = [[NSDateFormatter alloc] init] ;
    [formatter setDateStyle:NSDateFormatterMediumStyle];
    [formatter setTimeStyle:NSDateFormatterShortStyle];
    //设置你想要的格式,hh与HH的区别:分别表示12小时制,24小时制
    [formatter setDateFormat:@"YYYY-MM-dd HH:mm:ss"];
    //设置时区,这个对于时间的处理有时很重要
    NSTimeZone* timeZone = [NSTimeZone timeZoneWithName:@"Asia/Beijing"];
    [formatter setTimeZone:timeZone];
    //现在时间,你可以输出来看下是什么格式
    NSDate *datenow = [NSDate date];
    NSString *timeSp = [NSString stringWithFormat:@"%ld", (long)[datenow timeIntervalSince1970]];
    return timeSp;
}

//将某个时间转化成 时间戳
+(NSInteger)timeSwitchTimestamp:(NSString *)formatTime andFormatter:(NSString *)format
{
    NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
    [formatter setDateStyle:NSDateFormatterMediumStyle];
    [formatter setTimeStyle:NSDateFormatterShortStyle];
    [formatter setDateFormat:format];
    NSTimeZone* timeZone = [NSTimeZone timeZoneWithName:@"Asia/Beijing"];
    [formatter setTimeZone:timeZone];
    NSDate* date = [formatter dateFromString:formatTime];
    //时间转时间戳的方法:
    NSInteger timeSp = [[NSNumber numberWithDouble:[date timeIntervalSince1970]] integerValue];
    //时间戳的值
    return timeSp;
}

//将某个时间戳转化成 时间
+(NSString *)timestampSwitchTime:(NSInteger)timestamp andFormatter:(NSString *)format
{
    NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
    [formatter setDateStyle:NSDateFormatterMediumStyle];
    [formatter setTimeStyle:NSDateFormatterShortStyle];
    [formatter setDateFormat:format];
    NSTimeZone *timeZone = [NSTimeZone timeZoneWithName:@"Asia/Beijing"];
    [formatter setTimeZone:timeZone];
    NSDate *confromTimesp = [NSDate dateWithTimeIntervalSince1970:timestamp];
    NSString *confromTimespStr = [formatter stringFromDate:confromTimesp];
    return confromTimespStr;
}


//读取定时测温配置
+(LoggingInfo *)getLoggingInfo
{
    NSString *savePath = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) objectAtIndex:0];
    NSString *saveFile = [savePath stringByAppendingPathComponent:@"loggingConfig.dat"];
    
    NSFileManager *fileManager=[NSFileManager defaultManager];
    LoggingInfo* myInfo = nil;
    if([fileManager fileExistsAtPath:saveFile]){
        NSLog(@"配置文件已存在");
        NSMutableDictionary *dict = [NSKeyedUnarchiver unarchiveObjectWithFile:saveFile];
        if(dict.count>0){
            myInfo = [[LoggingInfo alloc] init];
            myInfo.delayMinutes = [dict objectForKey:@"delayMinutes"];
            myInfo.intervalSeconds = [dict objectForKey:@"intervalSeconds"];
            myInfo.loggingCount = [dict objectForKey:@"loggingCount"];
            myInfo.minTemperature = [dict objectForKey:@"minTemperature"];
            myInfo.maxTemperature = [dict objectForKey:@"maxTemperature"];
        }
    }
    else{
        NSLog(@"配置文件不存在");
    }
    if(!myInfo){
        myInfo = [[LoggingInfo alloc] init];
        myInfo.delayMinutes = [NSString stringWithFormat:@"%d", TAG_DELAY_MINUTES];
        myInfo.intervalSeconds = [NSString stringWithFormat:@"%d", TAG_INTERVAL_SECONDS];
        myInfo.loggingCount = [NSString stringWithFormat:@"%d", TAG_LOGGING_COUNT];
        myInfo.minTemperature = [NSString stringWithFormat:@"%d", TAG_MIN_TEMPERATURE];
        myInfo.maxTemperature = [NSString stringWithFormat:@"%d", TAG_MAX_TEMPERATURE];
    }
    return myInfo;
}

//保存定时测温配置
+(void)saveLoggingInfo:(LoggingInfo*)myInfo
{
    NSMutableDictionary *dict=[[NSMutableDictionary alloc] init];
    NSString *savePath = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) objectAtIndex:0];
    NSString *saveFile = [savePath stringByAppendingPathComponent:@"loggingConfig.dat"];

    if(myInfo.delayMinutes)
        [dict setValue:myInfo.delayMinutes forKey:@"delayMinutes"];
    if(myInfo.intervalSeconds)
        [dict setValue:myInfo.intervalSeconds forKey:@"intervalSeconds"];
    if(myInfo.loggingCount)
        [dict setValue:myInfo.loggingCount forKey:@"loggingCount"];
    if(myInfo.minTemperature)
        [dict setValue:myInfo.minTemperature forKey:@"minTemperature"];
    if (myInfo.maxTemperature)
        [dict setValue:myInfo.maxTemperature forKey:@"maxTemperature"];
    [NSKeyedArchiver archiveRootObject:dict toFile:saveFile];
}

//获取array中与text相等的元素index
+(NSInteger)getIndexFromArray:(NSArray *)array text:(NSString *)text{
    NSInteger result = 0;
    NSString *itemStr;
    for(int i=0;i<array.count;i++){
        itemStr = [array objectAtIndex:i];
        if([itemStr isEqualToString:text]){
            result = i;
        }
    }
    return result;
}

@end

@implementation LoggingInfo
@end

@implementation LoggingMsg
@end

@implementation MeasureMsg
@end

@implementation TempDetail
@end
