import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        CSVParser csv = new CSVParser();
        csv.parse("D://ICCDH/RecommenderSystemForMovies/Data/test.csv", true);
        DataSource dataSRC = new DataSource();
        SlopeOneMatrix avgDiff = new SlopeOneMatrix(dataSRC);
        SlopeOneRecommender slopeOne = new SlopeOneRecommender(dataSRC, avgDiff);
        RMSE rmse = new RMSE();
        double prediction = 0.0;
        double rating = 0.0;
        ArrayList<Double> predictions = new ArrayList<Double>();
        ArrayList<Double> ratings = new ArrayList<Double>();

        // Iterate all users
        for (int userID : dataSRC.getUsers()) {

            // Iterate all movies
            for (int i = 1; i <= dataSRC.getNumItems(); i++) {

                // Get a prediction
                prediction = slopeOne.recommendOne(userID, i);
                // Get the actual value
                rating = dataSRC.getRating(userID, i);

                // Rating and Prediction is NaN if rating does not exist
                // or if a user only has rated one movie
                if (!Double.isNaN(rating) && !Double.isNaN(prediction)) {
                    ratings.add(rating);
                    predictions.add(prediction);
                }
            }
        }
        System.out.println();
        System.out.println("RMSE: " + rmse.evaluate(ratings, predictions));
    }


//    public static void main(String[] args) {
//        Spark.get("/", new Route() {
//            public Object handle(Request request,
//                                 Response response) {
//                String todolist = "todo";
//                // XXX write your code here XXX
//                return "<html><body>"
//                        + "<form method=\"get\" action=\"./\">"
//                        + "<input type=\"text\" name=\"input\""
//                        + " placeholder=\"What do you want to do?\"><br>"
//                        + "<input type=\"submit\" value=\"Add item\">"
//                        + "</form><hr>"
//                        + (todolist == null ? "" : todolist)
//                        + "</body><html>";
//            }
//        });
//    }
}