package com.ronyelison.quiz.entity;

import com.ronyelison.quiz.dto.response.ResponseDTO;
import jakarta.persistence.*;

import java.time.LocalDateTime;
@Entity(name = "tb_response")
public class Response {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime dateTime = LocalDateTime.now();
    @ManyToOne(cascade = CascadeType.PERSIST)
    private User user;
    @ManyToOne(cascade = CascadeType.PERSIST)
    private Question question;
    @ManyToOne(cascade = CascadeType.PERSIST)
    private Alternative alternative;

    public Response() {

    }

    public Response(User user, Question question, Alternative alternative) {
        this.user = user;
        this.question = question;
        this.alternative = alternative;
    }

    public ResponseDTO entityToResponse(){
        return new ResponseDTO(id, dateTime,
                user.entityToResponse(),
                question.entityToMinResponse(),
                alternative.entityToResponse());
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public User getUser() {
        return user;
    }

    public Question getQuestion() {
        return question;
    }

    public Alternative getAlternative() {
        return alternative;
    }
}
