package codeministry.connect.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Content {
    private final Type type;

    @JsonInclude(Include.NON_NULL)
    private final String text;

    @JsonInclude(Include.NON_NULL)
    private List<Content> content;
}
