package util;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import static junit.framework.TestCase.assertEquals;

public class HttpRequsetTest{
    private String testCDirectory = "./src/test/resources/";

    @Test
    public void requset_GET() throws Exception {
        InputStream in = new FileInputStream(new File(testCDirectory+"Http_GET.txt"));
        HttpRequset requset = new HttpRequset(in);

        assertEquals("GET", requset.getMethod());
        assertEquals("/user/create", requset.getPath());
        assertEquals("keep-alive", requset.getHeader("Connection"));
        assertEquals("javajigi", requset.getParameter("userId"));
    }

    @Test
    public void requset_POST() throws Exception {
        InputStream in = new FileInputStream(new File(testCDirectory+"Http_POST.txt"));
        HttpRequset requset = new HttpRequset(in);

        assertEquals("POST", requset.getMethod());
        assertEquals("/user/create", requset.getPath());
        assertEquals("keep-alive", requset.getHeader("Connection"));
        assertEquals("javajigi", requset.getParameter("userId"));
    }
}