package util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.RequestHandler;

import java.io.*;
import java.util.HashMap;

public class HttpRequset {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private InputStream in;
    private BufferedReader bf;

    private HashMap<String,String> headerInfo;



    public HttpRequset(InputStream in) throws IOException {
        this.in = in;
        this.bf = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        setHeaderInfoMap();
    }

    private void setHeaderInfoMap() throws IOException {
        this.headerInfo = new HashMap<>();
        String headerStr = this.bf.readLine();
        log.debug("requset str : {}", headerStr);

        if(headerStr == null) {
            return;
        }

        String[] headerArr = headerStr.split(" ");

        headerInfo.put("method", headerArr[0]);
        headerInfo.put("path", headerArr[1].split("\\?")[0]);

        if(headerInfo.get("method").equals("GET")) {
            // path parameter
            String tempParam = headerArr[1].split("\\?")[1];
            String[] arr = tempParam.split("&");
            for(String s : arr) {
                headerInfo.put(s.split("=")[0], s.split("=")[1]);
            }
        }
        headerInfo.put("version", headerArr[2]);
        log.debug("");
        while (bf.ready() && !(headerStr = bf.readLine()).equals("")) {
            log.debug("header : {}", headerStr);
            headerInfo.put(headerStr.split(": ")[0],headerStr.split(": ")[1]);
        }

        if(headerInfo.get("method").equals("POST")) {
            String tempParam = bf.readLine();
            String[] arr = tempParam.split("&");
            for(String s : arr) {
                headerInfo.put(s.split("=")[0], s.split("=")[1]);
            }
        }
    }


    public String getMethod() {
        return headerInfo.get("method");
    }

    public String getPath() {
        return headerInfo.get("path");
    }

    public String getHeader(String field) {
        return headerInfo.get(field);
    }

    public String getParameter(String key) {
        return headerInfo.get(key);
    }
}
