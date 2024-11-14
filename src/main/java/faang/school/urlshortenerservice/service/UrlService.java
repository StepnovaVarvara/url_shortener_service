package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.exception.IncorrectUrl;
import faang.school.urlshortenerservice.hash.HashCache;
import faang.school.urlshortenerservice.model.UrlEntity;
import faang.school.urlshortenerservice.properties.HashProperties;
import faang.school.urlshortenerservice.properties.UrlShortenerProperties;
import faang.school.urlshortenerservice.repository.HashRepository;
import faang.school.urlshortenerservice.repository.URLCacheRepository;
import faang.school.urlshortenerservice.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class UrlService {
    private final HashCache hashCache;
    private final UrlRepository urlRepository;
    private final UrlShortenerProperties urlShortenerProperties;
    private final URLCacheRepository urlCacheRepository;
    private final HashProperties hashProperties;
    private final HashRepository hashRepository;

    @Transactional
    public String shorten(String longUrl) {
        validateUrl(longUrl);

        String hash = hashCache.getHash();

        UrlEntity entity = new UrlEntity();
        entity.setUrlValue(longUrl);
        entity.setHashValue(hash);
        urlRepository.save(entity);

        urlCacheRepository.put(hash, longUrl);

        return createShortUrl(hash);
    }

    @Transactional(readOnly = true)
    public String getUrl(String hash) {
       String longUrl = urlCacheRepository.get(hash);
       if (Objects.nonNull(longUrl)) {
           return longUrl;
       }
       UrlEntity currentUrl = urlRepository.findById(hash).orElseThrow();
       return currentUrl.getUrlValue();
    }

    @Transactional
    public void cleanHashes() {
        long dayToKeep = hashProperties.getDaysToKeep();
        LocalDateTime before = LocalDateTime.now().minusDays(dayToKeep);
        List<String> cleanedHash = urlRepository.deleteByCreatedAtBefore(before).stream()
                .map(UrlEntity::getHashValue)
                .toList();
        hashRepository.save(cleanedHash);

    }

    private void validateUrl(String longUrl) {
        try {
            new URL(longUrl);
        } catch (MalformedURLException e) {
            throw new IncorrectUrl("Incorrect URL: " + longUrl);
        }
    }

    private String createShortUrl(String hash) {
        String protocol = urlShortenerProperties.getProtocol();
        String domain = urlShortenerProperties.getDomain();
        return protocol + "://" + domain + "/" + hash;
    }
}
