package org.primaryServer;

public class Application {
    public static void main(String[] args) {
        PrimaryStorageServer server = new PrimaryStorageServer();
        server.start(5001);
    }
}
