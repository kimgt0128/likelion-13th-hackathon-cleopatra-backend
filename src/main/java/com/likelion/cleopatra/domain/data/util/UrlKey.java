package com.likelion.cleopatra.domain.data.util;

import com.likelion.cleopatra.domain.common.enums.Platform;
import org.springframework.util.DigestUtils;

import java.net.URI;
import java.nio.charset.StandardCharsets;

public final class UrlKey {
    private UrlKey() {}

    public static String canonicalize(String url) {
        try {
            URI u = URI.create(url.trim());
            String scheme = u.getScheme() == null ? "https" : u.getScheme().toLowerCase();
            String host = (u.getHost() == null ? "" : u.getHost().toLowerCase());
            String path = (u.getPath() == null ? "" : u.getPath());
            if (path.endsWith("/") && path.length() > 1) path = path.substring(0, path.length() - 1);
            // 쿼리/프래그먼트 제거 (트래킹 파라미터 등)
            return scheme + "://" + host + path;
        } catch (Exception e) {
            return url.trim();
        }
    }

    public static String idOf(Platform platform, String canonicalUrl) {
        String raw = platform.name() + "|" + canonicalUrl;
        return DigestUtils.md5DigestAsHex(raw.getBytes(StandardCharsets.UTF_8));
    }
}
