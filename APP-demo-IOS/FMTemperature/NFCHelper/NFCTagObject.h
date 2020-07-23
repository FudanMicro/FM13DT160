#import <CoreNFC/CoreNFC.h>

@interface NFCTagObject: NSObject

@property(nonatomic, weak) id<NFCTag> tag;
@property(nonatomic, copy) NSString *uuid;
@property(nonatomic, copy) NSString *tagType;
@property(nonatomic, assign) CMDType cmdType;

- (instancetype)initWithTag:(id<NFCTag>) tag;
- (NSString *)sendAPDU:(NSString *)command code:(NSInteger *)code withHead:(BOOL)withHead;

@end
