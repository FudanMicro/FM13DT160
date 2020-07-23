//  QFPickerView.h
//
//  Created by lubozhi on 2019/7/25.
//  Copyright © 2019年 复旦微电子集团股份科技有限公司. All rights reserved.

#import <UIKit/UIKit.h>

@interface QFPickerView : UIView

- (instancetype)initPickerViewWithArray:(NSArray *)dataArray backBlock:(void (^)(NSInteger index))block;

- (void)show;

//设置选中内容
- (void)setSelectData:(NSInteger)index;

@end
