package com.example.kimjeonghwan.fixyou.ethereum;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.kimjeonghwan.fixyou.R;
import com.example.kimjeonghwan.fixyou.ethereum.callback.CBGetCredential;
import com.example.kimjeonghwan.fixyou.ethereum.callback.CBLoadSmartContract;
import com.example.kimjeonghwan.fixyou.ethereum.callback.CBSendingToken;
import com.example.kimjeonghwan.fixyou.ethereum.smartcontract.LoadSmartContract;
import com.example.kimjeonghwan.fixyou.ethereum.utils.InfoDialog;
import com.example.kimjeonghwan.fixyou.ethereum.utils.ToastMsg;
import com.example.kimjeonghwan.fixyou.ethereum.utils.qr.Generate;
import com.example.kimjeonghwan.fixyou.ethereum.utils.qr.ScanIntegrator;
import com.example.kimjeonghwan.fixyou.ethereum.wallet.Balance;
import com.example.kimjeonghwan.fixyou.ethereum.wallet.SendingToken;
import com.example.kimjeonghwan.fixyou.ethereum.web3j.Initiate;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Convert;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class WalletActivity extends AppCompatActivity implements View.OnClickListener, CBGetCredential, CBLoadSmartContract, CBSendingToken {
    private String mNodeUrl = config.addressethnode(2);
    private String mSmartcontract = config.addresssmartcontract(1);
    private String mPasswordwallet = config.passwordwallet();

    IntentIntegrator qrScan;
    String user_keystore;

    TextView eth_address_user, eth_token_user;
    EditText eth_address, eth_amount, eth_gasLimit, eth_gasPrice;
    Button sendToken;
    ImageView qr_small, qr_big;

    private File keydir;
    private InfoDialog mInfoDialog;
    private Credentials mCredentials;
    private Web3j mWeb3j;
    private BigInteger mGasPrice;
    private BigInteger mGasLimit;
    private SendingToken sendingToken;
    private ToastMsg toastMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        eth_address_user = findViewById(R.id.eth_address_user);
        eth_token_user = findViewById(R.id.eth_token_user);
        eth_address = findViewById(R.id.eth_address);
        eth_amount = findViewById(R.id.eth_amount);
        sendToken = findViewById(R.id.sendToken);
        qr_small = findViewById(R.id.qr_small);
        eth_gasLimit = findViewById(R.id.eth_gasLimit);
        eth_gasPrice = findViewById(R.id.eth_gasPrice);

        sendToken.setOnClickListener(this);
        mInfoDialog = new InfoDialog(this);
        qrScan = new IntentIntegrator(this);
        toastMsg = new ToastMsg();

        //user_keystore = getApplicationContext().getSharedPreferences("login_info", Context.MODE_PRIVATE).getString("keystore","none");  // SharedPreference 에서 이메일 정보를 가져온다.
        //gekeystoreList();

        GetFee();
        getWeb3j();

        keydir = this.getDir("wallet", MODE_PRIVATE);
        Log.e("keydir2", String.valueOf(keydir));
        getCredentials(keydir);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sendToken:
                Log.e("sendToken", "sendToken");
                sendToken();
                break;
            case R.id.qr_small:
                final Dialog dialog = new Dialog(this);
                dialog.setContentView(R.layout.qr_view);
                qr_big = (ImageView) dialog.findViewById(R.id.qr_big);
                qr_big.setImageBitmap(new Generate().Get(getEthAddress(),600,600));
                dialog.show();
                break;
            case R.id.qrScan:
                new ScanIntegrator(this).startScan();
                break;
        }
    }

    /* QR Scan */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
            } else {
                Log.e("result", String.valueOf(result));
                setToAddress(result.getContents().substring(9));
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /* Get Web3j*/
    private void getWeb3j(){
        new Initiate(mNodeUrl);
        mWeb3j = Initiate.sWeb3jInstance;
    }

    /* Get Credentials */
    private void getCredentials(File keydir){
        File[] listfiles = keydir.listFiles();
        for (int i = 0; i < listfiles.length; i ++){
            Log.e("file",i+String.valueOf(listfiles[0].getAbsoluteFile()));
        }
        try {
            mInfoDialog.Get("Load Wallet","Please wait few seconds");
            GetCredentials getCredentials = new GetCredentials();
            getCredentials.registerCallBack(this);
            getCredentials.FromFile(listfiles[0].getAbsolutePath(),mPasswordwallet);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CipherException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void backLoadCredential(Credentials credentials) {
        mCredentials = credentials;
        Log.e("backLoadCredential", "backLoadCredential");
        mInfoDialog.Dismiss();
        LoadWallet();
    }
    private void LoadWallet(){
        Log.e("LoadWallet", "LoadWallet");
        setEthAddress(getEthAddress());
        setEthBalance(getEthBalance());
        GetTokenInfo();
    }

    /* Get Address Ethereum */
    private String getEthAddress(){
        return mCredentials.getAddress();
    }

    /* Set Address Ethereum */
    private void setEthAddress(String address){
        eth_address_user.setText(address);
        qr_small.setImageBitmap(new Generate().Get(address,200,200));
    }

    private String getToAddress(){
        return eth_address.getText().toString();
    }

    private void setToAddress(String toAddress){
        eth_address.setText(toAddress);
    }

    /* Get Balance */
    private String getEthBalance(){
        try {
            return new Balance(mWeb3j,getEthAddress()).getInEther().toString();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getSendTokenAmmount(){
        return eth_amount.getText().toString();
    }

    public void GetFee(){
        setGasPrice(getGasPrice());
        setGasLimit(getGasLimit());

        BigDecimal fee = BigDecimal.valueOf(mGasPrice.doubleValue()*mGasLimit.doubleValue());
        BigDecimal feeresult = Convert.fromWei(fee.toString(),Convert.Unit.ETHER);
        //tv_fee.setText(feeresult.toPlainString() + " ETH");
    }

    private String getGasPrice(){
        return eth_gasPrice.getText().toString();
    }

    private void setGasPrice(String gasPrice){
        mGasPrice = Convert.toWei(gasPrice,Convert.Unit.GWEI).toBigInteger();
    }

    private String getGasLimit() {
        return eth_gasLimit.getText().toString();
    }

    private void setGasLimit(String gasLimit){
        mGasLimit = BigInteger.valueOf(Long.valueOf(gasLimit));
    }

    /*Get Token Info*/
    private void GetTokenInfo(){
        Log.e("GetTokenInfo", "GetTokenInfo");
        LoadSmartContract loadSmartContract = new LoadSmartContract(mWeb3j,mCredentials,mSmartcontract,mGasPrice,mGasLimit);
        loadSmartContract.registerCallBack(this);
        loadSmartContract.LoadToken();
    }

    /* Get Token*/
    @Override
    public void backLoadSmartContract(Map<String,String> result) {
        Log.e("token", result.get("tokenbalance"));
        setTokenBalance(result.get("tokenbalance"));
        //setTokenName(result.get("tokenname"));
        //setTokenSymbol(result.get("tokensymbol"));
    }

    private void setTokenBalance(String value){
        eth_token_user.setText(value);
    }

    /* Set Balance */
    private void setEthBalance(String ethBalance){
        //ethbalance.setText(ethBalance);
    }

    private void sendToken(){
        mInfoDialog.Get("Send Token","Please wait few seconds");
        sendingToken = new SendingToken(mWeb3j,
                mCredentials,
                getGasPrice(),
                getGasLimit());
        sendingToken.registerCallBackToken(this);
        sendingToken.Send(mSmartcontract,getToAddress(),getSendTokenAmmount());
    }

    @Override
    public void backSendToken(TransactionReceipt result) {
        //toastMsg.Long(this,result.getTransactionHash());
        mInfoDialog.Dismiss();
        LoadWallet();
    }
}
