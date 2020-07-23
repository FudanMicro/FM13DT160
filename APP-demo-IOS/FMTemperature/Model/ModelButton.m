//  ModelButton.h
//
//  Created by lubozhi on 2019/7/25.
//  Copyright © 2019年 复旦微电子集团股份科技有限公司. All rights reserved.
//

#import "ModelButton.h"

@implementation ModelButton

- (void)setButtomImage:(NSString *)buttomImage ImageFrame:(CGRect )imageFrame Title:(NSString *)title TitleFrame:(CGRect )titleFrame{
    self.buttomImage = [[UIImageView alloc]initWithFrame:imageFrame];
    self.buttomImage.image = [UIImage imageNamed:buttomImage];
    [self addSubview:self.buttomImage];
    
    self.buttonLabel = [[UILabel alloc] initWithFrame:titleFrame];
    self.buttonLabel.text = title;
    self.buttonLabel.textColor = DSColor(92, 96, 99);
    self.buttonLabel.font = [UIFont fontWithName:@"PingFangSC-Regular" size:14*DSAdaptCoefficient];
    self.buttonLabel.textAlignment = NSTextAlignmentCenter;
    [self addSubview:self.buttonLabel];
}

- (void)setButtomImage:(NSString *)buttomImage Title:(NSString *)title{
    CGRect imageFrameNew = CGRectMake((self.frame.size.width-50*DSAdaptCoefficient)/2.0, 25*DSAdaptCoefficient, 50*DSAdaptCoefficient, 50*DSAdaptCoefficient);
    CGRect titleFrameNew = CGRectMake(0, CGRectGetMaxY(imageFrameNew)+6*DSAdaptCoefficient, self.frame.size.width, 15*DSAdaptCoefficient);
    [self setButtomImage:buttomImage ImageFrame:imageFrameNew Title:title TitleFrame:titleFrameNew];
}

@end
