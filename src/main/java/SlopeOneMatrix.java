import java.util.*;
import java.util.Map.*;

/**
 * The class SlopeOneMatrix is a repository for matrices used in Slope One.
 * itemAVGDiffMatrix is the rating differences between each pair of items.
 */
public class SlopeOneMatrix {
    private DataSource dataSRC;
    private HashMap<Integer, HashMap<Integer, Double>> itemAVGDiffMatrix;

    public SlopeOneMatrix(DataSource dataSRC) {
        this.dataSRC = dataSRC;
        itemAVGDiffMatrix = new HashMap<Integer, HashMap<Integer, Double>>();
        calcItemPairs();
    }

    private void calcItemPairs() {
        HashMap<Integer, Double> innerHashMapAVG = null;
        Integer ratingI = -1, ratingJ = -1, userI = -1, userJ = -1;

        int dev = 0;
        int sum = 0;
        int countSim = 0;
        Double average = 0.0;

        System.out.println("Now running: Calculate Item-Item Average Diff");

        // for all items, i
        for (int i = 1; i <= dataSRC.getNumItems(); i++) {
            // for all other item, j
            for (int j = 1; j <= i; j++) {
                // for every user u expressing preference for both i and j
                if((dataSRC.getRatings()).get(j)!=null) {
                    for (Entry<Integer, Integer> entry : (dataSRC.getRatings()).get(j).entrySet()) {
                        userJ = entry.getKey();
                        ratingJ = entry.getValue();

                        if (dataSRC.getRatings().get(i) != null && dataSRC.getRatings().get(i).containsKey(userJ)) {
                            if (i != j) {
                                userI = userJ;

                                ratingI = dataSRC.getRatings().get(i).get(userI);

                                dev = ratingJ - ratingI;
                                sum += dev;
                                countSim++;
                            }
                        }
                    }
                }

                if (i != j) {
                    // add the difference in u’s preference for i and j to an
                    // average
                    average = ((double) sum / (double) countSim);

                    innerHashMapAVG = itemAVGDiffMatrix.get(i);

                    if (innerHashMapAVG == null) {
                        innerHashMapAVG = new HashMap<Integer, Double>();
                    }
                }

                if (i != j) {
                    innerHashMapAVG.put(j, average);

                    // Put the deviation average in a matrix for the items
                    itemAVGDiffMatrix.put(i, innerHashMapAVG);

                    countSim = 0;
                    sum = 0;
                }
            }
        }
    }

    public double getItemPairAverageDiff(Integer i, Integer j) {
        HashMap<Integer, Double> outerHashMapI = itemAVGDiffMatrix.get(i);
        HashMap<Integer, Double> outerHashMapJ = itemAVGDiffMatrix.get(j);

        double avgDiff = 0.0;

        if (outerHashMapI != null && !outerHashMapI.isEmpty()
                && outerHashMapI.containsKey(j)) {
            // If itemI < itemJ return the item else return the negation
            if (i < j) {
                avgDiff = -outerHashMapI.get(j);
            } else {
                avgDiff = outerHashMapI.get(j);
            }
        } else if (outerHashMapJ != null && !outerHashMapJ.isEmpty()
                && outerHashMapJ.containsKey(i)) {
            if (i < j) {
                avgDiff = -outerHashMapJ.get(i);
            } else {
                avgDiff = outerHashMapJ.get(i);
            }
        }

        // If none of the cases applies above, the average difference is 0
        return avgDiff;
    }
}