package com.guardian.reader.models;

import com.guardian.reader.data.DataManager;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.realm.RealmResults;
/**
 * Created by john on 7/12/2017.
 */

public class Model
{
    private static Model instance;
    private final DataManager dm;
    private GuardianSection selectedSection;
    public static synchronized Model getInstance()
    {
        if(instance == null)
        {
            DataManager manager = new DataManager();
            instance = new Model(manager);
        }
        return instance;
    }

    private Model(DataManager manager)
    {
        dm = manager;
    }

    public Flowable<RealmResults<GuardianSection>> getAllSections()
    {
        return dm.loadAllSections();
    }

    public Observable<Boolean> isNetworkUsed()
    {
        return dm.networkInUse().distinctUntilChanged();
    }


    public void setSelectedSection(GuardianSection section)
    {
        selectedSection = section;
        dm.loadNewsFeed(selectedSection.id, false);
    }

    public GuardianSection getSelectedSection()
    {
        return this.selectedSection;
    }

    public Flowable<RealmResults<GuardianContent>> getSelectedNewsContent()
    {
        return dm.loadNewsFeed(selectedSection.id, false);
    }

    public  Flowable<RealmResults<GuardianContent>>  reloadNewsContent()
    {
        return dm.loadNewsFeed(selectedSection.id, true);
    }

    public void markAsRead(String id, boolean read)
    {
        dm.updateNewsReadState(id, read);
    }

}
