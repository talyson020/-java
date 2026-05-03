package model;

import java.sql.Connection;
import java.sql.DriverManager;

public class DAO {

    private String driver   = "com.mysql.cj.jdbc.Driver";
    private String url      = "jdbc:mysql://localhost:3306/dbcarometro";
    private String user     = "talyson020";
    private String password = "Plokij@789";

    public Connection conectar() {
        try {
            Class.forName(driver);
            return DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            System.out.println("Erro de conexão: " + e.getMessage());
            return null;
        }
    }
}