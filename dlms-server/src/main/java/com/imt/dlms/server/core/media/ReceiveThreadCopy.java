package com.imt.dlms.server.core.media;

import java.lang.reflect.Array;

import gurux.common.GXSynchronousMediaBase;
import gurux.common.ReceiveEventArgs;
import gurux.common.enums.TraceLevel;
import gurux.common.enums.TraceTypes;
import gurux.io.NativeCode;

/**
 * Copy of gurux.serial.GXReceiveThread
 *
 * Receive thread listens serial port and sends received data to the listeners.
 * Copy was made to extend access visibility permissions.
 * @author Gurux Ltd.
 */
public class ReceiveThreadCopy extends Thread {

    /**
     * If receiver buffer is empty how long is waited for new data.
     */
    static final int WAIT_TIME = 200;

    /**
     * Serial port handle.
     */
    private long comPort;
    /**
     * Parent component where notifies are send.
     */
    private final SerialCopy parentMedia;

    /**
     * Amount of bytes received.
     */
    private long bytesReceived = 0;

    /**
     * Constructor.
     * 
     * @param parent
     *            Parent component.
     * @param hComPort
     *            Handle for the serial port.
     */
    ReceiveThreadCopy(final SerialCopy parent, final long hComPort) {
        super("GXSerial " + String.valueOf(hComPort));
        comPort = hComPort;
        parentMedia = parent;
    }

    /**
     * Get amount of received bytes.
     * 
     * @return Amount of received bytes.
     */
    public final long getBytesReceived() {
        return bytesReceived;
    }

    /**
     * Reset amount of received bytes.
     */
    public final void resetBytesReceived() {
        bytesReceived = 0;
    }

    /**
     * Handle received data.
     * 
     * @param buffer
     *            Received data from the serial port.
     */
    private void handleReceivedData(final byte[] buffer) {
        int len = buffer.length;
        if (len == 0) {
            try {
                Thread.sleep(WAIT_TIME);
            } catch (Exception ex) {
                return;
            }
            return;
        }
        bytesReceived += len;
        int totalCount = 0;
        synchronized (parentMedia.getSyncBase().getSync()) {
            parentMedia.getSyncBase().appendData(buffer, 0, len);
            // Search End of Packet if given.
            if (parentMedia.getEop() != null) {
                if (parentMedia.getEop() instanceof Array) {
                    for (Object eop : (Object[]) parentMedia.getEop()) {
                        totalCount = GXSynchronousMediaBase.indexOf(buffer,
                                GXSynchronousMediaBase.getAsByteArray(eop), 0,
                                len);
                        if (totalCount != -1) {
                            break;
                        }
                    }
                } else {
                    totalCount = GXSynchronousMediaBase.indexOf(buffer,
                            GXSynchronousMediaBase.getAsByteArray(
                                    parentMedia.getEop()),
                            0, len);
                }
            }
        }

        if (parentMedia.getIsSynchronous()) {
            gurux.common.TraceEventArgs arg = null;
            synchronized (parentMedia.getSyncBase().getSync()) {
                if (totalCount != -1) {
                    if (parentMedia.getTrace() == TraceLevel.VERBOSE) {
                        arg = new gurux.common.TraceEventArgs(
                                TraceTypes.RECEIVED, buffer, 0, totalCount + 1);
                    }
                    parentMedia.getSyncBase().setReceived();
                }
            }
            if (arg != null) {
                parentMedia.notifyTrace(arg);
            }
        } else if (totalCount != -1) {
            byte[] data = parentMedia.getSyncBase().getReceivedData();
            parentMedia.getSyncBase().resetReceivedSize();
            if (parentMedia.getTrace() == TraceLevel.VERBOSE) {
                parentMedia.notifyTrace(new gurux.common.TraceEventArgs(
                        TraceTypes.RECEIVED, data));
            }
            ReceiveEventArgs e =
                    new ReceiveEventArgs(data, parentMedia.getPortName());
            parentMedia.notifyReceived(e);
        }
    }

    @Override
    public final void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                byte[] buff = NativeCode.read(this.comPort,
                        parentMedia.getReadTimeout(), parentMedia.getClosing());
                // If connection is closed.
                if (buff.length == 0
                        && Thread.currentThread().isInterrupted()) {
                    parentMedia.setClosing(0);
                    break;
                }
                Thread.sleep(parentMedia.getReceiveDelay());
                byte[] buff2 = null;
                try {
                    if (NativeCode.getBytesToRead(this.comPort) != 0) {
                        buff2 = NativeCode.read(this.comPort, 1,
                                parentMedia.getClosing());
                    }
                } catch (Exception ex) {
                    // getBytesToRead fails with some chipsets.
                    // Just ignore it.
                }
                if (buff2 != null && buff2.length != 0) {
                    byte[] tmp = new byte[buff.length + buff2.length];
                    System.arraycopy(buff, 0, tmp, 0, buff.length);
                    System.arraycopy(buff2, 0, tmp, buff.length, buff2.length);
                    handleReceivedData(tmp);
                } else {
                    handleReceivedData(buff);
                }

            } catch (Exception ex) {
                if (!Thread.currentThread().isInterrupted()) {
                    parentMedia
                            .notifyError(new RuntimeException(ex.getMessage()));
                } else {
                    break;
                }
            }
        }
    }
}