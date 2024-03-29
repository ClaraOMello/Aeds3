import java.io.RandomAccessFile;

/*
 * Ordenacao externa especifica para Book
 * Todos os metodos de ordenacao sao feitos pelo id do Book
 */
public class OrdenacaoExterna {
    static int m = 4; // quantidade de registros cabiveis em um bloco (memoria primaria)
    static int n = 2; // quantidade de caminhos
    static String bd = "Books.bd";

    public static void main(String[] args) throws Exception{
        bd = "dadosTeste.bd";
        
        /* Inicializacao do banco de dados de teste 
        
        RandomAccessFile dados = new RandomAccessFile(bd, "rw"); // base de dados
        byte[] ba;
        dados.setLength(0);

        int[] v = {789, 32, 2390, 12, 4, 111, 3487, 1, 780, 56};
        dados.writeInt(v.length);
        
        for(int i=0; i<v.length; i++) {
            ba = Book.read(v[i]).toByteArray();
            dados.writeBoolean(true);
            dados.writeInt(ba.length);
            dados.write(ba);
        }
        System.out.println(" <<<<<<<<<<< BANCO DE DADOS >>>>>>>>>>>> ");
        escrever(bd);
        System.out.println(" <<<<<<<<<<<<<<<<<<  >>>>>>>>>>>>>>>>> ");
                
        dados.close(); */
        
        try {
            //intercalacaoComum(n, m); escrever("dadosOrdenadoComum.bd");
            //intercalacaoVariavel(n, m); escrever("dadosOrdenadoVariavel.bd");
            intercalacaoSubstituicao(n, m); escrever("dadosOrdenadoSubstituicao.bd");
        } catch (Exception e) {
            System.out.println(e.getStackTrace());
        }
    }

    /**
     * Metodo de ordenacao externa atraves da intercalacao com selecao por substituicao
     * 
     * @param n = quantidade de caminhos
     * @param m = quantidade de registros cabiveis no heap (memoria primaria)
     */
    public static void intercalacaoSubstituicao(int n, int m) throws Exception {
        RandomAccessFile dados = new RandomAccessFile(bd, "rw"); // base de dados
        RandomAccessFile[] arqs = vetorArq(n); // arquivos intermediarios para ordenacao
        RandomAccessFile ordenado = null; // raf que possui os dados ordenados
        HeapCelula[] heap = new HeapCelula[m]; // vetor do heap para etapa de distribuicao
        int ultimo; // ultimo item que foi escrito nos arqW (na distribuicao)
        HeapCelula tmp; // armazena heapCelula temporaria do swap 

        Book[] vetor; // armazena registros a serem comparados durante intercalacoes 
        int len; // tamanho do vetor de bytes de um Book
        byte[] ba; // vetor de bytes de um Book

        int quant = 0; // quantidade de blocos de registros lidos/ ordenados
        int inter; // numero de intercalacoes
        int auxR, auxW; // determinam quais arqs sao de leitura ou de escrita (primeira ou segunda metade do vetor 'arqs')
        int posMenor; // armazena posicao do menor item do vetor
        Long auxPos; // posicao em arquivo de leitura para retornar caso parte ordenada tenha acabado
        Book aux; // valor do item lido para verificar se a parte ordenada acabou
        int auxN; // quantidade de arquivos que ja tiveram suas partes ordenadas intercaladas
        int eof; // quantidade de arquivos de leitura que chegaram ao fim
        RandomAccessFile raf = new RandomAccessFile("dadosOrdenadoSubstituicao.bd", "rw");
        
        /* Limpa todos os arquivos */
        for(int i=0; i<2*n; i++) arqs[i].setLength(0);
        raf.setLength(0);

        /** Distribuicao **/
        dados.seek(0);
        raf.writeInt(dados.readInt()); // ler ultimo id utilizado e escrever no novo arq

        // Construcao inicial do heap minimo
        // preenchimento
        for(int x=0; x<m; x++) {
            if(dados.readBoolean()) {
                len = dados.readInt();
                ba = new byte[len];
                dados.read(ba);
                Book b = new Book();
                b.fromByteArray(ba);                
                heap[x] = new HeapCelula(b);
            } else {
                len = dados.readInt();
                dados.skipBytes(len);
                x--;
            }
            
        }
        // organizacao
        for(int i=1; i < m; i++) {
            for(int k=i; k>0 && heap[(k-1)/2].id > heap[k].id; k=(k-1)/2) {
                // swap
                    tmp = heap[k].clone();
                    heap[k] = heap[(k-1)/2].clone();
                    heap[(k-1)/2] = tmp.clone();
            }
        }

        // Retirar menor elemento e repor no heap
        // caso o menor elemento do heap for maior que o ultimo elemento do arquivo de escrita, deve-se aumentar o grau do item
        // quando o grau de todos os elementos do heap forem iguais muda-se o arquivo de escrita
        int grau = 0; // identifica quando deve se alterar o arquivo de escrita (arqsW = grau%n)
        while(dados.getFilePointer() < dados.length()) {
            while(heap[0].grau == grau && dados.getFilePointer() < dados.length()) {
                ba = heap[0].b.toByteArray();
                arqs[grau%n].writeInt(ba.length);
                arqs[grau%n].write(ba);

                ultimo = heap[0].id;

                boolean positivo = dados.readBoolean();
                if(!positivo) {
                    while(!positivo && dados.getFilePointer() < dados.length()) {
                        len = dados.readInt();
                        dados.skipBytes(len);
                        if(dados.getFilePointer() < dados.length()) positivo = dados.readBoolean();
                    }
                } 
                
                if(dados.getFilePointer() < dados.length()) {
                    len = dados.readInt();
                    ba = new byte[len];
                    dados.read(ba);
                    Book b = new Book();
                    b.fromByteArray(ba);                
                    heap[0] = new HeapCelula(b);

                    if(heap[0].id < ultimo) heap[0].grau++;

                } else { // caso ultimo registro do bd tenha sido deletado
                    heap[0] = heap[m-1].clone();
                    heap[m-1] = null;
                }

                // Reorganizar
                for(int i=1; i < m; i++) {
                    for(int k=i; k>0 && heap[k] != null && ((heap[(k-1)/2].id > heap[k].id && heap[(k-1)/2].grau >= heap[k].grau) || heap[(k-1)/2].grau > heap[k].grau); k=(k-1)/2) {
                        // swap
                        tmp = heap[k].clone();
                        heap[k] = heap[(k-1)/2].clone();
                        heap[(k-1)/2] = tmp.clone();
                    }
                }

            }
            grau = heap[0].grau;   
        }

        // Escrever dados restantes do heap
        
        for(int cont=0; cont<m; cont++){
            if(heap[0] != null && heap[m-cont-1] != null) {
                ba = heap[0].b.toByteArray();
                arqs[heap[0].grau%n].writeInt(ba.length);
                arqs[heap[0].grau%n].write(ba);
                heap[0] = heap[m-cont-1].clone();
            }
                
            for(int i=1; i < m-cont; i++) {
                for(int k=i; k>0 && heap[k] != null && ((heap[(k-1)/2].id > heap[k].id && heap[(k-1)/2].grau >= heap[k].grau) || heap[(k-1)/2].grau > heap[k].grau); k=(k-1)/2) {
                    // swap
                    tmp = heap[k].clone();
                    heap[k] = heap[(k-1)/2].clone();
                    heap[(k-1)/2] = tmp.clone();
                    
                }
            }
        }


        dados.close();

        /* Intercalacoes */
        // arqs de leitura (arqsR) = (inter%2==0)?arqs[i]:arqs[n+i]; ou arqs[i+auxR]
        // arqs de escrita (arqsW) = (inter%2==0)?arqs[n+i]:arqs[i]; ou arqs[i+auxW]
        inter = 0;
        vetor = new Book[n]; // armazenar proximo item a ser intercalado de cada arquivo de leitura
        
        do { // verifica se intercalacao teve somente uma 'passada' (todos os registros foram para somente um arquivo)
            if(inter%2==0) {
                auxR=0; auxW=n;
            } else {
                auxR=n; auxW=0;
            }
            quant = 0; 
            eof = 0;
    
            // reiniciar arqs para reutilizacao
            for(int i=0; i<n; i++) arqs[i+auxW].setLength(0);
            // reposiciona ponteiro em cada arqsR 
            for(int i=0; i<n; i++) arqs[i+auxR].seek(0);
    
            while(eof < n) { // verifica se os arqR chegaram ao fim
                auxN = 0;
                // atribuir valores iniciais para 'vetor'
                for(int i=0; i<n; i++) {
                    if(arqs[i+auxR].getFilePointer() < arqs[i+auxR].length()) {
                        len = arqs[i+auxR].readInt();
                        ba = new byte[len];
                        arqs[i+auxR].read(ba);
                        vetor[i] = new Book();
                        vetor[i].fromByteArray(ba);
                    } else {
                        vetor[i] = null;
                    }
                }
    
                while(auxN < n) { // verifica se acabou partes ordenadas de todos os arqR
                    posMenor = posMenor(vetor);

                    if(vetor[posMenor] != null) {
                        ba = vetor[posMenor].toByteArray();
                        arqs[(quant%n)+auxW].writeInt(ba.length);
                        arqs[(quant%n)+auxW].write(ba);
                    }
                    if(arqs[posMenor+auxR].getFilePointer() < arqs[posMenor+auxR].length()) {
                        // verificacao se ainda se esta na parte ordenada do arquivo
                        auxPos = arqs[posMenor+auxR].getFilePointer();
                        len = arqs[posMenor+auxR].readInt();
                        ba = new byte[len];
                        arqs[posMenor+auxR].read(ba);
                        aux = new Book();
                        aux.fromByteArray(ba);
                        if(aux.GetId() < vetor[posMenor].GetId()) {
                            vetor[posMenor] = null;
                            auxN++;
                            arqs[posMenor+auxR].seek(auxPos);
                        } else {
                            vetor[posMenor] = aux;
                        }
                    } else {
                        vetor[posMenor] = null; 
                        auxN++;
                    }
                }

                // verificar quantos arqR chegaram ao fim
                for(int i=0; i<n; i++) if(arqs[i+auxR].getFilePointer() >= arqs[i+auxR].length()) eof++;
                
                ordenado = arqs[(quant%n) + auxW];
                quant++;
            }
            
            inter++;
        } while(quant>1);
        
        // transferir dados ordenados para um arquivo com nome padronizado
        ordenado.seek(0);
        while(ordenado.getFilePointer() < ordenado.length()) {
            raf.writeBoolean(true); // lapide
            len = ordenado.readInt();
            raf.writeInt(len);
            ba = new byte[len];
            ordenado.read(ba);
            raf.write(ba);
        }
        raf.close();
    }


    /**
     * Metodo de ordenacao externa atraves da intercalacao balanceada com blocos de tamanho variavel
     * 
     * @param n Quantidade de caminhos
     * @param m Quantidade de registros cabiveis em memoria primaria
     */
    public static void intercalacaoVariavel(int n, int m) throws Exception {
        RandomAccessFile dados = new RandomAccessFile(bd, "rw"); // base de dados
        RandomAccessFile[] arqs = vetorArq(n); // arquivos intermediarios para ordenacao
        Book[] vetor = new Book[m]; // registros a serem ordenados em memoria primaria na etapa de distribuicao
        int len; // tamanho do vetor de bytes de um Book
        byte[] ba; // vetor de bytes de um Book
        int quant = 0; // quantidade de blocos de registros lidos/ ordenados
        int pos; // posicao ate onde o vetor foi preenchido
        int inter; // numero de intercalacoes
        int auxR, auxW; // determinam quais arqs sao de leitura ou de escrita (primeira ou segunda metade do vetor 'arqs')
        int posMenor; // armazena posicao do menor item do vetor
        Long auxPos; // posicao em arquivo de leitura para retornar caso parte ordenada tenha acabado
        Book aux; // valor do item lido para verificar se a parte ordenada acabou
        int auxN; // quantidade de arquivos que ja tiveram suas partes ordenadas intercaladas
        int eof; // quantidade de arquivos de leitura que chegaram ao fim
        RandomAccessFile ordenado = null; // raf que possui os dados ordenados
        RandomAccessFile raf = new RandomAccessFile("dadosOrdenadoVariavel.bd", "rw");
        
        /* Limpa todos os arquivos */
        for(int i=0; i<2*n; i++) arqs[i].setLength(0);
        raf.setLength(0);

        /* Distribuicao */
        dados.seek(0);
        raf.writeInt(dados.readInt()); // ler ultimo id utilizado e escrever no novo arq
        
        while(dados.getFilePointer() < dados.length()) {
            // armazenar em memoria primaria m registros
            for(pos=0; dados.getFilePointer() < dados.length() && pos < m; pos++) { 
                if(dados.readBoolean()) {
                    len = dados.readInt();
                    ba = new byte[len];
                    dados.read(ba);
                    vetor[pos] = new Book();
                    vetor[pos].fromByteArray(ba);
                } else {
                    len = dados.readInt();
                    dados.skipBytes(len);
                    pos--;
                }
            }

            // ordenacao
            heapsort(vetor, pos);

            // distribuicao
            for(int i=0; i<pos; i++) {
                ba = vetor[i].toByteArray();
                arqs[quant%n].writeInt(ba.length);
                arqs[quant%n].write(ba);
            }

            quant++;
        }


        /* Intercalacoes */
        // arqs de leitura (arqsR) = (inter%2==0)?arqs[i]:arqs[n+i]; ou arqs[i+auxR]
        // arqs de escrita (arqsW) = (inter%2==0)?arqs[n+i]:arqs[i]; ou arqs[i+auxW]
        inter = 0;
        vetor = new Book[n]; // armazenar proximo item a ser intercalado de cada arquivo de leitura
        
        while(quant>1) { // verifica se intercalacao teve somente uma 'passada' (todos os registros foram para somente um arquivo)
            if(inter%2==0) {
                auxR=0; auxW=n;
            } else {
                auxR=n; auxW=0;
            }
            quant = 0; 
            eof = 0;
    
            // reiniciar arqs para reutilizacao
            for(int i=0; i<n; i++) arqs[i+auxW].setLength(0);
            // reposiciona ponteiro em cada arqsR 
            for(int i=0; i<n; i++) arqs[i+auxR].seek(0);
    
            while(eof < n) { // verifica se os arqR chegaram ao fim
                auxN = 0;
                // atribuir valores iniciais para 'vetor'
                for(int i=0; i<n; i++) {
                    if(arqs[i+auxR].getFilePointer() < arqs[i+auxR].length()) {
                        len = arqs[i+auxR].readInt();
                        ba = new byte[len];
                        arqs[i+auxR].read(ba);
                        vetor[i] = new Book();
                        vetor[i].fromByteArray(ba);
                    } else {
                        vetor[i] = null;
                    }
                }
    
                while(auxN < n) { // verifica se acabou partes ordenadas de todos os arqR
                    posMenor = posMenor(vetor);

                    if(vetor[posMenor] != null) {
                        ba = vetor[posMenor].toByteArray();
                        arqs[(quant%n)+auxW].writeInt(ba.length);
                        arqs[(quant%n)+auxW].write(ba);
                    }
                    if(arqs[posMenor+auxR].getFilePointer() < arqs[posMenor+auxR].length()) {
                        // verificacao se ainda se esta na parte ordenada do arquivo
                        auxPos = arqs[posMenor+auxR].getFilePointer();
                        len = arqs[posMenor+auxR].readInt();
                        ba = new byte[len];
                        arqs[posMenor+auxR].read(ba);
                        aux = new Book();
                        aux.fromByteArray(ba);
                        if(aux.GetId() < vetor[posMenor].GetId()) {
                            vetor[posMenor] = null;
                            auxN++;
                            arqs[posMenor+auxR].seek(auxPos);
                        } else {
                            vetor[posMenor] = aux;
                        }
                    } else {
                        vetor[posMenor] = null; 
                        auxN++;
                    }
                }

                // verificar quantos arqR chegaram ao fim
                for(int i=0; i<n; i++) if(arqs[i+auxR].getFilePointer() >= arqs[i+auxR].length()) eof++;
                
                ordenado = arqs[(quant%n) + auxW];
                quant++;
            }
            
            inter++;
        }
        
        // transferir dados ordenados para um arquivo com nome padronizado
        ordenado.seek(0);
        while(ordenado.getFilePointer() < ordenado.length()) {
            raf.writeBoolean(true); // lapide
            len = ordenado.readInt();
            raf.writeInt(len);
            ba = new byte[len];
            ordenado.read(ba);
            raf.write(ba);
        }
        raf.close();
    }

    
     /**
     * Metodo de ordenacao externa atraves da intercalacao balanceada comum
     * 
     * @param n Quantidade de caminhos
     * @param m Quantidade de registros cabiveis em um bloco (memoria primaria)
     */
    public static void intercalacaoComum(int n, int m) throws Exception {
        RandomAccessFile dados = new RandomAccessFile(bd, "rw"); // base de dados
        RandomAccessFile[] arqs = vetorArq(n); // arquivos intermediarios para ordenacao
        Book[] vetor = new Book[m]; // registros a serem ordenados em memoria primaria na etapa de distribuicao
        int len; // tamanho do vetor de bytes de um Book
        byte[] ba; // vetor de bytes de um Book
        int quant = 0; // quantidade de blocos de registros lidos/ ordenados
        int pos; // posicao ate onde o vetor foi preenchido
        int inter; // numero de intercalacoes
        int auxR, auxW; // determinam quais arqs sao de leitura ou de escrita (primeira ou segunda metade do vetor 'arqs')
        int posMenor; // armazena posicao do menor item do vetor
        int[] auxN; // contador de quantos registros de cada arquivo de leitura foram escritos em algum arqW
        boolean pOrd; // se parte que foi ordenada de um arqR chegou ao fim
        int eof; // quantidade de arquivos de leitura que chegaram ao fim
        RandomAccessFile ordenado = null; // raf que possui os dados ordenados
        RandomAccessFile raf = new RandomAccessFile("dadosOrdenadoComum.bd", "rw"); // arquivo com os dados ordenados
        raf.setLength(0);
        
        /* Reinicia todos os arquivos */
        for(int i=0; i<2*n; i++) arqs[i].setLength(0);
        raf.setLength(0);

        /* Distribuicao */
        dados.seek(0);
        raf.writeInt(dados.readInt()); // ler ultimo id utilizado e escrever no novo arq
        
        while(dados.getFilePointer() < dados.length()) {
            // armazenar em memoria primaria m registros
            for(pos=0; dados.getFilePointer() < dados.length() && pos < m; pos++) { 
                if(dados.readBoolean()) {
                    len = dados.readInt();
                    ba = new byte[len];
                    dados.read(ba);
                    vetor[pos] = new Book();
                    vetor[pos].fromByteArray(ba);
                } else {
                    len = dados.readInt();
                    dados.skipBytes(len);
                    pos--;
                }
            }

            // ordenacao
            heapsort(vetor, pos);

            // distribuicao
            for(int i=0; i<pos; i++) {
                ba = vetor[i].toByteArray();
                arqs[quant%n].writeInt(ba.length);
                arqs[quant%n].write(ba);
            }

            quant++;
        }

        /* Intercalacoes */
        // arqs de leitura (arqsR) = (inter%2==0)?arqs[i]:arqs[n+i]; ou arqs[i+auxR]
        // arqs de escrita (arqsW) = (inter%2==0)?arqs[n+i]:arqs[i]; ou arqs[i+auxW]
        // parte ordenada de um arq = Math.pow(2, inter)* m;
        inter = 0;
        vetor = new Book[n]; // armazenar proximo item a ser intercalado de cada arquivo de leitura
        quant = 2;
        auxN = new int[n];
        
        while(quant>1) { // verifica se intercalacao teve somente uma 'passada' (todos os registros foram para somente um arquivo)
            if(inter%2==0) {
                auxR=0; auxW=n;
            } else {
                auxR=n; auxW=0;
            }
            quant = 0; 
            eof = 0;
    
            // reiniciar arqs para reutilizacao
            for(int i=0; i<n; i++) arqs[i+auxW].setLength(0);
            // reposiciona ponteiro em cada arqsR 
            for(int i=0; i<n; i++) arqs[i+auxR].seek(0);
            
    
            while(eof < n) { // verifica se os arqR chegaram ao fim
                for(int i =0; i<n; i++) auxN[i] = 0;
                pOrd = true;

                // atribuir valores iniciais para 'vetor'
                for(int i=0; i<n; i++) {
                    if(arqs[i+auxR].getFilePointer() < arqs[i+auxR].length()) {
                        len = arqs[i+auxR].readInt();
                        ba = new byte[len];
                        arqs[i+auxR].read(ba);
                        vetor[i] = new Book();
                        vetor[i].fromByteArray(ba);
                    } else {
                        vetor[i] = null;
                        auxN[i] = (int)(Math.pow(2, inter) * m); // garante que seja identificado que a parte ordenada acabou
                    }
                }
                
                
                while(pOrd) { // verifica se acabou partes ordenadas de todos os arqR
                    posMenor = posMenor(vetor);
                    if(vetor[posMenor] != null) {
                        ba = vetor[posMenor].toByteArray();
                        arqs[(quant%n)+auxW].writeInt(ba.length);
                        arqs[(quant%n)+auxW].write(ba);
                        auxN[posMenor]++;
                    }

                    if(arqs[posMenor+auxR].getFilePointer() < arqs[posMenor+auxR].length() && auxN[posMenor] < (Math.pow(2, inter) * m)) {
                        len = arqs[posMenor+auxR].readInt();
                        ba = new byte[len];
                        arqs[posMenor+auxR].read(ba);
                        vetor[posMenor] = new Book();
                        vetor[posMenor].fromByteArray(ba);

                    } else {
                        vetor[posMenor] = null; 
                        auxN[posMenor] = (int)(Math.pow(2, inter) * m);
                    }

                    pOrd = false;
                    for(int i=0; i<n; i++) {
                        if(auxN[i] < Math.pow(2, inter) * m) { 
                            pOrd = true;
                            i=n; 
                        }
                    }
                }

                // verificar quantos arqR chegaram ao fim
                for(int i=0; i<n; i++) if(arqs[i+auxR].getFilePointer() >= arqs[i+auxR].length()) eof++;
                ordenado = arqs[(quant%n) + auxW];
                quant++;
            }
            
            inter++;
        }
        
        // transferir dados ordenados para um arquivo com nome padronizado
        ordenado.seek(0);
        while(ordenado.getFilePointer() < ordenado.length()) {
            raf.writeBoolean(true); // lapide
            len = ordenado.readInt();
            raf.writeInt(len);
            ba = new byte[len];
            ordenado.read(ba);
            raf.write(ba);
        }
        raf.close();
    }

    /*
     * Cria um vetor com 2*n arquivos (necessarios para intercalacao)
     */
    static RandomAccessFile[] vetorArq(int n) throws Exception {
        RandomAccessFile[] arqs = new RandomAccessFile[2*n];
        String s;
        for(int i = 0; i < 2*n; i++) {
            s = "arq";
            s += i;
            arqs[i] = new RandomAccessFile(s, "rw");
        }

        return arqs;
    }

    /*
     * Identifica posicao do menor item em um vetor
     */
    private static int posMenor(Book[] vetor) {
        int pos=0;
        for(int i=1; i<vetor.length; i++) {
            if(vetor[pos] == null) pos = i;
            else if(vetor[i] != null && vetor[i].GetId() < vetor[pos].GetId()) pos = i;
        }
        return pos;
    }

    /* 
     * Inverte os valores de duas posicoes de um vetor 
     */
    static void swap(Book[] vetor, int p1, int p2) {
        Book tmp = vetor[p1];
        vetor[p1] = vetor[p2];
        vetor[p2] = tmp;
    }

    /**
     * @param pos Identifica ate que posicao o vetor esta preenchido com dados validos
     */
    static void heapsort(Book[] v, int pos) {
        // Construcao do heap
        int i; // limita ate onde o vetor esta desordenado
        int k; // posicao do 'ponteiro' no vetor
        
        for(i=1; i < pos; i++) {
            for(k=i; k>0 && v[(k-1)/2].GetId() < v[k].GetId(); k=(k-1)/2) {
                swap(v, (k-1)/2, k);
            }
        }

        // Destruicao do heap
        for(i=pos-1; i>0; i--){
            swap(v, i, 0);
            int posMaiorFilho;
            k=0;

            for(int tam =i-1; tam>0 && tam>=(k*2)+1; tam--) {
                if(k*2+2 > tam || v[(k*2)+1].GetId() > v[(k*2)+2].GetId()) posMaiorFilho = k*2+1;
                else posMaiorFilho = k*2+2;
                if(v[posMaiorFilho].GetId() > v[k].GetId()) swap(v, posMaiorFilho, k);
            }
        }
    }

    /*
     * Escrever em texto dados de um arquivo s
     */
    static public void escrever(String s) {
        try {
            RandomAccessFile raf = new RandomAccessFile(s, "rw");
            raf.seek(0);
            int len;
            byte[] ba;
            Book b;
            raf.readInt();
            while(raf.getFilePointer() < raf.length()) {
                if(raf.readBoolean()) {
                    len = raf.readInt();
                    ba = new byte[len];
                    raf.read(ba);
                    b = new Book();
                    b.fromByteArray(ba);
                    System.out.println(b);
                } else {
                    len = raf.readInt();
                    raf.skipBytes(len);
                }
            }
            raf.close();
        } catch(Exception e) {
            System.out.println("Erro na leitura do arquivo passado");
            System.out.println("escrever() - OrdenacaoExterna");
        }
    }
}

/*
 * Classe auxiliar ao heap minimo da funcao de intercalacao por substituicao
 */
class HeapCelula {
    int id;
    Book b;
    short grau;
    HeapCelula(Book b) {
        this.b = b;
        id = b.GetId();
        grau = 0;
    }

    public HeapCelula clone(){
        HeapCelula hp = new HeapCelula(b);
        hp.id = b.GetId();
        hp.grau = grau;
        return hp;
    }
}