import java.io.Serializable;

public class Packet implements Serializable{
    public int port;
    public String type;
    public int id;
    public String val;

    public Packet(int port, String type, int id) {
        this.port = port;
        this.type = type;
        this.id = id;
        this.val = null;
    }

    public Packet(int port, String type, int id, String val) {
        this.port = port;
        this.type = type;
        this.id = id;
        this.val = val;
    }


}
