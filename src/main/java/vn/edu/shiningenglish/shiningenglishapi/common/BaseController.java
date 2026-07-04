package vn.edu.shiningenglish.shiningenglishapi.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class BaseController {

    protected ResponseEntity<Map<String, Object>> success(String message, Object data) {
        return success(message, data, HttpStatus.OK, null);
    }

    protected ResponseEntity<Map<String, Object>> success(Object data) {
        return success("OK", data, HttpStatus.OK, null);
    }

    protected ResponseEntity<Map<String, Object>> success(String message, Object data, int statusCode) {
        return success(message, data, HttpStatus.valueOf(statusCode), null);
    }

    protected ResponseEntity<Map<String, Object>> success(String message, Object data, int statusCode, Object meta) {
        return success(message, data, HttpStatus.valueOf(statusCode), meta);
    }

    protected ResponseEntity<Map<String, Object>> success(String message, Object data, HttpStatus status, Object meta) {
        var body = new LinkedHashMap<String, Object>();
        body.put("message", message);
        body.put("status", true);
        body.put("status_code", status.value());
        body.put("data", data);
        if (meta != null) {
            body.put("meta", meta);
        }
        return ResponseEntity.status(status).body(body);
    }

    protected ResponseEntity<Map<String, Object>> created(Object data) {
        return created(data, "Created");
    }

    protected ResponseEntity<Map<String, Object>> created(Object data, String message) {
        return success(message, data, HttpStatus.CREATED, null);
    }

    protected ResponseEntity<Map<String, Object>> deleted(String message) {
        return success(message, null, HttpStatus.OK, null);
    }

    protected ResponseEntity<Map<String, Object>> error(String message, int statusCode) {
        return error(message, statusCode, null);
    }

    protected ResponseEntity<Map<String, Object>> error(String message, int statusCode, Object errors) {
        var body = new LinkedHashMap<String, Object>();
        body.put("message", message);
        body.put("status", false);
        body.put("status_code", statusCode);
        if (errors != null) {
            body.put("errors", errors);
        }
        return ResponseEntity.status(statusCode).body(body);
    }

    protected ResponseEntity<Map<String, Object>> notfound() {
        return notfound("Not found");
    }

    protected ResponseEntity<Map<String, Object>> notfound(String message) {
        return error(message, 404);
    }

    protected ResponseEntity<Map<String, Object>> unauthorized(String message) {
        return error(message, 401);
    }

    protected ResponseEntity<Map<String, Object>> forbidden(String message) {
        return error(message, 403);
    }
}
