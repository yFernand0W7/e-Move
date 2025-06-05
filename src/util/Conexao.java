package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexao {
    private static final String URL = "YOU_DATABASE_URL";
    private static final String USUARIO = "YOU_DATABASE_USER";
    private static final String SENHA = "YOU_DATABASE_PASSWORD";

    public static Connection getConexao() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, SENHA);
    }
}
