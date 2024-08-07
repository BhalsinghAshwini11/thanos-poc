package com.prometheus.thanos.exporter.poc.scheduler;

import com.prometheus.thanos.exporter.poc.WriteRequestOuterClass;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.ipc.http.HttpSender;
import io.micrometer.core.ipc.http.HttpUrlConnectionSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.xerial.snappy.Snappy;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class ScheduledMetricsExporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledMetricsExporter.class);
    private final HttpSender httpClient;

    @Value("${thanos.receive.url}")
    private String thanosReceiveUrl;

    @Value("${prometheus.url}")
    private String prometheusUrl;

    @Value("${scheduler.job.enabled}")
    private boolean schedulerJobEnabled;

    private final Clock clock;

    public ScheduledMetricsExporter(Clock clock) {
        this.clock = clock;
        httpClient = new HttpUrlConnectionSender();
    }

    @Scheduled(fixedRateString = "${scheduler.fixedRate}", initialDelayString = "${scheduler.initialDelay}")
    public void reportCurrentTime() throws IOException {
        if (schedulerJobEnabled) {
            long startTime = clock.monotonicTime();
            LOGGER.debug("Schedule trigger for exporting metrics started");

            String metrics = fetchPrometheusMetricsViaHttpSender();

            WriteRequestOuterClass.WriteRequest writeRequest = convertMetricsToProtobuf(metrics);

            postMetricsToThanosViaHttpSender(writeRequest);

            LOGGER.info("Schedule trigger for exporting metrics took={}ms", calculateFinishTime(startTime));
        } else {
            LOGGER.info("Thanos exporter job disabled");
        }
    }

    private Double calculateFinishTime(long startTime) {
        long durationNs = clock.monotonicTime() - startTime;
        return (Double) (double) TimeUnit.NANOSECONDS.toMillis(durationNs);
    }

    /* Fetch Prometheus metrics exposed locally, using oio.micrometer.core.ipc.http.httpClient */
    private String fetchPrometheusMetricsViaHttpSender() {
        try {
            HttpSender.Request.Builder requestBuilder = httpClient.get(URI.create(prometheusUrl).toString());
            HttpSender.Response response = requestBuilder
                    .send();
            if (response.isSuccessful()) {
                LOGGER.debug("Httpclient: Successfully fetched metrics from local prometheusEndpoint");
            } else if (!response.isSuccessful()) {
                LOGGER.error("Failed to fetch metrics from prometheus endpoint={} with responseCode={}  and responseBody={}", prometheusUrl, response.code(), response.body());
            }
            return response.body();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Malformed fetch endpoint", e);
        } catch (Throwable e) {
            throw new RuntimeException(String.format("Integration issue with prometheus fetch endpoint: %s", prometheusUrl), e);
        }

    }

    /* Export protobuf converted prometheus metrics to Thanos, using oio.micrometer.core.ipc.http.httpClient */
    private void postMetricsToThanosViaHttpSender(WriteRequestOuterClass.WriteRequest writeRequest) throws IOException {

        byte[] compressedData = compressedData(writeRequest.toByteArray());

        try {
            HttpSender.Request.Builder requestBuilder = httpClient.post(thanosReceiveUrl);

            HttpSender.Response response = requestBuilder
                    //  .withContent(MediaType.ALL_VALUE, compressedData)
                    .withContent("application/x-protobuf", compressedData)
                    .send();
            if (response.isSuccessful()) {
                LOGGER.debug("Httpclient: Successfully exported metrics to thanos");
            } else {
                LOGGER.error("Failed to export metrics to thanos endpoint={}, responseCode={} responseBody={}", thanosReceiveUrl, response.code(), response.body());
            }
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Malformed thanos publishing endpoint", e);
        } catch (Throwable e) {
            throw new RuntimeException(String.format("Integration issue with thanos export endpoint: %s due to e: ", thanosReceiveUrl), e);
        }
    }

    /* Parse and convert the metrics to the WriteRequest protobuf format */
    private WriteRequestOuterClass.WriteRequest convertMetricsToProtobuf(String metrics) {

        List<WriteRequestOuterClass.TimeSeries> timeSeriesList = parseMetrics(metrics);

        return WriteRequestOuterClass.WriteRequest.newBuilder()
                .addAllTimeseries(timeSeriesList)
                .build();
    }

    private List<WriteRequestOuterClass.TimeSeries> parseMetrics(String metrics) {
        LOGGER.debug("Parsing prometheus data to proto before exporting to thanos");
        List<WriteRequestOuterClass.TimeSeries> timeSeriesList = new ArrayList<>();

        String[] lines = metrics.split("\n");
        WriteRequestOuterClass.TimeSeries.Builder timeSeriesBuilder = null;

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                // Skip comments and empty lines
                continue;
            }

            String[] parts = line.split(" ");
            if (parts.length < 2) {
                // Invalid metric line
                continue;
            }

            String metricName = parts[0];
            double value;
            try {
                value = Double.parseDouble(parts[1]);
            } catch (NumberFormatException e) {
                // Invalid metric value
                continue;
            }

            timeSeriesBuilder = WriteRequestOuterClass.TimeSeries.newBuilder();

            // Parse labels from the metric name
            String[] nameParts = metricName.split("\\{");
            String baseName = nameParts[0];
            timeSeriesBuilder.addLabels(WriteRequestOuterClass.Label.newBuilder().setName("__name__").setValue(baseName).build());

            if (nameParts.length > 1) {
                String labelsPart = nameParts[1].replace("}", "");
                String[] labels = labelsPart.split(",");
                for (String label : labels) {
                    String[] labelParts = label.split("=");
                    if (labelParts.length == 2) {
                        String labelName = labelParts[0];
                        String labelValue = labelParts[1].replace("\"", "");
                        timeSeriesBuilder.addLabels(WriteRequestOuterClass.Label.newBuilder().setName(labelName).setValue(labelValue).build());
                    }
                }
            }

            // Add the sample value
            WriteRequestOuterClass.Sample sample = WriteRequestOuterClass.Sample.newBuilder().setValue(value).setTimestamp(System.currentTimeMillis()).build();
            timeSeriesBuilder.addSamples(sample);

            timeSeriesList.add(timeSeriesBuilder.build());
        }
        return timeSeriesList;
    }

    /* Using Snappy lib for compress the data before sending as Snappy compression enabled server side */
    private byte[] compressedData(byte[] input) throws IOException {
        LOGGER.debug("Compressing data using Snappy");
        return Snappy.compress(input);
    }
}
