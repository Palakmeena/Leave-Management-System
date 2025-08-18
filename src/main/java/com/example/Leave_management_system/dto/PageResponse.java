// src/main/java/com/example/Leave_management_system/dto/PageResponse.java
package com.example.Leave_management_system.dto;

import lombok.*;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PageResponse<T> {
    private List<T> items;
    private int page;
    private int size;
    private long total;
}
