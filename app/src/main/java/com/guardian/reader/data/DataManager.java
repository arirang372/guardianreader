package com.guardian.reader.data;

import com.guardian.reader.GuardianApplication;
import com.guardian.reader.R;
import com.guardian.reader.constants.AppMetadata;
import com.guardian.reader.models.GuardianContent;
import com.guardian.reader.models.GuardianSection;
import com.guardian.reader.rest.DataLoader;
import java.io.Closeable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import io.realm.RealmResults;
import io.realm.Sort;
import io.realm.Realm;
import static com.guardian.reader.utils.LogUtils.LOGD;

/**
 * Created by john on 7/11/2017.
 */

public class DataManager implements Closeable
{
    private Realm realm;
    private final DataLoader dataLoader;
    private final String apiKey;
    private Map<String, Long> lastNetworkRequest = new HashMap<>();
    private BehaviorSubject<Boolean> networkLoading = BehaviorSubject.create();
    private final String TAG = this.getClass().getSimpleName();
    private final PreferenceManager preferenceManager;

    public DataManager()
    {
        realm = Realm.getDefaultInstance();
        dataLoader = new DataLoader();
        apiKey = GuardianApplication.getAppInstance().getString(R.string.api_key);
        preferenceManager = new PreferenceManager( GuardianApplication.getAppInstance());
    }

    public Observable<Boolean> networkInUse()
    {
        return networkLoading;
    }

    public Flowable<RealmResults<GuardianSection>> loadAllSections()
    {
        if(!preferenceManager.getBooleanValue("is_loaded"))
        {
            dataLoader.loadNewsSections(apiKey, realm, networkLoading);
            preferenceManager.put("is_loaded", true);
        }

        return     realm.where(GuardianSection.class)
                        .findAllAsync()
                        .asFlowable();
    }


    public Flowable<RealmResults<GuardianContent>> loadNewsFeed(String sectionId, boolean forceReload)
    {
        if(forceReload || timeSinceLastRequest(sectionId) > AppMetadata.MINIMUM_NETWORK_WAIT_SEC)
        {
            dataLoader.loadNewsContents(sectionId, apiKey, realm, networkLoading);
            lastNetworkRequest.put(sectionId, System.currentTimeMillis());
        }

        return realm.where(GuardianContent.class)
                    .equalTo("sectionId", sectionId)
                    .findAllAsync()
                    .sort("webPublicationDate", Sort.DESCENDING)
                    .asFlowable();
    }

    private long timeSinceLastRequest(String sectionId)
    {
        Long lastRequest = lastNetworkRequest.get(sectionId);
        if(lastRequest != null)
            return TimeUnit.SECONDS.convert(System.currentTimeMillis() - lastRequest, TimeUnit.MILLISECONDS);
        else
            return Long.MAX_VALUE;
    }

    public void updateNewsReadState(final String id, final boolean read)
    {
        realm.executeTransactionAsync(new Realm.Transaction()
        {
            @Override
            public void execute(Realm realm)
            {
                GuardianContent news = realm.where(GuardianContent.class)
                                            .equalTo("id", id)
                                            .findFirst();
                if(news != null)
                {
                    news.setIsRead(read);
                }

            }
        }, new Realm.Transaction.OnError()
        {
            @Override
            public void onError(Throwable error)
            {
                LOGD(TAG, error.getMessage());
            }
        });
    }


    public void close()
    {
        realm.close();
    }

}
