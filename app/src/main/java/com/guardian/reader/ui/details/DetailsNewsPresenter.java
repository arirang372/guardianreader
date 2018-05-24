package com.guardian.reader.ui.details;

import com.guardian.reader.models.GuardianContent;
import com.guardian.reader.models.Model;
import com.guardian.reader.ui.Presenter;
import org.reactivestreams.Subscription;
import java.util.concurrent.TimeUnit;
import io.reactivex.Flowable;
import io.reactivex.FlowableSubscriber;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 * Created by john on 7/12/2017.
 */

public class DetailsNewsPresenter implements Presenter
{
    private final DetailsNewsActivity view;
    private final Model model;
    private final GuardianContent content;

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
        Flowable.timer(2, TimeUnit.SECONDS)
                  .toObservable()
                                      .observeOn(AndroidSchedulers.mainThread())
                                      .subscribe(new Observer<Long>() {
                                          @Override
                                          public void onSubscribe(Disposable d) {

                                          }

                                          @Override
                                          public void onNext(Long aLong) {
                                              model.markAsRead(content.id, true);
                                              DetailsNewsPresenter.this.view.hideProgress();
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
    public void onPause()
    {

    }

    @Override
    public void onDestroy() {

    }
}
