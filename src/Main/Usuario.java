package Main;

import util.Conexao;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;

public class Usuario {
    private final int id; //ID do usuário no banco
    private final String nome;
    private final String cpf;
    private final Date dataNascimento;
    private final char sexo;
    private final String telefone;
    private final String email;

    //Construtor usado quando temos o ID do banco
    public Usuario(int id, String nome, String cpf, Date dataNascimento, char sexo, String telefone, String email) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.dataNascimento = dataNascimento;
        this.sexo = sexo;
        this.telefone = telefone;
        this.email = email;
    }

    //Getter para o ID
    public int getId() {
        return id;
    }

    public static Usuario cadastrarUsuarioViaTerminal(Scanner scanner) {
        System.out.print("Nome: ");
        String nome = scanner.nextLine();

        System.out.print("CPF: ");
        String cpf = scanner.nextLine();

        System.out.print("Data de nascimento (dd/MM/yyyy): ");
        Date dataNascimento = null;
        try {
            dataNascimento = new SimpleDateFormat("dd/MM/yyyy").parse(scanner.nextLine());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        System.out.print("Sexo (M/F): ");
        char sexo = scanner.nextLine().toUpperCase().charAt(0);

        System.out.print("Telefone: ");
        String telefone = scanner.nextLine();

        System.out.print("Email: ");
        String email = scanner.nextLine();

        System.out.print("Senha: ");
        String senha = scanner.nextLine();

        String sql = "INSERT INTO USUARIOS (NOME, CPF, DT_NASC, SEXO, TELEFONE, EMAIL, SENHA) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = Conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, nome);
            stmt.setString(2, cpf);
            stmt.setDate(3, new java.sql.Date(dataNascimento.getTime()));
            stmt.setString(4, String.valueOf(sexo));
            stmt.setString(5, telefone);
            stmt.setString(6, email);
            stmt.setString(7, senha);

            stmt.executeUpdate();

            //Recuperar o ID gerado
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int idUsuario = generatedKeys.getInt(1);
                    System.out.println("✅ Usuário cadastrado!");
                    return new Usuario(idUsuario, nome, cpf, dataNascimento, sexo, telefone, email);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Usuario logarUsuarioViaTerminal(Scanner scanner) {
        System.out.print("Email: ");
        String email = scanner.nextLine();

        System.out.print("Senha: ");
        String senha = scanner.nextLine();

        String sql = "SELECT * FROM USUARIOS WHERE EMAIL = ? AND SENHA = ?";

        try (Connection conn = Conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, senha);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int idUsuario = rs.getInt("IDUSUARIO");
                    String nome = rs.getString("NOME");
                    String cpf = rs.getString("CPF");
                    Date dataNascimento = rs.getDate("DT_NASC");
                    char sexo = rs.getString("SEXO").charAt(0);
                    String telefone = rs.getString("TELEFONE");

                    System.out.println("✅ Login bem-sucedido! Bem-vindo, " + nome);
                    return new Usuario(idUsuario, nome, cpf, dataNascimento, sexo, telefone, email);
                } else {
                    System.out.println("❌ E-mail ou senha incorretos.");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public int getQuantidadeVeiculos() {
        int quantidade = 0;
        String sql = "SELECT COUNT(*) as QuantidadeVeiculoCadastrado FROM USUARIOS_VEICULOS WHERE ID_USUARIO = ?";

        try (Connection conn = Conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, this.id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    quantidade = rs.getInt("QuantidadeVeiculoCadastrado");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return quantidade;
    }


    public void exibirResumo() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        System.out.println("\n=== DADOS DO USUÁRIO ===");
        System.out.println("Nome: " + nome);
        System.out.println("CPF: " + cpf);
        System.out.println("Nascimento: " + sdf.format(dataNascimento));
        System.out.println("Sexo: " + (sexo == 'M' ? "Masculino" : "Feminino"));
        System.out.println("Telefone: " + telefone);
        System.out.println("Email: " + email);
        System.out.println("Veículos cadastrados: " + getQuantidadeVeiculos());
    }
}
