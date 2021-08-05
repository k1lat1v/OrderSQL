package org.vitalii;

import com.mysql.cj.jdbc.exceptions.MysqlDataTruncation;

import java.sql.*;
import java.util.Scanner;

public class Main {

    private static final String DB_CONNECTION = "jdbc:mysql://localhost:3306/mydb?serverTimezone=Europe/Kiev";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "VitaliyLD2014";

    private static final Scanner scanner = new Scanner(System.in);

    private static final String[] tableNames = {"Goods", "Orders", "Clients"};

    public static void main(String[] args) {
        try(Connection conn = DriverManager.getConnection(DB_CONNECTION, DB_USER, DB_PASSWORD)){
            init(conn);
            menu(conn);
        }catch (SQLException e){
            System.out.println("Something went wrong");
        }
    }

    private static void init(Connection conn) throws SQLException{
        try(Statement st = conn.createStatement()){
            for(String name : tableNames){
                st.execute("SET FOREIGN_KEY_CHECKS = 0");
                st.execute("DROP TABLE IF EXISTS " + name);
                st.execute("SET FOREIGN_KEY_CHECKS = 1");
            }

            st.execute("CREATE TABLE Goods (id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, name VARCHAR(20) NOT NULL)");
            st.execute("CREATE TABLE Orders (id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, good_id INT NOT NULL, FOREIGN KEY (good_id) REFERENCES Goods (id))");
            st.execute("CREATE TABLE Clients (id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, firstName VARCHAR(20) NOT NULL, lastName VARCHAR(20) NOT NULL, order_id INT NOT NULL, FOREIGN KEY (order_id) REFERENCES Orders (id))");
        }
    }

    private static void menu(Connection conn) throws SQLException{
        while (true) {
            System.out.println("\t Select option:");
            System.out.println("1 -> view table");
            System.out.println("2 -> insert data");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    viewTable(conn);
                    break;
                case "2":
                    insert(conn);
                    break;
                default:
                    break;
            }
        }
    }

    private static int getTable(){
        System.out.println("\tselect table:");
        for(int i=0; i<tableNames.length; i++){
            System.out.println(i+1 + " -> " + tableNames[i]);
        }
        String temp = scanner.nextLine();
        try {
            return Integer.parseInt(temp);
        }catch (NumberFormatException | ArrayIndexOutOfBoundsException e){
            getTable();
        }
        return -1;
    }

    private static void viewTable(Connection conn) throws SQLException{
        int choice = getTable();
        String tableName = tableNames[choice-1];

        try(Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM " + tableName)){
            printTable(rs, tableName);
        }catch (NumberFormatException | ArrayIndexOutOfBoundsException e){
            viewTable(conn);
        }
    }

    private static void printTable(ResultSet rs, String tableName) throws SQLException{
        ResultSetMetaData rsmd = rs.getMetaData();

        System.out.println("\t *** TABLE " + tableName + " ***");

        for(int i=1; i<=rsmd.getColumnCount(); i++){
            System.out.print(rsmd.getColumnName(i) + '\t');
        }

        System.out.println();

        while(rs.next()){
            for(int i=1; i<=rsmd.getColumnCount(); i++){
                System.out.print(rs.getString(i) + '\t');
            }
            System.out.println();
        }

        System.out.println("\t ***");
    }

    private static void insert(Connection conn) throws SQLException{
        int choice = getTable();
        String tableName = tableNames[choice-1];

        try(Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM " + tableName)){
            ResultSetMetaData rsmd = rs.getMetaData();

            int size = rsmd.getColumnCount();

            String[] inputs = new String[size-1];
            String[] columns = new String[size-1];

            for(int i=2; i<=size; i++){
                String columnName = rsmd.getColumnName(i);
                System.out.println("Enter " + columnName + ":");
                columns[i-2]=columnName;
                inputs[i-2] = scanner.nextLine();
            }

            st.executeUpdate(createInsert(tableName, columns, inputs));
        }catch (SQLIntegrityConstraintViolationException e){
            System.out.println("Wrong id!");
        }catch (MysqlDataTruncation e){
            System.out.println("Wrong date input format! Try - YYYY-MM-DD HH:MM:SS");
        }
    }

    private static String createInsert(String tableName, String[] columns, String[] inputs){
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO ").append(tableName).append("(");
        for(String column : columns){
            query.append(column).append(",");
        }
        query.deleteCharAt(query.length()-1);
        query.append(") VALUES(");
        for(String input : inputs){
            query.append('\'' + input + '\'').append(",");
        }
        query.deleteCharAt(query.length()-1);
        query.append(")");

        return query.toString();
    }
}
