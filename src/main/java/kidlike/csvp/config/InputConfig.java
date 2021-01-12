package kidlike.csvp.config;

import io.quarkus.arc.config.ConfigProperties;
import lombok.Getter;
import lombok.Setter;

@ConfigProperties(prefix = "input")
@Setter
@Getter
public class InputConfig {
    String delimiter;

    Boolean hasHeader = true;
}
