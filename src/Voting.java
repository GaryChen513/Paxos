import java.io.FileOutputStream;
import java.io.IOException;

public class Voting {
    public Proposer[] proposers;
    public Acceptor[] acceptors;
    public long startTime;
    public int maxID = -1;

    public Proposer proposer1 = new Proposer("M1", 2011);
    public Proposer proposer2 = new Proposer("M2", 2012);
    public Proposer proposer3 = new Proposer("M3", 2013);

    public Acceptor acceptor1 = new Acceptor("M4", 2014);
    public Acceptor acceptor2 = new Acceptor("M5", 2015);
    public Acceptor acceptor3 = new Acceptor("M6", 2016);
    public Acceptor acceptor4 = new Acceptor("M7", 2017);
    public Acceptor acceptor5 = new Acceptor("M8", 2018);
    public Acceptor acceptor6 = new Acceptor("M9", 2019);

    public static void main(String[] args) throws IOException, InterruptedException {
        Voting v = new Voting();
        v.test1();
    }

    public void test1() throws IOException {
        this.proposers = new Proposer[] {
                proposer1,
                proposer2,
                proposer3
        };

        this.acceptors = new Acceptor[]{
                acceptor1,
                acceptor2,
                acceptor3,
                acceptor4,
                acceptor5,
                acceptor6
        };

        startProposers();
        startAcceptor();
        setState();

        startTime = System.currentTimeMillis();
        writeVotingStartTime("./src/VotingStartTime.txt", this.startTime);
        proposer1.sendingProposal();
        proposer2.sendingProposal();
        proposer3.sendingProposal();

    }

    /*
    This is a failure test case
    Failure happens on proposer3 right after it sends out a higher id PREPARE
    Proposers will keeping out a new proposal with higher id in every 10 seconds
     */

    public void test2() throws IOException, InterruptedException {
        this.proposers = new Proposer[] {
                proposer1,
                proposer2,
                proposer3
        };

        this.acceptors = new Acceptor[]{
                acceptor1,
                acceptor2,
                acceptor3,
                acceptor4,
                acceptor5,
                acceptor6
        };

        startProposers();
        startAcceptor();
        setState();

        startTime = System.currentTimeMillis();
        writeVotingStartTime("./src/VotingStartTime.txt", this.startTime);
        for (Proposer p : this.proposers) {
            startProposer(p);
            maxID = Math.max(p.max_id, maxID);
        }
        Thread.sleep(5000);
        proposer3.end();
    }

    public void setState() {
        this.proposer1.setMax_id(1);
        this.proposer2.setMax_id(10);
        this.proposer3.setMax_id(20);

        proposer2.setIsResponseStable(false);
        proposer3.setIsResponseStable(false);

        acceptor1.setIsResponseStable(false);
        acceptor2.setIsResponseStable(false);
        acceptor3.setIsResponseStable(false);
        acceptor4.setIsResponseStable(false);
        acceptor5.setIsResponseStable(false);
        acceptor6.setIsResponseStable(false);
    }

    public void startProposer(Proposer p) {
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        if (maxID != p.max_id)  {
                            p.max_id += (int) (Math.random() * 5);
                            p.sendingProposal();
                        }
                        maxID = Math.max(p.max_id, maxID);
                        Thread.sleep(1000 * 10);

                    } catch (InterruptedException | IOException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }
        }.start();
    }

    public void startProposers() {
        for (Proposer p : proposers) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        p.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }

    public void startAcceptor() {
        for (Acceptor a : acceptors) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        a.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }

    public void writeVotingStartTime(String path, long res) {
        FileOutputStream fout = null;

        try {
            fout = new FileOutputStream(path, false);
            String output = "Voting starts at " + String.valueOf(res);
            byte[] strToBytes = output.getBytes();
            fout.write(strToBytes);
            fout.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
