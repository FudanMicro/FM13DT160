//
//  AppDelegate.m
//  FMTemperature
//
//  Created by gaolailong on 2018/7/9.
//  Copyright © 2018年 复旦微电子集团股份科技有限公司. All rights reserved.

#import "AppDelegate.h"
#import "MeasureViewController.h"
#import "LoggingViewController.h"

@interface AppDelegate ()

@property (strong, nonatomic) UITabBarController *tabbarController;
@property(nonatomic, strong) UINavigationController *navMeasure;
@property(nonatomic, strong) UINavigationController *navLogging;
@property(nonatomic, strong) MeasureViewController *measureVC;
@property(nonatomic, strong) LoggingViewController *loggingVC;

@end

@implementation AppDelegate


- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
    [[UINavigationBar appearance] setBarTintColor:DSBlueColor];
    [[UINavigationBar appearance] setTintColor:[UIColor whiteColor]];
    //初始化
    _measureVC = [[MeasureViewController alloc] init];
    _loggingVC = [[LoggingViewController alloc] init];
    
    _navMeasure = [[UINavigationController alloc] initWithRootViewController:_measureVC];
    _navMeasure.tabBarItem.title=NSLocalizedString(@"FM_Tab_Measurement", nil);
    _navMeasure.tabBarItem.image=[UIImage imageNamed:@"temperature"];
    _navMeasure.tabBarItem.tag=0;
    //调整tabBar图标大小
    _navMeasure.tabBarItem.imageInsets = UIEdgeInsetsMake(-5, -5, -5, -5);
    //导航栏标题字体颜色
    _navMeasure.navigationBar.titleTextAttributes = [NSDictionary dictionaryWithObject:[UIColor whiteColor] forKey:NSForegroundColorAttributeName];
    
    _navLogging = [[UINavigationController alloc] initWithRootViewController:_loggingVC];
    _navLogging.tabBarItem.title=NSLocalizedString(@"FM_Tab_Logging", nil);
    _navLogging.tabBarItem.image=[UIImage imageNamed:@"history"];
    _navLogging.tabBarItem.tag=1;
    //调整tabBar图标大小
    _navLogging.tabBarItem.imageInsets = UIEdgeInsetsMake(-5, -5, -5, -5);
    //导航栏标题字体颜色
    _navLogging.navigationBar.titleTextAttributes = [NSDictionary dictionaryWithObject:[UIColor whiteColor] forKey:NSForegroundColorAttributeName];
    
    NSArray *arr_controller = [[NSArray alloc] initWithObjects:_navMeasure, _navLogging, nil];
    _tabbarController=[[UITabBarController alloc] init];
    _tabbarController.viewControllers=arr_controller;
    _tabbarController.selectedIndex=0;
    _tabbarController.tabBar.tintColor = DSBlueColor;
    _tabbarController.tabBar.barTintColor = DSCommonColor;
    
    self.window = [[UIWindow alloc] initWithFrame:[[UIScreen mainScreen] bounds]];
    self.window.backgroundColor = [UIColor whiteColor];
    self.window.rootViewController = _tabbarController;
    [self.window makeKeyAndVisible];
    return YES;
}


- (void)applicationWillResignActive:(UIApplication *)application {
    // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
    // Use this method to pause ongoing tasks, disable timers, and invalidate graphics rendering callbacks. Games should use this method to pause the game.
}


- (void)applicationDidEnterBackground:(UIApplication *)application {
    // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later.
    // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
}


- (void)applicationWillEnterForeground:(UIApplication *)application {
    // Called as part of the transition from the background to the active state; here you can undo many of the changes made on entering the background.
}


- (void)applicationDidBecomeActive:(UIApplication *)application {
    // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
}


- (void)applicationWillTerminate:(UIApplication *)application {
    // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
}


@end
