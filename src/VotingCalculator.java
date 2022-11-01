import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class VotingCalculator {

    public static void main(String[] args) {
        VotingCalculator vc = new VotingCalculator();
        Scanner sc = new Scanner(System.in);
        System.out.println("Please input the number of proposer in the last voting: ");
        int proposerCnt = sc.nextInt();
        System.out.println("Please input the number of acceptors in the last voting: ");
        int acceptCnt = sc.nextInt();

        long[] MatricList = vc.calculate(proposerCnt, acceptCnt);
        if (MatricList != null)
            vc.writeResult("./src/VotingResult.txt", MatricList);
    }

    /*
    opt == 1 -> reading finished time
    opt == 2 -> reading number of message
     */
    public long readInfo(String path, int opt) {
        long res = -1;
        try {
            File myObj = new File(path);
            Scanner myReader = new Scanner(myObj);
            String data = "";
            while (myReader.hasNextLine()) {
                data += myReader.nextLine();
            }
            if (opt == 1)
                res = Long.parseLong(data.split("at ", 2)[1]);
            else
                res = Long.parseLong(data.split(" messages", 2)[0]);

            myReader.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return res;
    }


    public long[] calculate(int proposerCnt, int acceptorCnt) {
        long startTime = readInfo("./src/VotingStartTime.txt", 1);
        if (startTime == -1) return null;
        long res = -1;
        long messageCnt = 0;

        for (int i = 1; i <= proposerCnt; i++) {
            String path = "./src/M" + i + ".txt";
            if (Files.notExists(Paths.get(path))) continue;
            long endTime = readInfo(path, 1);
            if (endTime == -1) continue;
            res = Math.max(res, endTime - startTime);

            long m = readInfo(path, 2);
            messageCnt += m;
        }

        for (int i = 1; i <= acceptorCnt; i++) {
            String path = "./src/M" + (i + 10) + ".txt";
            if (Files.notExists(Paths.get(path))) continue;
            long endTime = readInfo(path, 1);
            if (endTime == -1) continue;
            res = Math.max(res, endTime - startTime);

            long m = readInfo(path, 2);
            messageCnt += m;
        }

        return new long[] {res, messageCnt};
    }

    public void writeResult(String path, long[] matrics) {
        FileOutputStream fout = null;

        try {
            fout = new FileOutputStream(path, false);
            String output = "The duration of voting in millisecond is  " + String.valueOf(matrics[0]) + "\n";
            output += "The amount of total message sent: " + String.valueOf(matrics[1]);
            byte[] strToBytes = output.getBytes();
            fout.write(strToBytes);
            fout.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
