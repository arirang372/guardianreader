package com.guardian.reader.ui.details;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;
import com.guardian.reader.R;
import com.guardian.reader.models.GuardianContent;
import com.guardian.reader.models.Model;
import com.guardian.reader.utils.Utils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by john on 7/12/2017.
 */

public class DetailsNewsActivity extends AppCompatActivity
{
    private Unbinder unbinder;
    @BindView(R.id.wv_main)
    WebView wv_main;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.progressbar)
    ProgressBar progressbar;

    private GuardianContent content;
    private DetailsNewsPresenter presenter;

    public static void startActivity(Activity context, GuardianContent content)
    {
        Bundle b = new Bundle();
        b.putParcelable("news_contents", content);
        Utils.startActivity(context, DetailsNewsActivity.class, b);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        unbinder = ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        progressbar.setVisibility(View.VISIBLE);
        Bundle b = getIntent().getExtras();
        if(b != null)
        {
            init(b);
        }
        presenter.onCreate();
    }

    @Override
    public void onResume()
    {
        presenter.onResume();
        showNewsContent();
        super.onResume();
    }

    @Override
    public void onDestroy()
    {
        if(unbinder != null)
            unbinder.unbind();

        super.onDestroy();
    }


    private void showNewsContent()
    {
        toolbar.setTitle(content.webTitle);
        wv_main.getSettings().setJavaScriptEnabled(true);
        wv_main.loadUrl(content.webUrl);

    }

    private void init(Bundle b)
    {
        content =b.getParcelable("news_contents");
        presenter = new DetailsNewsPresenter(this, Model.getInstance(), content);
    }

    public void showProgress() {
        progressbar.setAlpha(1.0f);
        progressbar.setVisibility(View.VISIBLE);
    }

    public void hideProgress()
    {
        try {
            if (progressbar.getVisibility() != View.GONE) {
                progressbar.animate().alpha(0f).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationCancel(Animator animation) {
                        progressbar.setVisibility(View.GONE);
                    }
                });
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }


}
