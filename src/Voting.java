import java.io.File;
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
    public Proposer proposer4 = new Proposer("M4", 2014);
    public Proposer proposer5 = new Proposer("M5", 2015);
    public Proposer proposer6 = new Proposer("M6", 2016);
    public Proposer proposer7 = new Proposer("M7", 2017);
    public Proposer proposer8 = new Proposer("M8", 2018);
    public Proposer proposer9 = new Proposer("M9", 2019);
    public Proposer proposer10 = new Proposer("M9", 2020);

    public Acceptor acceptor1 = new Acceptor("M11", 2021);
    public Acceptor acceptor2 = new Acceptor("M12", 2022);
    public Acceptor acceptor3 = new Acceptor("M13", 2023);
    public Acceptor acceptor4 = new Acceptor("M14", 2024);
    public Acceptor acceptor5 = new Acceptor("M15", 2025);
    public Acceptor acceptor6 = new Acceptor("M16", 2026);
    public Acceptor acceptor7 = new Acceptor("M17", 2027);
    public Acceptor acceptor8 = new Acceptor("M18", 2028);
    public Acceptor acceptor9 = new Acceptor("M19", 2029);
    public Acceptor acceptor10 = new Acceptor("M20", 2030);

    public static void main(String[] args) throws IOException, InterruptedException {
        Voting v = new Voting();
        v.test3();
    }




    /*
    write tests below
    -------------------------------------------------------------------------------------
     */
    public void test1() throws IOException {
        deleteAllTxtFile();
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
        setVotingConfig();
        setNodeState();

        startProposers();
        startAcceptor();

        startTime = System.currentTimeMillis();
        writeVotingStartTime("./src/VotingStartTime.txt", this.startTime);
        for (Proposer p : proposers) {
            p.sendingProposal();
        }
    }

    /*
    This is a failure test case
    Failure happens on proposer3 right after it sends out a higher id PREPARE
    Proposers will keeping out a new proposal with higher id in every 10 seconds
     */
    public void test2() throws IOException, InterruptedException {
        deleteAllTxtFile();
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

        setVotingConfig();
        setNodeState();

        startProposers();
        startAcceptor();

        startTime = System.currentTimeMillis();
        writeVotingStartTime("./src/VotingStartTime.txt", this.startTime);
        for (Proposer p : this.proposers) {
            startProposer(p);
        }
        Thread.sleep(1000 * 2);
        proposer3.end();
    }

    public void test3() {
        deleteAllTxtFile();
        this.proposers = new Proposer[] {
                proposer1,
                proposer2,
                proposer3,
                proposer4,
                proposer5,
                proposer6
        };

        this.acceptors = new Acceptor[]{
                acceptor1,
                acceptor2,
                acceptor3,
                acceptor4,
                acceptor5,
                acceptor6,
                acceptor7,
                acceptor8,
                acceptor9,
                acceptor10
        };

        setVotingConfig();
        setNodeState();

        startProposers();
        startAcceptor();

        startTime = System.currentTimeMillis();
        writeVotingStartTime("./src/VotingStartTime.txt", this.startTime);
        for (Proposer p : this.proposers) {
            startProposer(p);
        }
    }

    /*
    -------------------------------------------------------------------------------------
    write test above
     */
    public void setVotingConfig () {
        for (Acceptor a: acceptors) {
            a.settingConfig(acceptors, proposers);
        }
        for (Proposer p: proposers) {
            p.settingConfig(acceptors, proposers);
            p.setNumOfNodes(acceptors.length + acceptors.length);
        }

    }

    public void setNodeState() {
        for (Proposer p : proposers) {
            p.setMax_id((int) (Math.random() * 20));
        }
        boolean isStable = (int) (Math.random() * 2) == 1;
        proposer1.setIsResponseStable(isStable);
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

    public void deleteAllTxtFile() {
        File folder = new File("./src");
        for (File file : folder.listFiles()) {
            String name = file.getName();
            if (name.endsWith(".txt")) file.delete();
        }
    }

}
