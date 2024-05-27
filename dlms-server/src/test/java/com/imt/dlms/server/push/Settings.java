package com.imt.dlms.server.push;

import gurux.common.GXCmdParameter;
import gurux.common.GXCommon;
import gurux.common.enums.TraceLevel;
import gurux.dlms.enums.InterfaceType;

import java.util.List;

public class Settings {

    public TraceLevel trace = TraceLevel.INFO;
    public int port = 4060;
    public InterfaceType interfaceType = InterfaceType.WRAPPER;

    /**
     * Show help.
     */
    static void showHelp() {
        System.out.println(
                "Gurux DLMS push listener waits push messages from DLMS devices.");
        System.out.println(
                " -t\t[Error, Warning, Info, Verbose] Trace messages.");
        System.out.println(" -p\tUser push port. Default is 4060.");
        System.out
                .println(" -i\tUsed communication interface. Ex. -i WRAPPER.");
    }

    static int getParameters(String[] args, Settings settings) {
        List<GXCmdParameter> parameters =
                GXCommon.getParameters(args, "t:p:i:");
        for (GXCmdParameter it : parameters) {
            switch (it.getTag()) {
                case 't':
                    // Trace.
                    if ("Error".compareTo(it.getValue()) == 0)
                        settings.trace = TraceLevel.ERROR;
                    else if ("Warning".compareTo(it.getValue()) == 0)
                        settings.trace = TraceLevel.WARNING;
                    else if ("Info".compareTo(it.getValue()) == 0)
                        settings.trace = TraceLevel.INFO;
                    else if ("Verbose".compareTo(it.getValue()) == 0)
                        settings.trace = TraceLevel.VERBOSE;
                    else if ("Off".compareTo(it.getValue()) == 0)
                        settings.trace = TraceLevel.OFF;
                    else
                        throw new IllegalArgumentException(
                                "Invalid Authentication option '" + it.getValue()
                                        + "'. (Error, Warning, Info, Verbose, Off).");
                    break;
                case 'p':
                    // Port.
                    settings.port = Integer.parseInt(it.getValue());
                    break;
                case 'i':// Interface type.
                    if ("HDLC".equalsIgnoreCase(it.getValue()))
                        settings.interfaceType = InterfaceType.HDLC;
                    else if ("WRAPPER".equalsIgnoreCase(it.getValue()))
                        settings.interfaceType = InterfaceType.WRAPPER;
                    else if ("HdlcWithModeE".equalsIgnoreCase(it.getValue()))
                        settings.interfaceType = InterfaceType.HDLC_WITH_MODE_E;
                    else if ("Plc".equalsIgnoreCase(it.getValue()))
                        settings.interfaceType = InterfaceType.PLC;
                    else if ("PlcHdlc".equalsIgnoreCase(it.getValue()))
                        settings.interfaceType = InterfaceType.PLC_HDLC;
                    else if ("CoAP".equalsIgnoreCase(it.getValue()))
                        settings.interfaceType = InterfaceType.COAP;
                    else
                        throw new IllegalArgumentException(
                                "Invalid interface type option." + it.getValue()
                                        + " (HDLC, WRAPPER, HdlcWithModeE, Plc, PlcHdlc, CoAP)");
                    break;
                case '?':
                    switch (it.getTag()) {
                        case 'p':
                            throw new IllegalArgumentException(
                                    "Missing mandatory port option.");
                        case 't':
                            throw new IllegalArgumentException(
                                    "Missing mandatory trace option.\n");
                        case 'i':
                            throw new IllegalArgumentException(
                                    "Missing mandatory interface type option.");
                        default:
                            showHelp();
                            return 1;
                    }
                default:
                    showHelp();
                    return 1;
            }
        }
        return 0;
    }
}
