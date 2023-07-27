package com.xartifex.prometheus.mon;

import com.sun.net.httpserver.HttpServer;
import io.micrometer.core.instrument.Counter;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.apache.ignite.IgniteException;
import org.apache.ignite.IgniteLogger;
import org.apache.ignite.lifecycle.LifecycleBean;
import org.apache.ignite.lifecycle.LifecycleEventType;
import org.apache.ignite.resources.LoggerResource;
import org.apache.ignite.resources.SpringApplicationContextResource;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class HandlersBean implements LifecycleBean {
    @SpringApplicationContextResource
    ApplicationContext applicationContext;

    @LoggerResource
    IgniteLogger logger;

    @Override
    public void onLifecycleEvent(LifecycleEventType evt) throws IgniteException {
        HttpServer server = null;
        switch (evt) {
            case AFTER_NODE_START -> {
                try {
                    MeterRegistryProvider meterRegistryProvider = (MeterRegistryProvider) applicationContext.getBean("meterRegistryProvider");
                    PrometheusMeterRegistry prometheusMeterRegistry = meterRegistryProvider.getMeterRegistry();

                    Counter counter = Counter.builder("callCount").description("counts calls to /count")
                            .register(prometheusMeterRegistry);

                    server = HttpServer.create(new InetSocketAddress("localhost", 8081), 0);
                    server.createContext("/prometheus", httpExchange -> {
                        String response = prometheusMeterRegistry.scrape();
                        httpExchange.sendResponseHeaders(200, response.getBytes().length);
                        try (OutputStream os = httpExchange.getResponseBody()) {
                            os.write(response.getBytes());
                        }
                    });

                    server.createContext("/count", httpExchange -> {
                        counter.increment();
                        String response = "metrics changed, check /prometheus";
                        httpExchange.sendResponseHeaders(200, response.getBytes().length);
                        try (OutputStream os = httpExchange.getResponseBody()) {
                            os.write(response.getBytes());
                        }
                    });
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                logger.warning("Metrics exporter server started");
                new Thread(server::start).start();
            }
            case AFTER_NODE_STOP -> {
                if (server != null) {
                    server.stop(1);
                }
                logger.warning("Metrics exporter server stopped");
            }
        }
    }
}
