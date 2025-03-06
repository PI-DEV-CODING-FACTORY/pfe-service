package com.example.pfe_service.dto;

import lombok.Data;
import java.util.List;
import java.time.Duration;

@Data
public class TestSubmissionRequest {
    private Long technicalTestId;
    private List<QuestionAnswer> answers;
    private Duration timeSpent;
    private Boolean cheated;

    @Data
    public static class QuestionAnswer {
        private Long questionId;
        private String answer;
    }
}