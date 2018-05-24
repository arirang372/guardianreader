package com.guardian.reader.rest;

import com.guardian.reader.models.GuardianContent;
import com.guardian.reader.models.GuardianSection;
import com.guardian.reader.rest.models.contents.GuardianContentResponse;
import com.guardian.reader.rest.models.contents.HttpContentResponse;
import com.guardian.reader.rest.models.sections.GuardianSectionResponse;
import com.guardian.reader.rest.models.sections.HttpSectionResponse;
import com.guardian.reader.utils.Utils;
import java.util.List;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import io.realm.Realm;
import retrofit2.Retrofit;
import static com.guardian.reader.constants.AppMetadata.BASE_URL;
import static com.guardian.reader.utils.LogUtils.LOGD;

public class DataLoader
{
    private final String TAG = this.getClass().getSimpleName();
    private GuadianService service;
    private BehaviorSubject<Boolean> networkInUse;
    private Utils utils = Utils.getInstance();
    private String apiKey;
    private Realm realm;

    public DataLoader()
    {
        Retrofit retrofit = new Retrofit.Builder()
                                        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                                        .addConverterFactory(GsonConverterFactory.create())
                                        .baseUrl(BASE_URL)
                                        .build();

        service = retrofit.create(GuadianService.class);
    }

    public void loadNewsSections(String apiKey, Realm realm, BehaviorSubject<Boolean> networkLoading)
    {
        this.apiKey = apiKey;
        this.realm = realm;
        this.networkInUse = networkLoading;
        loadAllSections();
    }

    private void loadAllSections()
    {
        this.networkInUse.onNext(true);
        service.getSectionNames("", apiKey)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Function<HttpSectionResponse, GuardianSectionResponse>()
                {
                    @Override
                    public GuardianSectionResponse apply(HttpSectionResponse httpSectionResponse) {
                        return httpSectionResponse.response;
                    }
                })
                .flatMap(new Function<GuardianSectionResponse, Observable<List<GuardianSection>>>() {
                    @Override
                    public Observable<List<GuardianSection>> apply(GuardianSectionResponse guardianSectionResponse) {
                        return Observable.just(guardianSectionResponse.results);
                    }
                })
                .subscribe(new Observer<List<GuardianSection>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(List<GuardianSection> guardianSections) {
                        LOGD(TAG, "Success - Section received ...");
                        networkInUse.onNext(false);
                        processNewsSections(realm, guardianSections);
                    }

                    @Override
                    public void onError(Throwable e) {
                        networkInUse.onNext(false);
                        LOGD(TAG, "Fail - error occurred ...");
                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    private void processNewsSections(final Realm realm, final List<GuardianSection> sections)
    {
        if(sections.isEmpty())
            return;

        realm.executeTransactionAsync(new Realm.Transaction()
                                      {
                                          @Override
                                          public void execute(Realm realm)
                                          {
                                              for(GuardianSection s : sections)
                                              {
                                                  realm.copyToRealmOrUpdate(s);
                                              }
                                          }
                                      },
                new Realm.Transaction.OnError()
                {
                    @Override
                    public void onError(Throwable error)
                    {
                        LOGD(TAG, String.format("Error on saving the sections %s", error.getMessage()));
                    }
                });
    }


    public void loadNewsContents(String sectionId, String apiKey, Realm realm, BehaviorSubject<Boolean> networkLoading)
    {
        this.apiKey = apiKey;
        this.realm = realm;
        this.networkInUse = networkLoading;
        loadNextContents(sectionId);
    }

    private void loadNextContents(final String sectionId)
    {
        this.networkInUse.onNext(true);
        service.getNewsContents(sectionId, apiKey)
               .subscribeOn(Schedulers.io())
               .observeOn(AndroidSchedulers.mainThread())
                .map(new Function<HttpContentResponse, GuardianContentResponse>()
                {
                    @Override
                    public GuardianContentResponse apply(HttpContentResponse httpContentResponse)
                    {
                        return httpContentResponse.response;
                    }
                })
                .flatMap(new Function<GuardianContentResponse, Observable<List<GuardianContent>>>()
                {
                    @Override
                    public Observable<List<GuardianContent>> apply(GuardianContentResponse guardianContentResponse)
                    {
                        return Observable.just(guardianContentResponse.results);
                    }
                })
                .subscribe(new Observer<List<GuardianContent>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(List<GuardianContent> guardianContents) {
                        LOGD(TAG, String.format("Success - Data received : %s", sectionId) );
                        processNewsContents(realm, guardianContents);
                        networkInUse.onNext(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        networkInUse.onNext(false);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }


    private void processNewsContents(final Realm realm, final List<GuardianContent> contents)
    {
        if(contents.isEmpty())
            return;

        realm.executeTransactionAsync(new Realm.Transaction()
        {
            @Override
            public void execute(Realm realm)
            {
                for(GuardianContent c : contents)
                {
                    c.webPublicationDate = utils.reformatDate(c.webPublicationDate);

                    GuardianContent localContent = realm.where(GuardianContent.class)
                                                        .equalTo("id", c.id).findFirst();
                    if(localContent != null)
                    {
                        c.setIsRead(localContent.getIsRead());
                    }

                    if(localContent == null || !localContent.webPublicationDate.equals(c.webPublicationDate))
                    {
                        realm.copyToRealmOrUpdate(c);
                    }
                }
            }
        },
        new Realm.Transaction.OnError()
        {
            @Override
            public void onError(Throwable error)
            {
                LOGD(TAG, String.format("Error on saving the contents %s", error.getMessage()));
            }
        });
    }



}
