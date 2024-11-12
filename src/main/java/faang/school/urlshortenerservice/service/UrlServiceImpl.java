package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.config.CacheProperties;
import faang.school.urlshortenerservice.dto.UrlDto;
import faang.school.urlshortenerservice.entity.Url;
import faang.school.urlshortenerservice.mapper.UrlMapper;
import faang.school.urlshortenerservice.repository.UrlRepository;
import faang.school.urlshortenerservice.service.cache.CacheService;
import faang.school.urlshortenerservice.service.cache.HashCacheService;
import faang.school.urlshortenerservice.service.outbox.OutboxService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class UrlServiceImpl implements UrlService {

    private final UrlRepository urlRepository;
    private final OutboxService outboxService;
    private final CacheService<String> cacheService;
    private final HashCacheService hashCacheService;
    private final CacheProperties cacheProperties;
    private final UrlMapper urlMapper;

    @Override
    public String redirectByHash(String hash) {
        String counterKey = hash + "::counter";
        long count = cacheService.incrementAndGet(counterKey);

        if (count >= cacheProperties.getRequestThreshold()) {
            Duration ttl = Duration.ofMillis(cacheProperties.getTtlIncrementTimeMs());
            String url = cacheService.getValue(hash, String.class)
                    .map(cachedUrl -> {
                        cacheService.addExpire(hash, ttl);
                        return cachedUrl;
                    }).orElseGet(() -> {
                        String fetchedUrl = getUrlFromDatabaseBy(hash);
                        cacheService.put(hash, fetchedUrl, ttl);
                        return fetchedUrl;
                    });
            cacheService.delete(counterKey);
            return url;
        }

        return getUrlBy(hash);
    }

    @Override
    public String getUrlBy(String hash) {
        return cacheService.getValue(hash, String.class)
                .orElseGet(() -> getUrlFromDatabaseBy(hash));
    }

    @Override
    public String getUrlFromDatabaseBy(String hash) {
        return urlRepository.findById(hash)
                .map(Url::getUrl)
                .orElseThrow(() -> new EntityNotFoundException("Url with hash %s not found".formatted(hash)));
    }

    @Override
    @Transactional
    public String shortenUrl(UrlDto urlDto) {
        String freeHash = hashCacheService.getHash();
        Url url = urlMapper.toEntity(urlDto, freeHash);

        urlRepository.save(url);
        outboxService.saveOutbox(url);

        return url.getUrl();
    }
}