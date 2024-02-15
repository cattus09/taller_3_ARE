package edu.escuelaing.arep.app.lambda;

import edu.escuelaing.arep.app.server.HttpServer;

import java.io.IOException;

public class SagvSpark {
    private static String headers = "HTTP/1.1 200 OK\r\n"
            + "Content-Type:text/html\r\n"
            + "\r\n";

    public static void main(String[] args) throws IOException {
        HttpServer.get("/get", p -> headers + p );
        HttpServer.post("/post", p -> headers +  p);

        HttpServer.getInstance().start(args);
    }
}