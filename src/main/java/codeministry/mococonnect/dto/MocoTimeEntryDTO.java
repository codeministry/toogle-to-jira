package codeministry.mococonnect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MocoTimeEntryDTO {
    @Builder.Default
    private String project_id = "945225865";
    @Builder.Default
    private String task_id = "5885654";

    private String date;
    private Double hours;
    private String description;
}
