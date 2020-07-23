//
//  NFCHelper.h
//  FMTemperature
//
//  Created by gaolailong on 2018/7/9.
//  Copyright © 2018年 复旦微电子集团股份科技有限公司. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import <CoreNFC/CoreNFC.h>

@protocol NFCTagHelperDelegate<NSObject>

@optional
//定时测温回调
- (void)NfcLoggingComplete:(LoggingMsg *)nfcMsg;
//即时测量回调
- (void)NfcMeasureComplete:(MeasureMsg *)nfcMsg;

@end

@interface NFCTagHelper: NSObject

+ (instancetype)shareInstance;

- (NSString *)startReadTag:(OperationType)opType;
- (void)setHexDelayMinutes:(NSString *)value;
- (void)setHexIntervalSeconds:(NSString *)value;
- (void)setHexLoggingCount:(NSString *)value;
- (void)setHexMinTemperature:(NSString *)value;
- (void)setHexMaxTemperature:(NSString *)value;

@property (nonatomic, weak) id<NFCTagHelperDelegate> delegate;

@end
