package com.guardian.reader.ui.intro;

import com.guardian.reader.models.GuardianContent;
import com.guardian.reader.models.GuardianSection;
import com.guardian.reader.models.Model;
import com.guardian.reader.ui.Presenter;
import com.guardian.reader.ui.details.DetailsNewsActivity;
import java.util.List;
import io.realm.RealmResults;
import rx.Subscription;
import rx.functions.Action1;
/**
 * Created by john on 7/12/2017.
 */

public class MainPresenter implements Presenter
{
    private final MainActivity view;
    private final Model model;
    private Subscription loaderSubscription;
    private Subscription sectionSubscription;
    private Subscription contentSubscription;
    private List<GuardianContent> newsContents;

    public MainPresenter(MainActivity activity, Model model)
    {
        this.view = activity;
        this.model = model;
    }

    @Override
    public void onCreate()
    {
        sectionSubscription = model.getAllSections()
                                   .subscribe(new Action1<RealmResults<GuardianSection>>() {
                                       @Override
                                       public void call(RealmResults<GuardianSection> guardianSections)
                                       {
                                           view.setupToolBar(guardianSections);
                                       }
                                   });
    }

    @Override
    public void onPause()
    {
        if(loaderSubscription != null)
            loaderSubscription.unsubscribe();
        if(sectionSubscription != null)
            sectionSubscription.unsubscribe();
    }

    @Override
    public void onResume()
    {
        loaderSubscription = model.isNetworkUsed()
                                    .subscribe(new Action1<Boolean>()
                                    {
                                        @Override
                                        public void call(Boolean netyworkInUse)
                                        {
                                            view.showNetworkLoading(netyworkInUse);
                                        }
                                    });

        sectionSelected(model.getSelectedSection());
    }

    private void sectionSelected(GuardianSection section)
    {
        if(section == null)
            return;

        model.setSelectedSection(section);
        if(contentSubscription != null)
            contentSubscription.unsubscribe();

        contentSubscription = model.getSelectedNewsContent()
                                   .subscribe(new Action1<RealmResults<GuardianContent>>()
                                   {
                                       @Override
                                       public void call(RealmResults<GuardianContent> guardianContents)
                                       {
                                           newsContents = guardianContents;
                                           view.showList(newsContents);
                                       }
                                   });
    }

    @Override
    public void onDestroy()
    {

    }

    public void listItemSelected(int position)
    {
        DetailsNewsActivity.startActivity(view, newsContents.get(position) );
    }

    public void titleSpinnerSelectionSelected(GuardianSection section)
    {
        this.sectionSelected(section);
    }

    public void refreshList()
    {
        model.reloadNewsContent();
        view.hideProgress();
    }


}
