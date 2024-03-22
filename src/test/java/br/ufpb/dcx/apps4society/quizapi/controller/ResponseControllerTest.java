package br.ufpb.dcx.apps4society.quizapi.controller;

import br.ufpb.dcx.apps4society.quizapi.QuizApplicationTests;
import br.ufpb.dcx.apps4society.quizapi.dto.alternative.AlternativeRequest;
import br.ufpb.dcx.apps4society.quizapi.dto.alternative.AlternativeResponse;
import br.ufpb.dcx.apps4society.quizapi.dto.question.QuestionRequest;
import br.ufpb.dcx.apps4society.quizapi.dto.question.QuestionResponse;
import br.ufpb.dcx.apps4society.quizapi.dto.response.ResponseDTO;
import br.ufpb.dcx.apps4society.quizapi.dto.theme.ThemeRequest;
import br.ufpb.dcx.apps4society.quizapi.dto.theme.ThemeResponse;
import br.ufpb.dcx.apps4society.quizapi.dto.user.UserLogin;
import br.ufpb.dcx.apps4society.quizapi.dto.user.UserRequest;
import br.ufpb.dcx.apps4society.quizapi.dto.user.UserResponse;
import br.ufpb.dcx.apps4society.quizapi.mock.MockAlternative;
import br.ufpb.dcx.apps4society.quizapi.mock.MockQuestion;
import br.ufpb.dcx.apps4society.quizapi.mock.MockTheme;
import br.ufpb.dcx.apps4society.quizapi.util.*;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ResponseControllerTest extends QuizApplicationTests {
    MockAlternative mockAlternative = new MockAlternative();
    MockQuestion mockQuestion = new MockQuestion();
    MockTheme mockTheme = new MockTheme();
    @Test
    void insertResponseByUserQuestionAlternative_shouldReturn201Test() {
        UserRequest userRequest = mockUser.mockRequest(1);
        UserResponse userResponse = UserRequestUtil.post(userRequest);
        String token = UserRequestUtil.login(mockUser.mockUserLogin());

        ThemeRequest themeRequest = mockTheme.mockRequest(1);
        ThemeResponse themeResponse = ThemeRequestUtil.post(themeRequest, token);

        QuestionRequest questionRequest = mockQuestion.mockRequest(1);
        QuestionResponse questionResponse = QuestionRequestUtil.post(questionRequest, token, themeResponse.id());

        AlternativeRequest alternativeRequest = mockAlternative.mockRequest(1);
        AlternativeResponse alternativeResponse = AlternativeRequestUtil.post(alternativeRequest, token, questionResponse.id());

        ResponseDTO responseDTO = given()
                .header("Authorization", "Bearer " + token)
                .when()
                .post(baseURI+":"+port+basePath+ ResponseRequestUtil.BASE_PATH_RESPONSE+"/"+userResponse.uuid()+"/"+questionResponse.id()+"/"+alternativeResponse.id())
                .then()
                .statusCode(201)
                .extract()
                .as(ResponseDTO.class);

        assertNotNull(responseDTO);
        assertNotNull(responseDTO.id());
        assertNotNull(responseDTO.user());
        assertNotNull(responseDTO.question());
        assertNotNull(responseDTO.alternative());
        assertNotNull(responseDTO.dateTime());

        QuestionRequestUtil.delete(questionResponse.id(), token);
        ThemeRequestUtil.delete(themeResponse.id(), token);
        UserRequestUtil.delete(userResponse);
    }

    @Test
    void findAllResponses() {
    }

    @Test
    void findResponsesByUser_shouldReturn200Test() {
        UserRequest userRequest = mockUser.mockRequest(1);
        UserResponse userResponse = UserRequestUtil.post(userRequest);
        String token = UserRequestUtil.login(mockUser.mockUserLogin());

        ThemeRequest themeRequest = mockTheme.mockRequest(1);
        ThemeResponse themeResponse = ThemeRequestUtil.post(themeRequest, token);

        QuestionRequest questionRequest = mockQuestion.mockRequest(1);
        QuestionResponse questionResponse = QuestionRequestUtil.post(questionRequest, token, themeResponse.id());

        AlternativeRequest alternativeRequest = mockAlternative.mockRequest(1);
        AlternativeResponse alternativeResponse = AlternativeRequestUtil.post(alternativeRequest, token, questionResponse.id());

        ResponseRequestUtil.post(userResponse.uuid(), questionResponse.id(),alternativeResponse.id(),token);

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get(baseURI + ":" + port + basePath + ResponseRequestUtil.BASE_PATH_RESPONSE + "/user")
                .then()
                .assertThat()
                .statusCode(200);

        QuestionRequestUtil.delete(questionResponse.id(), token);
        ThemeRequestUtil.delete(themeResponse.id(), token);
        UserRequestUtil.delete(userResponse);
    }

    @Test
    void findResponsesByQuestionCreator_shouldReturn200Test(){
        UserRequest userRequest = mockUser.mockRequest(1);
        UserResponse userResponse = UserRequestUtil.post(userRequest);
        String token = UserRequestUtil.login(mockUser.mockUserLogin());

        UserRequest anotherUser = new UserRequest("Another", "another@gmail.com","12345678");
        UserResponse anotherUserResponse = UserRequestUtil.post(anotherUser);
        UserLogin anotherUserLogin = new UserLogin(anotherUser.email(), anotherUser.password());
        String anotherUserToken = UserRequestUtil.login(anotherUserLogin);

        ThemeRequest themeRequest = mockTheme.mockRequest(1);
        ThemeResponse themeResponse = ThemeRequestUtil.post(themeRequest, token);

        QuestionRequest questionRequest = mockQuestion.mockRequest(1);
        QuestionResponse questionResponse = QuestionRequestUtil.post(questionRequest, token, themeResponse.id());

        AlternativeRequest alternativeRequest = mockAlternative.mockRequest(1);
        AlternativeResponse alternativeResponse = AlternativeRequestUtil.post(alternativeRequest, token, questionResponse.id());

        ResponseRequestUtil.post(anotherUserResponse.uuid(), questionResponse.id(),alternativeResponse.id(), anotherUserToken);

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get(baseURI + ":" + port + basePath + ResponseRequestUtil.BASE_PATH_RESPONSE + "/question/creator")
                .then()
                .assertThat()
                .statusCode(200);

        QuestionRequestUtil.delete(questionResponse.id(), token);
        ThemeRequestUtil.delete(themeResponse.id(), token);
        UserRequestUtil.delete(userResponse);
        UserRequestUtil.delete(anotherUserResponse.uuid(), anotherUserToken);
    }
}