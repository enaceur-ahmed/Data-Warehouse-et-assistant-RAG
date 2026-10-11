package com.example.BI.Chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatSource {
    /** "chunk" (donnée RAG) ou "computation" (calcul exact sur la DB) */
    private String type;
    private String label;
    private String detail;
}
