import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;

public class BoyerMoore {
    public static int encontrar(String padrao, RandomAccessFile arq) {
        int encontrados = 0, comparacoes = 0;

        return encontrados;

    }

    protected static int[] sufixoBom(String padrao) {
        int[] deslocamento = new int[padrao.length()];
        String sufixo;
        int pos;
        deslocamento[padrao.length()-1] = 1;

        for (int i = deslocamento.length-2; i >= 0; i--) {
            sufixo = padrao.substring(i+1, deslocamento.length);
            pos = infixoPrefixo(padrao, sufixo);
            /* existe o sufixo como infixo, porem antecedido de algo diferente */
            if(pos == -1) {
                pos = prefixoPartes(padrao, sufixo);
                if(pos == -1) {
                    deslocamento[i] = padrao.length();
                } else {
                    deslocamento[i] = i+1+pos;
                }
            } else {
                deslocamento[i] = i+1-pos;
            }
        }

        return deslocamento;
    }

    /**
     * Primeiro e Segundo caso do Sufixo Bom:
     * Dado um sufixo, encontra um possivel infixo no padrao.
     * Caso nao tenha infixo, verifica a possibilidade de todo o sufixo ser prefixo
     * @param padrao
     * @param sufixo
     * @return posicao do padrao que comeca o infixo
     */
    private static int infixoPrefixo(String padrao, String sufixo) {
        int posicao = -1, sufixoPos; // padrao nao eh infixo
        for (int i = padrao.length()-1-sufixo.length(); i >= 0; i--) {
            sufixoPos = 0;

            /* encontra onde sufixo se repete */
            while(sufixoPos < sufixo.length() && sufixo.charAt(sufixoPos) == padrao.charAt(i+sufixoPos)) {
                sufixoPos++;
            }

            if(sufixoPos == sufixo.length()) {
                /* 
                * caso seja infixo, verificar se carater anterior eh diferente ou
                * caso o sufixo inteiro seja prefixo
                */
                if(i == 0 || (i>0 && padrao.charAt(i-1) != padrao.charAt(padrao.length()-sufixo.length()-1))) {
                    posicao = i;
                    i = -1;
                }
            }
        }
        return posicao; // posicao onde comeca o infixo
    }

    /**
     * Terceiro caso do Sufixo Bom:
     * Verifica a possibilidade de haver um pedaco do sufixo que eh prefixo do padrao
     * @param padrao
     * @param sufixo
     * @return posicao do sufixo em que foi encontrado um prefixo
     */
    private static int prefixoPartes(String padrao, String sufixo) {
        int posicao = -1, sufixoPos; // nenhuma parte do sufixo eh prefixo
        for (int i = 1; i < sufixo.length(); i++) {
            sufixoPos = i;
            if(sufixo.charAt(i) == padrao.charAt(0)) {
                while(sufixoPos < sufixo.length() && sufixo.charAt(sufixoPos) == padrao.charAt(sufixoPos-i)) {
                    sufixoPos++;
                }
                if(sufixoPos == sufixo.length()) {
                    posicao = i;
                    i = sufixo.length();
                }
            }
        }
        return posicao;
    }
    
    protected static HashMap<Character, Integer> caraterRuim(String padrao) {
        HashMap<Character, Integer> hash = new HashMap<>();
        for (int i = padrao.length()-2; i >= 0; i--) {
            if(!hash.containsKey(padrao.charAt(i))) {
                hash.put(padrao.charAt(i), i);
            }
        }
        return hash;
    }

    private static String arqToString(RandomAccessFile arq) throws IOException {
        StringBuilder s = new StringBuilder();
        while(arq.getFilePointer() < arq.length()) {
            s.append(arq.readLine());
        }
        return s.toString();
    }
}
