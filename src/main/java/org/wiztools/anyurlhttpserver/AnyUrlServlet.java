package org.wiztools.anyurlhttpserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.wiztools.commons.Charsets;
import org.wiztools.commons.MultiValueMap;

/**
 *
 * @author subwiz
 */
public class AnyUrlServlet extends HttpServlet {
    private String contentType = "text/html";
    private String charset = "utf-8";
    private File file;
    private MultiValueMap<String, String> headers;
    private int statusCode = HttpServletResponse.SC_OK;
    
    // throttle per kb written:
    private long throttleMillis = -1;

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

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setThrottleMillis(long throttleMillis) {
        this.throttleMillis = throttleMillis;
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
        
        resp.setStatus(statusCode);
        
        try(OutputStream os = resp.getOutputStream();) {
            if(file != null && file.exists() && file.canRead()) {
                try(InputStream is = new FileInputStream(file)) {
                    try(
                            final ReadableByteChannel inChannel = Channels.newChannel(is);
                            final WritableByteChannel outChannel = Channels.newChannel(os);
                            ) {
                        final ByteBuffer buffer = ByteBuffer.allocate(1000); // 1000 bytes = 1 Kb
                        while(true) {
                            // throttle?
                            if(throttleMillis > 0l) {
                                try {
                                    Thread.sleep(throttleMillis);
                                }
                                catch(InterruptedException ex) {
                                    throw new ServletException(ex);
                                }
                            }
                            
                            // read & write:
                            int bytesRead = inChannel.read(buffer);
                            if(bytesRead == -1) break;
                            buffer.flip();
                            while(buffer.hasRemaining()) outChannel.write(buffer);
                            buffer.clear();
                        }
                    }
                }
            }
            else {
                byte[] out = "<p>Hello World!</p>".getBytes(Charsets.UTF_8);
                os.write(out);
            }
        }
    }
    
}
