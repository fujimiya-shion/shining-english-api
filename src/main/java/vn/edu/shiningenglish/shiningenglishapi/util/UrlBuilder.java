package vn.edu.shiningenglish.shiningenglishapi.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UrlBuilder {

    @Value("${app.url:http://localhost:8080}")
    private String appUrl;

    public String buildThumbnailUrl(String thumbnail) {
        if (thumbnail == null || thumbnail.isBlank()) return null;
        thumbnail = thumbnail.trim();
        if (thumbnail.startsWith("http://") || thumbnail.startsWith("https://")) {
            return thumbnail;
        }
        if (thumbnail.startsWith("/storage/")) {
            return appUrl + thumbnail;
        }
        if (thumbnail.startsWith("public/")) {
            return appUrl + "/storage/" + thumbnail.substring("public/".length()).replaceAll("^/+", "");
        }
        return appUrl + "/storage/" + thumbnail.replaceAll("^/+", "");
    }

    public String buildAvatarUrl(String avatar) {
        if (avatar == null || avatar.isBlank()) return null;
        if (avatar.startsWith("http://") || avatar.startsWith("https://")) {
            return avatar;
        }
        return appUrl + "/storage/" + avatar.replaceAll("^/+", "");
    }
}
