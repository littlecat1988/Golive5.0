package com.golive.cinema.player.domain.usecase;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.golive.cinema.ObjectWarp;
import com.golive.cinema.UseCase;
import com.golive.cinema.download.domain.usecase.CheckDownloadFinishUseCase;
import com.golive.cinema.download.domain.usecase.DownloadFileUseCase;
import com.golive.cinema.download.domain.usecase.GetDownloadTaskInfoUseCase;
import com.golive.cinema.filmdetail.domain.usecase.GetFilmDetailUseCase;
import com.golive.cinema.order.domain.usecase.GetValidOrderUseCase;
import com.golive.cinema.order.domain.usecase.RefreshOrderUseCase;
import com.golive.cinema.order.domain.usecase.ReportTicketStatusUseCase;
import com.golive.cinema.player.domain.model.PlaybackValidity;
import com.golive.cinema.player.kdm.KDM;
import com.golive.cinema.restapi.exception.RestApiException;
import com.golive.cinema.util.FileUtils;
import com.golive.cinema.util.StringUtils;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.entity.Film;
import com.golive.network.entity.Media;
import com.golive.network.entity.Order;
import com.golive.network.entity.Ticket;
import com.golive.player.kdm.KDMResCode;
import com.initialjie.download.aidl.DownloadTaskInfo;
import com.initialjie.log.Logger;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

import static com.golive.cinema.util.OkHttpUtils.write2File;
import static com.golive.cinema.util.Preconditions.checkNotNull;

/**
 * Created by Wangzj on 2016/10/27.
 */

public class GetPlaybackValidityUseCase extends
        UseCase<GetPlaybackValidityUseCase.RequestValues, GetPlaybackValidityUseCase
                .ResponseValue> {

    private static final String KDM_ONLINE = "KDM_online";
    private static final String DOWNLOAD_SUFFIX = ".gdl";
    private static final String ASSETMAP = "ASSETMAP";

    private final GetFilmDetailUseCase mGetFilmDetailUseCase;
    private final GetValidOrderUseCase mGetValidOrderUseCase;
    private final RefreshOrderUseCase mRefreshOrderUseCase;
    private final GetPlayTicketUseCase mGetPlayTicketUseCase;
    private final GetPlayTokenUseCase mGetPlayTokenUseCase;
    private final ReportTicketStatusUseCase mReportTicketStatusUseCase;
    private final GetDownloadTaskInfoUseCase mGetDownloadTaskInfoUseCase;
    private final CheckDownloadFinishUseCase mCheckDownloadFinishUseCase;
    private final DownloadFileUseCase mDownloadFileUseCase;
    private final KDM mKDM;

    private final BaseSchedulerProvider mSchedulerProvider;

    public GetPlaybackValidityUseCase(GetFilmDetailUseCase getFilmDetailUseCase,
            @NonNull GetValidOrderUseCase getValidOrderUseCase,
            @NonNull RefreshOrderUseCase refreshOrderUseCase,
            @NonNull GetPlayTicketUseCase getPlayTicketUseCase,
            @NonNull GetPlayTokenUseCase getPlayTokenUseCase,
            @NonNull ReportTicketStatusUseCase reportTicketStatusUseCase,
            @NonNull GetDownloadTaskInfoUseCase getDownloadTaskInfoUseCase,
            @NonNull CheckDownloadFinishUseCase checkDownloadFinishUseCase,
            @NonNull DownloadFileUseCase downloadFileUseCase, @NonNull KDM kdm,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        super(schedulerProvider);
        mGetFilmDetailUseCase = checkNotNull(getFilmDetailUseCase);
        mGetValidOrderUseCase = checkNotNull(getValidOrderUseCase);
        mRefreshOrderUseCase = checkNotNull(refreshOrderUseCase);
        mGetPlayTicketUseCase = checkNotNull(getPlayTicketUseCase);
        mGetPlayTokenUseCase = checkNotNull(getPlayTokenUseCase);
        mReportTicketStatusUseCase = checkNotNull(reportTicketStatusUseCase);
        mGetDownloadTaskInfoUseCase = checkNotNull(getDownloadTaskInfoUseCase);
        mCheckDownloadFinishUseCase = checkNotNull(checkDownloadFinishUseCase);
        mDownloadFileUseCase = checkNotNull(downloadFileUseCase);
        mSchedulerProvider = checkNotNull(schedulerProvider);
        mKDM = checkNotNull(kdm);
    }

    @Override
    protected Observable<ResponseValue> executeUseCase(final RequestValues requestValues) {

        // #1. get film detail & get valid order
        // #2.1. if no valid order, return overdue;
        // #2.2. else, return check media encryption type;
        // #3.1. if no encryption. return OK.
        // #3.2. if KDM encryption. return KDM's PlaybackValidity.

        final ObjectWarp<Media> mediaCache = new ObjectWarp<>();
        final ObjectWarp<List<Media>> mediaListCache = new ObjectWarp<>();

        final String filmId = requestValues.getFilmId();
        final String mediaId = requestValues.getMediaId();
        Logger.d("mediaId : " + mediaId);
        // get film detail
//        return mFilmsDataSource.getFilmDetail(filmId)
        return mGetFilmDetailUseCase.run(new GetFilmDetailUseCase.RequestValues(filmId))
                .map(new Func1<GetFilmDetailUseCase.ResponseValue, Film>() {
                    @Override
                    public Film call(GetFilmDetailUseCase.ResponseValue responseValue) {
                        return responseValue.getFilm();
                    }
                })
                .concatMap(new Func1<Film, Observable<List<Order>>>() {
                    @Override
                    public Observable<List<Order>> call(Film film) {
                        Media media = null;
                        List<Media> mediaList = film.getMedias();
                        mediaListCache.setObject(mediaList);
                        if (mediaList != null && !mediaList.isEmpty()) {
                            for (Media m : mediaList) {
                                String id = m.getId();
                                if (!StringUtils.isNullOrEmpty(id) && mediaId.equals(id)) {
                                    media = m;
                                    mediaCache.setObject(m);
                                    break;
                                }
                            }
                        }

                        String mediaType = media != null ? media.getType() : null;
                        boolean isOnline = StringUtils.isNullOrEmpty(mediaType)
                                || Media.MEDIA_TYPE_ONLINE.equals(mediaType);
                        String productType = isOnline ? Order.PRODUCT_TYPE_THEATRE_ONLINE
                                : Order.PRODUCT_TYPE_THEATRE_DOWNLOAD;
                        // get valid order
//                        return mOrdersDataSource.getValidOrders(filmId, productType);
                        return mGetValidOrderUseCase.run(
                                new GetValidOrderUseCase.RequestValues(filmId, productType))
                                .map(new Func1<GetValidOrderUseCase.ResponseValue, List<Order>>() {
                                    @Override
                                    public List<Order> call(GetValidOrderUseCase.ResponseValue
                                            responseValue) {
                                        return responseValue.getOrders();
                                    }
                                });
                    }
                })
                // filter order
                .map(new Func1<List<Order>, Order>() {
                    @Override
                    public Order call(List<Order> orders) {
                        if (orders != null && !orders.isEmpty()) {
                            for (Order order : orders) {
                                if (null == order) {
                                    continue;
                                }

                                if (order.isValid()) {
                                    String encryption = mediaCache.getObject().getEncryption();
                                    // no encryption || voole
                                    if (StringUtils.isNullOrEmpty(encryption)
                                            || Media.TYPE_NO_ENCRYPT.equals(encryption)
                                            || Media.TYPE_VOOLE.equals(encryption)) {
                                        return order;
                                    } else {
                                        if (!StringUtils.isNullOrEmpty(order.getMediaResourceId())
                                                && mediaId.equals(order.getMediaResourceId())) {
                                            return order;
                                        }
                                    }
                                }
                            }
                        }
                        return null;
                    }
                })
                // check media encryption type
                .concatMap(new Func1<Order, Observable<PlaybackValidity>>() {
                    @Override
                    public Observable<PlaybackValidity> call(final Order order) {
                        //if no valid order, return overdue;
                        if (null == order) { // no order
                            PlaybackValidity value = PlaybackValidity.generatePlaybackNotValid(
                                    PlaybackValidity.ERR_NO_VALID_ORDER);
                            return Observable.just(value);
                        } else if (!order.isValid()) { // order not valid
                            PlaybackValidity value = PlaybackValidity.generatePlaybackNotValid(
                                    PlaybackValidity.ERR_OVERDUE);
                            return Observable.just(value);
                        }

                        final String orderSerial = order.getSerial();
                        final Media media = mediaCache.getObject();
                        final String mediaType = media.getType();
                        final String mediaName = media.getName();
//                        final String mediaUrl = media.getUrl();
                        final String encryption = media.getEncryption();
                        final List<Media> mediaList = mediaListCache.getObject();
                        final String dataDir = requestValues.getDataDir();

                        // check encryption type
                        if (StringUtils.isNullOrEmpty(encryption) ||
                                !Media.TYPE_NO_ENCRYPT.equals(encryption)
                                        && !Media.TYPE_KDM.equals(encryption)
                                        && !Media.TYPE_VOOLE.equals(encryption)) {
                            // not support type
                            PlaybackValidity value =
                                    PlaybackValidity.generatePlaybackNotValid(
                                            PlaybackValidity.ERR_TYPE_NOT_SUPPORT);
                            return Observable.just(value);
                        }

                        return getPlaybackValidity(orderSerial, order.getStatus(), media, mediaType,
                                mediaName, mediaList, dataDir, filmId);
                    }
                })
                .map(new Func1<PlaybackValidity, ResponseValue>() {
                    @Override
                    public ResponseValue call(PlaybackValidity playbackValidity) {
                        return new ResponseValue(playbackValidity);
                    }
                })
//                .subscribeOn(mSchedulerProvider.io())
                ;
    }

    private Observable<PlaybackValidity> getPlaybackValidity(final String orderSerial,
            String orderStatus, final Media media, String mediaType, String mediaName,
            final List<Media> mediaList, final String dataDir, final String filmId) {

        final Observable<PlaybackValidity> playbackValidityObs = getPlaybackValidityObs(filmId,
                media, mediaList, orderSerial, dataDir);

        // order is finish
        if (!StringUtils.isNullOrEmpty(orderStatus) && Order.STATUS_PAY_FINISH.equals(
                orderStatus)) {
            return playbackValidityObs;
        }

        // try to get ticket
        boolean isOnline = StringUtils.isNullOrEmpty(mediaType)
                || Media.MEDIA_TYPE_ONLINE.equals(mediaType);
        String licenseId = isOnline ? Media.MEDIA_TYPE_ONLINE : Media.MEDIA_TYPE_DOWNLOAD;

        final ObjectWarp<String> eNoteWarp = new ObjectWarp<>();
        final ObjectWarp<Throwable> throwableWarp = new ObjectWarp<>();

        // get ticket
        return mGetPlayTicketUseCase.run(
                new GetPlayTicketUseCase.RequestValues(filmId, mediaName, orderSerial, licenseId))
                .onErrorReturn(new Func1<Throwable, GetPlayTicketUseCase.ResponseValue>() {
                    @Override
                    public GetPlayTicketUseCase.ResponseValue call(Throwable throwable) {
                        throwableWarp.setObject(throwable);
                        // rest exception
                        if (throwable instanceof RestApiException) {
                            String eNote = ((RestApiException) throwable).getNote();
                            eNoteWarp.setObject(eNote);
                        }
                        return null;
                    }
                })
                .concatMap(new Func1<GetPlayTicketUseCase.ResponseValue,
                        Observable<PlaybackValidity>>() {
                    @Override
                    public Observable<PlaybackValidity> call(
                            final GetPlayTicketUseCase.ResponseValue response) {
                        String eNote = null;
                        Ticket ticket = null;
                        if (response != null) {
                            ticket = response.getTicket();
                            if (ticket != null) {
                                eNote = ticket.getError().getNote();
                            }
                        } else {
                            Throwable throwable = throwableWarp.getObject();
                            if (throwable != null) {
                                // rest exception
                                if (throwable instanceof RestApiException) {
                                    RestApiException apiException = (RestApiException) throwable;
                                    eNote = apiException.getNote();
                                } else {
                                    // re-throw the original error
                                    return Observable.error(throwable);
                                }
                            }
                        }

                        // ticket is overdue
                        if (!StringUtils.isNullOrEmpty(eNote)
                                && (Ticket.TICKET_STATUS_OVERDUE.equals(eNote)
                                || Ticket.TICKET_STATUS_OVERDUE_PLAY.equals(eNote))) {

                            // refresh order
                            return mRefreshOrderUseCase.run(
                                    new RefreshOrderUseCase.RequestValues(orderSerial))
                                    .concatMap(new Func1<RefreshOrderUseCase.ResponseValue,
                                            Observable<? extends PlaybackValidity>>() {
                                        @Override
                                        public Observable<? extends PlaybackValidity> call(
                                                RefreshOrderUseCase.ResponseValue responseValue) {
                                            String ticketStr = null;
                                            Ticket ticket = null;
                                            if (response != null) {
                                                ticket = response.getTicket();
                                            } else {
                                                Throwable throwable = throwableWarp.getObject();
                                                if (throwable != null) {
                                                    // rest exception
                                                    if (throwable instanceof RestApiException) {
                                                        RestApiException apiException =
                                                                (RestApiException) throwable;
                                                        Object object = apiException.getObject();
                                                        if (object instanceof Ticket) {
                                                            ticket = (Ticket) object;
                                                        }
                                                    }
                                                }
                                            }

                                            if (ticket != null) {
                                                ticketStr = ticket.getTicketstring();
                                            }

                                            final PlaybackValidity validity =
                                                    PlaybackValidity.generatePlaybackNotValid(
                                                            PlaybackValidity.ERR_OVERDUE);

                                            // no ticketStr
                                            if (StringUtils.isNullOrEmpty(ticketStr)) {
                                                return Observable.just(validity);
                                            }

                                            // report ticket status
                                            return reportTicketStatusObs(orderSerial, ticketStr)
                                                    .concatMap(new Func1<ReportTicketStatusUseCase
                                                            .ResponseValue,
                                                            Observable<PlaybackValidity>>() {
                                                        @Override
                                                        public Observable<PlaybackValidity> call(
                                                                ReportTicketStatusUseCase
                                                                        .ResponseValue
                                                                        responseValue) {
                                                            // return the original ticket response
                                                            return Observable.just(validity);
                                                        }
                                                    });
                                        }
                                    });
                        }

                        // return the original ticket response
                        return playbackValidityObs;
                    }
                });
    }

    private Observable<GetPlayTicketUseCase.ResponseValue> getTicketObs(String filmId,
            String mediaName, final String orderSerial, String licenseId) {
        final ObjectWarp<String> eNoteWarp = new ObjectWarp<>();
        final ObjectWarp<Throwable> throwableWarp = new ObjectWarp<>();

        // get ticket
        return mGetPlayTicketUseCase.run(
                new GetPlayTicketUseCase.RequestValues(filmId, mediaName, orderSerial, licenseId))
                .onErrorReturn(new Func1<Throwable, GetPlayTicketUseCase.ResponseValue>() {
                    @Override
                    public GetPlayTicketUseCase.ResponseValue call(Throwable throwable) {
                        Logger.w(throwable, "GetPlayTicket, onErrorReturn : ");
                        throwableWarp.setObject(throwable);
                        // rest exception
                        if (throwable instanceof RestApiException) {
                            String eNote = ((RestApiException) throwable).getNote();
                            eNoteWarp.setObject(eNote);
                            // ticket is overdue
                            if (!StringUtils.isNullOrEmpty(eNote)
                                    && (Ticket.TICKET_STATUS_OVERDUE.equals(eNote)
                                    || Ticket.TICKET_STATUS_OVERDUE_PLAY.equals(eNote))) {
                            }
                        }
                        return null;
                    }
                })
                .concatMap(
                        new Func1<GetPlayTicketUseCase.ResponseValue, Observable<? extends
                                GetPlayTicketUseCase.ResponseValue>>() {
                            @Override
                            public Observable<? extends GetPlayTicketUseCase.ResponseValue> call(
                                    final GetPlayTicketUseCase.ResponseValue response) {
                                String eNote = null;
                                Ticket ticket = null;
                                if (response != null) {
                                    ticket = response.getTicket();
                                    if (ticket != null) {
                                        eNote = ticket.getError().getNote();
                                    }
                                } else {
                                    Throwable throwable = throwableWarp.getObject();
                                    if (throwable != null) {
                                        // rest exception
                                        if (throwable instanceof RestApiException) {
                                            RestApiException apiException =
                                                    (RestApiException) throwable;
                                            eNote = apiException.getNote();
                                            Object object = apiException.getObject();
                                            if (object instanceof Ticket) {
                                                ticket = (Ticket) object;
                                            }
                                        } else {
                                            // re-throw the original error
                                            return Observable.error(throwable);
                                        }
                                    }
                                }


                                // ticket is overdue
                                if (!StringUtils.isNullOrEmpty(eNote)
                                        && (Ticket.TICKET_STATUS_OVERDUE.equals(eNote)
                                        || Ticket.TICKET_STATUS_OVERDUE_PLAY.equals(eNote))) {
                                    String ticketStr = null;
                                    if (ticket != null) {
                                        ticketStr = ticket.getTicketstring();
                                    }

                                    // report ticket status
                                    return reportTicketStatusObs(orderSerial, ticketStr)
                                            .concatMap(new Func1<ReportTicketStatusUseCase
                                                    .ResponseValue, Observable<? extends
                                                    GetPlayTicketUseCase.ResponseValue>>() {
                                                @Override
                                                public Observable<? extends GetPlayTicketUseCase
                                                        .ResponseValue> call(
                                                        ReportTicketStatusUseCase.ResponseValue
                                                                responseValue) {
                                                    // return the original ticket response
                                                    return Observable.just(response);
                                                }
                                            });
                                }

                                // return the original ticket response
                                return Observable.just(response);
                            }
                        });
    }

    /**
     * Get playback validity
     */
    private Observable<PlaybackValidity> getPlaybackValidityObs(String filmId, Media media,
            List<Media> mediaList, String orderSerial, String dataDir) {
        final String mediaId = media.getId();
        final String mediaType = media.getType();
        final String mediaName = media.getName();
        final String mediaUrl = media.getUrl();
        final String encryption = media.getEncryption();

        final boolean isDownload = !StringUtils.isNullOrEmpty(mediaType)
                && Media.MEDIA_TYPE_DOWNLOAD.equals(mediaType);
        if (isDownload) { //download
            return getDownloadPlaybackValidity(filmId, mediaId, mediaUrl,
                    encryption, mediaType, mediaName, orderSerial);
        } else { // online
            return getOnlinePlaybackValidity(filmId, mediaId, media.getUrl(), encryption, mediaType,
                    mediaName, orderSerial, dataDir);
        }
    }

    @Nullable
    /**
     * Get online playback validity
     */
    private Observable<PlaybackValidity> getOnlinePlaybackValidity(String filmId, String mediaId,
            String mediaUrl, String encryption, String mediaType, String mediaName,
            String orderSerial, String dataDir) {
        if (StringUtils.isNullOrEmpty(encryption)) {
            return null;
        }

        // no encryption || voole
        if (Media.TYPE_NO_ENCRYPT.equals(encryption) || Media.TYPE_VOOLE.equals(encryption)) {
            // return OK.
            PlaybackValidity playbackValidity = new PlaybackValidity(true, true,
                    PlaybackValidity.ERR_OK, 0, 0, mediaUrl);
            return Observable.just(playbackValidity);
        }
        // KDM
        else if (Media.TYPE_KDM.equals(encryption)) {
            return getKdmOnlinePlaybackValidity(filmId, mediaId, mediaUrl, mediaType, mediaName,
                    orderSerial, dataDir);
        }
        return null;
    }

    /**
     * Get download playback validity
     */
    private Observable<PlaybackValidity> getDownloadPlaybackValidity(final String filmId,
            final String mediaId, final String mediaUrl, final String encryption,
            final String mediaType, final String mediaName, final String orderSerial) {

        final ObjectWarp<Boolean> isFinished = new ObjectWarp<>();

        // check download finish?
//        return mDownloadDataSource.isDownloadFinished(filmId, mediaId)
        return mCheckDownloadFinishUseCase.run(
                new CheckDownloadFinishUseCase.RequestValues(filmId, mediaId))
                .map(new Func1<CheckDownloadFinishUseCase.ResponseValue, Boolean>() {
                    @Override
                    public Boolean call(CheckDownloadFinishUseCase.ResponseValue
                            responseValue) {
                        return responseValue.isFinished();
                    }
                })
                .flatMap(new Func1<Boolean, Observable<DownloadTaskInfo>>() {
                    @Override
                    public Observable<DownloadTaskInfo> call(Boolean aBoolean) {
                        isFinished.setObject(aBoolean);
                        // get download info
//                        return mDownloadDataSource.getDownloadTaskInfo(filmId, mediaId);
                        return mGetDownloadTaskInfoUseCase.run(new GetDownloadTaskInfoUseCase
                                .RequestValues(filmId, mediaId))
                                .map(new Func1<GetDownloadTaskInfoUseCase.ResponseValue,
                                        DownloadTaskInfo>() {
                                    @Override
                                    public DownloadTaskInfo call(GetDownloadTaskInfoUseCase
                                            .ResponseValue responseValue) {
                                        return responseValue.getDownloadTaskInfo();
                                    }
                                });
                    }
                })
                // check download info
                .flatMap(new Func1<DownloadTaskInfo, Observable<PlaybackValidity>>() {
                    @Override
                    public Observable<PlaybackValidity> call(DownloadTaskInfo downloadTaskInfo) {

                        // no download task
                        if (null == downloadTaskInfo) {
                            PlaybackValidity value = PlaybackValidity.generatePlaybackNotValid(
                                    PlaybackValidity.ERR_DOWNLOAD_NO_TASK);
                            return Observable.just(value);
                        }

                        String filePath = downloadTaskInfo.getFilePath();

                        // file not found
                        if (StringUtils.isNullOrEmpty(filePath) || !new File(filePath).exists()) {
                            PlaybackValidity value = PlaybackValidity.generatePlaybackNotValid(
                                    PlaybackValidity.ERR_DOWNLOAD_FILE_NOT_FOUND);
                            return Observable.just(value);
                        }

                        // download not finish
                        if (!isFinished.getObject() || !downloadTaskInfo.isFinish()) {
                            PlaybackValidity value = PlaybackValidity.generatePlaybackNotValid(
                                    PlaybackValidity.ERR_DOWNLOAD_NOT_FINISH);
                            return Observable.just(value);
                        }

                        if (!StringUtils.isNullOrEmpty(encryption)) {
                            // no encryption || voole
                            if (Media.TYPE_NO_ENCRYPT.equals(encryption) || Media.TYPE_VOOLE.equals(
                                    encryption)) {
                                // return OK.
                                PlaybackValidity playbackValidity = new PlaybackValidity(true, true,
                                        PlaybackValidity.ERR_OK, 0, 0, mediaUrl);
                                return Observable.just(playbackValidity);
                            }
                            // KDM
                            else if (Media.TYPE_KDM.equals(encryption)) {
                                boolean isOnline = Media.MEDIA_TYPE_ONLINE.equals(mediaType);
                                // return KDM's getPlaybackValidity.
//                            Observable<PlaybackValidity> validPeriodObservable =
//                                    getKdmPlaybackValidity(filePath, filmId, mediaName,
//                                            orderSerial, isOnline);
//                            return validPeriodObservable.concatWith(
//                                    mKDM.getPlaybackValidity(filePath)).first();
                                return getKdmPlaybackValidity(filePath, filmId, mediaName,
                                        orderSerial, isOnline);
                            }
                        }
                        return null;
                    }
                });
    }

    /**
     * Get Kdm online playback validity.
     */
    private Observable<PlaybackValidity> getKdmOnlinePlaybackValidity(final String filmId,
            String mediaId, final String mediaUrl, final String mediaType, final String mediaName,
            final String orderSerial, String dataDir) {

        // Get Kdm online res path
        return getKdmOnlineResPath(filmId, mediaId, mediaUrl, mediaName, dataDir)
                // get Kdm playback validity
                .flatMap(new Func1<String, Observable<PlaybackValidity>>() {
                    @Override
                    public Observable<PlaybackValidity> call(String filePath) {
                        boolean isOnline = StringUtils.isNullOrEmpty(mediaType)
                                || Media.MEDIA_TYPE_ONLINE.equals(mediaType);
                        return getKdmPlaybackValidity(filePath, filmId, mediaName, orderSerial,
                                isOnline);
                    }
                })
                .subscribeOn(mSchedulerProvider.io());
    }

    @NonNull
    /**
     * Get kdm online res path
     */
    private Observable<String> getKdmOnlineResPath(String filmId, String mediaId,
            final String mediaUrl, final String mediaName, String dataDir) {

        final String tmpUrl;
        if (mediaUrl.endsWith(".xml")) {
            int pos = mediaUrl.lastIndexOf("/");
            tmpUrl = mediaUrl.substring(0, pos);
        } else {
            tmpUrl = mediaUrl;
        }

        // dataDir/KDM_ONLINE/filmId/mediaId
        final File kdmOnlineFile = new File(dataDir, KDM_ONLINE);
        final String filePath = new File(new File(kdmOnlineFile, filmId),
                mediaId).getAbsolutePath();
        return Observable.just(filePath)
                .observeOn(mSchedulerProvider.io())
                .doOnNext(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        File file = new File(filePath);
                        String uuid = getUuidFromMediaName(mediaName);

                        // delete old Kdm res
                        tryDeleteOldKdmRes(file, uuid);
                        if (!file.exists()) {
                            file.mkdirs();
                        }

                        // 创建一个以uuid命名的文件，写入DCP包的网络地址
                        File fileUuid = new File(filePath, uuid);

                        if (!fileUuid.exists()) {
                            FileOutputStream fos = null;
                            try {
                                fos = new FileOutputStream(fileUuid);
                                fos.write(tmpUrl.getBytes("UTF-8"));
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                if (fos != null) {
                                    try {
                                        fos.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                })
                .flatMap(new Func1<String, Observable<String>>() {
                    @Override
                    public Observable<String> call(final String filePath) {

                        boolean downloadFinish = false;
                        File[] subFiles = new File(filePath).listFiles();
                        if (subFiles != null) {
                            int length = subFiles.length;
                            Logger.d("getKdmOnlineResPath, file.listFiles().length : " + length);

                            // download files >= 4
                            if (length >= 4) {
                                downloadFinish = true;
                                for (File subFile : subFiles) {
                                    // check download finish
                                    if (subFile.getName().endsWith(DOWNLOAD_SUFFIX)) {
                                        downloadFinish = false;
                                        break;
                                    }
                                }
                            }
                        }

                        // download finish
                        if (downloadFinish) {
                            return Observable.just(filePath);
                        } else {
                            String url = tmpUrl + "/" + ASSETMAP;
                            Logger.d("getKdmOnlineResPath, download url : " + url);
                            // download ASSETMAP file
//                            return mDownloadDataSource.downloadFile(0, url)
                            return downloadFileObs(url)
                                    .observeOn(mSchedulerProvider.io())
                                    .flatMap(new Func1<ResponseBody, Observable<String>>() {
                                        @Override
                                        public Observable<String> call(ResponseBody responseBody) {
                                            final String truePath = new File(filePath,
                                                    ASSETMAP).getAbsolutePath();
                                            final File tmpFile = new File(
                                                    truePath + DOWNLOAD_SUFFIX);
                                            write2File(responseBody, tmpFile);
                                            tmpFile.renameTo(new File(truePath));
                                            List<String> urls = null;
                                            try {
                                                // parse Kdm asset
                                                urls = parseKdmASSETMAP(truePath);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }

                                            if (urls != null && !urls.isEmpty()) {
                                                return Observable.from(urls);
                                            }
                                            return null;
                                        }
                                    })
                                    // download files in ASSETMAP file
                                    .flatMap(new Func1<String, Observable<?>>() {
                                        @Override
                                        public Observable<?> call(final String s) {
                                            String url = tmpUrl + "/" + s;
                                            Logger.d(
                                                    "getKdmOnlineResPath, download url : " + url);
//                                            return mDownloadDataSource.downloadFile(0, url)
                                            return downloadFileObs(url)
                                                    .observeOn(mSchedulerProvider.io())
                                                    .doOnNext(new Action1<ResponseBody>() {
                                                        @Override
                                                        public void call(
                                                                ResponseBody responseBody) {
                                                            final String truePath = new File(
                                                                    filePath, s).getAbsolutePath();
                                                            final File tmpFile = new File(
                                                                    truePath + DOWNLOAD_SUFFIX);
                                                            write2File(responseBody, tmpFile);
                                                            tmpFile.renameTo(new File(truePath));
                                                        }
                                                    });
                                        }
                                    })
                                    .toList()
                                    .flatMap(new Func1<Object, Observable<String>>() {
                                        @Override
                                        public Observable<String> call(Object o) {
                                            return Observable.just(filePath);
                                        }
                                    });
                        }
                    }
                });
    }

    /**
     * Get kdm play back validity and auto try to re-active it(if still not valid, then report to
     * server).
     */
    private Observable<PlaybackValidity> getKdmPlaybackValidity(final String filePath,
            final String filmId, final String mediaName, final String orderSerial,
            final boolean isOnline) {
        // get kdm playback validity
        return mKDM.getPlaybackValidity(filePath)
                .concatMap(new Func1<PlaybackValidity, Observable<? extends PlaybackValidity>>() {
                    @Override
                    public Observable<? extends PlaybackValidity> call(
                            PlaybackValidity playbackValidity) {
                        // valid
                        if (playbackValidity.isUnlimited() || playbackValidity.isValid()) {
                            return Observable.just(playbackValidity);
                        }

                        // try re-active playback
                        return tryReActiveKdmPlayback(filePath, isOnline, filmId, mediaName,
                                orderSerial);
                    }
                })
                .subscribeOn(mSchedulerProvider.io());
    }

    private Observable<? extends PlaybackValidity> tryReActiveKdmPlayback(final String filePath,
            boolean isOnline, String filmId, String mediaName, final String orderSerial) {
        final ObjectWarp<Ticket> ticketCache = new ObjectWarp<>();
        final String lisenceId = isOnline ? Media.MEDIA_TYPE_ONLINE : Media.MEDIA_TYPE_DOWNLOAD;
        Logger.d("tryReActiveKdmPlayback, get ticket");
        // get ticket
        return mGetPlayTicketUseCase.run(
                new GetPlayTicketUseCase.RequestValues(filmId, mediaName, orderSerial, lisenceId))
                .map(new Func1<GetPlayTicketUseCase.ResponseValue, Ticket>() {
                    @Override
                    public Ticket call(GetPlayTicketUseCase.ResponseValue responseValue) {
                        Ticket ticket = responseValue.getTicket();
                        ticketCache.setObject(ticket);
                        return ticket;
                    }
                })
                // get token
                .concatMap(new Func1<Ticket, Observable<Ticket>>() {
                    @Override
                    public Observable<Ticket> call(Ticket ticket) {
                        Logger.d("tryReActiveKdmPlayback, get token");
                        return mGetPlayTokenUseCase.run(
                                new GetPlayTokenUseCase.RequestValues(ticket.getTicketstring(),
                                        lisenceId, "", ""))
                                .map(new Func1<GetPlayTokenUseCase.ResponseValue, Ticket>() {
                                    @Override
                                    public Ticket call(GetPlayTokenUseCase.ResponseValue response) {
                                        return response.getTicket();
                                    }
                                });
                    }
                })
                .observeOn(mSchedulerProvider.io())
                // set kdm token
                .concatMap(new Func1<Ticket, Observable<KDMResCode>>() {
                    @Override
                    public Observable<KDMResCode> call(Ticket ticket) {
                        Logger.d("tryReActiveKdmPlayback, set Kdm Token");
                        return mKDM.setKdmTokenByPath(filePath, ticket.getTickettoken())
                                .onErrorReturn(new Func1<Throwable, KDMResCode>() {
                                    @Override
                                    public KDMResCode call(Throwable throwable) {
                                        Logger.w(throwable, "setKdmTokenByPath, onErrorReturn : ");
                                        return null;
                                    }
                                });
                    }
                })
                .observeOn(mSchedulerProvider.io())
                // re-get kdm playback validity
                .concatMap(new Func1<KDMResCode, Observable<? extends PlaybackValidity>>() {
                    @Override
                    public Observable<? extends PlaybackValidity> call(KDMResCode ticket) {
                        Logger.d("tryReActiveKdmPlayback, re-get kdm playback validity");
                        return mKDM.getPlaybackValidity(filePath);
                    }
                })
                .concatMap(new Func1<PlaybackValidity, Observable<? extends PlaybackValidity>>() {
                    @Override
                    public Observable<? extends PlaybackValidity> call(
                            final PlaybackValidity playbackValidity) {
                        boolean valid =
                                playbackValidity.isUnlimited() || playbackValidity.isValid();
                        return Observable.just(valid)
                                .concatMap(new Func1<Boolean, Observable<?>>() {
                                    @Override
                                    public Observable<?> call(Boolean isValid) {
                                        // not valid
                                        if (!isValid) {
                                            String ticketstring =
                                                    ticketCache.getObject().getTicketstring();
                                            // report ticket status to server
                                            return reportTicketStatusObs(orderSerial, ticketstring);
                                        }

                                        return Observable.just(null);
                                    }
                                })
                                .concatMap(new Func1<Object, Observable<? extends PlaybackValidity>>
                                        () {
                                    @Override
                                    public Observable<? extends PlaybackValidity> call(Object o) {
                                        //  return the playback validity
                                        return Observable.just(playbackValidity);
                                    }
                                });
                    }
                });
    }

    private Observable<ReportTicketStatusUseCase.ResponseValue> reportTicketStatusObs(
            String orderSerial, String ticketstring) {
        Logger.d("report ticket status to server");
        return mReportTicketStatusUseCase.run(
                new ReportTicketStatusUseCase.RequestValues(orderSerial, ticketstring,
                        Ticket.TICKET_STATUS_PLAY_FINISH, ""))
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Logger.w(throwable, "reportTicketStatus doOnError : ");
                    }
                });
    }

    private Observable<ResponseBody> downloadFileObs(String url) {
        return mDownloadFileUseCase.run(new DownloadFileUseCase.RequestValues(0, url))
                .map(new Func1<DownloadFileUseCase.ResponseValue, ResponseBody>() {
                    @Override
                    public ResponseBody call(DownloadFileUseCase.ResponseValue responseValue) {
                        return responseValue.getResponseBody();
                    }
                });
    }

    /**
     *
     * @param name
     * @return
     */
    private String getUuidFromMediaName(String name) {
        int startPosition = name.lastIndexOf(":");
        return name.substring(startPosition + 1);
    }

    private static void tryDeleteOldKdmRes(File file, String newUuid) {
        if (null == file || !file.exists() || StringUtils.isNullOrEmpty(newUuid)) {
            return;
        }

        boolean isChangeRes = true;
        File[] files = file.listFiles();
        if (file != null && files.length > 0) {
            for (File fileItem : files) {
                String name = fileItem.getName();
                if (!StringUtils.isNullOrEmpty(name) && name.equals(newUuid)) {
                    isChangeRes = false;
                }
            }
        }

        if (isChangeRes) {
            FileUtils.deleteRecursively(file);
//            for (File fileItem : file.listFiles()) {
//                fileItem.delete();
//            }
        }
    }

    /**
     * 解析下载好的 ASSETMAP 文件，获取其他需要下载的文件
     */
    private List<String> parseKdmASSETMAP(String path) throws Exception {
        if (StringUtils.isNullOrEmpty(path) || !new File(path).exists()) {
            return null;
        }

        List<String> filePaths = new ArrayList<>();
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        XmlPullParser xpp = factory.newPullParser();
        InputStream is = new FileInputStream(path);
        xpp.setInput(is, null);
        int eventType = xpp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                String name = xpp.getName();
                if (!StringUtils.isNullOrEmpty(name) && "Path".equals(name)) {
                    String str = xpp.nextText();
                    if (!StringUtils.isNullOrEmpty(str) && str.endsWith(".xml")) {
                        filePaths.add(str);
                    }
                }
            }
            try {
                eventType = xpp.next();
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
        if (is != null) {
            try {
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return filePaths;
    }

    public static class RequestValues implements UseCase.RequestValues {
        private final String mFilmId;
        private final String mMediaId;
        private final String mDataDir;

        public RequestValues(String filmId, String mediaId, String dataDir) {
            mFilmId = filmId;
            mMediaId = mediaId;
            mDataDir = dataDir;
        }

        String getFilmId() {
            return mFilmId;
        }

        String getMediaId() {
            return mMediaId;
        }

        String getDataDir() {
            return mDataDir;
        }
    }

    public static class ResponseValue implements UseCase.ResponseValue {
        private final PlaybackValidity mPlaybackValidity;

        public ResponseValue(PlaybackValidity playbackValidity) {
            mPlaybackValidity = playbackValidity;
        }

        public PlaybackValidity getPlaybackValidity() {
            return mPlaybackValidity;
        }
    }
}
