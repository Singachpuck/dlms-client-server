package com.dochkas.mandjetDlmsWeb.service.dlms;

import com.dochkas.mandjetDlmsWeb.exception.MandjetException;
import com.dochkas.mandjetDlmsWeb.model.dto.MandjetDto;
import com.imt.dlms.client.service.DlmsClientService;
import gurux.common.IGXMedia;
import gurux.common.enums.TraceLevel;
import gurux.dlms.GXDLMSClient;
import gurux.dlms.enums.ObjectType;
import gurux.dlms.objects.GXDLMSData;
import gurux.dlms.objects.GXDLMSProfileGeneric;
import gurux.dlms.objects.GXDLMSRegister;
import gurux.net.GXNet;
import gurux.net.enums.NetworkType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class MandjetService implements InitializingBean, EnvironmentAware {

    private final Logger logger = LoggerFactory.getLogger(NotificationHandler.class);

    private Environment env;

    @Value("${dlms.port}")
    private int dlmsPort;

    @Value("${dlms.host}")
    private String dlmsHost;

    private IGXMedia media;

    @Autowired
    @Qualifier("dlmsManagementClient")
    private GXDLMSClient managementClient;

    @Autowired
    @Qualifier("dlmsSupportingClient")
    private GXDLMSClient supportingClient;

    private DlmsClientService managementService;

    private DlmsClientService supportingService;

    private boolean initialized = false;

    @Override
    public void afterPropertiesSet() throws Exception {
        final GXNet net = new GXNet(NetworkType.UDP, dlmsPort);
        net.setHostName(dlmsHost);
        net.setServer(false);
        net.setTrace(TraceLevel.VERBOSE);
        this.media = net;

        this.managementService = new DlmsClientService(managementClient, media);
        this.supportingService = new DlmsClientService(supportingClient, media);

//        try {
//            if (!media.isOpen()) {
//                media.open();
//            }
//
//            managementService.connect();
//            managementService.readAssociationView();
//
//            managementService.disconnect();
//
//            supportingService.connect();
//            supportingService.readAssociationView();
//            logger.trace(supportingClient.getObjects().toString());
//            supportingService.disconnect();
//
//            initialized = true;
//        } catch (Exception e) {
//            logger.error(e.getMessage());
//            initialized = false;
//        } finally {
//            media.close();
//        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.env = environment;
    }

    public MandjetDto getMandgetData() {
        try {
            final MandjetDto mandjetDto = new MandjetDto();
            mandjetDto.setSensorValues(this.getSensorValues());
            mandjetDto.setVoltage(this.getVoltage());
            mandjetDto.setBattery(this.getBatteryLevel());
            return mandjetDto;
        } catch (Exception e) {
            throw new MandjetException(e.getMessage(), e);
        }
    }

    public int[][] getSensorValues() throws Exception {
        if (!initialized) {
            throw new MandjetException("DLMS host is unreachable!");
        }

        try {
            if (!media.isOpen()) {
                media.open();
            }

            supportingService.connect();

            final GXDLMSProfileGeneric pg = ((GXDLMSProfileGeneric) supportingClient.getObjects()
                    .getObjects(ObjectType.PROFILE_GENERIC).get(0));
            supportingService.readObject(pg, 3);
            long entriesInUse = ((Number) supportingService.readObject(pg, 7)).longValue();
            final Object[] cells = supportingService.readRowsByEntry(pg, 0, (int) entriesInUse);

            int i = 0;
            final int[][] sensorValues = new int[cells.length][];
            for (Object rows : cells) {
                int j = 0;
                Object[] row = (Object[]) rows;
                sensorValues[i] = new int[row.length];
                for (Object cell : row) {
                    sensorValues[i][j] = ((Number) cell).intValue();
                    j++;
                }
                i++;
            }
            return sensorValues;
        } catch (Exception e) {
            throw new MandjetException(e.getMessage(), e);
        } finally {
            supportingService.close();
        }
    }

    public float getVoltage() throws Exception {
        if (!initialized) {
            throw new MandjetException("DLMS host is unreachable!");
        }

        try {
            if (!media.isOpen()) {
                media.open();
            }

            supportingService.connect();

            final GXDLMSRegister voltageRegister = ((GXDLMSRegister) supportingClient.getObjects()
                    .findByLN(ObjectType.REGISTER, "1.0.32.4.0.255"));

            supportingService.readObject(voltageRegister, 2);

            return ((Number) voltageRegister.getValue()).floatValue();
        } catch (Exception e) {
            throw new MandjetException(e.getMessage(), e);
        } finally {
            supportingService.close();
        }
    }

    public int getBatteryLevel() throws Exception {
        if (!initialized) {
            throw new MandjetException("DLMS host is unreachable!");
        }

        try {
            if (!media.isOpen()) {
                media.open();
            }

            managementService.connect();

            final GXDLMSData batteryData = ((GXDLMSData) managementClient.getObjects()
                    .findByLN(ObjectType.DATA, "0.0.96.1.10.255"));

            managementService.readObject(batteryData, 2);

            return ((Number) batteryData.getValue()).intValue();
        } catch (Exception e) {
            throw new MandjetException(e.getMessage(), e);
        } finally {
            managementService.close();
        }
    }
}
