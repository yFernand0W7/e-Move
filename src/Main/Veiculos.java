package Main;

import util.Conexao;
import java.sql.*;
import java.util.*;

public record Veiculos(String marca, String modelo, int tipoPlugin, double autonomia) {

    public static void cadastrarVeiculo(Scanner scanner, Usuario usuario) {
        System.out.println("\n===== CADASTRO DE VEÍCULO =====");
        System.out.println("1 - Vincular veículo popular do banco");
        System.out.print("Escolha: ");
        int opcao = 0;
        try {
            opcao = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("❌ Opção inválida. Por favor, digite um número.");
            return;
        }
        if (opcao == 1) {
            selecionarVeiculoPopular(scanner, usuario);
        }
    }

    private static void selecionarVeiculoPopular(Scanner scanner, Usuario usuario) {
        System.out.println("\n===== VEÍCULOS POPULARES NO BANCO =====");
        String sql = "SELECT IDVEICULO, MARCA, MODELO, TIPO_PLUGIN, AUTONOMIA FROM VEICULOS";
        try (Connection conn = Conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            int contador = 1;
            Map<Integer, Integer> mapaVeiculos = new HashMap<>();
            while (rs.next()) {
                int idVeiculoDB = rs.getInt("IDVEICULO");
                String marcaDB = rs.getString("MARCA");
                String modeloDB = rs.getString("MODELO");
                String tipoPluginDB = rs.getString("TIPO_PLUGIN");
                double autonomiaDB = rs.getDouble("AUTONOMIA");
                System.out.printf("%d - %s %s (%.0f km, plug: %s)\n",
                        contador, marcaDB, modeloDB, autonomiaDB, tipoPluginDB);
                mapaVeiculos.put(contador, idVeiculoDB);
                contador++;
            }
            if (mapaVeiculos.isEmpty()) {
                System.out.println("Nenhum veículo disponível no banco para seleção.");
                return;
            }
            System.out.print("Escolha o número do veículo para vincular: ");
            int escolha = 0;
            try {
                escolha = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("❌ Entrada inválida. Por favor, digite um número.");
                return;
            }
            if (!mapaVeiculos.containsKey(escolha)) {
                System.out.println("❌ Opção inválida.");
                return;
            }
            int idVeiculoEscolhido = mapaVeiculos.get(escolha);
            String verificaSql = "SELECT ID_USUARIO FROM USUARIOS_VEICULOS WHERE ID_USUARIO = ? AND ID_VEICULO = ?";
            try (PreparedStatement verificaStmt = conn.prepareStatement(verificaSql)) {
                verificaStmt.setInt(1, usuario.getId());
                verificaStmt.setInt(2, idVeiculoEscolhido);
                try (ResultSet verificaRs = verificaStmt.executeQuery()) {
                    if (verificaRs.next()) {
                        System.out.println("⚠️ Você já vinculou esse veículo!");
                        return;
                    }
                }
            }
            String insertSql = "INSERT INTO USUARIOS_VEICULOS (ID_USUARIO, ID_VEICULO) VALUES (?, ?)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setInt(1, usuario.getId());
                insertStmt.setInt(2, idVeiculoEscolhido);
                int linhasAfetadas = insertStmt.executeUpdate();
                if (linhasAfetadas > 0) {
                    System.out.println("✅ Veículo vinculado com sucesso!");
                } else {
                    System.out.println("❌ Erro ao vincular o veículo.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("❌ Erro ao acessar o banco de dados ao selecionar veículo popular.");
        }
    }

    public static void listarVeiculos(Usuario usuario) {
        System.out.println("\n===== MEUS VEÍCULOS =====");
        String sql = "SELECT v.MARCA, v.MODELO, v.TIPO_PLUGIN, v.AUTONOMIA " +
                "FROM VEICULOS v " +
                "JOIN USUARIOS_VEICULOS uv ON v.IDVEICULO = uv.ID_VEICULO " +
                "WHERE uv.ID_USUARIO = ?";
        try (Connection conn = Conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, usuario.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                boolean encontrou = false;
                while (rs.next()) {
                    String marcaDB = rs.getString("MARCA");
                    String modeloDB = rs.getString("MODELO");
                    String tipoPluginDB = rs.getString("TIPO_PLUGIN");
                    double autonomiaDB = rs.getDouble("AUTONOMIA");
                    System.out.printf("- %s %s: Autonomia de %.0f km, Plug: %s\n",
                            marcaDB, modeloDB, autonomiaDB, tipoPluginDB);
                    encontrou = true;
                }
                if (!encontrou) {
                    System.out.println("Nenhum veículo vinculado à sua conta.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("❌ Erro ao listar veículos do banco de dados.");
        }
    }

    public static int selecionarVeiculoDoUsuario(Scanner scanner, int idUsuario) {
        String sql = "SELECT V.IDVEICULO, V.MODELO, V.AUTONOMIA " +
                "FROM USUARIOS_VEICULOS UV " +
                "INNER JOIN VEICULOS V ON UV.ID_VEICULO = V.IDVEICULO " +
                "WHERE UV.ID_USUARIO = ?";
        try (Connection conn = Conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);
            ResultSet rs = stmt.executeQuery();
            List<Integer> veiculoIds = new ArrayList<>();
            int count = 1;
            System.out.println("\n=== SELECIONE UM DOS SEUS VEÍCULOS PARA A ROTA ===");
            while (rs.next()) {
                int idVeiculo = rs.getInt("IDVEICULO");
                String modelo = rs.getString("MODELO");
                int autonomiaDb = rs.getInt("AUTONOMIA");
                System.out.printf("%d - %s (Autonomia: %dkm)%n", count, modelo, autonomiaDb);
                veiculoIds.add(idVeiculo);
                count++;
            }
            if (veiculoIds.isEmpty()) {
                System.out.println("❌ Você não possui veículos vinculados. Cadastre ou vincule um veículo primeiro.");
                return -1;
            }
            System.out.print("Escolha o número do veículo: ");
            int escolhaNumero;
            try {
                escolhaNumero = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("❌ Entrada inválida. Por favor, digite um número.");
                return -1;
            }
            if (escolhaNumero >= 1 && escolhaNumero <= veiculoIds.size()) {
                return veiculoIds.get(escolhaNumero - 1);
            } else {
                System.out.println("❌ Escolha inválida.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("❌ Erro ao buscar seus veículos no banco de dados.");
        }
        return -1;
    }


    //Pergunta ao usuário o estado da bateria e retorna a autonomia inicial para a rota.
    public static int getAutonomiaInicialParaRota(Scanner scanner, int idVeiculo) {
        System.out.print("Seu veículo está totalmente carregado? (S/N): ");
        String resposta = scanner.nextLine().trim();
        char opcao = 'S'; // Padrao para Sim se a entrada for vazia ou inválida

        if (!resposta.isEmpty()) {
            opcao = resposta.toUpperCase().charAt(0);
        }

        if (opcao == 'N') {
            System.out.print("Quanto de bateria em % (0-100) o seu veículo ainda possui? ");
            int percentualBateria;
            try {
                percentualBateria = Integer.parseInt(scanner.nextLine());
                if (percentualBateria < 0 || percentualBateria > 100) {
                    System.out.println("❌ Percentual de bateria inválido. Tente o cálculo de rota novamente.");
                    return -1; // Indica erro para o chamador
                }
            } catch (NumberFormatException e) {
                System.out.println("❌ Entrada de percentual inválida. Tente o cálculo de rota novamente.");
                return -1; // Indica erro para o chamador
            }
            return CalcAutonomia.calcularAutonomiaComPercentual(idVeiculo, percentualBateria);
        } else { // Se 'S' ou qualquer outra coisa
            return CalcAutonomia.getAutonomiaTotalVeiculo(idVeiculo);
        }
    }
}