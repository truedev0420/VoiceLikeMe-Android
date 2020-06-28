package com.appbestsmile.voicelikeme.chat;

import android.app.ProgressDialog;
import android.content.Context;

import com.appbestsmile.voicelikeme.R;

public class WaitProgressDialog extends ProgressDialog {

    public WaitProgressDialog(Context context, String message){
        super(context);

        setProgressStyle(ProgressDialog.STYLE_SPINNER);
        setIndeterminate(true);
        setTitle(R.string.app_name);
        setCanceledOnTouchOutside(false);
        setMessage(message);
    }
}
