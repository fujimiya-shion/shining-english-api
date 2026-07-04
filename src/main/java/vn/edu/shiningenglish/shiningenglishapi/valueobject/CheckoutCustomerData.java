package vn.edu.shiningenglish.shiningenglishapi.valueobject;

import java.util.Map;

public class CheckoutCustomerData {
    private String fullName;
    private String email;
    private String phone;

    public CheckoutCustomerData(String fullName, String email, String phone) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
    }

    public static CheckoutCustomerData fromArray(Map<String, String[]> data) {
        return new CheckoutCustomerData(
            normalize(data.containsKey("buyer_name") ? data.get("buyer_name")[0] : 
                     data.containsKey("full_name") ? data.get("full_name")[0] : null),
            normalize(data.containsKey("buyer_email") ? data.get("buyer_email")[0] : 
                     data.containsKey("email") ? data.get("email")[0] : null),
            normalize(data.containsKey("buyer_phone") ? data.get("buyer_phone")[0] : 
                     data.containsKey("phone") ? data.get("phone")[0] : null)
        );
    }

    private static String normalize(String value) {
        if (value == null) return null;
        var trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static CheckoutCustomerData fromRawMap(Map<String, Object> data) {
        return new CheckoutCustomerData(
            normalize((String) data.getOrDefault("buyer_name", data.getOrDefault("full_name", null))),
            normalize((String) data.getOrDefault("buyer_email", data.getOrDefault("email", null))),
            normalize((String) data.getOrDefault("buyer_phone", data.getOrDefault("phone", null)))
        );
    }

    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
}
