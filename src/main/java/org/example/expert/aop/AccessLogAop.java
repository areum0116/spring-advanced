package org.example.expert.aop;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Slf4j
@Aspect
public class AccessLogAop {

    @Pointcut("@annotation(org.example.expert.domain.common.annotation.AccessLog)")
    public void accessLog() {}

    @Before("accessLog()")
    public void leaveAccessLog() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            log.info("요청한 사용자의 ID : {}", request.getAttribute("userId"));
            log.info("API 요청 시각 : {}", LocalDateTime.now());
            log.info("API 요청 URL : {}", request.getRequestURL());
        } catch (IllegalStateException e) {
            System.out.println("No request found. Error : " + e);
        }
    }
}
