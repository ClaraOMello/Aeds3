import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

/*
 * Inicio do registro vai ser considerado como o tamanho total do registro (int)
 * 
 * Arquivo dos indices para Arvore B: arvoreB
 */
public class ArvoreB {
    static String bd = "Books.bd";
    static int TAM_NO = No.sizeOf(); // tamanho de um no em bytes
    public static void main(String args[]) throws Exception {
        Book.construirBD("", 50);
        create();
        escreverArvore();
        //int[] ids = {1, 5, 90, 45, 37, 101, 156, 150};
        //for(int i=0; i<ids.length; i++) System.out.println(read(ids[i]));
    }
    
    /** Indexacao por Avore B de ordem 8 **/
    public static void create() throws Exception {
        long posReg; // posicao do registro no bd
        Book b;
        boolean lapide;
        int len;
        byte[] ba;
        RandomAccessFile dados = new RandomAccessFile(bd, "r");

        RandomAccessFile indice = new RandomAccessFile("arvoreB", "rw");
        indice.setLength(0);
        indice.writeLong(0);
        indice.close();

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

        System.out.println("Árvore B criada");
    }
    
    /*
     * Acrescentar Book ao indice de Arvore B
     */
    public static void add(Registro ri) throws Exception {
        short ordem = 8;
        RandomAccessFile indice = new RandomAccessFile("arvoreB", "rw");
        byte[] noBytes = new byte[TAM_NO]; 
        Retorno rtn = new Retorno();
        No raiz, nRaiz;
        int raizN; // checagem se sera necessario criar nova raiz

        indice.seek(0);
        indice.seek(indice.readLong());
        // Encontrar no raiz
        if(indice.getFilePointer() == 0) { // caso ainda nao exista raiz
            indice.writeLong(indice.length());
            indice.seek(indice.length());
            raiz = new No(indice.getFilePointer());
            indice.write(raiz.toByteArray());
        } else {
            raiz = new No(indice.getFilePointer());
            indice.read(noBytes);
            raiz.fromByteArray(noBytes);
        }
        
        raizN = raiz.GetN();
        indice.close();

        rtn = add(ri, raiz);

        // caso ainda se deva acrescentar algum registro a raiz
        if(rtn.promovido != null) {
            indice = new RandomAccessFile("arvoreB", "rw");

            if(raiz.GetN() < ordem-1 && raizN < ordem-1) {
                raiz.addReg(rtn.promovido, rtn.novoNo.GetPos());

            } else { // caso haja a necessidade de criacao de uma nova raiz
                // raiz anterior = pontEsq da nRaiz
                nRaiz = new No();
                nRaiz.p[0] = raiz.GetPos();
                // nNo a ser criado aqui = pontDir da raiz
                nRaiz.p[1] = rtn.novoNo.GetPos();
                // raiz.r[ordem/2] = registro da raiz
                nRaiz.r[0] = rtn.promovido.clone();
                nRaiz.SetN(nRaiz.GetN()+1);

                indice.seek(indice.length());
                nRaiz.SetPos(indice.getFilePointer());
                indice.write(nRaiz.toByteArray());
                indice.seek(0);
                indice.writeLong(nRaiz.GetPos());
            }
            
            indice.seek(raiz.GetPos());
            indice.write(raiz.toByteArray());

            indice.close();
        }
    }
    
    /**
     * Funcao recursiva que possibilita a fragmentacao dos nos em cascata
     * @param   no No que vai ser analisado
     * @return  item promovido e novo no criado cuja posicao eh o ponteiro direito do registro promovido
     */
    private static Retorno add(Registro ri, No no) throws Exception {
        short ordem = 8;
        No nNo = new No();
        Retorno rtn = new Retorno(); // retorno recursivo
        Retorno fim = new Retorno(); // retorno da funcao
        byte[] noBytes = new byte[TAM_NO];
        Registro tmp;
        long pontDir = -1;
        RandomAccessFile indice = new RandomAccessFile("arvoreB", "rw");

        if(no.ehFolha()) {
            if(no.GetN() >= ordem-1) {
                if(ri.GetId() < no.r[no.r.length-1].id) {
                    tmp = new Registro();
                    tmp = ri.clone();
                    ri = no.GetReg(ordem-1).clone();
                    no.SetReg(ordem-1, tmp);
                    no.ordena();
                }

                for(int i=ordem/2 +1; i<no.r.length; i++) {
                    nNo.addReg(no.r[i], no.p[i+1]);
                    no.r[i].reset();
                    no.p[i+1] = -1;
                }
                nNo.addReg(ri, -1);
                no.SetN(ordem/2);
                
                fim.novoNo = nNo;
                fim.promovido = no.r[ordem/2].clone();
                no.r[ordem/2].reset();

                indice.seek(indice.length());
                nNo.SetPos(indice.getFilePointer());
                indice.write(nNo.toByteArray());
                indice.seek(no.GetPos());
                indice.write(no.toByteArray());

            } else {
                // caso o elemento caiba na folha
                no.addReg(ri, -1); // ponteiros de folhas sempre valem -1
                fim.promovido = null;
                fim.novoNo = null;
                indice.seek(no.GetPos());
                indice.write(no.toByteArray());
            }

        } else {
            // Descobrir qual ponteiro do no seguir
            nNo.SetPos(no.direcao(ri.GetId()));
            indice.seek(nNo.GetPos());
            indice.read(noBytes);
            nNo.fromByteArray(noBytes);
            indice.close();

            rtn = add(ri, nNo);

            indice = new RandomAccessFile("arvoreB", "rw");
            if(rtn.promovido != null) {
                if(no.GetN() < ordem-1) {
                    no.addReg(rtn.promovido, rtn.novoNo.GetPos());
                    indice.seek(no.GetPos());
                    indice.write(no.toByteArray());
                    fim.promovido = null;
                    fim.novoNo = null;
                } else { // criar novo no
                    nNo = new No();
                    if(rtn.promovido.GetId() < no.r[no.r.length-1].id) {
                        tmp = new Registro();
                        tmp = rtn.promovido.clone();
                        rtn.promovido = no.GetReg(ordem-1).clone();
                        no.SetReg(ordem-1, tmp);
                        
                        pontDir = no.p[no.p.length-1];
                        no.p[no.p.length-1] = rtn.novoNo.GetPos();
                    } else pontDir = rtn.novoNo.GetPos();
                    no.ordena();

                    nNo.p[0] = no.p[ordem/2+1];
                    no.p[ordem/2+1] = -1;

                    // preencher nNo
                    for(int i=ordem/2+1; i<no.r.length; i++) {
                        nNo.addReg(no.r[i], no.p[i+1]);
                        no.r[i].reset();
                        no.p[i+1] = -1;
                    }
                    nNo.addReg(rtn.promovido, pontDir);
                    no.SetN(ordem/2);

                    fim.promovido = no.r[ordem/2].clone();
                    fim.novoNo = nNo;
                    no.r[ordem/2].reset();

                    indice.seek(indice.length());
                    nNo.SetPos(indice.getFilePointer());
                    indice.write(nNo.toByteArray());
                    indice.seek(no.GetPos());
                    indice.write(no.toByteArray());

                }

            } else {
                fim.promovido = null;
                fim.novoNo = null;
            }
        }

        indice.close();
        return fim;
    }
    
    /**
     * Acesso a registros atraves da Arvore B 
     *
     * @param  id do Book a ser encontrado
     * @throws  FileNotFoundException caso o arquivo indice da arvoreB nao tenha sido criado
    */
    public static Book read(int id) throws Exception {
        RandomAccessFile indice = new RandomAccessFile("arvoreB", "rw");
        RandomAccessFile bd = new RandomAccessFile("Books.bd", "r");
        Book b = null;
        No no = new No();
        boolean achou = false;
        long direcao;
        byte[] ba = new byte[TAM_NO];
        
        indice.seek(0);
        direcao = indice.readLong();
        
        while(!achou && direcao > -1) {
            indice.seek(direcao);
            indice.read(ba);
            no.fromByteArray(ba);
            
            direcao = no.p[no.GetN()];
            for(int i=0; i < no.GetN(); i++) {
                if(id < no.r[i].GetId()) {
                    direcao = no.p[i];
                    i=no.GetN();
                } else if(id == no.r[i].GetId()) {
                    achou = true;
                    bd.seek(no.r[i].end);
                    ba = new byte[bd.readInt()];
                    bd.read(ba);
                    b = new Book();
                    b.fromByteArray(ba);

                    i=no.GetN();
                }
            }
        }

        indice.close();
        bd.close();

        return b;
    }

    /*
     * Remover Book do indice de Arvore B
     */
    public static void delete(int id) {

    }

    /* Mostra no terminal a representacao do indice da Arvore B no arquivo */
    public static void escreverArvore() throws IOException {
        RandomAccessFile indice = new RandomAccessFile("arvoreB", "r");
        indice.seek(0);
        System.out.println("  n  |   p1   |   c1   |   E1   |   p2   |   c2   |   E2   |   p3   |   c3   |   E3   |   p4   |   c4   |   E4   |   p5   |   c5   |   E5   |   p6   |   c6   |   E6   |   p7   |   c7   |   E7   |   p8   ");
        System.out.println(indice.readLong());
        while (indice.getFilePointer() < indice.length()) {
            No no = new No(indice.getFilePointer());
            byte[] ba = new byte[TAM_NO];
            indice.read(ba);
            no.fromByteArray(ba);
            
            System.out.println(no);
        }

        indice.close();
    }
    public static void escreverArvoreEmArq() throws IOException {
        RandomAccessFile indice = new RandomAccessFile("arvoreB", "r");
        RandomAccessFile arqW = new RandomAccessFile("arvore.txt", "rw");
        indice.seek(0);
        arqW.writeUTF("  n  |   p1   |   c1   |   E1   |   p2   |   c2   |   E2   |   p3   |   c3   |   E3   |   p4   |   c4   |   E4   |   p5   |   c5   |   E5   |   p6   |   c6   |   E6   |   p7   |   c7   |   E7   |   p8   \n");
        arqW.writeUTF(Long.toString(indice.readLong()));
        arqW.writeChar('\n');
        while (indice.getFilePointer() < indice.length()) {
            No no = new No(indice.getFilePointer());
            byte[] ba = new byte[TAM_NO];
            indice.read(ba);
            no.fromByteArray(ba);
            
            arqW.writeUTF(no.toString() + "\n");
        }

        indice.close();
        arqW.close();
    }
}



class Registro {
    protected int id;
    protected long end;

    Registro() {
        id = 0;
        end = -1;
    }
    Registro(int id, long end) {
        this.id = id;
        this.end = end;
    }

    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeInt(id);
        dos.writeLong(end);

        return baos.toByteArray();
    }

    public Registro clone() {
        Registro ri = new Registro();
        ri.id = this.id;
        ri.end = this.end;
        return ri;
    }

    public String toString() {
        String s = id +"|"+ end;
        return s;
    }

    public void reset() {
        id = 0;
        end = -1;
    }

    public int GetId() {
        return id;
    }

    public static int sizeOf() {
        return Integer.BYTES + Long.BYTES;
    }
}



class No extends Registro {
    private static short ordem = 8;
    private int n = 0; // max 7
    private long pos = -1;
    long[] p = new long[ordem];
    Registro[] r = new Registro[ordem-1];

    No() {
        n = 0;
        for(int i = 0; i <ordem-1; i++) {
            p[i] = -1;
            r[i] = new Registro();
        }
        p[ordem-1] = -1;
    }
    No(long pos) {
        this.pos = pos;
        n = 0;
        for(int i = 0; i <ordem-1; i++) {
            p[i] = -1;
            r[i] = new Registro();
        }
        p[ordem-1] = -1;
    }

    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeInt(n);
        for(int i = 0; i < ordem-1; i++) {
            dos.writeLong(p[i]);
            dos.write(r[i].toByteArray());   
        }
        dos.writeLong(p[ordem-1]);

        return baos.toByteArray();
    }

    public void fromByteArray(byte[] ba) throws IOException{
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);

        n = dis.readInt();
        for(int i = 0; i < ordem-1; i++) {
            p[i] = dis.readLong();
            r[i].id = dis.readInt();
            r[i].end = dis.readLong();
        }
        p[ordem-1] = dis.readLong();
    }

    /*
     * Adicionar um novo Registro
     */
    public void addReg(Registro reg, long pontDir) {
        if (n < ordem-1) {
            r[n] = reg.clone();
            p[n+1] = pontDir;
            n++;
            ordena();
        }
    }

    // Considera que somente o ultimo elemento esta fora de ordem
    public void ordena() {
        Registro tmp = new Registro();
        long pTmp;
        if(n>1) {
            for(int i=0; i <n-1; i++) {
                if(r[n-1].id < r[i].id) {
                    tmp = r[n-1].clone();
                    r[n-1] = r[i].clone();
                    r[i] = tmp.clone();
                    
                    pTmp = p[n];
                    p[n] = p[i+1];
                    p[i+1] = pTmp;
                } 
            }
        }
    }

    public String toString() {
        String s = pos + "  "+ n +"  |   "+ p[0] +"   |   "+ r[0] +"   |   "+ p[1] +"   |   "+ r[1] +"   |   "+ p[2] +"   |   "+ r[2] +"   |   "+ p[3] +"   |   "+ r[3] +"   |   "+ p[4] +"   |   "+ r[4] +"   |   "+ p[5] +"   |   "+ r[5] +"   |   "+ p[6] +"   |   "+ r[6] +"   |   "+ p[7] +"   ";
        return s;
    }

    public boolean ehFolha() {
        if(p[0] == -1) return true;
        return false;
    }

    /* Define qual ponteiro de um no deve se seguir para encontrar um id */
    public long direcao(int id) {
        long end = -1;
        if(!ehFolha()) {
            end = p[n];
            for(int i=0; i<n; i++) {
                if(id < r[i].id) end = p[i];
            }
        }
        return end;
    }
    
    /* Espaco em bytes ocupados por um no em arquivo (representacao em arquivo nao inclui ordem nem pos) */
    public static int sizeOf() { 
        return Integer.BYTES + ordem*Long.BYTES + (ordem-1)*Registro.sizeOf();
    }

    public int GetN() { return n; }
    public Registro[] GetRegs() { return r; }
    public Registro GetReg(int pos) { return r[pos]; }
    public long GetPos() { return pos; }
    
    public void SetN(int n){ this.n = n; }
    public void SetPos(long pos) { this.pos = pos; }
    public void SetReg(int pos, Registro rg) { r[pos] = rg.clone(); }

}


/* 
 * Classe para auxiliar o retorno de add
 * novoNo = endereco de um novo que possa ser criado ao dividir um no
 * promovido = item que sera promovido ao no pai apos a divisao do no
 */
class Retorno {
    No novoNo;
    Registro promovido;
    Retorno() {
        novoNo = new No();
        promovido = new Registro();
    }
    Retorno(No no, Registro rg) {
        novoNo = no;
        promovido = rg;
    }
}
