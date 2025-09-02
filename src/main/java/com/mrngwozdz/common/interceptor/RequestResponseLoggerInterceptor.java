package com.mrngwozdz.common.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mrngwozdz.common.annotation.LogRequestResponse;
import com.mrngwozdz.service.appevent.AppEventService;
import com.mrngwozdz.api.model.request.EventRequest;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.UUID;

@LogRequestResponse
@Interceptor
@Slf4j
public class RequestResponseLoggerInterceptor {

    @Inject
    AppEventService appEventService;

    @Inject
    ObjectMapper objectMapper;

    @AroundInvoke
    public Object logRequestResponse(InvocationContext context) throws Exception {
        UUID serial = UUID.randomUUID();
        String requestId = serial.toString();
        String methodName = context.getMethod().getName();
        String className = context.getTarget().getClass().getSimpleName();
        
        long startTime = System.currentTimeMillis();
        
        // Log request
        String requestData = serializeParameters(context.getParameters());
        log.info("[{}] Request to {}.{} - Parameters: {}", requestId, className, methodName, requestData);
        
        // Skip logging for SSE stream endpoints to avoid transaction issues
        if (methodName.equals("streamEvents")) {
            return context.proceed();
        }
        
        // Log as event with serial UUID
        appEventService.logEvent(serial, new EventRequest(
                "API_REQUEST", 
                String.format("[%s] API call to %s.%s", requestId, className, methodName),
                String.format("{\"requestId\":\"%s\",\"parameters\":%s}", requestId, requestData)
        ));
        
        Object result = null;
        Exception exception = null;
        
        try {
            result = context.proceed();
            return result;
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            if (exception != null) {
                // Log error
                log.error("[{}] Error in {}.{} after {}ms - Error: {}", 
                        requestId, className, methodName, duration, exception.getMessage());
                
                appEventService.logEvent(serial, new EventRequest(
                        "API_ERROR",
                        String.format("[%s] API error in %s.%s", requestId, className, methodName),
                        String.format("{\"requestId\":\"%s\",\"error\":\"%s\",\"duration\":%d}", requestId, exception.getMessage(), duration)
                ));
            } else {
                // Log successful response
                String responseData = serializeResponse(result);
                log.info("[{}] Response from {}.{} after {}ms - Response: {}", 
                        requestId, className, methodName, duration, responseData);
                
                appEventService.logEvent(serial, new EventRequest(
                        "API_RESPONSE",
                        String.format("[%s] API response from %s.%s", requestId, className, methodName),
                        String.format("{\"requestId\":\"%s\",\"duration\":%d,\"response\":%s}", requestId, duration, responseData)
                ));
            }
        }
    }

    private String serializeParameters(Object[] parameters) {
        try {
            if (parameters == null || parameters.length == 0) {
                return "[]";
            }
            return objectMapper.writeValueAsString(Arrays.asList(parameters));
        } catch (Exception e) {
            return "[Error serializing parameters: " + e.getMessage() + "]";
        }
    }

    private String serializeResponse(Object response) {
        try {
            if (response == null) {
                return "null";
            }
            // For JAX-RS Response objects, we'll just log the status
            if (response instanceof jakarta.ws.rs.core.Response) {
                jakarta.ws.rs.core.Response jaxRsResponse = (jakarta.ws.rs.core.Response) response;
                return String.format("{\"status\":%d}", jaxRsResponse.getStatus());
            }
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            return "[Error serializing response: " + e.getMessage() + "]";
        }
    }
}