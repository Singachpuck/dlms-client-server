package com.dochkas.mandjetDlmsWeb.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SupportingNotificationDto {

    private byte[] ldn;

    private float[] sensorValues;

    private float voltage;
}
