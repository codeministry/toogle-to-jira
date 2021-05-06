package codeministry.connect.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Comment {

    @Builder.Default
    private final Type type = Type.doc;

    @Builder.Default
    private final int version = 1;

    private List<Content> content;
}
