package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

import javax.xml.crypto.Data;

/**
 * 멀티쓰레드를 지원하기 위해 Thread를 extends하며
 * 사용자의 요청에 대한 처리와 응답을 해주는 핵심 클래스
 * */
public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {

            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.

            BufferedReader bf = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String str = bf.readLine();
            log.debug("requset str : {}", str);

            if(str == null) {
                return;
            }

            String[] inputArr = str.split(" ");
            int contentLength = 0;
            String logined = "";

            while (!str.isEmpty()) {
                str = bf.readLine();
                log.debug("header : {}", str);

                if(str.contains("Content-Length")) {
                    contentLength = Integer.parseInt(str.split(":")[1].trim());
                }
                if(str.contains("Set-Cookie")) {
                    logined = HttpRequestUtils.parseCookies(str.split(":")[1]).get("logined");
                }
            }


            String status = inputArr[0]; // 요청상태
            String requestUrl = inputArr[1]; // url 요청 형태
            String etc = inputArr[2]; // HTTP/1:1 프로토콜 정보

            // 요구사항 1
            if (status.equals("GET") && requestUrl.equals("/index.html")) {
                DataOutputStream dos = new DataOutputStream(out);
                byte[] bytes = Files.readAllBytes(Paths.get("./webapp" + requestUrl));
                response200Header(dos, bytes.length);
                responseBody(dos, bytes);
            }

            // 요구사항 2 SUB - 회원가입 페이지로 이동
            if(status.equals("GET") && requestUrl.equals("/user/form.html")) {
                DataOutputStream dos = new DataOutputStream(out);
                byte[] bytes = Files.readAllBytes(Paths.get("./webapp" + requestUrl));
                response200Header(dos, bytes.length);
                responseBody(dos, bytes);
            }

            // 요구사항 2
            if (status.equals("GET") && requestUrl.contains("/user/create")) {

                String data = requestUrl.split("\\?")[1];
                String[] input = data.split("&");

                String userId = HttpRequestUtils.parseQueryString(input[0]).get("userId");
                String password = HttpRequestUtils.parseQueryString(input[1]).get("password");
                String name = HttpRequestUtils.parseQueryString(input[2]).get("name");
                String email = HttpRequestUtils.parseQueryString(input[3]).get("email");

                User user = new User(userId, password, name, email);
                DataBase.addUser(user);
            }


            // 요구사항 3
            if (status.equals("POST") && requestUrl.equals("/user/create")) {
                String data = IOUtils.readData(bf, contentLength);
                System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                System.out.println(data);
                String[] input = data.split("&");
                String userId = HttpRequestUtils.parseQueryString(input[0]).get("userId");
                String password = HttpRequestUtils.parseQueryString(input[1]).get("password");
                String name = HttpRequestUtils.parseQueryString(input[2]).get("name");
                String email = HttpRequestUtils.parseQueryString(input[3]).get("email");
                System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

                User user = new User(userId, password, name, email);
                DataBase.addUser(user);

                // 요구사항 4
                DataOutputStream dos = new DataOutputStream(out);
                byte[] bytes = Files.readAllBytes(Paths.get("./webapp/index.html"));
                response302Header(dos, bytes.length);
                responseBody(dos, bytes);
            }


            if (status.equals("GET") && requestUrl.equals("/user/login.html")) {
                DataOutputStream dos = new DataOutputStream(out);
                byte[] bytes = Files.readAllBytes(Paths.get("./webapp/user/login.html"));
                response200Header(dos, bytes.length);
                responseBody(dos, bytes);
            }

            // 요구사항 5
            if (status.equals("POST") && requestUrl.equals("/user/login")) {
                String data = IOUtils.readData(bf, contentLength);
                String[] input = data.split("&");
                String userId = HttpRequestUtils.parseQueryString(input[0]).get("userId");
                String password = HttpRequestUtils.parseQueryString(input[1]).get("password");

                User userInfo = DataBase.findUserById(userId);
                String loginStatus = "false";

                if(userInfo !=null && userInfo.getPassword().equals(password)) {
                    loginStatus = "true";
                }

                // logined if분기문 잘 처리하기
                if(loginStatus.equals("true")) {
                    DataOutputStream dos = new DataOutputStream(out);
                    byte[] bytes = Files.readAllBytes(Paths.get("./webapp/index.html"));
                    response302HeaderFound(dos, bytes.length, true);
                    responseBody(dos, bytes);
                }else if(loginStatus.equals("false")) {
                    DataOutputStream dos = new DataOutputStream(out);
                    byte[] bytes = Files.readAllBytes(Paths.get("./webapp/user/login_failed.html"));
                    response302HeaderFound(dos, bytes.length, false);
                    responseBody(dos, bytes);
                }
            }

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 302 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302HeaderFound(DataOutputStream dos, int lengthOfBodyContent, boolean logined) {
        try {
            dos.writeBytes("HTTP/1.1 302 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("Set-Cookie: logined="+logined+"\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
