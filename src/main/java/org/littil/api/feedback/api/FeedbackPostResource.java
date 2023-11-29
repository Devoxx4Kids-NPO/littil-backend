package org.littil.api.feedback.api;

import lombok.Data;

@Data
public class FeedbackPostResource {

    private String feedbackType;

    private String message;

    private Long timestamp;

}
