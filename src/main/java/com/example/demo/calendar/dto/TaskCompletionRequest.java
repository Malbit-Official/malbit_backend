package com.example.demo.calendar.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TaskCompletionRequest {

    @JsonProperty("is_completed") 
    private boolean isCompleted;
}