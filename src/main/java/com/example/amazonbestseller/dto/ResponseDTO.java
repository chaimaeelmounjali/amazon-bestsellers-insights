// File: ResponseDTO.java
package com.example.amazonbestseller.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseDTO {
    private Boolean success;
    private String message;
    private Object data;

    public static ResponseDTO success(String message, Object data) {
        return new ResponseDTO(true, message, data);
    }

    public static ResponseDTO error(String message) {
        return new ResponseDTO(false, message, null);
    }
}
