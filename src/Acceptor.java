import java.io.*;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Acceptor {
    private String name;
    private int max_id = -1;
    private String accepted_val = null;
    private boolean proposal_accepted = false;
    private int port;
    private boolean isResponseStable;
    private boolean hasRes = false;
    private HashMap<String, Integer> mapping = new HashMap<>();
    private int messageCnt = 0;

    public long endTime;

    public Acceptor (String name, int port) {
        this.name = name;
        this.port = port;
        settingConfig();

        endTime = -1;
    }

    public void settingConfig() {
        for (int i = 1; i <= 9; i++) {
            mapping.put("M" + i, 2010 + i);
        }
    }

    public void start() throws IOException {
        ServerSocket server = new ServerSocket(this.port);

        while (true) {
            Socket client = server.accept();
            handleRequest(client);
        }
    }

    public void sendPacket(int port, Packet packet) throws IOException {
        if (hasRes) return;
        try {
            if (!this.isResponseStable) {
                int sleepTime = (int) (Math.random() * 5);
                Thread.sleep(sleepTime * 500);
            }
            messageCnt += 1;
            Socket client = new Socket("127.0.0.1", port);
            DataOutputStream data_out = new DataOutputStream(client.getOutputStream());
            ObjectOutputStream object_out = new ObjectOutputStream(data_out);

            object_out.writeObject(packet);
            object_out.flush();
            client.close();
        } catch (ConnectException | InterruptedException ex){
//            ex.printStackTrace();
        }

    }

    public void handlePrepare(Packet p) throws IOException {
        if (p.id <= this.max_id) return;

        setMax_id(p.id);
        Packet packet = null;
        if (!proposal_accepted) {
            packet = new Packet(this.port, "PROMISE", this.max_id);
        } else {
            packet = new Packet(this.port, "PROMISE", this.max_id, this.accepted_val);
        }

        sendPacket(p.port, packet);
    }

    public void handlePropose(Packet p) throws IOException {
        if (p.id != this.max_id) return;

        setProposal_accepted(true);
        setAccepted_val(p.val);
        setMax_id(p.id);
        Packet packet = new Packet(this.port, "ACCEPT", this.max_id, this.accepted_val);

//        endTime = System.currentTimeMillis();
        for (String key : mapping.keySet()) {
            if (key.equals(this.name) ) continue;
            sendPacket(mapping.get(key), packet);
        }
    }

    public void handleAccept(Packet p) throws IOException {
        setMax_id(Math.max(p.id, this.max_id));
        setAccepted_val(p.val);
        if (endTime == -1) endTime = System.currentTimeMillis();

        hasRes = true;
        String path = "./src/" + this.name + ".txt";
        writeResult(path, this.accepted_val);

    }

    public void handleRequest(Socket socket)  {

        DataInputStream data_in;
        ObjectInputStream object_in;

        try {
            data_in =  new DataInputStream( socket.getInputStream());
            object_in = new ObjectInputStream(data_in);

            Packet packet_received = (Packet) object_in.readObject();
            System.out.println(this.name + " received " + packet_received.type + " from " + packet_received.port + " with value: " + packet_received.val + " in ID of " + packet_received.id);
            switch (packet_received.type) {
                case "PREPARE":
                    handlePrepare(packet_received);
                    break;
                case "PROPOSE":
                    handlePropose(packet_received);
                    break;
                case "ACCEPT":
                    handleAccept(packet_received);
                    break;
                default:
                    break;
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public synchronized void writeResult(String path, String res) throws IOException{
        FileOutputStream fout = null;

        try {
            fout = new FileOutputStream(path, false);
            String messageCNT = messageCnt + " messages have been sent from " + this.name + "\n" ;
            String output = messageCNT + this.name + " voted " + res + " at " + endTime;;
            byte[] strToBytes = output.getBytes();
            fout.write(strToBytes);
            fout.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setIsResponseStable (boolean bool) {this.isResponseStable = bool;}

    public synchronized void setProposal_accepted(boolean bool) { this.proposal_accepted = bool;}

    public synchronized void setMax_id(int id) {
        this.max_id = id;
    }

    public synchronized void setAccepted_val(String val) {
        this.accepted_val = val;
    }
}
