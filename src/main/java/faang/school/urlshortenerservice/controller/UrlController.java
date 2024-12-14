package faang.school.urlshortenerservice.controller;

import faang.school.urlshortenerservice.dto.UrlDto;
import faang.school.urlshortenerservice.service.url.UrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/v1/urls")
@RequiredArgsConstructor
@Tag(name = "UrlController", description = "Url management APIs")
public class UrlController {

    private final UrlService urlService;

    @CrossOrigin
    @Operation(summary = "Redirect to long URL")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Long URL found. Redirecting to URL"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Long URL by hash not found")
    })
    @GetMapping("/{hash}")
    public ResponseEntity<Void> redirectToLongUrl(@PathVariable @NotNull
                                                  @Parameter(description = "Хеш сокращенного Url", required = true)
                                                  String hash) {
        String longUrl = urlService.getLongUrl(hash);

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(longUrl))
                .build();
    }

    @Operation(summary = "Generate a short URL")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Short URL successfully generated",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = UrlDto.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid URL")
    })
    @PostMapping
    public UrlDto createShortUrl(@io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UrlDto.class),
                    examples = @ExampleObject(value = "{ \"url\": \"https://www.baeldung.com/\" }")))
                                 @RequestBody @Valid UrlDto urlDto) {

        return urlService.createShortUrl(urlDto);
    }
}
