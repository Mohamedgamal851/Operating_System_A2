import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

class Semaphore {
    int value;

    public Semaphore(int value) {
        this.value = value;
    }

    public synchronized void wait(Device device) throws InterruptedException {
        value--;
        if (value < 0) {
            Network.writeToLogFile("- (" + device.name + ") (" + device.type + ")" + " arrived and waiting");
            wait();
        } else {
            Network.writeToLogFile("- (" + device.name + ") (" + device.type + ")" + " arrived");
        }

    }

    public synchronized void signal() {
        value++;
        if (value <= 0) {
            notify();
        }
    }
}

class Router {

    int maxNumberOfConnections;
    ArrayList<Integer> connectionsDevices;
    private Semaphore semaphore;

    public Router(int maxNumberOfConnections) {
        this.maxNumberOfConnections = maxNumberOfConnections;
        connectionsDevices = new ArrayList<Integer>();
        semaphore = new Semaphore(maxNumberOfConnections);
        for (int i = 0; i < maxNumberOfConnections; i++) {
            connectionsDevices.add(0);
        }
    }

    public int startConnection(Device d) throws InterruptedException {

        semaphore.wait(d);
        int connectionNumber = 0;
        for (int i = 0; i < maxNumberOfConnections; i++) {
            if (connectionsDevices.get(i) == 0) {
                connectionsDevices.set(i, 1);
                connectionNumber = i + 1;
                break;
            }
        }
        return connectionNumber;

    }

    public void endConnection(int connectionNumber) {
        connectionsDevices.set(connectionNumber - 1, 0);
        semaphore.signal();
    }

}

class Device extends Thread {

    public String name, type;
    public Router router;
    public int connectionNumber;

    public Device(String name, String type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public void run() {

        try {
            deviceConnect();
            performActivity();
            deviceDisconnect();
        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
        }

    }


    private void deviceConnect() throws InterruptedException {

        connectionNumber = router.startConnection(this);
    }

    private void performActivity() throws IOException {

        Network.writeToLogFile("- Connection " + connectionNumber + ": " + name + " occupied");
        Network.writeToLogFile("- Connection " + connectionNumber + ": " + name + " login");
        Network.writeToLogFile("- Connection " + connectionNumber + ": " + name + " performs online activity");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void deviceDisconnect() throws IOException {

        Network.writeToLogFile("- Connection " + connectionNumber + ": " + name + " Logged out ");
        router.endConnection(connectionNumber);
    }
}


class Network {
    public static void writeToLogFile(String message) {
        try (PrintStream out = new PrintStream(new FileOutputStream("output.txt", true))) {
            out.println(message);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        int maxNumberOfConnections;
        int maxNumberOfDevices;
        ArrayList<Device> devices = new ArrayList<Device>();
        Scanner input = new Scanner(System.in);
        System.out.println("What is number of WI-FI Connections?");
        maxNumberOfConnections = input.nextInt();
        System.out.println("What is number of devices Clients want to connect?");
        maxNumberOfDevices = input.nextInt();

        for (int i = 0; i < maxNumberOfDevices; i++) {
            String name = input.next();
            String type = input.next();
            devices.add(new Device(name, type));
        }

        Router router = new Router(maxNumberOfConnections);

        for (int i = 0; i < maxNumberOfDevices; i++) {
            devices.get(i).router = router;
            devices.get(i).start();
        }


    }
}