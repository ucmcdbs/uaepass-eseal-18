package com.ucmcswg.samples.eseal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.soap.security.wss4j2.Wss4jSecurityInterceptor;

@Configuration
public class EsealClientConfig {

    private EsealClientProperties esealClientProperties;

    @Autowired
    public void setProperties(EsealClientProperties esealClientProperties) {
        this.esealClientProperties = esealClientProperties;
    }

    @Bean
    public Jaxb2Marshaller getMarshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setPackagesToScan(
            "com.safelayer.tws",
            "oasis.names.tc.saml._1_0.assertion",
            "org.oasis_open.docs.dss._2004._06.oasis_dss_1_0_core_schema_wd_27"
        );
        return marshaller;
    }

    @Bean
    public Wss4jSecurityInterceptor securityInterceptor(EsealClientProperties properties) {
        Wss4jSecurityInterceptor security = new Wss4jSecurityInterceptor();
        security.setSecurementActions("UsernameToken");
        security.setSecurementUsername(properties.getClient().get("username"));
        security.setSecurementPassword(properties.getClient().get("password"));
        security.setSecurementPasswordType("PasswordText");
        return security;
    }

    @Bean
    public EsealClient esealClient() {
        EsealClient client = new EsealClient(esealClientProperties);
        client.setDefaultUri(esealClientProperties.getClient().get("uri"));
        client.setMarshaller(getMarshaller());
        client.setUnmarshaller(getMarshaller());
        client.setInterceptors(new ClientInterceptor[]{ securityInterceptor(esealClientProperties) });
        return client;
    }

}
