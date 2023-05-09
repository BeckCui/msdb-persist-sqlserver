package com.dhl.fin.api.common.config;

import com.dhl.fin.api.common.util.SpringContextUtil;
import com.dhl.fin.api.common.util.StringUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadPoolExecutor;

@Component
public class ThreadPoolConfig {


    @Primary
    @Bean(name = "FintpAsyncThreadPool")
    public ThreadPoolTaskExecutor taskExecutor() {


        String coreSizeStr = SpringContextUtil.getPropertiesValue("async.executor.pool.coreSize");
        String maxSizeStr = SpringContextUtil.getPropertiesValue("async.executor.pool.maxSize");
        String queueCapacityStr = SpringContextUtil.getPropertiesValue("async.executor.pool.queueCapacity");
        String keepAliveStr = SpringContextUtil.getPropertiesValue("async.executor.pool.keepAlive");
        String threadNamePrefix = SpringContextUtil.getPropertiesValue("async.executor.pool.threadNamePrefix");

        int coreSize = StringUtil.isEmpty(coreSizeStr) ? 10 : Integer.valueOf(coreSizeStr);

        int maxSize = StringUtil.isEmpty(maxSizeStr) ? 20 : Integer.valueOf(maxSizeStr);

        int queueCapacity = StringUtil.isEmpty(queueCapacityStr) ? 30 : Integer.valueOf(queueCapacityStr);

        int keepAlive = StringUtil.isEmpty(keepAliveStr) ? 60 : Integer.valueOf(keepAliveStr);


        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数
        executor.setCorePoolSize(coreSize);
        // 最大线程数
        executor.setMaxPoolSize(maxSize);
        // 任务队列大小
        executor.setQueueCapacity(queueCapacity);
        // 线程前缀名
        executor.setThreadNamePrefix(threadNamePrefix);
        // 线程的空闲时间
        executor.setKeepAliveSeconds(keepAlive);
        // 拒绝策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 线程初始化
        executor.initialize();

        return executor;

    }


}



