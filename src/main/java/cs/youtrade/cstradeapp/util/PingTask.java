package cs.youtrade.cstradeapp.util;

import cs.youtrade.cstradeapp.MainWindowController;
import cs.youtrade.cstradeapp.storage.TmApiRepository;
import cs.youtrade.cstradeapp.storage.UserData;
import javafx.collections.ObservableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PingTask implements Runnable {
    private final static Logger log = LoggerFactory.getLogger(PingTask.class);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final MainWindowController mainWindowController;

    public PingTask(MainWindowController mainWindowController) {
        this.mainWindowController = mainWindowController;
    }

    @Override
    public void run() {
        ObservableMap<String, UserData> keys = TmApiRepository.getKeys();
        keys.forEach((key, userData) -> {
            try {
                TmCommunicationService.pingTM(userData);
            } catch (TmPingException | ExecutionException e) {
                log.error("Error occurred while pinging TM: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this, 0, 2, TimeUnit.MINUTES);
    }

    public void stop() {
        scheduler.shutdown();
    }
}
