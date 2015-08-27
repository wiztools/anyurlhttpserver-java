package org.wiztools.anyurlhttpserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.wiztools.commons.Charsets;
import org.wiztools.commons.MultiValueMap;
import org.wiztools.commons.StreamUtil;

/**
 *
 * @author subwiz
 */
public class AnyUrlServlet extends HttpServlet {
    private String contentType = "text/html";
    private String charset = "utf-8";
    private File file;
    private MultiValueMap<String, String> headers;

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    
    public void setCharset(String charset) {
        this.charset = charset;
    }

    public void setFile(File file) {
        this.file = file;
    }
    
    public void setHeaders(MultiValueMap headers) {
        this.headers = headers;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        process(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        process(req, resp);
    }
    
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        process(req, resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        process(req, resp);
    }

    
    private void process(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType(contentType);
        resp.setCharacterEncoding(charset);
        
        if(headers != null) {
            for(String key: headers.keySet()) {
                for(String value: headers.get(key)) {
                    resp.addHeader(key, value);
                }
            }
        }
        
        try(OutputStream os = resp.getOutputStream();) {
            if(file != null && file.exists() && file.canRead()) {
                try(InputStream is = new FileInputStream(file)) {
                    StreamUtil.copy(is, os);
                }
            }
            else {
                byte[] out = "<p>Hello World!</p>".getBytes(Charsets.UTF_8);
                os.write(out);
            }
        }
    }
    
}
