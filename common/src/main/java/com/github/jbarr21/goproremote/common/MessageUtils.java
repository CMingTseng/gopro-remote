package com.github.jbarr21.goproremote.common;

import android.os.Looper;
import android.support.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.gson.Gson;
import com.twotoasters.servos.util.ListUtils;

import java.net.ConnectException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.schedulers.Schedulers;
import timber.log.Timber;

import static com.github.jbarr21.goproremote.common.Constants.GO_PRO_COMMAND_TIMEOUT_SEC;
import static com.github.jbarr21.goproremote.common.Constants.WEARABLE_CONN_TIMEOUT_MILLIS;

public class MessageUtils {

    public static final boolean SUCCESS = true;
    public static final boolean FAILURE = false;

    private static final Gson GSON = new Gson();

    private MessageUtils() { }

    public static Observable<Integer> sendGoProCommandMessage(@NonNull GoogleApiClient googleApiClient, GoProCommandRequest commandRequest) {
        return connectGoogleApiClient(googleApiClient)
                .flatMap(connectionResult -> fetchPeerId(googleApiClient))
                .flatMap(peerId -> sendCommandRequest(googleApiClient, peerId, commandRequest))
                .subscribeOn(Schedulers.newThread());
    }

    private static Observable<ConnectionResult> connectGoogleApiClient(GoogleApiClient googleApiClient) {
        return Observable.create(subscriber -> {
            Timber.d("connectGoogleApiClient - isOnMainThread? %b", Looper.myLooper() == Looper.getMainLooper());
            ConnectionResult connectionResult = googleApiClient.blockingConnect(WEARABLE_CONN_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
            if (connectionResult != null && connectionResult.isSuccess()) {
                subscriber.onNext(connectionResult);
            } else {
                subscriber.onError(new ConnectException(connectionResult != null ? connectionResult.toString() : "Failed to connect to Google API client"));
            }
        });
    }

    private static Observable<String> fetchPeerId(GoogleApiClient googleApiClient) {
        Timber.d("fetchPeerId - isOnMainThread? %b", Looper.myLooper() == Looper.getMainLooper());
        List<Node> nodes = Wearable.NodeApi.getConnectedNodes(googleApiClient).await(WEARABLE_CONN_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS).getNodes();
        return !ListUtils.isEmpty(nodes)
            ? Observable.just(nodes.get(0).getId())
            : Observable.error(new IllegalStateException("Unable to fetch peer id"));
    }

    private static Observable<Integer> sendCommandRequest(GoogleApiClient googleApiClient, String peerId, GoProCommandRequest commandRequest) {
        Timber.d("connectGoogleApiClient - sendCommandRequest? %b", Looper.myLooper() == Looper.getMainLooper());
        int requestId = Wearable.MessageApi
                .sendMessage(googleApiClient, peerId, Constants.COMMAND_REQUEST_PATH, GSON.toJson(commandRequest).getBytes())
                .await(GO_PRO_COMMAND_TIMEOUT_SEC, TimeUnit.SECONDS)
                .getRequestId();

        return requestId != MessageApi.UNKNOWN_REQUEST_ID
                ? Observable.just(requestId)
                : Observable.error(new Exception("Unable to send command message"));
    }

    public static Observable<Integer> sendGoProCommandResponse(GoogleApiClient googleApiClient, String peerId, GoProCommandResponse response) {
        Timber.d("connectGoogleApiClient - sendCommandRequest? %b", Looper.myLooper() == Looper.getMainLooper());
        int responseRequestId = Wearable.MessageApi
                .sendMessage(googleApiClient, peerId, Constants.COMMAND_RESPONSE_PATH, GSON.toJson(response).getBytes())
                .await(GO_PRO_COMMAND_TIMEOUT_SEC, TimeUnit.SECONDS)
                .getRequestId();

        return responseRequestId != MessageApi.UNKNOWN_REQUEST_ID
                ? Observable.just(responseRequestId)
                : Observable.error(new Exception("Unable to send command message"));
    }

    public static void disconnectGoogleApiClient(GoogleApiClient googleApiClient) {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }
}