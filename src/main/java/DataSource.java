import java.sql.*;
import java.util.*;

/**
 * Data source is represented as a repository of information about users, items
 * and the users preference (rating) for the items. Fetches and writes
 * information with query to a SQL-database.
 */
public class DataSource {
    private Connection conn;
    private Statement statement;
    private DBConnect dbconnect;
    private ResultSet resultSet;
    private int numItems, numUsers, getUserItemRating;
    int[] items, users;
    private HashMap<Integer, HashMap<Integer, Integer>> ratings;


    public DataSource() {
        dbconnect = new DBConnect();
        conn = dbconnect.getConnection();
        resultSet = null;
        numItems = -1;
        numUsers = -1;
        getUserItemRating = -1;
        items = null;
        ratings = null;
        try {
            statement = conn.createStatement();
            //clearRatings();
            //clearMovies();
            //if (getNumItems() == 0)
            //    fillDB();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void fillDB() {
        CSVParser csv = new CSVParser();
        csv.parse(this, "D://ICCDH/MoviesRecommenderSystem/Data/movies.csv", true);
        csv.parse(this, "D://ICCDH/MoviesRecommenderSystem/Data/ratings.csv", false);
    }

    public void addMovie(int id, String title) {
        try {
            String sql = "INSERT INTO movies VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            stmt.setString(2, title);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("id: " + id);
            e.printStackTrace();
        }
    }

    public void addRating(int userId, int movieId, double rating) {
        try {
            String sql = "INSERT INTO ratings VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, movieId);
            stmt.setDouble(3, rating);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("userId: " + userId + " movieId: " + movieId + " rating: " + rating);
            e.printStackTrace();
        }
    }

    private void clearMovies() {
        try {
            statement.executeUpdate("DELETE FROM movies");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void clearRatings() {
        try {
            statement.executeUpdate("DELETE FROM ratings");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Get the total number of users
    public int getNumUsers() {
        if (numUsers == -1) {
            try {
                resultSet = statement.executeQuery("SELECT COUNT(*) FROM ratings group by userId");

                if (resultSet.next()) {
                    numUsers = resultSet.getInt(1);
                }
                resultSet.close();

            } catch (SQLException e) {
                e.printStackTrace();
            }

        }
        return numUsers;
    }

    // Get the total number of items
    public int getNumItems() {
        if (numItems == -1) {
            try {
                resultSet = statement.executeQuery("SELECT COUNT(*) FROM movies");

                if (resultSet.next()) {
                    numItems = resultSet.getInt(1);
                }

                resultSet.close();

            } catch (SQLException e) {
            }
        }
        return numItems;
    }

    // Get the set of items
    public int[] getItems() {
        if (items == null) {
            try {
                resultSet = statement.executeQuery("SELECT * FROM movies");
                items = new int[getNumItems()];

                // Fill in array with data
                int i = 0;
                while (resultSet.next()) {
                    items[i] = resultSet.getInt(1);
                    i++;
                }
                resultSet.close();
            } catch (SQLException e) {
            }
        }
        return items;
    }

    // Get the set of users
    public int[] getUsers() {
        if (users == null) {
            try {
                users = new int[getNumUsers()];

                resultSet = statement.executeQuery("SELECT userId FROM ratings group by userId");

                int i = 0;
                while (resultSet.next()) {
                    users[i] = resultSet.getInt(1);
                    i++;
                }
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return users;
    }

    /*
     * Get the rating for item i for user u, if NaN is returned, the rating is
     * non existent.
     */
    public double getRating(int u, int i) {
        try {
            String query = "SELECT rating FROM ratings " + "WHERE userId = "
                    + u + " " + "AND movieId = " + i;

            resultSet = statement.executeQuery(query);

            if (resultSet.next()) {
                getUserItemRating = resultSet.getInt(1);
                resultSet.close();
                return getUserItemRating;

            } else {
                resultSet.close();
                return Double.NaN;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Double.NaN;
    }

    /*
     * Get ratings represented in a nestled HashMap: HashMap{[item_id],
     * HashMap{[user_id],[rating]} }
     */
    public HashMap<Integer, HashMap<Integer, Integer>> getRatings() {

        if (ratings == null) {
            try {
                // --quick in MySQL
                statement.setFetchSize(Integer.MIN_VALUE);
                resultSet = statement.executeQuery("SELECT * FROM ratings");
                ratings = new HashMap<Integer, HashMap<Integer, Integer>>();

                Integer item, user, rating;

                HashMap<Integer, Integer> innerHashMap = null;
                while (resultSet.next()) {

                    item = resultSet.getInt(2);
                    user = resultSet.getInt(1);
                    rating = resultSet.getInt(3);

                    innerHashMap = ratings.get(item);

                    if (innerHashMap == null) {
                        innerHashMap = new HashMap<Integer, Integer>();
                    }

                    innerHashMap.put(user, rating);
                    ratings.put(item, innerHashMap);

                }
                resultSet.close();
            } catch (SQLException e) {
            }


        }
        return ratings;
    }
}