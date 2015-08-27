package org.wiztools.anyurlhttpserver;

import java.io.File;
import java.io.PrintStream;
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
        out.println("\t-p\t: [Mandatory] port number.");
        out.println("\t-f\t: File to serve. When not given, prints <p>Hello World!</p>");
        out.println("\t-c\t: Response Content-Type. Default is text/html.");
        out.println("\t-r\t: Response character encoding. Default is utf-8.");
        out.println("\t-H\t: Response header in the format: `header:value'.");
        out.println("\t-h\t: Print this help.");
    }
    
    public static void main(String[] args) throws Exception {
        OptionParser parser = new OptionParser("p:f:c:r:H:h");
        OptionSet options = parser.parse(args);
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
                    final String header = headerLine.substring(0, sepIdx);
                    final String value = headerLine.substring(sepIdx + 1);
                    // System.out.printf("\nHeader: Value => %s:%s\n", header, value);
                    headers.put(header, value);
                }
            }
            servlet.setHeaders(headers);
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
