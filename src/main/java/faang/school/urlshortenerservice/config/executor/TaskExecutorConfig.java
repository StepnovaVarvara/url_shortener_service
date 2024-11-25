package faang.school.urlshortenerservice.config.executor;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@RequiredArgsConstructor
public class TaskExecutorConfig {

    private final TaskExecutorParams taskExecutorParams;

    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(taskExecutorParams.getCorePoolSize());
        executor.setMaxPoolSize(taskExecutorParams.getMaxPoolSize());
        executor.setQueueCapacity(taskExecutorParams.getQueueCapacity());
        executor.setThreadNamePrefix(taskExecutorParams.getThreadNamePrefix());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }
}