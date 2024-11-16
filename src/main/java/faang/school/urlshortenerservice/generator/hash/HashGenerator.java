package faang.school.urlshortenerservice.generator.hash;

import faang.school.urlshortenerservice.config.properties.hash.HashProperties;
import faang.school.urlshortenerservice.encoder.Base62Encoder;
import faang.school.urlshortenerservice.model.hash.Hash;
import faang.school.urlshortenerservice.repository.hash.HashRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class HashGenerator {

    private final HashProperties hashProperties;
    private final HashRepository hashRepository;
    private final Base62Encoder base62Encoder;

    @Async("hashGeneratorExecutor")
    public void generateBatch() {
        try {
            String threadName = Thread.currentThread().getName();
            int batchSize = hashProperties.getBatch().getGet();
            List<Long> uniqueNumbers = hashRepository.getUniqueNumbers(batchSize);
            log.debug("Thread {} , retrieved {} of unique numbers!", threadName, uniqueNumbers.size());
            List<Hash> hashes = base62Encoder.encode(uniqueNumbers);
            log.debug("Thread {} , encoded {} hashes! Starting to batch save!", threadName, hashes.size());
            hashRepository.saveAllCustom(hashes);
            log.debug("Thread {} , successfully saved hashes in DB!", threadName);
        } catch (DataAccessException dae) {
            log.error("Error while generating batch! :", dae);
            throw new RuntimeException("Error! " + dae.getMessage() + " ", dae);
        }
    }

    @Async("hashCacheGeneratorExecutor")
    public CompletableFuture<List<Hash>> generateBatchForCache(int capacity) {
        log.debug("Start generating batch for cache");
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<Long> uniqueNumbers = hashRepository.getUniqueNumbers(capacity);
                return base62Encoder.encode(uniqueNumbers);
            } catch (DataAccessException dae) {
                log.error("Error while generating batch! :", dae);
                throw new RuntimeException("Error! " + dae.getMessage() + " ", dae);
            }
        });
    }
}