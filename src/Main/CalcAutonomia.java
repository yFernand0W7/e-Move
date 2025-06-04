package Main;

import util.Conexao; 
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CalcAutonomia {

    public static int getAutonomiaTotalVeiculo(int idVeiculo) {
        String sql = "SELECT AUTONOMIA FROM VEICULOS WHERE IDVEICULO = ?";
        try (Connection conn = Conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idVeiculo);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("AUTONOMIA");
            } else {
                System.out.println("⚠️ Autonomia não encontrada no banco para o veículo ID: " + idVeiculo);
            }
        } catch (SQLException e) {
            System.out.println("❌ Erro ao obter autonomia total do veículo ID: " + idVeiculo + ". Detalhe: " + e.getMessage());
        }
        return -1;
    }
    //Calcula a autonomia atual de um veículo com base na porcentagem da bateria usando a autonomia total do veículo como referência.
    public static int calcularAutonomiaComPercentual(int idVeiculo, int percentualBateria) {
        if (percentualBateria < 0 || percentualBateria > 100) {
            System.out.println("⚠️ Percentual de bateria inválido: " + percentualBateria + "%. Deve ser entre 0 e 100.");
            return -1;
        }

        int autonomiaTotalBase = getAutonomiaTotalVeiculo(idVeiculo);
        if (autonomiaTotalBase <= 0) { // Se for -1 (erro) ou 0 (inválido)
            System.out.println("Não foi possível calcular autonomia atual pois a autonomia total do veículo é inválida ou não foi encontrada.");
            return -1;
        }

        //usar double para o cálculo da porcentagem
        double autonomiaCalculada = autonomiaTotalBase * (percentualBateria / 100.0);
        return (int) Math.round(autonomiaCalculada);
    }
}
