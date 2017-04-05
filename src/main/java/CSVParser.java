import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class CSVParser {

    public void parse(DataSource ds, String fileName, boolean isMovies) {
        BufferedReader br = null;
        String cvsSplitBy = ",";
        try {
            br = new BufferedReader(new FileReader(fileName));
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                if (isMovies) {
                    String[] parts = line.split(cvsSplitBy);
                    String movieTitle = parts[1];
                    if (movieTitle.contains("\"")) {
                        String title = parts[1].concat(",".concat(parts[2]));
                        movieTitle = title.replaceAll("\"", "");
                    }
                    ds.addMovie(Integer.parseInt(parts[0]), movieTitle);
                } else {
                    String[] parts = line.split(cvsSplitBy);
                    ds.addRating(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Double.parseDouble(parts[2]));
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
