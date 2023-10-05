package com.example.jwttests.domain;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DecodedJWT {

    JsonNode header;
    JsonNode payload;
    JsonNode signature;
}
