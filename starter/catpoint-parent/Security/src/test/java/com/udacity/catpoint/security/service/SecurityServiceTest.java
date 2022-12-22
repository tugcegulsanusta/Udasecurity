package com.udacity.catpoint.security.service;

import com.udacity.catpoint.image.service.FakeImageService;
import com.udacity.catpoint.image.service.ImageServiceInterface;
import com.udacity.catpoint.security.application.StatusListener;
import com.udacity.catpoint.security.data.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.utils.Pair;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SecurityServiceTest {


    private Sensor sensor;
    private final String randomString = UUID.randomUUID().toString();
    private SecurityService securityService;
    @Mock
    private ImageServiceInterface imageServiceInterface;
    //@Mock
    //private StatusListener statusListener;
    @Mock
    public SecurityRepository securityRepository;

    private Set<Sensor>  mockFiveSensor (boolean isActive ) {
        Set<Sensor> sensors = IntStream.range(1, 5).mapToObj(i -> {
            Sensor sensor = new Sensor("mockSensor_" + i, SensorType.DOOR);
            sensor.setActive(isActive);
            return sensor;
        }).collect(Collectors.toSet());
        return sensors;
    };

    // End Of Custom Impl.

    private Sensor getSensor(){
        return new Sensor(randomString, SensorType.DOOR);
    }
    @BeforeEach
    void setUp(){
        securityService = new SecurityService(securityRepository, imageServiceInterface);
        sensor = getSensor();
    }

    @Test //Test1:If alarm is armed and a sensor becomes activated, put the system into pending alarm status.
    void ifAlarmIsArmed_SenSorBecomesActivated__PutTheSystemIntoPendingAlarmStatus(){
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
         when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        securityService.changeSensorActivationStatus(sensor,true);
        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }

    @Test //Test2:If alarm is armed and a sensor becomes activated and the system is already pending alarm, set the alarm status to alarm.
    void ifAlarmIsArmed_SensorBecomesActivated_SystemIsAlreadyPendingAlarm__SetTheAlarmStatusToAlarmOn() {
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
         when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        securityService.changeSensorActivationStatus(sensor, true);
        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test //Test3:If pending alarm and all sensors are inactive, return to no alarm state.
    void ifPendingAlarm_AllSensorsAreInactive__ReturnToNoAlarmState(){
         when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        securityService.changeSensorActivationStatus(sensor,false);
        verify(securityRepository,times(1)).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @ParameterizedTest //Test 4:If alarm is active, change in sensor state should not affect the alarm state.
    @ValueSource(booleans = {true, false})
    void ifAlarmIsActive_ChangeinSensorStateShouldNotAffectAlarmState(boolean currentStatus){
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        securityService.changeSensorActivationStatus(sensor, currentStatus);
        verify(securityRepository, times(0)).setAlarmStatus(any(AlarmStatus.class));
    }

    @Test //Test 5:If a sensor is activated while already active and the system is in pending state, change it to alarm state.
    void ifASensorIsActivated_WhileAlreadyActive_AndTheSystemIsInPendingState__ChangeItToAlarmState(){
         when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        securityService.changeSensorActivationStatus(sensor,true);
        verify(securityRepository,atMostOnce()).setAlarmStatus(AlarmStatus.ALARM);
    }

    @ParameterizedTest //Test 6:If a sensor is deactivated while already inactive, make no changes to the alarm state.
    @EnumSource(value = AlarmStatus.class, names = {"NO_ALARM", "PENDING_ALARM", "ALARM"})
    void ifASensorIsDeactivated_WhileAlreadyInactive__MakeNoChangesToTheAlarmState(AlarmStatus status){
        when(securityRepository.getAlarmStatus()).thenReturn(status);
        securityService.changeSensorActivationStatus(sensor,false);
        verify(securityRepository, times(0)).setAlarmStatus(any(AlarmStatus.class));
    }

    @Test //Test 7:If the image service identifies an image containing a cat while the system is armed-home, put the system into alarm status.
    void ifTheImageServiceIdentifiesAcat_WhileTheSystemIsArmed__PutTheSystemIntoAlarmStatus(){
        BufferedImage catImage = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME );
        when(imageServiceInterface.imageContainsCat(any(), ArgumentMatchers.anyFloat())).thenReturn(true);

        securityService.processImage(catImage);
        verify(securityRepository,times(1)).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test //Test 8:If the image service identifies an image that does not contain a cat, change the status to no alarm as long as the sensors are not active.
    void ifTheImageServiceIdentifiesAnImageNotContainCat_AsLongAsSensorsAraInactive__ChangeTheStatusToNoAlarm(){
        Set<Sensor> fiveSensors =  mockFiveSensor(false);
        when(securityService.getSensors()).thenReturn( fiveSensors );
        when(imageServiceInterface.imageContainsCat(any(), ArgumentMatchers.anyFloat())).thenReturn(false);

        securityService.processImage(mock(BufferedImage.class));

        verify(securityRepository,times(1)).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @Test //Test 9: If the system is disarmed, set the status to no alarm.
    void ifTheSystemisDisarmed__SetTheStatusToNoAlarm(){
        securityService.setArmingStatus(ArmingStatus.DISARMED);
        verify(securityRepository,times(1)).setAlarmStatus(AlarmStatus.NO_ALARM);
    }
    @ParameterizedTest //Test 10: If the system is armed, reset all sensors to inactive.
    @EnumSource(value = ArmingStatus.class, names = {"ARMED_HOME", "ARMED_AWAY"})
    void ifTheSystemIsArmed__ResetAllSensorsToInactive(ArmingStatus status){
        when(securityService.getSensors()).thenReturn( mockFiveSensor(true) );
         when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        securityService.setArmingStatus(status);
        securityService.getSensors().stream().map(Sensor::getActive).forEach(Assertions::assertFalse);
    }
    @Test //Test 11: If the system is armed-home while the camera shows a cat, set the alarm status to alarm.
    void ifTheSystemIsArmedHome_WhileTheCameraShowsACat__SetTheAlarmStatusNoAlarm(){
        BufferedImage catImage = new BufferedImage(256,256,BufferedImage.TYPE_INT_RGB);
        when(imageServiceInterface.imageContainsCat(any(), ArgumentMatchers.anyFloat())).thenReturn(true);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.DISARMED);

        securityService.processImage(catImage);
        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);

        verify(securityRepository,times(1)).setAlarmStatus(AlarmStatus.ALARM);
    }




}
