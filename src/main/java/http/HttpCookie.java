package http;


import util.HttpRequestUtils;

import java.util.Map;

/**
 * Cookie의 헤터값을 관리하는 클래스
 * */
public class HttpCookie {
    private Map<String, String> cookies;

    HttpCookie(String cookieVale) {
        cookies = HttpRequestUtils.parseQueryString(cookieVale);
    }

    public String getCookie(String name) {
        return cookies.get(name);
    }
}
