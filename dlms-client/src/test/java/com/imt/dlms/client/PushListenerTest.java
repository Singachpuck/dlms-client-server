package com.imt.dlms.client;

import gurux.dlms.enums.InterfaceType;
import org.junit.jupiter.api.Test;

import java.util.Scanner;

public class PushListenerTest {

    private final Scanner in = new Scanner(System.in);

    private final int port = 4060;

    private final InterfaceType type = InterfaceType.WRAPPER;

    public static void main(String[] args) throws Exception {
        final PushListenerTest t = new PushListenerTest();
        t.receivePush();
    }

    void receivePush() throws Exception {
        try (PushListener SNServer = new PushListener(port, type)) {
            System.out.println("Starting to listen Push messages in port "
                    + port);

            while (!in.nextLine().equals("exit")) {
                System.out.println("Type 'exit' to close.");
            }
        }
    }
}
