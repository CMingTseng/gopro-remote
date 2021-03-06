package com.github.jbarr21.goproremote.data.api;

import com.github.jbarr21.goproremote.BuildConfig;
import com.github.jbarr21.goproremote.GoProRemoteApp;
import com.github.jbarr21.goproremote.common.utils.Parser;
import com.github.jbarr21.goproremote.data.storage.ConfigStorage;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import okhttp3.ConnectionSpec;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.moshi.MoshiConverterFactory;

public final class Apis {

    private static final String GO_PRO_ENDPOINT_URL = "http://10.5.5.9/";

    private static GoProApi goProApi;
    private static OkHttpClient okHttpClient;

    private Apis() { }

    public static GoProApi getGoProApi() {
        if (goProApi == null) {
            goProApi = new Retrofit.Builder()
                    .client(getOkHttpClient())
                    .addConverterFactory(MoshiConverterFactory.create(Parser.getMoshi()))
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .baseUrl(HttpUrl.parse(GO_PRO_ENDPOINT_URL))
                    .build()
                    .create(GoProApi.class);
        }
        return goProApi;
    }

    public static OkHttpClient getOkHttpClient() {
        if (okHttpClient == null) {
            ConfigStorage configStorage = new ConfigStorage(GoProRemoteApp.getInstance());
            PasswordSigningInterceptor signingInterceptor = new PasswordSigningInterceptor(configStorage);

            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.HEADERS : HttpLoggingInterceptor.Level.NONE);

            okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .connectionSpecs(Arrays.asList(ConnectionSpec.MODERN_TLS, ConnectionSpec.CLEARTEXT))
                    //.cookieJar(CookieHandler.getDefault())
                    .addInterceptor(signingInterceptor)
//                    .addInterceptor(loggingInterceptor)
//                    .addInterceptor(new CurlLoggingInterceptor())
//                    .addNetworkInterceptor(new StethoInterceptor())
                    .build();
        }
        return okHttpClient;
    }

    public static void setOkHttpClient(OkHttpClient okHttpClient) {
        Apis.okHttpClient = okHttpClient;
        Apis.goProApi = null;
        Apis.goProApi = getGoProApi();
    }
}
