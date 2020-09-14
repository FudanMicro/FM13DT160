package com.fmsh.nfcinstruct.tools;

import com.fmsh.nfcinstruct.utils.MyConstant;
import com.fmsh.nfcinstruct.utils.TransUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wuyajiang
 * @date 2020/8/10
 */
public class InstructMap {
    private static int[] parameterArr = new int[5];


    /**
     * 14443指令集合
     *
     * @param type
     * @return
     */
    public static byte[] instructA(int type, int value, int value1) {
        byte[] bytes = null;
        switch (type) {
            case 1://唤醒指令
                bytes = new byte[]{0x40, (byte) 0xc4, 0x00, 0x00, 0x00, 0x00, 0x00};
                break;
            case 2://查看唤醒状态
                bytes = new byte[]{0x40, (byte) 0xc4, (byte) 0x80, 0x00, 0x00, 0x00, 0x00};
                break;
            case 3://配置延时测温时间 m
                bytes = new byte[]{0x40, (byte) 0xc5, (byte) 0xc0, (byte) 0x84, 0x00, (byte) value, 0x00};
                break;
            case 4://查看延时测温时间
                bytes = new byte[]{0x40, (byte) 0xc6, (byte) 0xc0, (byte) 0x84, 0x00, 0x00, 0x00};
                break;
            case 5://配置测温间隔时间 s
                bytes = new byte[]{0x40, (byte) 0xc5, (byte) 0xc0, (byte) 0x85, (byte) (value >> 8 & 0xff), (byte) (value & 0xff), 0x00};
                break;
            case 6://查看测温间隔时间
                bytes = new byte[]{0x40, (byte) 0xc6, (byte) 0xc0, (byte) 0x85, 0x00, 0x00, 0x00};
                break;
            case 7: //开启rtc测温
                bytes = new byte[]{0x40, (byte) 0xc2, (byte) 0x00, (byte) 0x00, 0x00, 0x00, 0x00};
                break;
            case 8:// 结束rtc测温
                bytes = new byte[]{0x40, (byte) 0xc2, (byte) 0x80, (byte) 0x00, 0x00, 0x00, 0x00};
                break;
            case 9://查看状态结果
                bytes = new byte[]{0x40, (byte) 0xcf, (byte) 0x01, (byte) 0x00, 0x00, 0x00, 0x00};
                break;
            case 10: //读取rtc测温数据
                bytes = new byte[]{0x40, (byte) 0xb1, (byte) 0x10, 0x00, 0x00, (byte) 0xfa, 0x00};
                break;
            case 11: //启动实时测温
                bytes = new byte[]{0x40, (byte) 0xc0, 0x06, 0x00, 0x00, 0x00, 0x00};
                break;
            case 12: // 获取实时测温数据
                bytes = new byte[]{0x40, (byte) 0xc0, (byte) 0x84, 0x00, 0x00, 0x00, 0x00};
                break;
            case 13://设置测温次数
                bytes = new byte[]{0x40, (byte) 0xb3, (byte) 0xb0, (byte) 0x94, 0x01, 0x00, 0x00, (byte) (value & 0xff), (byte) (value >> 8 & 0xff)};
                break;
            case 14: //查看测温次数
                bytes = new byte[]{0x40, (byte) 0xb1, (byte) 0xb0, (byte) 0x94, 0x00, (byte) 0x3, 0x00};
                break;
            case 15: //获取测温时间
                bytes = new byte[]{0x40, (byte) 0xb1, 0x00, 0x26, 0x00, 0x07, 0x00};
                break;
            case 16: // 设置最小温度值

                if (value < 0) {

                    bytes = new byte[]{0x40, (byte) 0xb3, (byte) 0xb0, (byte) 0x80, 0x01, 0x00, 0x00, (byte) ((value * 4 + 0x400) & 0xff), (byte) ((value * 4 + 0x400) >> 8 & 0xff)};

                } else {

                    bytes = new byte[]{0x40, (byte) 0xb3, (byte) 0xb0, (byte) 0x80, 0x01, 0x00, 0x00, (byte) ((value * 4) & 0xff), (byte) ((value * 4) >> 8 & 0xff)};

                }
                break;
            case 17: // 设置最大温度值

                if (value < 0) {
                    bytes = new byte[]{0x40, (byte) 0xb3, (byte) 0xb0, (byte) 0x82, 0x01, 0x00, 0x00, (byte) ((value * 4 + 0x400) & 0xff), (byte) ((value * 4 + 0x400) >> 8 & 0xff)};

                } else {
                    bytes = new byte[]{0x40, (byte) 0xb3, (byte) 0xb0, (byte) 0x82, 0x01, 0x00, 0x00, (byte) ((value * 4) & 0xff), (byte) ((value * 4) >> 8 & 0xff)};

                }

                break;
            case 18: //获取最大最小温度值
                bytes = new byte[]{0x40, (byte) 0xb1, (byte) 0xb0, (byte) 0x80, 0x00, 0x03, 0x00};
                break;
            case 19: //写到block9温度大小值
                bytes = new byte[]{0x40, (byte) 0xb3, (byte) 0x00, (byte) 0x24, 0x03, 0x00, 0x00, 0x00, (byte) value, 0x00, (byte) value1};
                break;
            case 20: //写测温block10间隔
                bytes = new byte[]{0x40, (byte) 0xb3, (byte) 0x00, (byte) 0x28, 0x03, 0x00, 0x00, (byte) (value >> 8 & 0xff), (byte) (value & 0xff), 0x12, 0x03};
                break;
            case 21: //读取测温间隔
                bytes = new byte[]{0x40, (byte) 0xb1, 0x00, 0x20, 0x00, 0x0b, 0x00};
                break;
            case 22: //写时间
                long time = System.currentTimeMillis() / 1000;
                String format = String.format("%08x", time);
                byte[] timeByte = TransUtil.hexToByte(format);
                bytes = new byte[]{0x40, (byte) 0xb3, (byte) 0x00, (byte) 0x20, 0x03, 0x00, 0x00, (byte) timeByte[0], (byte) timeByte[1], (byte) timeByte[2], (byte) timeByte[3]};
                break;
            case 23: //读测温时间
                bytes = new byte[]{0x40, (byte) 0xb1, 0x00, 0x20, 0x00, 0x03, 0x00};
                break;
            case 24: //休眠
                bytes = new byte[]{0x40, (byte) 0xc3, 0x01, 0x00, 0x00, 0x00, 0x00};
                break;
            case 25://Init Regfile
                bytes = new byte[]{0x40, (byte) 0xce, 0x00, 0x00, 0x00, 0x00, 0x00};
                break;
            case 26: //led on
                bytes = new byte[]{0x40, (byte) 0xc9, 0x02, 0x00, 0x00, 0x00, 0x00};
                break;
            case 27: //led off
                bytes = new byte[]{0x40, (byte) 0xc9, 0x01, 0x00, 0x00, 0x00, 0x00};
                break;
            case 28: //获取电池电压第一步 配置寄存器
                bytes = new byte[]{0x40, (byte) 0xc5, (byte) 0xc0, 0x12, 0x00, 0x08, 0x00};
                break;
            case 29: //获取电池电压第二步  启动测量
                bytes = new byte[]{0x40, (byte) 0xc0, (byte) 0x12, 0x00, 0x00, 0x00, 0x00};
                break;
            case 30: //获取电池电压第三步 读取结果
                bytes = new byte[]{0x40, (byte) 0xc0, (byte) 0x92, 0x00, 0x00, 0x00, 0x00};
                break;
            case 31://获取电池电压第四步  恢复寄存器
                bytes = new byte[]{0x40, (byte) 0xc5, (byte) 0xc0, 0x12, 0x00, 0x00, 0x00};
                break;
            case 32://读C091寄存器
                bytes = new byte[]{0x40, (byte) 0xc6, (byte) 0xc0, (byte) 0x91, 0x00, 0x00, 0x00};
                break;
            case 33://读C092寄存器
                bytes = new byte[]{0x40, (byte) 0xc6, (byte) 0xc0, (byte) 0x92, 0x00, 0x00, 0x00};
                break;
            case 34://获取场强指令
                bytes = new byte[]{0x40, (byte) 0xD0, (byte) 0x00, (byte) 0x00, 0x00, 0x00, 0x00};
                break;
            case 35: //获取随机数
                bytes = new byte[]{0x40, (byte) 0xB2, (byte) 0x00, (byte) 0x00, 0x00, 0x00, 0x00};
                break;
            case 36: //读取c094寄存器
                bytes = new byte[]{0x40, (byte) 0xc6, (byte) 0xc0, (byte) 0x94, 0x00, 0x00, 0x00};
                break;
            case 37: //读取B180地址
                bytes = new byte[]{0x40, (byte) 0xb1, (byte) 0xb1, (byte) 0x80, 0x00, 0x04, 0x00};
                break;
            case 38: //读取c09a寄存器max_limit_cnt
                bytes = new byte[]{0x40, (byte) 0xc6, (byte) 0xc0, (byte) 0x9a, 0x00, 0x00, 0x00};
                break;
            case 39: //读取c09b寄存器min_limit_cnt
                bytes = new byte[]{0x40, (byte) 0xc6, (byte) 0xc0, (byte) 0x9b, 0x00, 0x00, 0x00};
                break;
            case 40: //写测温最小值
                bytes = new byte[]{0x40, (byte) 0xc5, (byte) 0xc0, (byte) 0x99, (byte) ((100 * 4) >> 8 & 0xff), (byte) ((100 * 4) & 0xff), 0x00};
                break;
            case 41: //写测温最大值
                bytes = new byte[]{0x40, (byte) 0xc5, (byte) 0xc0, (byte) 0x98, (byte) ((-100 * 4 + 0x400) >> 8 & 0xff), (byte) ((-100 * 4 + 0x400) & 0xff), 0x00};
                break;
            case 42: //读测温最小值
                bytes = new byte[]{0x40, (byte) 0xc6, (byte) 0xc0, (byte) 0x99, (byte) 0x00, 0x00, 0x00};
                break;
            case 43: //读测温最大值
                bytes = new byte[]{0x40, (byte) 0xc6, (byte) 0xc0, (byte) 0x98, (byte) 0x00, 0x00, 0x00};
                break;
            case 44: //读测温次数
                bytes = new byte[]{0x40, (byte) 0xb1, (byte) 0xb1, (byte) 0x88, (byte) 0x00, 0x00, 0x00};
                break;
            case 45:
                //停止测温发送随机数指令
                bytes = new byte[]{0x40, (byte) 0xc2, (byte) 0x80, 0, 0, 0, 0};
                break;
            case 46:
                // 读取测温数据指令
                bytes = new byte[]{0x40, (byte) 0xb1, (byte) 0xb0, 0x54, 0x00, (byte) 0x03, 0x00};
                break;
            case 47:
                // 读取温度记录数据
                bytes = new byte[]{0x40, (byte) 0xb1, (byte) 0, (byte) 0, (byte) 0x00, (byte) 0, 0x00};
                break;
            case 48:
                //获取配置信息,检测当前标签是否为体温标签
                bytes = new byte[]{0x40, (byte) 0xb1, (byte) 0xb0,0x40,0,0,0};
                break;
            case 49:
                //获取det信息
                bytes  = new byte[]{0x40, (byte) 0xb1, (byte) 0xb0, 0x48, 0x00, 0x04, 0};
                break;
            case 50:
                //设置用户配置模式
                bytes = new byte[]{0x40, (byte) 0xb3, (byte) 0xb0,0x40,0x03,0,0,(byte) 0xDC,0x23,0x29, (byte) 0xD6};
                break;
            case 51:
                //设置用户配置模式
                bytes = new byte[]{0x40, (byte) 0xb3, (byte) 0xb0,0x40, 0x03,0,0,(byte) 0x4C, (byte) 0xB3,0x29, (byte) 0xD6};
                break;
            case 52:
                //测温流程中测温次数
                bytes = new byte[]{0x40, (byte) 0xc6, (byte) 0xc0, (byte) 0x96, (byte) 0x00, 0x00, 0x00};
                break;
            case 53:
                //获取auth_rb_cfg
                bytes  = new byte[]{0x40, (byte) 0xb1, (byte) 0xb1, 0x38, 0x00, 0, 0};
                break;
            case 54:
                // 设置密码
                bytes = new byte[]{0x40, (byte) 0xb3, (byte) 0,0,0x03,0,0,(byte) 0,0,0, (byte) 0};
                break;
            case 55:
                //密码认证
                bytes = new byte[]{0x40, (byte) 0xb4,  (byte) 0x04,  0, 0, 0,0};
                break;
            default:
                break;
        }
        return bytes;
    }

    public static byte[] instructA(int type) {
        return instructA(type, 0);
    }

    public static byte[] instructA(int type, int value) {
        return instructA(type, value, 0);
    }


    /**
     * 指令类型处理
     *
     * @param type
     * @return
     */
    public static byte[] instructV(int type, int value, int value1) {
        byte[] bytes = null;
        switch (type) {
            case 1: //唤醒
                bytes = new byte[]{0x22, (byte) 0xc4, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0x00};
                break;
            case 2://查看唤醒
                bytes = new byte[]{0x22, (byte) 0xc4, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0x80};
                break;
            case 3: //延时
                bytes = new byte[]{0x22, (byte) 0xc5, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xc0, (byte) 0x84, (byte) 0x00, (byte) value};
                break;
            case 4: //查看延时
                bytes = new byte[]{0x22, (byte) 0xc6, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xc0, (byte) 0x84};
                break;
            case 5: //测温间隔
                bytes = new byte[]{0x22, (byte) 0xc5, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xc0, (byte) 0x85, (byte) (value >> 8 & 0xff), (byte) (value & 0xff)};
                break;
            case 6: //查看测温间隔
                bytes = new byte[]{0x22, (byte) 0xc6, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xc0, (byte) 0x85};
                break;
            case 7: //开始rtc
                bytes = new byte[]{0x22, (byte) 0xc2, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0x00, 0, 0, 0, 0};
                break;
            case 8: //结束rtc测温
                bytes = new byte[]{0x22, (byte) 0xc2, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0x80, 0, 0, 0, 0};
                break;
            case 9://查看电池,等状态
                bytes = new byte[]{0x22, (byte) 0xcf, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0x01, (byte) 0x00, 0x00};
                break;
            case 10://读取数据
                bytes = new byte[]{0x22, (byte) 0xb1, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0x10, (byte) 0x00, 0x00, (byte) 0xfa};
                break;
            case 11://开始实时测温
                bytes = new byte[]{0x22, (byte) 0xc0, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0x06, (byte) 0x00};
                break;
            case 12://获取实时测温结果
                bytes = new byte[]{0x22, (byte) 0xc0, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0x84, (byte) 0x00};
                break;
            case 13: //设置测温次数
                bytes = new byte[]{0x22, (byte) 0xb3, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xb0, (byte) 0x94, 0x01, (byte) (value & 0xff), (byte) (value >> 8 & 0xff)};
                break;
            case 14: //查看测温次数
                bytes = new byte[]{0x22, (byte) 0xb1, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xb0, (byte) 0x94, 0x00, (byte) 0x3};
                break;
            case 15:
                bytes = new byte[]{0x22, (byte) 0xb1, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0x00, 0x26, 0x00, 0x07};
                break;
            case 16://设置最小温度值

                if (value1 < 0) {
                    bytes = new byte[]{0x22, (byte) 0xb3, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xb0, (byte) 0x80, 0x01, (byte) ((value * 4 + 0x400) & 0xff), (byte) ((value * 4 + 0x400) >> 8 & 0xff)};

                } else {
                    bytes = new byte[]{0x22, (byte) 0xb3, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xb0, (byte) 0x80, 0x01, (byte) (byte) ((value * 4) & 0xff), (byte) ((value * 4) >> 8 & 0xff)};

                }
                break;
            case 17: // 设置最大温度值

                if (value < 0) {
                    bytes = new byte[]{0x22, (byte) 0xb3, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xb0, (byte) 0x82, 0x01, (byte) ((value * 4 + 0x400) & 0xff), (byte) ((value * 4 + 0x400) >> 8 & 0xff)};

                } else {
                    bytes = new byte[]{0x22, (byte) 0xb3, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xb0, (byte) 0x82, 0x01, (byte) ((value * 4) & 0xff), (byte) ((value * 4) >> 8 & 0xff)};

                }
                break;
            case 18://获取温度值
                bytes = new byte[]{0x22, (byte) 0xb1, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xb0, (byte) 0x80, 0x00, 0x03};
                break;
            case 19: //写温度大小值
                bytes = new byte[]{0x22, (byte) 0xb3, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0x00, (byte) 0x1e, 0x03, 0x00, (byte) value, 0x00, (byte) value1};
                break;
            case 20: //写测温间隔
                bytes = new byte[]{0x22, (byte) 0xb3, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0x00, (byte) 0x22, 0x01, (byte) (value >> 8 & 0xff), (byte) (value & 0xff)};
                break;
            case 21: // 读取测温间隔
                bytes = new byte[]{0x22, (byte) 0xb1, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0x00, (byte) 0x18, 0x00, 0x0b};
                break;
            case 22://写时间
                long time = System.currentTimeMillis() / 1000;
                String format = String.format("%08x", time);
                byte[] timeByte = TransUtil.hexToByte(format);
                bytes = new byte[]{0x22, (byte) 0xb3, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, 0x00, 0x1a, 0x03, (byte) timeByte[0], (byte) timeByte[1], (byte) timeByte[2], (byte) timeByte[3]};
                break;
            case 23: //读取时间
                bytes = new byte[]{0x22, (byte) 0xb1, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, 0x00, (byte) 0x18, 0x00, 0x07};
                break;
            case 24: //休眠
                bytes = new byte[]{0x22, (byte) 0xc3, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, 0x01};
                break;
            case 25://Init Regfile
                bytes = new byte[]{0x22, (byte) 0xce, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                break;
            case 26: //led on
                bytes = new byte[]{0x22, (byte) 0xc9, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0x02};
                break;
            case 27: //led off
                bytes = new byte[]{0x22, (byte) 0xc9, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0x00};
                break;
            case 28: //获取电池电压第一步 配置寄存器
                bytes = new byte[]{0x22, (byte) 0xc5, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xc0, 0x12, 0x00, 0x08};
                break;
            case 29: //获取电池电压第二步  启动测量
                bytes = new byte[]{0x22, (byte) 0xc0, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, 0x12, 0x00};
                break;
            case 30: //获取电池电压第三步 读取结果
                bytes = new byte[]{0x22, (byte) 0xc0, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0x92, 0x00};
                break;
            case 31://获取电池电压第四步  恢复寄存器
                bytes = new byte[]{0x22, (byte) 0xc5, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xc0, 0x12, 0x00, 0x00};
                break;
            case 32://读C091寄存器
                bytes = new byte[]{0x22, (byte) 0xc6, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xc0, (byte) 0x91};
                break;
            case 33://读C092寄存器
                bytes = new byte[]{0x22, (byte) 0xc6, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xc0, (byte) 0x92};
                break;
            case 34://获取场强指令
                bytes = new byte[]{0x22, (byte) 0xD0, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, 0x00};
                break;
            case 35: //获取随机数
                bytes = new byte[]{0x22, (byte) 0xB2, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0};
                break;
            case 36: //读取c094寄存器
                bytes = new byte[]{0x22, (byte) 0xc6, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xc0, (byte) 0x94};
                break;
            case 37: //读取B180地址
                bytes = new byte[]{0x22, (byte) 0xb1, (byte) 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xb1, (byte) 0x80, 0x00, 0x04};
                break;
            case 38: //读取c09a寄存器max_limit_cnt
                bytes = new byte[]{0x22, (byte) 0xc6, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xc0, (byte) 0x9a};
                break;
            case 39: //读取c09b寄存器min_limit_cnt
                bytes = new byte[]{0x22, (byte) 0xc6, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xc0, (byte) 0x9b};
                break;
            case 40: //写测温最小值
                bytes = new byte[]{0x22, (byte) 0xc5, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xc0, (byte) 0x99, (byte) ((100 * 4) >> 8 & 0xff), (byte) ((100 * 4) & 0xff)};
                break;
            case 41: //写测温最大值
                bytes = new byte[]{0x22, (byte) 0xc5, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xc0, (byte) 0x98, (byte) ((-100 * 4 + 0x400) >> 8 & 0xff), (byte) ((-100 * 4 + 0x400) & 0xff)};
                break;
            case 42: //读测温最小值
                bytes = new byte[]{0x22, (byte) 0xc6, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xc0, (byte) 0x99};
                break;
            case 43: //读测温最大值
                bytes = new byte[]{0x22, (byte) 0xc6, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xc0, (byte) 0x98};
                break;
            case 44: //读测温次数
                bytes = new byte[]{0x22, (byte) 0xb1, (byte) 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xb1, (byte) 0x88, 0x00, 0x00};
                break;
            case 45:
                //停止测温发送随机数指令
                bytes = new byte[]{0x22, (byte) 0xc2, (byte) 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0x80, 0, 0, 0, 0};
                break;
            case 46:
                // 读取测温数据指令
                bytes = new byte[]{0x22, (byte) 0xb1, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xb0, 0x54, 0x00, (byte) 0x03};
                break;
            case 47:
                // 读取温度记录数据
                bytes = new byte[]{0x22, (byte) 0xb1, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0, (byte) 0, 0x00, (byte) 0};
                break;
            case 48:
                //查询用户配置
                bytes = new byte[]{0x22, (byte) 0xb1, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xb0, (byte) 0x40, 0x00, (byte) 0};
                break;
            case 49:
                //获取det数据
                bytes = new byte[]{0x22, (byte) 0xb1, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xb0, (byte) 0x48, 0x00, (byte) 0x04};
                break;
            case 50:
                //设置原始数据模式
                bytes = new byte[]{0x22, (byte) 0xb3, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xb0, (byte) 0x40, 0x03, (byte) 0xdc,0x23,0x29, (byte) 0xd6};
                break;
            case 51:
                //设置标准数据模式
                bytes = new byte[]{0x22, (byte) 0xb3, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xb0, (byte) 0x40, 0x03, (byte) 0x4C, (byte) 0xB3,0x29, (byte) 0xD6};
                break;
            case 52:
                //测温流程中测温次数
                bytes = new byte[]{0x22, (byte) 0xc6, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xc0, (byte) 0x96};
                break;
            case 53:
                //获取auth_rb_cfg
                bytes = new byte[]{0x22, (byte) 0xb1, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xb1, (byte) 0x38, 0x00, (byte) 0x00};
                break;
            case 54:
                //设置密码
                bytes = new byte[]{0x22, (byte) 0xb3, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0, (byte) 0, 0x03, (byte) 0,0,0, (byte) 0};
                break;
            case 55:
                //密码认证
                bytes = new byte[]{0x22, (byte) 0xb4, 0x1d, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0x04,  0, 0, 0,0};
                break;
            default:
                break;
        }
        return bytes;
    }

    public static byte[] instructV(int type) {
        return instructV(type, 0);
    }

    public static byte[] instructV(int type, int value) {
        return instructV(type, value, 0);
    }


    /**
     * 获取场强,温度,电压等指令集合
     *
     * @param nfcA
     * @return
     */
    public static List<byte[]> getBaseInstructList(boolean nfcA) {
        ArrayList<byte[]> byteList = new ArrayList<>();
        if (nfcA) {
            byteList.add(instructA(34));
            byteList.add(instructA(11));
            byteList.add(instructA(12));
            byteList.add(instructA(9));
            byteList.add(instructA(28));
            byteList.add(instructA(29));
            byteList.add(instructA(30));
            byteList.add(instructA(31));
        } else {
            byteList.add(instructV(34));
            byteList.add(instructV(11));
            byteList.add(instructV(12));
            byteList.add(instructV(9));
            byteList.add(instructV(28));
            byteList.add(instructV(29));
            byteList.add(instructV(30));
            byteList.add(instructV(31));
        }
        return byteList;
    }

    public static List<byte[]> getParameterInstructList(boolean nfcA) {
        List<byte[]> byteList = new ArrayList<>();
        if (nfcA) {
            byteList.add(instructA(9));
            byteList.add(instructA(1));
            byteList.add(instructA(2));
            byteList.add(instructA(3, parameterArr[0]));
            byteList.add(instructA(5, parameterArr[1]));
            byteList.add(instructA(13, parameterArr[2]));
            byteList.add(instructA(40));
            byteList.add(instructA(41));
            byteList.add(instructA(16, parameterArr[3]));
            byteList.add(instructA(17, parameterArr[4]));
            byteList.add(instructA(19, parameterArr[3], parameterArr[4]));
            byteList.add(instructA(20, parameterArr[1]));
            byteList.add(instructA(22));
            byteList.add(instructA(7));
        } else {
            byteList.add(instructV(9));
            byteList.add(instructV(1));
            byteList.add(instructV(2));
            byteList.add(instructV(3, parameterArr[0]));
            byteList.add(instructV(5, parameterArr[1]));
            byteList.add(instructV(13, parameterArr[2]));
            byteList.add(instructV(40));
            byteList.add(instructV(41));
            byteList.add(instructV(16, parameterArr[3]));
            byteList.add(instructV(17, parameterArr[4]));
            byteList.add(instructV(19, parameterArr[3], parameterArr[4]));
            byteList.add(instructV(20, parameterArr[1]));
            byteList.add(instructV(22));
            byteList.add(instructV(7));
        }
        return byteList;
    }

    public static List<byte[]> getStopInstruct(boolean nfcA) {
        List<byte[]> byteList = new ArrayList<>();
        if (nfcA) {
            byteList.add(instructA(35));
            byteList.add(instructA(53));
            byteList.add(instructA(55));
            byteList.add(instructA(45));
        } else {
            byteList.add(instructV(35));
            byteList.add(instructV(53));
            byteList.add(instructV(55));
            byteList.add(instructV(45));
        }
        return byteList;
    }
    public static List<byte[]> getResultInstruct(boolean nfcA){
        List<byte[]> byteList = new ArrayList<>();
        if (nfcA) {
            byteList.add(instructA(46));
            byteList.add(instructA(14));
            byteList.add(instructA(21));
            byteList.add(instructA(18));
            byteList.add(instructA(36));
            byteList.add(instructA(37));
            byteList.add(instructA(38));
            byteList.add(instructA(39));
            byteList.add(instructA(42));
            byteList.add(instructA(43));
            byteList.add(instructA(4));
            byteList.add(instructA(9));
            byteList.add(instructA(32));
            byteList.add(instructA(44));
            byteList.add(instructA(47));
        } else {
            byteList.add(instructV(46));
            byteList.add(instructV(14));
            byteList.add(instructV(21));
            byteList.add(instructV(18));
            byteList.add(instructV(36));
            byteList.add(instructV(37));
            byteList.add(instructV(38));
            byteList.add(instructV(39));
            byteList.add(instructV(42));
            byteList.add(instructV(43));
            byteList.add(instructV(4));
            byteList.add(instructV(9));
            byteList.add(instructV(32));
            byteList.add(instructV(44));
            byteList.add(instructV(47));
        }
        return byteList;
    }
    public static List<byte[]> getStandard(boolean isNfcA){
        List<byte[]> byteList = new ArrayList<>();
        if(isNfcA){
            byteList.add(instructA(48));
            byteList.add(instructA(51));
        }else {
            byteList.add(instructV(48));
            byteList.add(instructV(51));

        }
        return byteList;
    }
    public static List<byte[]> getUpdatePassword(boolean isNfcA){
        List<byte[]> byteList = new ArrayList<>();
        if (isNfcA) {
            byteList.add(instructA(35));
            byteList.add(instructA(53));
            byteList.add(instructA(55));
            byteList.add(instructA(54));
        } else {
            byteList.add(instructV(35));
            byteList.add(instructV(53));
            byteList.add(instructV(55));
            byteList.add(instructV(54));
        }
        return byteList;
    }

    public static byte[] getInstruct(boolean nfcA, int type) {
        if (nfcA) {
            return instructA(type);
        } else {
            return instructV(type);
        }
    }

    public static void setParameter(int delayMinutes, int intervalSeconds, int loggingCount, int minTemperature, int maxTemperature) {
        parameterArr[0] = delayMinutes;
        parameterArr[1] = intervalSeconds;
        parameterArr[2] = loggingCount;
        parameterArr[3] = minTemperature;
        parameterArr[4] = maxTemperature;
    }

    public static int[] getParameterArr() {
        return parameterArr;
    }
}
