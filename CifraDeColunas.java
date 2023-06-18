import java.util.Arrays;

public class CifraDeColunas {
    static String chave = "CHAVE";
    public static void main(String[] args) {
        String padrao = "PROVA NA SEGUNDA FEIRA";
        String s = cifrar(padrao, chave);
        System.out.println(s);
        System.out.println(descifrar(s, chave));
    }

    public String getChave() {
        return chave;
    }
    public boolean setChave(String chave) {
        boolean repete = (chave.length() > 0) ? false : true;
        for (int i = 0; i < chave.length() && !repete; i++) {
            for (int j = i+1; j < chave.length() && !repete; j++) {
                if(chave.charAt(i) == chave.charAt(j)) repete = true;
            }
        }
        if(!repete) CifraDeColunas.chave = chave;
        return !repete;
    }

    private static int[] ordemChave(String chave) {
        int[] ordem = new int[chave.length()];
        
        char[] caracteres = new char[chave.length()];
        chave.toUpperCase().getChars(0, chave.length(), caracteres, 0);

        char[] caracteresOrdenados = Arrays.copyOf(caracteres, caracteres.length);
        Arrays.sort(caracteresOrdenados);

        for (int i = 0; i < caracteres.length; i++) {
            int posicao = Arrays.binarySearch(caracteresOrdenados, caracteres[i]);
            ordem[posicao] = i;
        }

        return ordem;
    }
    
    public static String cifrar(String texto) {
        return cifrar(texto, chave);
    }
    private static String cifrar(String texto, String chave) {
        StringBuilder cifra = new StringBuilder("");
        int[] ordem = ordemChave(chave);
        char[][] tabela = new char[(int) Math.ceil((double) texto.length()/chave.length())][chave.length()];
        int pos = 0;
        int vazio = 0;

        for (int linha = 0; linha < tabela.length; linha++) {
            for (int coluna = 0; coluna < chave.length() && pos < texto.length(); coluna++) {
                tabela[linha][coluna] = texto.charAt(pos++);
            }
        }        

        for (int coluna = 0; coluna < chave.length(); coluna++) {
            /* 
             * Limita a escrita da cifra somente com caracteres validos
             * os espacos vazios que se tem com o "excesso" da tabela nao
             * sao considerados --> tamanho da tabela = tabela.length*chave.length()
             * 
             * Os espacos vazios estao nas ultimas colunas --> tamanho da tabela - pos -1
             * (tamanho da tabela - pos) retorna a quantidade de colunas com espacos vazios,
             * ja o (-1) retorna a posicao dessas colunas
             */
            if(ordem[coluna] >= chave.length()-((tabela.length*chave.length())-pos-1)) {
                vazio = 1;
            } else vazio = 0;

            for (int linha = 0; linha < tabela.length-vazio; linha++) {
                cifra.append(tabela[linha][ordem[coluna]]);
            }
        }
        return cifra.toString();
    }

    public static String descifrar(String cifra) {
        return descifrar(cifra, chave);
    }
    private static String descifrar(String cifra, String chave) {
        StringBuilder texto = new StringBuilder("");
        int[] ordem = ordemChave(chave);
        char[][] tabela = new char[(int) Math.ceil((double) cifra.length()/chave.length())][chave.length()];
        int quantLinha;
        int pos = 0;
        int vazios = (tabela.length*chave.length()) - cifra.length();

        for (int coluna = 0; coluna < chave.length(); coluna++) {
            if(ordem[coluna] >= chave.length()-vazios) {
                quantLinha = 1;
            } else quantLinha = 0;
            for (int linha = 0; linha < tabela.length - quantLinha; linha++) {
                tabela[linha][ordem[coluna]] = cifra.charAt(pos++);
            }   
        }

        for (int linha = 0; linha < tabela.length; linha++) {
            for (int coluna = 0; coluna < chave.length(); coluna++) {
                texto.append(tabela[linha][coluna]);
            }
        }

        return texto.toString();
    }
}
