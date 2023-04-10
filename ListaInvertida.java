import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

/*
 * Arquivo dos indices para Lista Invertida: 
 *  - Gêneros: indiceGenres
 *  - Título: indiceTitle
 */
public class ListaInvertida {
    static String bd = "Books.bd";
    static short TAM_LONG = Long.BYTES;
    
    public static void main(String[] args) throws Exception {
        //create();
        //escreverLista(new RandomAccessFile("indiceTitle", "rw"));
        read("fiction fantasy");
    }

    public static void create() throws Exception {
        long posReg; // posicao do registro no bd
        Book b;
        boolean lapide;
        int len;
        byte[] ba;
        RandomAccessFile dados = new RandomAccessFile(bd, "r");

        RandomAccessFile indiceTitulo = new RandomAccessFile("indiceTitle", "rw");
        RandomAccessFile indiceGenero = new RandomAccessFile("indiceGenres", "rw");
        /* Inicialiazacao dos arquivos indice */
        indiceTitulo.setLength(0);
        indiceGenero.setLength(0);

        indiceTitulo.writeLong(-1); // endereco dos lixos
        indiceGenero.writeLong(-1); 

        indiceTitulo.writeLong(-1); // endereco da primeira palavra
        indiceGenero.writeLong(-1);

        indiceTitulo.close();
        indiceGenero.close();

        dados.readInt(); // pular contador inicial

        while(dados.getFilePointer() < dados.length()) {
            /* Ler registro no BD */
            lapide = dados.readBoolean();
            posReg = dados.getFilePointer();
            len = dados.readInt();
            if(lapide) {
                ba = new byte[len];
                dados.read(ba);
                b = new Book();
                b.fromByteArray(ba);
                
                /* Adicionar registro na arvore */
                add(b, posReg);

            } else {
                dados.skipBytes(len);
            }
        }
        dados.close();

        System.out.println("Listas invertidas criadas");
    }  

    /*
     * Adicionar um novo Book ao indice
     * @param   posRg = posicao do Book no arquivo
     */
    public static void add(Book b, long posRg) throws Exception {
        String[] generos, titulo = null;

        if(b.GetLanguage().equals("Portuguese")) titulo = tratamento(b.GetTitle(), (short) 0);
        else if(b.GetLanguage().equals("English")) titulo = tratamento(b.GetTitle(), (short) 1);
        
        generos = tratamentoGenres(b.GetGenres());
        
        add(posRg, titulo, new RandomAccessFile("indiceTitle", "rw"));
        add(posRg, generos, new RandomAccessFile("indiceGenres", "rw"));
    }
    private static void add(long posRg, String[] palavras, RandomAccessFile indice) throws Exception {
        long plvrAtual, proxRg, endLixos, plvrAnt, antRg, nRg;
        RegistroLista rg, tmpRg;
        Palavra plvr, nPlvr;
        byte[] ba, lixos = new byte[0];
        boolean achou;

        endLixos = indice.readLong();
        plvrAtual = indice.readLong();
        rg = new RegistroLista(posRg);

        for(int i=0; palavras != null && i<palavras.length; i++) {
            if(plvrAtual == -1) { // caso indice esteja vazio
                plvr = new Palavra(palavras[i]);
                plvrAtual = indice.length();

                indice.seek(TAM_LONG);
                indice.writeLong(plvrAtual);

                // escrever palavra
                ba = plvr.toByteArray();
                indice.writeInt(ba.length);
                indice.write(ba);

                plvr.endRg = indice.getFilePointer();

                // escrever registro
                indice.write(rg.toByteArray());

                // reescrever palavra
                indice.seek(plvrAtual);
                indice.readInt();
                indice.write(plvr.toByteArray());
            } else {
                achou = false;
                indice.seek(plvrAtual);
                ba = new byte[indice.readInt()];
                indice.read(ba);
                plvr = new Palavra();
                plvr.fromByteArray(ba);
                plvrAnt = -1; // se == -1 quando for add palavra, deve-se alterar o endereco da primeira palavra no inicio do arq

                while(!achou && plvr.endProxPalavra > -1 && plvr.termo.compareToIgnoreCase(palavras[i]) <= 0) {
                    if(plvr.termo.equalsIgnoreCase(palavras[i])) achou = true;
                    else {
                        plvrAnt = plvrAtual;
                        plvrAtual = plvr.endProxPalavra;

                        indice.seek(plvrAtual);
                        ba = new byte[indice.readInt()];
                        indice.read(ba);
                        plvr.fromByteArray(ba);
                    }
                }

                if(achou) {
                    proxRg = plvr.endRg;
                    tmpRg = new RegistroLista();
                    antRg = -1;

                    while(proxRg > -1) {
                        indice.seek(proxRg);

                        ba = new byte[2*TAM_LONG];
                        indice.read(ba);
                        tmpRg.fromByteArray(ba);

                        antRg = proxRg; // armazenar endereco do rg
                        proxRg = tmpRg.endProxRg;
                    }

                    if(endLixos > -1) {
                        indice.seek(indice.length()-TAM_LONG);
                        nRg = indice.readLong();
                        indice.setLength(indice.length()-TAM_LONG);

                    } else {
                        nRg = indice.length();
                    }

                    if(antRg > -1) {
                        tmpRg.endProxRg = nRg;

                        indice.seek(antRg);
                        indice.write(tmpRg.toByteArray());
                    } else {
                        plvr.endRg = nRg;
                        indice.seek(plvrAtual);
                        indice.skipBytes(Integer.BYTES);
                        indice.write(plvr.toByteArray());
                    }
                    
                    indice.seek(nRg);
                    indice.write(rg.toByteArray());

                } else {
                    nPlvr = new Palavra(palavras[i]);
                        
                    if(endLixos > -1) {
                        lixos = new byte[(int)(indice.length()- endLixos)];
                        indice.seek(endLixos);
                        indice.read(lixos);
                        indice.setLength(indice.length()- endLixos);
                    }
                    
                    if(plvrAnt == -1 && palavras[i].compareToIgnoreCase(plvr.termo) < 0) {
                        nPlvr.endProxPalavra = plvrAtual;
                        
                        /* Atualizar primeira palavra da ordem alfabetica do indice */
                        indice.seek(TAM_LONG);
                        indice.writeLong(indice.length());

                    } else if (palavras[i].compareToIgnoreCase(plvr.termo) < 0) {
                        indice.seek(plvrAnt);
                        ba = new byte[indice.readInt()];
                        indice.read(ba);
                        plvr.fromByteArray(ba);

                        nPlvr.endProxPalavra = plvrAtual;

                        plvr.endProxPalavra = indice.length();

                        indice.seek(plvrAnt);
                        indice.writeInt(ba.length);
                        indice.write(plvr.toByteArray());

                    } else { // plvr.endProxPlvr == -1
                        plvr.endProxPalavra = indice.length();
                        indice.seek(plvrAtual);
                        indice.readInt();
                        indice.write(plvr.toByteArray());

                    }

                    /* Escrever nova Palavra */
                    plvrAtual = indice.length();
                    indice.seek(plvrAtual);
                    ba = nPlvr.toByteArray();
                    indice.writeInt(ba.length);
                    indice.write(ba);

                    nPlvr.endRg = indice.getFilePointer();

                    // escrever registro
                    indice.write(rg.toByteArray());

                    // reescrever palavra
                    indice.seek(plvrAtual);
                    indice.readInt();
                    indice.write(nPlvr.toByteArray());
                        
                    if(endLixos > -1) {
                        indice.seek(0);
                        indice.writeLong(indice.length());
                        indice.seek(indice.length());
                        indice.write(lixos);
                    }
                }
            }

        }

        indice.close();

    }

    /*
     * Metodos para tratamento das palavras 
     * 
     * @return  palavras relevantes em ordem alfabetica
     */
    /* Separacao das palavras no titulo */
    private static String[] tratamento(String s, String[] remove) {
        String[] palavras = s.split(" ");
        char[] simbolos = {'\\', '\'', ':', '(', ')', '{', '}', ',', ';', '[', ']'};
        String[] selecionadas;
        String tmp;
        short quant = 0;

        for(int j = 0; j<palavras.length; j++) {
            if(palavras[j].charAt(palavras[j].length()-1) == 's') {
                palavras[j] = palavras[j].substring(0, palavras[j].length()-1);
            }
            for(int n=0; n<simbolos.length; n++) {
                if(palavras[j].charAt(palavras[j].length()-1) == simbolos[n]) palavras[j] = palavras[j].substring(0, palavras[j].length()-1);
                else if(palavras[j].charAt(0) == simbolos[n]) palavras[j] = palavras[j].substring(1, palavras[j].length());
            }
        }

        for(int j = 0; j<palavras.length; j++) {
            for(int i = 0; i < remove.length; i++) {
                if(remove[i].equalsIgnoreCase(palavras[j])) {
                    palavras[j]="";
                    quant++;
                    i=remove.length;
                }
            }
        }

        selecionadas = new String[palavras.length - quant];
        for(int i=0, k=0; i <palavras.length; i++) {
            if(palavras[i].equals("")) ;
            else selecionadas[k++] = palavras[i];
        }

        // ordenacao
        for(int i=1; i<selecionadas.length; i++) {
            for(int j=i-1; j>=0; j--) {
                if(selecionadas[j+1].compareToIgnoreCase(selecionadas[j]) < 0) {
                    //swap
                    tmp = selecionadas[j+1];
                    selecionadas[j+1] = selecionadas[j];
                    selecionadas[j] = tmp;
                }
            }
        }

        return selecionadas;
    }
    
    /* Tratamento das palavras do genero */
    private static String[] tratamentoGenres(String[] palavras) {
        String[] tmp;
        ArrayList<String> tratadas = new ArrayList<>();

        // separar generos compostos
        for(int i=0; i<palavras.length; i++) {
            tmp = palavras[i].split(" ");
            for(int k=0; k<tmp.length; k++) tratadas.add(tmp[k]);
        }

        tmp = new String[1];
        tratadas.sort(null);
        tmp[0] = tratadas.get(0);

        // tirar o 's' do final de todas as palavras e as palavras repetidas do array
        if(tmp[0].charAt(tmp[0].length()-1) == 's') tratadas.set(0, tmp[0].substring(0, tmp[0].length()-1));
        for(int i=1; i<tratadas.size(); i++) {
            tmp[0] = tratadas.get(i);
            if(tmp[0].charAt(tmp[0].length()-1) == 's') tratadas.set(i, tmp[0].substring(0, tmp[0].length()-1));
            if(tratadas.get(i).equalsIgnoreCase("for") || tratadas.get(i).equalsIgnoreCase("of")) tratadas.remove(i--);
            else {
                for(int j=0; j<i; j++) {
                    if(tratadas.get(i).equalsIgnoreCase(tratadas.get(j))) {
                        tratadas.remove(j);
                        i--;
                    }
                }
            }
        }

        tmp = new String[tratadas.size()];
        tratadas.toArray(tmp);
        return tmp;
    }
    
    /* 
     * Definicao das palavras que serao as stopWords
     * @param   ling = 0 -> Portugues
     *               = 1 -> Ingles
     *               !(0 || 1) -> Portugues e Ingles
    */
    private static String[] tratamento(String s, short ling) {
        String[] remove;
        
        // StopWords (nao possuem o 's' no final)
        if(ling == 0) {
            String[] descarte = {"a", "à", "ao", "aquela", "aquele", "aquilo", "até", "com", "como", "da", "de", "dela", "dele", "do", "e", "é", "ela", "ele", "em", "entre", "isso", "isto", "já", "lhe", "mai", "ma", "me", "mesmo", "meu", "minha", "muito", "na", "nem", "no", "nó", "nossa", "nosso", "numa", "o", "ou", "para", "pela", "pelo", "por", "qual", "quando", "que", "quem", "são", "se", "seja", "sejam", "sem", "ser", "será", "serão", "seria", "seriam", "seu", "só", "somo", "sou", "sua", "também", "te", "tem", "têm", "tenho", "ter", "teu", "tinha", "tu", "tua", "um", "uma", "você", "&"};
            remove = descarte;
        } else if(ling == 1) {
            String[] discard = {"a", "about", "above", "after", "again", "against", "all", "am", "an", "and", "any", "are", "at", "be", "because", "been", "before", "being", "below", "between", "both", "but", "by", "can", "da", "did", "do", "doe", "doing", "don", "down", "during", "each", "few", "for", "from", "further", "had", "ha", "have", "having", "he", "her", "here", "herself", "him", "himself", "how", "i", "if", "in", "into", "it", "itself", "just", "le", "me", "more", "most", "my", "myself", "no", "nor", "not", "now", "of", "off", "on", "once", "only", "or", "other", "our", "ourselve", "out", "over", "own", "", "same", "she", "should", "so", "some", "such", "t", "than", "that", "the", "their", "their", "them", "themselve", "then", "there", "these", "they", "thi", "those", "through", "to", "too", "under", "until", "up", "very", "wa", "we", "were", "what", "when", "where", "which", "while", "who", "whom", "why", "will", "with", "you", "your", "yourself", "yourselve", "&"};
            remove = discard;
        } else {
            String[] misto = {"a", "à", "ao", "aquela", "aquele", "aquilo", "até", "com", "como", "da", "de", "dela", "dele", "do", "e", "é", "ela", "ele", "em", "entre", "isso", "isto", "já", "lhe", "mai", "ma", "me", "mesmo", "meu", "minha", "muito", "na", "nem", "no", "nó", "nossa", "nosso", "numa", "o", "ou", "para", "pela", "pelo", "por", "qual", "quando", "que", "quem", "são", "se", "seja", "sejam", "sem", "ser", "será", "serão", "seria", "seriam", "seu", "só", "somo", "sou", "sua", "também", "te", "tem", "têm", "tenho", "ter", "teu", "tinha", "tu", "tua", "um", "uma", "você", "&", "about", "above", "after", "again", "against", "all", "am", "an", "and", "any", "are", "at", "be", "because", "been", "before", "being", "below", "between", "both", "but", "by", "can", "did", "do", "doe", "doing", "don", "down", "during", "each", "few", "for", "from", "further", "had", "ha", "have", "having", "he", "her", "here", "herself", "him", "himself", "how", "i", "if", "in", "into", "it", "itself", "just", "le", "me", "more", "most", "my", "myself", "no", "nor", "not", "now", "of", "off", "on", "once", "only", "or", "other", "our", "ourselve", "out", "over", "own", "", "same", "she", "should", "so", "some", "such", "t", "than", "that", "the", "their", "their", "them", "themselve", "then", "there", "these", "they", "thi", "those", "through", "to", "too", "under", "until", "up", "very", "wa", "we", "were", "what", "when", "where", "which", "while", "who", "whom", "why", "will", "with", "you", "your", "yourself", "yourselve"};
            remove = misto;
        }

        return tratamento(s, remove);
    }
    
    /*
     * Leitura de Books com base no input do usuario
     * @param   arq: 0 -> acessa somente o indiceTitle
     *                1 -> acessa somente o indiceGenres
     *                2 -> acessa ambos os indices
     */
    public static ArrayList<Book> read(String s) throws Exception {
        return read(tratamento(s, (short)-1), (short)2);
    }
    public static ArrayList<Book> read(String s, short arq) throws Exception {
        return read(tratamento(s, (short)-1), arq);
    }
    private static ArrayList<Book> read(String[] palavras, short arq) throws Exception {
        long proxPlvr, proxRg;
        Palavra p = new Palavra();
        RegistroLista rg = new RegistroLista();
        byte[] baPlvr, baRg;
        int pos = 0;
        Book b;
        boolean igual; // 
        
        RandomAccessFile dados = new RandomAccessFile(bd, "r");

        ArrayList<Book> books = new ArrayList<>();
        ArrayList<Long> registros = new ArrayList<>();
        RandomAccessFile indice;
        if(arq == 0) indice = new RandomAccessFile("indiceTitle", "r");
        else indice = new RandomAccessFile("indiceGenres", "r");

        do {
            indice.readLong();
            proxPlvr = indice.readLong();
            
            while(proxPlvr > -1 && pos < palavras.length) {
                indice.seek(proxPlvr);

                baPlvr = new byte[indice.readInt()];
                indice.read(baPlvr);
                p.fromByteArray(baPlvr);
                
                proxPlvr = p.endProxPalavra;
                
                if (p.termo.equalsIgnoreCase(palavras[pos])) {
                    proxRg = p.endRg;
                    while(proxRg > -1) {
                        indice.seek(proxRg);
    
                        baRg = new byte[2*TAM_LONG];
                        indice.read(baRg);
                        rg.fromByteArray(baRg);
                        
                        registros.add(rg.rg);

                        proxRg = rg.endProxRg;
                    }
                    pos++;
                } else if(p.termo.compareToIgnoreCase(palavras[pos]) > 0) {
                    pos++;
                    if (pos < palavras.length && p.termo.equalsIgnoreCase(palavras[pos])) {
                        proxRg = p.endRg;
                        while(proxRg > -1) {
                            indice.seek(proxRg);
        
                            baRg = new byte[2*TAM_LONG];
                            indice.read(baRg);
                            rg.fromByteArray(baRg);
                            
                            registros.add(rg.rg);
    
                            proxRg = rg.endProxRg;
                        }
                        pos++;
                    }
                }
            }

            /* Mudar o arquivo de leitura */
            if(arq >= 2) {
                arq++;
                indice = new RandomAccessFile("indiceTitle", "r");
                pos = 0;
            }
        } while(arq == 3);
        registros.sort(null);

        /* Selecionar intersecao dos registros */
        // caso a quantidade de resgistros encontrada seja pequena, nao havera selecao por intersecao
        for(int i=0; i<registros.size(); i++) {
            if(registros.size() <= 7) igual = true;
            else igual = false;

            for(int j=registros.size()-1; j>i; j--) {
                if(registros.get(i).longValue() == registros.get(j).longValue()) {
                    registros.remove(j);
                    igual = true;
                }
            }

            if(!igual) registros.remove(i--);
        }

        /* Armazenar Books */
        for (Long reg : registros) {
            b = new Book();
            dados.seek(reg.longValue());
            baRg = new byte[dados.readInt()];
            dados.read(baRg);
            b.fromByteArray(baRg);
            books.add(b);
        }

        indice.close();
        dados.close();
        return books;
    }

    public static void escreverLista(RandomAccessFile indice) throws IOException {
        long proxPlvr, proxRg;
        Palavra p = new Palavra();
        RegistroLista rg = new RegistroLista();
        byte[] baPlvr, baRg;

        indice.readLong();
        proxPlvr = indice.readLong();
        
        while(proxPlvr > -1) {
            indice.seek(proxPlvr);

            baPlvr = new byte[indice.readInt()];
            indice.read(baPlvr);
            p.fromByteArray(baPlvr);
            proxRg = p.endRg;
            
            System.out.print(p + "\t");

            while(proxRg > -1) {
                indice.seek(proxRg);

                baRg = new byte[2*TAM_LONG];
                indice.read(baRg);
                rg.fromByteArray(baRg);

                System.out.print( " --> " + rg );

                proxRg = rg.endProxRg;

            }
            System.out.println();
            proxPlvr = p.endProxPalavra;
        }

    }
}


class Palavra {
    String termo;
    long endRg;
    long endProxPalavra;

    Palavra(String termo) {
        this.termo = termo;
        endRg = -1;
        endProxPalavra = -1;
    }
    Palavra() {
        termo = "";
        endRg = -1;
        endProxPalavra = -1;
    }

    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeUTF(termo);
        dos.writeLong(endRg);
        dos.writeLong(endProxPalavra);

        return baos.toByteArray();
    }
    public void fromByteArray(byte[] ba) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);

        termo = dis.readUTF();
        endRg = dis.readLong();
        endProxPalavra = dis.readLong();        
    }

    public Palavra clone() { 
        Palavra p = new Palavra(termo);
        p.endRg = endRg;
        p.endProxPalavra = endProxPalavra;

        return p;
    }

    public String toString() {
        return termo + " | " +endRg+ " | " +endProxPalavra;
    }
}


class RegistroLista {
    long rg; // posicao do Book no bd
    long endProxRg;

    RegistroLista(){
        rg=-1;
        endProxRg=-1;
    }
    RegistroLista(long pos) {
        rg = pos;
        endProxRg = -1;
    }
    RegistroLista(long pos, long prox) {
        rg = pos;
        endProxRg = prox;
    }

    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeLong(rg);
        dos.writeLong(endProxRg);

        return baos.toByteArray();
    }
    public void fromByteArray(byte[] ba) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);

        rg = dis.readLong();
        endProxRg = dis.readLong();        
    }

    public String toString() {
        String s = "";
        s += rg+ " | " +endProxRg;
        return s;
    }
}

