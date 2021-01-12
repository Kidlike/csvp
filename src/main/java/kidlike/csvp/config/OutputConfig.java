package kidlike.csvp.config;

import java.util.List;
import java.util.Optional;

import io.quarkus.arc.config.ConfigProperties;
import lombok.Getter;
import lombok.Setter;

@ConfigProperties(prefix = "output")
@Setter
@Getter
public class OutputConfig {
    String delimiter;
    Optional<List<String>> headers;
    Optional<String> wrapCellsWith;
    String rowTransform;
}
