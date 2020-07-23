#import "NFCTagObject.h"

@interface NFCTagObject()


@end

@implementation NFCTagObject

- (instancetype)initWithTag:(id<NFCTag>) tag {
    if (self = [super init]) {
        self.tag = tag;
        if(self.tag.type == NFCTagTypeMiFare){
            id<NFCMiFareTag> miFareTag = (id<NFCMiFareTag>)self.tag;
            self.uuid = [CommonUtils hexStringWithData:miFareTag.identifier];
            self.uuid = self.uuid;
            self.tagType = @"14443";
            self.cmdType = CMD_14443;
        }
        else if(self.tag.type == NFCTagTypeISO15693){
            id<NFCISO15693Tag> tag15693 = (id<NFCISO15693Tag>)self.tag;
            self.uuid = [CommonUtils hexStringWithData:tag15693.identifier];
            self.tagType = @"15693";
            self.cmdType = CMD_15693;
        }
    }
    return self;
}

- (NSString *)sendAPDU:(NSString *)command code:(NSInteger *)code withHead:(BOOL)withHead
{
    __block NSString *resultCmd = nil;
    *code = BT_OPERATION_FAILED;
    NSData *cmdData = [CommonUtils dataWithHexString:command];
    dispatch_semaphore_t sem = dispatch_semaphore_create(0);
    if(_tag.type == NFCTagTypeMiFare){
        __block id<NFCMiFareTag> blockTag = (id<NFCMiFareTag>)_tag;
        [blockTag sendMiFareCommand:cmdData
                  completionHandler:^(NSData *response, NSError *error){
            if(error != nil){
                *code = error.code;
                NSLog(@"send command error, command:%@ code:%lu", command, error.code);
            }
            else{
                resultCmd = [CommonUtils hexStringWithData:response];
                //截取第一字节00
                if(!withHead&&resultCmd.length>2){
                    resultCmd = [resultCmd substringFromIndex:2];
                }
                *code = BT_SUCCESS;
                NSLog(@"send command success, command:%@ data size:%lu response:%@", command, [resultCmd length]/2, resultCmd);
            }
            dispatch_semaphore_signal(sem);
        }];
    }
    else if(_tag.type == NFCTagTypeISO15693){
        NSString *cfgStr = @"";
        if(command.length<=2){
            cmdData = nil;
        }
        else{
            cfgStr = [command substringFromIndex:2];
            cmdData = [CommonUtils dataWithHexString:cfgStr];
        }
        NSInteger cmdCode = [CommonUtils getDecimalByHex:[command substringToIndex:2]];
        __block id<NFCISO15693Tag> blockTag = (id<NFCISO15693Tag>)_tag;
        [blockTag selectWithRequestFlags:RequestFlagAddress|RequestFlagHighDataRate
        completionHandler:^(NSError *error){
            if(error != nil){
                NSLog(@"select 失败");
                dispatch_semaphore_signal(sem);
            }
            else{
                NSLog(@"select 成功");
                [blockTag customCommandWithRequestFlag:RequestFlagHighDataRate customCommandCode:cmdCode customRequestParameters:cmdData completionHandler:^(NSData *response, NSError *error){
                    if(error != nil){
                        *code = error.code;
                        NSLog(@"send command error, command:%@ code:%lu", command, error.code);
                    }
                    else{
                        resultCmd = [CommonUtils hexStringWithData:response];
                        //截取第一字节00
                        if([command isEqualToString:CMD_GET_SIZE_V]&&resultCmd.length>2){
                            resultCmd = [resultCmd substringFromIndex:2];
                        }
                        *code = BT_SUCCESS;
                        NSLog(@"send command success, command:%@ data size:%lu response:%@", command, [resultCmd length]/2, resultCmd);
                    }
                    dispatch_semaphore_signal(sem);
                }];
            }
        }];
    }
    else{
        dispatch_semaphore_signal(sem);
    }
    //等待信号
    dispatch_semaphore_wait(sem, DISPATCH_TIME_FOREVER);
    return resultCmd;
}

@end
