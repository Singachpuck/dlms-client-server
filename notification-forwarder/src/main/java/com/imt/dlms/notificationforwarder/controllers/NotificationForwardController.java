package com.imt.dlms.notificationforwarder.controllers;

import com.imt.dlms.notificationforwarder.model.ForwardData;
import com.imt.dlms.notificationforwarder.service.NotificationForwardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;

@RestController
@RequestMapping("/forward")
public class NotificationForwardController {

    private final Logger logger = LoggerFactory.getLogger(NotificationForwardController.class);

    @Autowired
    private NotificationForwardService forwardService;

    @PostMapping
    ResponseEntity<?> forward(@RequestBody ForwardData data) {
        if (data.getData() != null) {
            forwardService.forwardDlmsNotification(data.getData());
        }
        return ResponseEntity.ok().build();
    }
}
