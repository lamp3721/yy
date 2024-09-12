package org.example.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class Http {
    
    static OkHttpClient client = UnsafeOkHttpClient.getUnsafeOkHttpClient();
    
    public static String get(String url) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        try (Response execute = client.newCall(request).execute()){
            
            // 判断是否请求成功
            if (!execute.isSuccessful()) {
                return "404";
            }
            // 返回数据
            return execute.body().string();
        } catch (IOException e) {
            return "404";
        }
    }

    public static void post(String url, JSONObject json) {
        
    }
}
