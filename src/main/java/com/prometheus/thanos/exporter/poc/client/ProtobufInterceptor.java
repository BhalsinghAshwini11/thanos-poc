package com.prometheus.thanos.exporter.poc.client;

import com.google.protobuf.Message;
import com.prometheus.thanos.exporter.poc.WriteResponseOuterClass;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;

//TODO WIP
public class ProtobufInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        // If the request body is Protobuf, convert it to the Protobuf format
        if (request.body() instanceof ProtobufRequestBody) {
            ProtobufRequestBody protobufBody = (ProtobufRequestBody) request.body();
            request = request.newBuilder()
                    .post(protobufBody)
                    .header("Content-Type", "application/x-protobuf")
                    .build();
        }

        Response response = chain.proceed(request);

        // Log the raw response for debugging
        String responseBodyString = response.body() != null ? response.body().string() : "null";
        // System.out.println("Raw Response Body: " + responseBodyString);

        // If the response body is Protobuf, convert it from the Protobuf format
        if ("application/x-protobuf".equals(response.header("Content-Type"))) {
            ResponseBody responseBody = response.body();
            if (responseBody != null) {
                // Implement your Protobuf parsing logic here
                Message protobufMessage = parseProtobuf(responseBody.bytes());
                response = response.newBuilder()
                        .body(ResponseBody.create(protobufMessage.toByteArray(), responseBody.contentType()))
                        .build();
            }
        }

        return response;
    }

    private Message parseProtobuf(byte[] bytes) {
        try {
            // Parse the bytes to a WriteResponse Protobuf message
            return WriteResponseOuterClass.WriteResponse.parseFrom(bytes);
        } catch (IOException e) {
            // Handle parsing error
            e.printStackTrace();
            return null;
        }
    }
}