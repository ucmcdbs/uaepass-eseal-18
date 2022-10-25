package com.ucmcswg.samples.eseal;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "eseal")
public class EsealClientProperties {

    private Map<String, String> client;
    private Map<String, String> signature;

    public Map<String, String> getClient() {
        return this.client;
    }

    public void setClient(Map<String, String> client) {
        this.client = client;
    }

    public Map<String, String> getSignature() {
        return this.signature;
    }

    public void setSignature(Map<String, String> signature) {
        this.signature = signature;
    }

    @Override
    public String toString() {
        return "EsealClientProperties{" +
                ", client=" + client +
                ", signature=" + signature +
                '}';
    }
}
