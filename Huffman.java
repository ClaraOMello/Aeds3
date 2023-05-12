import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class Huffman {

    /**
     * Inicialização para compactação do arquivo
     * 
     * @param arq nome do arquivo a ser compactado
     * @return HashMap com todos os simbolos presentes no arquivo e suas representacoes em binario
     * @throws IOException
     */
    private static HashMap<Byte, String> inicializar(String arq) throws IOException {
        HashMap<Byte, Integer> frequencia = frequenciaCaracteres(arq);

        HashMap<Byte, String> arvore = simboloToBits(construirArvore(frequencia));

        return arvore;
    }

    /**
     * Calculo da frequencia de cada simbolo
     * 
     * @param arq nome do arquivo a ser compactado
     * @return HashMap com todos os simbolos presentes no arquivo e suas frequencias no texto
     * @throws IOException
     */
    private static HashMap<Byte, Integer> frequenciaCaracteres(String arq) throws IOException {
        HashMap<Byte, Integer> frequencia = new HashMap<>(256);
        RandomAccessFile file;
        byte bits;
        try {
            file = new RandomAccessFile(arq, "r");
        } catch(FileNotFoundException e) {
            System.out.println("Arquivo de leitura para criação do HashMap não encontrado");
            return null;
        }
        
        while(file.getFilePointer() < file.length()) {
            bits = file.readByte();
            
            if(frequencia.containsKey(bits)) {
                frequencia.replace(bits, frequencia.get(bits)+1);
            } else {
                frequencia.put(bits, 1);
            }
        }
        file.close();
        return frequencia;
    }
    
    /**
     * Construcao da arvore de Huffman
     * @param frequencia HashMap com as frequencias dos simbolos
     * @return raiz da arvore
     */
    private static NoHuffman construirArvore(HashMap<Byte, Integer> frequencia) {
        NoHuffman raiz = null;

        // Passar hash para Lista
        ArrayList<NoHuffman> lista = new ArrayList<>();
        for(Map.Entry<Byte, Integer> elem : frequencia.entrySet()) {
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

    /**
     * Definicao das representacoes de cada simbolo do texto
     * @param raiz da arvore de Huffman
     * @return HashMap com os simbolos do texto e suas representacoes
     */
    private static HashMap<Byte, String> simboloToBits(NoHuffman raiz) {
        return simboloToBits(raiz, "", new HashMap<Byte, String>(256));
    }
    private static HashMap<Byte, String> simboloToBits(NoHuffman no, String codigo, HashMap<Byte, String> arvore) {
        if(no.ehFolha()) {
            arvore.put(no.getSimbolo(), codigo);
        } else {
            simboloToBits(no.esq, codigo + "0", arvore);
            simboloToBits(no.dir, codigo + "1", arvore);
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
    private byte simbolo;
    private int frequencia;
    NoHuffman(byte simbolo, int frequencia) {
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
    public byte getSimbolo() { return simbolo; }
}
