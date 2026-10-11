package com.example.BI.Chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
    private String message;
    private List<ChatMessage> history; // ~10 derniers messages max
}
