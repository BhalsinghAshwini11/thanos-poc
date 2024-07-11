package com.prometheus.thanos.exporter.poc.client;

import com.google.protobuf.Message;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

import java.io.IOException;

//TODO WIP
public class ProtobufRequestBody extends RequestBody {

    private static final MediaType MEDIA_TYPE = MediaType.get("application/x-protobuf");
    private final Message message;

    public ProtobufRequestBody(Message message) {
        this.message = message;
    }

    @Override
    public MediaType contentType() {
        return MEDIA_TYPE;
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        sink.write(message.toByteArray());
    }
}
