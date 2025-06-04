package Main;

public class Dijkstra {

    public static class DijkstraResult {
        private final int[] dist;
        private final int[] pai;

        public DijkstraResult(int[] dist, int[] pai) {
            this.dist = dist;
            this.pai = pai;
        }

        public int[] getDist() { return dist; }
        public int[] getPai() { return pai; }
    }

    public static int[][] buildGraph(Rota.Ponto[] pontos, int distanciaBase) { // Usando Rota.Ponto
        int n = pontos.length;
        int[][] grafo = new int[n][n];

        if (distanciaBase <= 0) {
            System.err.println("Aviso: distanciaBase deve ser positiva para o cálculo correto das distâncias no grafo.");
        }

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    grafo[i][j] = 0;
                } else {
                    // Usa o metodo da classe Mapa
                    int passos = calcularPassosManhattan(pontos[i], pontos[j]);
                    if (passos == Integer.MAX_VALUE) { // Checagem de erro do calcularPassosManhattan
                        System.err.println("Erro ao calcular passos entre " + pontos[i].nome + " e " + pontos[j].nome);
                        // Decidir como tratar: pode lançar exceção ou atribuir um peso "infinito" no grafo
                        grafo[i][j] = Integer.MAX_VALUE; // Para Dijkstra, isso efetivamente remove a aresta ou a torna muito custosa
                    } else {
                        grafo[i][j] = passos * distanciaBase;
                    }
                }
            }
        }
        return grafo;
    }

    public static DijkstraResult dijkstra(int[][] grafo, int origem) {
        int n = grafo.length;
        int[] dist = new int[n];
        int[] pai = new int[n];
        boolean[] visitado = new boolean[n];
        final int INF = Integer.MAX_VALUE;

        for (int i = 0; i < n; i++) {
            dist[i] = INF;
            pai[i] = -1;
            visitado[i] = false;
        }
        dist[origem] = 0;

        for (int count = 0; count < n - 1; count++) {
            int u = minDist(dist, visitado);
            if (u == -1) break;
            visitado[u] = true;

            for (int v = 0; v < n; v++) {
                if (grafo[u][v] != Integer.MAX_VALUE && // Checa se a aresta é "infinita"
                        !visitado[v] && grafo[u][v] != 0 && dist[u] != INF &&
                        dist[u] + grafo[u][v] < dist[v]) {
                    dist[v] = dist[u] + grafo[u][v];
                    pai[v] = u;
                }
            }
        }
        return new DijkstraResult(dist, pai);
    }

    private static int minDist(int[] dist, boolean[] visitado) {
        int min = Integer.MAX_VALUE;
        int minIndex = -1;
        for (int v = 0; v < dist.length; v++) {
            if (!visitado[v] && dist[v] <= min) {
                min = dist[v];
                minIndex = v;
            }
        }
        return minIndex;
    }

    public static int calcularPassosManhattan(Rota.Ponto p1, Rota.Ponto p2) {
        if (p1 == null || p2 == null) {
            System.err.println("Erro: Pontos nulos foram passados para Mapa.calcularPassosManhattan.");
            return Integer.MAX_VALUE; // Ou lançar uma exceção
        }
        return Math.abs(p1.linha - p2.linha) + Math.abs(p1.coluna - p2.coluna);
    }
}