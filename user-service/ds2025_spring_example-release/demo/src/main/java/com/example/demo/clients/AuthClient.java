package com.example.demo.clients;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
public class AuthClient {
    private static final Logger log = LoggerFactory.getLogger(AuthClient.class);

    private final RestTemplate restTemplate;

    @Value("${auth.service.base-url:http://localhost:8080}")
    private String authServiceBaseUrl;

    @Value("${internal.token:changeme}")
    private String internalToken;

    public AuthClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // Folosit când auth-service ne cere ștergerea (intern, fără roluri)
    public void cascadeDeleteUserInternal(UUID userId) {
        String url = String.format("%s/internal/users/%s", authServiceBaseUrl, userId);
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Internal-Token", internalToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<Void> resp = restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
            log.info("Internal cascade delete in auth-service for user {} responded {}", userId, resp.getStatusCode());
        } catch (HttpClientErrorException.NotFound nf) {
            log.warn("User {} not found in auth-service (internal cascade)", userId);
        } catch (HttpClientErrorException.Forbidden fb) {
            log.error("Forbidden internal cascade delete. Check INTERNAL_TOKEN config.");
            throw fb;
        } catch (HttpClientErrorException e) {
            log.error("Internal cascade delete failed status {} body {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        }
    }

    // Folosit când un ADMIN cere ștergerea unui user
    public void cascadeDeleteUserWithAuth(UUID userId, String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing/invalid Authorization header for admin cascade delete of user {}", userId);
            return; // Security ar trebui să fi blocat deja cererea
        }
        String url = String.format("%s/admin/users/%s", authServiceBaseUrl, userId);
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, authHeader);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<Void> resp = restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
            log.info("Admin cascade delete user {} responded {}", userId, resp.getStatusCode());
        } catch (HttpClientErrorException.Forbidden fb) {
            log.warn("Forbidden deleting user {} via admin endpoint (probabil încercare de a șterge alt ADMIN)", userId);
            throw fb;
        } catch (HttpClientErrorException.NotFound nf) {
            log.warn("User {} not found in auth-service (admin cascade)", userId);
        } catch (HttpClientErrorException e) {
            log.error("Admin cascade delete failed status {} body {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        }
    }

    public record CurrentUserInfo(UUID id, String username, String role) {}

    public CurrentUserInfo getCurrentUser(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String url = String.format("%s/me", authServiceBaseUrl);
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, authHeader);
        try {
            ResponseEntity<java.util.Map> resp = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), java.util.Map.class);
            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                return null;
            }
            var body = resp.getBody();
            Object idObj = body.get("userId");
            Object roleObj = body.get("role");
            Object usernameObj = body.get("username");
            UUID id = idObj == null ? null : UUID.fromString(idObj.toString());
            String role = roleObj == null ? null : roleObj.toString();
            String username = usernameObj == null ? null : usernameObj.toString();
            return new CurrentUserInfo(id, username, role);
        } catch (HttpClientErrorException e) {
            log.warn("Failed to get current user info: status {}", e.getStatusCode());
            return null;
        }
    }
}
