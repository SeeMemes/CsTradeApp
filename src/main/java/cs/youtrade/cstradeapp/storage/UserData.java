package cs.youtrade.cstradeapp.storage;

import cs.youtrade.cstradeapp.util.ProxyModel;

public class UserData {
    private final String uName;
    private final String pWord;
    private final String tmApiKey;
    private final String sharedSecret;
    private String accessToken;
    private String refreshToken;
    private boolean works;
    private ProxyModel proxyModel;

    public UserData(String uName, String pWord, String tmApiKey, String sharedSecret) {
        this.uName = uName;
        this.pWord = pWord;
        this.tmApiKey = tmApiKey;
        this.sharedSecret = sharedSecret;
        this.works = true;
    }

    public String getuName() {
        return uName;
    }

    public String getpWord() {
        return pWord;
    }

    public String getTmApiKey() {
        return tmApiKey;
    }

    public String getSharedSecret() {
        return sharedSecret;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public UserData setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        return this;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public UserData setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
        return this;
    }

    public boolean isWorks() {
        return works;
    }

    public void setWorks(boolean works) {
        this.works = works;
    }

    public ProxyModel getProxyModel() {
        return proxyModel;
    }

    public void setProxyModel(ProxyModel proxyModel) {
        this.proxyModel = proxyModel;
    }
}
