//  QFPickerView.m
//
//  Created by lubozhi on 2019/7/25.
//  Copyright © 2019年 复旦微电子集团股份科技有限公司. All rights reserved.

#import "QFPickerView.h"

@interface QFPickerView () <UIPickerViewDataSource,UIPickerViewDelegate>

@property (nonatomic, copy) void (^backBlock)(NSInteger index);
@property (nonatomic, weak) NSMutableArray *dataArray;
@property (nonatomic, strong) UIView *contentView;
@property (nonatomic, strong) UIPickerView *pickerView;

@end

@implementation QFPickerView

#pragma mark - initDatePickerView
- (instancetype)initPickerViewWithArray:(NSArray *)dataArray backBlock:(void (^)(NSInteger index))backBlock{
    if (self = [super init]) {
        self.frame = [UIScreen mainScreen].bounds;
    }
    if (backBlock) {
        self.backBlock = backBlock;
        self.dataArray = (NSMutableArray *)dataArray;
    }
    _contentView = [[UIView alloc] initWithFrame:CGRectMake(0, self.frame.size.height, self.frame.size.width, 300)];
    [self addSubview:_contentView];
    //设置背景颜色为黑色，并有0.4的透明度
    self.backgroundColor = [UIColor colorWithWhite:0 alpha:0.4];
    [self addGestureRecognizer:[[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(dismiss)]];
    //添加白色view
    UIView *whiteView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, self.frame.size.width, 40)];
    whiteView.backgroundColor = [UIColor whiteColor];
    [_contentView addSubview:whiteView];
    //添加确定和取消按钮
    for (int i = 0; i < 2; i ++) {
        UIButton *button = [[UIButton alloc] initWithFrame:CGRectMake((self.frame.size.width - 80) * i, 0, 80, 40)];
        [button setTitle:i == 0 ? NSLocalizedString(@"FM_Cancel", nil) : NSLocalizedString(@"FM_Confirm", nil) forState:UIControlStateNormal];
        if (i == 0) {
            [button setTitleColor:[UIColor colorWithRed:97.0 / 255.0 green:97.0 / 255.0 blue:97.0 / 255.0 alpha:1] forState:UIControlStateNormal];
        } else {
            [button setTitleColor:DSBlueColor forState:UIControlStateNormal];
        }
        [whiteView addSubview:button];
        [button addTarget:self action:@selector(buttonTapped:) forControlEvents:UIControlEventTouchUpInside];
        button.tag = 10 + i;
    }
    
    _pickerView = [[UIPickerView alloc] initWithFrame:CGRectMake(0, 40, CGRectGetWidth(self.bounds), 260)];
    _pickerView.delegate = self;
    _pickerView.dataSource = self;
    _pickerView.backgroundColor = [UIColor colorWithRed:240.0/255 green:243.0/255 blue:250.0/255 alpha:1];
    
    [_contentView addSubview:_pickerView];
    return self;
}

//设置选中内容
- (void)setSelectData:(NSInteger)index{
    [_pickerView selectRow:index inComponent:0 animated:NO];
}

#pragma mark - Actions
- (void)buttonTapped:(UIButton *)sender {
    if (sender.tag == 10) {
        [self dismiss];
    } else {
        _backBlock([_pickerView selectedRowInComponent:0]);
        [self dismiss];
    }
}

#pragma mark - pickerView出现
- (void)show {
    __block typeof(self) blockSelf = self;
    [[UIApplication sharedApplication].delegate.window.rootViewController.view addSubview:self];
    [UIView animateWithDuration:0.3 animations:^{
        blockSelf.contentView.center = CGPointMake(self.frame.size.width/2, blockSelf.contentView.center.y - blockSelf.contentView.frame.size.height);
    }];
}
#pragma mark - pickerView消失
- (void)dismiss{
    __block typeof(self) blockSelf = self;
    [UIView animateWithDuration:0.3 animations:^{
        blockSelf.contentView.center = CGPointMake(blockSelf.frame.size.width/2, blockSelf.contentView.center.y + blockSelf.contentView.frame.size.height);
    } completion:^(BOOL finished) {
        [blockSelf removeFromSuperview];
    }];
}

#pragma mark - UIPickerViewDataSource UIPickerViewDelegate
- (NSInteger)numberOfComponentsInPickerView:(UIPickerView *)pickerView {
    return 1;
}

- (NSInteger)pickerView:(UIPickerView *)pickerView numberOfRowsInComponent:(NSInteger)component {
    return _dataArray.count;
}

- (NSString *)pickerView:(UIPickerView *)pickerView titleForRow:(NSInteger)row forComponent:(NSInteger)component {
    return _dataArray[row];
}

-(UIView *)pickerView:(UIPickerView *)pickerView viewForRow:(NSInteger)row forComponent:(NSInteger)component reusingView:(UIView *)view{
    UILabel *lbl = (UILabel *)view;
    if (!lbl) {
        lbl = [[UILabel alloc]init];
        //在这里设置字体相关属性
        lbl.font = [UIFont fontWithName:@"PingFangSC-Regular" size:18*DSAdaptCoefficient];
        lbl.textColor = DSBlueColor;
        lbl.textAlignment = NSTextAlignmentCenter;
        [lbl setBackgroundColor:[UIColor clearColor]];
    }
    //重新加载lbl的文字内容
    lbl.text = [self pickerView:pickerView titleForRow:row forComponent:component];
    return lbl;
}

@end
