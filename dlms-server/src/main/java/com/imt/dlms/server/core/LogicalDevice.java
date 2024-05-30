package com.imt.dlms.server.core;

import com.imt.dlms.server.ServerManager;
import com.imt.dlms.server.config.DlmsConfig;
import com.imt.dlms.server.service.notification.DLMSNotifyService;
import com.imt.dlms.server.service.DLMSUtil;
import com.imt.dlms.server.service.notification.PushListener;
import gurux.dlms.GXDLMSConnectionEventArgs;
import gurux.dlms.GXDLMSNotify;
import gurux.dlms.ValueEventArgs;
import gurux.dlms.enums.*;
import gurux.dlms.objects.*;
import gurux.dlms.secure.GXDLMSSecureServer2;

import java.util.Arrays;

// TODO: Add clock object +, support users -, set conformance +, add profile generic read +, mandjet model +, add high authentication (optional) -,
//  update permissions +, add None Authentication to supporting server -
public abstract class LogicalDevice extends GXDLMSSecureServer2 implements PushListener, Initializable {

    private final String logicalDeviceName;

    private final GXDLMSAssociationLogicalName aa;

    private final DLMSNotifyService notifyService;

    private final GXDLMSNotify notify;

    public LogicalDevice(String logicalDeviceName,
                         GXDLMSAssociationLogicalName ln,
                         GXDLMSTcpUdpSetup wrapper,
                         short sap,
                         DLMSNotifyService notify) {
        super(ln, wrapper);
        this.logicalDeviceName = logicalDeviceName;
        this.aa = ln;
        this.notifyService = notify;
        this.notify = new GXDLMSNotify(true,
                DLMSUtil.PUBLIC_CLIENT_SAP,
                sap,
                InterfaceType.WRAPPER);
        this.notify.getConformance().add(Conformance.GENERAL_BLOCK_TRANSFER);
        ln.setServerSAP(sap);
    }

    @Override
    public void init() {
        this.addLogicalDeviceName();
        this.initialize();
    }

    private void addLogicalDeviceName() {
        if (getItems().findByLN(ObjectType.DATA, "0.0.42.0.0.255") == null) {
            GXDLMSData d = new GXDLMSData("0.0.42.0.0.255");
            d.setValue(logicalDeviceName);
            // Set access right. Client can't change Device name.
            d.setAccess(2, AccessMode.READ);
            d.setDataType(2, DataType.OCTET_STRING);
            d.setUIDataType(2, DataType.STRING);
            getItems().add(d);
        }
    }

    @Override
    protected boolean isTarget(int serverAddress, int clientAddress) {
        // Only one connection per meter at the time is allowed.
        if (getAssignedAssociation() != null) {
            return false;
        }
        boolean ret = false;
        // Check HDLC station address if it's used.
        if (getInterfaceType() == InterfaceType.HDLC && getHdlc() != null
                && getHdlc().getDeviceAddress() != 0) {
            ret = getHdlc().getDeviceAddress() == serverAddress;
        }
        // Check server address using serial number.
        boolean broadcast = (serverAddress & 0x3FFF) == 0x3FFF
                || (serverAddress & 0x7F) == 0x7F;
        if (!(broadcast
                || (serverAddress & 0x3FFF) == DlmsConfig.SERIAL_NUMBER % 10000 + 1000)) {
            ret = this.getLogicalDeviceSAP() == serverAddress;
        }
        if (ret) {
            setAssignedAssociation(null);
            for (GXDLMSObject it : getItems().getObjects(ObjectType.ASSOCIATION_LOGICAL_NAME)) {
                GXDLMSAssociationLogicalName ln = (GXDLMSAssociationLogicalName) it;
                if (ln.getClientSAP() == clientAddress) {
                    setAssignedAssociation(ln);
                    break;
                }
            }
        }
        return ret;
    }

    @Override
    protected SourceDiagnostic onValidateAuthentication(Authentication authentication, byte[] password) throws Exception {
        return checkPassword(authentication, password);
    }

    private SourceDiagnostic checkPassword(final Authentication authentication,
                                   final byte[] password) {
        if (authentication == Authentication.LOW) {
            byte[] expected;
            if (getUseLogicalNameReferencing()) {
                GXDLMSAssociationLogicalName ln = getAssignedAssociation();
                expected = ln.getSecret();
            } else {
                return SourceDiagnostic.NOT_SUPPORTED;
            }
            if (Arrays.equals(expected, password)) {
                return SourceDiagnostic.NONE;
            }
            String actual = "";
            if (password != null) {
                actual = new String(password);
            }
            System.out.println("Password does not match. Actual: '" + actual
                    + "' Expected: '" + new String(expected) + "'");
            return SourceDiagnostic.AUTHENTICATION_FAILURE;
        }
        // Other authentication levels are check on phase two.
        return SourceDiagnostic.NONE;

    }

    @Override
    public void onPreGet(ValueEventArgs[] args) throws Exception {
        System.out.println("onPreGet");
    }

    @Override
    public void onPostGet(ValueEventArgs[] args) throws Exception {
        System.out.println("onPostGet");
    }

    @Override
    protected GXDLMSObject onFindObject(ObjectType objectType, int sn, String ln) throws Exception {
        if (objectType == ObjectType.ASSOCIATION_LOGICAL_NAME) {
            for (GXDLMSObject it : getItems().getObjects(ObjectType.ASSOCIATION_LOGICAL_NAME)) {
                final GXDLMSAssociationLogicalName a = (GXDLMSAssociationLogicalName) it;
                if (a.getClientSAP() == getSettings().getClientAddress()
                        && a.getAuthenticationMechanismName()
                        .getMechanismId() == getSettings()
                        .getAuthentication()
                        && (ln.compareTo(a.getLogicalName()) == 0
                        || ln.compareTo("0.0.40.0.0.255") == 0))
                    return it;
            }
        }
        return null;
    }

    @Override
    public void onPreRead(ValueEventArgs[] args) throws Exception {
        System.out.println("onPreRead");
    }

    @Override
    public void onPostRead(ValueEventArgs[] args) throws Exception {
        System.out.println("onPostRead");
    }

    @Override
    protected void onPreWrite(ValueEventArgs[] args) throws Exception {
        System.out.println("onPreWrite");
    }

    @Override
    protected void onPostWrite(ValueEventArgs[] args) throws Exception {
        System.out.println("onPostWrite");
    }

    @Override
    protected void onConnected(GXDLMSConnectionEventArgs connectionInfo) throws Exception {
        System.out.println("onConnected");
    }

    @Override
    protected void onInvalidConnection(GXDLMSConnectionEventArgs connectionInfo) throws Exception {
        System.out.println("onInvalidConnection");
    }

    @Override
    protected void onDisconnected(GXDLMSConnectionEventArgs connectionInfo) throws Exception {
        System.out.println("onDisconnected");
    }

    @Override
    protected AccessMode onGetAttributeAccess(ValueEventArgs arg) throws Exception {
        System.out.println("onGetAttributeAccess");
        System.out.println();
        return arg.getTarget().getAccess(arg.getIndex());
    }

    @Override
    protected MethodAccessMode onGetMethodAccess(ValueEventArgs arg) throws Exception {
        System.out.println("onGetMethodAccess");
        return MethodAccessMode.NO_ACCESS;
    }

    @Override
    protected void onPreAction(ValueEventArgs[] args) throws Exception {
        System.out.println("onPreAction");
    }

    @Override
    protected void onPostAction(ValueEventArgs[] args) throws Exception {
        System.out.println("onPostAction");
    }

    public String getLogicalDeviceName() {
        return logicalDeviceName;
    }

    public int getLogicalDeviceSAP() {
        return aa.getServerSAP();
    }

    public DLMSNotifyService getNotifyService() {
        return notifyService;
    }

    public GXDLMSNotify getNotify() {
        return notify;
    }
}
