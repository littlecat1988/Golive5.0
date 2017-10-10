package com.golive.cinema.init.domain.usecase;


import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.UseCase;
import com.golive.cinema.data.source.ServerInitDataSource;
import com.golive.cinema.util.StringUtils;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.entity.ServerMessage;

import java.util.List;

import rx.Observable;
import rx.functions.Func1;

public class GetShutdownMessageUseCase extends
        UseCase<GetShutdownMessageUseCase.RequestValues, GetShutdownMessageUseCase.ResponseValue> {

    private final ServerInitDataSource mServerInitDataSource;

    public GetShutdownMessageUseCase(@NonNull ServerInitDataSource serverInitDataSource,
            @NonNull BaseSchedulerProvider baseSchedulerProvider) {
        super(baseSchedulerProvider);
        this.mServerInitDataSource = checkNotNull(serverInitDataSource,
                "ServerInitDataSource cannot be null!");
    }

    @Override
    protected Observable<ResponseValue> executeUseCase(RequestValues requestValues) {
        return mServerInitDataSource.queryServerMessages()
                .map(new Func1<List<ServerMessage>, ResponseValue>() {
                    @Override
                    public ResponseValue call(List<ServerMessage> serverMessages) {
                        ServerMessage msg = null;
                        if (serverMessages != null && !serverMessages.isEmpty()) {
                            for (int i = 0; i < serverMessages.size(); i++) {
                                ServerMessage serverMessage = serverMessages.get(i);
                                String type = serverMessage.getType();
                                if (StringUtils.isNullOrEmpty(type)) {
                                    continue;
                                }

                                if (ServerMessage.SERVER_MESSAGE_TYPE_SHUTDOWN.equals(type)) {
                                    msg = serverMessage;
                                    break;
                                }
                            }
                        }
                        return new ResponseValue(msg);
                    }
                });
    }

    public static class RequestValues implements UseCase.RequestValues {
    }

    public static class ResponseValue implements UseCase.ResponseValue {
        private ServerMessage serverMessage;

        public ResponseValue(ServerMessage serverMessage) {
            this.serverMessage = serverMessage;
        }

        public ServerMessage getServerMessage() {
            return serverMessage;
        }

    }
}
