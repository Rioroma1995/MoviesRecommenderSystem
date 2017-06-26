package app.services;

import org.springframework.stereotype.Component;
import app.pojo.Book;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class SlopeOne {
    private Connection conn;

    public Integer getCurrentUserId() {
        return currentUserId;
    }

    private Integer currentUserId;

    SlopeOne() {
        conn = (new DBConnect()).getConnection();
        currentUserId = currentUserId();
    }

    void fillDevFromRatings() {
        try {
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM `BX-Book-Ratings`;");
            while (resultSet.next()) {
                int userId = resultSet.getInt(1);
                String itemId = resultSet.getString(2);
                System.out.println("userId: " + userId + ", itemId: " + itemId);
                updateDevTable(userId, itemId);
            }
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Integer currentUserId() {
        int userId = 0;
        try {
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery("select max(userId) from `BX-Book-Ratings`");
            if (resultSet.next())
                userId = resultSet.getInt(1) + 1;
            System.out.println("currentUserId: " + userId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userId;
    }

    public void addNewRating(String isbn, int rating) {
        try {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO `BX-Book-Ratings` VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE Book_Rating=?");
            stmt.setInt(1, currentUserId);
            stmt.setString(2, isbn);
            stmt.setInt(3, rating);
            stmt.setInt(4, rating);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void fillDevForCurrentUser() {
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM `BX-Book-Ratings` where userId=?;");
            stmt.setInt(1, currentUserId);
            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                String itemId = resultSet.getString("ISBN");
                System.out.println("currentUserId: " + currentUserId + ", itemId: " + itemId);
                updateDevTable(currentUserId, itemId);
            }
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addNewRatings(List<Book> list) {
        try {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO `BX-Book-Ratings` VALUES (?, ?, ?)");
            for (Book book : list) {
                stmt.setInt(1, currentUserId);
                stmt.setString(2, book.getIsbn());
                stmt.setInt(3, book.getRating());
                stmt.executeUpdate();
            }
            for (Book book : list)
                updateDevTable(currentUserId, book.getIsbn());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void updateDevTable(int userId, String itemId) {
        try {
            //Користувач оцінив новий елемент, тому ми шукаємо різницю в оцініці миж ним, та кожним іншим елементом, який оцінив наш юзер
            String sql = "SELECT DISTINCT r.ISBN, (r2.Book_Rating - r.Book_Rating) as ratingDifference " +
                    "FROM `BX-Book-Ratings` r, `BX-Book-Ratings` r2 " +
                    "WHERE r.userID=? AND r.ISBN<>? " +
                    "AND r2.ISBN=? AND r2.userID=?;";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setString(2, itemId);
            stmt.setString(3, itemId);
            stmt.setInt(4, userId);
            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                String otherItemID = resultSet.getString("ISBN");
                int ratingDifference = resultSet.getInt("ratingDifference");
                //Якщо пара (itemID, otherItemID) вже є у матриці, то оновимо значення для count і sum
                int countOfPairs = 0;
                stmt = conn.prepareStatement("SELECT count(itemID1) FROM dev WHERE itemID1=? AND itemID2=?");
                stmt.setString(1, itemId);
                stmt.setString(2, otherItemID);
                ResultSet resultSet2 = stmt.executeQuery();
                if (resultSet2.next())
                    countOfPairs = resultSet2.getInt(1);
                resultSet2.close();
                if (countOfPairs > 0) {
                    String sql2 = "UPDATE dev SET count=count+1, sum=sum+? " +
                            "WHERE itemID1=? AND itemID2=?";
                    stmt = conn.prepareStatement(sql2);
                    stmt.setInt(1, ratingDifference);
                    stmt.setString(2, itemId);
                    stmt.setString(3, otherItemID);
                    stmt.executeUpdate();
                } else { //Якщо пари не було, то додаємо її
                    String sql2 = "INSERT INTO dev VALUES (?, ?, 1, ?)";
                    stmt = conn.prepareStatement(sql2);
                    stmt.setString(1, itemId);
                    stmt.setString(2, otherItemID);
                    stmt.setInt(3, ratingDifference);
                    stmt.executeUpdate();
                }
            }
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public double predict(int userId, String itemId) {
        try {
            double denom = 0.0; //знаменник
            double numer = 0.0; //чисельник
            String sql = "SELECT r.ISBN, r.Book_Rating FROM `BX-Book-Ratings` r WHERE r.userID=? AND r.ISBN <> ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setString(2, itemId);
            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                String j = resultSet.getString("ISBN");
                int ratingValue = resultSet.getInt("Book_Rating");
                //скільки разів користувачі оцінювали пару (itemId, j)
                stmt = conn.prepareStatement("SELECT d.count, d.sum FROM dev d WHERE itemID1=? AND itemID2=?");
                stmt.setString(1, itemId);
                stmt.setString(2, j);
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

    public List<Book> showInitialBooks() {
        List<Book> books = new ArrayList<>(10);
        try {
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT d.itemID1 FROM dev d where d.itemID1 <> 0380001411 AND d.itemID1 <> 0385504209 ORDER BY d.sum DESC LIMIT 10");
            ResultSet resultSet2;
            PreparedStatement stmt;
            while (resultSet.next()) {
                String itemId = resultSet.getString("itemID1");
                stmt = conn.prepareStatement("SELECT ISBN, `book-title`, `book-author`, `image-URL-L` FROM `bx-books` where ISBN = ?;");
                stmt.setString(1, itemId);
                resultSet2 = stmt.executeQuery();
                if (resultSet2.next())
                    books.add(getBookFromResultSet(resultSet2));
                resultSet2.close();
            }
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }


        return books;
    }

    private Book getBookFromResultSet(ResultSet resultSet) throws SQLException {
        String isbn = resultSet.getString(1);
        String title = resultSet.getString(2);
        String author = resultSet.getString(3);
        String image = resultSet.getString(4);
        //System.out.println("isbn: " + isbn + " title: " + title + " author: " + author + " image: " + image);
        Book book = new Book();
        book.setIsbn(isbn);
        book.setTitle(title);
        book.setAuthor(author);
        book.setImg(image);
        return book;
    }

    public List<Book> predictBest(int userId, int n) {
        List<Book> books = new ArrayList<>(n);
        try {
            String sql = "SELECT d.itemID1 as item, " +
                    "sum(d.count*(d.sum/d.count+ r.Book_Rating))/sum(d.count) as avgRat " +
                    "FROM  `BX-Book-Ratings` r, dev d " +
                    "WHERE r.userID=? " +
                    "AND d.itemID1 NOT IN (SELECT ISBN FROM `BX-Book-Ratings` WHERE userID=?) " +
                    "AND d.itemID2=r.ISBN " +
                    "GROUP BY d.itemID1 ORDER BY avgRat DESC LIMIT ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            stmt.setInt(3, n);
            ResultSet resultSet = stmt.executeQuery();
            ResultSet resultSet2;
            while (resultSet.next()) {
                String itemId = resultSet.getString("item");
                double ratingValue = resultSet.getDouble("avgRat");
                stmt = conn.prepareStatement("SELECT ISBN, `book-title`, `book-author`, `image-URL-L` FROM `bx-books` where ISBN = ?;");
                stmt.setString(1, itemId);
                resultSet2 = stmt.executeQuery();
                if (resultSet2.next())
                    books.add(getBookFromResultSet(resultSet2));
                //System.out.println("isbn: " + itemId + ", ratingValue: " + ratingValue);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return books;
    }

    private void clearRatings() {
        try {
            Statement statement = conn.createStatement();
            statement.executeUpdate("DELETE FROM `BX-Book-Ratings`");
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
