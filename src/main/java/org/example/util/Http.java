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
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36 Edg/129.0.0.0")
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
