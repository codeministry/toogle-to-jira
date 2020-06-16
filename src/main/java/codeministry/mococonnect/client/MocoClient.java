package codeministry.mococonnect.client;

import codeministry.mococonnect.dto.MocoTimeEntryDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "moco-client", url = "${application.settings.moco.url}")
public interface MocoClient {

    @PostMapping(value = "/api/v1/activities", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> createTimeEntry(@RequestBody MocoTimeEntryDTO mocoTimeEntryDTO);
}
