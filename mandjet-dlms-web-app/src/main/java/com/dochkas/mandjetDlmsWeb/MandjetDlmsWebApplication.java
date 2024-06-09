package com.dochkas.mandjetDlmsWeb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class MandjetDlmsWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(MandjetDlmsWebApplication.class, args);
    }

}
