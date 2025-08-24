// src/main/java/com/likelion/cleopatra/global/aop/ServiceProfilerAspect.java
package com.likelion.cleopatra.global.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
@Aspect
@Configuration
@Profile("dev")
@ConditionalOnProperty(prefix = "app.aop.profiler", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class ServiceProfilerAspect {

    private final ObjectMapper objectMapper;

    @Value("${app.aop.profiler.print-args:false}")       private boolean printArgs;
    @Value("${app.aop.profiler.print-return:false}")     private boolean printReturn;
    @Value("${app.aop.profiler.pretty-print:true}")      private boolean prettyPrint;
    @Value("${app.aop.profiler.slow-threshold-ms:500}")  private long slowThresholdMs;
    @Value("${app.aop.profiler.return-max-chars:100000}") private int returnMaxChars; // 넉넉히

    @Around("execution(public * com.likelion.cleopatra..service..*(..))")
    public Object profile(ProceedingJoinPoint pjp) throws Throwable {
        long t0 = System.nanoTime();

        MethodSignature sig = (MethodSignature) pjp.getSignature();
        String cls = sig.getDeclaringType().getName();
        String method = sig.getName();
        String args = printArgs ? argSummary(sig, pjp.getArgs()) : "";

        log.info("[PROF][START] {}#{}{}", cls, method, args);

        try {
            Object ret = pjp.proceed();
            long ms = (System.nanoTime() - t0) / 1_000_000;

            if (ret instanceof Mono<?> mono) {
                return mono.doOnSuccess(v -> afterSuccess(cls, method, ms, v))
                        .doOnError(e -> afterError(cls, method, ms, e));
            }
            if (ret instanceof Flux<?> flux) {
                return flux.collectList()
                        .doOnSuccess(v -> afterSuccess(cls, method, ms, v))
                        .doOnError(e -> afterError(cls, method, ms, e))
                        .flatMapMany(Flux::fromIterable);
            }

            afterSuccess(cls, method, ms, ret);
            return ret;
        } catch (Throwable e) {
            long ms = (System.nanoTime() - t0) / 1_000_000;
            afterError(cls, method, ms, e);
            throw e;
        }
    }

    private void afterSuccess(String cls, String method, long ms, Object ret) {
        if (ms >= slowThresholdMs) log.warn("[PROF][END][SLOW] {}#{} {} ms", cls, method, ms);
        else                       log.info("[PROF][END] {}#{} {} ms", cls, method, ms);

        if (printReturn && isLoggableReturn(ret)) {
            String json = toJson(ret, prettyPrint);
            log.info("[PROF][RET] {}#{} -> \n{}", cls, method, shorten(json));
        }
    }

    private void afterError(String cls, String method, long ms, Throwable e) {
        log.warn("[PROF][ERR] {}#{} {} ms cause={} msg={}",
                cls, method, ms, e.getClass().getSimpleName(), safeMsg(e));
    }

    // ---------- helpers ----------
    private boolean isLoggableReturn(Object ret) {
        if (ret == null) return false;
        if (ret instanceof byte[] || ret instanceof char[]) return false;
        String name = ret.getClass().getSimpleName();
        String pkg  = ret.getClass().getPackageName();
        return name.endsWith("Res") || pkg.contains(".dto");
    }

    private String toJson(Object v, boolean pretty) {
        try {
            return pretty
                    ? objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(v)
                    : objectMapper.writeValueAsString(v);
        } catch (Exception e) {
            return String.valueOf(v);
        }
    }

    private String shorten(String s) {
        if (s == null) return "null";
        return s.length() > returnMaxChars ? s.substring(0, returnMaxChars) + "\n...(truncated)" : s;
    }

    private String argSummary(MethodSignature sig, Object[] args) {
        if (args == null || args.length == 0) return "";
        String[] names = sig.getParameterNames(); // -parameters 옵션 필요
        String joined = "";
        try {
            joined = Arrays.stream(args)
                    .map(this::argToJson)
                    .collect(Collectors.joining(",\n"));
            joined = "[\n" + joined + "\n]";
        } catch (Exception e) {
            // fallback to toString
            joined = Arrays.stream(args)
                    .map(this::shortenArg)
                    .collect(Collectors.joining(", "));
        }
        return " args=\n" + joined;
    }

    private String argToJson(Object o) {
        if (o == null) return "null";
        if (o instanceof String || o.getClass().isPrimitive()
                || Number.class.isAssignableFrom(o.getClass())
                || Boolean.class.isAssignableFrom(o.getClass())) {
            return String.valueOf(o);
        }
        return toJson(o, true); // args는 항상 pretty
    }

    private String shortenArg(Object o) {
        if (o == null) return "null";
        String s = o.toString();
        return s.length() > 200 ? s.substring(0, 200) + "..." : s;
    }

    private String safeMsg(Throwable t) {
        String m = t.getMessage();
        return m == null ? "" : (m.length() > 200 ? m.substring(0, 200) + "..." : m);
    }
}
