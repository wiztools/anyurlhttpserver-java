package org.wiztools.anyurlhttpserver;

import java.io.File;
import java.io.PrintStream;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

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
    }
    
    public static void main(String[] args) throws Exception {
        OptionParser parser = new OptionParser("p:f:c:r:h");
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
        
        final File file;
        if(options.has("f")) {
            String fileStr = options.valueOf("f").toString();
            file = new File(fileStr);
        }
        else {
            file = null;
        }
        
        String contentType = null;
        if(options.has("c")) {
            contentType = options.valueOf("c").toString();
        }
        
        String charset = null;
        if(options.has("r")) {
            charset = options.valueOf("r").toString();
        }
        
        AnyUrlServlet servlet = new AnyUrlServlet();
        servlet.setFile(file);
        servlet.setContentType(contentType);
        servlet.setCharset(charset);
        
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
