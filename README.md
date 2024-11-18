# distributed-system-primary-storage-java
분산처리 데이터베이스 동기화

## ✅ Overview
![image](https://github.com/user-attachments/assets/acc5471d-06c1-4e75-8c7a-cc6bc39632ec)

☑️ 로컬 스토리지 사용
  
각각의 서버들은 로컬 스토리지에서 데이터를 쓰고 읽는다.

☑️ 데이터 동기화
  
로컬 스토리지는 서버의 primary storage와 통신하며 데이터를 동기화한다.

☑️ 서버 설정

각 서버는 `APP_NAME` 과 `LOCAL_PORT`를 가진다.

## ✅ API Server
![image](https://github.com/user-attachments/assets/e7acc60e-829f-43c0-b200-30eeb2fa472e)
![image](https://github.com/user-attachments/assets/b6f17251-dc95-469e-9828-b3f4d48aafcc)
![image](https://github.com/user-attachments/assets/db8fabc3-3892-4551-8bd7-af77c3e6f91c)

☑️ [GET]

- 전체 조회
- 단건 조회

☑️ [PATCH]

일부 필드 수정

☑️ [POST]

- json 형태로 메모 전달
- id는 API Server에서 순차적으로 증가하도록 설정

☑️ [PUT]

덮어쓰기

☑️ [DELETE]

단건 삭제

📢 ERROR 메시지 반환

"msg" : "ERROR"


## ✅ TCP Server
![image](https://github.com/user-attachments/assets/11d89dbe-b7bb-45ce-9ece-6e976ffbbcb6)

TCP 서버

JSON의 형식을 총 두 번 정의해줘야함

- http 핸들링

  method + pattern 의 json 형태

- 전달값

  json 형태


## ✅ UDP Server
![image](https://github.com/user-attachments/assets/188800e8-b57b-4cec-b899-ddb19acc29a3)


## 💡 Distributed Storage 💡 
![image](https://github.com/user-attachments/assets/b28c1b7d-16c4-4276-95b2-e4539edbcc1a)
![image](https://github.com/user-attachments/assets/0758a0ac-4c91-4e9f-b3b1-2a7894df03e5)
백업 서버는 x

### 동작과정
0. 사전에 LocalStorageServer는 PrimaryStorageServer와 연결하고, Primary서버로부터 전체 데이터를 받는다.
1. client가 api서버에 post 요청을 날린다. (POST /notes)
(POST 요청시에는 POST /primary가 동시에 이루어지도록, 동시에 request하도록 설계한다.)
2. localStorage가 업데이트 되며, primaryStorage에게 request를 날린다 (POST /primary)
3. primaryStorage는 backupServer에 update된 내용을 전달한다 (POST /backup)
4. 동시에 primaryStorage는 연결되어있는 localStorage들에게 update된 내용을 전달한다(write). (localStorage Server의 Post 메서드 호출 POST /notes)
5. primaryStorage가 LocalStorage에 write하면 LocalStorageServer는 response message를 전달한다.
6. 이러한 모든 과정이 끝나면 client에게 다시 acknowledge message를 전달한다.


## 📨 Messages
![image](https://github.com/user-attachments/assets/0500d622-0a8c-4190-aff6-16494f213525)

로그 띄우기
 
