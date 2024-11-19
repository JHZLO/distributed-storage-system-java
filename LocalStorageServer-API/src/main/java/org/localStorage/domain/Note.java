package org.localStorage.domain;

import java.io.Serializable;

public class Note implements Serializable { // Serializable 인터페이스 구현
    private static final long serialVersionUID = 1L; // 클래스 버전 관리를 위한 ID

    private int id;
    private String title;
    private String body;

    public Note(int id, String title, String body) {
        this.id = id;
        this.title = title;
        this.body = body;
    }

    // Getter 및 Setter
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "ID: " + id + ", Title: " + title + ", Body: " + body;
    }
}
