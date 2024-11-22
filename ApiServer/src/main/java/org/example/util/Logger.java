package org.example.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
    private static final String APP_NAME = "ApiServer";

    public static void log(String method, String path, String requestBody, String responseBody) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        System.out.printf("[%s] [%s] [%s] [%s] [REQUEST BODY: %s] [RESPONSE BODY: %s]%n",
                timestamp, APP_NAME, method, path, requestBody, responseBody);
    }
}
