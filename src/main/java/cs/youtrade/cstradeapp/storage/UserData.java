package cs.youtrade.cstradeapp.storage;

import cs.youtrade.cstradeapp.util.ProxyModel;
import org.bouncycastle.asn1.x509.Time;

import java.time.LocalDateTime;
import java.time.temporal.TemporalField;

public class UserData {
    private final String uName;
    private final String pWord;
    private final String tmApiKey;
    private final String sharedSecret;
    private String accessToken;
    private String refreshToken;
    private long updateTime;
    private boolean works;
    private ProxyModel proxyModel;

    public UserData(String uName, String pWord, String tmApiKey, String sharedSecret) {
        this.uName = uName;
        this.pWord = pWord;
        this.tmApiKey = tmApiKey;
        this.sharedSecret = sharedSecret;
        this.works = true;
        this.updateTime = System.currentTimeMillis();
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

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public void setProxyModel(ProxyModel proxyModel) {
        this.proxyModel = proxyModel;
    }
}
