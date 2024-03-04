package com.ronyelison.quiz.service;

import com.ronyelison.quiz.dto.theme.ThemeRequest;
import com.ronyelison.quiz.dto.theme.ThemeResponse;
import com.ronyelison.quiz.dto.theme.ThemeUpdate;
import com.ronyelison.quiz.entity.Theme;
import com.ronyelison.quiz.entity.User;
import com.ronyelison.quiz.repository.ThemeRepository;
import com.ronyelison.quiz.service.exception.ThemeAlreadyExistsException;
import com.ronyelison.quiz.service.exception.ThemeNotFoundException;
import com.ronyelison.quiz.service.exception.UserNotHavePermissionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ThemeService {
    private ThemeRepository repository;
    private UserService userService;

    @Autowired
    public ThemeService(ThemeRepository repository, UserService userService) {
        this.repository = repository;
        this.userService = userService;
    }

    public ThemeResponse insertTheme(ThemeRequest themeRequest, String token) throws ThemeAlreadyExistsException {
        Theme theme = repository.findByNameIgnoreCase(themeRequest.name());

        if (theme != null){
            throw new ThemeAlreadyExistsException("Esse tema já foi cadastrado, tente novamente com outro Nome");
        }

        User user = userService.findUserByToken(token);

        Theme saveTheme = new Theme(themeRequest.name(), user);
        user.addTheme(saveTheme);

        repository.save(saveTheme);
        return saveTheme.entityToResponse();
    }

    public void removeTheme(Long id, String token) throws UserNotHavePermissionException {
        User user = userService.findUserByToken(token);

        Theme theme = repository.findById(id)
                .orElseThrow(() -> new ThemeNotFoundException("Tema não encontrado"));

        if (theme.containsQuestionsInTheList()){
            throw new DataIntegrityViolationException("Não é permitido remover um Tema que contém questões ligadas a ele");
        } else if (user.userNotHavePermission(theme.getCreator())) {
            throw new UserNotHavePermissionException("Usuário não tem permissão para remover esse tema");
        }

        repository.delete(theme);
    }

    public List<ThemeResponse> findAllThemes(){
        List<Theme> themes = repository.findAll();
        if (themes.isEmpty()){
            throw new ThemeNotFoundException("Nenhum tema foi cadastrado");
        }

        return themes.stream()
                .map(Theme::entityToResponse)
                .toList();
    }

    public ThemeResponse findThemeById(Long id){
        return repository.findById(id)
                .orElseThrow(() -> new ThemeNotFoundException("Tema não encontrado"))
                .entityToResponse();
    }

    public ThemeResponse updateTheme(Long id, ThemeUpdate themeUpdate, String token) throws UserNotHavePermissionException {
        User user = userService.findUserByToken(token);

        Theme theme = repository.findById(id)
                .orElseThrow(() -> new ThemeNotFoundException("Tema não encontrado"));

        if (user.userNotHavePermission(theme.getCreator())){
            throw new UserNotHavePermissionException("Usuário não tem permissão para atualizar esse tema");
        }

        updateData(theme, themeUpdate);
        repository.save(theme);

        return theme.entityToResponse();
    }

    private void updateData(Theme theme, ThemeUpdate themeUpdate){
        theme.setName(themeUpdate.name());
    }
}
