package com.guardian.reader;

import android.app.Application;

import io.reactivex.functions.Consumer;
import io.reactivex.plugins.RxJavaPlugins;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import static com.guardian.reader.utils.LogUtils.LOGD;

/**
 * Created by john on 7/11/2017
 */

public class GuardianApplication extends Application
{
    private static GuardianApplication instance;
    private final String TAG = this.getClass().getSimpleName();

    public static synchronized GuardianApplication getAppInstance()
    {
       return instance;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        instance = this;

       RxJavaPlugins.setErrorHandler(new Consumer<Throwable>() {
           @Override
           public void accept(Throwable e) throws Exception {
               LOGD(TAG, e.getMessage());
           }
       });

        Realm.init(this);
        RealmConfiguration realmConfig = new RealmConfiguration.Builder().build();
        Realm.setDefaultConfiguration(realmConfig);
    }


    @Override
    public void onLowMemory()
    {
        LOGD(TAG, "onLow memory....");
        super.onLowMemory();
    }

}
