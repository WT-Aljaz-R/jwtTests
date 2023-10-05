package com.example.jwttests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.Base64;
import com.example.jwttests.domain.DecodedJWT;

import static io.restassured.RestAssured.given;
import static io.restassured.config.LogConfig.logConfig;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@EnableConfigurationProperties(value = {
        TestConfiguration.class
})
public class JwtTestsApplicationTests {

    private final TestConfiguration testConfiguration;

    @SpringBootConfiguration
    public static class TestConfig {}

    @Autowired
    public JwtTestsApplicationTests(TestConfiguration testConfiguration) throws IOException {
        this.testConfiguration = testConfiguration;
    }



    private RequestSpecification getJwtRequest(){
        RequestSpecification specification = given()
                .config(RestAssured.config()
                        .logConfig(logConfig().enableLoggingOfRequestAndResponseIfValidationFails(LogDetail.ALL)))
                .baseUri("https://cornerstone-test-internal.builder.blurb.com")
                .contentType(ContentType.JSON)
                .filter(new RequestLoggingFilter())
                .filter(new ResponseLoggingFilter());
        return specification;
    }

    private RequestSpecification connectorRequest(){
        RequestSpecification specification = given()
                .config(RestAssured.config()
                        .logConfig(logConfig().enableLoggingOfRequestAndResponseIfValidationFails(LogDetail.ALL)))
                .baseUri("https://blurby-connector-test.builder.blurb.com/blurby-connector-public")
                .contentType(ContentType.JSON)
                .filter(new RequestLoggingFilter())
                .filter(new ResponseLoggingFilter());
        return specification;
    }

    private String getAccessToken(){
        String token = getJwtRequest()
                .body("""
                        {"username":"test-09082023",
                         "password":"Banana123"
                         }
                        """)
                .when()
                .post("/auth/login")
                .then()
                .extract()
                .response().path("accessToken");

        return token;
    }

    private DecodedJWT decodeJWT() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String jwtToken = getAccessToken();
        String[] splitString = jwtToken.split("\\.");
        String base64EncodedHeader = splitString[0];
        String base64EncodedPayload = splitString[1];
        String base64EncodedSignature = splitString[2];

        String header = new String(Base64.getUrlDecoder().decode(base64EncodedHeader));
        String payload = new String(Base64.getUrlDecoder().decode(base64EncodedPayload));
        String signature = new String(Base64.getUrlDecoder().decode(base64EncodedSignature));

        DecodedJWT jwt = new DecodedJWT();
        jwt.setHeader(mapper.readTree(header));
        jwt.setPayload(mapper.readTree(payload));
        jwt.setSignature(mapper.readTree(signature));

        return jwt;
    }

    @Test
    void getToken() {
        getJwtRequest()
                .body("""
                        {"username":"test-09082023",
                         "password":"Banana123"
                         }
                        """)
                .when()
                .post("/auth/login")
                .then()
                .assertThat()
                .statusCode(200);

    }

    @Test
    void validateToken() {
        connectorRequest()
                .header("Authorization", "Bearer "+getAccessToken())
                .get("/validate-jwt")
                .then()
                .assertThat()
                .statusCode(200);
    }

    @Test
    void validateTokenNoAlg() throws JsonProcessingException {
        DecodedJWT decodedJWT = decodeJWT();

        System.out.println(decodedJWT.getHeader().findValue("alg"));
    }

}
