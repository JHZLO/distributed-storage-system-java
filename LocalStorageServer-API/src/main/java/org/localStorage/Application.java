package org.localStorage;

public class Application {
    public static void main(String[] args) {
        LocalStorageServer server = new LocalStorageServer();
        server.start(3301);
    }
}
