package com.guardian.reader.ui.custom;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.guardian.reader.R;
import com.guardian.reader.models.DialogContent;

/**
 * Created by john on 6/14/2016.
 */
public class CustomDialog extends AlertDialog implements View.OnClickListener
{
    private TextView txtTitle;
    private TextView txtMessage;
    private Button btnYes;
    private Button btnNo;
    private CustomDialogListener listener;

    public interface CustomDialogListener
    {
        void onYesClicked(CustomDialog dialog);
        void onNoClicked(CustomDialog dialog);
    }

    public CustomDialog(Context context, DialogContent content, CustomDialogListener listener)
    {
        super(context, R.style.AppTheme);
        final Context themeContext = getContext();
        final LayoutInflater inflater = LayoutInflater.from(themeContext);
        final View view = inflater.inflate(R.layout.dialog_confirmation, null);
        setView(view);

        this.listener = listener;

        txtTitle = (TextView) view.findViewById(R.id.txtTitle);
        txtMessage = (TextView) view.findViewById(R.id.txtMessage);
        btnYes = (Button) view.findViewById(R.id.btnYes);
        btnNo = (Button) view.findViewById(R.id.btnNo);

        txtTitle.setText(content.title);
        txtMessage.setText(content.message);

        if(TextUtils.isEmpty(content.yesButtonText))
            btnYes.setText("Yes");
        else
            btnYes.setText(content.yesButtonText);

        if(TextUtils.isEmpty(content.noButtonText))
            btnNo.setText("No");
        else
            btnNo.setText(content.noButtonText);

        btnYes.setOnClickListener(this);
        btnNo.setOnClickListener(this);

    }

    @Override
    public void onClick(View v)
    {
       if( ((Button)v)== btnYes )
       {
           if(listener == null)
           {
               this.dismiss();
           }
           else
           {
               listener.onYesClicked(this);
           }
       }
       else if(((Button)v) == btnNo )
       {
           if(listener == null)
           {
               this.dismiss();
           }
           else
           {
               listener.onNoClicked(this);
           }
       }
    }
}
