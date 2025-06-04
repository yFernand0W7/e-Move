package Main;
import java.util.List;

public class EstacoesRecarga {
    private String id;
    private String nome;
    private String endereco;
    private double potencia;
    private double precoPorKWh;
    private boolean disponivel;
    private List<String> comentarios;

    public boolean estaDisponivel() {
        return disponivel;
    }

    public void adicionarComentario(String comentario) {
        comentarios.add(comentario);
    }

    // Outros métodos para interagir com a estação
}
