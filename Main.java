import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static Scanner sc = new Scanner(System.in);
    private static String bd = "Books.bd";

    public static void main(String args[]) throws Exception {
        menu();
    }

    private static void menu(){
        char op, opIndice;
        boolean indice = false;
        int quant = 2147483647; // bd nao foi incializado
        long time = 0;
        try {
            RandomAccessFile arq = new RandomAccessFile(bd, "r");
            quant = arq.readInt();
            System.out.println("\n <<<<<< " + bd + " >>>>>>");
            System.out.println("Último id: " + quant + "\n");
            arq.close();
        } catch (Exception e) {
            System.out.println("\n !!!!! Banco de Dados não inicializado !!!!! \n");
        } 
        System.out.println("1. Inicializar Banco de Dados");
        System.out.println("2. Operações com CRUD");
        System.out.println("3. Ordenações");
        System.out.println("4. Indexação");
        System.out.println("5. Compactação");
        System.out.println("\n(aperte qualquer outra tecla para sair)\n");

        op = sc.next().charAt(0);
        switch(op) {
            case '1':     
                        // Indexacao
                        System.out.println("Deseja inicializar os arquivos de índice? (Essa ação pode levar alguns minutos)");
                        System.out.println("1. Sim, mas limitando a quantidade de registros (Recomendado: 1000)");
                        System.out.println("2. Sim, quero com todos os registros (+50.000)");
                        System.out.println("3. Não, mas limitando a quantidade de registros");
                        System.out.println("4. Não");

                        opIndice = sc.next().charAt(0);
                        switch(opIndice) {
                            case '1': System.out.print("Quantidade desejada: ");
                                        quant = sc.nextInt();
                                        indice = true;
                                        break;
                            case '2':  System.out.println("...");
                                        indice = true;
                                        break;
                            case '3': System.out.print("Quantidade desejada: ");
                                        quant = sc.nextInt();
                                        break;
                            case '4': break;
                        }

                        System.out.println("Aguarde...");
                        try {
                            Book.construirBD("", quant);
                            if(indice) {
                                time = System.currentTimeMillis();
                                ArvoreB.create();
                                time = System.currentTimeMillis() - time;
                                System.out.println("Tempo de criação: " + (double)time/1000 + "s\n");
                                
                                time = System.currentTimeMillis();
                                ListaInvertida.create();
                                time = System.currentTimeMillis() - time;
                                System.out.println("Tempo de criação: " + (double)time/1000 + "s\n");
                                
                                time = System.currentTimeMillis();
                                Hashing.create();
                                time = System.currentTimeMillis() - time;
                                System.out.println("Tempo de criação: " + (double)time/1000 + "s\n");
                            }

                        } catch(FileNotFoundException e) {
                            System.out.println("Arquivo necessário: \"BD.csv\"");
                        } catch(Exception e) {
                            System.out.println("Erro");
                        }
                        menu();
                    break;
            case '2': menuCRUD(); break;
            case '3': menuOrdenacao(); break;
            case '4': menuIndexacao(); break;
            case '5': menuCompactacao(); break;
            default: break;
        }
    }
    private static void menuCRUD(){
        Book b;
        char op;
        int id;
        System.out.println("\t <<<<<<<<<<<<<<<<<<<<<< >>>>>>>>>>>>>>>>>>>>>\n");
        System.out.println("1. Adicionar registro");
        System.out.println("2. Ler registro");
        System.out.println("3. Atualizar registro");
        System.out.println("4. Remover registro");
        System.out.println("5. Voltar\n");

        op = sc.next().charAt(0);
        try {
            switch(op) {
                case '1':
                    b = pedirBook();
                    System.out.println("Criado: \n" + Book.create(b));
                    menuCRUD();
                    break;
                case '2': 
                    System.out.println("Id desejado: ");
                    id = sc.nextInt();
                    b = Book.read(id);
                    System.out.println((b==null)? "Id não encontrado" : b);
                    menuCRUD();
                    break;
                case '3': 
                    System.out.println("Id desejado: ");
                    id = sc.nextInt(); 
                    b = Book.read(id);
                    if(b != null) {
                        b = pedirBook(b);
                        System.out.println("Atualizado: \n" + Book.update(id, b));
                    } else {
                        System.out.println("Id não encontrado");
                    }
                    menuCRUD();
                    break;
                case '4': 
                    System.out.println("Id desejado: ");
                    id = sc.nextInt();
                    System.out.println("Apagado: \n" + Book.delete(id));
                    menuCRUD();
                    break;
                case '5':
                    menu();
                    break;
                default: break;
            }
        } catch (FileNotFoundException e) {
            System.out.println("Arquivo não encontrado");
        } catch (Exception e) {
            System.out.println("Erro");
        }
    }
    private static void menuOrdenacao(){
        char op;
        int n; // quantidade de caminhos
        int m; // quantidade de registros
        System.out.println("\t <<<<<<<<<<<<<<<<<<<<<< Ordenação >>>>>>>>>>>>>>>>>>>>>\n");
        System.out.println("1. Comum");
        System.out.println("2. Blocos de tamanho variável");
        System.out.println("3. Seleção por substituição");
        System.out.println("4. Voltar\n");

        op = sc.next().charAt(0);
        switch(op) {
            case '1': 
                System.out.print("Quantidade de caminhos: ");
                n = sc.nextInt();
                System.out.println("Quantidade de registros a serem lidos por passada: ");
                m = sc.nextInt();
                try {
                    System.out.println("Aguarde...");
                    OrdenacaoExterna.intercalacaoComum(n, m);
                    System.out.println("Ordenação completa: nome do arquivo \"dadosOrdenadoComum.bd\"");
                } catch(Exception e) {
                    System.out.println("!! Erro !! " + e.getStackTrace());
                }
                menuOrdenacao();
                break;
            case '2':
                System.out.print("Quantidade de caminhos: ");
                n = sc.nextInt();
                System.out.println("Quantidade de registros a serem lidos por passada: ");
                m = sc.nextInt();
                try {
                    System.out.println("Aguarde...");
                    OrdenacaoExterna.intercalacaoVariavel(n, m);
                    System.out.println("Ordenação completa: nome do arquivo \"dadosOrdenadoVariavel.bd\"");
                } catch(Exception e) {
                    System.out.println("!! Erro !! " + e.getStackTrace());
                }
                menuOrdenacao();
                break;
            case '3': 
                System.out.print("Quantidade de caminhos: ");
                n = sc.nextInt();
                System.out.println("Quantidade de registros a serem lidos por passada: ");
                m = sc.nextInt();
                try {
                    System.out.println("Aguarde...");
                    OrdenacaoExterna.intercalacaoSubstituicao(n, m);
                    System.out.println("Ordenação completa: nome do arquivo \"dadosOrdenadoSubstituicao.bd\"");
                } catch(Exception e) {
                    System.out.println("!! Erro !! " + e.getStackTrace());
                }
                menuOrdenacao();
                break;
            case '4': menu(); break;
            default: break;
        }
    }
    private static void menuIndexacao(){
        char op = ' ';
        String pesquisa;
        long time = 0;
        ArrayList<Book> resultado = new ArrayList<>();
        int id;
        try {
            RandomAccessFile arq = new RandomAccessFile("arvoreB", "r");
            arq.close();

            System.out.println("\t <<<<<<<<<<<<<<<<<<<<<< >>>>>>>>>>>>>>>>>>>>>\n");
            System.out.println("1. Leitura em Arvore B");
            System.out.println("2. Leitura em Hashing");
            System.out.println("3. Leitura em Lista Invertida: gênero e título");
            System.out.println("4. Leitura em Lista Invertida: gênero");
            System.out.println("5. Leitura em Lista Invertida: título");
            System.out.println("6. Voltar\n");
            System.out.println("\n(aperte qualquer outra tecla para sair)\n");
    
            op = sc.next().charAt(0);
        } catch (Exception e) {
            System.out.println("\n !!!!! Índices não inicializados !!!!! \n");
        } 
        sc.nextLine();

        if(op == '1') {
            System.out.print("Id: ");
            id = sc.nextInt();
            try {
                time = System.currentTimeMillis();
                resultado.add(ArvoreB.read(id));
                time = System.currentTimeMillis() - time;
            } catch (Exception e) {
                System.out.println("Erro na leitura do arquivo 'arvoreB'");
            }
            
        } else if(op == '2') {
            System.out.print("Id: ");
            id = sc.nextInt();
            try {
                time = System.currentTimeMillis();
                resultado.add(Hashing.read(id));
                time = System.currentTimeMillis() - time;
            } catch (Exception e) {
                System.out.println("Erro na leitura do arquivo 'diretorioHash' e/ou 'hshingEstendido'");
            }

        } else if (op == '3' || op == '4' || op == '5') {
            System.out.print("Pesquisa: ");
            pesquisa = sc.nextLine();
            try {
                if(op == '3') {
                    time = System.currentTimeMillis();
                    resultado = ListaInvertida.read(pesquisa);
                } else if(op == '4') {
                    time = System.currentTimeMillis();
                    resultado = ListaInvertida.read(pesquisa, (short)1);
                } else {
                    time = System.currentTimeMillis();
                    resultado = ListaInvertida.read(pesquisa, (short)0);
                }
                time = System.currentTimeMillis() - time;
            } catch (Exception e) {
                System.out.println("Erro na leitura do arquivo 'indiceGenres' e/ou 'indiceTitle'");
            }

        } else if (op == ' ' || op == '6') {
            menu();
        }

        if(op == '1' || op == '2' || op == '3' || op == '4' || op == '5') {
            System.out.println("\t\t <<<< Resultado >>>> ");
            for (Book b : resultado) {
                System.out.println(" * " + b);
            }
            System.out.println("Tempo de busca: " + (double)time/1000 + "s");
            menuIndexacao();
        }
    }
    private static void menuCompactacao() {
        char op;
        int versao; // versao do arquivo de descompactacao
        long time;

        System.out.println("\t <<<<<<<<<<<<<<<<<<<<<< Compactação >>>>>>>>>>>>>>>>>>>>>\n");
        System.out.println("1. Compactar Banco de Dados");
        System.out.println("2. Descompactar Banco de Dados");
        System.out.println("3. Trocar o Banco de Dados");
        System.out.println("4. Voltar\n");

        op = sc.next().charAt(0);
        switch(op) {
            case '1': 
                try {
                    time = System.currentTimeMillis();
                    LZW.compactar(bd);
                    time = System.currentTimeMillis() - time;
                    System.out.println("Compactação LZW concluída em: " + (double)time/1000 + "s");
                } catch (IOException e) {
                    System.out.println("Erro na leitura do arquivo em LZW");
                }
                try {
                    time = System.currentTimeMillis();
                    Huffman.compactar(bd);
                    time = System.currentTimeMillis() - time;
                    System.out.println("Compactação Huffman concluída em: " + (double)time/1000 + "s");
                } catch (IOException e) {
                    System.out.println("Erro na leitura do arquivo em Huffman");
                }
                menuCompactacao();
                break;

            case '2':
                System.out.println("Qual a versão? ");
                versao = sc.nextInt();
                try {
                    time = System.currentTimeMillis();
                    LZW.descompactar("BooksLZWCompressao" + versao + ".bd");
                    time = System.currentTimeMillis() - time;
                    System.out.println("Descompactação LZW concluída em: " + (double)time/1000 + "s");
                } catch (IOException e) {
                    System.out.println("Erro na descompactação do arquivo em LZW");
                }
                try {
                    time = System.currentTimeMillis();
                    Huffman.descompactar("BooksHuffmanCompressao" + versao + ".bd");
                    time = System.currentTimeMillis() - time;
                    System.out.println("Descompactação Huffman concluída em: " + (double)time/1000 + "s");
                } catch (IOException e) {
                    System.out.println("Erro na descompactação do arquivo em Huffman");
                }
                menuCompactacao();
                break;

            case '3': 
                File pasta = new File(System.getProperty("user.dir"));
                String[] bdPossiveis = new String[10];
                int quant = 0, arqIndice;
                if (pasta.exists() && pasta.isDirectory()) {
                    File[] arquivos = pasta.listFiles();
        
                    bdPossiveis[quant++] = "Books.bd";
                    if (arquivos != null) {
                        for (File arquivo : arquivos) {
                            if(arquivo.getName().contains("BooksHuffman") && !arquivo.getName().contains("Compressao")) {
                                bdPossiveis[quant++] = arquivo.getName();
                            } else if(arquivo.getName().contains("BooksLZW") && !arquivo.getName().contains("Compressao")) {
                                bdPossiveis[quant++] = arquivo.getName();
                            }
                        }
                    }

                    if(quant == 0) {
                        System.out.println("Não há nenhum arquivo descompactado");
                    } else {
                        System.out.println("0. Cancelar");
                        for(int i=0; i<quant; i++) {
                            System.out.print(i+1 + ". " + bdPossiveis[i]);
                            if(bdPossiveis[i].compareTo(bd) == 0) System.out.println("  (EM USO)");
                            else System.out.println();
                        }
                        System.out.println("Escolha o arquivo para substitução do BD atual: ");
                        arqIndice = sc.nextInt();
                        if(arqIndice == 0) menuCompactacao();
                        else if(arqIndice > 0 && arqIndice <= quant) {
                            bd = bdPossiveis[arqIndice-1];
                            Book.setBD(bdPossiveis[arqIndice-1]);
                            menu();
                        }
                    }
                } else {
                    System.out.println("Nenhum arquivo encontrado");
                }
                break;
            case '4': menu(); break;

            default: break;
        }
    }
    
    /**
     * Funcao que constroi um Book a partir dos input do usuario
     * 
     * @param todos = se havera alteracao em todos os atributos de Book (create)
     */
    private static Book pedirBook() {
        return pedirBook(new Book(), true);
    }
    private static Book pedirBook(Book b) {
        return pedirBook(b, false);
    }
    private static Book pedirBook(Book b, Boolean todos) {
        String s;

        if(todos) {
            System.out.println("\nPressione enter para campos vazios");
        } else {
            System.out.println(b);
            System.out.println("\nPressione enter para os campos que não sofrerão alteração");
        }

        sc.nextLine(); 
        
        System.out.println("Título: ");
        s = sc.nextLine();
        if(s.length() > 0) b.SetTitle(s);
        
        System.out.println("Série: ");
        s = sc.nextLine();
        if(s.length() > 0) b.SetSeries(s);

        System.out.println("Autor: ");
        s = sc.nextLine();
        if(s.length() > 0) b.SetAuthor(s);

        System.out.println("Avaliação: ");
        s = sc.nextLine();
        if(s.length() > 0) b.SetRating((Float.parseFloat(s)));

        System.out.println("Língua: ");
        s = sc.nextLine();
        if(s.length() > 0) b.SetLanguage(s);

        System.out.println("ISBN: ");
        s = sc.nextLine();
        if(s.length() > 0) {
            char[] c = new char[13];
            for(int k = 12; k >= 0; k--) {
                if(k > s.length()-1) c[12-k] = '0';
                else c[12-k] = s.charAt(s.length() - k -1);
            }        
            b.SetIsbn(c);
        }        

        System.out.println("Quantidade de Gêneros: ");
        s = sc.nextLine();
        if(s.length() > 0) {
            int tmp = Integer.parseInt(s);
            String[] generos  = new String[tmp];
            if(tmp > 0) {
                System.out.println("Gêneros (um por linha): ");
                for(int i = 0; i < tmp; i++) generos[i] = sc.nextLine();
            }
            b.SetGenres(generos);
        }
        
        System.out.println("Formato do Livro: ");
        s = sc.nextLine();
        if(s.length() > 0) b.SetBookFormat(s);

        System.out.println("Edição: ");
        s = sc.nextLine();
        if(s.length() > 0) b.SetEdition(s);

        System.out.println("Quantidade de Páginas: ");
        s = sc.nextLine();
        if(s.length() > 0) b.SetPages(Integer.parseInt(s));

        System.out.println("Editora: ");
        s = sc.nextLine();
        if(s.length() > 0) b.SetPublisher(s);

        System.out.println("Data de Publicação: ");
        s = sc.nextLine();
        if(s.length() > 0) b.SetPublishDate(s);

        System.out.println("Primeira Data de Publicação (dd-mm-yyyy): ");
        s = sc.nextLine();

        if(s.length() > 0) {
            Pattern integerPattern = Pattern.compile("\\d+");
            Matcher matcher = integerPattern.matcher(s);

            int[] data = new int[3];
            int num = 0;
            while (matcher.find() && num < 3) {
                System.out.println(matcher.group());
                data[num++] = Integer.parseInt(matcher.group());
            }
            try {
                b.SetFirstPublishDate(LocalDate.of(data[2], data[1], data[0]));
            } catch (Exception e) {
                System.out.println("Data inválida! \nTente novamente no padrão (dia mês ano): ");
                try {
                    b.SetFirstPublishDate(LocalDate.of(data[2], data[1], data[0]));
                } catch (Exception e2) {
                    System.out.println("Data inválida!");
                }
            }
        }
                
        System.out.println("Porcentagem de gostei: ");
        s = sc.nextLine();
        if(s.length() > 0) b.SetLikedPercent(Short.parseShort(s));

        System.out.println("Preço: ");
        s = sc.nextLine();
        if(s.length() > 0) b.SetPrice(Float.parseFloat(s));
        
        return b;
    }

}
