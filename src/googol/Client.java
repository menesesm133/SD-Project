package googol;

import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Scanner;

public class Client extends UnicastRemoteObject implements ClientInterface {
    private static int id;

    protected Client() throws RemoteException {
        super();
    }

    public void printResults(List<String> results) {
        Scanner scanner = new Scanner(System.in);
        int page = 0;
        while (true) {
            int start = page * 10 * 4;
            if (start >= results.size()) {
                System.out.println("No more results.");
                break;
            }
            int end = Math.min(start + 10 * 4, results.size());
            List<String> sublist = results.subList(start, end);
            for (int i = 0; i < sublist.size(); i += 4) {
                for (int j = 0; j < 4; j++) {
                    if (i + j < sublist.size()) {
                        System.out.println(sublist.get(i + j));
                    }
                }
                System.out.println();
            }
            System.out.println("\nEnter the number of the next page or 0 to exit:");
            page = scanner.nextInt();
            if (page == 0) {
                break;
            }
        }
    }

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

    public void start(Scanner sc, GateWayInterface gateway, String username)
            throws RemoteException, NotBoundException, MalformedURLException {
        int opcao = 0;
        do {
            menu();
            opcao = validaInt(sc);
            try {
                switch (opcao) {
                    case 1:
                        System.out.println("Insira o URL a indexar:");
                        String url = sc.next();
                        gateway.indexUrl(username, url);
                        break;
                    case 2:
                        System.out.println("Insira o termo de pesquisa:");
                        if (sc.hasNext()) {
                            String termo = sc.next();
                            printResults(gateway.searchWord(termo));
                        } else {
                            System.out.println("Nenhuma entrada fornecida.");
                        }
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
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } while (opcao != 4);
    }

    public static void main(String[] args) throws MalformedURLException, NotBoundException, RemoteException {
        try {
            System.out.println("Googol Client.... a iniciar.");
            Scanner sc = new Scanner(System.in);
            String username = new String();
            username = sc.next();
            System.out.println("Conectando ao servidor...");
            GateWayInterface gateway = (GateWayInterface) Naming.lookup("rmi://localhost:1099/client");
            Client client = new Client();
            id = gateway.subscribeuser((ClientInterface) client);
            System.out.println("Client sent subscription request to server.\n");
            client.start(sc, gateway, username);
        } catch (RemoteException | MalformedURLException | NotBoundException e) {
            e.printStackTrace();
        }
    }
}
