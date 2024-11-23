package org.primaryServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import org.primaryServer.controller.RequestHandler;
import org.primaryServer.domain.Note;
import org.primaryServer.repository.NoteRepository;
import org.primaryServer.util.Logger;

public class PrimaryStorageServer {
    private final RequestHandler requestHandler;

    private static final List<String> LOCAL_STORAGE_SERVERS = Arrays.asList(
            "http://10.20.0.119:5000", // udp
            "http://10.20.0.121:5000", // api
            "http://10.20.0.161:5000" // tcp
    );

    public PrimaryStorageServer() {
        NoteRepository repository = new NoteRepository();
        this.requestHandler = new RequestHandler(repository);
    }

    public void start(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("PrimaryStorageServer가 포트 " + port + "에서 실행 중입니다...");

            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                     PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                    String request = in.readLine();

                    JSONObject jsonRequest = new JSONObject(request);
                    String method = jsonRequest.getString("method");
                    String path = jsonRequest.getString("path");

                    String response = "";

                    if (path.equals("/init") && method.equalsIgnoreCase("GET")) { // 데이터 초기화 -> 로컬 스토리지 연결 시 초기화
                        response = handleInitRequest();
                    }

                    if (!path.equals("/init") || !method.equalsIgnoreCase("GET")) { // 데이터 저장
                        response = requestHandler.handleRequest(method, path, jsonRequest);
                    }

                    if (isDataChangingRequest(method)) { // 데이터 변경 있는 경우
                        synchronizeToLocalStorageServers(jsonRequest);
                    }

                    sendSuccessResponse(out, response);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String handleInitRequest() {
        try {
            // Note 데이터를 가져옴
            String notesJsonString = requestHandler.handleGetAllNotes();

            // Note 데이터가 null이거나 비어 있는 경우 빈 notes JSON 생성
            if (notesJsonString == null || notesJsonString.isEmpty() || notesJsonString.equals("[]")) {
                System.out.println("Notes 데이터가 비어 있습니다.");
                return new JSONObject().put("notes", new JSONArray()).toString(); // 빈 JSONArray 반환
            }

            // Notes JSON 문자열을 JSONArray로 변환
            JSONArray notesArray = new JSONArray(notesJsonString);

            // 최종 응답 JSON 생성
            JSONObject response = new JSONObject();
            response.put("notes", notesArray);

            System.out.println("Init Response: " + response.toString()); // 디버깅용 출력
            return response.toString();

        } catch (Exception e) {
            e.printStackTrace();
            // 예외 발생 시 빈 JSON 반환
            return new JSONObject().put("notes", new JSONArray()).toString();
        }
    }

    private void synchronizeToLocalStorageServers(JSONObject jsonRequest) {
        String originServer = jsonRequest.optString("origin", "");

        for (String serverUrl : LOCAL_STORAGE_SERVERS) {
            String[] serverInfo = serverUrl.replace("http://", "").split(":");
            String host = serverInfo[0];
            int port = Integer.parseInt(serverInfo[1]);

            // 요청을 보낸 서버와 동일한 서버는 동기화 대상에서 제외
            if (serverUrl.equals(originServer)) {
                continue;
            }

            try {
                sendToLocalStorageServer(serverUrl, jsonRequest);
            } catch (IOException e) {
                System.err.println("LocalStorageServer와의 통신 실패: " + serverUrl);
                e.printStackTrace();
            }
        }
    }


    private void sendToLocalStorageServer(String serverUrl, JSONObject jsonRequest) throws IOException {
        jsonRequest.put("origin", "primary");
        String[] serverInfo = serverUrl.replace("http://", "").split(":");
        String host = serverInfo[0];
        int port = Integer.parseInt(serverInfo[1]);

        try (Socket localSocket = new Socket(host, port);
             PrintWriter out = new PrintWriter(localSocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(localSocket.getInputStream()))) {

            Logger.log(host + ":" + port, jsonRequest.toString(), "Forward Request to LocalStorage");
            out.println(jsonRequest.toString());

            String primaryResponse = in.readLine();
            Logger.log(host + ":" + port, primaryResponse, "Acknowledge write completed");

        } catch (IOException e) {
            System.err.println("LocalStorageServer(" + host + ":" + port + ")와의 연결 실패");
            throw e;
        }
    }

    private boolean isDataChangingRequest(String method) { // 데이터 변경 요청인지 확인
        return method.equalsIgnoreCase("POST") ||
                method.equalsIgnoreCase("PUT") ||
                method.equalsIgnoreCase("PATCH") ||
                method.equalsIgnoreCase("DELETE");
    }

    private void sendSuccessResponse(PrintWriter out, String response) {
        out.println(response);
    }
}
