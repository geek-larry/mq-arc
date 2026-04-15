package com.license.server.rrpc;

import com.license.common.message.LicenseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * RRPC响应等待管理器
 * 管理请求-响应模式中的等待队列
 */
@Slf4j
@Component
public class ResponseWaitManager {

    private final Map<String, CompletableFuture<LicenseResponse>> pendingRequests = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
    
    private static final long DEFAULT_TIMEOUT = 30000;

    /**
     * 注册等待响应的请求
     * 
     * @param messageId 消息ID
     * @return CompletableFuture用于等待响应
     */
    public CompletableFuture<LicenseResponse> registerRequest(String messageId) {
        return registerRequest(messageId, DEFAULT_TIMEOUT);
    }

    /**
     * 注册等待响应的请求（带超时时间）
     * 
     * @param messageId 消息ID
     * @param timeoutMs 超时时间（毫秒）
     * @return CompletableFuture用于等待响应
     */
    public CompletableFuture<LicenseResponse> registerRequest(String messageId, long timeoutMs) {
        CompletableFuture<LicenseResponse> future = new CompletableFuture<>();
        pendingRequests.put(messageId, future);
        
        scheduler.schedule(() -> {
            CompletableFuture<LicenseResponse> timeoutFuture = pendingRequests.remove(messageId);
            if (timeoutFuture != null && !timeoutFuture.isDone()) {
                LicenseResponse timeoutResponse = LicenseResponse.builder()
                        .requestId(messageId)
                        .status("TIMEOUT")
                        .message("Request timeout after " + timeoutMs + "ms")
                        .timestamp(System.currentTimeMillis())
                        .build();
                timeoutFuture.complete(timeoutResponse);
                log.warn("Request timeout, messageId: {}", messageId);
            }
        }, timeoutMs, TimeUnit.MILLISECONDS);
        
        log.debug("Registered request, messageId: {}, timeout: {}ms", messageId, timeoutMs);
        return future;
    }

    /**
     * 完成等待的请求
     * 
     * @param response 响应消息
     * @return 是否成功完成
     */
    public boolean completeRequest(LicenseResponse response) {
        String requestId = response.getRequestId();
        CompletableFuture<LicenseResponse> future = pendingRequests.remove(requestId);
        
        if (future != null) {
            boolean completed = future.complete(response);
            if (completed) {
                log.debug("Completed request, messageId: {}, status: {}", requestId, response.getStatus());
            } else {
                log.warn("Request already completed or timeout, messageId: {}", requestId);
            }
            return completed;
        } else {
            log.warn("No pending request found for messageId: {}", requestId);
            return false;
        }
    }

    /**
     * 取消等待的请求
     * 
     * @param messageId 消息ID
     * @return 是否成功取消
     */
    public boolean cancelRequest(String messageId) {
        CompletableFuture<LicenseResponse> future = pendingRequests.remove(messageId);
        if (future != null) {
            boolean cancelled = future.cancel(false);
            log.debug("Cancelled request, messageId: {}, result: {}", messageId, cancelled);
            return cancelled;
        }
        return false;
    }

    /**
     * 获取等待中的请求数量
     */
    public int getPendingRequestCount() {
        return pendingRequests.size();
    }

    /**
     * 清理所有等待中的请求
     */
    public void clearAll() {
        pendingRequests.clear();
        log.info("Cleared all pending requests");
    }

    /**
     * 关闭调度器
     */
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("ResponseWaitManager shutdown completed");
    }
}
