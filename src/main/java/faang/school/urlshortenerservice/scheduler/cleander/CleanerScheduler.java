package faang.school.urlshortenerservice.scheduler.cleander;

import faang.school.urlshortenerservice.service.url.UrlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CleanerScheduler {

    private final UrlService urlService;

    @Scheduled(cron = "${spring.hash.scheduler.cleaner.cron}")
    public void cleaningOldHashes() {
        urlService.cleaningOldHashes();
    }
}
