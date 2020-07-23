//  ModelButton.h
//
//  Created by lubozhi on 2019/7/25.
//  Copyright © 2019年 复旦微电子集团股份科技有限公司. All rights reserved.

@interface ModelButton : UIButton

@property(nonatomic, strong) UIImageView *buttomImage;
@property(nonatomic, strong) UILabel *buttonLabel;
/**
 *  设置按钮的图片和标题
 *
 *  @param buttomImage 图片
 *  @param title       标题
 */
- (void)setButtomImage:(NSString *)buttomImage Title:(NSString *)title;

/**
 *  根据fream设置按钮的图片和标题
 *
 *  @param buttomImage 按钮图片
 *  @param imageFrame  图片frame
 *  @param title       按钮的标题
 *  @param titleFrame  标题的frame
 */
- (void)setButtomImage:(NSString *)buttomImage ImageFrame:(CGRect )imageFrame Title:(NSString *)title TitleFrame:(CGRect )titleFrame;

@end
