package faang.school.urlshortenerservice.dto;

import lombok.*;
import org.hibernate.validator.constraints.URL;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Request {

    @URL(message = "Invalid URL")
    private String url;
}