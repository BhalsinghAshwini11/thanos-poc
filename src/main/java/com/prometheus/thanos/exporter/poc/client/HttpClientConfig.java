package com.prometheus.thanos.exporter.poc.client;

import org.springframework.stereotype.Component;

//TODO WIP
@Component
public class HttpClientConfig {

   /* public OkHttpClient getClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        return new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(5, 5, TimeUnit.MINUTES))
                .retryOnConnectionFailure(true)
                // .addInterceptor(logging)
                .addInterceptor(new ProtobufInterceptor())
                .build();
    }*/
}

