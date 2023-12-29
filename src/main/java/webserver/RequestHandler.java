package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
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

            String[] tokens = str.split(" ");
            int contentLength = 0;
            boolean logined = false;

            while (!str.isEmpty()) {
                str = bf.readLine();
                log.debug("header : {}", str);

                if(str.contains("Content-Length")) { // 본문 데이터에 대한 길이가 여기에 담겨져서 내려온다.
                    contentLength = getContentLength(str);
                }
                if(str.contains("Cookie")) {
                    logined = isLogin(str);
                }
            }


            String httpMethods = tokens[0]; // 요청상태
            String requestUrl = tokens[1]; // url 요청 형태
            String httpVersion = tokens[2]; // HTTP/1:1 프로토콜 정보

            // 요구사항 1
            if (httpMethods.equals("GET") && requestUrl.equals("/index.html")) {
                DataOutputStream dos = new DataOutputStream(out);
                byte[] bytes = Files.readAllBytes(Paths.get("./webapp" + requestUrl));
                response200Header(dos, bytes.length);
                responseBody(dos, bytes);
            }

            // 요구사항 2 SUB - 회원가입 페이지로 이동
            if(httpMethods.equals("GET") && requestUrl.equals("/user/form.html")) {
                DataOutputStream dos = new DataOutputStream(out);
                byte[] bytes = Files.readAllBytes(Paths.get("./webapp" + requestUrl));
                response200Header(dos, bytes.length);
                responseBody(dos, bytes);
            }

            // 요구사항 2
            if (httpMethods.equals("GET") && requestUrl.contains("/user/create")) {
                String data = requestUrl.split("\\?")[1];
                Map<String, String> params = HttpRequestUtils.parseQueryString(data);
                User user = new User(params.get("userId"), params.get("password"), params.get("name"), params.get("email"));
                DataBase.addUser(user); // DB저장
                log.debug("User : {}", user);
            }

            // 요구사항 3
            if (httpMethods.equals("POST") && requestUrl.equals("/user/create")) {
                String data = IOUtils.readData(bf, contentLength);
                Map<String, String> params =  HttpRequestUtils.parseQueryString(data);
                User user = new User(params.get("userId"), params.get("password"), params.get("name"), params.get("email"));
                DataBase.addUser(user);

                log.debug("User : {}", user);


                // 요구사항 4
                DataOutputStream dos = new DataOutputStream(out);
                response302Header(dos, "/index.html");
            }


            // 요구사항 5 로그인 화면 진입
            if (httpMethods.equals("GET") && requestUrl.equals("/user/login.html")) {
                DataOutputStream dos = new DataOutputStream(out);
                byte[] bytes = Files.readAllBytes(Paths.get("./webapp/user/login.html"));
                response200Header(dos, bytes.length);
                responseBody(dos, bytes);
            }


            // 요구사항 5
            if (httpMethods.equals("POST") && requestUrl.equals("/user/login")) {
                String data = IOUtils.readData(bf, contentLength);
                Map<String, String> params = HttpRequestUtils.parseQueryString(data);
                User userInfo = DataBase.findUserById(params.get("userId"));

                if (userInfo == null) {
                    reaaponseResource(out, "/user/login_failed.html");
                    return;
                }

                if(userInfo.getPassword().equals(params.get("password"))) {
                    DataOutputStream dos = new DataOutputStream(out);
                    response302HeaderLoginSuccessHeader(dos);
                }else {
                    reaaponseResource(out, "/user/login_failed.html");
                }
            }

            // 요구사항 6
            if(httpMethods.equals("GET") && requestUrl.equals("/user/list")) {
                if(!logined) {
                    reaaponseResource(out, "/user/login.html");
                    return;
                }

                Collection<User> userList = DataBase.findAll();
                StringBuilder sb = new StringBuilder();
                sb.append("<table border = '1'>");
                for(User user : userList) {
                    sb.append("<tr>");
                    sb.append("<td>" + user.getUserId() + "</td>");
                    sb.append("<td>" + user.getName() + "</td>");
                    sb.append("<td>" + user.getEmail() + "</td>");
                    sb.append("</tr>");
                }
                sb.append("</table>");

                byte[] body = sb.toString().getBytes();
                DataOutputStream dos = new DataOutputStream(out);
                response200Header(dos, body.length);
                responseBody(dos, body);
            }

            // 요구사항 7
            if(requestUrl.endsWith(".css")) {
                DataOutputStream dos = new DataOutputStream(out);
                byte[] body = Files.readAllBytes(new File("./webapp" + requestUrl).toPath());
                response200CssHeader(dos, body.length);
                responseBody(dos, body);
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

    private void response200CssHeader(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/css;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos, String url) {
        try {
            dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
            dos.writeBytes("Location: " + url + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void reaaponseResource(OutputStream out, String url) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
        response200Header(dos, body.length);
        responseBody(dos, body);
    }

    private void response302HeaderLoginSuccessHeader(DataOutputStream dos) {
        try {
            dos.writeBytes("HTTP/1.1 302 OK \r\n");
            dos.writeBytes("Set-Cookie: logined= true\r\n");
            dos.writeBytes("Location: /index.html \r\n");
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

    private int getContentLength(String line) {
        String[] headerTokens = line.split(":");
        return Integer.parseInt(headerTokens[1].trim());
    }

    private boolean isLogin(String line) {
        String[] headerTokens = line.split(":");
        Map<String, String> cookies = HttpRequestUtils.parseCookies(headerTokens[1].trim());
        String value = cookies.get("logined");
        if (value == null) {
            // 쿠키가 없으면
            return false;
        }

        return Boolean.parseBoolean(value);
    }
}
