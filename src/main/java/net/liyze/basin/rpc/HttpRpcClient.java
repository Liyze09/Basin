package net.liyze.basin.rpc;

import io.fury.Fury;
import io.fury.Language;
import io.fury.ThreadSafeFury;
import net.liyze.basin.util.LoggingUtils;
import okhttp3.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public final class HttpRpcClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpRpcClient.class);
    private static final Random random = new Random();
    private static final OkHttpClient client = new OkHttpClient();
    private final Map<Integer, Optional<Object>> results = new ConcurrentHashMap<>();
    private static final ThreadSafeFury FURY = Fury.builder()
            .withLanguage(Language.JAVA)
            .withRefTracking(true)
            .requireClassRegistration(false)
            .buildThreadSafeFuryPool(1, 8, 16, TimeUnit.SECONDS);
    private String url;

    public HttpRpcClient(String url) {
        this.url = url;
    }

    public int request(@NotNull String className, @NotNull String methodName, Object @NotNull [] args) {
        LOGGER.debug("Request {}.{}::{}", url, className, methodName);
        int sign = random.nextInt();
        RequestBody body = RequestBody.create(FURY.serialize(args));
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Class-Name", className)
                .addHeader("Method-Name", methodName)
                .addHeader("Request-Sign", String.valueOf(sign))
                .post(body)
                .build();
        results.put(sign, Optional.empty());
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                LoggingUtils.printException(LOGGER, e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.body() != null) {
                    results.put(Integer.parseInt(Objects.requireNonNull(response.header("Request-Sign"))),
                            Optional.of(FURY.deserialize(response.body().bytes())));
                }
            }
        });
        return sign;
    }

    public Optional<Object> getOptionalResult(int sign) {
        return results.get(sign);
    }

    public @NotNull Object getResult(int sign) {
        while (results.get(sign).isEmpty()) Thread.onSpinWait();
        return results.get(sign).get();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        final var that = (HttpRpcClient) obj;
        return Objects.equals(this.url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }

    @Contract(pure = true)
    @Override
    public @NotNull String toString() {
        return "HttpRpcClient[" + url + ']';
    }

}
