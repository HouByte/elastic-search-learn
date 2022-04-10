package cn.flowboot.es.config;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * <h1>自定义异步任务线程</h1>
 * @author Vincent Vic
 * @version 1.0
 * @since 2021/4/10
 */
@Slf4j
@EnableAsync
@Configuration
public class AsyncPoolConfig implements AsyncConfigurer {

    /**
     * The {@link Executor} instance to be used when processing async
     * method invocations.
     */
    @Bean
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //核心数量
        executor.setCorePoolSize(10);
        //线程池最多数量
        executor.setMaxPoolSize(20);
        //队列容量
        executor.setQueueCapacity(20);
        //最长生存时间
        executor.setKeepAliveSeconds(60);
        //线程前缀
        executor.setThreadNamePrefix("bugio-task-");

        //是否等待线程完成
        executor.setWaitForTasksToCompleteOnShutdown(true);
        //线程池中任务的等待时间
        executor.setAwaitTerminationSeconds(60);

        //拦截策略
        executor.setRejectedExecutionHandler(
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        //线程池初始化
        executor.initialize();


        return executor;
    }

    /**
     * The {@link AsyncUncaughtExceptionHandler} instance to be used
     * when an exception is thrown during an asynchronous method execution
     * with {@code void} return type.
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new AsyncExceptionHandler();
    }

    @SuppressWarnings("all")
    class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

        @Override
        public void handleUncaughtException(Throwable throwable,
                                            Method method,
                                            Object... objects) {
            throwable.printStackTrace();
            log.error("AsyncError: {}, Method: {}, Param: {}",
                    throwable.getMessage(), method.getName(),
                    JSON.toJSONString(objects));

            // TODO 发送邮件或短信, 做进一步的处理
        }
    }
}
