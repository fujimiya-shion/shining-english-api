package vn.edu.shiningenglish.shiningenglishapi.valueobject;

import java.util.Map;

public class DeviceInfo {
    private String identifier;
    private String name;
    private String platform;
    private String ipAddress;
    private String userAgent;

    public DeviceInfo(String identifier, String name, String platform, String ipAddress, String userAgent) {
        if (identifier == null || identifier.isBlank()) {
            throw new IllegalArgumentException("device_identifier is required");
        }
        this.identifier = identifier;
        this.name = name;
        this.platform = platform;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }

    public static DeviceInfo fromArray(Map<String, String> data) {
        return new DeviceInfo(
            data.get("device_identifier"),
            data.get("device_name"),
            data.get("platform"),
            data.get("ip_address"),
            data.get("user_agent")
        );
    }

    public String getIdentifier() { return identifier; }
    public String getName() { return name; }
    public String getPlatform() { return platform; }
    public String getIpAddress() { return ipAddress; }
    public String getUserAgent() { return userAgent; }
}
