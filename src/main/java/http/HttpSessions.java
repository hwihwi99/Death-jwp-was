package http;

import java.util.HashMap;
import java.util.Map;

/**
 * 서버는 다수의 클라이언트 세션을 지원해야 한다.
 * 따라서 모든 클라이언트의 세션을 관리할 수 있는 저장소가 필요하다.
 * */
public class HttpSessions {
    private static Map<String, HttpSession> sessions = new HashMap<>();

    public static HttpSession getSession(String id) {
        HttpSession session = sessions.get(id);

        if(session == null) {
            session = new HttpSession(id);
            sessions.put(id, session);
            return session;
        }

        return session;
    }

    static void remove(String id) {
        sessions.remove(id);
    }
}
