//
//  NFCTagHelper.m
//  FMTemperature
//
//  Created by lubozhi on 2019/7/19.
//  Copyright © 2018年 复旦微电子集团股份科技有限公司. All rights reserved.
//

#import "NFCTagHelper.h"
#import "NFCTagObject.h"

API_AVAILABLE(ios(13.0))
@interface NFCTagHelper()<NFCTagReaderSessionDelegate>

@property (nonatomic, strong) NFCTagReaderSession *session;
@property (nonatomic, strong) NSMutableArray *messagesArrayM;
@property (nonatomic, strong) NSMutableDictionary *hexDic;
//操作类型
@property (nonatomic, assign) OperationType opType;
//延迟测温时间hex，单位分钟
@property (nonatomic, copy) NSString *hexDelayMinutes;
//测温间隔时间hex，单位秒
@property (nonatomic, copy) NSString *hexIntervalSeconds;
//测温次数hex
@property (nonatomic, copy) NSString *hexLoggingCount;
//测温最小值hex
@property (nonatomic, copy) NSString *hexMinTemperature;
@property (nonatomic, copy) NSString *hexBlock9Min;
//测温最大值hex
@property (nonatomic, copy) NSString *hexMaxTemperature;
@property (nonatomic, copy) NSString *hexBlock9Max;
//标签格式 0为默认模式，1为原始数据格式，2为标准数据格式
@property (nonatomic, assign) NSInteger tagFormat;
//vdet_a
@property (nonatomic, assign) CGFloat vDetA;
//vdet_b
@property (nonatomic, assign) CGFloat vDetB;
//vdet_offset
@property (nonatomic, assign) CGFloat vDetOffset;
//自定义指令
@property (nonatomic, copy) NSString *command;
@property (nonatomic, strong) MeasureMsg *mMsg;
@property (nonatomic, strong) LoggingMsg *lMsg;
@property (nonatomic, assign) BOOL isContinueScan;
@property (nonatomic, strong) void(^measureCompleteBlock)(MeasureMsg *);
@property (nonatomic, strong) void(^loggingCompleteBlock)(LoggingMsg *);
@end

@implementation NFCTagHelper

+ (NSString *)getLibVersion{
    return SDK_VERSION;
}

+ (instancetype)shareInstance {
    static dispatch_once_t onceToken;
    static NFCTagHelper *nfcHelper;
    dispatch_once(&onceToken, ^{
        nfcHelper = [[NFCTagHelper alloc] init];
    });
    return nfcHelper;
}

- (instancetype)init {
    if (self = [super init]) {
        NSLog(@"NFC Helper initialized...");
        _messagesArrayM = [NSMutableArray arrayWithCapacity:0];
        _hexDic = [[NSMutableDictionary alloc] initWithCapacity:16];
        [_hexDic setObject:@"0000" forKey:@"0"];
        [_hexDic setObject:@"0001" forKey:@"1"];
        [_hexDic setObject:@"0010" forKey:@"2"];
        [_hexDic setObject:@"0011" forKey:@"3"];
        [_hexDic setObject:@"0100" forKey:@"4"];
        [_hexDic setObject:@"0101" forKey:@"5"];
        [_hexDic setObject:@"0110" forKey:@"6"];
        [_hexDic setObject:@"0111" forKey:@"7"];
        [_hexDic setObject:@"1000" forKey:@"8"];
        [_hexDic setObject:@"1001" forKey:@"9"];
        [_hexDic setObject:@"1010" forKey:@"a"];
        [_hexDic setObject:@"1011" forKey:@"b"];
        [_hexDic setObject:@"1100" forKey:@"c"];
        [_hexDic setObject:@"1101" forKey:@"d"];
        [_hexDic setObject:@"1110" forKey:@"e"];
        [_hexDic setObject:@"1111" forKey:@"f"];
        
        _isContinueScan = NO;
    }
    return self;
}

/**十六进制字符串转二进制*/
- (NSString *)getBinaryByHex:(NSString *)hex {
    NSMutableString *binary = [NSMutableString string];
    for (int i = 0; i < hex.length; i++) {
        NSString *key = [hex substringWithRange:NSMakeRange(i, 1)];
        key = key.lowercaseString;
        NSString *binaryStr = _hexDic[key];
        [binary appendString:[NSString stringWithFormat:@"%@",binaryStr]];
    }
    return binary;
}

//将执行结果解析为二进制字符串
- (NSString *)formatFMBinaryString:(NSString *)hex {
    NSString *result = [CommonUtils revert2BytesHexString:hex];
    result = [self getBinaryByHex:result];
//    NSLog(@"FMBinary:%@", result);
    return result;
}

- (void)settingHexDelayMinutes:(NSInteger)value{
    _hexDelayMinutes = [CommonUtils getHexByDecimal:value digit:2];
}

- (void)settingHexIntervalSeconds:(NSInteger)value{
    _hexIntervalSeconds = [CommonUtils getHexByDecimal:value digit:4];
}

- (void)settingHexLoggingCount:(NSInteger)value{
    _hexLoggingCount = [CommonUtils revert2BytesHexString:[CommonUtils getHexByDecimal:value digit:4]];
}

- (void)settingHexMinTemperature:(NSInteger)value{
    NSInteger tmpValue = value;
    if(tmpValue < 0){
        tmpValue = tmpValue+256;
    }
    _hexMinTemperature = [CommonUtils revert2BytesHexString:[self getHexFromTemperature:tmpValue]];
    _hexBlock9Min = [CommonUtils getHexByDecimal:tmpValue digit:2];
}

- (void)settingHexMaxTemperature:(NSInteger)value{
    NSInteger tmpValue = value;
    if(tmpValue < 0){
        tmpValue = tmpValue+256;
    }
    _hexMaxTemperature = [CommonUtils revert2BytesHexString:[self getHexFromTemperature:tmpValue]];
    _hexBlock9Max = [CommonUtils getHexByDecimal:tmpValue digit:2];
}

//将整型温度转换为hexstring
- (NSString *)getHexFromTemperature:(NSInteger)value{
    NSString *result;
    if(value<0){
        result = [CommonUtils getHexByDecimal:(value*4+0x400) digit:4];
    }
    else{
        result = [CommonUtils getHexByDecimal:value*4 digit:4];
    }
    return result;
}

//将hexstring转换为浮点温度
- (CGFloat)getTemperatureFromHex:(NSString *)hex{
    CGFloat fValue = 0;
    NSString *result = [self formatFMBinaryString:hex];
    NSString *symbol = [result substringWithRange:NSMakeRange(6, 1)];
        result = [result substringFromIndex:7];
    result = [CommonUtils convertDecimalFromBinary:result];
    NSInteger intValue = [result integerValue];
    //bit9=0,正数,bit0,bit1为小数; bit9=1,负数,补码形式,bit0,bit1为小数;
    if([symbol isEqualToString:@"0"]){
        fValue = intValue / 4.0;
//        NSLog(@"温度为%.2f", fValue);
    }
    else{
        intValue = 0x1ff-(intValue-1);
        fValue = intValue / 4.0;
        fValue = -1*fValue;//转换为负数
//        NSLog(@"温度为%.2f", fValue);
    }
    return fValue;
}

//转换原始单次测温数据
- (CGFloat)getOrignalOnceFromHex:(NSString *)hex{
    CGFloat fValue = 0;
    NSString *result = [self formatFMBinaryString:hex];
    NSString *symbol = [result substringWithRange:NSMakeRange(6, 1)];
        result = [result substringFromIndex:7];
    result = [CommonUtils convertDecimalFromBinary:result];
    NSInteger intValue = [result integerValue];
    //bit9=0,正数,bit0,bit1为小数; bit9=1,负数,补码形式,bit0,bit1,bit2为小数;
    if([symbol isEqualToString:@"0"]){
        fValue = intValue / 8.0;
//        NSLog(@"温度为%.2f", fValue);
    }
    else{
        intValue = 0x1ff-(intValue-1);
        fValue = intValue / 8.0;
        fValue = -1*fValue;//转换为负数
//        NSLog(@"温度为%.2f", fValue);
    }
    return fValue;
}

//转换原始明细数据
- (CGFloat)getOrignalDetailFromHex:(NSString *)hex{
    CGFloat fValue = 0;
    NSString *result = [self formatFMBinaryString:hex];
    result = [result substringFromIndex:4];
    result = [CommonUtils convertDecimalFromBinary:result];
    NSInteger intValue = [result integerValue];
    fValue = _vDetA*intValue/8192.0+_vDetB+_vDetOffset;
    return fValue;
}

//det_a,det_b,det_offset转换方法
- (CGFloat)getDetDataFromHex:(NSString *)hex{
    CGFloat fValue = 0;
    NSString *result = [self formatFMBinaryString:hex];
    NSString *symbol = [result substringWithRange:NSMakeRange(0, 1)];
    result = [CommonUtils convertDecimalFromBinary:result];
    NSInteger intValue = [result integerValue];
    //symbol bit15=0,正数, bit15-bit4为整数, bit0-bit3为小数;
    if([symbol isEqualToString:@"0"]){
        fValue = intValue / 16.0;
        NSLog(@"转换温度为%.3f", fValue);
    }
    else{
        intValue = intValue-0x10000;
        fValue = intValue / 16.0;
        NSLog(@"转换温度为%.3f", fValue);
    }
    return fValue;
}

//发送自定义指令
- (void)sendInstruct:(NSString *)instruction onComplete:(void (^)(MeasureMsg *resultData))onComplete{
    _loggingCompleteBlock = nil;
    _lMsg = nil;
    
    _opType = CustomType;
    _measureCompleteBlock = onComplete;
    _mMsg = [[MeasureMsg alloc] init];
    _mMsg.isSuccess = NO;
    _mMsg.tagType = @"";
    _mMsg.uid = @"";
    _mMsg.isWakeup = NO;
    _command = instruction;
    
    if (@available(iOS 13.0, *)) {
        [self readNFCTag];
    }
    else{
        [self showMsgOnSession:@"nfc reader only support on iOS13 and above"];
    }
}

//获取UID
- (void)getTagUID:(void (^)(MeasureMsg *resultData))onComplete{
    _loggingCompleteBlock = nil;
    _lMsg = nil;
    
    _opType = GetUIDType;
    _measureCompleteBlock = onComplete;
    _mMsg = [[MeasureMsg alloc] init];
    _mMsg.isSuccess = NO;
    _mMsg.tagType = @"";
    _mMsg.uid = @"";
    _mMsg.isWakeup = NO;
    
    if (@available(iOS 13.0, *)) {
        [self readNFCTag];
    }
    else{
        [self showMsgOnSession:@"nfc reader only support on iOS13 and above"];
    }
}

//获取基础测量数据
- (void)getBasicData:(void (^)(MeasureMsg *resultData))onComplete{
    _loggingCompleteBlock = nil;
    _lMsg = nil;
    
    _opType = BasicType;
    _measureCompleteBlock = onComplete;
    _mMsg = [[MeasureMsg alloc] init];
    _mMsg.isSuccess = NO;
    _mMsg.tagType = @"";
    _mMsg.uid = @"";
    _mMsg.isWakeup = NO;
    
    if (@available(iOS 13.0, *)) {
        [self readNFCTag];
    }
    else{
        [self showMsgOnSession:@"nfc reader only support on iOS13 and above"];
    }
}

//获取是否处于唤醒状态
- (void)checkWakeUp:(void (^)(MeasureMsg *resultData))onComplete{
    _loggingCompleteBlock = nil;
    _lMsg = nil;
    
    _opType = IfWakeupType;
    _measureCompleteBlock = onComplete;
    _mMsg = [[MeasureMsg alloc] init];
    _mMsg.isSuccess = NO;
    _mMsg.tagType = @"";
    _mMsg.uid = @"";
    _mMsg.isWakeup = NO;
    
    if (@available(iOS 13.0, *)) {
        [self readNFCTag];
    }
    else{
        [self showMsgOnSession:@"nfc reader only support on iOS13 and above"];
    }
}

//休眠
- (void)doSleep:(void (^)(MeasureMsg *resultData))onComplete{
    _loggingCompleteBlock = nil;
    _lMsg = nil;
    
    _opType = SleepType;
    _measureCompleteBlock = onComplete;
    _mMsg = [[MeasureMsg alloc] init];
    _mMsg.isSuccess = NO;
    _mMsg.tagType = @"";
    _mMsg.uid = @"";
    _mMsg.isWakeup = NO;
    
    if (@available(iOS 13.0, *)) {
        [self readNFCTag];
    }
    else{
        [self showMsgOnSession:@"nfc reader only support on iOS13 and above"];
    }
}

//超高频初始化
- (void)initUHF:(void (^)(MeasureMsg *resultData))onComplete{
    _loggingCompleteBlock = nil;
    _lMsg = nil;
    
    _opType = UHFInitType;
    _measureCompleteBlock = onComplete;
    _mMsg = [[MeasureMsg alloc] init];
    _mMsg.isSuccess = NO;
    _mMsg.tagType = @"";
    _mMsg.uid = @"";
    _mMsg.isWakeup = NO;
    
    if (@available(iOS 13.0, *)) {
        [self readNFCTag];
    }
    else{
        [self showMsgOnSession:@"nfc reader only support on iOS13 and above"];
    }
}

//唤醒
- (void)doWakeup:(void (^)(MeasureMsg *resultData))onComplete{
    _loggingCompleteBlock = nil;
    _lMsg = nil;
    
    _opType = WakeupType;
    _measureCompleteBlock = onComplete;
    _mMsg = [[MeasureMsg alloc] init];
    _mMsg.isSuccess = NO;
    _mMsg.tagType = @"";
    _mMsg.uid = @"";
    _mMsg.isWakeup = NO;
    
    if (@available(iOS 13.0, *)) {
        [self readNFCTag];
    }
    else{
        [self showMsgOnSession:@"nfc reader only support on iOS13 and above"];
    }
}

//打开LED灯
- (void)turnOnLED:(void (^)(MeasureMsg *resultData))onComplete{
    _loggingCompleteBlock = nil;
    _lMsg = nil;
    
    _opType = LEDOnType;
    _measureCompleteBlock = onComplete;
    _mMsg = [[MeasureMsg alloc] init];
    _mMsg.isSuccess = NO;
    _mMsg.tagType = @"";
    _mMsg.uid = @"";
    _mMsg.isWakeup = NO;
    
    if (@available(iOS 13.0, *)) {
        [self readNFCTag];
    }
    else{
        [self showMsgOnSession:@"nfc reader only support on iOS13 and above"];
    }
}

//开启定时测温
- (void)startLogging:(NSInteger)delayMinutes intervalSeconds:(NSInteger)intervalSeconds loggingCount:(NSInteger)loggingCount minTemperature:(NSInteger)minTemperature maxTemperature:(NSInteger)maxTemperature onComplete:(void (^)(MeasureMsg *resultData))onComplete{
    _loggingCompleteBlock = nil;
    _lMsg = nil;
    
    _opType = LoggingStartType;
    _measureCompleteBlock = onComplete;
    _mMsg = [[MeasureMsg alloc] init];
    _mMsg.isSuccess = NO;
    _mMsg.tagType = @"";
    _mMsg.uid = @"";
    _mMsg.isWakeup = NO;
    
    [self settingHexDelayMinutes:delayMinutes];
    [self settingHexIntervalSeconds:intervalSeconds];
    [self settingHexLoggingCount:loggingCount];
    [self settingHexMinTemperature:minTemperature];
    [self settingHexMaxTemperature:maxTemperature];
    if (@available(iOS 13.0, *)) {
        [self readNFCTag];
    }
    else{
        [self showMsgOnSession:@"nfc reader only support on iOS13 and above"];
    }
}

//停止定时测温
- (void)stopLogging:(void (^)(MeasureMsg *resultData))onComplete{
    _loggingCompleteBlock = nil;
    _lMsg = nil;
    
    _opType = LoggingStopType;
    _measureCompleteBlock = onComplete;
    _mMsg = [[MeasureMsg alloc] init];
    _mMsg.isSuccess = NO;
    _mMsg.tagType = @"";
    _mMsg.uid = @"";
    _mMsg.isWakeup = NO;
    
    if (@available(iOS 13.0, *)) {
        [self readNFCTag];
    }
    else{
        [self showMsgOnSession:@"nfc reader only support on iOS13 and above"];
    }
}

//获取测温结果
- (void)getLoggingResult:(void (^)(LoggingMsg *resultData))onComplete{
    _measureCompleteBlock = nil;
    _mMsg = nil;
    
    _opType = LoggingResultType;
    _loggingCompleteBlock = onComplete;
    _lMsg = [[LoggingMsg alloc] init];
    _lMsg.isSuccess = NO;
    _lMsg.tagType = @"";
    _lMsg.uid = @"";
    
    if (@available(iOS 13.0, *)) {
        [self readNFCTag];
    }
    else{
        [self showMsgOnSession:@"nfc reader only support on iOS13 and above"];
    }
}

//关闭LED灯
- (void)turnOffLED:(void (^)(MeasureMsg *resultData))onComplete{
    _loggingCompleteBlock = nil;
    _lMsg = nil;
    
    _opType = LEDOffType;
    _measureCompleteBlock = onComplete;
    _mMsg = [[MeasureMsg alloc] init];
    _mMsg.isSuccess = NO;
    _mMsg.tagType = @"";
    _mMsg.uid = @"";
    _mMsg.isWakeup = NO;
    
    if (@available(iOS 13.0, *)) {
        [self readNFCTag];
    }
    else{
        [self showMsgOnSession:@"nfc reader only support on iOS13 and above"];
    }
}

- (void)readNFCTag {
    _tagFormat = 0;
    _vDetA = 0;
    _vDetB = 0;
    _vDetOffset = 0;
    if(!_session){
        _session = [[NFCTagReaderSession alloc] initWithPollingOption:NFCPollingISO14443|NFCPollingISO15693 delegate:self queue:dispatch_get_global_queue(0, 0)];
    }
    [_session beginSession];
}

- (void)removeSession{
    if(_session){
        [_session invalidateSession];
        _session = nil;
    }
}

//连接tag
- (BOOL)connectTag:(id<NFCTag>) tag{
    __block NFCTagReaderSession *blockSession = _session;
    __block BOOL result = NO;
    dispatch_semaphore_t sem = dispatch_semaphore_create(0);
    [blockSession connectToTag:tag completionHandler:^(NSError *connectError){
        if(connectError != nil){
            NSLog(@"connect error:%lu", connectError.code);
        }
        else{
            result = YES;
        }
        dispatch_semaphore_signal(sem);
    }];
    dispatch_semaphore_wait(sem, DISPATCH_TIME_FOREVER);
    return result;
}

//循环获取定时测温结果明细
- (BOOL)getTempDetailA:(NFCTagObject *)tagObj totalSize:(NSInteger)totalSize msg:(LoggingMsg *)msg cmdType:(NSInteger)cmdType
{
    // x 高位 y 低位 ,n 读取大小,pageCount 次数,p 余数
    NSInteger x = 0x10, y = 0x00, n = 0, pageCount = 0, p = 0;
    NSInteger pageSize = 252;
    NSInteger bytesCount = 4;//普通模式4字节，原始数据2字节
    if(_tagFormat==1){
        bytesCount = 2;
    }
    p = totalSize % pageSize;
    pageCount = totalSize / pageSize;
    if (p != 0) {
        pageCount = pageCount + 1;
    }
    NSInteger statusCode = BT_OPERATION_FAILED;
    NSInteger dataCount = 0;
    NSInteger intValue = 0;
    NSString *result, *cmdStr, *xyStr, *nStr, *tempStr;
    CGFloat fValue = 0;
    for (int i = 0; i < pageCount; i++) {
        if (i == 0) {
            y = 0x00;
        } else if (i == 1) {
            y = y + pageSize;
        } else {
            x = x + 0x01;
            y = pageSize - 0x04;
        }
        if (p != 0 && i == pageCount - 1) {
            //b1指令取值最小单位是4字节
            if(p%4 == 0){
                n = p-4;
            }
            else{
                n = p;
            }
            dataCount = p/bytesCount;
        }
        else{
            n = pageSize-4;
            dataCount = pageSize/bytesCount;
        }
        if(n<0){
            n=0;
        }
        xyStr = [NSString stringWithFormat:@"%@%@", [CommonUtils getHexByDecimal:x digit:2], [CommonUtils getHexByDecimal:y digit:2]];
        nStr = [CommonUtils getHexByDecimal:n digit:4];
        cmdStr = [NSString stringWithFormat:GET_TAG_CMD(GET_DETAIL, cmdType), xyStr, nStr];
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:YES];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, (long)statusCode]];
            return NO;
        }
        if([result isEqualToString:FM_DETAIL_END]){
            break;
        }
        TempDetail *item;
        
        for(int j=0;j<dataCount;j++){
            item = [[TempDetail alloc] init];
            tempStr = [result substringWithRange:NSMakeRange(bytesCount*j*2, 4)];
            item.tempID = [NSString stringWithFormat:@"%d",j+1];
            item.hexTemp = tempStr;
            item.fieldFlag = 2;
            if(_tagFormat==1){
                fValue = [self getOrignalDetailFromHex:tempStr];
                tempStr = [NSString stringWithFormat:@"%.3f", fValue];
            }
            else{
                //标准数据模式
                if(_tagFormat==2){
                    NSInteger intConvert = [CommonUtils getDecimalByHex:tempStr];
                    NSLog(@"intConvert:%ld", intConvert);
                    NSInteger intFieldFlag = (intConvert&0x20)>>5;
                    NSLog(@"场强位:%ld", intFieldFlag);
                    item.fieldFlag = intFieldFlag;
                }
                fValue = [self getTemperatureFromHex:tempStr];
                tempStr = [NSString stringWithFormat:@"%.3f", fValue];
            }
            item.decimalTemp = tempStr;
            intValue = msg.startTime + j*msg.intervalSeconds;
            item.opTime = [CommonUtils timestampSwitchTime:intValue andFormatter:@"YYYY-MM-dd HH:mm:ss"];
            [msg.temperaturesArray addObject:item];
        }
    }
    return YES;
}

//-----------------------------------
//NFCTagReaderSessionDelegate
- (void)tagReaderSessionDidBecomeActive:(NFCTagReaderSession *)session API_AVAILABLE(ios(13.0))
{
    NSLog(@"tagReaderSessionDidBecomeActive");
}

- (void)tagReaderSession:(NFCTagReaderSession *)session didDetectTags:(NSArray<__kindof id<NFCTag>> *)tags API_AVAILABLE(ios(13.0))
{
    if(tags.count > 1) {
        [self showMsgOnSession:@"More than 1 tags was found. Please present only 1 tag."];
        return;
    }
    id<NFCTag> tag = tags.firstObject;
    if(tag.type != NFCTagTypeMiFare && tag.type != NFCTagTypeISO15693){
        [self showMsgOnSession:@"NFCType: %lu，not support this protocol"];
        return;
    }

    //NSLog(@"NFCType: %lu，支持NFCMiFareTag协议",(unsigned long)tag.type);
    BOOL isConnected = [self connectTag:tag];
    if(!isConnected){
        [self showMsgOnSession:@"failed to connected tag."];
        return;
    }

    NFCTagObject *tagObj = [[NFCTagObject alloc] initWithTag:tag];
    NSInteger statusCode = BT_OPERATION_FAILED;
    NSString *result, *cmdStr, *symbol, *innerStr;
    NSInteger intValue = 0;
    CGFloat fValue = 0;
    NSInteger cmdType = tagObj.cmdType;
    
    if(_mMsg){
        _mMsg.tagType = tagObj.tagType;
        _mMsg.uid = tagObj.uuid;
    }
    else if(_lMsg){
        _lMsg.temperaturesArray = [[NSMutableArray alloc] init];
        _lMsg.tagType = tagObj.tagType;
        _lMsg.uid = tagObj.uuid;
    }
    if(_opType == GetUIDType){
        //todo nothing
    }
    else if(_opType == BasicType){
        //测场强
        cmdStr = GET_TAG_CMD(FIELD, cmdType);
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, (long)statusCode]];
            return;
        }
        unichar hexChar = [result characterAtIndex:1];
        result = [CommonUtils convertDecimalFromHexChar:hexChar];
        _mMsg.fieldValue = result;
        NSLog(@"场强为:%@", _mMsg.fieldValue);
        
        //单次测温
        cmdStr = GET_TAG_CMD(ONCE_TEMPERATURE, cmdType);
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, (long)statusCode]];
            return;
        }
        if(![result containsString:FM_F_SUCCESS]){
            [self showMsgOnSession:[NSString stringWithFormat:@"temperature start failed,command:%@ failed,result:%@", cmdStr, result]];
            return;
        }
        
        [NSThread sleepForTimeInterval:0.32];
        
        cmdStr = GET_TAG_CMD(ONCE_RESULT, cmdType);
        NSString *onceResult = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, (long)statusCode]];
            return;
        }
        
        _tagFormat = 0;//默认普通数据格式
        cmdStr = GET_TAG_CMD(GET_USER_CFG, cmdType);
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:YES];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, (long)statusCode]];
            return;
        }
        NSString *formatType = [self getBinaryByHex:result];
        formatType = [formatType substringWithRange:NSMakeRange(3, 3)];
        NSLog(@"温度格式为:%@",formatType);
        
        if([formatType isEqualToString:@"111"]){
            _tagFormat = 1;//原始数据格式
            cmdStr = GET_TAG_CMD(GET_DET, cmdType);
            result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:YES];
            if(statusCode != BT_SUCCESS){
                [self showMsgOnSession:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, (long)statusCode]];
                return;
            }
            NSLog(@"det数据:%@", result);
            NSString *offsetStr = [result substringWithRange:NSMakeRange(4,4)];
            NSString *detAStr = [result substringWithRange:NSMakeRange(8,4)];
            NSString *detBStr = [result substringWithRange:NSMakeRange(12,4)];
            //转换为原始数据，高低位字节互换
            _vDetOffset = [self getDetDataFromHex:offsetStr];
            _vDetA = [self getDetDataFromHex:detAStr];
            _vDetB = [self getDetDataFromHex:detBStr];
            NSLog(@"detA:%.3f, detB:%.3f", _vDetA, _vDetB);
            NSLog(@"offset:%.3f", _vDetOffset);
        }
        else if([formatType isEqualToString:@"011"]){
            _tagFormat = 2;//标准数据格式
//            NSLog(@"标准数据格式");
        }
        
        //计算温度
        if(_tagFormat==1){
            fValue = [self getOrignalOnceFromHex:onceResult];
            _mMsg.tempValue = [NSString stringWithFormat:@"%.3f °C", fValue];
        }
        else{
            fValue = [self getTemperatureFromHex:onceResult];
            _mMsg.tempValue = [NSString stringWithFormat:@"%.3f °C", fValue];
        }
        
        //测电压
        //唤醒指令有bug，需要去掉
//        cmdStr = GET_TAG_CMD(WAKEUP, cmdType);
//        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
//        if(statusCode != BT_SUCCESS){
//            [self showMsgOnSession: session message:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode]];
//            return;
//        }
        
//        cmdStr = GET_TAG_CMD(PDSTATUS, cmdType);
//        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
//        if(statusCode != BT_SUCCESS){
//            [self showMsgOnSession: session message:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode]];
//            return;
//        }
//        if(![result containsString:FM_OUT_PD]){
//            [self showMsgOnSession: session message:[NSString stringWithFormat:@"wake up failed,code:%ld", statusCode]];
//            return;
//        }
        
        //判断是否有接电池
        cmdStr = GET_TAG_CMD(CHECK_STATUS, cmdType);
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode]];
            return;
        }
        result = [self formatFMBinaryString:result];
        NSString *powerFlag = [result substringWithRange:NSMakeRange(7, 1)];
        if([powerFlag isEqualToString:@"0"]){
            _mMsg.voltageValue = @"out of power";
        }
        else{
            cmdStr = GET_TAG_CMD(VOLTAGE_1, cmdType);
            result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
            if(statusCode != BT_SUCCESS){
                [self showMsgOnSession:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode]];
                return;
            }
            
            cmdStr = GET_TAG_CMD(VOLTAGE_2, cmdType);
            result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
            if(statusCode != BT_SUCCESS){
                [self showMsgOnSession:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode]];
                return;
            }
            if(![result containsString:FM_F_SUCCESS]){
                [self showMsgOnSession:[NSString stringWithFormat:@"voltage start failed,command:%@ failed,result:%@", cmdStr, result]];
                return;
            }
            [NSThread sleepForTimeInterval:0.32];
            
            cmdStr = GET_TAG_CMD(VOLTAGE_3, cmdType);
            result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
            if(statusCode != BT_SUCCESS){
                [self showMsgOnSession:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode]];
                return;
            }
            //计算电压
            result = [self formatFMBinaryString:result];
            result = [result substringFromIndex:3];
            result = [CommonUtils convertDecimalFromBinary:result];
            intValue = [result integerValue];
            fValue = intValue / 8192.0 * 2.5;
            _mMsg.voltageValue = [NSString stringWithFormat:@"%.2f V", fValue];
            NSLog(@"电压为:%@", _mMsg.voltageValue);
            
            cmdStr = GET_TAG_CMD(VOLTAGE_4, cmdType);
            result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
            if(statusCode != BT_SUCCESS){
                [self showMsgOnSession:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode]];
                return;
            }
        }
    }
    else if(_opType == LoggingStartType){
        //检查定时测温状态
        cmdStr = GET_TAG_CMD(CHECK_STATUS, cmdType);
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode]];
            return;
        }
        result = [self formatFMBinaryString:result];
        symbol = [result substringWithRange:NSMakeRange(3, 1)];
        NSString *powerFlag = [result substringWithRange:NSMakeRange(7, 1)];
        if([powerFlag isEqualToString:@"0"]){
            [self showMsgOnSession:@"this tag is out of power"];
            return;
        }
        //bit12 = 1表示当前处于定时测温状态
        if([symbol isEqualToString:@"1"]){
            [self showMsgOnSession:@"this tag is logging, please wait a moment"];
            return;
        }
        
        //唤醒
        cmdStr = GET_TAG_CMD(WAKEUP, cmdType);
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode]];
            return;
        }
        cmdStr = GET_TAG_CMD(PDSTATUS, cmdType);
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode]];
            return;
        }
        if(![result containsString:FM_OUT_PD]){
            [self showMsgOnSession:[NSString stringWithFormat:@"wake up failed,code:%ld", statusCode]];
            return;
        }
        
        //设置延迟测温时间
        cmdStr = [NSString stringWithFormat: GET_TAG_CMD(SET_DELAY, cmdType), _hexDelayMinutes];
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode]];
            return;
        }
        cmdStr = GET_TAG_CMD(READ_DELAY, cmdType);
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode]];
            return;
        }
        //返回结果第一字节（低位）为延迟时间
        result = [result substringWithRange:NSMakeRange(0, 2)];
        if(![result isEqualToString:_hexDelayMinutes]){
            [self showMsgOnSession:@"delay time set failed"];
            return;
        }
        
        //设置测温间隔时间
        cmdStr = [NSString stringWithFormat: GET_TAG_CMD(SET_INTERVAL, cmdType), _hexIntervalSeconds];
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode]];
            return;
        }
        cmdStr = GET_TAG_CMD(READ_INTERVAL, cmdType);
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode]];
            return;
        }
        result = [CommonUtils revert2BytesHexString:result];
        if(![result isEqualToString:_hexIntervalSeconds]){
            [self showMsgOnSession:@"interval time set failed"];
            return;
        }
        
        //设置测温次数
        cmdStr = [NSString stringWithFormat: GET_TAG_CMD(SET_COUNT, cmdType), _hexLoggingCount];
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode]];
            return;
        }
        cmdStr = GET_TAG_CMD(READ_COUNT, cmdType);
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:YES];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode]];
            return;
        }
        if(result.length>=4){
            result = [result substringWithRange:NSMakeRange(0, 4)];
        }
        if(![result isEqualToString:_hexLoggingCount]){
            [self showMsgOnSession:@"logging count set failed"];
            return;
        }
        
        //配置寄存器，设置测温最大最小值，固定
        cmdStr = [NSString stringWithFormat: GET_TAG_CMD(SETREG_MAX_TMP, cmdType), [CommonUtils getHexByDecimal:100*4 digit:4]];
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode]];
            return;
        }
        cmdStr = [NSString stringWithFormat: GET_TAG_CMD(SETREG_MIN_TMP, cmdType), [CommonUtils getHexByDecimal:(-100*4+0x400) digit:4]];
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode]];
            return;
        }
        
        //设置EEPROM，设置用户设定最大最小测温值
        cmdStr = [NSString stringWithFormat: GET_TAG_CMD(SETEEPROM_MIN_TMP, cmdType), _hexMinTemperature];
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode]];
            return;
        }
        cmdStr = [NSString stringWithFormat: GET_TAG_CMD(SETEEPROM_MAX_TMP, cmdType), _hexMaxTemperature];
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode]];
            return;
        }
        
        //写block9，最小，最大温度
        cmdStr = [NSString stringWithFormat: GET_TAG_CMD(SET_BLOCK9_TMP, cmdType), _hexBlock9Min, _hexBlock9Max];
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode]];
            return;
        }
        
        //写block10，测温间隔时间
        cmdStr = [NSString stringWithFormat: GET_TAG_CMD(SET_BLOCK10_INTERVAL, cmdType), _hexIntervalSeconds];
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode]];
            return;
        }
        
        //记录时间戳
        NSInteger nowTimestamp = [[CommonUtils getNowTimeTimestamp] integerValue];
        intValue = [CommonUtils getDecimalByHex: _hexDelayMinutes] * 60;
        //时间戳=当前时间+延迟时间
        nowTimestamp = nowTimestamp + intValue;
        cmdStr = [NSString stringWithFormat: GET_TAG_CMD(SET_TIMESTAMP, cmdType), [CommonUtils getHexByDecimal:nowTimestamp digit:8]];
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode]];
            return;
        }
        
        //启动定时测温
        cmdStr = GET_TAG_CMD(START_LOGGING, cmdType);
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode]];
            return;
        }
        if(![result containsString:FM_Z_SUCCESS]){
            [self showMsgOnSession:[NSString stringWithFormat:@"logging start failed, command:%@ failed,result:%@", cmdStr, result]];
            return;
        }
    }
    else if(_opType == LoggingStopType){
        //停止定时测温
        cmdStr = GET_TAG_CMD(GET_RANDOM, cmdType);
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode]];
            return;
        }
        result = [NSString stringWithFormat:@"%@%@%@%@", [result substringWithRange:NSMakeRange(2,2)],[result substringWithRange:NSMakeRange(6,2)],[result substringWithRange:NSMakeRange(0,2)],[result substringWithRange:NSMakeRange(4,2)]];
        //NSLog(@"转换顺序后:%@", result);
        result = [self getBinaryByHex:result];
        //NSLog(@"二进制:%@", result);
        result = [NSString stringWithFormat:@"%@%@", [result substringFromIndex:result.length-3],[result substringWithRange:NSMakeRange(0,result.length-3)]];
        //NSLog(@"转换顺序后二进制:%@", result);
        result = [CommonUtils convertDecimalFromBinary:result];
        //NSLog(@"二进制转十进制:%@", result);
        result = [CommonUtils getHexByDecimal:[result integerValue] digit:8];
        //NSLog(@"十进制转hex:%@", result);
        result = [NSString stringWithFormat:@"%@%@%@%@", [result substringWithRange:NSMakeRange(6,2)],[result substringWithRange:NSMakeRange(4,2)],[result substringWithRange:NSMakeRange(2,2)],[result substringWithRange:NSMakeRange(0,2)]];
        //NSLog(@"转换顺序后hex:%@", result);
        
        cmdStr = [NSString stringWithFormat:GET_TAG_CMD(STOP_LOGGING, cmdType), result];
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode]];
            return;
        }
        if(![result containsString:FM_STOP_SUCCESS]){
            [self showMsgOnSession:[NSString stringWithFormat:@"logging stop failed, command:%@ failed,result:%@", cmdStr, result]];
            return;
        }
        //必须要sleep一会，否则停不住
        [NSThread sleepForTimeInterval:0.3];
    }
    else if(_opType == LoggingResultType){
        _tagFormat = 0;//默认普通数据格式
        
        cmdStr = GET_TAG_CMD(GET_USER_CFG, cmdType);
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:YES];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, (long)statusCode]];
            return;
        }
        NSString *formatType = [self getBinaryByHex:result];
        formatType = [formatType substringWithRange:NSMakeRange(3, 3)];
        NSLog(@"温度格式为:%@",formatType);
        
        if([formatType isEqualToString:@"111"]){
            _tagFormat = 1;//原始数据格式
            cmdStr = GET_TAG_CMD(GET_DET, cmdType);
            result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:YES];
            if(statusCode != BT_SUCCESS){
                [self showMsgOnSession:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode]];
                return;
            }
            NSLog(@"det数据:%@", result);
            NSString *offsetStr = [result substringWithRange:NSMakeRange(4,4)];
            NSString *detAStr = [result substringWithRange:NSMakeRange(8,4)];
            NSString *detBStr = [result substringWithRange:NSMakeRange(12,4)];
            //转换为原始数据，高低位字节互换
            _vDetOffset = [self getDetDataFromHex:offsetStr];
            _vDetA = [self getDetDataFromHex:detAStr];
            _vDetB = [self getDetDataFromHex:detBStr];
            NSLog(@"detA:%.3f, detB:%.3f", _vDetA, _vDetB);
            NSLog(@"offset:%.3f", _vDetOffset);
        }
        else if([formatType isEqualToString:@"011"]){
            _tagFormat = 2;//标准数据格式
//            NSLog(@"标准数据格式");
        }
        
        //获取存储数据区域的大小
        cmdStr = GET_TAG_CMD(GET_SIZE, cmdType);
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode]];
            return;
        }
        if(!result||result.length<=0){
            [self showMsgOnSession:@"no data found"];
            return;
        }
        result = [result substringWithRange:NSMakeRange(4, 2)];
        NSInteger maxSize = [CommonUtils getDecimalByHex:result]*1024;
        NSLog(@"size 大小为:%ld", maxSize);
        
        //读取测温总次数
        cmdStr = GET_TAG_CMD(READ_COUNT, cmdType);
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:YES];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode]];
            return;
        }
        if(result.length>=4){
            result = [result substringWithRange:NSMakeRange(0, 4)];
        }
        
        _lMsg.totalCount = [CommonUtils getDecimalByHex:[CommonUtils revert2BytesHexString:result]];
        NSLog(@"测温次数:%ld", _lMsg.totalCount);
        
        //获取测温间隔时间和开始测温时间
        cmdStr = GET_TAG_CMD(GET_START_TIME, cmdType);
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:YES];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode]];
            return;
        }
        if(tagObj.cmdType == CMD_14443){
            innerStr = [result substringWithRange:NSMakeRange(0, 8)];
            _lMsg.startTime = [CommonUtils getDecimalByHex: innerStr];
            innerStr = [CommonUtils timestampSwitchTime:_lMsg.startTime andFormatter:@"YYYY-MM-dd HH:mm:ss"];
            _lMsg.intervalSeconds = [[result substringWithRange:NSMakeRange(result.length-8, 4)] integerValue];
        }
        else{
            innerStr = [result substringWithRange:NSMakeRange(4, 8)];
            _lMsg.startTime = [CommonUtils getDecimalByHex: innerStr];
            innerStr = [CommonUtils timestampSwitchTime:_lMsg.startTime andFormatter:@"YYYY-MM-dd HH:mm:ss"];
            _lMsg.intervalSeconds = [[result substringWithRange:NSMakeRange(result.length-4, 4)] integerValue];
        }
        NSLog(@"开始时间:%@，间隔时间:%ld", innerStr, _lMsg.intervalSeconds);
        
        //获取最大最小温度范围
        cmdStr = GET_TAG_CMD(GET_MAX_MIN_TEMP, cmdType);
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:YES];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode]];
            return;
        }
        
        _lMsg.validMinimum = [self getTemperatureFromHex:[result substringWithRange:NSMakeRange(0, 4)]];
        _lMsg.validMaximum = [self getTemperatureFromHex:[result substringWithRange:NSMakeRange(4, 4)]];
        NSLog(@"最小有效温度:%.3f, 最大有效温度:%.3f", _lMsg.validMinimum, _lMsg.validMaximum);
        
        //检查定时测温状态
        cmdStr = GET_TAG_CMD(CHECK_STATUS, cmdType);
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode]];
            return;
        }
        result = [self formatFMBinaryString:result];
        symbol = [result substringWithRange:NSMakeRange(3, 1)];
        //symbol 0表示完成测温，1表示未完成测温
        if([symbol isEqualToString:@"1"]){
            //读取C094寄存器
            cmdStr = GET_TAG_CMD(GET_C094, cmdType);
            result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:YES];
            if(statusCode != BT_SUCCESS){
                [self showMsgOnSession:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode]];
                return;
            }
            
            //0x0010等待测温，0x0020正在测温，0x0000标签异常状态
            if([result isEqualToString:@"000000"]){
                _lMsg.opStatus = STATUS_ERROR;
            }
            else if([result isEqualToString:@"001000"]){
                _lMsg.opStatus = STATUS_WAITING;
            }
            else{
                _lMsg.opStatus = STATUS_LOGGING;
            }
//            cmdStr = GET_TAG_CMD(GET_C09A, cmdType);
//            result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
//            if(statusCode != BT_SUCCESS){
//                [self showMsgOnSession:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode]];
//                return;
//            }
//            result = [CommonUtils revert2BytesHexString:result];
//            _lMsg.overHighCount = [CommonUtils getDecimalByHex:result];
//
//            cmdStr = GET_TAG_CMD(GET_C09B, cmdType);
//            result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
//            if(statusCode != BT_SUCCESS){
//                [self showMsgOnSession:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode]];
//                return;
//            }
//            result = [CommonUtils revert2BytesHexString:result];
//            _lMsg.overLowCount = [CommonUtils getDecimalByHex:result];
            
//            cmdStr = GET_TAG_CMD(GET_MIN_RECORD, cmdType);
//            result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
//            if(statusCode != BT_SUCCESS){
//                [self showMsgOnSession:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode]];
//                return;
//            }
//            _lMsg.recordedMinimum = [self getTemperatureFromHex:result];
//
//            cmdStr = GET_TAG_CMD(GET_MAX_RECORD, cmdType);
//            result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
//            if(statusCode != BT_SUCCESS){
//                [self showMsgOnSession:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode]];
//                return;
//            }
//            _lMsg.recordedMaximum = [self getTemperatureFromHex:result];
            
            //读取测温延时
            cmdStr = GET_TAG_CMD(READ_DELAY, cmdType);
            result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
            if(statusCode != BT_SUCCESS){
                [self showMsgOnSession:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode]];
                return;
            }
            //返回结果第一字节（低位）为延迟时间
            result = [result substringWithRange:NSMakeRange(0, 2)];
            _lMsg.delayMinutes = [CommonUtils getDecimalByHex:result];
            
            cmdStr = GET_TAG_CMD(BUSY_RECORDED_COUNT, cmdType);
            result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
            if(statusCode != BT_SUCCESS){
                [self showMsgOnSession:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode]];
                return;
            }
            result = [NSString stringWithFormat:@"%@%@", [result substringFromIndex:2],[result substringWithRange:NSMakeRange(0, 2)]];
            _lMsg.recordedCount = [CommonUtils getDecimalByHex:result];
            if(_lMsg.delayMinutes == 0){
                _lMsg.recordedCount++;
            }
            NSLog(@"繁忙，实际测温次数:%ld", _lMsg.recordedCount);
        }
        else{
            _lMsg.opStatus = STATUS_FINISH;
            
            cmdStr = GET_TAG_CMD(GET_B180, cmdType);
            result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:YES];
            if(statusCode != BT_SUCCESS){
                [self showMsgOnSession:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode]];
                return;
            }
//            _lMsg.recordedMaximum = [self getTemperatureFromHex:[result substringWithRange:NSMakeRange(0, 4)]];
//            _lMsg.recordedMinimum = [self getTemperatureFromHex:[result substringWithRange:NSMakeRange(4, 4)]];
//            NSLog(@"最小实测温度:%.3f, 最大实测温度:%.3f", _lMsg.recordedMinimum, _lMsg.recordedMaximum);
//            innerStr = [result substringWithRange:NSMakeRange(8, 4)];
//            innerStr = [CommonUtils revert2BytesHexString:innerStr];
//            _lMsg.overHighCount = [CommonUtils getDecimalByHex:innerStr];
//            innerStr = [result substringWithRange:NSMakeRange(12, 4)];
//            innerStr = [CommonUtils revert2BytesHexString:innerStr];
//            _lMsg.overLowCount = [CommonUtils getDecimalByHex:innerStr];
            
            cmdStr = GET_TAG_CMD(IDLE_RECORDED_COUNT, cmdType);
            result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:YES];
            if(statusCode != BT_SUCCESS){
                [self showMsgOnSession:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode]];
                return;
            }
            result = [NSString stringWithFormat:@"%@%@", [result substringWithRange:NSMakeRange(2,2)], [result substringWithRange:NSMakeRange(0,2)]];
            _lMsg.recordedCount = [CommonUtils getDecimalByHex:result];
            if(_tagFormat==1){
                _lMsg.recordedCount = _lMsg.recordedCount*2;
            }
            _lMsg.recordedCount++;
            NSLog(@"空闲，实际测温次数:%ld", _lMsg.recordedCount);
        }
        
        //读取测温明细
        NSInteger bytesCount = 4;
        if(_tagFormat==1){
            bytesCount = 2;
        }
        NSInteger dataSize = bytesCount*_lMsg.recordedCount;
        if(dataSize > maxSize){
            NSLog(@"dataSize超出范围，dataSize:%ld, maxSize:%ld", dataSize, maxSize);
            dataSize = maxSize;
        }
        //返回NO，表明明细数据获取异常
        if(![self getTempDetailA:tagObj totalSize:dataSize msg:_lMsg cmdType:cmdType])
        {
            return;
        }
        //计算最大最小值
        for(int i=0; i<_lMsg.temperaturesArray.count;i++){
            TempDetail *item = _lMsg.temperaturesArray[i];
            NSLog(@"第%@个 hex:%@ decimal:%@ time:%@", item.tempID, item.hexTemp, item.decimalTemp, item.opTime);
            CGFloat decimalTemp = [item.decimalTemp floatValue];
            if(i==0){
                _lMsg.recordedMaximum = decimalTemp;
                _lMsg.recordedMinimum = decimalTemp;
            }
            else{
                if(decimalTemp>_lMsg.recordedMaximum){
                    _lMsg.recordedMaximum = decimalTemp;
                }
                if(decimalTemp<_lMsg.recordedMinimum){
                    _lMsg.recordedMinimum = decimalTemp;
                }
            }
        }
        
        _lMsg.overHighCount = 0;
        _lMsg.overLowCount = 0;
        //计算超限次数
        for(int i=0; i<_lMsg.temperaturesArray.count;i++){
            TempDetail *item = _lMsg.temperaturesArray[i];
            CGFloat decimalTemp = [item.decimalTemp floatValue];

            if(decimalTemp>_lMsg.validMaximum){
                _lMsg.overHighCount++;
            }
            if(decimalTemp<_lMsg.validMinimum){
                _lMsg.overLowCount++;
            }
        }
    }
    else if(_opType == IfWakeupType){
        //检测是否唤醒
        cmdStr = GET_TAG_CMD(PDSTATUS, cmdType);
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode]];
            return;
        }
        if([result containsString:FM_OUT_PD]){
            _mMsg.isWakeup = YES;
        }
    }
    else if(_opType == SleepType){
        //休眠
        cmdStr = GET_TAG_CMD(SLEEP, cmdType);
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode]];
            return;
        }
    }
    else if(_opType == WakeupType){
        //唤醒
        cmdStr = GET_TAG_CMD(WAKEUP, cmdType);
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode]];
            return;
        }
    }
    else if(_opType == UHFInitType){
        //超高频初始化
        cmdStr = GET_TAG_CMD(UHF_INIT, cmdType);
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode]];
            return;
        }
    }
    else if(_opType == CustomType){
        cmdStr = _command;
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:YES];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode]];
            return;
        }
        if(_mMsg){
            _mMsg.message = result;
        }
    }
    else if(_opType == LEDOnType){
        //打开LED灯
        cmdStr = GET_TAG_CMD(LED_ON, cmdType);
        [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode]];
            return;
        }
    }
    else if(_opType == LEDOffType){
//        cmdStr = GET_TAG_CMD(LED_ON, cmdType);
//        [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
//        if(statusCode != BT_SUCCESS){
//            [self showMsgOnSession:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode]];
//            return;
//        }
//        [NSThread sleepForTimeInterval:5];
        cmdStr = GET_TAG_CMD(LED_OFF, cmdType);
        [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode]];
            return;
        }
    }
    session.alertMessage = @"operation success";
    [self removeSession];
    
    __block typeof(self) blockSelf = self;
    if (_mMsg && _measureCompleteBlock) {
        dispatch_sync(dispatch_get_main_queue(), ^{
            blockSelf.mMsg.isSuccess = YES;
            blockSelf.measureCompleteBlock(blockSelf.mMsg);
        });
    }
    if (_lMsg && _loggingCompleteBlock) {
        dispatch_sync(dispatch_get_main_queue(), ^{
            blockSelf.lMsg.isSuccess = YES;
            blockSelf.loggingCompleteBlock(blockSelf.lMsg);
        });
    }
}

//扫描界面显示提示文字
- (void)showMsgOnSession:(NSString *)msg{
    if(_mMsg){
        _mMsg.message = msg;
    }
    else if(_lMsg){
        _lMsg.message = msg;
    }
    _session.alertMessage = msg;
    __block typeof(self) blockSelf = self;
    if(_measureCompleteBlock){
        dispatch_sync(dispatch_get_main_queue(), ^{
            blockSelf.measureCompleteBlock(blockSelf.mMsg);
        });
    }
    else if(_loggingCompleteBlock){
        dispatch_sync(dispatch_get_main_queue(), ^{
            blockSelf.loggingCompleteBlock(blockSelf.lMsg);
        });
    }
    if(_session){
        if(_isContinueScan){
            [NSThread sleepForTimeInterval:2];
            [_session restartPolling];
        }
        else{
            [_session invalidateSessionWithErrorMessage:msg];
            _session = nil;
        }
    }
}

- (void)tagReaderSession:(NFCTagReaderSession *)session didInvalidateWithError:(NSError *)error API_AVAILABLE(ios(13.0)){
    NSLog(@"didInvalidateWithError:%@",error);
    
    switch (error.code) {
        case NFCReaderErrorUnsupportedFeature: {
            NSLog(@"NFCReaderErrorUnsupportedFeature");
        }
            break;
        case NFCReaderErrorSecurityViolation: {
            NSLog(@"NFCReaderErrorUnsupportedFeature");
        }
            break;
        case NFCReaderTransceiveErrorTagConnectionLost: {
            NSLog(@"NFCReaderErrorUnsupportedFeature");
        }
            break;
        case NFCReaderTransceiveErrorRetryExceeded: {
            NSLog(@"NFCReaderTransceiveErrorRetryExceeded");
        }
            break;
        case NFCReaderTransceiveErrorTagResponseError: {
            NSLog(@"NFCReaderTransceiveErrorTagResponseError");
        }
            break;
        case NFCReaderSessionInvalidationErrorUserCanceled: {
            NSLog(@"NFCReaderSessionInvalidationErrorUserCanceled");
        }
            break;
        case NFCReaderSessionInvalidationErrorSessionTimeout: {
            NSLog(@"NFCReaderSessionInvalidationErrorSessionTimeout");
        }
            break;
        case NFCReaderSessionInvalidationErrorSessionTerminatedUnexpectedly: {
            NSLog(@"NFCReaderSessionInvalidationErrorSessionTerminatedUnexpectedly");
        }
            break;
        case NFCReaderSessionInvalidationErrorSystemIsBusy: {
            NSLog(@"NFCReaderSessionInvalidationErrorSystemIsBusy");
        }
            break;
        case NFCReaderSessionInvalidationErrorFirstNDEFTagRead: {
            NSLog(@"NFCReaderSessionInvalidationErrorFirstNDEFTagRead");
        }
            break;
        case NFCTagCommandConfigurationErrorInvalidParameters: {
            NSLog(@"NFCTagCommandConfigurationErrorInvalidParameters");
        }
            break;
        default:
            break;
    }
    [self removeSession];
}

@end

@implementation LoggingMsg
@end

@implementation MeasureMsg
@end

@implementation TempDetail
@end
