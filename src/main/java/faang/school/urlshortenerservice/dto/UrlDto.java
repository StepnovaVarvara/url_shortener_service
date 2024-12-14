package faang.school.urlshortenerservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.URL;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class UrlDto {

    @Schema(
            description = "URL address",
            name = "url",
            type = "string",
            example = "https://www.baeldung.com/")
    @URL(message = "Invalid URL")
    private String url;
}
