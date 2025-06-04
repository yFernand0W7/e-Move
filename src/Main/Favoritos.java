package Main;
import java.util.List;

public class Favoritos {
    private List<EstacoesRecarga> estacoesFavoritas;

    public void adicionarEstacaoFavorita(EstacoesRecarga estacao) {
        estacoesFavoritas.add(estacao);
    }

    public void removerEstacaoFavorita(EstacoesRecarga estacao) {
        estacoesFavoritas.remove(estacao);
    }
}
