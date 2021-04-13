package api;

import java.sql.*;

public class MySQLAccess {

    private Connection cnx;
    // private Statement statement;
    private PreparedStatement query;
    // private ResultSet resultSet;

    public MySQLAccess() throws SQLException, ClassNotFoundException {
        connect("root", "");
    }

    private void connect(String user, String password) throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        cnx = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/csddb?" +
                "user=" + user + "&password=" + password);
    }

    public void addUser(String name, String password, double amount) {
        try {
            query = cnx.prepareStatement(
                    "INSERT INTO users VALES (?, ?, ?)"
            );
            query.setString(1, name);
            query.setString(2, password);
            query.setDouble(3, amount);
            query.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            System.out.println("User " + name + " added.");
            try {
                query.close();
            } catch (SQLException e){
                e.printStackTrace();
            }
        }
    }
}
