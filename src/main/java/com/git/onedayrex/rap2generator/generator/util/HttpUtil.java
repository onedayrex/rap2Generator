package com.git.onedayrex.rap2generator.generator.util;

import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class HttpUtil {
    //HTTP部分
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static OkHttpClient CLIENT = new OkHttpClient();
    
    public static String post(String url, String json, String cookie) throws IOException {

        System.out.println(("request json ==> " + json));
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Cookie", cookie)
                .build();
        try (Response response = CLIENT.newCall(request).execute()) {
            assert response.body() != null;
            return response.body().string();
        }
    }

    public static String get(String url, String cookie) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .header("Cookie", cookie)
                .build();
        try (Response response = CLIENT.newCall(request).execute()) {
            assert response.body() != null;
            return response.body().string();
        }
    }
}


