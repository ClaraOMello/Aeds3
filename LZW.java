import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class LZW {
    private static int versao = 1;
    public static void main(String[] args) throws Exception {
        String arq = "teste";
        RandomAccessFile raf = new RandomAccessFile("teste.bd", "rw");
        raf.setLength(0);
        for(int i =0; i<9; i++) {
            raf.writeByte(12);
            raf.writeByte(121);
            raf.writeByte(1);
            raf.writeByte(90);
        }
        raf.close();

        compactar(arq + ".bd");
        descompactar(arq + "LZWCompressao1.bd");
    }

    private static String byteToString(Byte b) {
        String bString = "";
        for(int i=7; i>=0; i--) {
            bString += b >> i & 1;
        }
        return bString;
    }
    
    /**
     * Inicializa o dicionario do arquivo a ser compactado
     * @param file
     * @return lista de todos os bytes presentes em file
     * @throws IOException
     */
    private static ArrayList<String> dicionarioInicial(RandomAccessFile file) throws IOException {
        ArrayList<String> dicionario = new ArrayList<>();
        byte bits;
        String bString;
        
        while(file.getFilePointer() < file.length()) {
            bits = file.readByte();
            bString = byteToString(bits);
            
            if(!dicionario.contains(bString)) {
                dicionario.add(bString);
            }
        }
        return dicionario;
    }

    /**
     * Escreve o dicionario inicial no arquivo de compactacao
     * @param dicionario
     * @param file arquivo com o texto compactado
     * @throws IOException
     */
    private static void dicionarioParaArquivo(ArrayList<String> dicionario, RandomAccessFile file) throws IOException {
        file.writeInt(dicionario.size());

        for(int i=0; i<dicionario.size(); i++) {
            file.writeByte(Byte.parseByte(dicionario.get(i), 2));
        }
    }

    private static ArrayList<String> arquivoParaDicionario(RandomAccessFile file) throws IOException {
        int tamanho = file.readInt();
        ArrayList<String> dicionario = new ArrayList<>(tamanho);

        for(int i=0; i<tamanho; i++) {
            dicionario.add(byteToString(file.readByte()));
        }
        
        return dicionario;
    }

    /**
     * Compactacao de arquivo
     * @param arq
     * @return false caso o arq nao seja encontrado
     * @throws IOException
     */
    public static boolean compactar(String arq) throws IOException {
        RandomAccessFile arqRead, arqWrite;
        String[] partes;
        ArrayList<String> dicionario;
        String prefixo = "";
        byte byteLido;
        int token = 0;

        try {
            arqRead = new RandomAccessFile(arq, "r");
        } catch(FileNotFoundException e) {
            System.out.println("Arquivo para compactação não encontrado");
            return false;
        }

        // Inicializar novo arquivo
        partes = arq.split("\\.");
        arq = partes[0] + "LZWCompressao" + versao++;
        arq += (partes.length > 1) ? "." + partes[1] : "";        
        arqWrite = new RandomAccessFile(arq, "rw");
        arqWrite.setLength(0);

        dicionario = dicionarioInicial(arqRead);
        dicionarioParaArquivo(dicionario, arqWrite);
        arqRead.seek(0);

        while(arqRead.getFilePointer() < arqRead.length()) {
            byteLido = arqRead.readByte();
            prefixo += byteToString(byteLido);
            if(dicionario.contains(prefixo)) {
                token = dicionario.indexOf(prefixo);
            } else {
                dicionario.add(prefixo);
                arqWrite.writeInt(token);
                prefixo = prefixo.substring(prefixo.length() - 8);
                token = dicionario.indexOf(prefixo);
            }
        }
        arqWrite.writeInt(token);
        System.out.println(dicionario);
        
        arqRead.close();
        arqWrite.close();
        return true;
    }

    /**
     * Descompactacao de arquivo
     * @param arq
     * @return false caso o arq nao seja encontrado
     * @throws IOException
     */
    public static boolean descompactar(String arq) throws IOException {
        RandomAccessFile arqRead, arqWrite;
        String[] partes;
        ArrayList<String> dicionario;
        String prefixo = "", escrita = "";
        int token = 0;

        try {
            arqRead = new RandomAccessFile(arq, "r");
        } catch(FileNotFoundException e) {
            System.out.println("Arquivo para compactação não encontrado");
            return false;
        }

        /* Inicializar arquivo de descompressao */
        partes = arq.split("Compressao");
        arq = partes[0] + ((partes.length > 1) ? partes[1] : "");
        arqWrite = new RandomAccessFile(arq, "rw");
        arqWrite.setLength(0);

        int cont = 0;
        /* Descompactacao */
        dicionario = arquivoParaDicionario(arqRead);
        while(arqRead.getFilePointer() < arqRead.length()) {
            token = arqRead.readInt();
            escrita = dicionario.get(token);

            for(int i=0; i<escrita.length(); i+=8) {
                arqWrite.writeByte(Byte.parseByte(escrita.substring(i, i+8), 2));
            } 
            
            prefixo += escrita.substring(0, 8);

            if(dicionario.contains(prefixo)) {
            } else {
                dicionario.add(prefixo);
                System.out.println(prefixo);
                prefixo = escrita;
            }
            //System.out.println(cont++ + " " +dicionario);
        }

        arqRead.close();
        arqWrite.close();
        return true;
    }
}
