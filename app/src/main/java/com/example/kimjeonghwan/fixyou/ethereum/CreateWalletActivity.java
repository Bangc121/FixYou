package com.example.kimjeonghwan.fixyou.ethereum;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.kimjeonghwan.fixyou.R;
import com.example.kimjeonghwan.fixyou.ethereum.callback.CBBip44;
import com.example.kimjeonghwan.fixyou.ethereum.utils.InfoDialog;
import com.example.kimjeonghwan.fixyou.ethereum.wallet.generate.Bip44;

import org.web3j.crypto.Credentials;

import java.io.File;
import java.util.Map;

public class CreateWalletActivity extends AppCompatActivity implements CBBip44 {
    private String mPasswordwallet = config.passwordwallet();

    EditText et_password1;
    EditText et_password2;
    Button btn_generate;
    TextView tv_mgs;

    private File keydir;
    private InfoDialog mInfoDialog;

    private Credentials mCredentials;
    final Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_wallet);

        mInfoDialog = new InfoDialog(this);
        et_password1 = findViewById(R.id.et_password1);
        et_password2 = findViewById(R.id.et_password2);
        btn_generate = findViewById(R.id.btn_generate);
        tv_mgs = findViewById(R.id.tv_mgs);

        keydir = this.getDir("wallet", MODE_PRIVATE);
        Log.e("keydir", String.valueOf(keydir));
        btn_generate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("btn_generate", "지갑생성중");
                CreateWallet();
            }
        });
    }

    /* Create Wallet */
    private void CreateWallet(){
        Bip44 bip44 = new Bip44();
        bip44.registerCallBack(this);
        bip44.execute(mPasswordwallet);
        mInfoDialog.Get("Wallet generation", "Please wait few seconds");
    }

    @Override
    public void backGeneration(Map<String, String> result, Credentials credentials) {
        mCredentials = credentials;
        Log.e("address", result.get("address"));
        new SaveWallet(keydir,mCredentials,mPasswordwallet, context).execute();
        mInfoDialog.Dismiss();

    }

    /* Set Address Ethereum */
    private void setEthAddress(String address){
    }

    /* Set Balance */
    private void setEthBalance(String ethBalance){
        //ethbalance.setText(ethBalance);
    }
}
