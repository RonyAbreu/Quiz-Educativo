package br.ufpb.dcx.apps4society.quizapi.dto.question;

import br.ufpb.dcx.apps4society.quizapi.dto.alternative.AlternativeResponse;
import br.ufpb.dcx.apps4society.quizapi.dto.theme.ThemeResponse;

import java.util.List;
import java.util.UUID;

public record QuestionResponse(
         Long id,
         String title,
         String imageUrl,
         UUID creatorId,
         ThemeResponse theme,
         List<AlternativeResponse> alternatives) {
}
