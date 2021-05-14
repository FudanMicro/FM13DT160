//
//  GraphicViewController.m
//  FMTemperature
//
//  Created by gaolailong on 2018/7/11.
//  Copyright © 2018年 复旦微电子集团股份科技有限公司. All rights reserved.
//

#import "GraphicViewController.h"
#import <MessageUI/MessageUI.h>

@interface GraphicViewController ()<MFMailComposeViewControllerDelegate,UIScrollViewDelegate>

//用于绘制界面的时候记录view底部
@property(nonatomic, assign) CGFloat viewBottom;
//按钮高度
@property(nonatomic, assign) CGFloat btnHeight;
//背景view
@property(nonatomic, strong) UIScrollView *bgView;
//图表
@property (nonatomic, strong) UIWebView *temperatureDataWeb;

@end

@implementation GraphicViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.navigationItem.title=NSLocalizedString(@"FM_Data_Details", nil);
    CGRect statusRect = [[UIApplication sharedApplication] statusBarFrame];
    CGRect navRect = self.navigationController.navigationBar.frame;
    //初始高度=状态栏高度+导航栏高度
    _viewBottom = statusRect.size.height+navRect.size.height;
    _btnHeight = 50*DSAdaptCoefficient;
    //创建滚动视图
    _bgView = [[UIScrollView alloc] initWithFrame:CGRectMake(0, 0, DSScreenWidth, DSScreenHeight - _btnHeight - 30*DSAdaptCoefficient)];
    _bgView.showsVerticalScrollIndicator = NO;
    _bgView.delegate = self;
    _bgView.backgroundColor = DSColor(246, 246, 246);
    [self.view addSubview:_bgView];
    self.view.backgroundColor = DSColor(246, 246, 246);
    
    //初始化图表信息
    [self initChartView];
    //初始化概要信息
    [self initSummaryView];
    //初始化底部按钮
    [self initButtonView];
    
    _bgView.contentSize = CGSizeMake(DSScreenWidth, _viewBottom);
}

//初始化概要信息
- (void)initSummaryView{
    UIFont *normalFont = [UIFont fontWithName:@"PingFangSC-Regular" size:14*DSAdaptCoefficient];
    UILabel *headerLabel = [[UILabel alloc] initWithFrame:CGRectMake(5*DSAdaptCoefficient, _viewBottom, 200*DSAdaptCoefficient, 26*DSAdaptCoefficient)];
    UIColor *labelColor = DSColor(92, 96, 99);
    headerLabel.text = NSLocalizedString(@"FM_Summary", nil);
    headerLabel.textColor = DSColor(51, 51, 51);
    headerLabel.font = [UIFont fontWithName:@"PingFangSC-Semibold" size:14*DSAdaptCoefficient];
    headerLabel.textAlignment = NSTextAlignmentLeft;
    [_bgView addSubview:headerLabel];
    _viewBottom = CGRectGetMaxY(headerLabel.frame);
    
    //与左边缘距离
    CGFloat spaceWidth = 5*DSAdaptCoefficient;
    //内容容器宽度
    CGFloat contentWidth = DSScreenWidth;
    CGFloat contentHeight = 32*DSAdaptCoefficient;
    //文字宽度
    CGFloat textWidth = (contentWidth - 2*spaceWidth)/2;
    CGFloat xPos = spaceWidth;
    UIView *contentView = [[UIView alloc] initWithFrame:CGRectMake(0,_viewBottom,contentWidth,contentHeight)];
    contentView.backgroundColor = DSCommonColor;
    [_bgView addSubview:contentView];
    _viewBottom = CGRectGetMaxY(contentView.frame)+1*DSAdaptCoefficient;
    
    UILabel *leftLabel = [[UILabel alloc] initWithFrame:CGRectMake(xPos,0,textWidth,contentHeight)];
    leftLabel.textAlignment = NSTextAlignmentLeft;
    leftLabel.text = [NSString stringWithFormat:@"UID:%@", _nfcMsg.uid];
    leftLabel.font = normalFont;
    leftLabel.textColor = labelColor;
    [contentView addSubview:leftLabel];
    
    xPos += textWidth;
    UILabel *rightLabel = [[UILabel alloc] initWithFrame:CGRectMake(xPos,0,textWidth,contentHeight)];
    rightLabel.textAlignment = NSTextAlignmentLeft;
    rightLabel.text = [NSString stringWithFormat:@"%@: %@", NSLocalizedString(@"FM_Type", nil), _nfcMsg.tagType];
    rightLabel.font = normalFont;
    rightLabel.textColor = labelColor;
    [contentView addSubview:rightLabel];
    
    contentView = [[UIView alloc] initWithFrame:CGRectMake(0,_viewBottom,contentWidth,contentHeight)];
    contentView.backgroundColor = DSCommonColor;
    [_bgView addSubview:contentView];
    _viewBottom = CGRectGetMaxY(contentView.frame)+1*DSAdaptCoefficient;
    
    xPos = spaceWidth;
    leftLabel = [[UILabel alloc] initWithFrame:CGRectMake(xPos,0,textWidth,contentHeight)];
    leftLabel.textAlignment = NSTextAlignmentLeft;
    NSString *tmpStr;
    if(_nfcMsg.opStatus==STATUS_LOGGING){
        tmpStr = NSLocalizedString(@"FM_Ongoing", nil);
    }
    else if(_nfcMsg.opStatus==STATUS_WAITING){
        tmpStr = NSLocalizedString(@"FM_Waiting", nil);
    }
    else if(_nfcMsg.opStatus==STATUS_ERROR){
        tmpStr = NSLocalizedString(@"FM_Error_Status", nil);
    }
    else{
        tmpStr = NSLocalizedString(@"FM_Finished", nil);
    }
    leftLabel.text = [NSString stringWithFormat:@"%@: %@", NSLocalizedString(@"FM_Status", nil), tmpStr];
    leftLabel.font = normalFont;
    leftLabel.textColor = labelColor;
    [contentView addSubview:leftLabel];
    
    xPos += textWidth;
    rightLabel = [[UILabel alloc] initWithFrame:CGRectMake(xPos,0,textWidth,contentHeight)];
    rightLabel.textAlignment = NSTextAlignmentLeft;
    rightLabel.text = [NSString stringWithFormat:@"%@: %lds", NSLocalizedString(@"FM_Time_Interval", nil),_nfcMsg.intervalSeconds];
    rightLabel.font = normalFont;
    rightLabel.textColor = labelColor;
    [contentView addSubview:rightLabel];
    
    contentView = [[UIView alloc] initWithFrame:CGRectMake(0,_viewBottom,contentWidth,contentHeight)];
    contentView.backgroundColor = DSCommonColor;
    [_bgView addSubview:contentView];
    _viewBottom = CGRectGetMaxY(contentView.frame)+1*DSAdaptCoefficient;
    
    xPos = spaceWidth;
    leftLabel = [[UILabel alloc] initWithFrame:CGRectMake(xPos,0,contentWidth-2*spaceWidth,contentHeight)];
    leftLabel.textAlignment = NSTextAlignmentLeft;
    leftLabel.text = [NSString stringWithFormat:@"%@: %@", NSLocalizedString(@"FM_Start_Time", nil), [Commons timestampSwitchTime:_nfcMsg.startTime andFormatter:@"YYYY-MM-dd HH:mm:ss"]];
    leftLabel.font = normalFont;
    leftLabel.textColor = labelColor;
    [contentView addSubview:leftLabel];
    
    contentView = [[UIView alloc] initWithFrame:CGRectMake(0,_viewBottom,contentWidth,contentHeight)];
    contentView.backgroundColor = DSCommonColor;
    [_bgView addSubview:contentView];
    _viewBottom = CGRectGetMaxY(contentView.frame)+1*DSAdaptCoefficient;
    
    xPos = spaceWidth;
    leftLabel = [[UILabel alloc] initWithFrame:CGRectMake(xPos,0,contentWidth-2*spaceWidth,contentHeight)];
    leftLabel.textAlignment = NSTextAlignmentLeft;
    leftLabel.text = [NSString stringWithFormat:@"%@: %ld/%ld",NSLocalizedString(@"FM_Logging_Points", nil),_nfcMsg.recordedCount,_nfcMsg.totalCount];
    leftLabel.font = normalFont;
    leftLabel.textColor = labelColor;
    [contentView addSubview:leftLabel];
    
    contentView = [[UIView alloc] initWithFrame:CGRectMake(0,_viewBottom,contentWidth,contentHeight)];
    contentView.backgroundColor = DSCommonColor;
    [_bgView addSubview:contentView];
    _viewBottom = CGRectGetMaxY(contentView.frame)+1*DSAdaptCoefficient;
    
    xPos = spaceWidth;
    leftLabel = [[UILabel alloc] initWithFrame:CGRectMake(xPos,0,contentWidth-2*spaceWidth,contentHeight)];
    leftLabel.textAlignment = NSTextAlignmentLeft;
    leftLabel.text = [NSString stringWithFormat:@"%@: [%.0f°C/%.0f°C]",NSLocalizedString(@"FM_Limited_Temperature_Range", nil),_nfcMsg.validMinimum,_nfcMsg.validMaximum];
    leftLabel.font = normalFont;
    leftLabel.textColor = labelColor;
    [contentView addSubview:leftLabel];
    
    contentView = [[UIView alloc] initWithFrame:CGRectMake(0,_viewBottom,contentWidth,contentHeight)];
    contentView.backgroundColor = DSCommonColor;
    [_bgView addSubview:contentView];
    _viewBottom = CGRectGetMaxY(contentView.frame)+1*DSAdaptCoefficient;
    
    xPos = spaceWidth;
    leftLabel = [[UILabel alloc] initWithFrame:CGRectMake(xPos,0,contentWidth-2*spaceWidth,contentHeight)];
    leftLabel.textAlignment = NSTextAlignmentLeft;
    leftLabel.text = [NSString stringWithFormat:@"%@: %.3f°C", NSLocalizedString(@"FM_Minimum_Temperature",nil),_nfcMsg.recordedMinimum];
    leftLabel.font = normalFont;
    leftLabel.textColor = labelColor;
    [contentView addSubview:leftLabel];
    
    contentView = [[UIView alloc] initWithFrame:CGRectMake(0,_viewBottom,contentWidth,contentHeight)];
    contentView.backgroundColor = DSCommonColor;
    [_bgView addSubview:contentView];
    _viewBottom = CGRectGetMaxY(contentView.frame)+1*DSAdaptCoefficient;
    
    xPos = spaceWidth;
    leftLabel = [[UILabel alloc] initWithFrame:CGRectMake(xPos,0,contentWidth-2*spaceWidth,contentHeight)];
    leftLabel.textAlignment = NSTextAlignmentLeft;
    leftLabel.text = [NSString stringWithFormat:@"%@: %.3f°C", NSLocalizedString(@"FM_Maximum_Temperature",nil),_nfcMsg.recordedMaximum];
    leftLabel.font = normalFont;
    leftLabel.textColor = labelColor;
    [contentView addSubview:leftLabel];
    
    contentView = [[UIView alloc] initWithFrame:CGRectMake(0,_viewBottom,contentWidth,contentHeight)];
    contentView.backgroundColor = DSCommonColor;
    [_bgView addSubview:contentView];
    _viewBottom = CGRectGetMaxY(contentView.frame)+1*DSAdaptCoefficient;
    
    xPos = spaceWidth;
    leftLabel = [[UILabel alloc] initWithFrame:CGRectMake(xPos,0,contentWidth-2*spaceWidth,contentHeight)];
    leftLabel.textAlignment = NSTextAlignmentLeft;
    leftLabel.text = [NSString stringWithFormat:@"%@: %ld", NSLocalizedString(@"FM_Number_Of_Over_Low_Threshold",nil),_nfcMsg.overLowCount];
    leftLabel.font = normalFont;
    leftLabel.textColor = labelColor;
    [contentView addSubview:leftLabel];
    
    contentView = [[UIView alloc] initWithFrame:CGRectMake(0,_viewBottom,contentWidth,contentHeight)];
    contentView.backgroundColor = DSCommonColor;
    [_bgView addSubview:contentView];
    _viewBottom = CGRectGetMaxY(contentView.frame)+1*DSAdaptCoefficient;
    
    xPos = spaceWidth;
    leftLabel = [[UILabel alloc] initWithFrame:CGRectMake(xPos,0,contentWidth-2*spaceWidth,contentHeight)];
    leftLabel.textAlignment = NSTextAlignmentLeft;
    leftLabel.text = [NSString stringWithFormat:@"%@: %ld", NSLocalizedString(@"FM_Number_Of_Over_High_Threshold",nil),_nfcMsg.overHighCount];
    leftLabel.font = normalFont;
    leftLabel.textColor = labelColor;
    [contentView addSubview:leftLabel];
}

//初始化底部按钮
- (void)initButtonView{
    CGFloat btnSpacing = 27*DSAdaptCoefficient;//距离两边间距
    
    UIButton *btn = [[UIButton alloc]initWithFrame:CGRectMake(btnSpacing, DSScreenHeight - _btnHeight - 30*DSAdaptCoefficient, DSScreenWidth - 2*btnSpacing, _btnHeight)];
    btn.layer.borderWidth = 1*DSAdaptCoefficient;
    btn.layer.cornerRadius = 5*DSAdaptCoefficient;
    btn.titleLabel.font = [UIFont fontWithName:@"PingFangSC-Regular" size:18*DSAdaptCoefficient];
    btn.backgroundColor = DSBlueColor;
    btn.layer.borderColor = DSBlueColor.CGColor;
    [btn setTitleColor:DSCommonColor forState:(UIControlStateNormal)];
    [btn setTitle:NSLocalizedString(@"FM_Send_Mail", nil) forState:UIControlStateNormal];
    [btn addTarget:self action:@selector(doSendMail:) forControlEvents:UIControlEventTouchUpInside];
    [self.view addSubview:btn];
}

//读取测温结果
-(void)doSendMail:(UIButton *)button{
    Class mailClass = (NSClassFromString(@"MFMailComposeViewController"));
    if (mailClass != nil) {
        if ([mailClass canSendMail]) {
            MFMailComposeViewController *emailVC = [[MFMailComposeViewController alloc] init];
            emailVC.mailComposeDelegate = self;
            //设置主题
            [emailVC setSubject:@"FMSH Temperature export data"];
            //设置收件人
            NSArray *receviedUsers = [NSArray arrayWithObjects:@"", nil];
            [emailVC setToRecipients:receviedUsers];
            
            NSMutableString *mailContent = [NSMutableString stringWithCapacity:0];
            [mailContent appendFormat:@"[summary]\r\n"];
            [mailContent appendFormat:@"UID=%@\r\n",_nfcMsg.uid];
            [mailContent appendFormat:@"tagType=%@\r\n",_nfcMsg.tagType];
            NSString *tmpStr;
            if(_nfcMsg.opStatus==STATUS_LOGGING){
                tmpStr = @"true";
            }
            else{
                tmpStr = @"false";
            }
            [mailContent appendFormat:@"isLogging=%@\r\n",tmpStr];
            [mailContent appendFormat:@"recordedCount=%ld\r\n",_nfcMsg.recordedCount];
            [mailContent appendFormat:@"totalCount=%ld\r\n",_nfcMsg.totalCount];
            tmpStr = [Commons timestampSwitchTime:_nfcMsg.startTime andFormatter:@"YYYY-MM-dd HH:mm:ss"];
            [mailContent appendFormat:@"startTime=%@\r\n",tmpStr];
            [mailContent appendFormat:@"intervalSeconds=%ld\r\n",_nfcMsg.intervalSeconds];
            [mailContent appendFormat:@"recordedMinimum=%.3f\r\n",_nfcMsg.recordedMinimum];
            [mailContent appendFormat:@"recordedMaximum=%.3f\r\n",_nfcMsg.recordedMaximum];
            [mailContent appendFormat:@"validMinimum=%.3f\r\n",_nfcMsg.validMinimum];
            [mailContent appendFormat:@"validMaximum=%.3f\r\n",_nfcMsg.validMaximum];
            [mailContent appendFormat:@"Number_Of_Over_Low_Threshold=%ld\r\n",_nfcMsg.overLowCount];
            [mailContent appendFormat:@"Number_Of_Over_High_Threshold=%ld\r\n",_nfcMsg.overHighCount];
            
            [mailContent appendFormat:@"[detials]\r\n"];
            for(TempDetail *item in _nfcMsg.temperaturesArray){
                [mailContent appendFormat:@"data=%@;%@;%@;%@\r\n", item.tempID,item.hexTemp,item.decimalTemp,item.opTime];
            }
            
            NSData *mailData = [mailContent dataUsingEncoding:NSUTF8StringEncoding];
            [emailVC addAttachmentData:mailData mimeType:@"text/plain" fileName:@"FMSH TemperatureData.txt"];
            
            [self presentViewController:emailVC animated:YES completion:nil];
        }
    }
}

//初始化图表信息
- (void)initChartView{
    _temperatureDataWeb = [[UIWebView alloc] initWithFrame:CGRectMake(0, _viewBottom, DSScreenWidth, 300*DSAdaptCoefficient)];
    _temperatureDataWeb.scrollView.scrollEnabled = NO;
    [_temperatureDataWeb setScalesPageToFit:NO];
    NSURL * baseURL = [NSURL fileURLWithPath:[NSString stringWithFormat:@"%@", [[NSBundle mainBundle] bundlePath]]];
    if (_nfcMsg) {
        if (_nfcMsg.temperaturesArray.count > 0) {
            NSString * webContent = [self getChartRepresentationOfTempData];
            [self.temperatureDataWeb loadHTMLString:webContent baseURL:baseURL];
        }
        else{
            [self.temperatureDataWeb loadHTMLString:@"" baseURL:baseURL];
        }
    }else {
        [self.temperatureDataWeb loadHTMLString:@"" baseURL:baseURL];
    }
    
    [_bgView addSubview:_temperatureDataWeb];
    _viewBottom = CGRectGetMaxY(_temperatureDataWeb.frame);
}

- (void)mailComposeController:(MFMailComposeViewController *)controller didFinishWithResult:(MFMailComposeResult)result error:(nullable NSError *)error {
    //关闭邮件发送窗口
    [self dismissViewControllerAnimated:YES completion:NULL];
    NSString *msg;
    switch (result) {     // result是枚举类型
        case MFMailComposeResultCancelled:
            msg = @"用户取消编辑邮件";
            break;
        case MFMailComposeResultSaved:
            msg = @"用户成功保存邮件";
            break;
        case MFMailComposeResultSent:
            msg = @"用户点击发送，将邮件放到队列中，还没发送";
            break;
        case MFMailComposeResultFailed:
            msg = @"邮件发送失败";
            break;
        default:
            msg = @"";
            break;
    }
    NSLog(@"%@",msg);
    if(result==MFMailComposeResultFailed){
        [Commons showError:@"邮件发送失败" controller:self onClick:nil];
    }
}

- (NSMutableString *) getChartRepresentationOfTempData {

    NSMutableString * content = [[NSMutableString alloc] initWithString:@""];

    [content appendString:@"<html>"];
    [content appendString:@"<head>"];
    [content appendString:@"<title>Value count of NHS 3100 Temerature Logger</title>"];

    NSString * csFusionPath = [[NSBundle mainBundle]pathForResource:@"fusioncharts" ofType:@"js" inDirectory:@""];
    [content appendFormat:@"<script type='text/javascript' src='%@'></script>", csFusionPath];

    [content appendString:@"</head>"];
    [content appendString:@"<body style=\"background-color: TRANSPARENT;\">"];
    [content appendString:@"<div id=\"chartContainer\" style=\"margin:-8px; \">Range will be displayed here.</div>"];
    [content appendString:@"<script type=\"text/javascript\">"];

    [content appendString:@"var revenueChart = new FusionCharts({"
     "type: 'area2d',"
     "renderAt: 'chartContainer',"];

    [content appendFormat:@"\"width\": '%f',", self.temperatureDataWeb.frame.size.width];
    //[content appendFormat:@"\"height\": '%f',", self.temperatureDataWeb.frame.size.height];
    [content appendString:@"dataFormat: 'json',"
     "dataSource: {"
     "\"chart\": {"
     "\"caption\": \"Recorded Temperature Values\","];
    
    if (_nfcMsg.temperaturesArray.count > 0) {
        NSString *csStr = [NSString stringWithFormat:@"%lu values",_nfcMsg.temperaturesArray.count];
        [content appendFormat:@"\"xAxisName\": \"%@\",", csStr];
    } else {
        [content appendString:@"\"xAxisName\": \"Values recorded over time\","];
    }

    [content appendFormat:@"\"yAxisName\": \"Temperature  °C\","];

    int iMin = (int)self.nfcMsg.recordedMinimum;
    int iMax = (int)self.nfcMsg.recordedMaximum;
    float fMin = self.nfcMsg.validMinimum;
    float fMax = self.nfcMsg.validMaximum;
    
    if (fMin == 0 && fMax == 0) {
        fMin = iMin - 5;
        fMax = iMax + 5;
    }

    float fMinLimit = -40.0;
    float fMaxLimit = 85.0;

    if (fMin == 0 && fMax == 0) {
        if (self.nfcMsg.recordedMinimum != 0)
            fMinLimit = self.nfcMsg.recordedMinimum - 5.0;
        if (self.nfcMsg.recordedMaximum != 0) {
            fMaxLimit = self.nfcMsg.recordedMaximum + 5.0;
        }
    } else {
//        if (self.nfcMsg.recordedMinimum != 0 && self.nfcMsg.recordedMinimum > fMin)
            fMinLimit = self.nfcMsg.recordedMinimum - 2.0;
//        else
//            fMinLimit = fMin - 5.0;

//        if (self.nfcMsg.recordedMaximum != 0 && self.nfcMsg.recordedMaximum < fMax)
            fMaxLimit = self.nfcMsg.recordedMaximum + 2.0;
//        else
//            fMaxLimit = fMax + 5.0;
    }

//    if (iMax > fMaxLimit) {
//        fMaxLimit = iMax;
//    }
//
//    if (iMin < fMinLimit) {
//        fMinLimit = iMin;
//    }
//
//    if (fMaxLimit > 85)
//        fMaxLimit = 85.0;
//
//    if (fMinLimit < -40)
//        fMinLimit = -40.0;

    [content appendString:@"\"numberPrefix\": \"\","
     "\"paletteColors\": \"#0075c2\","
     "\"plotFillAlpha\":\"75\","
     "\"bgColor\": \"#ffffff\","
     "\"borderAlpha\": \"30\","
     "\"canvasBorderAlpha\": \"0\","
     "\"usePlotGradientColor\": \"0\","
     "\"plotBorderAlpha\": \"50\","
     "\"placevaluesInside\": \"1\","
     "\"rotatevalues\": \"1\","
     "\"valueFontColor\": \"#ffffff\","
     "\"showXAxisLine\": \"1\","
     "\"xAxisLineColor\": \"#999999\","
     "\"divlineColor\": \"#999999\","
     "\"divLineIsDashed\": \"1\","
     "\"showAlternateHGridColor\": \"0\","
     "\"subcaptionFontBold\": \"0\","
     "\"subcaptionFontSize\": \"14\","
     "\"usePlotGradientColor\":\"1\","
     "\"showValues\":\"0\","];
    [content appendFormat:@"\"yAxisMinValue\": \"%d\",", (int)fMinLimit];
    [content appendFormat:@"\"yAxisMaxValue\": \"%d\"", (int)fMaxLimit];
    [content appendString:@"},"
     "\"data\": ["];

    for (int i = 0; i < _nfcMsg.temperaturesArray.count; i++) {
        TempDetail *item = [_nfcMsg.temperaturesArray objectAtIndex:i];
        [content appendFormat:@"{\"value\":\"%@\"},", item.decimalTemp];
    }

    [content appendString:@"],"
     "\"trendlines\": ["
     "{"
     "\"line\": ["
     "{"];

//    if (fMin != fMax) {
//
//        [content appendFormat:@"\"startvalue\": \"%.1f\",", fMin];
//        [content appendString:@"\"color\": \"#1aaf5d\","
//         "\"valueOnRight\": \"1\","];
//
//        [content appendFormat:@"\"displayvalue\": \"%.1f\"", fMinLimit];
//        [content appendString:@"},"
//         "{"];
//        [content appendFormat:@"\"startvalue\": \"%.1f\",", fMax];
//
//        [content appendString:@"\"color\": \"#1aaf5d\","
//         "\"valueOnRight\": \"1\","];
//        [content appendFormat:@"\"displayvalue\": \"%.1f\"", fMaxLimit];
//    }

    [content appendString:@"}"
     "]"
     "}"
     "]"
     "}"
     "});"
     "revenueChart.render();"];

    [content appendString:@"</script>"];
    [content appendString:@"</body>"];
    [content appendString:@"</html>"];
    return [content mutableCopy];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

/////////////////////////////////////////////////////////
// scrollViewDelegate
/////////////////////////////////////////////////////////
//禁止滚动条顶部的惯性滚动
- (void)scrollViewDidScroll:(UIScrollView *)scrollView
{
    if (scrollView.contentOffset.y <= 0)
    {
        CGPoint offset = scrollView.contentOffset;
        offset.y = 0;
        scrollView.contentOffset = offset;
    }
}

@end
