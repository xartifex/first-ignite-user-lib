package com.xartifex.prometheus.mon;


import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class MeterRegistryProvider {

    private final PrometheusMeterRegistry meterRegistry;

    public MeterRegistryProvider() {
        meterRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
    }

    public PrometheusMeterRegistry getMeterRegistry() {
        return meterRegistry;
    }
}
