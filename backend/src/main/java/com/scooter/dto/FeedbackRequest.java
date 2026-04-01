package com.scooter.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class FeedbackRequest {
    @NotBlank(message = "标题不能为空")
    private String title;
    
    private String description;
}