package com.guardian.reader.ui.intro;

import com.guardian.reader.models.GuardianContent;
import com.guardian.reader.models.GuardianSection;
import com.guardian.reader.models.Model;
import com.guardian.reader.ui.Presenter;
import com.guardian.reader.ui.details.DetailsNewsActivity;
import org.reactivestreams.Subscription;
import java.util.List;
import io.reactivex.FlowableSubscriber;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.realm.RealmResults;
/**
 * Created by john on 7/12/2017.
 */

public class MainPresenter implements Presenter
{
    private final MainActivity view;
    private final Model model;
    private List<GuardianContent> newsContents;

    public MainPresenter(MainActivity activity, Model model)
    {
        this.view = activity;
        this.model = model;
    }

    @Override
    public void onCreate()
    {
         model.getAllSections()
                 .toObservable()
                 .subscribe(new Observer<RealmResults<GuardianSection>>() {
             @Override
             public void onSubscribe(Disposable d) {

             }

             @Override
             public void onNext(RealmResults<GuardianSection> guardianSections) {
                 view.setupToolBar(guardianSections);
             }

             @Override
             public void onError(Throwable e) {
                e.printStackTrace();
             }

             @Override
             public void onComplete() {

             }
         });
    }

    @Override
    public void onPause() {

    }

    @Override
    public void onResume()
    {
        model.isNetworkUsed()
             .subscribe(new Observer<Boolean>() {
                 @Override
                 public void onSubscribe(Disposable d) {

                 }

                 @Override
                 public void onNext(Boolean netyworkInUse) {
                     view.showNetworkLoading(netyworkInUse);
                 }

                 @Override
                 public void onError(Throwable e) {

                 }

                 @Override
                 public void onComplete() {

                 }
             });

        sectionSelected(model.getSelectedSection());
    }

    private void sectionSelected(GuardianSection section)
    {
        if(section == null)
            return;

        model.setSelectedSection(section);
        model.getSelectedNewsContent()
                .toObservable()
                .subscribe(new Observer<RealmResults<GuardianContent>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(RealmResults<GuardianContent> guardianContents) {
                        newsContents = guardianContents;
                        view.showList(newsContents);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {

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
