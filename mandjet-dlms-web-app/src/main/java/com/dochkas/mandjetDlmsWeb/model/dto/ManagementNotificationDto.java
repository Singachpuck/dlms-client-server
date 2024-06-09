package com.dochkas.mandjetDlmsWeb.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class ManagementNotificationDto {

    private byte[] ldn;

    private Date timestamp;

    private short battery;
}
