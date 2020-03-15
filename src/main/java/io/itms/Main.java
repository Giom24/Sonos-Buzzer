package io.itms;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.kilianB.sonos.SonosDiscovery;
import com.github.kilianB.sonos.model.SonosZoneInfo;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.github.kilianB.exception.SonosControllerException;
import com.github.kilianB.sonos.SonosDevice;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;

/**
 * Main class.
 *
 */
public class Main {

    private static List<SonosDevice> devices;
    private static final String interfaceName = "wlan0";
    private static final String room = "Workspace";

    /**
     * Main method.
     * 
     * @param args
     * @throws IOException
     * @throws SonosControllerException
     */
    public static void main(String[] args) throws IOException, SonosControllerException {
        final HttpServer server = startServer();
        final GpioController gpio = GpioFactory.getInstance();

        String ip = getExternalIpOfInterface(interfaceName).orElseThrow().toString();
        String uri = String.format("http:/%s:8080/clips/clip.mp3", ip);

        Buzzer buzzer = new Buzzer(gpio, room, uri);

        while (true) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {

                e.printStackTrace();
            }
        }

        // server.shutdownNow();
        // gpio.shutdown();
    }

    private static Optional<InetAddress> getExternalIpOfInterface(String interfaceName) throws SocketException {
        NetworkInterface networkInterface = NetworkInterface.getByName(interfaceName);
        return Collections.list(networkInterface.getInetAddresses()).stream()
                .filter(address -> address instanceof Inet4Address && !address.isLoopbackAddress()).findFirst();
    }

    // TODO Error Handling. Log Error only in Logs
    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this
     * application.
     * 
     * @return Grizzly HTTP server.
     * @throws IOExceptionimport java.util.logging.Level;
     */
    private static HttpServer startServer() throws IOException {
        ServerConfig serverConfig = new ServerConfig();
        return GrizzlyHttpServerFactory.createHttpServer(ServerConfig.BASE_URI, serverConfig);
    }

}
