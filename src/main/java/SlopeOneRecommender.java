public class SlopeOneRecommender {
    DataSource dataSRC;
    SlopeOneMatrix soMatrix;

    public SlopeOneRecommender(DataSource dataSRC, SlopeOneMatrix soMatrix) {
        this.dataSRC = dataSRC;
        this.soMatrix = soMatrix;

    }

    /*
     * Predicts one item i for the user u using the Slope One algorithm.
     */
    public double recommendOne(int u, int i) {
        double difference = 0.0, userRatingSum = 0.0, prediction = 0.0;
        int numRatings = 0;

        // For every item j that user u has rated
        for (int j = 1; j <= dataSRC.getNumItems(); j++) {
            if (dataSRC.getRatings().get(j).get(u) != null && i != j) {
                difference += soMatrix.getItemPairAverageDiff(j, i);
                userRatingSum += dataSRC.getRatings().get(j).get(u);
                // calculate the number of ratings u has rated
                numRatings++;
            }

        }

        // calculate the prediction
        prediction = (double) ((userRatingSum + difference) / numRatings);

        return prediction;
    }

}