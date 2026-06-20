package com.realestate.backend.auth

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthFlowIntegrationSpec extends Specification {

    private static final String REGISTER_USER_URL = "/auth/register/user"
    private static final String LOGIN_URL = "/auth/login"
    private static final String ME_URL = "/auth/me"

    private static final String DEFAULT_PASSWORD = "StrongPass123!"

    @Autowired
    MockMvc mockMvc

    @Autowired
    ObjectMapper objectMapper

    def "user can register login and access protected endpoint"() {
        given:
        String email = "user-${UUID.randomUUID()}@gmail.com"

        Map registerRequest = [
                fullName: "Test User",
                email   : email,
                password: DEFAULT_PASSWORD,
                phone   : "+994501112233"
        ]

        Map loginRequest = [
                email   : email,
                password: DEFAULT_PASSWORD
        ]

        when: "user registers"
        MvcResult registerResult = mockMvc.perform(
                post(REGISTER_USER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(registerRequest))
        ).andReturn()

        then:
        registerResult.response.status in [200, 201]

        when: "user logs in"
        MvcResult loginResult = mockMvc.perform(
                post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(loginRequest))
        )
                .andExpect(status().isOk())
                .andReturn()

        Map loginJson = readBody(loginResult)
        String accessToken = getValue(loginJson, "accessToken")

        then:
        accessToken != null
        accessToken.trim() != ""

        when: "user opens protected endpoint"
        MvcResult meResult = mockMvc.perform(
                get(ME_URL)
                        .header("Authorization", "Bearer ${accessToken}")
        ).andReturn()

        then:
        meResult.response.status == 200
    }

    def "protected endpoint returns 401 without token"() {
        expect:
        mockMvc.perform(
                get(ME_URL)
        )
                .andExpect(status().isUnauthorized())
    }

    def "login fails with wrong password"() {
        given:
        String email = "wrong-${UUID.randomUUID()}@gmail.com"

        Map registerRequest = [
                fullName: "Wrong Password User",
                email   : email,
                password: DEFAULT_PASSWORD,
                phone   : "+994501112244"
        ]

        Map wrongLoginRequest = [
                email   : email,
                password: "WrongPass123!"
        ]

        and:
        MvcResult registerResult = mockMvc.perform(
                post(REGISTER_USER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(registerRequest))
        ).andReturn()

        assert registerResult.response.status in [200, 201]

        expect:
        mockMvc.perform(
                post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(wrongLoginRequest))
        )
                .andExpect(status().isUnauthorized())
    }

    private String toJson(Object value) {
        return objectMapper.writeValueAsString(value)
    }

    private Map readBody(MvcResult result) {
        String body = result.response.contentAsString

        if (body == null || body.trim().isEmpty()) {
            return [:]
        }

        return objectMapper.readValue(body, Map)
    }

    private static String getValue(Map json, String key) {
        if (json[key] != null) {
            return json[key] as String
        }

        if (json.data instanceof Map && json.data[key] != null) {
            return json.data[key] as String
        }

        if (json.payload instanceof Map && json.payload[key] != null) {
            return json.payload[key] as String
        }

        if (json.result instanceof Map && json.result[key] != null) {
            return json.result[key] as String
        }

        return null
    }
}