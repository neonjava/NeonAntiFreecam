package me.neonjava.antifreecam.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import me.neonjava.antifreecam.bootstrap.NeonAntiFreecamPlugin;
import me.neonjava.antifreecam.metrics.PerformanceMetrics;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

/**
 * Lightweight built-in REST API dashboard server.
 */
public final class WebDashboardApi {
    private HttpServer server;
    private final NeonAntiFreecamPlugin plugin;
    private final PerformanceMetrics metrics;

    public WebDashboardApi(NeonAntiFreecamPlugin plugin, PerformanceMetrics metrics) {
        this.plugin = plugin;
        this.metrics = metrics;
    }

    public void start(int port) {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/status", new StatusHandler());
            server.createContext("/performance", new PerformanceHandler());
            server.setExecutor(null);
            server.start();
            plugin.getLogger().log(Level.INFO, "[NeonAntiFreecam] REST Web API started on port " + port);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "[NeonAntiFreecam] Failed to start REST Web API", e);
        }
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    private static void sendJsonResponse(HttpExchange exchange, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private class StatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String json = "{\n" +
                    "  \"status\": \"online\",\n" +
                    "  \"plugin\": \"NeonAntiFreecam\",\n" +
                    "  \"version\": \"" + plugin.getDescription().getVersion() + "\"\n" +
                    "}";
            sendJsonResponse(exchange, json);
        }
    }

    private class PerformanceHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String json = "{\n" +
                    "  \"protected_players\": " + metrics.getProtectedPlayersCount() + ",\n" +
                    "  \"modified_packets\": " + metrics.getModifiedPacketsCount() + ",\n" +
                    "  \"hidden_entities\": " + metrics.getHiddenEntitiesCount() + ",\n" +
                    "  \"average_processing_time_ms\": " + metrics.getAverageProcessingTimeMs() + "\n" +
                    "}";
            sendJsonResponse(exchange, json);
        }
    }
}
