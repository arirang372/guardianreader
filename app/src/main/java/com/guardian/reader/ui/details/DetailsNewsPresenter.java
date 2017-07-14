package com.guardian.reader.ui.details;

import com.guardian.reader.models.GuardianContent;
import com.guardian.reader.models.Model;
import com.guardian.reader.ui.Presenter;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
/**
 * Created by john on 7/12/2017.
 */

public class DetailsNewsPresenter implements Presenter
{
    private final DetailsNewsActivity view;
    private final Model model;
    private final GuardianContent content;
    private Subscription timerSubscription;

    public DetailsNewsPresenter(DetailsNewsActivity activity, Model model, GuardianContent content)
    {
        this.view = activity;
        this.model = model;
        this.content = content;
    }

    @Override
    public void onCreate()
    {
        this.view.showProgress();
    }

    @Override
    public void onResume()
    {
        timerSubscription = Observable.timer(2, TimeUnit.SECONDS)
                                      .observeOn(AndroidSchedulers.mainThread())
                                      .subscribe(new Action1<Long>() {
                                          @Override
                                          public void call(Long aLong) {
                                              model.markAsRead(content.sectionId, true);
                                              DetailsNewsPresenter.this.view.hideProgress();
                                          }
                                      });
    }

    @Override
    public void onPause()
    {
        if(timerSubscription != null)
            timerSubscription.unsubscribe();
    }



    @Override
    public void onDestroy() {

    }
}
