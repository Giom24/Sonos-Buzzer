package io.itms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.kilianB.exception.SonosControllerException;
import com.github.kilianB.sonos.SonosDevice;
import com.github.kilianB.sonos.SonosDiscovery;
import com.github.kilianB.sonos.listener.SonosEventAdapter;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

public class Buzzer {

    private static final int DISCOVER_TIME = 5;
    private static final int VOLUME_OFFSET = 70;
    private static final int VOLUME_CAP = 85;
    private final String uri;
    private final GpioController gpio;
    private final GpioPinDigitalInput buzzer;
    private final List<SonosDevice> devices;
    private Date date;
    private static long lastTime;

    public Buzzer(GpioController gpio, String room, String uri) throws IOException, SonosControllerException {

        this.uri = uri;
        this.gpio = gpio;
        this.devices = this.getDeviceFromRoom(room);
        this.lastTime = 0;

        this.buzzer = gpio.provisionDigitalInputPin(RaspiPin.GPIO_02, PinPullResistance.PULL_DOWN);
        this.buzzer.setShutdownOptions(true);
        this.buzzer.addListener(new GpioPinListenerDigital() {
            @Override
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {

                if (event.getState() == PinState.HIGH) {
                    long currentTime = new Date().getTime();
                    if (currentTime - lastTime >= 25 * 1000) {
                        lastTime = currentTime;
                        System.out.println("Buzzer Triggered!");
                        try {

                            for (SonosDevice device : devices) {
                                if (device.isCoordinator()) {
                                    device.pause();
                                }
                            }
                            int volumes[] = new int[devices.size()];
                            for(int i = 0; i < devices.size(); i++){
                                SonosDevice device = devices.get(i);
                                volumes[i] = devices.get(i).getVolume();
                                int newVolume =  volumes[i] >= VOLUME_CAP - VOLUME_OFFSET ? VOLUME_CAP : volumes[i] + VOLUME_OFFSET;
                                System.out.println(String.format("Play on device with %d Volume", newVolume));
                                device.setVolume(newVolume);
                            }

                           
                            for (SonosDevice device : devices) {
                                if (device.isCoordinator()) {
                                    device.clip(uri, null);
                                }
                            }

                            for(int i = 0; i < devices.size(); i++){
                                SonosDevice device = devices.get(i);
                                device.setVolume(volumes[i]);
                            }

                            for (SonosDevice device : devices) {
                                if (device.isCoordinator()) {
                                    device.play();
                                }
                            }

                        } catch (IOException | SonosControllerException | InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
        });

        System.out.println(String.format("Buzzer Startet in \"%s\" for \"%s\"", room, uri));

    }

    private List<SonosDevice> getDeviceFromRoom(String room) throws IOException, SonosControllerException {
        System.out.println(String.format("Discovering Sonos Device \"%s\"...", room));
        Optional<SonosDevice> optDevice = SonosDiscovery.discover(DISCOVER_TIME).stream().filter(sonos -> {
            try {
                return sonos.getRoomName().equals(room);
            } catch (IOException | SonosControllerException e) {
                throw new RuntimeException(e);
            }
        }).findAny();

        if (!optDevice.isPresent()) {
            throw new RuntimeException(String.format("Device \"%s\" not found", room));
        }

        SonosDevice device = optDevice.get();

        var devices = new LinkedList<SonosDevice>();
        devices.addAll(device.joinedWith());
        System.out.println(String.format("%d Devices found", devices.size()));
        return devices;

    }

}