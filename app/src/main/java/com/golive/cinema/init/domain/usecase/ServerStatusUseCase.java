package com.golive.cinema.init.domain.usecase;


import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.UseCase;
import com.golive.cinema.data.source.ServerInitDataSource;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.entity.ServerMessage;

import java.util.List;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by chgang on 2016/10/28.
 */

public class ServerStatusUseCase extends
        UseCase<ServerStatusUseCase.RequestValues, ServerStatusUseCase.ResponseValue> {

    private final ServerInitDataSource mServerInitDataSource;

    public ServerStatusUseCase(@NonNull ServerInitDataSource serverInitDataSource,
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
                    public ResponseValue call(List<ServerMessage> serverMessagesList) {
                        return new ResponseValue(serverMessagesList);
                    }
                });
    }

    public static class RequestValues implements UseCase.RequestValues {
    }

    public static class ResponseValue implements UseCase.ResponseValue {

        private ServerMessage serverMessage;

        private List<ServerMessage> serverMessageList;

        public ResponseValue(List<ServerMessage> serverMessageList) {
            this.serverMessageList = serverMessageList;
        }

        public ResponseValue(ServerMessage serverMessage) {
            this.serverMessage = serverMessage;
        }

        public ServerMessage getServerMessage() {
            return serverMessage;
        }

        public List<ServerMessage> getServerMessageList() {
            return serverMessageList;
        }
    }
}
