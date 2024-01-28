package com.imt.dlms.server;

import java.util.Scanner;

public class DlmsServerApplication {

    private final Scanner in = new Scanner(System.in);

    private final int port = 4059;

    public static void main(String[] args) throws Exception {
        final DlmsServerApplication app = new DlmsServerApplication();
        app.init();
    }

    public void init() throws Exception {
        final ServerManager manager = new ServerManager();
        manager.init();
        System.out.println(
                "Logical Name DLMS Server in port " + port);
        System.out.println("Example connection settings:");
        System.out.println("Gurux.DLMS.Client.Example.Net -h localhost -p " + port);
        System.out.println("----------------------------------------------------------");

        System.out.println("Type 'exit' to close.");

        while (!in.nextLine().equals("exit")) {
            System.out.println("Type 'exit' to close.");
        }

        /// Close servers.
        System.out.println("Closing servers.");
        manager.close();
        System.out.println("Servers closed.");
    }
}
