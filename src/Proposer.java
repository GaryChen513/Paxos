import java.io.*;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.HashSet;


public class Proposer {
    public String name;
    public int max_id = 1;
    private String accepted_val;
    public int port;
    private boolean isResponseStable;
    private ServerSocket server;
    private HashSet<Integer> received_promise;
    private HashMap<String, Integer> mapping;
    private boolean hasRes = false;
    private boolean isAlive;
    private int messageCnt = 0;
    private int total_num_nodes = 9;

    private Acceptor[] acceptors = new Acceptor[] {};
    private Proposer[] proposers = new Proposer[] {};

//    public long startTime;
    public long endTime;

    public Proposer (String name, int port) {
        this.name = name;
        this.accepted_val = name;
        this.port = port;
        this.received_promise = new HashSet<>();
        this.mapping = new HashMap<>();
        this.isResponseStable = true;

        endTime = -1;
    }

    public void settingConfig(Acceptor[] acceptors, Proposer[] proposers) {
        this.acceptors = acceptors;
        this.proposers = proposers;

        for (Acceptor a: acceptors) {
            mapping.put(a.name, a.port);
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
        System.out.println(this.name + " proposer socket is closed");
        isAlive = false;
    }


    /*
    send proposal to other nodes in the cluster
     */
    public void sendingProposal() throws IOException{
        if (hasRes || !isAlive) return;
        for (String key : mapping.keySet()) {
            if (key.equals(this.name) ) continue;
            int port = mapping.get(key);
            try {
                Socket proposer = new Socket("127.0.0.1", port);
                Packet packet = new Packet(this.port, "PREPARE", this.max_id);

                DataOutputStream data_out = new DataOutputStream( proposer.getOutputStream());
                ObjectOutputStream object_out = new ObjectOutputStream(data_out);

                object_out.writeObject(packet);
                messageCnt += 1;
            } catch (Exception ex) {
                continue;
            }
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
            messageCnt += 1;

            Socket client = new Socket("127.0.0.1", port);
            DataOutputStream data_out = new DataOutputStream(client.getOutputStream());
            ObjectOutputStream object_out = new ObjectOutputStream(data_out);

            object_out.writeObject(packet);
            object_out.flush();
            client.close();
        } catch (ConnectException | InterruptedException ex) {
//            ex.printStackTrace();
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

        if (this.received_promise.size() >= total_num_nodes / 2 ) {
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
            String messageCNT = messageCnt + " messages have been sent from " + this.name + "\n" ;
            String output = messageCNT + this.name + " voted " + res + " at " + endTime;
            byte[] strToBytes = output.getBytes();
            fout.write(strToBytes);
            fout.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setNumOfNodes(int number) {this.total_num_nodes = number;}

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
