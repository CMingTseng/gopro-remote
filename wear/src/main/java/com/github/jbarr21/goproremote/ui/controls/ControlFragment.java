package com.github.jbarr21.goproremote.ui.controls;

import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.jbarr21.goproremote.R;
import com.github.jbarr21.goproremote.common.data.GoProCommand;
import com.github.jbarr21.goproremote.common.data.GoProCommandRequest;
import com.github.jbarr21.goproremote.common.utils.MessageUtils;
import com.github.jbarr21.goproremote.ui.BaseFragment;
import com.github.jbarr21.goproremote.ui.ProgressActivity;

import java.util.HashMap;

import butterknife.OnClick;
import rx.functions.Action1;
import timber.log.Timber;

public class ControlFragment extends BaseFragment {

    private static final HashMap<Integer, GoProCommand> COMMAND_MAP;
    static {
        COMMAND_MAP = new HashMap<>();
        COMMAND_MAP.put(R.id.powerOn, GoProCommand.POWER_ON);
        COMMAND_MAP.put(R.id.startRecording, GoProCommand.START_RECORDING);
        COMMAND_MAP.put(R.id.stopRecording, GoProCommand.STOP_RECORDING);
        COMMAND_MAP.put(R.id.powerOff, GoProCommand.POWER_OFF);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_control, container, false);
    }

    @SuppressWarnings("unused")
    @OnClick({ R.id.powerOn, R.id.startRecording, R.id.stopRecording, R.id.powerOff })
    public void onClickCommand(View view) {
        final GoProCommand command = COMMAND_MAP.get(view.getId());
        final GoProCommandRequest commandRequest = GoProCommandRequest.create(command, System.currentTimeMillis());
        MessageUtils.sendGoProCommandMessage(googleApiClient, commandRequest)
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer requestId) {
                        Timber.d("sent message onNext - isOnMainThread? %b", Looper.myLooper() == Looper.getMainLooper());
                        Timber.d("Sent GoPro command (%s) successfully", command.name());
                        MessageUtils.disconnectGoogleApiClient(googleApiClient);
                        if (getActivity() != null) {
                            ProgressActivity.showProgressOrFailure(getActivity(), MessageUtils.SUCCESS, commandRequest.id());
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Timber.d("sent message onError - isOnMainThread? %b", Looper.myLooper() == Looper.getMainLooper());
                        Timber.e(throwable, "Failed to send GoPro command: %s", command.name());
                        MessageUtils.disconnectGoogleApiClient(googleApiClient);
                        if (getActivity() != null) {
                            ProgressActivity.showProgressOrFailure(getActivity(), MessageUtils.FAILURE, commandRequest.id());
                        }
                    }
                });
    }
}
