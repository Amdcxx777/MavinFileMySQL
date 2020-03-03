import java.sql.DriverManager;
import java.sql.SQLException;

public class DBWorker extends Main { // подключение к MySQL
    private final String USERNAME = "root";
    private final String PASSWORD = "Shaury"; // пароль к базе MySQL
    private final String HOSTNAME = "jdbc:mysql://localhost:3306/test ?serverTimezone=UTC";

    public DBWorker() {
        try {
             connection = DriverManager.getConnection(HOSTNAME, USERNAME, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
