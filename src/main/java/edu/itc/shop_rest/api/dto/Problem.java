package edu.itc.shop_rest.api.dto;

import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Problem(String type, String title, int status, String detail,
                      String instance, List<Map<String, String>> errors) {
    public static final String MEDIA_TYPE = "application/problem+json";
}