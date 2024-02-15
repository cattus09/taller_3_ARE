package edu.escuelaing.arep.app;

import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Test;

import edu.escuelaing.arep.app.server.HttpServer;

class HttpServerTest {

    @Test
    void testGetFileResponseForExistingImage() {
        String uri = "/file?name=cat.jpg";
        String response = HttpServer.getFileResponse(uri);
        System.out.println("\n"+response+"\n");
        assertTrue(response.contains("HTTP/1.1 200 OK"));
        assertTrue(response.contains("Content-Type: image/jpeg"));

    }

    @Test
    void testGetFileResponseForExistingHTML() {
        String uri = "/file?name=quieroDormir.html";
        String response = HttpServer.getFileResponse(uri);
        assertTrue(response.contains("HTTP/1.1 200 OK"));
        assertTrue(response.contains("Content-Type: text/html"));
        assertTrue(response.contains("<!DOCTYPE html>"));
    }

    @Test
    void testGetFileResponseForNonExistingFile() {
        String uri = "/file/nonexistent.txt";
        String response = HttpServer.getFileResponse(uri);
        assertTrue(response.contains("HTTP/1.1 404 Not Found"));
        assertTrue(response.contains("<h1>404 Not Found</h1>"));
    }

    @Test
    void testGetIndexResponse() {
        String response = HttpServer.getIndexResponse();
        assertTrue(response.contains("HTTP/1.1 200 OK"));
        assertTrue(response.contains("Content-Type: text/html"));
        assertTrue(response.contains("<form action=\"/file\">"));
        assertTrue(response.contains("<h1>files</h1>"));
    }

}
