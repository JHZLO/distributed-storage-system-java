package org.apiClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ApiClient {
    public static void main(String[] args) {
        String serverUrl = "http://10.20.0.121:8080";

        String request;
        // request = String.format("curl -X GET %s/notes", serverUrl);
        request = String.format("curl -X POST %s/notes -H \"Content-Type: application/json\" -d '{\"title\": \"API Note\", \"body\": \"This is a API note\"}'", serverUrl);
        // request = String.format("curl -X GET %s/notes/3", serverUrl);
        // request = String.format("curl -X PUT %s/notes/3 -H \"Content-Type: application/json\" -d '{\"title\": \"API put Note\"}'", serverUrl);
        // request = String.format("curl -X PATCH %s/notes/3 -H \"Content-Type: application/json\" -d '{\"title\": \"API patch Note\", \"body\": \"This is a API patch note\"}'", serverUrl);
        // request = String.format("curl -X DELETE %s/notes/3", serverUrl);

        try {
            System.out.println("Request: " + request);
            Process process = new ProcessBuilder(request.split(" ")).start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("Response: " + line);
                }
            }

            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
