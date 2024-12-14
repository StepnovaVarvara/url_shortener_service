package faang.school.urlshortenerservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redis.testcontainers.RedisContainer;
import faang.school.urlshortenerservice.dto.UrlDto;
import faang.school.urlshortenerservice.service.url.UrlService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
public class UrlControllerMockMvcIT {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UrlService urlService;

    private static final String LONG_URL = "https://www.baeldung.com/";
    private static final String INVALID_URL = "www.baeldung.com";
    private static final String DOMAIN_URL = "https://domain.com/";
    private static final String HASH = "hash";

    @Container
    public static PostgreSQLContainer<?> POSTGRESQL_CONTAINER =
            new PostgreSQLContainer<>("postgres:14");

    @Container
    public static final RedisContainer REDIS_CONTAINER =
            new RedisContainer(DockerImageName.parse("redis/redis-stack:latest"));

    @DynamicPropertySource
    static void start(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRESQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);

        registry.add("spring.data.redis.port", () -> REDIS_CONTAINER.getMappedPort(6379));
        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Nested
    class NegativeTests {

        @Test
        @DisplayName("Exception when create short url with empty content")
        public void whenCreateShortUrlWithEmptyContentThenReturnException() throws Exception {
            mockMvc.perform(post("/v1/urls")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .content(objectMapper.writeValueAsString(new UrlDto()
                                    .setUrl(" ")))
                    )
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Exception when create short url with invalid url")
        public void whenCreateShortUrlInvalidUrlThenReturnException() throws Exception {
            mockMvc.perform(post("/v1/urls")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .content(objectMapper.writeValueAsString(new UrlDto()
                                    .setUrl(INVALID_URL)))
                    )
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Exception when try redirect to long url with does not existed url in DB")
        public void whenRedirectToLongUrlThenReturnException() throws Exception {
            mockMvc.perform(get("/v1/urls/" + HASH))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Nested
    class PositiveTests {

        @Test
        @DisplayName("Success when create short url")
        public void whenCreateShortUrlThenReturnUrlDto() throws Exception {
            when(urlService.createShortUrl(new UrlDto().setUrl(LONG_URL)))
                    .thenReturn(new UrlDto().setUrl(DOMAIN_URL + HASH));

            MvcResult mvcResult = mockMvc.perform(post("/v1/urls")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .content(objectMapper.writeValueAsString(new UrlDto()
                                    .setUrl(LONG_URL)))
                    )
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andReturn();

            String shortUrl = mvcResult.getResponse().getContentAsString();

            assertNotNull(shortUrl);
            assertTrue(shortUrl.contains(DOMAIN_URL));
        }

        @Test
        @DisplayName("Success when redirect to long url")
        public void whenRedirectToLongUrlThenSuccessfulRedirect() throws Exception {
            when(urlService.getLongUrl(HASH)).thenReturn(LONG_URL);

            mockMvc.perform(get("/v1/urls/" + HASH))
                    .andExpect(status().isFound())
                    .andExpect(header().string("Location", LONG_URL));
        }
    }
}
