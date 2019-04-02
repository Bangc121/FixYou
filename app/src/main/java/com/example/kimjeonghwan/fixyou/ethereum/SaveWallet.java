package com.example.kimjeonghwan.fixyou.ethereum;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;

import java.io.File;
import java.io.IOException;

public class SaveWallet extends AsyncTask<Void,Void,Void>{

    private String mPasswordwallet;

    private File mKeystoredir;

    private Credentials mCredentials;

    private Context mContext;

    public SaveWallet(File keydir, Credentials credentials, String passwordwallet, Context context){
        mKeystoredir = keydir;
        mCredentials = credentials;
        mPasswordwallet = passwordwallet;
        mContext = context;
    }

    @Override
    protected Void doInBackground(Void... voids) {

        String FileWallet = null;
        try {
            FileWallet = WalletUtils.generateWalletFile(mPasswordwallet,mCredentials.getEcKeyPair(), mKeystoredir,false);
        } catch (CipherException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("BIP44 FILE Wallet: "+ FileWallet);
        Intent intent = new Intent(mContext, WalletActivity.class);
        mContext.startActivity(intent);
        ((Activity)mContext).finish();
        return null;
    }
}
