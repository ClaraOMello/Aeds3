import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class Huffman {
    /*
     * Arvore binaria --> hashMap(caracter, representação)
     */
    public static void main(String[] args) throws Exception {
        String verifica = inicializar("teste.txt").toString();
        System.out.println(verifica);
        //System.out.println("Diferenças: " + verifica.compareTo("{A=1, B=011, C=001, D=000, R=010}")); //invalido
        
    }

    /* Criação da árvore */
    private static HashMap<Character, String> inicializar(String arq) throws IOException {
        HashMap<Character, Integer> frequencia = frequenciaCaracteres(arq);

        HashMap<Character, String> arvore = caracterToBits(construirArvore(frequencia));

        return arvore;
    }
    // Identificar caracteres e frequencia
    private static HashMap<Character, Integer> frequenciaCaracteres(String arq) throws IOException {
        HashMap<Character, Integer> frequencia = new HashMap<>(256);
        RandomAccessFile file;
        String linha;
        char caracter;
        try {
            file = new RandomAccessFile(arq, "r");
        } catch(FileNotFoundException e) {
            System.out.println("Arquivo de leitura para criação do HashMap não encontrado");
            return null;
        }
        
        frequencia.put('\n', -1);
        while(file.getFilePointer() < file.length()) {
            linha = file.readLine();
            frequencia.replace('\n', frequencia.get('\n')+1);
            for(int i=0; i<linha.length(); i++) {
                caracter = linha.charAt(i);
                if(frequencia.containsKey(caracter)) {
                    frequencia.replace(caracter, frequencia.get(caracter)+1);
                } else {
                    frequencia.put(caracter, 1);
                }
            } 
        }
        if(frequencia.get('\n') == 0) frequencia.remove('\n');
        file.close();
        return frequencia;
    }
    
    // Construir a arvore em si
    private static NoHuffman construirArvore(HashMap<Character, Integer> frequencia) {
        NoHuffman raiz = null;

        // Passar hash para Lista
        ArrayList<NoHuffman> lista = new ArrayList<>();
        for(Map.Entry<Character, Integer> elem : frequencia.entrySet()) {
            lista.add(new NoHuffman(elem.getKey(), elem.getValue()));
        }

        // Construir arvore
        while(lista.size() > 1) {
            lista.sort(Comparator.comparingInt(NoHuffman::getFrequencia));
            raiz = new NoHuffman(lista.get(0), lista.get(1), lista.get(0).getFrequencia() + lista.get(1).getFrequencia());
            lista.remove(1);
            lista.remove(0);
            lista.add(raiz);
        }
        return raiz;
    }

    // Determinar os bits que representam cada caracter
    private static HashMap<Character, String> caracterToBits(NoHuffman raiz) {
        return caracterToBits(raiz, "", new HashMap<Character, String>(256));
    }
    private static HashMap<Character, String> caracterToBits(NoHuffman no, String codigo, HashMap<Character, String> arvore) {
        if(no.ehFolha()) {
            arvore.put(no.getSimbolo(), codigo);
        } else {
            caracterToBits(no.esq, codigo + "0", arvore);
            caracterToBits(no.dir, codigo + "1", arvore);
        }
        
        return arvore;
    }

    /* Compactação */
    public static void compactar(String arqNome) {

    }

    /* Descompactação */
    public static void descompactar(String arqNome) {

    }
}

class NoHuffman {
    NoHuffman esq;
    NoHuffman dir;
    private boolean folha;
    private char simbolo;
    private int frequencia;
    NoHuffman(char simbolo, int frequencia) {
        esq = null;
        dir = null;
        folha = true;
        this.simbolo = simbolo;
        this.frequencia = frequencia;
    }
    NoHuffman(NoHuffman esq, NoHuffman dir, int frequencia) {
        this.esq = esq;
        this.dir = dir;
        folha = false;
        this.frequencia = frequencia;
    }
    public String toString() {
        return simbolo + ": " + frequencia + " ";
    }
    public boolean ehFolha() { return folha; }
    public int getFrequencia() { return frequencia; }
    public char getSimbolo() { return simbolo; }
}
