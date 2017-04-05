import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class CSVParser {

    public void parse(String fileName, boolean isMovies) {
        BufferedReader br = null;
        String line;
        String cvsSplitBy = ",";
        try {
            br = new BufferedReader(new FileReader(fileName));
            while ((line = br.readLine()) != null) {
                if(isMovies) {
                    String[] parts = line.split(cvsSplitBy);
                    String movieId = parts[0];
                    String movieTitle = parts[1];
                    if (movieTitle.contains("\"")) {
                        String title = parts[1].concat(",".concat(parts[2]));
                        movieTitle = title.replaceAll("\"", "");
                    }
                    System.out.println(movieId + "\t" + movieTitle);
                } else{

                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
