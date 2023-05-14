import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LZW {
    private static int versao = 1;

    private static String byteToString(Byte b) {
        String bString = "";
        for(int i=7; i>=0; i--) {
            bString += b >> i & 1;
        }
        return bString;
    }
    // Criar dicionario
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

    // Iniciar arq compactado 
    private static void dicionarioParaArquivo(ArrayList<String> dicionario, RandomAccessFile file) throws IOException {
        file.writeInt(dicionario.size());

        for(int i=0; i<dicionario.size(); i++) {
            file.writeByte(Byte.parseByte(dicionario.get(i), 2));
        }
    }

    /* Compactacao */
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
        
        arqRead.close();
        arqWrite.close();
        return true;
    }

    /* Descompactacao */
    public static void descompactar(String arqNome) {
        
    }
}
