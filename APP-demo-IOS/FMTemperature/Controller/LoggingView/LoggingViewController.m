//  LoggingViewController.m
//
//  Created by lubozhi on 2019/7/25.
//  Copyright © 2019年 复旦微电子集团股份科技有限公司. All rights reserved.

#import "LoggingViewController.h"
#import "QFPickerView.h"
#import "NFCTagHelper.h"
#import "GraphicViewController.h"

@interface LoggingViewController ()<NFCTagHelperDelegate>

//用于绘制界面的时候记录view底部
@property(nonatomic, assign) CGFloat viewBottom;
//背景view
@property(nonatomic, strong) UIView *bgView;
//测温延时
@property(nonatomic, strong) UILabel *delayLabel;
//测温间隔
@property(nonatomic, strong) UILabel *intervalLabel;
//测温次数
@property(nonatomic, strong) UILabel *tempCountLabel;
//最大温度
@property(nonatomic, strong) UILabel *tempMaxLabel;
//最小温度
@property(nonatomic, strong) UILabel *tempMinLabel;

//条件数据
@property(nonatomic, strong) NSArray *delayTextArr;
@property(nonatomic, strong) NSArray *delayValueArr;
@property(nonatomic, assign) NSInteger delayIndex;

@property(nonatomic, strong) NSArray *intervalTextArr;
@property(nonatomic, strong) NSArray *intervalValueArr;
@property(nonatomic, assign) NSInteger intervalIndex;

@property(nonatomic, strong) NSArray *tempCountTextArr;
@property(nonatomic, strong) NSArray *tempCountValueArr;
@property(nonatomic, assign) NSInteger tempCountIndex;

@property(nonatomic, strong) NSArray *tempMinTextArr;
@property(nonatomic, strong) NSArray *tempMinValueArr;
@property(nonatomic, assign) NSInteger tempMinIndex;

@property(nonatomic, strong) NSArray *tempMaxTextArr;
@property(nonatomic, strong) NSArray *tempMaxValueArr;
@property(nonatomic, assign) NSInteger tempMaxIndex;

//条件信息
@property(nonatomic, strong) LoggingInfo *myLoggingInfo;
@end

@implementation LoggingViewController

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

- (void)viewDidLoad {
    
    [super viewDidLoad];
    self.navigationItem.title=NSLocalizedString(@"FM_Tab_Logging", nil);
    CGRect statusRect = [[UIApplication sharedApplication] statusBarFrame];
    CGRect navRect = self.navigationController.navigationBar.frame;
    //初始高度=状态栏高度+导航栏高度
    _viewBottom = statusRect.size.height+navRect.size.height;
    _bgView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, DSScreenWidth, DSScreenHeight)];
    _bgView.backgroundColor = DSColor(246, 246, 246);
    [self.view addSubview:_bgView];
    
    _delayTextArr = @[@"no delay", @"1 minute", @"2 minutes", @"5 minutes", @"10 minutes",@"15 minutes",@"30 minutes",@"1 hour",@"2 hours",@"4 hours"];
    _delayValueArr = @[@"0",@"1",@"2",@"5",@"10",@"15",@"30",@"60",@"120",@"240"];
    
    _intervalTextArr = @[@"1 s",@"2 s",@"3 s",@"4 s",@"5 s",@"6 s",@"7 s",@"8 s",@"9 s",@"10 s",@"15 s",@"20 s",@"25 s",@"30 s",@"35 s",@"40 s",@"45 s",@"50 s",@"55 s",@"1 minute",@"2 minutes",@"5 minutes",@"10 minutes",@"15 minutes",@"30 minutes",@"1 hour"];
    _intervalValueArr = @[@"1",@"2",@"3",@"4",@"5",@"6",@"7",@"8",@"9",@"10",@"15",@"20",@"25",@"30",@"35",@"40",@"45",@"50",@"55",@"60",@"120",@"300",@"600",@"900",@"1800",@"3600"];
    
    _tempCountTextArr = @[@"2",@"3",@"5",@"8",@"10",@"20",@"30",@"50",@"80",@"100",@"200",@"300",@"500",@"800",@"1000",@"2000",@"3000",@"4000",@"4864"];
    _tempCountValueArr = @[@"2",@"3",@"5",@"8",@"10",@"20",@"30",@"50",@"80",@"100",@"200",@"300",@"500",@"800",@"1000",@"2000",@"3000",@"4000",@"4864"];
    
    _tempMinTextArr = @[@"-40 °C",@"-30 °C",@"-20 °C",@"-18 °C",@"-15 °C",@"-10 °C",@"-8 °C",@"-5 °C",@"-4 °C",@"-3 °C",@"-2 °C",@"-1 °C",@"0 °C",@"1 °C",@"2 °C",@"3 °C",@"4 °C",@"5 °C",@"8 °C",@"10 °C",@"15 °C",@"18 °C",@"20 °C",@"23 °C",@"25 °C",@"28 °C",@"29 °C",@"30 °C",@"35 °C",@"40 °C",@"50 °C",@"60 °C",@"70 °C",@"80 °C"];
    _tempMinValueArr = @[@"-40",@"-30",@"-20",@"-18",@"-15",@"-10",@"-8",@"-5",@"-4",@"-3",@"-2",@"-1",@"0",@"1",@"2",@"3",@"4",@"5",@"8",@"10",@"15",@"18",@"20",@"23",@"25",@"28",@"29",@"30",@"35",@"40",@"50",@"60",@"70",@"80"];
    
    _tempMaxTextArr = @[@"-40 °C",@"-30 °C",@"-20 °C",@"-18 °C",@"-15 °C",@"-10 °C",@"-8 °C",@"-5 °C",@"-4 °C",@"-3 °C",@"-2 °C",@"-1 °C",@"0 °C",@"1 °C",@"2 °C",@"3 °C",@"4 °C",@"5 °C",@"8 °C",@"10 °C",@"15 °C",@"18 °C",@"20 °C",@"23 °C",@"25 °C",@"28 °C",@"29 °C",@"30 °C",@"35 °C",@"40 °C",@"50 °C",@"60 °C",@"70 °C",@"80 °C"];
    _tempMaxValueArr = @[@"-40",@"-30",@"-20",@"-18",@"-15",@"-10",@"-8",@"-5",@"-4",@"-3",@"-2",@"-1",@"0",@"1",@"2",@"3",@"4",@"5",@"8",@"10",@"15",@"18",@"20",@"23",@"25",@"28",@"29",@"30",@"35",@"40",@"50",@"60",@"70",@"80"];
    
    _myLoggingInfo = [CommonUtils getLoggingInfo];
    
    _delayIndex = [CommonUtils getIndexFromArray:_delayValueArr text:_myLoggingInfo.delayMinutes];
    _intervalIndex = [CommonUtils getIndexFromArray:_intervalValueArr text:_myLoggingInfo.intervalSeconds];
    _tempCountIndex = [CommonUtils getIndexFromArray:_tempCountValueArr text:_myLoggingInfo.loggingCount];
    _tempMinIndex = [CommonUtils getIndexFromArray:_tempMinValueArr text:_myLoggingInfo.minTemperature];
    _tempMaxIndex = [CommonUtils getIndexFromArray:_tempMaxValueArr text:_myLoggingInfo.maxTemperature];
    
    //初始化测温条件视图
    [self initConditionView];
    //初始化按钮时图
    [self initButtonView];
}

//初始化按钮时图
- (void)initButtonView{
    CGFloat btnInterval = 10*DSAdaptCoefficient;
    CGFloat btnSpacing = 27*DSAdaptCoefficient;//距离两边间距
    CGFloat btnHeight = 50*DSAdaptCoefficient;
    _viewBottom += btnInterval;
    
    //开启测温
    UIButton *btn = [[UIButton alloc]initWithFrame:CGRectMake(btnSpacing, _viewBottom+10*DSAdaptCoefficient, DSScreenWidth - 2*btnSpacing, btnHeight)];
    btn.layer.borderWidth = 1;
    btn.layer.cornerRadius = 5;
    btn.titleLabel.font = [UIFont fontWithName:@"PingFangSC-Regular" size:18*DSAdaptCoefficient];
    btn.backgroundColor = DSBlueColor;
    btn.layer.borderColor = DSBlueColor.CGColor;
    [btn setTitleColor:DSCommonColor forState:(UIControlStateNormal)];
    [btn setTitle:NSLocalizedString(@"FM_Start_Logging", nil) forState:UIControlStateNormal];
    [btn addTarget:self action:@selector(startLogging:) forControlEvents:UIControlEventTouchUpInside];
    [_bgView addSubview:btn];
    _viewBottom = CGRectGetMaxY(btn.frame)+btnInterval;
    
    //查看测温结果
    btn = [[UIButton alloc]initWithFrame:CGRectMake(btnSpacing, _viewBottom, DSScreenWidth - 2*btnSpacing, btnHeight)];
    btn.layer.borderWidth = 1;
    btn.layer.cornerRadius = 5;
    btn.titleLabel.font = [UIFont fontWithName:@"PingFangSC-Regular" size:18*DSAdaptCoefficient];
    btn.backgroundColor = DSBlueColor;
    btn.layer.borderColor = DSBlueColor.CGColor;
    [btn setTitleColor:DSCommonColor forState:(UIControlStateNormal)];
    [btn setTitle:NSLocalizedString(@"FM_Read_Data", nil) forState:UIControlStateNormal];
    [btn addTarget:self action:@selector(getLoggingResult:) forControlEvents:UIControlEventTouchUpInside];
    [_bgView addSubview:btn];
    _viewBottom = CGRectGetMaxY(btn.frame)+btnInterval;
    
    //停止测温
    btn = [[UIButton alloc]initWithFrame:CGRectMake(btnSpacing, _viewBottom, DSScreenWidth - 2*btnSpacing, btnHeight)];
    btn.layer.borderWidth = 1;
    btn.layer.cornerRadius = 5;
    btn.titleLabel.font = [UIFont fontWithName:@"PingFangSC-Regular" size:18*DSAdaptCoefficient];
    btn.backgroundColor = DSBlueColor;
    btn.layer.borderColor = DSBlueColor.CGColor;
    [btn setTitleColor:DSCommonColor forState:(UIControlStateNormal)];
    [btn setTitle:NSLocalizedString(@"FM_Stop_Logging", nil) forState:UIControlStateNormal];
    [btn addTarget:self action:@selector(stopLogging:) forControlEvents:UIControlEventTouchUpInside];
    [_bgView addSubview:btn];
    _viewBottom = CGRectGetMaxY(btn.frame)+btnInterval;
}

//初始化测温条件视图
- (void)initConditionView{
    UIFont *normalFont = [UIFont fontWithName:@"PingFangSC-Regular" size:16*DSAdaptCoefficient];
    UILabel *headerLabel = [[UILabel alloc] initWithFrame:CGRectMake(10*DSAdaptCoefficient, _viewBottom, 200*DSAdaptCoefficient, 32*DSAdaptCoefficient)];
    UIColor *labelColor = DSColor(92, 96, 99);
    headerLabel.text = NSLocalizedString(@"FM_Logging_Config", nil);
    headerLabel.textColor = DSColor(51, 51, 51);
    headerLabel.font = [UIFont fontWithName:@"PingFangSC-Semibold" size:16*DSAdaptCoefficient];
    headerLabel.textAlignment = NSTextAlignmentLeft;
    [_bgView addSubview:headerLabel];
    _viewBottom = CGRectGetMaxY(headerLabel.frame);
    
    //与左边缘距离
    CGFloat spaceWidth = 10*DSAdaptCoefficient;
    //内容容器宽度
    CGFloat contentWidth = DSScreenWidth;
    CGFloat contentHeight = 40*DSAdaptCoefficient;
    //标题宽度
    CGFloat titleWidth = 256*DSAdaptCoefficient;
    CGFloat arrowWidth = 7*DSAdaptCoefficient;
    CGFloat arrowHeight = 14*DSAdaptCoefficient;
    CGFloat arrowYPos = (contentHeight-arrowHeight)/2;
    //arrow距两边的间隔
    CGFloat arrowSpaceWidth = 5*DSAdaptCoefficient;
    CGFloat xPos = spaceWidth;
    //数值宽度
    CGFloat textWidth = contentWidth - 2*spaceWidth - 2*arrowWidth - titleWidth;
    UIView *contentView = [[UIView alloc] initWithFrame:CGRectMake(0,_viewBottom,contentWidth,contentHeight)];
    contentView.backgroundColor = DSCommonColor;
    [_bgView addSubview:contentView];
    _viewBottom = CGRectGetMaxY(contentView.frame)+1*DSAdaptCoefficient;
    
    UILabel *titleLabel = [[UILabel alloc] initWithFrame:CGRectMake(xPos,0,titleWidth,contentHeight)];
    titleLabel.textAlignment = NSTextAlignmentLeft;
    titleLabel.text = NSLocalizedString(@"FM_Delay_Time", nil);
    titleLabel.font = normalFont;
    titleLabel.textColor = labelColor;
    [contentView addSubview:titleLabel];
    
    xPos += titleWidth;
    _delayLabel = [[UILabel alloc] initWithFrame:CGRectMake(xPos, 0, textWidth, contentHeight)];
    _delayLabel.textAlignment = NSTextAlignmentRight;
    _delayLabel.text = [_delayTextArr objectAtIndex:_delayIndex];
    _delayLabel.font = normalFont;
    _delayLabel.textColor = labelColor;
    [contentView addSubview:_delayLabel];
    
    xPos = xPos + textWidth + arrowSpaceWidth;
    //箭头
    UIImageView *arrowImageView = [[UIImageView alloc] initWithFrame:CGRectMake(xPos, arrowYPos, arrowWidth, arrowHeight)];
    arrowImageView.image = [UIImage imageNamed:@"arrow_right"];
    [contentView addSubview:arrowImageView];
    
    UITapGestureRecognizer *recognizer=[[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(onDelayClick:)];
    [contentView addGestureRecognizer:recognizer];
    
    xPos = spaceWidth;
    contentView = [[UIView alloc] initWithFrame:CGRectMake(0,_viewBottom,contentWidth,contentHeight)];
    contentView.backgroundColor = DSCommonColor;
    [_bgView addSubview:contentView];
    _viewBottom = CGRectGetMaxY(contentView.frame)+1*DSAdaptCoefficient;
    
    titleLabel = [[UILabel alloc] initWithFrame:CGRectMake(xPos,0,titleWidth,contentHeight)];
    titleLabel.textAlignment = NSTextAlignmentLeft;
    titleLabel.text = NSLocalizedString(@"FM_Time_Interval", nil);
    titleLabel.font = normalFont;
    titleLabel.textColor = labelColor;
    [contentView addSubview:titleLabel];
    
    xPos += titleWidth;
    _intervalLabel = [[UILabel alloc] initWithFrame:CGRectMake(xPos, 0, textWidth, contentHeight)];
    _intervalLabel.textAlignment = NSTextAlignmentRight;
    _intervalLabel.text = [_intervalTextArr objectAtIndex:_intervalIndex];
    _intervalLabel.font = normalFont;
    _intervalLabel.textColor = labelColor;
    [contentView addSubview:_intervalLabel];
    
    xPos = xPos + textWidth + arrowSpaceWidth;
    //箭头
    arrowImageView = [[UIImageView alloc] initWithFrame:CGRectMake(xPos, arrowYPos, arrowWidth, arrowHeight)];
    arrowImageView.image = [UIImage imageNamed:@"arrow_right"];
    [contentView addSubview:arrowImageView];
    
    recognizer=[[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(onIntervalClick:)];
    [contentView addGestureRecognizer:recognizer];
    
    xPos = spaceWidth;
    contentView = [[UIView alloc] initWithFrame:CGRectMake(0,_viewBottom,contentWidth,contentHeight)];
    contentView.backgroundColor = DSCommonColor;
    [_bgView addSubview:contentView];
    _viewBottom = CGRectGetMaxY(contentView.frame)+1*DSAdaptCoefficient;
    
    titleLabel = [[UILabel alloc] initWithFrame:CGRectMake(xPos,0,titleWidth,contentHeight)];
    titleLabel.textAlignment = NSTextAlignmentLeft;
    titleLabel.text = NSLocalizedString(@"FM_Logging_Points", nil);
    titleLabel.font = normalFont;
    titleLabel.textColor = labelColor;
    [contentView addSubview:titleLabel];
    
    xPos += titleWidth;
    _tempCountLabel = [[UILabel alloc] initWithFrame:CGRectMake(xPos, 0, textWidth, contentHeight)];
    _tempCountLabel.textAlignment = NSTextAlignmentRight;
    _tempCountLabel.text = [_tempCountTextArr objectAtIndex:_tempCountIndex];;
    _tempCountLabel.font = normalFont;
    _tempCountLabel.textColor = labelColor;
    [contentView addSubview:_tempCountLabel];
    
    xPos = xPos + textWidth + arrowSpaceWidth;
    //箭头
    arrowImageView = [[UIImageView alloc] initWithFrame:CGRectMake(xPos, arrowYPos, arrowWidth, arrowHeight)];
    arrowImageView.image = [UIImage imageNamed:@"arrow_right"];
    [contentView addSubview:arrowImageView];
    
    recognizer=[[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(onTempCountClick:)];
    [contentView addGestureRecognizer:recognizer];
    
    xPos = spaceWidth;
    contentView = [[UIView alloc] initWithFrame:CGRectMake(0,_viewBottom,contentWidth,contentHeight)];
    contentView.backgroundColor = DSCommonColor;
    [_bgView addSubview:contentView];
    _viewBottom = CGRectGetMaxY(contentView.frame)+1*DSAdaptCoefficient;
    
    titleLabel = [[UILabel alloc] initWithFrame:CGRectMake(xPos,0,titleWidth,contentHeight)];
    titleLabel.textAlignment = NSTextAlignmentLeft;
    titleLabel.text = NSLocalizedString(@"FM_Low_Temperature_Thresholds", nil);
    titleLabel.font = normalFont;
    titleLabel.textColor = labelColor;
    [contentView addSubview:titleLabel];
    
    xPos += titleWidth;
    _tempMinLabel = [[UILabel alloc] initWithFrame:CGRectMake(xPos, 0, textWidth, contentHeight)];
    _tempMinLabel.textAlignment = NSTextAlignmentRight;
    _tempMinLabel.text = [_tempMinTextArr objectAtIndex:_tempMinIndex];
    _tempMinLabel.font = normalFont;
    _tempMinLabel.textColor = labelColor;
    [contentView addSubview:_tempMinLabel];
    
    xPos = xPos + textWidth + arrowSpaceWidth;
    //箭头
    arrowImageView = [[UIImageView alloc] initWithFrame:CGRectMake(xPos, arrowYPos, arrowWidth, arrowHeight)];
    arrowImageView.image = [UIImage imageNamed:@"arrow_right"];
    [contentView addSubview:arrowImageView];
    
    recognizer=[[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(onMinClick:)];
    [contentView addGestureRecognizer:recognizer];
    
    xPos = spaceWidth;
    contentView = [[UIView alloc] initWithFrame:CGRectMake(0,_viewBottom,contentWidth,contentHeight)];
    contentView.backgroundColor = DSCommonColor;
    [_bgView addSubview:contentView];
    _viewBottom = CGRectGetMaxY(contentView.frame)+1*DSAdaptCoefficient;
    
    titleLabel = [[UILabel alloc] initWithFrame:CGRectMake(xPos,0,titleWidth,contentHeight)];
    titleLabel.textAlignment = NSTextAlignmentLeft;
    titleLabel.text = NSLocalizedString(@"FM_High_Temperature_Thresholds", nil);
    titleLabel.font = normalFont;
    titleLabel.textColor = labelColor;
    [contentView addSubview:titleLabel];
    
    xPos += titleWidth;
    _tempMaxLabel = [[UILabel alloc] initWithFrame:CGRectMake(xPos, 0, textWidth, contentHeight)];
    _tempMaxLabel.textAlignment = NSTextAlignmentRight;
    _tempMaxLabel.text = [_tempMaxTextArr objectAtIndex:_tempMaxIndex];;
    _tempMaxLabel.font = normalFont;
    _tempMaxLabel.textColor = labelColor;
    [contentView addSubview:_tempMaxLabel];
    
    xPos = xPos + textWidth + arrowSpaceWidth;
    //箭头
    arrowImageView = [[UIImageView alloc] initWithFrame:CGRectMake(xPos, arrowYPos, arrowWidth, arrowHeight)];
    arrowImageView.image = [UIImage imageNamed:@"arrow_right"];
    [contentView addSubview:arrowImageView];
    
    recognizer=[[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(onMaxClick:)];
    [contentView addGestureRecognizer:recognizer];
}

//启动测温
-(void)startLogging:(UIButton *)button{
    //保存条件设置
    [CommonUtils saveLoggingInfo:_myLoggingInfo];
    
    NFCTagHelper *tagInstance = [NFCTagHelper shareInstance];
    tagInstance.delegate = self;
    
    [tagInstance setHexDelayMinutes:_myLoggingInfo.delayMinutes];
    [tagInstance setHexIntervalSeconds:_myLoggingInfo.intervalSeconds];
    [tagInstance setHexLoggingCount:_myLoggingInfo.loggingCount];
    [tagInstance setHexMinTemperature:_myLoggingInfo.minTemperature];
    [tagInstance setHexMaxTemperature:_myLoggingInfo.maxTemperature];
    
    NSString *response = [tagInstance startReadTag:LoggingStartType];
    if(response.length>0){
        [CommonUtils showError:response controller:self onClick:nil];
    }
}

//停止测温
-(void)stopLogging:(UIButton *)button{
    NFCTagHelper *tagInstance = [NFCTagHelper shareInstance];
    tagInstance.delegate = self;
    NSString *response = [tagInstance startReadTag:LoggingStopType];
    if(response.length>0){
        [CommonUtils showError:response controller:self onClick:nil];
    }
}

//读取测温结果
-(void)getLoggingResult:(UIButton *)button{
    NFCTagHelper *tagInstance = [NFCTagHelper shareInstance];
    tagInstance.delegate = self;
    NSString *response = [tagInstance startReadTag:LoggingResultType];
    if(response.length>0){
        [CommonUtils showError:response controller:self onClick:nil];
    }
}

-(void)onDelayClick:(UIGestureRecognizer*)recognizer
{
    __block typeof(self) blockSelf = self;
    QFPickerView *datePickerView = [[QFPickerView alloc]initPickerViewWithArray:blockSelf.delayTextArr backBlock:^(NSInteger index) {
        blockSelf.delayIndex = index;
        blockSelf.delayLabel.text = [blockSelf.delayTextArr objectAtIndex:blockSelf.delayIndex];
        blockSelf.myLoggingInfo.delayMinutes = [blockSelf.delayValueArr objectAtIndex:blockSelf.delayIndex];
    }];
    [datePickerView setSelectData:_delayIndex];
    [datePickerView show];
}

-(void)onIntervalClick:(UIGestureRecognizer*)recognizer
{
    __block typeof(self) blockSelf = self;
    QFPickerView *datePickerView = [[QFPickerView alloc]initPickerViewWithArray:blockSelf.intervalTextArr backBlock:^(NSInteger index) {
        blockSelf.intervalIndex = index;
        blockSelf.intervalLabel.text = [blockSelf.intervalTextArr objectAtIndex:blockSelf.intervalIndex];
        blockSelf.myLoggingInfo.intervalSeconds = [blockSelf.intervalValueArr objectAtIndex:blockSelf.intervalIndex];
    }];
    [datePickerView setSelectData:_intervalIndex];
    [datePickerView show];
}

-(void)onTempCountClick:(UIGestureRecognizer*)recognizer
{
    __block typeof(self) blockSelf = self;
    QFPickerView *datePickerView = [[QFPickerView alloc]initPickerViewWithArray:blockSelf.tempCountTextArr backBlock:^(NSInteger index) {
        blockSelf.tempCountIndex = index;
        blockSelf.tempCountLabel.text = [blockSelf.tempCountTextArr objectAtIndex:blockSelf.tempCountIndex];
        blockSelf.myLoggingInfo.loggingCount = [blockSelf.tempCountValueArr objectAtIndex:blockSelf.tempCountIndex];
    }];
    [datePickerView setSelectData:_tempCountIndex];
    [datePickerView show];
}

-(void)onMinClick:(UIGestureRecognizer*)recognizer
{
    __block typeof(self) blockSelf = self;
    QFPickerView *datePickerView = [[QFPickerView alloc]initPickerViewWithArray:blockSelf.tempMinTextArr backBlock:^(NSInteger index) {
        blockSelf.tempMinIndex = index;
        blockSelf.tempMinLabel.text = [blockSelf.tempMinTextArr objectAtIndex:blockSelf.tempMinIndex];
        blockSelf.myLoggingInfo.minTemperature = [blockSelf.tempMinValueArr objectAtIndex:blockSelf.tempMinIndex];
    }];
    [datePickerView setSelectData:_tempMinIndex];
    [datePickerView show];
}

-(void)onMaxClick:(UIGestureRecognizer*)recognizer
{
    __block typeof(self) blockSelf = self;
    QFPickerView *datePickerView = [[QFPickerView alloc]initPickerViewWithArray:blockSelf.tempMaxTextArr backBlock:^(NSInteger index) {
        blockSelf.tempMaxIndex = index;
        blockSelf.tempMaxLabel.text = [blockSelf.tempMaxTextArr objectAtIndex:blockSelf.tempMaxIndex];
        blockSelf.myLoggingInfo.maxTemperature = [blockSelf.tempMaxValueArr objectAtIndex:blockSelf.tempMaxIndex];
    }];
    [datePickerView setSelectData:_tempMaxIndex];
    [datePickerView show];
}

//NFCTagHelperDelegate
- (void)NfcLoggingComplete:(LoggingMsg *)nfcMsg{
    UIBarButtonItem *backItem = [[UIBarButtonItem alloc]init];
    backItem.title = NSLocalizedString(@"FM_Back", nil);
    self.navigationItem.backBarButtonItem=backItem;
    
    GraphicViewController *childController = [[GraphicViewController alloc] init];
    childController.nfcMsg = nfcMsg;
    childController.hidesBottomBarWhenPushed = YES;  //隐藏tabbar
    [self.navigationController pushViewController:childController animated:YES];
}
@end
