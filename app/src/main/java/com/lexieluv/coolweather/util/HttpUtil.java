package com.lexieluv.coolweather.util;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * 2.进行一系列配置（创建assets，添加注册app名字）
 * 3.现在创建数据网络解析类,作用是：能够和服务器进行交互，发起一条http请求，传入请求地址，并注册一个回调来处理服务器响应
 */
public class HttpUtil {
    public static void sendOkHttpRequest(String address, Callback callback){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }
}
