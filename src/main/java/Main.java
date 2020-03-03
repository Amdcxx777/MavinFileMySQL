import org.sputnikdev.bluetooth.manager.BluetoothManager;
import org.sputnikdev.bluetooth.manager.GenericBluetoothDeviceListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Main {
    private static int valuePrise;
    private static int valueSize;
    private static char index;
    private static PrintWriter printWrt;
    GenericBluetoothDeviceListener bluetoothDeviceListener;
//    BluetoothDevice bluetoothDevice;
//    BluetoothManager

    private static String createTab = "CREATE TABLE IF NOT EXISTS Books (id MEDIUMINT NOT NULL AUTO_INCREMENT, price int NOT NULL, " +
            "size int NOT NULL, type CHAR(1) NOT NULL, comment CHAR(100) NOT NULL, PRIMARY KEY (id));";
//    private static String dataIn = "insert into Books VALUES (NULL, 99, 0, 'A', 'Размер нулевой, но это цена выше лучшей заявки на продажу, поэтому она имеет тип А'), " +
//            "(NULL , 98, 50, 'A', 'Лучшая (с минимальной ценой) заявка на продажу'), (NULL , 97, 0, 'S', ''), (NULL , 96, 0, 'S', ''), " +
//            "(NULL , 95, 40, 'B', 'Лучшая (с максимальной ценой) заявка на покупку'), (NULL , 94, 30, 'B', ''), (NULL , 93, 0, 'B', ''), (NULL , 92, 77, 'B', '')";

    public static Connection connection;

    public static void main(String[] args) throws NullPointerException, NoSuchElementException, IOException, SQLException {
         new DBWorker();
        Statement statement = connection.createStatement();
        {
//            statement.executeUpdate("drop table Books"); // удаление базы
            statement.executeUpdate(createTab); // создание базы
//            statement.executeUpdate(dataIn); // заполнение базы своими значениями
        }

        File file = new File("src/datafiles/otput_data.txt"); // адрес исходящего файла
        if(!file.exists()) file.createNewFile();
        printWrt = new PrintWriter(file);
        readDataFromFile();
        printWrt.close();

//        outBaseInConsole();
    }

    public static void readDataFromFile() throws NullPointerException, NoSuchElementException, FileNotFoundException, SQLException {
        Scanner scanner = new Scanner(new File("src/datafiles/input_data.txt")); // путь к файлу
        while (scanner.hasNextLine()) { // обработка файла
            String line = scanner.nextLine(); // чтение строк из файла
            String[] array = line.split(","); // разделитель
            if (array[0].equals("u") && array.length == 4)
                writeBD(Integer.parseInt(array[1]), Integer.parseInt(array[2]), array[3]);
            else if (array[0].equals("o") && array.length == 3) renewBD(array[1], Integer.parseInt(array[2]));
            else if(array[0].equals("q") && array.length == 2) outputBestRequest(array[1]);
            else if (array[0].equals("q") && array.length == 3) outputBestPrice(Integer.parseInt(array[2]));
            else break;
        }
        scanner.close();
    }

    public static void writeBD(int priceIn, int sizeIn, String indexIn) throws SQLException { //добавление в базу
        if (indexIn.equals("bid")) index = 'B'; // заявка на покупку
        else if (indexIn.equals("ask")) index = 'A'; //заявка на продажу
        else return;
        Statement statement = connection.createStatement();
        {
            statement.executeUpdate("insert into Books VALUES (NULL, '" + priceIn + "', '" + sizeIn + "', '" + index + "', '')");
        }
    }

    public static void renewBD(String delRequest, int sizeIn) throws SQLException {
        if (delRequest.equals("sell")) { index = 'B'; // удаляет <размер> акций из заявок на покупку с максимальной ценой.
            Statement statement = connection.createStatement(); {
                ResultSet resultSet = statement.executeQuery("select * from Books where (price = (SELECT MAX(price) FROM Books where type = '" + index + "'))");
                while (resultSet.next()) {
                    valuePrise = resultSet.getInt(2);
                    valueSize = resultSet.getInt(3);
                if(valueSize < sizeIn) sizeIn = valueSize; // сокращает размер акций если они превышают доступность
                }
                statement.executeUpdate("UPDATE Books set size = size - '" + sizeIn + "' where price = '" + valuePrise + "' AND type = '" + index + "'");
            }
        } else if (delRequest.equals("buy")) { index = 'A'; // удаляет <размер> акций из заявок на продажу с минимальной ценой.
            Statement statement = connection.createStatement();
            {
                ResultSet resultSet = statement.executeQuery("select * from Books where (price = (SELECT MIN(price) FROM Books where type = '" + index + "'))");
                while (resultSet.next()) {
                    valuePrise = resultSet.getInt(2);
                    valueSize = resultSet.getInt(3);
                    if(valueSize < sizeIn) sizeIn = valueSize; // сокращает размер акций если они превышают доступность
                }
                statement.executeUpdate("UPDATE Books set size = size - '" + sizeIn + "' where price = '" + valuePrise + "' AND type = '" + index + "'");
            }
        } else return;

    }
    public static void outputBestRequest(String request) throws SQLException {
        if (request.equals("best_bid")) {
            Statement statement = connection.createStatement();
            {
                ResultSet resultSet = statement.executeQuery("select * from Books where (price = (SELECT MAX(price) FROM Books where type = 'B'))");
                while (resultSet.next()) {
//                    System.out.println(resultSet.getInt(2) + " " + resultSet.getInt(3) + " " + resultSet.getString(4) + " " + resultSet.getString(5));
                    printWrt.println(resultSet.getInt(2) + "," + resultSet.getInt(3));
                }
            }
        } else if (request.equals("best_ask")) {
            Statement statement = connection.createStatement();
            {
                ResultSet resultSet = statement.executeQuery("select * from Books where (price = (SELECT MIN(price) FROM Books where type = 'A'))");
                while (resultSet.next()) {
//                    System.out.println(resultSet.getInt(2) + " " + resultSet.getInt(3) + " " + resultSet.getString(4) + " " + resultSet.getString(5));
                    printWrt.println(resultSet.getInt(2) + "," + resultSet.getInt(3));
                }
            }
        }
    }
    public static void outputBestPrice(int price) throws SQLException {
        Statement statement1 = connection.createStatement();
        {
            ResultSet resultSet = statement1.executeQuery("SELECT * FROM Books where price = '" + price + "'");
            while (resultSet.next()) {
//                System.out.println(resultSet.getInt(2) + " " + resultSet.getInt(3) + " " + resultSet.getString(4) + " " + resultSet.getString(5));
                printWrt.println(resultSet.getInt(3));
            }
        }
    }
//        public static void outBaseInConsole() throws SQLException {
//        Statement statement1 = connection.createStatement();
//        {
//            ResultSet resultSet = statement1.executeQuery("SELECT * FROM Books");
//            while (resultSet.next()) {
//                System.out.println(resultSet.getInt(2) + " " + resultSet.getInt(3) + " " + resultSet.getString(4) + " " + resultSet.getString(5));
//            }
//        }
//    }
}


