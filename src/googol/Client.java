package googol;

import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

public class Client extends UnicastRemoteObject implements ClientInterface {
    private static int id;

    protected Client() throws RemoteException {
        super();
    }

    /**
     * Prints the results in a paginated format.
     *
     * This method prints the results in pages of 10 items each, with each item
     * consisting of 4 lines. The user is prompted to enter the number of the next
     * page to view. If the user enters 0, the method exits. If there are no more
     * results to display, the method informs the user and exits.
     *
     * @param results The list of results to print. Each result is a string and the
     *                list is assumed to be in the order in which the results should
     *                be printed.
     */
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

    /**
     * Prints the main menu options to the console.
     *
     * This method displays a list of options that the user can perform. The options
     * include indexing a new URL, searching by word, searching by URL, and exiting
     * the program. After displaying the options, it prompts the user to enter their
     * desired option.
     */
    public static void menu() {
        // menu com as opções que utilizador pode realizar
        System.out.println("1) Indexar um novo Url\n"
                + "2) Pesquisar por palavra\n"
                + "3) Pesquisar por URL\n"
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

    /**
     * Starts the client application and handles user input.
     *
     * This method displays a menu to the user and processes their input. The user
     * can choose to index a new URL, search by word, search by URL, or exit the
     * application. The user's input is validated before being processed. If the
     * user enters an invalid option, a message is displayed and the menu is shown
     * again. If a RemoteException occurs during the execution of an option, the
     * stack trace is printed to the standard error stream.
     *
     * @param sc       The Scanner object to read user input.
     * @param gateway  The GateWayInterface object to perform operations.
     * @param username The username of the user.
     * @throws RemoteException       If a remote access error occurs.
     * @throws NotBoundException     If an attempt is made to lookup or unbind in
     *                               the registry a name that has no associated
     *                               binding.
     * @throws MalformedURLException If a string is passed as a parameter to a
     *                               method and the string does not have the
     *                               appropriate format.
     */
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
                        break;
                    case 3:
                        System.out.println("Insira o url de pesquisa:");
                        if (sc.hasNext()) {
                            String urlpesquisa = sc.next();

                            HashSet<String> urlslists = gateway.searchUrls(urlpesquisa);

                            for (String url1 : urlslists) {
                                System.out.println(url1);
                            }

                        } else {
                            System.out.println("Nenhuma entrada fornecida.");
                        }
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
