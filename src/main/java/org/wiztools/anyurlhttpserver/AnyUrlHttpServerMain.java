package org.wiztools.anyurlhttpserver;

import java.io.File;
import java.io.PrintStream;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.wiztools.commons.MultiValueMap;
import org.wiztools.commons.MultiValueMapLinkedHashSet;

/**
 *
 * @author subwiz
 */
public class AnyUrlHttpServerMain {
    
    private static void printHelp(PrintStream out) {
        out.println("Parameters: ");
        out.println("  -p    [Mandatory] port number.");
        out.println("  -f    File to serve. When not given, prints <p>Hello World!</p>");
        out.println("  -c    Response Content-Type. Default is text/html.");
        out.println("  -r    Response character encoding. Default is utf-8.");
        out.println("  -H    * Response header in the format: `header:value'.");
        out.println("  -s    Response status code. Default is 200.");
        out.println("  -t    Throttle milliseconds between each kb written. Used to simulate slow connection.");
        out.println("  -h    Print this help.");
        out.println("Parameters with * can be used more than once.");
    }
    
    public static void main(String[] args) throws Exception {
        OptionParser parser = new OptionParser("p:f:c:r:H:s:t:h");
        OptionSet options = null;
        try {
            options = parser.parse(args);
        }
        catch(OptionException ex) {
            System.err.println(ex.getMessage());
            System.exit(1);
        }
        
        if(options.has("h")) {
            printHelp(System.out);
            System.exit(0);
        }
        
        final int port;
        if(options.has("p")) {
            String portStr = options.valueOf("p").toString();
            try {
                port = Integer.parseInt(portStr);
            }
            catch(NumberFormatException ex) {
                throw new IllegalArgumentException(
                        "Invalid port number: " + portStr, ex);
            }
        }
        else {
            port = -1;
            System.err.println("Mandatory parameter port not given.");
            System.exit(1);
        }
        
        AnyUrlServlet servlet = new AnyUrlServlet();
        
        if(options.has("f")) {
            String fileStr = options.valueOf("f").toString();
            servlet.setFile(new File(fileStr));
        }
        
        if(options.has("c")) {
            servlet.setContentType(options.valueOf("c").toString());
        }
        
        if(options.has("r")) {
            servlet.setCharset(options.valueOf("r").toString());
        }
        
        if(options.has("H")) {
            MultiValueMap headers = new MultiValueMapLinkedHashSet();
            for(Object t: options.valuesOf("H")) {
                final String headerLine = t.toString().trim();
                final int sepIdx = headerLine.indexOf(':');
                if((sepIdx > 0) && (sepIdx < headerLine.length())) {
                    final String header = headerLine.substring(0, sepIdx).trim();
                    final String value = headerLine.substring(sepIdx + 1).trim();
                    // System.out.printf("\nHeader: Value => %s:%s\n", header, value);
                    headers.put(header, value);
                }
            }
            servlet.setHeaders(headers);
        }
        
        if(options.has("s")) {
            try {
                int statusCode = Integer.parseInt(options.valueOf("s").toString());
                servlet.setStatusCode(statusCode);
            }
            catch(NumberFormatException ex) {
                throw new IllegalArgumentException("Param -s must be a valid status code.");
            }
        }
        
        if(options.has("t")) {
            try {
                long throttleMillis = Long.parseLong(options.valueOf("t").toString());
                if(throttleMillis > 1) {
                    servlet.setThrottleMillis(throttleMillis);
                }
                else {
                    throw new NumberFormatException();
                }
            }
            catch(NumberFormatException ex) {
                throw new IllegalArgumentException("Param -t must be a valid number.");
            }
        }
        
        Server server = new Server(port);
        server.setStopAtShutdown(true);
        
        // Attach the servlet:
        ServletContextHandler ctx = new ServletContextHandler();
        ctx.setContextPath("/");
        server.setHandler(ctx);
        ctx.addServlet(new ServletHolder(servlet), "/*");
        
        server.start();
    }
}
