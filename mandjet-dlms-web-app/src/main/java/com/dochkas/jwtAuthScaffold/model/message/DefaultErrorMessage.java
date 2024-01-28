package com.dochkas.jwtAuthScaffold.model.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class DefaultErrorMessage {

    private final String status = "KO";

    private final String type;

    private final String description;
}
