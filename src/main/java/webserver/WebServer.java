package webserver;

import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WebServer 클래스
 * 웹 서버를 시작하고,
 * 사용자의 요청이 있을 때까지 대기상태
 * 요청이 들어오면 사용자의 요청을 RequestHandler 클래스에 위임
 * */
public class WebServer {
    private static final Logger log = LoggerFactory.getLogger(WebServer.class);
    private static final int DEFAULT_PORT = 8080;

    public static void main(String args[]) throws Exception {
        int port = 0;
        if (args == null || args.length == 0) {
            port = DEFAULT_PORT;
        } else {
            port = Integer.parseInt(args[0]);
        }

        // 서버소켓을 생성한다. 웹서버는 기본적으로 8080번 포트를 사용한다.

        /**
         * ServerSocket : 사용자의 요청이 발생할 때까지 대기상태에 있도록 지원
         * */
        try (ServerSocket listenSocket = new ServerSocket(port)) {
            log.info("Web Application Server started {} port.", port);

            // 클라이언트가 연결될때까지 대기한다.
            Socket connection;
            while ((connection = listenSocket.accept()) != null) {
                RequestHandler requestHandler = new RequestHandler(connection); // 요청이 발생하면 RequestHandelr에 전달해서 새로운 쓰레드를 실행한다. (멀티쓰레드 방식 지원)
                requestHandler.start();
            }
        }
    }
}
