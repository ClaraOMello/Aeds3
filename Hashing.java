import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

/*
 * Arquivo dos indices para Hashing Estendido: hashingEstendido
 */
public class Hashing {
    // 5% de 2000 - Base original possui mais de 50000 registros, mas espera-se uma base reduzida para criacao do indice
    static int TAM_MAX_N = 100;
    static String bd = "Books.bd";

    public static void main(String[] args) throws Exception{
        //create();
        //escreverHash();
        System.out.println(read(1));
    }
    
    public static void create() throws Exception {
        RandomAccessFile indice = new RandomAccessFile("hashingEstendido", "rw");
        RandomAccessFile diretorio = new RandomAccessFile("diretorioHash", "rw");
        RandomAccessFile dados = new RandomAccessFile(bd, "r");
        long posReg; // posicao do registro no bd
        Book b;
        boolean lapide;
        int len;
        byte[] ba;

        /* Inicializacao do indice e do diretorio (criacao dos dois primeiros buckets) */
        indice.setLength(0);
        diretorio.setLength(0);

        diretorio.writeInt(1); // profundidade global inicial

        diretorio.writeLong(indice.length()); // endereco do primeiro bucket
        indice.write((new Bucket(1)).toByteArray());
 
        diretorio.writeLong(indice.length()); // endereco do segundo bucket
        indice.write((new Bucket(1)).toByteArray());

        indice.close();
        diretorio.close();


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
                add(new Registro(b.GetId(), posReg));

            } else {
                dados.skipBytes(len);
            }
        }
        dados.close();

        System.out.println("Hashing criado");
    }

    public static void add(Registro rg) throws Exception {
        RandomAccessFile indice = new RandomAccessFile("hashingEstendido", "rw");
        RandomAccessFile diretorio = new RandomAccessFile("diretorioHash", "rw");
        
        long endBucket;
        byte[] ba;
        Bucket bkt = new Bucket();
        Bucket nBkt;
        long antB, antNB, atual, endBktDiretorio;
        int pGlobal;
        RegistroHash rh = new RegistroHash();
        
        diretorio.seek(0);
        pGlobal = diretorio.readInt();

        diretorio.seek(hash(rg.id, pGlobal));
        endBucket = diretorio.readLong();
        indice.seek(endBucket);
        ba = new byte[Bucket.sizeOf()];
        indice.read(ba);
        bkt.fromByteArray(ba);

        if(bkt.n < TAM_MAX_N) {
            bkt.n++;
            if(bkt.primeiroRg > -1) {
                atual = bkt.primeiroRg;
                antB = -1;

                while(atual > -1) {
                    indice.seek(atual);
                    ba = new byte[RegistroHash.sizeOf()];
                    indice.read(ba);
                    rh.fromByteArray(ba);

                    antB = atual;
                    atual = rh.proxRg;
                }

                rh.proxRg = indice.length();
                indice.seek(antB);
                indice.write(rh.toByteArray());

            } else {
                bkt.primeiroRg = indice.length();
            }
                
            /* Reescrever cabecalho de bucket */
            indice.seek(endBucket);
            indice.write(bkt.toByteArray());

            /* Escrever novo registro */
            rh = new RegistroHash(rg);
            indice.seek(indice.length());
            indice.write(rh.toByteArray());
        } else {
            nBkt = new Bucket(bkt.p + 1);
            
            diretorio.seek(hash(rg.id, bkt.p)); // garantir posicao no diretorio sem a mudanca da profundidade global (1/2)
            
            if(bkt.p >= pGlobal) {
                /* Aumentar o diretorio */
                diretorio.seek(0);
                diretorio.writeInt(pGlobal+1);

                long tmp;
                for(int i = 0; i<Math.pow(2,pGlobal); i++) {
                    diretorio.seek((i*Long.BYTES) + Integer.BYTES);
                    tmp = diretorio.readLong();
                    diretorio.seek(diretorio.length());
                    diretorio.writeLong(tmp);
                }
                
                diretorio.seek(hash(rg.id, pGlobal)); // garantir pos no diretorio sem a mudanca da prof global (2/2)
                pGlobal++;
            }

            /* Reorganizar ponteiros */
            while(diretorio.getFilePointer() < diretorio.length()) {
                diretorio.skipBytes((int) Math.pow(2, bkt.p) * Long.BYTES);
                diretorio.writeLong(indice.length()); // posicao do nBkt
                diretorio.skipBytes(((int) Math.pow(2, bkt.p)-1) * Long.BYTES);
            }
            bkt.p++;

            /* Redistribuir os elementos */
            atual = bkt.primeiroRg;
            antB = endBucket;
            antNB = indice.length(); // endereco do novo Bucket

            while(atual > -1) {
                indice.seek(atual);
                ba = new byte[RegistroHash.sizeOf()];
                indice.read(ba);
                rh.fromByteArray(ba);

                diretorio.seek(hash(rh.rg.id, pGlobal));
                endBktDiretorio = diretorio.readLong();

                if(endBktDiretorio == endBucket) { // registro continuara pertencente a bkt
                    if(antB == endBucket) { // alteracao a ser feita no cabecalho do bkt
                        bkt.primeiroRg = atual;
                        
                        antB = atual;
                        atual = rh.proxRg;

                    } else { // alteracao a ser feita no registro do endereco antB
                        indice.seek(antB);
                        indice.skipBytes(Registro.sizeOf());
                        indice.writeLong(atual);

                        antB = atual;
                        atual = rh.proxRg;
    
                        /* Resetar o endereco do proxRg */
                        indice.seek(antNB);
                        indice.skipBytes(Registro.sizeOf());
                        indice.writeLong(-1);
                    }
                    
                } else { // registro ira pertencer a nBkt
                    if(antNB == indice.length()) { // alteracao a ser feita no cabecalho do nBkt
                        nBkt.primeiroRg = atual;
                        
                        antNB = atual;
                        atual = rh.proxRg;

                    } else { // alteracao a ser feita no registro do endereco antNB
                        indice.seek(antNB);
                        indice.skipBytes(Registro.sizeOf());
                        indice.writeLong(atual);

                        antNB = atual;
                        atual = rh.proxRg;
    
                        /* Resetar o endereco do proxRg */
                        indice.seek(antNB);
                        indice.skipBytes(Registro.sizeOf());
                        indice.writeLong(-1);
                    }
                    bkt.n--;
                    nBkt.n++;
                }
            }
            
            /* Reescrever Bucket */
            indice.seek(endBucket);
            indice.write(bkt.toByteArray());

            /* Escrever novo Bucket */
            indice.seek(indice.length());
            indice.write(nBkt.toByteArray());

            /* Adicionar novo registro */
            rh = new RegistroHash(rg);
            diretorio.seek(hash(rh.rg.id, pGlobal));
            endBktDiretorio = diretorio.readLong();
            if(endBktDiretorio == endBucket) {
                indice.seek(antB);
            } else {
                indice.seek(antNB);
            }
            indice.skipBytes(Registro.sizeOf());
            indice.writeLong(indice.length());

            indice.seek(indice.length());
            indice.write(rh.toByteArray());
        }

        indice.close();
        diretorio.close();
    }

    public static Book read(int id) throws Exception {
        Book b = null;
        byte[] ba;
        boolean achou = false;
        RegistroHash rh = new RegistroHash();
        Bucket bkt = new Bucket();
        RandomAccessFile diretorio = new RandomAccessFile("diretorioHash", "r");
        RandomAccessFile indice = new RandomAccessFile("hashingEstendido", "r");
        RandomAccessFile dados = new RandomAccessFile("Books.bd", "r");

        diretorio.seek(0);
        diretorio.seek(hash(id, diretorio.readInt()));

        indice.seek(diretorio.readLong());

        ba = new byte[Bucket.sizeOf()];
        indice.read(ba);
        bkt.fromByteArray(ba);

        rh.proxRg = bkt.primeiroRg;

        while(!achou && rh.proxRg > -1) {
            indice.seek(rh.proxRg);
            ba = new byte[RegistroHash.sizeOf()];
            indice.read(ba);
            rh.fromByteArray(ba);

            if(rh.rg.id == id) achou = true;
        }

        if(achou) {
            b = new Book();
            dados.seek(rh.rg.end);
            ba = new byte[dados.readInt()];
            dados.read(ba);
            b.fromByteArray(ba);
        }

        diretorio.close();
        indice.close();
        dados.close();

        return b;
    }
    /**
     * Identificar posicao no diretorio que contem o endereco de Bucket desejado
     * @param id
     * @return  endereco do item do diretorio com o endereco do bucket
     */
    public static long hash(int id, int pGlobal) {
        // (posicao relativa no diretorio * tamanho de cada item do diretorio) + espaco ocupado pela profundidade global 
        return (long) ((id % Math.pow(2, pGlobal)) * Long.BYTES) + Integer.BYTES;
    }

    public static void escreverHash() throws Exception {
        RandomAccessFile indice = new RandomAccessFile("hashingEstendido", "r");
        RandomAccessFile diretorio = new RandomAccessFile("diretorioHash", "r");
        RegistroHash rh = new RegistroHash();
        Bucket bkt = new Bucket();
        byte[] ba;
        long end, prox;
        ArrayList<Long> ends = new ArrayList<>();

        diretorio.readInt();
        while(diretorio.getFilePointer() < diretorio.length()) {
            end = diretorio.readLong();
            indice.seek(end);

            ba = new byte[Bucket.sizeOf()];
            indice.read(ba);
            bkt.fromByteArray(ba);

            System.out.print(end + ": " + bkt);

            if(!ends.contains(Long.valueOf(end)) && bkt.primeiroRg > -1) {
                indice.seek(bkt.primeiroRg);

                ba = new byte[RegistroHash.sizeOf()];
                indice.read(ba);
                rh.fromByteArray(ba);

                System.out.print("  ->  "+rh);

                prox = rh.proxRg;

                while(prox > -1) {
                    indice.seek(rh.proxRg);
                    ba = new byte[RegistroHash.sizeOf()];
                    indice.read(ba);
                    rh.fromByteArray(ba);
                    System.out.print("\t"+rh);
                    prox = rh.proxRg;
                }
            }

            System.out.println();
            ends.add(Long.valueOf(end));
        }

        indice.close();
        diretorio.close();
    }
}

/* Registro -> classe ArvoreB */
class RegistroHash extends Registro {
    Registro rg; // posicao do Book no bd
    long proxRg;

    RegistroHash(){
        rg = new Registro();
        proxRg = -1;
    }
    RegistroHash(Registro rg) {
        this.rg = rg.clone();
        proxRg = -1;
    }
    RegistroHash(Registro rg, long proxRg) {
        rg = rg.clone();
        this.proxRg = proxRg;
    }

    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.write(rg.toByteArray());
        dos.writeLong(proxRg);

        return baos.toByteArray();
    }
    public void fromByteArray(byte[] ba) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);

        rg.id = dis.readInt();
        rg.end = dis.readLong();
        proxRg = dis.readLong();        
    }

    public String toString() {
        String s = "";
        s += rg+ " | " +proxRg;
        return s;
    }
    public static int sizeOf() {
        return Registro.sizeOf() + Long.BYTES;
    }
}


class Bucket {
    int p;
    int n;
    long primeiroRg;

    Bucket() {
        p = 1;
        n = 0;        
        primeiroRg = -1;
    }
    Bucket(int p) {
        this.p = p;
        n = 0;
        primeiroRg = -1;
    }

    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeInt(p);
        dos.writeInt(n);
        dos.writeLong(primeiroRg);

        return baos.toByteArray();
    }
    public void fromByteArray(byte[] ba) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);

        p = dis.readInt();
        n = dis.readInt();
        primeiroRg = dis.readLong();
    }

    public String toString() {
        return p +" | "+ n +" | " +primeiroRg;
    }

    public static int sizeOf() {
        return 2*Integer.BYTES + Long.BYTES;
    }
}
