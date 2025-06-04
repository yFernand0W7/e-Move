package Main;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Usuario cadastrante;
        String opcao;

        // Tela de login
        while (true) {
            System.out.println("\n===== SEJA BEM-VINDO AO SISTEMA DE VEÍCULOS ELÉTRICOS =====");
            System.out.println("1 - Cadastrar novo usuário");
            System.out.println("2 - Fazer login");
            System.out.println("3 - Sair do sistema");
            System.out.print("Escolha uma opção: ");
            opcao = scanner.nextLine();

            switch (opcao) {
                case "1":
                    cadastrante = Usuario.cadastrarUsuarioViaTerminal(scanner);
                    cadastrante.exibirResumo();
                    mostrarMenuPrincipal(scanner, cadastrante);
                    break;
                case "2":
                    cadastrante = Usuario.logarUsuarioViaTerminal(scanner);
                    if (cadastrante != null) {
                        mostrarMenuPrincipal(scanner, cadastrante);
                    }
                    break;
                case "3":
                    System.out.println("\nEncerrando programa. Até logo!");
                    scanner.close();
                    return;
                default:
                    System.out.println("\nOpção inválida. Digite 1, 2 ou 3.");
            }
        }
    }

    private static void mostrarMenuPrincipal(Scanner scanner, Usuario usuario) {
        String opcao;
        //Menu Principal
        while (true) {
            System.out.println("\n===== MENU PRINCIPAL =====");
            System.out.println("1 - Cadastrar novo veículo");
            System.out.println("2 - Listar meus veículos");
            System.out.println("3 - Ver meus dados");
            System.out.println("4 - Calcular uma rota");
            System.out.println("5 - Sair");
            System.out.print("Escolha uma opção: ");
            opcao = scanner.nextLine();

            switch (opcao) {
                case "1":
                    Veiculos.cadastrarVeiculo(scanner, usuario);
                    break;
                case "2":
                    Veiculos.listarVeiculos(usuario);
                    break;
                case "3":
                    usuario.exibirResumo();
                    break;
                case "4":
                    Rota.iniciarCalculoRota(scanner, usuario.getId()); // Agora passa o ID do usuário
                    break;
                case "5":
                    System.out.println("\nRetornando ao menu inicial...");
                    return;
                default:
                    System.out.println("\nOpção inválida. Tente novamente.");
            }
        }
    }
}
