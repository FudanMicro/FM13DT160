//  MeasureViewController.m
//
//  Created by lubozhi on 2019/7/25.
//  Copyright © 2019年 复旦微电子集团股份科技有限公司. All rights reserved.

#import "MeasureViewController.h"
#import "GraphicViewController.h"
#import "ModelButton.h"
#import "NFCTagHelper.h"

@interface MeasureViewController ()

//用于绘制界面的时候记录view底部
@property(nonatomic, assign) CGFloat viewBottom;
//背景view
@property(nonatomic, strong) UIView *bgView;
//场强
@property(nonatomic, strong) UILabel *fieldLabel;
//温度
@property(nonatomic, strong) UILabel *temperatureLabel;
//电压
@property(nonatomic, strong) UILabel *voltageLabel;
//类型
@property(nonatomic, strong) UILabel *typeLabel;
//UID
@property(nonatomic, strong) UILabel *uidLabel;
//操作结果
@property(nonatomic, strong) UILabel *resultLabel;
//指令输入
@property(nonatomic, strong) UITextField *cmdField;
@end

@implementation MeasureViewController

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    self.navigationItem.title=NSLocalizedString(@"FM_Tab_Measurement", nil);
    CGRect statusRect = [[UIApplication sharedApplication] statusBarFrame];
    CGRect navRect = self.navigationController.navigationBar.frame;
    //初始高度=状态栏高度+导航栏高度
    _viewBottom = statusRect.size.height+navRect.size.height;
    _bgView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, DSScreenWidth, DSScreenHeight)];
    _bgView.backgroundColor = DSColor(246, 246, 246);
    [self.view addSubview:_bgView];
    //初始化读取结果视图
    [self initResultView];
    //初始化按钮视图
    [self initButtonView];
    
    UITapGestureRecognizer *tap1 = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(viewTapped:)];
    tap1.cancelsTouchesInView = NO;
    [self.view addGestureRecognizer:tap1];
    
    NSLog(@"sdk版本: %@",[NFCTagHelper getLibVersion]);
}

-(void)viewTapped:(UITapGestureRecognizer*)tap1
{
    [self.view endEditing:YES];
}

//初始化读取结果视图
- (void)initResultView{
    UIFont *normalFont = [UIFont fontWithName:@"PingFangSC-Regular" size:16*DSAdaptCoefficient];
    UILabel *headerLabel = [[UILabel alloc] initWithFrame:CGRectMake(10*DSAdaptCoefficient, _viewBottom, 200*DSAdaptCoefficient, 32*DSAdaptCoefficient)];
    UIColor *labelColor = DSColor(92, 96, 99);
    headerLabel.text = NSLocalizedString(@"FM_Measurement_Result", nil);
    headerLabel.textColor = DSColor(51, 51, 51);
    headerLabel.font = [UIFont fontWithName:@"PingFangSC-Semibold" size:16*DSAdaptCoefficient];
    headerLabel.textAlignment = NSTextAlignmentLeft;
    [_bgView addSubview:headerLabel];
    _viewBottom = CGRectGetMaxY(headerLabel.frame);
    
    //与左边缘距离
    CGFloat spaceWidth = 10*DSAdaptCoefficient;
    //内容容器宽度
    CGFloat contentWidth = DSScreenWidth;
    CGFloat contentHeight = 36*DSAdaptCoefficient;
    //标题宽度
    CGFloat titleWidth = 100*DSAdaptCoefficient;
//    CGFloat arrowWidth = 7*DSAdaptCoefficient;
//    CGFloat arrowHeight = 14*DSAdaptCoefficient;
//    CGFloat arrowYPos = (contentHeight-arrowHeight)/2;
    //arrow距两边的间隔
//    CGFloat arrowSpaceWidth = 5*DSAdaptCoefficient;
    CGFloat xPos = spaceWidth;
    //数值宽度
    CGFloat textWidth = contentWidth - 2*spaceWidth - titleWidth;
    UIView *contentView = [[UIView alloc] initWithFrame:CGRectMake(0,_viewBottom,contentWidth,contentHeight)];
    contentView.backgroundColor = DSCommonColor;
    [_bgView addSubview:contentView];
    _viewBottom = CGRectGetMaxY(contentView.frame)+1*DSAdaptCoefficient;
    
    UILabel *titleLabel = [[UILabel alloc] initWithFrame:CGRectMake(xPos,0,titleWidth,contentHeight)];
    titleLabel.textAlignment = NSTextAlignmentLeft;
    titleLabel.text = NSLocalizedString(@"FM_Field_Strength", nil);
    titleLabel.font = normalFont;
    titleLabel.textColor = labelColor;
    [contentView addSubview:titleLabel];
    
    xPos += titleWidth;
    _fieldLabel = [[UILabel alloc] initWithFrame:CGRectMake(xPos, 0, textWidth, contentHeight)];
    _fieldLabel.textAlignment = NSTextAlignmentRight;
    _fieldLabel.text = @"";
    _fieldLabel.font = normalFont;
    _fieldLabel.textColor = labelColor;
    [contentView addSubview:_fieldLabel];
    
//    xPos = xPos + textWidth + arrowSpaceWidth;
//    //箭头
//    UIImageView *arrowImageView = [[UIImageView alloc] initWithFrame:CGRectMake(xPos, arrowYPos, arrowWidth, arrowHeight)];
//    arrowImageView.image = [UIImage imageNamed:@"arrow_right"];
//    [contentView addSubview:arrowImageView];
    
    xPos = spaceWidth;
    contentView = [[UIView alloc] initWithFrame:CGRectMake(0,_viewBottom,contentWidth,contentHeight)];
    contentView.backgroundColor = DSCommonColor;
    [_bgView addSubview:contentView];
    _viewBottom = CGRectGetMaxY(contentView.frame)+1*DSAdaptCoefficient;
    
    titleLabel = [[UILabel alloc] initWithFrame:CGRectMake(xPos,0,titleWidth,contentHeight)];
    titleLabel.textAlignment = NSTextAlignmentLeft;
    titleLabel.text = NSLocalizedString(@"FM_Temperature", nil);
    titleLabel.font = normalFont;
    titleLabel.textColor = labelColor;
    [contentView addSubview:titleLabel];
    
    xPos += titleWidth;
    _temperatureLabel = [[UILabel alloc] initWithFrame:CGRectMake(xPos, 0, textWidth, contentHeight)];
    _temperatureLabel.textAlignment = NSTextAlignmentRight;
    _temperatureLabel.text = @"";
    _temperatureLabel.font = normalFont;
    _temperatureLabel.textColor = labelColor;
    [contentView addSubview:_temperatureLabel];
    
    xPos = spaceWidth;
    contentView = [[UIView alloc] initWithFrame:CGRectMake(0,_viewBottom,contentWidth,contentHeight)];
    contentView.backgroundColor = DSCommonColor;
    [_bgView addSubview:contentView];
    _viewBottom = CGRectGetMaxY(contentView.frame)+1*DSAdaptCoefficient;
    
    titleLabel = [[UILabel alloc] initWithFrame:CGRectMake(xPos,0,titleWidth,contentHeight)];
    titleLabel.textAlignment = NSTextAlignmentLeft;
    titleLabel.text = NSLocalizedString(@"FM_Voltage", nil);
    titleLabel.font = normalFont;
    titleLabel.textColor = labelColor;
    [contentView addSubview:titleLabel];
    
    xPos += titleWidth;
    _voltageLabel = [[UILabel alloc] initWithFrame:CGRectMake(xPos, 0, textWidth, contentHeight)];
    _voltageLabel.textAlignment = NSTextAlignmentRight;
    _voltageLabel.text = @"";
    _voltageLabel.font = normalFont;
    _voltageLabel.textColor = labelColor;
    [contentView addSubview:_voltageLabel];
    
    xPos = spaceWidth;
    contentView = [[UIView alloc] initWithFrame:CGRectMake(0,_viewBottom,contentWidth,contentHeight)];
    contentView.backgroundColor = DSCommonColor;
    [_bgView addSubview:contentView];
    _viewBottom = CGRectGetMaxY(contentView.frame)+1*DSAdaptCoefficient;
    
    titleLabel = [[UILabel alloc] initWithFrame:CGRectMake(xPos,0,titleWidth,contentHeight)];
    titleLabel.textAlignment = NSTextAlignmentLeft;
    titleLabel.text = @"UID";
    titleLabel.font = normalFont;
    titleLabel.textColor = labelColor;
    [contentView addSubview:titleLabel];
    
    xPos += titleWidth;
    _uidLabel = [[UILabel alloc] initWithFrame:CGRectMake(xPos, 0, textWidth, contentHeight)];
    _uidLabel.textAlignment = NSTextAlignmentRight;
    _uidLabel.text = @"";
    _uidLabel.font = normalFont;
    _uidLabel.textColor = labelColor;
    [contentView addSubview:_uidLabel];
    
    xPos = spaceWidth;
    contentView = [[UIView alloc] initWithFrame:CGRectMake(0,_viewBottom,contentWidth,contentHeight)];
    contentView.backgroundColor = DSCommonColor;
    [_bgView addSubview:contentView];
    _viewBottom = CGRectGetMaxY(contentView.frame)+1*DSAdaptCoefficient;
    
    titleLabel = [[UILabel alloc] initWithFrame:CGRectMake(xPos,0,titleWidth,contentHeight)];
    titleLabel.textAlignment = NSTextAlignmentLeft;
    titleLabel.text = NSLocalizedString(@"FM_Type", nil);
    titleLabel.font = normalFont;
    titleLabel.textColor = labelColor;
    [contentView addSubview:titleLabel];
    
    xPos += titleWidth;
    _typeLabel = [[UILabel alloc] initWithFrame:CGRectMake(xPos, 0, textWidth, contentHeight)];
    _typeLabel.textAlignment = NSTextAlignmentRight;
    _typeLabel.text = @"";
    _typeLabel.font = normalFont;
    _typeLabel.textColor = labelColor;
    [contentView addSubview:_typeLabel];
    
    xPos = spaceWidth;
    contentView = [[UIView alloc] initWithFrame:CGRectMake(0,_viewBottom,contentWidth,contentHeight)];
    contentView.backgroundColor = DSCommonColor;
    [_bgView addSubview:contentView];
    _viewBottom = CGRectGetMaxY(contentView.frame)+10*DSAdaptCoefficient;
    
    titleLabel = [[UILabel alloc] initWithFrame:CGRectMake(xPos,0,titleWidth,contentHeight)];
    titleLabel.textAlignment = NSTextAlignmentLeft;
    titleLabel.text = NSLocalizedString(@"FM_Response", nil);
    titleLabel.font = normalFont;
    titleLabel.textColor = labelColor;
    [contentView addSubview:titleLabel];
    
    xPos += titleWidth;
    _resultLabel = [[UILabel alloc] initWithFrame:CGRectMake(xPos, 0, textWidth, contentHeight)];
    _resultLabel.textAlignment = NSTextAlignmentRight;
    _resultLabel.text = @"";
    _resultLabel.font = normalFont;
    _resultLabel.textColor = labelColor;
    [contentView addSubview:_resultLabel];
    
    _cmdField = [[UITextField alloc]initWithFrame:CGRectMake(spaceWidth, _viewBottom, DSScreenWidth-2*spaceWidth, contentHeight)];
    _cmdField.placeholder = NSLocalizedString(@"FM_Input_Instruction", nil);
    _cmdField.layer.borderWidth = 1*DSAdaptCoefficient;
    _cmdField.layer.borderColor = DSColor(223, 223, 223).CGColor;
    _cmdField.clearButtonMode = UITextFieldViewModeWhileEditing;
    _cmdField.autocapitalizationType = UITextAutocapitalizationTypeNone; //首字母是否大写
    _cmdField.font = normalFont;
    [_bgView addSubview:_cmdField];
    _viewBottom = CGRectGetMaxY(_cmdField.frame)+10*DSAdaptCoefficient;
}

//初始化按钮视图
- (void)initButtonView {
    UIFont *normalFont = [UIFont fontWithName:@"PingFangSC-Regular" size:16*DSAdaptCoefficient];
    NSMutableArray *titleArray = [[NSMutableArray alloc]init];
    NSMutableArray *iconArray = [[NSMutableArray alloc]init];
    
    [titleArray addObject:NSLocalizedString(@"FM_CMD_Measurement", nil)];
    [iconArray addObject:@"0"];
    
    [titleArray addObject:NSLocalizedString(@"FM_Check_Status", nil)];
    [iconArray addObject:@"1"];
    
    [titleArray addObject:NSLocalizedString(@"FM_Sleep", nil)];
    [iconArray addObject:@"4"];
    
    [titleArray addObject:NSLocalizedString(@"FM_Wakeup", nil)];
    [iconArray addObject:@"2"];
    
    [titleArray addObject:NSLocalizedString(@"FM_UHF_Initialization", nil)];
    [iconArray addObject:@"3"];
    
    [titleArray addObject:NSLocalizedString(@"FM_Cuscom_Send", nil)];
    [iconArray addObject:@"1"];
    
//    UILabel *btnTitleLabel = [[UILabel alloc] initWithFrame:CGRectMake(12*DSAdaptCoefficient, _viewBottom, 100*DSAdaptCoefficient, 20*DSAdaptCoefficient)];
//    btnTitleLabel.text = @"";
//    btnTitleLabel.textColor = DSColor(51, 51, 51);
//    btnTitleLabel.font = normalFont;
//    btnTitleLabel.textAlignment = NSTextAlignmentLeft;
//    [_bgView addSubview:btnTitleLabel];
    
    //列数
    NSInteger columnNum = 3;
    //行数
    NSInteger rowNum = titleArray.count%columnNum==0?(titleArray.count/columnNum):(titleArray.count/columnNum+1);
    //按钮宽度
    CGFloat applyButtonWidth = DSScreenWidth/columnNum;
    //按钮高度
    CGFloat applyButtonHeight = DSScreenWidth/columnNum;

    UIView *buttonView = [[UIView alloc] initWithFrame:CGRectMake(0, _viewBottom, DSScreenWidth, applyButtonHeight * rowNum)];
    buttonView.backgroundColor = DSCommonColor;
    [_bgView addSubview:buttonView];
    
    for (int i=0; i<rowNum; i++) {
        for (int j=0; j<columnNum; j++) {
            if (titleArray.count>=columnNum*i+j+1) {
                ModelButton *button = [ModelButton buttonWithType:UIButtonTypeCustom];
                [button setFrame:CGRectMake(applyButtonWidth*j, applyButtonHeight*i, applyButtonWidth, applyButtonHeight)];
                [button setButtomImage:[NSString stringWithFormat:@"button_%@",iconArray[columnNum*i+j]] Title:titleArray[columnNum*i+j]];
                [button addTarget:self action:@selector(applyButtonClick:) forControlEvents:UIControlEventTouchUpInside];
                [buttonView addSubview:button];
            }
            else
                break;
        }
    }
    
    //分隔线
    if(rowNum > 1){
        UIView *lineView1 = [[UIView alloc] initWithFrame:CGRectMake(0, DSScreenWidth/columnNum, CGRectGetWidth(buttonView.frame), 1)];
        lineView1.backgroundColor = DSColor(241, 239, 239);
        [buttonView addSubview:lineView1];
    }
    
    UIView *lineView2 = [[UIView alloc] initWithFrame:CGRectMake(DSScreenWidth/columnNum, 0, 1, DSScreenWidth/columnNum*rowNum)];
    lineView2.backgroundColor = DSColor(241, 239, 239);
    [buttonView addSubview:lineView2];
    
    UIView *lineView3 = [[UIView alloc] initWithFrame:CGRectMake(DSScreenWidth/columnNum*2, 0, 1, DSScreenWidth/columnNum*rowNum)];
    lineView3.backgroundColor = DSColor(241, 239, 239);
    [buttonView addSubview:lineView3];

    _viewBottom = CGRectGetMaxY(buttonView.frame);
}

- (void)applyButtonClick:(ModelButton *)button {
    _typeLabel.text = @"";
    _uidLabel.text = @"";
    _fieldLabel.text = @"";
    _temperatureLabel.text = @"";
    _voltageLabel.text = @"";
    _resultLabel.text = @"";
    
    __block typeof(self) blockSelf = self;
    NSString *btnText = button.buttonLabel.text;
    //基础测量
    if ([btnText isEqualToString:NSLocalizedString(@"FM_CMD_Measurement", nil)]) {
        [[NFCTagHelper shareInstance] getBasicData:^(MeasureMsg *resultData){
            blockSelf.typeLabel.text = resultData.tagType;
            blockSelf.uidLabel.text = resultData.uid;
            if(resultData.isSuccess){
                blockSelf.fieldLabel.text = resultData.fieldValue;
                blockSelf.temperatureLabel.text = resultData.tempValue;
                blockSelf.voltageLabel.text = resultData.voltageValue;
                
                blockSelf.resultLabel.text = NSLocalizedString(@"FM_Success", nil);
            }
            else{
                blockSelf.resultLabel.text = resultData.message;
            }
        }];
        return;
    }
    //检测是否唤醒
    else if ([btnText isEqualToString:NSLocalizedString(@"FM_Check_Status", nil)]) {
        [[NFCTagHelper shareInstance] checkWakeUp:^(MeasureMsg *resultData){
            blockSelf.typeLabel.text = resultData.tagType;
            blockSelf.uidLabel.text = resultData.uid;
            if(resultData.isSuccess){
                if(resultData.isWakeup){
                    blockSelf.resultLabel.text = NSLocalizedString(@"FM_Wakeup", nil);
                }
                else{
                    blockSelf.resultLabel.text = NSLocalizedString(@"FM_Sleep", nil);
                }
            }
            else{
                blockSelf.resultLabel.text = resultData.message;
            }
        }];
    }
    //休眠
    else if ([btnText isEqualToString:NSLocalizedString(@"FM_Sleep", nil)]) {
        [[NFCTagHelper shareInstance] doSleep:^(MeasureMsg *resultData){
            blockSelf.typeLabel.text = resultData.tagType;
            blockSelf.uidLabel.text = resultData.uid;
            if(resultData.isSuccess){
                blockSelf.resultLabel.text = NSLocalizedString(@"FM_Success", nil);
            }
            else{
                blockSelf.resultLabel.text = resultData.message;
            }
        }];
    }
    //高频初始化
    else if ([btnText isEqualToString:NSLocalizedString(@"FM_UHF_Initialization", nil)]) {
        [[NFCTagHelper shareInstance] initUHF:^(MeasureMsg *resultData){
            blockSelf.typeLabel.text = resultData.tagType;
            blockSelf.uidLabel.text = resultData.uid;
            if(resultData.isSuccess){
                blockSelf.resultLabel.text = NSLocalizedString(@"FM_Success", nil);
            }
            else{
                blockSelf.resultLabel.text = resultData.message;
            }
        }];
    }
    //唤醒
    else if ([btnText isEqualToString:NSLocalizedString(@"FM_Wakeup", nil)]) {
        [[NFCTagHelper shareInstance] doWakeup:^(MeasureMsg *resultData){
            blockSelf.typeLabel.text = resultData.tagType;
            blockSelf.uidLabel.text = resultData.uid;
            if(resultData.isSuccess){
                blockSelf.resultLabel.text = NSLocalizedString(@"FM_Success", nil);
            }
            else{
                blockSelf.resultLabel.text = resultData.message;
            }
        }];
    }
    //40B3B040030000DC2329D6
    //自定义指令
    else if ([btnText isEqualToString:NSLocalizedString(@"FM_Cuscom_Send", nil)]) {
        NSString *cmdStr = [_cmdField.text stringByReplacingOccurrencesOfString:@" " withString:@""];
        if(cmdStr&&cmdStr.length>0){
           [[NFCTagHelper shareInstance] sendInstruct:cmdStr onComplete:^(MeasureMsg *resultData){
               blockSelf.typeLabel.text = resultData.tagType;
               blockSelf.uidLabel.text = resultData.uid;
               blockSelf.resultLabel.text = resultData.message;
           }];
        }
        else{
           [Commons showError:NSLocalizedString(@"FM_Input_Instruction", nil) controller:self onClick:nil];
        }
    }
}

-(void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
}
-(void)viewDidDisappear:(BOOL)animated
{
    [super viewDidDisappear:animated];
}

- (void)getUIDComplete:(MeasureMsg *)resultMsg{
    
}

@end
