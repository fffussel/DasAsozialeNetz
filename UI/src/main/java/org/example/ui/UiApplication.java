package org.example.ui;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "org.example.ui.client")
@Theme("default")
public class UiApplication implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(UiApplication.class, args);
    }

}
