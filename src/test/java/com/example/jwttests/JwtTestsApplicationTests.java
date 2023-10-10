package com.example.jwttests;

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

    public enum Env{
        TEST,
        SAND
    }



    private RequestSpecification blurbCoreRequestTest(){
        RequestSpecification specification = given()
                .config(RestAssured.config()
                        .logConfig(logConfig().enableLoggingOfRequestAndResponseIfValidationFails(LogDetail.ALL)))
                .baseUri(testConfiguration.getBlurbCoreBaseUrlTest())
                .contentType(ContentType.JSON)
                .filter(new RequestLoggingFilter())
                .filter(new ResponseLoggingFilter());
        return specification;
    }

    private RequestSpecification connectorRequestTest(){
        RequestSpecification specification = given()
                .config(RestAssured.config()
                        .logConfig(logConfig().enableLoggingOfRequestAndResponseIfValidationFails(LogDetail.ALL)))
                .baseUri(testConfiguration.getConnectorBaseUrlTest())
                .contentType(ContentType.JSON)
                .filter(new RequestLoggingFilter())
                .filter(new ResponseLoggingFilter());
        return specification;
    }

    private RequestSpecification blurbCoreRequestSandbox(){
        RequestSpecification specification = given()
                .config(RestAssured.config()
                        .logConfig(logConfig().enableLoggingOfRequestAndResponseIfValidationFails(LogDetail.ALL)))
                .baseUri(testConfiguration.getBlurbCoreBaseUrlSand())
                .contentType(ContentType.JSON)
                .filter(new RequestLoggingFilter())
                .filter(new ResponseLoggingFilter());
        return specification;
    }

    private RequestSpecification connectorRequestSandbox(){
        RequestSpecification specification = given()
                .config(RestAssured.config()
                        .logConfig(logConfig().enableLoggingOfRequestAndResponseIfValidationFails(LogDetail.ALL)))
                .baseUri(testConfiguration.getConnectorBaseUrlSand())
                .contentType(ContentType.JSON)
                .filter(new RequestLoggingFilter())
                .filter(new ResponseLoggingFilter());
        return specification;
    }

    private String getAccessToken(Env env){
        if (env == Env.TEST){
            String token = blurbCoreRequestTest()
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
        }else if (env == Env.SAND){

            String token = blurbCoreRequestSandbox()
                    .body("""
                                {"username":"gorazd-21092023",
                                 "password":"123456"
                                 }
                            """)
                    .when()
                    .post("/auth/login")
                    .then()
                    .extract()
                    .response().path("accessToken");

            return token;
        }
        return "Set env";
    }

    @Test
    void validateTokenBlurbCore() {
        blurbCoreRequestTest()
                .header("Authorization", "Bearer "+getAccessToken(Env.TEST))
                .get("/users/me")
                .then()
                .assertThat()
                .statusCode(200);

    }

    @Test
    void validateTokenConnector() {
        connectorRequestTest()
                .header("Authorization", "Bearer "+getAccessToken(Env.TEST))
                .get("/validate-jwt")
                .then()
                .assertThat()
                .statusCode(200);
    }

    @Test
    void validateTokenNoAlgConnector(){
        String tokenHeaderAlgNone = "eyJ0eXAiOiJKV1QiLCJhbGciOiJub25lIn0.";

        String validToken = getAccessToken(Env.TEST);
        String[] splitToken = validToken.split("\\.");

        String tokenAlgNone = tokenHeaderAlgNone+splitToken[1]+".";

        connectorRequestTest()
                .header("Authorization", "Bearer "+tokenAlgNone)
                .get("/validate-jwt")
                .then()
                .assertThat()
                .statusCode(403);
    }

    @Test
    void validateTokenNoAlgBlurbCore(){
       String tokenHeaderAlgNone = "eyJ0eXAiOiJKV1QiLCJhbGciOiJub25lIn0.";

       String validToken = getAccessToken(Env.TEST);
       String[] splitToken = validToken.split("\\.");

       String tokenAlgNone = tokenHeaderAlgNone+splitToken[1]+".";

        blurbCoreRequestTest()
                .header("Authorization", "Bearer "+tokenAlgNone)
                .get("/users/me")
                .then()
                .assertThat()
                .statusCode(401);

    }

    @Test
    void validateTestTokenOnSandboxConnector(){
        String token = getAccessToken(Env.TEST);

        connectorRequestTest()
                .header("Authorization", "Bearer "+token)
                .get("/validate-jwt")
                .then()
                .assertThat()
                .statusCode(200);

        connectorRequestSandbox()
                .header("Authorization", "Bearer "+token)
                .get("/validate-jwt")
                .then()
                .assertThat()
                .statusCode(403);
    }

    @Test
    void validateSandTokenOnTestConnector(){
        String token = getAccessToken(Env.SAND);

        connectorRequestSandbox()
                .header("Authorization", "Bearer "+token)
                .get("/validate-jwt")
                .then()
                .assertThat()
                .statusCode(200);

        connectorRequestTest()
                .header("Authorization", "Bearer "+token)
                .get("/validate-jwt")
                .then()
                .assertThat()
                .statusCode(403);
    }

    @Test
    void validateTestTokenOnSandboxBlurbCore(){
        String token = getAccessToken(Env.TEST);

        blurbCoreRequestTest()
                .header("Authorization", "Bearer "+token)
                .get("/users/me")
                .then()
                .assertThat()
                .statusCode(200);

        blurbCoreRequestSandbox()
                .header("Authorization", "Bearer "+token)
                .get("/users/me")
                .then()
                .assertThat()
                .statusCode(401);
    }

    @Test
    void validateSandTokenOnTestBlurbCore(){
        String token = getAccessToken(Env.SAND);

        blurbCoreRequestSandbox()
                .header("Authorization", "Bearer "+token)
                .get("/users/me")
                .then()
                .assertThat()
                .statusCode(200);

        blurbCoreRequestTest()
                .header("Authorization", "Bearer "+token)
                .get("/users/me")
                .then()
                .assertThat()
                .statusCode(401);
    }

}
