package io.pivotal.cfapp.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "spring.h2.console", name = "enabled", havingValue = "true", matchIfMissing = false)
public class H2ConsoleConfig {

    private org.h2.tools.Server webServer;

    private org.h2.tools.Server server;

    @EventListener(ContextRefreshedEvent.class)
    public void start() throws java.sql.SQLException {
        this.webServer = org.h2.tools.Server.createWebServer("-webPort", "8082", "-tcpAllowOthers").start();
        this.server = org.h2.tools.Server.createTcpServer("-tcpPort", "9092", "-tcpAllowOthers").start();
    }

    @EventListener(ContextClosedEvent.class)
    public void stop() {
        this.webServer.stop();
        this.server.stop();
    }

}