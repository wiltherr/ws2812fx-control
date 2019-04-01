package de.wiltherr.ws2812fx.app;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/**
 * The entry point of the Spring Boot application.
 */
@SpringBootApplication(scanBasePackageClasses = {
        de.wiltherr.ws2812fx.ui.WS2812ControlUI.class,
        de.wiltherr.ws2812fx.app.service.WS2812FXService.class
})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

//https://www.javacodegeeks.com/2018/05/introduction-to-using-vaadin-in-spring-boot.html EINFÜHRUNG VIEWS
    //UND VAADIN mit Spring Boot.

}
