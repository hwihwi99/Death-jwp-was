package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

            BufferedReader bf = new BufferedReader(new InputStreamReader(in));

            String str = bf.readLine();

            if(str.contains("/index.html")) {
                DataOutputStream dos = new DataOutputStream(out);
                byte[] bytes = Files.readAllBytes(Paths.get("./webapp/index.html"));
                response200Header(dos, bytes.length);
                responseBody(dos, bytes);
            }

            if(str.contains("/user/form.html")) {
                DataOutputStream dos = new DataOutputStream(out);
                byte[] bytes = Files.readAllBytes(Paths.get("./webapp/user/form.html"));
                response200Header(dos, bytes.length);
                responseBody(dos, bytes);
            }

//            if(str.contains("/user/create")) {
//                String inputData = str.split('? | &')[1];
//                System.out.println(inputData);
//            }

//            DataOutputStream dos = new DataOutputStream(out);
//            byte[] body = "Hello World".getBytes();
//            response200Header(dos, body.length);
//            responseBody(dos, body);
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

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
