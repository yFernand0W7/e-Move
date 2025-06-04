package Main;

import util.Conexao;

import java.sql.*;
import java.util.*;

public class Rota {

    public static class Ponto {
        public String nome;
        public int linha;
        public int coluna;
        //Futuramente, adicionar mais campos aqui (UF, LAT, LONGI, QT_ESTACOES) e ajustar o construtor e o metodo carregarPontosDoBanco() para populá-los.

        public Ponto(String nome, int linha, int coluna) {
            this.nome = nome;
            this.linha = linha;
            this.coluna = coluna;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Ponto ponto = (Ponto) o;
            return linha == ponto.linha && coluna == ponto.coluna && Objects.equals(nome, ponto.nome);
        }

        @Override
        public int hashCode() {
            return Objects.hash(nome, linha, coluna);
        }

        @Override
        public String toString() { // Útil para debug
            return "Ponto{" +
                    "nome='" + nome + '\'' +
                    ", linha=" + linha +
                    ", coluna=" + coluna +
                    '}';
        }
    }

    private static Ponto[] carregarPontosDoBanco() {
        List<Ponto> listaPontos = new ArrayList<>();
        //Seleciona as colunas NM_CIDADE, COORD_LINHA, COORD_COLUNA da tabela CIDADES
        //Adicionado filtro para garantir que coordenadas não sejam nulas,
        String sql = "SELECT NM_CIDADE, COORD_LINHA, COORD_COLUNA FROM CIDADES " +
                "WHERE COORD_LINHA IS NOT NULL AND COORD_COLUNA IS NOT NULL";

        try (Connection conn = Conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String nome = rs.getString("NM_CIDADE");
                //Tratamento para caso COORD_LINHA ou COORD_COLUNA possam ser NULL no DB,
                int linha = rs.getInt("COORD_LINHA");
                int coluna = rs.getInt("COORD_COLUNA");

                if (nome != null && !nome.trim().isEmpty()) {
                    listaPontos.add(new Ponto(nome, linha, coluna));
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erro ao carregar cidades do banco de dados: " + e.getMessage());
            //e.printStackTrace(); // Descomente para ver o log de erro
            return new Ponto[0]; // etorna array vazio para evitar NullPointerException
        }

        if (listaPontos.isEmpty()) {
            System.out.println("⚠️ Nenhuma cidade encontrada no banco de dados com coordenadas válidas ou ocorreu um erro ao carregar.");
        }
        return listaPontos.toArray(new Ponto[0]);
    }

    public static void iniciarCalculoRota(Scanner scanner, int idUsuario) {
        Ponto[] pontos = carregarPontosDoBanco();

        if (pontos == null || pontos.length == 0) {
            System.out.println("❌ Não foi possível carregar os dados do mapa do banco de dados. Operação de cálculo de rota cancelada.");
            return;
        }

        int idVeiculo = Veiculos.selecionarVeiculoDoUsuario(scanner, idUsuario);
        if (idVeiculo == -1) {
            //Mensagem de erro já é exibida por selecionarVeiculoDoUsuario
            return;
        }

        //Obter autonomia inicial (pode ser parcial)
        int autonomiaInicial = Veiculos.getAutonomiaInicialParaRota(scanner, idVeiculo);
        if (autonomiaInicial < 0) { // getAutonomiaInicialParaRota retorna -1 em caso de erro
            System.out.println("❌ Não foi possível determinar a autonomia inicial do veículo para a rota. Operação cancelada.");
            return;
        }

        //Obter autonomia máxima do veículo (para usar após recargas)
        int autonomiaMaxima = CalcAutonomia.getAutonomiaTotalVeiculo(idVeiculo);
        if (autonomiaMaxima <= 0) { // Se for -1 (erro) ou 0 (inválido)
            System.out.println("❌ Não foi possível determinar a autonomia máxima do veículo (valor inválido: " + autonomiaMaxima + "). Operação cancelada.");
            return;
        }

        //Sanity check e informação ao usuário
        if (autonomiaInicial > autonomiaMaxima) {
            System.out.println("⚠️ Autonomia inicial (" + autonomiaInicial + "km) informada é maior que a autonomia máxima do veículo (" + autonomiaMaxima + "km). Usando autonomia máxima.");
            autonomiaInicial = autonomiaMaxima;
        }
        System.out.println("ℹ️ Autonomia para iniciar a rota: " + autonomiaInicial + "km (Máxima do veículo: " + autonomiaMaxima + "km).");


        //*****A distanciaBase foi definida como 10km para simulação de distancias entre cidades*********
        int distanciaBaseEntreVizinhos = 10;
        int[][] grafo = Dijkstra.buildGraph(pontos, distanciaBaseEntreVizinhos);

        System.out.print("Digite o nome da cidade de ORIGEM (exatamente como na lista): ");
        String origemNome = scanner.nextLine().trim();
        System.out.print("Digite o nome da cidade de DESTINO (exatamente como na lista): ");
        String destinoNome = scanner.nextLine().trim();

        int origemIdx = encontrarIndice(origemNome, pontos);
        int destinoIdx = encontrarIndice(destinoNome, pontos);

        if (origemIdx == -1) {
            System.out.println("❌ Cidade de origem '" + origemNome + "' inválida ou não encontrada.");
            return;
        }
        if (destinoIdx == -1) {
            System.out.println("❌ Cidade de destino '" + destinoNome + "' inválida ou não encontrada.");
            return;
        }

        Dijkstra.DijkstraResult result = Dijkstra.dijkstra(grafo, origemIdx);

        exibirRotaComParadas(result.getDist(), result.getPai(), origemIdx, destinoIdx, pontos,
                autonomiaInicial, autonomiaMaxima, distanciaBaseEntreVizinhos);
    }

    private static void exibirRotaComParadas(int[] distOriginalDijkstra, int[] paiOriginalDijkstra,
                                             int origemIdx, int destinoIdx, Ponto[] todosPontos,
                                             int autonomiaInicialVeiculo, int autonomiaMaximaVeiculo,
                                             int distanciaBase) {

        if (distOriginalDijkstra[destinoIdx] == Integer.MAX_VALUE) {
            System.out.println("🛣️ Não foi possível calcular uma rota entre " + todosPontos[origemIdx].nome + " e " + todosPontos[destinoIdx].nome + ".");
            return;
        }

        System.out.println("\n=== DETALHES DA ROTA COM SUGESTÕES DE PARADA ===");

        int distanciaTotalGradeOrigemDestino = distOriginalDijkstra[destinoIdx];
        Ponto pontoOrigemReal = todosPontos[origemIdx];
        Ponto pontoDestinoReal = todosPontos[destinoIdx];

        System.out.println("🏁 Origem: " + pontoOrigemReal.nome);
        System.out.println("🎯 Destino Final: " + pontoDestinoReal.nome);
        System.out.printf("📏 Distância Total (em linha de grade direta): %d km%n", distanciaTotalGradeOrigemDestino);

        if (autonomiaInicialVeiculo <= 0 && distanciaTotalGradeOrigemDestino > 0) {
            System.out.println("⚠️ Autonomia inicial do veículo é zero ou inválida. Não é possível iniciar a rota.");
            return;
        }
        //Checagem da autonomia máxima
        if (autonomiaMaximaVeiculo <= 0 && distanciaTotalGradeOrigemDestino > autonomiaInicialVeiculo) {
            System.out.println("⚠️ Autonomia máxima do veículo é inválida. Não é possível calcular recargas de forma confiável se forem necessárias.");
            //Pode prosseguir se a autonomia inicial for suficiente, mas alerta sobre recargas
        }


        if (distanciaTotalGradeOrigemDestino == 0) {
            if (origemIdx == destinoIdx) System.out.println("🏁 Você já está no destino!");
            else System.out.println("⚠️ Distância 0 entre cidades diferentes (provavelmente mesmas coordenadas no grid).");
            return;
        }

        if (distanciaTotalGradeOrigemDestino <= autonomiaInicialVeiculo) {
            System.out.println("🛣️ Rota Sugerida: " + pontoOrigemReal.nome + " -> " + pontoDestinoReal.nome);
            System.out.println("✅ A autonomia inicial do veículo (" + autonomiaInicialVeiculo + "km) é suficiente para esta rota.");
            return;
        }

        System.out.println("\n🛣️ Rota Sugerida com Paradas para Recarga:");
        List<Ponto> rotaSugeridaComCidades = new ArrayList<>();
        rotaSugeridaComCidades.add(pontoOrigemReal);
        Ponto cidadeAtualSimulacao = pontoOrigemReal;
        int distanciaAcumuladaNaRotaRealSugerida = 0;
        int limiteParadas = todosPontos.length + 2; // Contra loops infinitos
        int paradasFeitas = 0;

        int autonomiaDisponivelParaEsteTrecho = autonomiaInicialVeiculo;

        while (!cidadeAtualSimulacao.equals(pontoDestinoReal) && paradasFeitas < limiteParadas) {
            int distDiretaAtualAoDestinoFinal = Dijkstra.calcularPassosManhattan(cidadeAtualSimulacao, pontoDestinoReal) * distanciaBase;

            if (distDiretaAtualAoDestinoFinal <= autonomiaDisponivelParaEsteTrecho) {
                distanciaAcumuladaNaRotaRealSugerida += distDiretaAtualAoDestinoFinal;
                cidadeAtualSimulacao = pontoDestinoReal; // Define para sair do loop; o destino é adicionado após o loop
            } else {
                Ponto melhorProximaCidadeParada = null;
                int menorDistanciaAoDestinoViaCandidato = Integer.MAX_VALUE;

                int passosIdeaisNoTrecho = (autonomiaDisponivelParaEsteTrecho > 0 && distanciaBase > 0) ? (autonomiaDisponivelParaEsteTrecho / distanciaBase) : 0;
                if (passosIdeaisNoTrecho == 0 && autonomiaDisponivelParaEsteTrecho > 0) passosIdeaisNoTrecho = 1; // Tentar andar pelo menos 1 passo se tiver alguma autonomia

                //Ponto pontoIdealNoGrid = Mapa.getPontoIdealIntermediarioNoGrid(cidadeAtualSimulacao, pontoDestinoReal, passosIdeaisNoTrecho);

                for (Ponto candidato : todosPontos) {
                    if (candidato.equals(cidadeAtualSimulacao) || rotaSugeridaComCidades.contains(candidato)) {
                        continue;
                    }
                    int distAtualParaCandidato = Dijkstra.calcularPassosManhattan(cidadeAtualSimulacao, candidato) * distanciaBase;

                    if (distAtualParaCandidato > 0 && distAtualParaCandidato <= autonomiaDisponivelParaEsteTrecho) {
                        int distCandidatoAoDestinoFinal = Dijkstra.calcularPassosManhattan(candidato, pontoDestinoReal) * distanciaBase;
                        //Prioriza o candidato que nos leva mais perto do destino final
                        if (distCandidatoAoDestinoFinal < menorDistanciaAoDestinoViaCandidato) {
                            //Garante que estamos progredindo em direção ao destino
                            if (distCandidatoAoDestinoFinal < distDiretaAtualAoDestinoFinal) {
                                melhorProximaCidadeParada = candidato;
                                menorDistanciaAoDestinoViaCandidato = distCandidatoAoDestinoFinal;
                            }
                        }
                    }
                }

                if (melhorProximaCidadeParada != null) {
                    distanciaAcumuladaNaRotaRealSugerida += Dijkstra.calcularPassosManhattan(cidadeAtualSimulacao, melhorProximaCidadeParada) * distanciaBase;
                    rotaSugeridaComCidades.add(melhorProximaCidadeParada);
                    cidadeAtualSimulacao = melhorProximaCidadeParada;
                    autonomiaDisponivelParaEsteTrecho = autonomiaMaximaVeiculo; //Recarrega para autonomia MÁXIMA
                } else {
                    //Se não encontrou cidade intermediária, informa necessidade recarga
                    int rechargesConceituais = (autonomiaMaximaVeiculo > 0) ? ((int) Math.ceil((double) distanciaTotalGradeOrigemDestino / autonomiaMaximaVeiculo) - 1) : -1;
                    if (rechargesConceituais < 0) rechargesConceituais = 0; // Caso autonomiaMaximaVeiculo seja inválida ou não precise

                    System.out.println("   (Não foi possível identificar uma cidade definida adequada para a próxima parada a partir de " + cidadeAtualSimulacao.nome + ")");
                    if (autonomiaMaximaVeiculo > 0 && rechargesConceituais >=0) { // Evita imprimir MAX_VALUE
                        System.out.printf("A rota total exigi %d parada(s) para recarga (considerando carga total após cada).%n", rechargesConceituais);
                    }
                    //Limpa sugestões parciais e termina a rota, pois não há como prosseguir com paradas definidas
                    rotaSugeridaComCidades.clear();
                    rotaSugeridaComCidades.add(pontoOrigemReal);
                    cidadeAtualSimulacao = pontoDestinoReal; //Força saída do loop, a rota direta será impressa
                }
            }
            paradasFeitas++;
        }

        //Garante que o destino final seja o último da lista se ainda não for, ou se a lista foi limpa.
        if (rotaSugeridaComCidades.isEmpty() || !rotaSugeridaComCidades.get(rotaSugeridaComCidades.size() - 1).equals(pontoDestinoReal)) {
            //Se a lista foi limpa (porque não encontrou paradas), ela conterá apenas a origem. Adiciona destino.
            //Se o loop terminou e a última cidade não é o destino (ex: limite de paradas), adiciona destino.
            rotaSugeridaComCidades.add(pontoDestinoReal);
        }

        if (paradasFeitas >= limiteParadas && !cidadeAtualSimulacao.equals(pontoDestinoReal)) {
            System.out.println("   (Atingiu o limite de sugestões de paradas. A rota pode estar incompleta ou otimização difícil).");
        }

        //Imprimir a rota sugerida com cidades
        for (int i = 0; i < rotaSugeridaComCidades.size(); i++) {
            System.out.print(rotaSugeridaComCidades.get(i).nome);
            boolean isUltimaCidadeDaLista = (i == rotaSugeridaComCidades.size() - 1);
            boolean isOrigem = (i == 0);

            if (!isUltimaCidadeDaLista) { //Se não for a última cidade da lista impressa
                //Adiciona "(Recarga Sugerida)" apenas se for uma parada intermediária REAL
                if (!isOrigem && !rotaSugeridaComCidades.get(i+1).equals(pontoDestinoReal)) {
                    System.out.print(" (Recarga Sugerida)");
                } else if (!isOrigem && rotaSugeridaComCidades.get(i+1).equals(pontoDestinoReal) && rotaSugeridaComCidades.size() > 2){
                    //Caso especial: Origem -> Parada -> Destino.
                    System.out.print(" (Recarga Sugerida)");
                }
                System.out.print(" -> ");
            }
        }
        System.out.println();

        //Informa sobre a distância da rota com desvios, se houver paradas e for diferente da direta
        if (rotaSugeridaComCidades.size() > 2 && //Houve pelo menos uma parada intermediária
                distanciaAcumuladaNaRotaRealSugerida > 0 &&
                Math.abs(distanciaAcumuladaNaRotaRealSugerida - distanciaTotalGradeOrigemDestino) > distanciaBase) { //Se for significativamente diferente
            System.out.printf("   Distância da rota com paradas sugeridas: %d km%n", distanciaAcumuladaNaRotaRealSugerida);
            System.out.println("   (Esta distância pode ser maior que a distância de grade direta devido aos desvios para cidades de recarga).");
        }
    }

    private static int encontrarIndice(String nome, Ponto[] pontos) {
        for (int i = 0; i < pontos.length; i++) {
            if (pontos[i].nome.equalsIgnoreCase(nome)) return i;
        }
        return -1;
    }
}