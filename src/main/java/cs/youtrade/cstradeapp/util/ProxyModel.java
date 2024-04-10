package cs.youtrade.cstradeapp.util;

public class ProxyModel {
    private final String hostname;
    private final int port;
    private String userName = "";
    private String password = "";

    public ProxyModel(
            String hostname, int port
    ) {
        this.hostname = hostname;
        this.port = port;
    }

    public ProxyModel(
            String hostname, int port,
            String userName, String password
    ) {
        this.hostname = hostname;
        this.port = port;
        this.password = password;
        this.userName = userName;
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }
}
