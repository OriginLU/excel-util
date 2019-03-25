package com.chl.excel.util;

import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * @author lch
 * @since 2018-08-22
 */
public class MacUtil {


    private static void getLocalMac() throws SocketException, UnknownHostException {
        //获取网卡，获取地址
        InetAddress localHost = InetAddress.getLocalHost();
        System.out.println(localHost.getHostName());
        System.out.println(localHost.getAddress());
        byte[] mac = NetworkInterface.getByInetAddress(localHost).getHardwareAddress();
        System.out.println("mac数组长度：" + mac.length);
        StringBuffer sb = new StringBuffer("");
        for (int i = 0; i < mac.length; i++) {
            if (i != 0) {
                sb.append("-");
            }
            int temp = mac[i] & 0xff;
            String str = Integer.toHexString(temp);
            System.out.println("每8位:" + str);
            if (str.length() == 1) {
                sb.append("0" + str);
            } else {
                sb.append(str);
            }
        }
        System.out.println("本机MAC地址:" + sb.toString().toUpperCase());
    }

    public static void main(String[] args) {

        try {
            getLocalMac();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
