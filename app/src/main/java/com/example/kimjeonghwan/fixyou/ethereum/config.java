package com.example.kimjeonghwan.fixyou.ethereum;

public class config {

    public static String addressethnode(int node) {
        switch(node){
            case 1:
                return "http://176.74.13.102:18087";
            case 2:
                return "https://ropsten.infura.io/b700a711fe8f4d929e0c521961ef35c6";
            default:
                        return "https://mainnet.infura.io/avyPSzkHujVHtFtf8xwY";
        }
    }

    public static String addresssmartcontract(int contract) {
        switch (contract){
            case 1:
                return "0x9cfb304b9bf165cd630fa23a841e061a9a94ad1d";
            default :
                return "0x9cfb304b9bf165cd630fa23a841e061a9a94ad1d";
        }
    }

    public static String passwordwallet() {
        return "";
    }

}
