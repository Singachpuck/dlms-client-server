package com.dochkas.jwtAuthScaffold.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MandjetDto {

    private int[][] sensorValues;

    private float voltage;

    private int battery;
}
