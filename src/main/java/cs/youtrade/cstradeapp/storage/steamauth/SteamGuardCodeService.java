package cs.youtrade.cstradeapp.storage.steamauth;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import in.dragonbra.javasteam.steam.authentication.IAuthenticator;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

public class SteamGuardCodeService implements IAuthenticator {
    private final static Logger log = LoggerFactory.getLogger(SteamGuardCodeService.class);
    private final String SYMBOLS = "23456789BCDFGHJKMNPQRTVWXY";
    private final String sharedSecret;

    public SteamGuardCodeService (String sharedSecret) {
        this.sharedSecret = sharedSecret;
    }

    @NotNull
    @Override
    public CompletableFuture<Boolean> acceptDeviceConfirmation() {
        return null;
    }

    @NotNull
    @Override
    public CompletableFuture<String> getDeviceCode(boolean b) {
        return CompletableFuture.supplyAsync(() -> {
            long time = getServerTime();
            int interval = (int) Math.floor(time / 30000);
            ByteBuffer buffer = ByteBuffer.allocate(8);
            buffer.putInt(0, (int) Math.floor(interval / Math.pow(2, 32)));
            buffer.putInt(4, (int) (interval % Math.pow(2, 32)));
            byte[] secretBuffer = Base64.getDecoder().decode(sharedSecret);
            try {
                Mac hmac = Mac.getInstance("HmacSHA1");
                hmac.init(new SecretKeySpec(secretBuffer, "HmacSHA1"));
                byte[] mac = hmac.doFinal(buffer.array());
                int start = mac[19] & 0x0f;
                int value = (mac[start] & 0x7f) << 24 | (mac[start + 1] & 0xff) << 16 | (mac[start + 2] & 0xff) << 8 | (mac[start + 3] & 0xff);
                StringBuilder code = new StringBuilder();
                for (int i = 0; i < 5; i++) {
                    code.append(SYMBOLS.charAt(value % SYMBOLS.length()));
                    value /= SYMBOLS.length();
                }
                return code.toString();
            } catch (NoSuchAlgorithmException | InvalidKeyException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @NotNull
    @Override
    public CompletableFuture<String> getEmailCode(@Nullable String s, boolean b) {
        return null;
    }

    private long getServerTime(){
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            String steamOffsetLink = "https://api.steampowered.com/ITwoFactorService/QueryTime/v0001";
            URI steamOffsetURI = new URIBuilder(steamOffsetLink)
                    .build();
            HttpPost httpPost = new HttpPost(steamOffsetURI);
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    HttpEntity entity = response.getEntity();
                    String jsonString = EntityUtils.toString(entity);
                    Gson gson = new Gson();
                    SteamOffsetResponse steamResponse = gson.fromJson(jsonString, SteamOffsetResponse.class);
                    return steamResponse.getResponse().getServerTime() * 1000;
                }
            }
        } catch (URISyntaxException e) {
            log.error("Bad URI: " + e.getMessage());
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return -1;
    }

    public static class SteamOffsetDetails {
        @SerializedName("server_time")
        private long serverTime;

        @SerializedName("skew_tolerance_seconds")
        private String skewToleranceSeconds;

        @SerializedName("large_time_jink")
        private String largeTimeJink;

        @SerializedName("probe_frequency_seconds")
        private int probeFrequencySeconds;

        @SerializedName("adjusted_time_probe_frequency_seconds")
        private int adjustedTimeProbeFrequencySeconds;

        @SerializedName("hint_probe_frequency_seconds")
        private int hintProbeFrequencySeconds;

        @SerializedName("sync_timeout")
        private int syncTimeout;

        @SerializedName("try_again_seconds")
        private int tryAgainSeconds;

        @SerializedName("max_attempts")
        private int maxAttempts;

        public long getServerTime() {
            return serverTime;
        }

        public void setServerTime(long serverTime) {
            this.serverTime = serverTime;
        }

        public String getSkewToleranceSeconds() {
            return skewToleranceSeconds;
        }

        public void setSkewToleranceSeconds(String skewToleranceSeconds) {
            this.skewToleranceSeconds = skewToleranceSeconds;
        }

        public String getLargeTimeJink() {
            return largeTimeJink;
        }

        public void setLargeTimeJink(String largeTimeJink) {
            this.largeTimeJink = largeTimeJink;
        }

        public int getProbeFrequencySeconds() {
            return probeFrequencySeconds;
        }

        public void setProbeFrequencySeconds(int probeFrequencySeconds) {
            this.probeFrequencySeconds = probeFrequencySeconds;
        }

        public int getAdjustedTimeProbeFrequencySeconds() {
            return adjustedTimeProbeFrequencySeconds;
        }

        public void setAdjustedTimeProbeFrequencySeconds(int adjustedTimeProbeFrequencySeconds) {
            this.adjustedTimeProbeFrequencySeconds = adjustedTimeProbeFrequencySeconds;
        }

        public int getHintProbeFrequencySeconds() {
            return hintProbeFrequencySeconds;
        }

        public void setHintProbeFrequencySeconds(int hintProbeFrequencySeconds) {
            this.hintProbeFrequencySeconds = hintProbeFrequencySeconds;
        }

        public int getSyncTimeout() {
            return syncTimeout;
        }

        public void setSyncTimeout(int syncTimeout) {
            this.syncTimeout = syncTimeout;
        }

        public int getTryAgainSeconds() {
            return tryAgainSeconds;
        }

        public void setTryAgainSeconds(int tryAgainSeconds) {
            this.tryAgainSeconds = tryAgainSeconds;
        }

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }
    }

    public static class SteamOffsetResponse {
        private SteamOffsetDetails response;

        public SteamOffsetDetails getResponse() {
            return response;
        }

        public void setResponse(SteamOffsetDetails response) {
            this.response = response;
        }
    }
}
