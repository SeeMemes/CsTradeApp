package cs.youtrade.cstradeapp.storage.steamauth;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import cs.youtrade.cstradeapp.storage.UserData;
import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.steam.authentication.*;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages;
import in.dragonbra.javasteam.steam.handlers.steamuser.LogOnDetails;
import in.dragonbra.javasteam.steam.handlers.steamuser.SteamUser;
import in.dragonbra.javasteam.steam.handlers.steamuser.callback.LoggedOffCallback;
import in.dragonbra.javasteam.steam.handlers.steamuser.callback.LoggedOnCallback;
import in.dragonbra.javasteam.steam.steamclient.SteamClient;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackManager;
import in.dragonbra.javasteam.steam.steamclient.callbacks.ConnectedCallback;
import in.dragonbra.javasteam.steam.steamclient.callbacks.DisconnectedCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.Random;
import java.util.concurrent.CancellationException;

public class SteamSessionService implements Runnable{
    private final static Logger log = LoggerFactory.getLogger(SteamSessionService.class);
    private SteamClient steamClient;
    private SteamUnifiedMessages unifiedMessages;
    private CallbackManager manager;
    private SteamUser steamUser;
    private boolean isRunning;
    private final UserData userData;
    private final SteamGuardCodeService guardCodeService;

    public SteamSessionService(UserData userData) {
        this.userData = userData;
        this.guardCodeService = new SteamGuardCodeService(userData.getSharedSecret());
    }

    @Override
    public void run() {
        steamClient = new SteamClient();

        manager = new CallbackManager(steamClient);

        unifiedMessages = steamClient.getHandler(SteamUnifiedMessages.class);

        steamUser = steamClient.getHandler(SteamUser.class);

        manager.subscribe(ConnectedCallback.class, this::onConnected);
        manager.subscribe(DisconnectedCallback.class, this::onDisconnected);

        manager.subscribe(LoggedOnCallback.class, this::onLoggedOn);
        manager.subscribe(LoggedOffCallback.class, this::onLoggedOff);

        isRunning = true;

        log.info("Connecting to steam...");

        steamClient.connect();

        while (isRunning) {
            manager.runWaitCallbacks(1000L);
        }
    }

    private void onConnected(ConnectedCallback callback) {
        System.out.println("Connected to Steam! Logging in " + userData.getuName() + "...");

        AuthSessionDetails authDetails = new AuthSessionDetails();
        authDetails.username = userData.getuName();
        authDetails.password = userData.getpWord();
        authDetails.persistentSession = false;
        authDetails.authenticator = guardCodeService;

        try {
            // get the authentication handler, which used for authenticating with Steam
            SteamAuthentication auth = new SteamAuthentication(steamClient, unifiedMessages);

            CredentialsAuthSession authSession = auth.beginAuthSessionViaCredentials(authDetails);

            AuthPollResult pollResponse = authSession.pollingWaitForResultCompat();

            LogOnDetails details = new LogOnDetails();
            details.setUsername(pollResponse.getAccountName());
            details.setAccessToken(pollResponse.getRefreshToken());

            // Set LoginID to a non-zero value if you have another client connected using the same account,
            // the same private ip, and same public ip.
            details.setLoginID(new Random().nextInt(10000) + 1);

            steamUser.logOn(details);

            // This is not required, but it is possible to parse the JWT access token to see the scope and expiration date.
            // parseJsonWebToken(pollResponse.accessToken, "AccessToken");
            // parseJsonWebToken(pollResponse.refreshToken, "RefreshToken");
            userData.setAccessToken(pollResponse.getAccessToken());
            userData.setRefreshToken(pollResponse.getRefreshToken());
        } catch (Exception e) {
            if (e instanceof AuthenticationException) {
                System.out.println("An Authentication error has occurred.");
            }

            if (e instanceof CancellationException) {
                System.out.println("An Cancellation exception was raised. Usually means a timeout occurred");
            }
        }
    }

    private void onDisconnected(DisconnectedCallback callback) {
        System.out.println("Disconnected from Steam");

        isRunning = false;
    }

    private void onLoggedOn(LoggedOnCallback callback) {
        if (callback.getResult() != EResult.OK) {
            System.out.println("Unable to logon to Steam: " + callback.getResult() + " / " + callback.getExtendedResult());

            isRunning = false;
            return;
        }

        System.out.println("Successfully logged on!");

        // at this point, we'd be able to perform actions on Steam

        // for this sample we'll just log off
        steamUser.logOff();
    }

    private void onLoggedOff(LoggedOffCallback callback) {
        System.out.println("Logged off of Steam: " + callback.getResult());

        isRunning = false;
    }


    @SuppressWarnings("unused")
    private void parseJsonWebToken(String token, String name) {
        String[] tokenComponents = token.split("\\.");

        // Fix up base64url to normal base64
        String base64 = tokenComponents[1].replace('-', '+').replace('_', '/');

        if (base64.length() % 4 != 0) {
            base64 += new String(new char[4 - base64.length() % 4]).replace('\0', '=');
        }

        byte[] payloadBytes = Base64.getDecoder().decode(base64);

        // Payload can be parsed as JSON, and then fields such expiration date, scope, etc can be accessed
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonElement payload = JsonParser.parseString(new String(payloadBytes));
        String formatted = gson.toJson(payload);

        // For brevity, we will simply output formatted json to console
        System.out.println(name + ": " + formatted);
        System.out.println();
    }

}
