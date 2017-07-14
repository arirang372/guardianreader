package com.guardian.reader.rest;

import com.guardian.reader.models.GuardianContent;
import com.guardian.reader.models.GuardianSection;
import com.guardian.reader.rest.models.contents.GuardianContentResponse;
import com.guardian.reader.rest.models.contents.HttpContentResponse;
import com.guardian.reader.rest.models.sections.GuardianSectionResponse;
import com.guardian.reader.rest.models.sections.HttpSectionResponse;
import com.guardian.reader.utils.Utils;
import java.util.List;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import io.realm.Realm;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import rx.functions.Action1;
import rx.subjects.BehaviorSubject;
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
                                        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
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
                .map(new Func1<HttpSectionResponse, GuardianSectionResponse>()
                {
                    @Override
                    public GuardianSectionResponse call(HttpSectionResponse httpSectionResponse) {
                        return httpSectionResponse.response;
                    }
                })
                .flatMap(new Func1<GuardianSectionResponse, Observable<List<GuardianSection>>>() {
                    @Override
                    public Observable<List<GuardianSection>> call(GuardianSectionResponse guardianSectionResponse) {
                        return Observable.just(guardianSectionResponse.results);
                    }
                })
               .subscribe(new Action1<List<GuardianSection>>() {
                   @Override
                   public void call(List<GuardianSection> listGuardianResponse) {
                       LOGD(TAG, "Success - Section received ...");
                       networkInUse.onNext(false);
                       processNewsSections(realm, listGuardianResponse);
                   }
               }, new Action1<Throwable>() {
                   @Override
                   public void call(Throwable throwable) {
                       networkInUse.onNext(false);
                       LOGD(TAG, "Fail - error occurred ...");
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
                .map(new Func1<HttpContentResponse, GuardianContentResponse>()
                {
                    @Override
                    public GuardianContentResponse call(HttpContentResponse httpContentResponse)
                    {
                        return httpContentResponse.response;
                    }
                })
                .flatMap(new Func1<GuardianContentResponse, Observable<List<GuardianContent>>>()
                {
                    @Override
                    public Observable<List<GuardianContent>> call(GuardianContentResponse guardianContentResponse)
                    {
                        return Observable.just(guardianContentResponse.results);
                    }
                })
               .subscribe(new Action1<List<GuardianContent>>()
               {
                   @Override
                   public void call(List<GuardianContent> contents)
                   {
                       LOGD(TAG, String.format("Success - Data received : %s", sectionId) );
                       processNewsContents(realm, contents);
                       networkInUse.onNext(false);
                   }
               }, new Action1<Throwable>()
               {
                   @Override
                   public void call(Throwable throwable) {

                        networkInUse.onNext(false);
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
