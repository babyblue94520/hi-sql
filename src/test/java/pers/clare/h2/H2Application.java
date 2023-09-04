package pers.clare.h2;

import org.h2.tools.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.sql.SQLException;

@SpringBootApplication
public class H2Application {
    public static void main(String[] args) {
        SpringApplication.run(H2Application.class);
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public Server h2DatabaseServer(
            @Value("${h2.port:3390}") String port
    ) throws SQLException {
        return Server.createTcpServer(
                "-tcp", "-tcpAllowOthers", "-tcpPort", port);
    }
}
