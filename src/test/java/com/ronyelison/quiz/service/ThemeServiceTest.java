package com.ronyelison.quiz.service;

import com.ronyelison.quiz.dto.theme.ThemeRequest;
import com.ronyelison.quiz.dto.theme.ThemeResponse;
import com.ronyelison.quiz.dto.theme.ThemeUpdate;
import com.ronyelison.quiz.entity.Question;
import com.ronyelison.quiz.entity.Theme;
import com.ronyelison.quiz.entity.User;
import com.ronyelison.quiz.mock.MockQuestion;
import com.ronyelison.quiz.mock.MockTheme;
import com.ronyelison.quiz.mock.MockUser;
import com.ronyelison.quiz.repository.ThemeRepository;
import com.ronyelison.quiz.service.exception.ThemeAlreadyExistsException;
import com.ronyelison.quiz.service.exception.ThemeNotFoundException;
import com.ronyelison.quiz.service.exception.UserNotHavePermissionException;
import com.ronyelison.quiz.util.Messages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ThemeServiceTest {
    MockTheme mockTheme;
    MockUser mockUser;
    MockQuestion mockQuestion;
    @Mock
    ThemeRepository repository;
    @Mock
    UserService userService;
    @InjectMocks
    ThemeService themeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockTheme = new MockTheme();
        mockUser = new MockUser();
        mockQuestion = new MockQuestion();
    }

    @Test
    void insertTheme() throws ThemeAlreadyExistsException {
        ThemeRequest themeRequest = mockTheme.mockDTO(1);
        User user = mockUser.mockEntity(1);
        Theme theme = mockTheme.mockEntity(1);

        Mockito.lenient().when(repository.findByNameIgnoreCase(theme.getName())).thenReturn(null);
        Mockito.lenient().when(userService.findUserByToken(MockUser.MOCK_TOKEN)).thenReturn(user);
        Mockito.lenient().when(repository.save(theme)).thenReturn(theme);

        ThemeResponse result = themeService.insertTheme(themeRequest, MockUser.MOCK_TOKEN);

        assertNotNull(result);
        assertEquals("Tema", result.name());
    }

    @Test
    void insertThemeAlreadyExists() {
        ThemeRequest themeRequest = mockTheme.mockDTO(1);
        User user = mockUser.mockEntity(1);
        Theme theme = mockTheme.mockEntity(1);

        Mockito.lenient().when(repository.findByNameIgnoreCase(theme.getName())).thenReturn(theme);
        Mockito.lenient().when(userService.findUserByToken(MockUser.MOCK_TOKEN)).thenReturn(user);
        Mockito.lenient().when(repository.save(theme)).thenReturn(theme);

        Exception e = assertThrows(ThemeAlreadyExistsException.class, () ->{
            themeService.insertTheme(themeRequest, MockUser.MOCK_TOKEN);
        });

        assertEquals(e.getMessage(), Messages.THEME_ALREADY_EXISTS);
    }

    @Test
    void removeTheme() {
        User user = mockUser.mockEntity(1);
        Theme theme = mockTheme.mockEntity(1, user);

        Mockito.lenient().when(userService.findUserByToken(MockUser.MOCK_TOKEN)).thenReturn(user);
        Mockito.lenient().when(repository.findById(theme.getId())).thenReturn(Optional.of(theme));

        assertDoesNotThrow(() -> themeService.removeTheme(theme.getId(), MockUser.MOCK_TOKEN));
        verify(repository, times(1)).delete(theme);
    }

    @Test
    void removeThemeNotFound() {
        User user = mockUser.mockEntity(1);
        Theme theme = mockTheme.mockEntity(1, user);
        Theme falseTheme = mockTheme.mockEntity(2);

        Mockito.lenient().when(userService.findUserByToken(MockUser.MOCK_TOKEN)).thenReturn(user);
        Mockito.lenient().when(repository.findById(theme.getId())).thenReturn(Optional.of(theme));

        Exception e = assertThrows(ThemeNotFoundException.class, () ->{
            themeService.removeTheme(falseTheme.getId(), MockUser.MOCK_TOKEN);
        });

        assertEquals(e.getMessage(), Messages.THEME_NOT_FOUND);
    }

    @Test
    void removeThemeWithQuestions() {
        User user = mockUser.mockEntity(1);
        Theme theme = mockTheme.mockEntity(1, user);
        Question question = mockQuestion.mockEntity(1);
        theme.addQuestion(question);

        Mockito.lenient().when(userService.findUserByToken(MockUser.MOCK_TOKEN)).thenReturn(user);
        Mockito.lenient().when(repository.findById(theme.getId())).thenReturn(Optional.of(theme));

        Exception e = assertThrows(DataIntegrityViolationException.class, () ->{
            themeService.removeTheme(theme.getId(), MockUser.MOCK_TOKEN);
        });

        assertEquals(e.getMessage(), Messages.REMOVE_THEME_WITH_QUESTIONS);
    }

    @Test
    void removeThemeNotHavePermission() {
        User user = mockUser.mockEntity(1);
        User falseUser = mockUser.mockEntity(2);
        Theme theme = mockTheme.mockEntity(1, user);

        Mockito.lenient().when(userService.findUserByToken(MockUser.MOCK_TOKEN)).thenReturn(falseUser);
        Mockito.lenient().when(repository.findById(theme.getId())).thenReturn(Optional.of(theme));

        Exception e = assertThrows(UserNotHavePermissionException.class, () ->{
            themeService.removeTheme(theme.getId(), MockUser.MOCK_TOKEN);
        });

        assertEquals(e.getMessage(), Messages.USER_NOT_HAVE_PERMISSION_FOR_REMOVE_THEME);
    }

    @Test
    void findAllThemes() {
        Pageable pageable = mock(Pageable.class);

        List<Theme> themeList = Collections.singletonList(new Theme("TestTheme", new User("userId", "userName", "userToken")));
        Page<Theme> themePage = new PageImpl<>(themeList);
        Mockito.lenient().when(repository.findAll(pageable)).thenReturn(themePage);

        assertDoesNotThrow(() -> themeService.findAllThemes(pageable));
        assertEquals(1, themePage.getNumberOfElements());

        verify(repository, times(1)).findAll(pageable);
    }

    @Test
    void findAllThemesNotFound() {
        Pageable pageable = mock(Pageable.class);

        List<Theme> themeList = new ArrayList<>();
        Page<Theme> themePage = new PageImpl<>(themeList);
        Mockito.lenient().when(repository.findAll(pageable)).thenReturn(themePage);

        Exception e = assertThrows(ThemeNotFoundException.class, () ->{
            themeService.findAllThemes(pageable);
        });

        assertEquals(0, themePage.getNumberOfElements());
    }

    @Test
    void findThemeById() {
        Theme theme = mockTheme.mockEntity(1);

        Mockito.lenient().when(repository.findById(theme.getId())).thenReturn(Optional.of(theme));

        ThemeResponse result = themeService.findThemeById(theme.getId());

        assertNotNull(result);
        assertEquals("Tema", result.name());
    }

    @Test
    void findThemeByIdNotFound() {
        Theme theme = mockTheme.mockEntity(1);
        Theme falseTheme = mockTheme.mockEntity(2);

        Mockito.lenient().when(repository.findById(theme.getId())).thenReturn(Optional.of(theme));

        Exception e = assertThrows(ThemeNotFoundException.class, ()-> {
            themeService.findThemeById(falseTheme.getId());
        });

        assertEquals(e.getMessage(), Messages.THEME_NOT_FOUND);
    }

    @Test
    void updateTheme() throws UserNotHavePermissionException {
        ThemeUpdate themeUpdate = mockTheme.mockThemeUpdate();
        User user = mockUser.mockEntity(1);
        Theme theme = mockTheme.mockEntity(1, user);

        Mockito.lenient().when(userService.findUserByToken(MockUser.MOCK_TOKEN)).thenReturn(user);
        Mockito.lenient().when(repository.findById(theme.getId())).thenReturn(Optional.of(theme));

        ThemeResponse result = themeService.updateTheme(theme.getId(), themeUpdate, MockUser.MOCK_TOKEN);

        Mockito.lenient().when(repository.save(theme)).thenReturn(theme);

        assertNotNull(result);
        assertEquals("Novo tema", result.name());
    }

    @Test
    void updateThemeNotFound() {
        ThemeUpdate themeUpdate = mockTheme.mockThemeUpdate();
        User user = mockUser.mockEntity(1);
        Theme theme = mockTheme.mockEntity(1, user);
        Theme falseTheme = mockTheme.mockEntity(2);

        Mockito.lenient().when(userService.findUserByToken(MockUser.MOCK_TOKEN)).thenReturn(user);
        Mockito.lenient().when(repository.findById(theme.getId())).thenReturn(Optional.of(theme));

        Exception e = assertThrows(ThemeNotFoundException.class, () ->{
            themeService.updateTheme(falseTheme.getId(), themeUpdate, MockUser.MOCK_TOKEN);
        });

        assertEquals(e.getMessage(), Messages.THEME_NOT_FOUND);
    }

    @Test
    void updateThemeNotHavePermission() {
        ThemeUpdate themeUpdate = mockTheme.mockThemeUpdate();
        User user = mockUser.mockEntity(1);
        Theme theme = mockTheme.mockEntity(1, user);
        User falseUser = mockUser.mockEntity(2);

        Mockito.lenient().when(userService.findUserByToken(MockUser.MOCK_TOKEN)).thenReturn(falseUser);
        Mockito.lenient().when(repository.findById(theme.getId())).thenReturn(Optional.of(theme));

        Exception e = assertThrows(UserNotHavePermissionException.class, () ->{
            themeService.updateTheme(theme.getId(), themeUpdate, MockUser.MOCK_TOKEN);
        });

        assertEquals(e.getMessage(), Messages.USER_NOT_HAVE_PERMISSION_FOR_UPDATE_THEME);
    }
}