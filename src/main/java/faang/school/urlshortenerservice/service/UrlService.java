package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.dto.request.UrlRequest;
import faang.school.urlshortenerservice.dto.response.UrlResponse;
import faang.school.urlshortenerservice.model.Url;

import java.util.List;

public interface UrlService {

    List<String> deleteUnusedHashes();

    void updateUrls(List<String> hashes);

    UrlResponse shortenUrl(UrlRequest request);
}
