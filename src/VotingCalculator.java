import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class VotingCalculator {
    public static void main(String[] args) {
        VotingCalculator vc = new VotingCalculator();
        long res = vc.calculate();
        vc.writeResult("./src/VotingDuration.txt", res);
    }

    public long readTime(String path) {
        long res = -1;
        try {
            File myObj = new File(path);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                long t = Long.parseLong(data.split("at ", 2)[1]);
                res = t;
            }
            myReader.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return res;
    }

    public long calculate() {
        long startTime = readTime("./src/VotingStartTime.txt");
        if (startTime == -1) return -1;
        long res = -1;

        for (int i = 1; i <= 9; i++) {
            String path = "./src/M" + i + ".txt";
            if (Files.notExists(Paths.get(path))) continue;
            long endTime = readTime(path);
            if (endTime == -1) continue;
            res = Math.max(res, endTime - startTime);
        }

        return res;
    }

    public void writeResult(String path, long res) {
        FileOutputStream fout = null;

        try {
            fout = new FileOutputStream(path, false);
            String output = "This voting takes " + String.valueOf(res);
            byte[] strToBytes = output.getBytes();
            fout.write(strToBytes);
            fout.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
