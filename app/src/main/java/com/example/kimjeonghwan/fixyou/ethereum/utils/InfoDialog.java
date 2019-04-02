package com.example.kimjeonghwan.fixyou.ethereum.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.v7.app.AlertDialog;

public class InfoDialog {

    private Context mContext;

    private ProgressDialog infodialog;

    public InfoDialog(Context context){
        mContext = context;
    }

    public void Get(String title, String message){
        infodialog = new ProgressDialog(mContext);
        infodialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        infodialog.setMessage(message);
        infodialog.setCanceledOnTouchOutside(false);
        infodialog.show();
    }

    public void Dismiss(){
        infodialog.dismiss();
    }

}
