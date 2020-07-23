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
@property (nonatomic, assign) NSInteger NDEFDuplicatesCounter;
@property (nonatomic, strong) NSMutableArray *messagesArrayM;
@property (nonatomic, strong) NSMutableDictionary *hexDic;
//操作类型
@property (nonatomic, assign) OperationType opType;
//延迟测温时间hex，单位分钟
@property (nonatomic, strong) NSString *hexDelayMinutes;
//测温间隔时间hex，单位秒
@property (nonatomic, strong) NSString *hexIntervalSeconds;
//测温次数hex
@property (nonatomic, strong) NSString *hexLoggingCount;
//测温最小值hex
@property (nonatomic, strong) NSString *hexMinTemperature;
@property (nonatomic, strong) NSString *hexBlock9Min;
//测温最大值hex
@property (nonatomic, strong) NSString *hexMaxTemperature;
@property (nonatomic, strong) NSString *hexBlock9Max;
@property (nonatomic, strong) MeasureMsg *mMsg;
@property (nonatomic, strong) LoggingMsg *lMsg;

@end

@implementation NFCTagHelper

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

- (void)setHexDelayMinutes:(NSString *)value{
    _hexDelayMinutes = [CommonUtils getHexByDecimal:[value integerValue] digit:2];
}

- (void)setHexIntervalSeconds:(NSString *)value{
    _hexIntervalSeconds = [CommonUtils getHexByDecimal:[value integerValue] digit:4];
}

- (void)setHexLoggingCount:(NSString *)value{
    _hexLoggingCount = [CommonUtils revert2BytesHexString:[CommonUtils getHexByDecimal:[value integerValue] digit:4]];
}

- (void)setHexMinTemperature:(NSString *)value{
    NSInteger tmpValue = [value integerValue];
    if(tmpValue < 0){
        tmpValue = tmpValue+256;
    }
    _hexMinTemperature = [CommonUtils revert2BytesHexString:[self getHexFromTemperature:tmpValue]];
    _hexBlock9Min = [CommonUtils getHexByDecimal:tmpValue digit:2];
}

- (void)setHexMaxTemperature:(NSString *)value{
    NSInteger tmpValue = [value integerValue];
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
        NSLog(@"温度为%.2f", fValue);
    }
    return fValue;
}

- (NSString *)startReadTag:(OperationType)opType {
    NSString *response = @"";
    _opType = opType;
    if (@available(iOS 13.0, *)) {
        [self readNFCTag];
    } else {
        response = @"该功能仅适用于iOS 13以上系统";
    }
    return response;
}

- (void)readNFCTag {
    _session = [[NFCTagReaderSession alloc] initWithPollingOption:NFCPollingISO14443|NFCPollingISO15693 delegate:self queue:dispatch_get_global_queue(0, 0)];
    [_session beginSession];
    _NDEFDuplicatesCounter = 0;
//    if (NFCTagReaderSession.readingAvailable) {
//        NSLog(@"Reading from device is allowed.");
//    } else {
//        NSLog(@"Cannot read from device.");
//    }
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
            n = p-4;
        }
        else{
            n = pageSize-4;
        }
        xyStr = [NSString stringWithFormat:@"%@%@", [CommonUtils getHexByDecimal:x digit:2], [CommonUtils getHexByDecimal:y digit:2]];
        nStr = [CommonUtils getHexByDecimal:n digit:4];
        cmdStr = [NSString stringWithFormat:GET_TAG_CMD(GET_DETAIL, cmdType), xyStr, nStr];
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:YES];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession: _session message:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode] noCallback:NO];
            return NO;
        }
        if([result isEqualToString:FM_DETAIL_END]){
            break;
        }
        if(result.length % 4!=0){
            return NO;
        }
        dataCount = result.length / 8;
        TempDetail *item;
        for(int j=0;j<dataCount;j++){
            item = [[TempDetail alloc] init];
            tempStr = [result substringWithRange:NSMakeRange(2*j*4, 4)];
            item.tempID = [NSString stringWithFormat:@"%d",j+1];
            item.hexTemp = tempStr;
            fValue = [self getTemperatureFromHex:tempStr];
            tempStr = [NSString stringWithFormat:@"%.2f", fValue];
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
        [self showMsgOnSession: session message:@"More than 1 tags was found. Please present only 1 tag." noCallback:YES];
        return;
    }
    id<NFCTag> tag = tags.firstObject;
    if(tag.type != NFCTagTypeMiFare && tag.type != NFCTagTypeISO15693){
        [self showMsgOnSession: session message:@"NFCType: %lu，not support this protocol" noCallback:YES];
        return;
    }

    //NSLog(@"NFCType: %lu，支持NFCMiFareTag协议",(unsigned long)tag.type);
    BOOL isConnected = [self connectTag:tag];
    if(!isConnected){
        [self showMsgOnSession: session message:@"failed to connected tag." noCallback:YES];
        return;
    }

    NFCTagObject *tagObj = [[NFCTagObject alloc] initWithTag:tag];
    NSInteger statusCode = BT_OPERATION_FAILED;
    NSString *result, *cmdStr, *symbol, *innerStr;
    NSInteger intValue = 0;
    NSInteger finishFlag = 0;
    CGFloat fValue = 0;
    NSInteger cmdType = tagObj.cmdType;
    
    _mMsg = nil;
    _lMsg = nil;
    if(_opType!=LoggingStartType&&_opType!=LoggingStopType&&_opType!=LoggingResultType){
        _mMsg = [[MeasureMsg alloc] init];
        _mMsg.isSuccess = NO;
        _mMsg.tagType = tagObj.tagType;
        _mMsg.opType = _opType;
        _mMsg.uid = tagObj.uuid;
        _mMsg.isWakeup = NO;
    }
    else{
        if(_opType==LoggingResultType){
            _lMsg = [[LoggingMsg alloc] init];
            _lMsg.temperaturesArray = [[NSMutableArray alloc] init];
            _lMsg.tagType = tagObj.tagType;
            _lMsg.opType = _opType;
            _lMsg.uid = tagObj.uuid;
        }
    }
    if(_opType == BasicType){
        //测场强
        cmdStr = GET_TAG_CMD(FIELD, cmdType);
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession: session message:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode] noCallback:NO];
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
            [self showMsgOnSession: session message:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode] noCallback:NO];
            return;
        }
        if(![result containsString:FM_F_SUCCESS]){
            [self showMsgOnSession: session message:[NSString stringWithFormat:@"temperature start failed,command:%@ failed,result:%@", cmdStr, result] noCallback:NO];
            return;
        }
        
        [NSThread sleepForTimeInterval:0.32];
        cmdStr = GET_TAG_CMD(ONCE_RESULT, cmdType);
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession: session message:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode] noCallback:NO];
            return;
        }
        //计算温度
        fValue = [self getTemperatureFromHex:result];
        _mMsg.tempValue = [NSString stringWithFormat:@"%.2f °C", fValue];
        
        //测电压
        //唤醒指令有bug，需要去掉
//        cmdStr = GET_TAG_CMD(WAKEUP, cmdType);
//        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
//        if(statusCode != BT_SUCCESS){
//            [self showMsgOnSession: session message:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode] noCallback:NO];
//            return;
//        }
        
//        cmdStr = GET_TAG_CMD(PDSTATUS, cmdType);
//        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
//        if(statusCode != BT_SUCCESS){
//            [self showMsgOnSession: session message:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode] noCallback:NO];
//            return;
//        }
//        if(![result containsString:FM_OUT_PD]){
//            [self showMsgOnSession: session message:[NSString stringWithFormat:@"wake up failed,code:%ld", statusCode] noCallback:NO];
//            return;
//        }
        
        cmdStr = GET_TAG_CMD(VOLTAGE_1, cmdType);
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession: session message:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode] noCallback:NO];
            return;
        }
        
        cmdStr = GET_TAG_CMD(VOLTAGE_2, cmdType);
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession: session message:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode] noCallback:NO];
            return;
        }
        if(![result containsString:FM_F_SUCCESS]){
            [self showMsgOnSession: session message:[NSString stringWithFormat:@"voltage start failed,command:%@ failed,result:%@", cmdStr, result] noCallback:NO];
            return;
        }
        [NSThread sleepForTimeInterval:0.32];
        
        cmdStr = GET_TAG_CMD(VOLTAGE_3, cmdType);
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession: session message:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode] noCallback:NO];
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
            [self showMsgOnSession: session message:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode] noCallback:NO];
            return;
        }
    }
    else if(_opType == LEDType){
        cmdStr = GET_TAG_CMD(LED_ON, cmdType);
        [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession: session message:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode] noCallback:NO];
            return;
        }
        [NSThread sleepForTimeInterval:5];
        cmdStr = GET_TAG_CMD(LED_OFF, cmdType);
        [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession: session message:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode] noCallback:NO];
            return;
        }
    }
    else if(_opType == LoggingStartType){
        //检查定时测温状态
        cmdStr = GET_TAG_CMD(CHECK_STATUS, cmdType);
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession: session message:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode] noCallback:NO];
            return;
        }
        result = [self formatFMBinaryString:result];
        symbol = [result substringWithRange:NSMakeRange(3, 1)];
        //bit12 = 1表示当前处于定时测温状态
        if([symbol isEqualToString:@"1"]){
            [self showMsgOnSession: session message:@"this tag is logging, please wait a moment" noCallback:NO];
            return;
        }
        
        //唤醒
        cmdStr = GET_TAG_CMD(WAKEUP, cmdType);
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession: session message:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode] noCallback:NO];
            return;
        }
        cmdStr = GET_TAG_CMD(PDSTATUS, cmdType);
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession: session message:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode] noCallback:NO];
            return;
        }
        if(![result containsString:FM_OUT_PD]){
            [self showMsgOnSession: session message:[NSString stringWithFormat:@"wake up failed,code:%ld", statusCode] noCallback:NO];
            return;
        }
        
        //设置延迟测温时间
        cmdStr = [NSString stringWithFormat: GET_TAG_CMD(SET_DELAY, cmdType), _hexDelayMinutes];
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession: session message:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode] noCallback:NO];
            return;
        }
        cmdStr = GET_TAG_CMD(READ_DELAY, cmdType);
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession: session message:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode] noCallback:NO];
            return;
        }
        //返回结果第一字节（低位）为延迟时间
        result = [result substringWithRange:NSMakeRange(0, 2)];
        if(![result isEqualToString:_hexDelayMinutes]){
            [self showMsgOnSession: session message:@"delay time set failed" noCallback:NO];
            return;
        }
        
        //设置测温间隔时间
        cmdStr = [NSString stringWithFormat: GET_TAG_CMD(SET_INTERVAL, cmdType), _hexIntervalSeconds];
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession: session message:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode] noCallback:NO];
            return;
        }
        cmdStr = GET_TAG_CMD(READ_INTERVAL, cmdType);
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession: session message:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode] noCallback:NO];
            return;
        }
        result = [CommonUtils revert2BytesHexString:result];
        if(![result isEqualToString:_hexIntervalSeconds]){
            [self showMsgOnSession: session message:@"interval time set failed" noCallback:NO];
            return;
        }
        
        //设置测温次数
        cmdStr = [NSString stringWithFormat: GET_TAG_CMD(SET_COUNT, cmdType), _hexLoggingCount];
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession: session message:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode] noCallback:NO];
            return;
        }
        cmdStr = GET_TAG_CMD(READ_COUNT, cmdType);
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:YES];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession: session message:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode] noCallback:NO];
            return;
        }
        if(result.length>=4){
            result = [result substringWithRange:NSMakeRange(0, 4)];
        }
        if(![result isEqualToString:_hexLoggingCount]){
            [self showMsgOnSession: session message:@"logging count set failed" noCallback:NO];
            return;
        }
        
        //配置寄存器，设置测温最大最小值，固定
        cmdStr = [NSString stringWithFormat: GET_TAG_CMD(SETREG_MAX_TMP, cmdType), [CommonUtils getHexByDecimal:100*4 digit:4]];
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession: session message:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode] noCallback:NO];
            return;
        }
        cmdStr = [NSString stringWithFormat: GET_TAG_CMD(SETREG_MIN_TMP, cmdType), [CommonUtils getHexByDecimal:(-100*4+0x400) digit:4]];
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession: session message:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode] noCallback:NO];
            return;
        }
        
        //设置EEPROM，设置用户设定最大最小测温值
        cmdStr = [NSString stringWithFormat: GET_TAG_CMD(SETEEPROM_MIN_TMP, cmdType), _hexMinTemperature];
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession: session message:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode] noCallback:NO];
            return;
        }
        cmdStr = [NSString stringWithFormat: GET_TAG_CMD(SETEEPROM_MAX_TMP, cmdType), _hexMaxTemperature];
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession: session message:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode] noCallback:NO];
            return;
        }
        
        //写block9，最小，最大温度
        cmdStr = [NSString stringWithFormat: GET_TAG_CMD(SET_BLOCK9_TMP, cmdType), _hexBlock9Min, _hexBlock9Max];
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession: session message:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode] noCallback:NO];
            return;
        }
        
        //写block10，测温间隔时间
        cmdStr = [NSString stringWithFormat: GET_TAG_CMD(SET_BLOCK10_INTERVAL, cmdType), _hexIntervalSeconds];
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession: session message:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode] noCallback:NO];
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
            [self showMsgOnSession: session message:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode] noCallback:NO];
            return;
        }
        
        //启动定时测温
        cmdStr = GET_TAG_CMD(START_LOGGING, cmdType);
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession: session message:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode] noCallback:NO];
            return;
        }
        if(![result containsString:FM_Z_SUCCESS]){
            [self showMsgOnSession: session message:[NSString stringWithFormat:@"logging start failed, command:%@ failed,result:%@", cmdStr, result] noCallback:NO];
            return;
        }
    }
    else if(_opType == LoggingStopType){
        //停止定时测温
        cmdStr = GET_TAG_CMD(GET_RANDOM, cmdType);
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession: session message:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode] noCallback:NO];
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
            [self showMsgOnSession: session message:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode] noCallback:NO];
            return;
        }
        if(![result containsString:FM_STOP_SUCCESS]){
            [self showMsgOnSession: session message:[NSString stringWithFormat:@"logging stop failed, command:%@ failed,result:%@", cmdStr, result] noCallback:NO];
            return;
        }
        //必须要sleep一会，否则停不住
        [NSThread sleepForTimeInterval:0.3];
    }
    else if(_opType == LoggingResultType){
        //获取存储数据区域的大小
        cmdStr = GET_TAG_CMD(GET_SIZE, cmdType);
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession: session message:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode] noCallback:NO];
            return;
        }
        if(!result||result.length<=0){
            [self showMsgOnSession: session message:@"no data found" noCallback:NO];
            return;
        }
        result = [result substringWithRange:NSMakeRange(4, 2)];
        NSInteger maxSize = [CommonUtils getDecimalByHex:result]*1024;
        NSLog(@"size 大小为:%ld", maxSize);
        
        //读取测温总次数
        cmdStr = GET_TAG_CMD(READ_COUNT, cmdType);
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:YES];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession: session message:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode] noCallback:NO];
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
            [self showMsgOnSession: session message:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode] noCallback:NO];
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
            [self showMsgOnSession: session message:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode] noCallback:NO];
            return;
        }
        
        _lMsg.validMinimum = [self getTemperatureFromHex:[result substringWithRange:NSMakeRange(0, 4)]];
        _lMsg.validMaximum = [self getTemperatureFromHex:[result substringWithRange:NSMakeRange(4, 4)]];
        NSLog(@"最小有效温度:%.2f, 最大有效温度:%.2f", _lMsg.validMinimum, _lMsg.validMaximum);
        
        //检查定时测温状态
        cmdStr = GET_TAG_CMD(CHECK_STATUS, cmdType);
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession: session message:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode] noCallback:NO];
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
                [self showMsgOnSession: session message:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode] noCallback:NO];
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
            cmdStr = GET_TAG_CMD(GET_C09A, cmdType);
            result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
            if(statusCode != BT_SUCCESS){
                [self showMsgOnSession: session message:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode] noCallback:NO];
                return;
            }
            result = [CommonUtils revert2BytesHexString:result];
            _lMsg.overHighCount = [CommonUtils getDecimalByHex:result];
            
            cmdStr = GET_TAG_CMD(GET_C09B, cmdType);
            result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
            if(statusCode != BT_SUCCESS){
                [self showMsgOnSession: session message:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode] noCallback:NO];
                return;
            }
            result = [CommonUtils revert2BytesHexString:result];
            _lMsg.overLowCount = [CommonUtils getDecimalByHex:result];
            
            cmdStr = GET_TAG_CMD(GET_MIN_RECORD, cmdType);
            result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
            if(statusCode != BT_SUCCESS){
                [self showMsgOnSession: session message:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode] noCallback:NO];
                return;
            }
            _lMsg.recordedMinimum = [self getTemperatureFromHex:result];
            
            cmdStr = GET_TAG_CMD(GET_MAX_RECORD, cmdType);
            result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
            if(statusCode != BT_SUCCESS){
                [self showMsgOnSession: session message:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode] noCallback:NO];
                return;
            }
            _lMsg.recordedMaximum = [self getTemperatureFromHex:result];
            
            //读取测温延时
            cmdStr = GET_TAG_CMD(READ_DELAY, cmdType);
            result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
            if(statusCode != BT_SUCCESS){
                [self showMsgOnSession: session message:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode] noCallback:NO];
                return;
            }
            //返回结果第一字节（低位）为延迟时间
            result = [result substringWithRange:NSMakeRange(0, 2)];
            _lMsg.delayMinutes = [CommonUtils getDecimalByHex:result];
            
            cmdStr = GET_TAG_CMD(BUSY_RECORDED_COUNT, cmdType);
            result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
            if(statusCode != BT_SUCCESS){
                [self showMsgOnSession: session message:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode] noCallback:NO];
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
                [self showMsgOnSession: session message:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode] noCallback:NO];
                return;
            }
            _lMsg.recordedMaximum = [self getTemperatureFromHex:[result substringWithRange:NSMakeRange(0, 4)]];
            _lMsg.recordedMinimum = [self getTemperatureFromHex:[result substringWithRange:NSMakeRange(4, 4)]];
            NSLog(@"最小实测温度:%.2f, 最大实测温度:%.2f", _lMsg.recordedMinimum, _lMsg.recordedMaximum);
            innerStr = [result substringWithRange:NSMakeRange(8, 4)];
            innerStr = [CommonUtils revert2BytesHexString:innerStr];
            _lMsg.overHighCount = [CommonUtils getDecimalByHex:innerStr];
            innerStr = [result substringWithRange:NSMakeRange(12, 4)];
            innerStr = [CommonUtils revert2BytesHexString:innerStr];
            _lMsg.overLowCount = [CommonUtils getDecimalByHex:innerStr];
            
            cmdStr = GET_TAG_CMD(IDLE_RECORDED_COUNT, cmdType);
            result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:YES];
            if(statusCode != BT_SUCCESS){
                [self showMsgOnSession: session message:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode] noCallback:NO];
                return;
            }
            result = [NSString stringWithFormat:@"%@%@", [result substringWithRange:NSMakeRange(2,2)], [result substringWithRange:NSMakeRange(0,2)]];
            _lMsg.recordedCount = [CommonUtils getDecimalByHex:result]+1;
            NSLog(@"空闲，实际测温次数:%ld", _lMsg.recordedCount);
        }
        
        //读取测温明细
        NSInteger dataSize = _lMsg.recordedCount*4;
        if(dataSize > maxSize){
            NSLog(@"dataSize超出范围，dataSize:%ld, maxSize:%ld", dataSize, maxSize);
            dataSize = maxSize;
        }
        //返回NO，表明明细数据获取异常
        if(![self getTempDetailA:tagObj totalSize:dataSize msg:_lMsg cmdType:cmdType])
        {
            return;
        }
        for(int i=0; i<_lMsg.temperaturesArray.count;i++){
            TempDetail *item = _lMsg.temperaturesArray[i];
            NSLog(@"第%@个 hex:%@ decimal:%@ time:%@", item.tempID, item.hexTemp, item.decimalTemp, item.opTime);
            if(i==_lMsg.temperaturesArray.count-1){
                CGFloat decimalTemp = [item.decimalTemp floatValue];
                if(decimalTemp>_lMsg.recordedMaximum){
                    _lMsg.recordedMaximum = decimalTemp;
                }
                if(decimalTemp<_lMsg.recordedMinimum){
                    _lMsg.recordedMinimum = decimalTemp;
                }
                if(finishFlag==0){
                    if(decimalTemp>_lMsg.validMaximum){
                        _lMsg.overHighCount++;
                    }
                    if(decimalTemp<_lMsg.validMinimum){
                        _lMsg.overLowCount++;
                    }
                }
            }
        }
        
    }
    else if(_opType == IfWakeupType){
        //检测是否唤醒
        cmdStr = GET_TAG_CMD(PDSTATUS, cmdType);
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession: session message:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode] noCallback:NO];
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
            [self showMsgOnSession: session message:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode] noCallback:NO];
            return;
        }
    }
    else if(_opType == UHFInitType){
        //超高频初始化
        cmdStr = GET_TAG_CMD(UHF_INIT, cmdType);
        result = [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession: session message:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode] noCallback:NO];
            return;
        }
    }
    else if(_opType == LEDOnType){
        //打开LED灯
        cmdStr = GET_TAG_CMD(LED_ON, cmdType);
        [tagObj sendAPDU:cmdStr code:&statusCode withHead:NO];
        if(statusCode != BT_SUCCESS){
            [self showMsgOnSession: session message:[NSString stringWithFormat:@"command:%@ send failed,code:%ld", cmdStr, statusCode] noCallback:NO];
            return;
        }
    }
    session.alertMessage = @"operation success";
    [self removeSession];
    
    __block typeof(self) blockSelf = self;
    dispatch_sync(dispatch_get_main_queue(), ^{
        if (blockSelf.mMsg && blockSelf.delegate && [blockSelf.delegate respondsToSelector:@selector(NfcMeasureComplete:)]) {
            blockSelf.mMsg.isSuccess = YES;
            [blockSelf.delegate NfcMeasureComplete:blockSelf.mMsg];
        }
        if (blockSelf.lMsg && blockSelf.delegate && [blockSelf.delegate respondsToSelector:@selector(NfcLoggingComplete:)]) {
            [blockSelf.delegate NfcLoggingComplete:blockSelf.lMsg];
        }
    });
}

//扫描界面显示提示文字
- (void)showMsgOnSession:(NFCReaderSession *)session message:(NSString *)msg noCallback:(BOOL)noCallback{
    session.alertMessage = msg;
    if(!noCallback){
        __block typeof(self) blockSelf = self;
        dispatch_sync(dispatch_get_main_queue(), ^{
            if (blockSelf.mMsg && blockSelf.delegate && [blockSelf.delegate respondsToSelector:@selector(NfcMeasureComplete:)]) {
                [blockSelf.delegate NfcMeasureComplete:blockSelf.mMsg];
            }
            if (blockSelf.lMsg && blockSelf.delegate && [blockSelf.delegate respondsToSelector:@selector(NfcMeasureComplete:)]) {
                [blockSelf.delegate NfcLoggingComplete:blockSelf.lMsg];
            }
        });
    }
    [NSThread sleepForTimeInterval:2];
    [_session restartPolling];
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
