package com.dochkas.jwtAuthScaffold.service.dlms;

import com.dochkas.jwtAuthScaffold.exception.MandjetException;
import com.dochkas.jwtAuthScaffold.model.dto.MandjetDto;
import com.imt.dlms.client.service.DlmsClientService;
import gurux.common.IGXMedia;
import gurux.common.enums.TraceLevel;
import gurux.dlms.enums.Authentication;
import gurux.dlms.enums.InterfaceType;
import gurux.dlms.enums.ObjectType;
import gurux.dlms.objects.GXDLMSData;
import gurux.dlms.objects.GXDLMSProfileGeneric;
import gurux.dlms.objects.GXDLMSRegister;
import gurux.dlms.secure.GXDLMSSecureClient;
import gurux.net.GXNet;
import gurux.net.enums.NetworkType;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class MandjetService implements InitializingBean, EnvironmentAware {

    private Environment env;

    @Value("${dlms.port}")
    private int dlmsPort;

    private IGXMedia media;

    private GXDLMSSecureClient managementClient;

    private GXDLMSSecureClient supportingClient;

    private DlmsClientService managementService;

    private DlmsClientService supportingService;

    @Override
    public void afterPropertiesSet() throws Exception {
        final int port = dlmsPort;
        final GXNet net = new GXNet(NetworkType.TCP, port);
        net.setServer(false);
        net.setTrace(TraceLevel.VERBOSE);
        this.media = net;

        this.managementClient = new GXDLMSSecureClient(true, 16,
                1, Authentication.NONE, null, InterfaceType.WRAPPER);

        this.supportingClient = new GXDLMSSecureClient(true, 16,
                3013, Authentication.LOW, env.getProperty("DLMS_PASSWORD"), InterfaceType.WRAPPER);

        this.managementService = new DlmsClientService(managementClient, media);
        this.supportingService = new DlmsClientService(supportingClient, media);

        try {
            if (!media.isOpen()) {
                media.open();
            }

            managementService.connect();
            managementService.readAssociationView();

            managementService.disconnect();

            supportingService.connect();
            supportingService.readAssociationView();
            System.out.println(supportingClient.getObjects());
            supportingService.disconnect();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        } finally {
            media.close();
        }
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
