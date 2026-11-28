package org.example.campusclubmanagement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 安全与参数校验集成测试：验证未登录拦截、越权拦截、参数校验分支可被触发。
 * 全部用例在写库前被拦截，不污染演示数据。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SecurityAndValidationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void useBufferingFactory() {
        // 避免 HttpURLConnection 在 streaming 模式下收到 401 时无法重试读取响应体
        restTemplate.getRestTemplate().setRequestFactory(
                new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }

    private String login(String username, String password) throws Exception {
        HttpEntity<String> req = new HttpEntity<>(
                objectMapper.writeValueAsString(Map.of("username", username, "password", password)),
                jsonHeaders());
        String body = restTemplate.postForObject("/auth/login", req, String.class);
        JsonNode json = objectMapper.readTree(body);
        return json.path("data").path("token").asText();
    }

    @Test
    void loginMissingPasswordTriggersValidation() {
        HttpEntity<String> req = new HttpEntity<>("{\"username\":\"admin\"}", jsonHeaders());
        String body = restTemplate.postForObject("/auth/login", req, String.class);
        assertThat(body).contains("\"code\":400");
    }

    @Test
    void unauthenticatedCreateClubIsRejected() {
        HttpEntity<String> req = new HttpEntity<>(
                "{\"clubName\":\"x\",\"clubCode\":\"x\"}", jsonHeaders());
        // 未登录返回 401，触发 HttpURLConnection 的认证重试异常，以此证明请求被拒
        assertThatThrownBy(() -> restTemplate.postForObject("/clubs", req, String.class))
                .hasMessageContaining("server authentication");
    }

    @Test
    void studentCannotCreateClub() throws Exception {
        String token = login("2023001", "123456");
        HttpHeaders h = jsonHeaders();
        h.setBearerAuth(token);
        HttpEntity<String> req = new HttpEntity<>(
                "{\"clubName\":\"越权测试\",\"clubCode\":\"TEST\"}", h);
        String body = restTemplate.postForObject("/clubs", req, String.class);
        assertThat(body).contains("\"code\":403");
    }

    @Test
    void createClubBlankNameTriggersValidation() throws Exception {
        String token = login("admin", "123456");
        HttpHeaders h = jsonHeaders();
        h.setBearerAuth(token);
        // 缺 clubName：参数校验应先于业务执行，返回 400
        HttpEntity<String> req = new HttpEntity<>("{\"clubCode\":\"TEST\"}", h);
        String body = restTemplate.postForObject("/clubs", req, String.class);
        assertThat(body).contains("\"code\":400");
    }
}
