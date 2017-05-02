import java.sql.*;

class SlopeOne {
    private Connection conn;

    SlopeOne() {
        conn = (new DBConnect()).getConnection();
    }

    void test() {
        clearMatrix();
        try {
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM rating");
            while (resultSet.next()) {
                int userId = resultSet.getInt(1);
                int itemId = resultSet.getInt(2);
                System.out.println("userId: " + userId + ", itemId: " + itemId);
                updateDevTable(userId, itemId);
            }
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void updateDevTable(int userId, int itemId) {
        try {
            //Користувач оцінив новий елемент, тому ми шукаємо різницю в оцініці миж ним, та кожним іншим елементом, який оцінив наш юзер
            String sql = "SELECT DISTINCT r.itemID, (r2.ratingValue - r.ratingValue) as ratingDifference FROM rating r, rating r2 " +
                    "WHERE r.userID=? AND r.itemID<>? AND r2.itemID=? AND r2.userID=?;";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, itemId);
            stmt.setInt(3, itemId);
            stmt.setInt(4, userId);
            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                int otherItemID = resultSet.getInt("itemID");
                int ratingDifference = resultSet.getInt("ratingDifference");
                //Якщо пара (itemID, otherItemID) вже є у матриці, то оновимо значення для count і sum
                int countOfPairs = 0;
                stmt = conn.prepareStatement("SELECT count(itemID1) FROM dev WHERE itemID1=? AND itemID2=?");
                stmt.setInt(1, itemId);
                stmt.setInt(2, otherItemID);
                ResultSet resultSet2 = stmt.executeQuery();
                if (resultSet2.next())
                    countOfPairs = resultSet2.getInt(1);
                resultSet2.close();
                if (countOfPairs > 0) {
                    String sql2 = "UPDATE dev SET count=count+1, sum=sum+? " +
                            "WHERE itemID1=? AND itemID2=?";
                    stmt = conn.prepareStatement(sql2);
                    stmt.setInt(1, ratingDifference);
                    stmt.setInt(2, itemId);
                    stmt.setInt(3, otherItemID);
                    stmt.executeUpdate();
                } else { //Якщо пари не було, то додаємо її
                    String sql2 = "INSERT INTO dev VALUES (?, ?, 1, ?)";
                    stmt = conn.prepareStatement(sql2);
                    stmt.setInt(1, itemId);
                    stmt.setInt(2, otherItemID);
                    stmt.setInt(3, ratingDifference);
                    stmt.executeUpdate();
                }
            }
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    double predict(int userId, int itemId) {
        try {
            double denom = 0.0; //знаменник
            double numer = 0.0; //чисельник
            String sql = "SELECT r.itemID, r.ratingValue FROM rating r WHERE r.userID=? AND r.itemID <> ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, itemId);
            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                int j = resultSet.getInt("itemID");
                int ratingValue = resultSet.getInt("ratingValue");
                //скільки разів користувачі оцінювали пару (itemId, j)
                stmt = conn.prepareStatement("SELECT d.count, d.sum FROM dev d WHERE itemID1=? AND itemID2=?");
                stmt.setInt(1, itemId);
                stmt.setInt(2, j);
                ResultSet resultSet2 = stmt.executeQuery();
                if (resultSet2.next()) {
                    int count = resultSet2.getInt("count");
                    double sum = resultSet2.getDouble("sum");
                    if (count > 0) {
                        double average = sum / count;
                        denom += count;
                        numer += count * (average + ratingValue);
                    }
                }
                resultSet2.close();
            }
            if (denom != 0)
                return (numer / denom);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    void predictBest(int userId, int n) {
        try {
            String sql = "SELECT d.itemID1 as item, " +
                    "sum(d.count*(d.sum/d.count+ r.ratingValue))/sum(d.count) as avgRat " +
                    "FROM  rating r, dev d " +
                    "WHERE r.userID=? " +
                    "AND d.itemID1 NOT IN (SELECT itemID FROM rating WHERE userID=?) " +
                    "AND d.itemID2=r.itemID " +
                    "GROUP BY d.itemID1 ORDER BY avgRat DESC LIMIT ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            stmt.setInt(3, n);
            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                int itemId = resultSet.getInt("item");
                double ratingValue = resultSet.getDouble("avgRat");
                System.out.println("itemId: " + itemId + ", ratingValue: " + ratingValue);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void clearRatings() {
        try {
            Statement statement = conn.createStatement();
            statement.executeUpdate("DELETE FROM rating");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void clearMatrix() {
        try {
            Statement statement = conn.createStatement();
            statement.executeUpdate("DELETE FROM dev");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}