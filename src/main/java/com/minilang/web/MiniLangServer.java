package com.minilang.web;

import com.minilang.MiniLangTranslator;
import com.minilang.TranslationResult;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class MiniLangServer {

    private static final MiniLangTranslator translator = new MiniLangTranslator();

    public static void main(String[] args) throws Exception {

        int port = Integer.parseInt(
                System.getenv().getOrDefault("PORT", "10000")
        );

        HttpServer server = HttpServer.create(
                new InetSocketAddress("0.0.0.0", port),
                0
        );

        server.createContext("/", MiniLangServer::handleHome);
        server.createContext("/translate", MiniLangServer::handleTranslate);

        server.setExecutor(null);
        server.start();

        System.out.println("MiniLang Web Server running on port " + port);
    }

    private static void handleHome(HttpExchange exchange) throws IOException {

        String html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>MiniLang Translator</title>
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            max-width: 1100px;
                            margin: 40px auto;
                            padding: 20px;
                            background: #f5f5f5;
                        }

                        h1 {
                            text-align: center;
                        }

                        textarea {
                            width: 100%;
                            height: 250px;
                            padding: 15px;
                            font-family: monospace;
                            font-size: 15px;
                            box-sizing: border-box;
                        }

                        button {
                            margin-top: 15px;
                            padding: 12px 25px;
                            font-size: 16px;
                            cursor: pointer;
                        }

                        pre {
                            background: #222;
                            color: #fff;
                            padding: 20px;
                            overflow-x: auto;
                            white-space: pre-wrap;
                        }

                        .box {
                            background: white;
                            padding: 20px;
                            margin-top: 20px;
                            border-radius: 8px;
                        }
                    </style>
                </head>

                <body>

                    <h1>MiniLang Translator</h1>

                    <div class="box">

                        <h2>Enter MiniLang Code</h2>

                        <form method="POST" action="/translate">

                            <textarea name="source" placeholder="Enter MiniLang code here...">int a = 10;
                int b = 20;
                print(a + b);</textarea>

                            <br>

                            <button type="submit">
                                Translate
                            </button>

                        </form>

                    </div>

                </body>
                </html>
                """;

        sendResponse(exchange, 200, html);
    }

    private static void handleTranslate(HttpExchange exchange) throws IOException {

        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }

        String body = new String(
                exchange.getRequestBody().readAllBytes(),
                StandardCharsets.UTF_8
        );

        Map<String, String> form = parseForm(body);

        String source = form.getOrDefault("source", "");

        try {

            TranslationResult result = translator.translate(source);

            String html = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta charset="UTF-8">
                        <title>MiniLang Translation Result</title>

                        <style>
                            body {
                                font-family: Arial, sans-serif;
                                max-width: 1200px;
                                margin: 40px auto;
                                padding: 20px;
                                background: #f5f5f5;
                            }

                            textarea {
                                width: 100%;
                                height: 220px;
                                font-family: monospace;
                                padding: 15px;
                                box-sizing: border-box;
                            }

                            pre {
                                background: #222;
                                color: #fff;
                                padding: 20px;
                                overflow-x: auto;
                                white-space: pre-wrap;
                            }

                            .box {
                                background: white;
                                padding: 20px;
                                margin-top: 20px;
                                border-radius: 8px;
                            }

                            a {
                                display: inline-block;
                                margin-top: 20px;
                            }
                        </style>

                    </head>

                    <body>

                        <h1>MiniLang Translator</h1>

                        <div class="box">

                            <h2>Source Code</h2>

                            <textarea readonly>%s</textarea>

                        </div>

                        <div class="box">

                            <h2>Generated Python</h2>
                            <pre>%s</pre>

                        </div>

                        <div class="box">

                            <h2>Generated Java</h2>
                            <pre>%s</pre>

                        </div>

                        <div class="box">

                            <h2>Generated C</h2>
                            <pre>%s</pre>

                        </div>

                        <div class="box">

                            <h2>AST</h2>
                            <pre>%s</pre>

                        </div>

                        <div class="box">

                            <h2>Phase Timings</h2>
                            <pre>%s</pre>

                        </div>

                        <a href="/">← Translate another program</a>

                    </body>
                    </html>
                    """.formatted(
                            escapeHtml(source),
                            escapeHtml(result.getPythonCode()),
                            escapeHtml(result.getJavaCode()),
                            escapeHtml(result.getCCode()),
                            escapeHtml(result.getAstTreeString()),
                            escapeHtml(result.getPhaseTimingsMs().toString())
                    );

            sendResponse(exchange, 200, html);

        } catch (Exception e) {

            String error = """
                    <html>
                    <body>
                        <h1>Translation Error</h1>
                        <pre>%s</pre>
                        <a href="/">Go back</a>
                    </body>
                    </html>
                    """.formatted(escapeHtml(e.getMessage()));

            sendResponse(exchange, 400, error);
        }
    }

    private static Map<String, String> parseForm(String body) {

        Map<String, String> result = new HashMap<>();

        for (String pair : body.split("&")) {

            String[] parts = pair.split("=", 2);

            if (parts.length == 2) {

                String key = URLDecoder.decode(
                        parts[0],
                        StandardCharsets.UTF_8
                );

                String value = URLDecoder.decode(
                        parts[1],
                        StandardCharsets.UTF_8
                );

                result.put(key, value);
            }
        }

        return result;
    }

    private static String escapeHtml(String text) {

        if (text == null) {
            return "";
        }

        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private static void sendResponse(
            HttpExchange exchange,
            int status,
            String response
    ) throws IOException {

        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set(
                "Content-Type",
                "text/html; charset=UTF-8"
        );

        exchange.sendResponseHeaders(status, bytes.length);

        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }
}
