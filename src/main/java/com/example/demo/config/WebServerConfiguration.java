package com.example.demo.config;

import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.coyote.http11.Http11NioProtocol;
import org.springframework.boot.web.embedded.tomcat.ConfigurableTomcatWebServerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

//when there is no TomactEmbeddedServletContainerFactory in spring container, load this bean into container
@Component
public class WebServerConfiguration implements WebServerFactoryCustomizer<ConfigurableTomcatWebServerFactory> {

    //Self define tomcat connector



    @Override
    public void customize(ConfigurableTomcatWebServerFactory factory) {
        ((TomcatServletWebServerFactory)factory).addConnectorCustomizers(new TomcatConnectorCustomizer() {
            @Override
            public void customize(Connector connector) {
                Http11NioProtocol http11NioProtocol = (Http11NioProtocol) connector.getProtocolHandler();

                //30s disconnect if no request
                http11NioProtocol.setKeepAliveTimeout(30000);
                //max request number of clients
                http11NioProtocol.setMaxKeepAliveRequests(10000);
            }
        });
    }
}
