package edu.escuelaing.arep.app.server;

import edu.escuelaing.arep.app.lambda.SagvService;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;

public class HttpServer {
    private static HashMap<String, SagvService> services = new HashMap<>();
    private static HttpServer _instance = new HttpServer();


    private HttpServer() {
    }

    public static HttpServer getInstance() {
        return _instance;
    }

    public void start(String[] args) throws IOException {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(35000);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 35000.");
            System.exit(1);
        }

        boolean running = true;
        while (running) {
            try (Socket clientSocket = serverSocket.accept();
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
                String inputLine;
                boolean fLine = true;
                String uriS = "";

                while ((inputLine = in.readLine()) != null) {
                    System.out.println("Received: " + inputLine);
                    if (fLine) {
                        fLine = false;
                        uriS = inputLine.split(" ")[1];
                    }
                    if (!in.ready()) {
                        break;
                    }
                }

                String response;
                if (uriS.startsWith("/get/")) {
                    String parameter = extractParameter(uriS);
                    response = getFileResponse("/file?name="+parameter);
                } else if (uriS.startsWith("/post?parametro=")) {
                    response = handlePostRequest(uriS, in);
                } else if (uriS.equals("/")) {
                    response = getIndexResponse();
                } else {
                    response = getFileResponse(uriS);
                }
                out.println(response);
            } catch (IOException e) {
                System.err.println("Error processing request: " + e.getMessage());
            }
        }

        serverSocket.close();
    }

    public static void get(String r, SagvService s) {
        services.put(r, s);
    }

    public static void post(String r, SagvService s) {
        services.put(r, s);
    }

    private String handlePostRequest(String uri, BufferedReader in) throws IOException {
        System.out.println(uri.substring(uri.lastIndexOf('/') + 16));
        String content = new String(uri.substring(uri.lastIndexOf('/') + 16));
        return "HTTP/1.1 200 OK\r\n"
                + "Content-Type: txt \r\n"
                + "\r\n"
                + content;
    
    }

    private String extractParameter(String uri) {
        return uri.substring("/get/".length());
    }


    /**
     * This method determines the type of file being requested while also checking for the existence of the specified file
     * @param uri The search parameter of the search engine
     * @retur Returns the header and the requested file in the response
     */
    public static String getFileResponse(String uri) {
        System.out.println(uri);
        String fileName = uri.substring(uri.lastIndexOf('/') + 11);
        String currentDirectory = System.getProperty("user.dir");
        Path filePath = Paths.get(currentDirectory, "src/resources/public/" + fileName);
    
        if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
            try {
                String contentType = Files.probeContentType(filePath);
                byte[] fileContent = Files.readAllBytes(filePath);

                if (contentType != null && contentType.startsWith("image")) {
                    return imgResponse(filePath, contentType);       
                } else {
                    return otherResponse(fileContent, contentType);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    
        return "HTTP/1.1 404 Not Found\r\n"
                + "Content-Type: text/html\r\n"
                + "\r\n"
                + "<!DOCTYPE html>\n"
                + "<html>\n"
                + "    <head>\n"
                + "        <title>404 Not Found</title>\n"
                + "    </head>\n"
                + "    <body>\n"
                + "        <h1>404 Not Found</h1>\n"
                + "        <p>The requested URL localhost:35000" + uri + " was not found on this server.</p>\n"
                + "    </body>\n"
                + "</html>";
    }

    /**
     * This method generates the web header for image files
     * @param path Location of the file
     * @param contentType Type of file
     * @retur Returns the header and the requested file in the response
     */
    public static String imgResponse(Path path, String contentType) throws IOException {
        byte[] bytes = Files.readAllBytes(path);
        String base64 = Base64.getEncoder().encodeToString(bytes);
        return "HTTP/1.1 200 OK\r\n"
                + "Content-Type: " + contentType + "\r\n"
                + "\r\n"
                + "<center><img src=\"data:image/" + contentType + ";base64," + base64 + "\"></center>";
    }

    /**
     * This method generates the web header for other files
     * @param fileContent File content in bytes
     * @param contentType Type of file
     * @retur Returns the header and the requested file in the response
     */
    public static String otherResponse(byte[] fileContent, String contentType) throws IOException {
        String content = new String(fileContent);
        return "HTTP/1.1 200 OK\r\n"
                + "Content-Type: " + contentType + "\r\n"
                + "\r\n"
                + content;
    }

    /**
     * This method encapsulates the web page enabling file search functionality
     * @return returns the search page for files  
     */
    public static String getIndexResponse() {
        return "HTTP/1.1 200 OK\r\n"
        + "Content-Type: text/html\r\n"
        + "\r\n"
        + "<!DOCTYPE html>\n" +
        "<html>\n" +
        "    <head>\n" +
        "        <title>files</title>\n" +
        "        <meta charset=\"UTF-8\">\n" +
        "        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
        "       <style>\n" +
        "           body{\n" +
        "               background-color: #f0f0ff;\n" +
        "               font-family: \"Ubuntu\",sans-serif;\n" +
        "           }\n" +
        "           h1 {\n" +
        "               text-align:center;\n" +
        "               margin-top: 50px; \n" +
        "           }\n" +
        "           label, input[type=\"text\"],input[type=\"button\"]{\n" +
        "               display: block;\n" +
        "               margin: 0 auto;\n" +
        "               text-align: center;\n" +
        "           }"+
        "       </style>" +
        "    </head>\n" +
        "    <body>\n" +
        "        <h1>files</h1>\n" +
        "        <form action=\"/file\">\n" +
        "            <label for=\"name\">Name:</label><br>\n" +
        "            <input type=\"text\" id=\"name\" name=\"name\" value=\"\"><br><br>\n" +
        "            <input type=\"button\" value=\"Submit\" onclick=\"loadGetMsg()\">\n" +
        "        </form> \n" +
        "        <div id=\"getrespmsg\"></div>\n" +
        "\n" +
        "        <script>\n" +
        "            function loadGetMsg() {\n" +
        "                let nameVar = document.getElementById(\"name\").value;\n" +
        "                const xhttp = new XMLHttpRequest();\n" +
        "                xhttp.onload = function() {\n" +
        "                    document.getElementById(\"getrespmsg\").innerHTML =\n" +
        "                    this.responseText;\n" +
        "                }\n" +
        "                xhttp.open(\"GET\", \"/file?name=\"+nameVar);\n" +
        "                xhttp.send();\n" +
        "            }\n" +
        "        </script>\n" +
        "\n" +
        "        </script>\n" +
        "    </body>\n" +
        "</html>";
    }


}