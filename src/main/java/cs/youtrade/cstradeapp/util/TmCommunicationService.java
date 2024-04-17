package cs.youtrade.cstradeapp.util;

import com.google.gson.Gson;
import cs.youtrade.cstradeapp.storage.UserData;
import cs.youtrade.cstradeapp.storage.steamauth.SteamSessionService;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;

public class TmCommunicationService {
    private final static Logger log = LoggerFactory.getLogger(TmCommunicationService.class);

    public static void pingTM(UserData userData, boolean firstTime) throws ExecutionException {
        try (CloseableHttpClient client =
                     (userData.getProxyModel() == null) ?
                             setConnection() :
                             setConnection(userData.getProxyModel())
        ) {
            String tmPingURL = "https://market.csgo.com/api/v2/ping-new";
            URI tmPingURI = new URIBuilder(tmPingURL)
                    .addParameter("key", userData.getTmApiKey())
                    .build();
            HttpPost httpPost = new HttpPost(tmPingURI);
            Gson gson = new Gson();
            httpPost.setHeader("steamLoginSecure", "COOKIE_PAYLOAD");
            httpPost.setEntity(new StringEntity(gson.toJson(new PingBodyTemplate(userData.getAccessToken()))));
            try (CloseableHttpResponse httpResponse = client.execute(httpPost)) {
                HttpEntity entity = httpResponse.getEntity();
                if (entity != null) {
                    try (InputStream content = entity.getContent()) {
                        String responseBody = new String(content.readAllBytes(), StandardCharsets.UTF_8);
                        PingAnswerTemplate pingAnswer = gson.fromJson(responseBody, PingAnswerTemplate.class);

                        if (!pingAnswer.success && !pingAnswer.message().equals("too early for pong")) {
                            if (!firstTime) {
                                log.info("BAD API KEY [" + userData.getuName() + "]. Generating new access key");
                                SteamSessionService steamSessionService = new SteamSessionService(userData);
                                steamSessionService.run();
                            } else {
                                throw new TmPingException("Couldn't get access_token on log in");
                            }
                        } else {
                            log.info("\nPINGED [" + userData.getuName() + "]:" +
                                    "\n     online: " + pingAnswer.online +
                                    "\n     p2p: " + pingAnswer.p2p +
                                    "\n     steamApiKey: " + pingAnswer.steamApiKey +
                                    "\n     message: " + pingAnswer.message()
                            );
                        }
                    }
                } else
                    throw new TmPingException("Got empty answer from server");
            }
        } catch (IOException | URISyntaxException e) {
            throw new TmPingException(e);
        }
    }

    private static CloseableHttpClient setConnection(ProxyModel proxyModel) {
        CloseableHttpClient asyncClient;
        HttpHost proxy = new HttpHost(proxyModel.getHostname(), proxyModel.getPort());
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(11000)
                .setConnectTimeout(5000)
                .build();
        if (proxyModel.getUserName().isEmpty()) {
            asyncClient = HttpClients.custom()
                    .setProxy(proxy)
                    .setDefaultRequestConfig(requestConfig)
                    .setMaxConnPerRoute(32)
                    .build();
        } else {
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(
                    new AuthScope(proxy),
                    new UsernamePasswordCredentials(proxyModel.getUserName(), proxyModel.getPassword()));

            asyncClient = HttpClients.custom()
                    .setProxy(proxy)
                    .setDefaultCredentialsProvider(credentialsProvider)
                    .setDefaultRequestConfig(requestConfig)
                    .setMaxConnPerRoute(32)
                    .build();
        }

        return asyncClient;
    }

    private static CloseableHttpClient setConnection() {
        return HttpClients.custom()
                .setDefaultRequestConfig(
                        RequestConfig.custom()
                                .setSocketTimeout(11000)
                                .setConnectTimeout(5000)
                                .build()
                )
                .setMaxConnPerRoute(32)
                .build();
    }

    private record PingBodyTemplate(String access_token) {
    }

    private record PingAnswerTemplate(Boolean success, String ping, boolean online, boolean p2p, boolean steamApiKey,
                                      String message) {
    }
}
