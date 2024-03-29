package googol;

import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Scanner;

public class Client extends UnicastRemoteObject implements ClientInterface {

    protected Client() throws RemoteException {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * As opções disponíveis são:
     *
     * Indexar um novo URL: permite que o usuário insira manualmente um URL para ser
     * indexado pelo módulo de busca remoto.
     * Consultar lista de páginas com ligação para uma página específica: permite
     * que o usuário digite os termos de pesquisa e recebe uma lista de páginas que
     * contêm links para a página procurada.
     * Página de administração atualizada em tempo real: permite que o usuário
     * visualize as 10 pesquisas mais comuns realizadas pelos usuários.
     * Sair: fecha o programa.
     */
    public static void menu() {
        // menu com as opções que utilizador pode realizar
        System.out.println("1) Indexar um novo Url\n"
                + "2) Pesquisar\n"
                + "3) Página de administração atualizada em tempo real\n"
                + "4) Sair\n");

        System.out.println("Digite a opção desejada:");
    }

    // O metodo validaInteiro() testa cada token que é lida no canal de leitura,
    // retornando um valor se for um inteiro.
    public static int validaInt(Scanner sc) {
        while (!sc.hasNextInt()) {
            System.out.println("Opção inválida. Tente novamente.");
            sc.next();
        }
        return sc.nextInt();
    }

    public static String register() {
        String username = new String();
        System.out.println("Insira o seu nome de utilizador:");
        Scanner sc = new Scanner(System.in);
        username = sc.next();
        if (username.isEmpty()) {
            System.out.println("Nome de utilizador inválido.");
            register();
        } else if (isUsernameTaken(username)) {
            System.out.println("Nome de utilizador já está em uso.");
            register();
        }

        return username;
    }

    private static boolean isUsernameTaken(String username) {
        // Check if the username is already taken
        // Return true if it is taken, false otherwise
        // You can implement your own logic here
        return false;
    }

    public void start(Scanner sc, GateWayInterface gateway)
            throws RemoteException, NotBoundException, MalformedURLException {
        int opcao = 0;
        do {
            menu();
            opcao = validaInt(sc);
            switch (opcao) {
                case 1:
                    System.out.println("Insira o URL a indexar:");
                    String url = sc.next();
                    gateway.indexUrl(url);
                    break;
                case 2:
                    System.out.println("Insira o termo de pesquisa:");
                    String termo = sc.next();
                    // pesquisar(termo);
                    break;
                case 3:
                    // admin();
                    break;
                case 4:
                    System.out.println("A sair...");
                    break;
                default:
                    System.out.println("Opção inválida.");
                    break;
            }
        } while (opcao != 4);
    }

    public static void main(String[] args) throws MalformedURLException, NotBoundException, RemoteException {
        System.out.println("Googol Client.... a iniciar.");
        Scanner sc = new Scanner(System.in);

        System.out.println("Conectando ao servidor...");
        GateWayInterface gateway = (GateWayInterface) Naming.lookup("rmi://localhost/gate");
        Client client = new Client();
        gateway.subscribe(args[0], (ClientInterface) client);
        System.out.println("Client sent subscription request to server.");
        client.start(sc, gateway);
    }
}
