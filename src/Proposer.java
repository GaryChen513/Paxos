import java.io.*;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.HashSet;


public class Proposer {
    private String name;
    public int max_id = 1;
    private String accepted_val;
    private int port;
    private boolean isResponseStable;
    private ServerSocket server;
    private HashSet<Integer> received_promise;
    private HashMap<String, Integer> mapping;
    private boolean hasRes = false;
    private boolean isAlive;

//    public long startTime;
    public long endTime;

    public Proposer (String name, int port) {
        this.name = name;
        this.accepted_val = name;
        this.port = port;
        this.received_promise = new HashSet<>();
        this.mapping = new HashMap<>();
        this.isResponseStable = true;
        settingConfig();

        endTime = -1;
    }

    public static void main(String[] args ) throws IOException{
        Proposer proposer1 = new Proposer("M1", 2011);
        Proposer proposer2 = new Proposer("M2", 2012);
        Proposer proposer3 = new Proposer("M3", 2013);

        new Thread() {
            @Override
            public void run() {
                try {
                    proposer1.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
        new Thread() {
            @Override
            public void run() {
                try {
                    proposer2.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
        new Thread() {
            @Override
            public void run() {
                try {
                    proposer3.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
        proposer1.max_id = 14;

        proposer1.sendingProposal();
        proposer1.setIsResponseStable(false);

        proposer2.sendingProposal();
        proposer3.max_id = 10;
        proposer3.sendingProposal();
        proposer3.max_id = 19;
        proposer3.sendingProposal();
        proposer3.end();
    }

    public void settingConfig() {
        for (int i = 4; i <= 9; i++) {
            mapping.put("M" + i, 2010 + i);
        }
    }

    /*
    start server;
    server socket will be closed if this.proposer.end() is called
     */
    public void start() throws IOException{
        isAlive = true;
        server = new ServerSocket(this.port);
        while (true) {
            try {
                Socket client = server.accept();
                handleRequest(client);
            } catch (SocketException ex) {
//                ex.printStackTrace();
                break;
            }
        }
    }

    /*
    Close server socket
     */
    public void end() throws IOException {
        if (server != null) server.close();
        isAlive = false;
    }


    /*
    send proposal to other nodes in the cluster
     */
    public void sendingProposal() throws IOException{

        for (String key : mapping.keySet()) {
            if (key.equals(this.name) ) continue;
            int port = mapping.get(key);
            Socket proposer = new Socket("127.0.0.1", port);
            Packet packet = new Packet(this.port, "PREPARE", this.max_id);

            DataOutputStream data_out = new DataOutputStream( proposer.getOutputStream());
            ObjectOutputStream object_out = new ObjectOutputStream(data_out);

            object_out.writeObject(packet);
        }
    }

    public void sendPacket(int port, Packet packet) throws IOException {
        if (hasRes) return;
        try {
            // if this node is not stable, wait in random seconds
            if (!this.isResponseStable) {
                int sleepTime = (int) (Math.random() * 5);
                Thread.sleep(sleepTime * 500);
            }

            Socket client = new Socket("127.0.0.1", port);
            DataOutputStream data_out = new DataOutputStream(client.getOutputStream());
            ObjectOutputStream object_out = new ObjectOutputStream(data_out);

            object_out.writeObject(packet);
            object_out.flush();
            client.close();
        } catch (ConnectException | InterruptedException ex) {
            ex.printStackTrace();
        }

    }

    /*
    Handling PROMISE
    Once meet majority consensus, broadcast PROPOSE to other nodes
     */
    public void handlePromise(Packet p) throws IOException {
        addToReceived_promise(p.port);
        setMax_id(p.id);
        if (p.val != null) {
            setAccepted_val(p.val);
        }

        if (this.received_promise.size() >= 9 / 2 + 1) {
            Packet packet = new Packet(this.port, "PROPOSE", this.max_id, this.accepted_val);
            System.out.println("A potential consensus id is found as " + this.max_id);
            for (String key : mapping.keySet()) {
                if (key.equals(this.name) ) continue;
                sendPacket(mapping.get(key), packet);
            }
        }
    }

    /*
    Handling ACCEPT
    If receiving ACCEPT, write voting result to local txt file
     */
    public void handleAccept(Packet p) throws IOException {
        setMax_id(p.id);
        setAccepted_val(p.val);
        if (endTime == -1) endTime = System.currentTimeMillis();

        hasRes = true;
        System.out.println(this.name + " received ACCEPT with id " + p.id);
        String path = "./src/" + this.name + ".txt";
        writeResult(path, this.accepted_val);
    }

    /*
    Handling two request; PROMISE and ACCEPT
     */
    public void handleRequest(Socket socket)  {
        if (!isAlive) return;
        DataInputStream data_in;
        ObjectInputStream object_in;

        try {
            data_in =  new DataInputStream( socket.getInputStream());
            object_in = new ObjectInputStream(data_in);

            Packet packet_received = (Packet) object_in.readObject();

            switch (packet_received.type) {
                case "PROMISE":
                    handlePromise(packet_received);
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
            String output = this.name + " voted " + res + " at " + endTime;
            byte[] strToBytes = output.getBytes();
            fout.write(strToBytes);
            fout.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setIsResponseStable (boolean bool) {this.isResponseStable = bool;}

    public synchronized void addToReceived_promise(int port) {
        this.received_promise.add(port);
    }

    public synchronized void setMax_id(int id) {
        this.max_id = Math.max(id, this.max_id);
    }

    public synchronized void setAccepted_val(String val) {
        this.accepted_val = val;
    }
}