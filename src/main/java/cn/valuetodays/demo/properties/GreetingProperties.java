package cn.valuetodays.demo.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("greeting")
@Data
public class GreetingProperties {
    private String title;
    private String version;
}