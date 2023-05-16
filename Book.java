import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Book { 
    private short likedPercent;
    private int id, pages;
    private String title, series, author, language, bookFormat, edition, publisher, publishDate;
    private float rating, price;
    private char[] isbn = new char[13];
    private String[] genres;
    private LocalDate firstPublishDate;
    private static String nomeFileBD = "Books.bd";
    public static void setBD(String nome) {
        nomeFileBD = nome;
    }

    Book() {
        likedPercent = 0;
        id = 0;
        pages = 0;
        title = "";
        series = "";
        author = "";
        language = "";
        bookFormat = "";
        edition = "";
        publisher = "";
        publishDate = "";
        rating = 0;
        price = 0;
        for(int i = 0; i < 13; i++) isbn[i] = '0';
        genres = new String[0];
        firstPublishDate = LocalDate.of(1900, 1, 1);
    }
    Book(String title, String series, String author, float rating, String language, char[] isbn, String[] genres, String bookFormat, String edition, int pages, String publisher, String publishDate, LocalDate firstPublishDate, short likedPercent, float price) {
        SetLikedPercent(likedPercent);
        this.id = 0; // id nao pode ser definida manualmente
        SetPages(pages);
        SetTitle(title);
        SetSeries(series);
        SetAuthor(author);
        SetLanguage(language);
        SetBookFormat(bookFormat);
        SetEdition(edition);
        SetPublisher(publisher);
        SetPublishDate(publishDate);
        SetRating(rating);
        SetPrice(price);
        SetIsbn(isbn);
        SetGenres(genres);
        SetFirstPublishDate(firstPublishDate);
    }

    
    /*
     * Construcao do BD inicial a ser utilizado
     */
    public static void construirBD(String bd, int stop) throws Exception {
        Scanner fread = new Scanner(new File((bd.length() == 0) ? "BD.csv" : bd));
        RandomAccessFile fwrite = new RandomAccessFile(nomeFileBD, "rw");
        fwrite.setLength(0);

        String linha = "";
        Book b;
        byte[] array;
        int ultimoID = (fwrite.length() > 4) ? fwrite.readInt() : 0 ;
        
        linha = fread.nextLine(); // pular cabecalho

        while(fread.hasNext() && stop-- >0){
            linha = fread.nextLine();
            b = toBook(linha, true);
            array = b.toByteArray();

            // atualizar, no cabecalho, o ultimo id utilizado
            fwrite.seek(0);
            fwrite.writeInt(++ultimoID);

            // sempre posicionar no final do arquivo
            fwrite.seek(fwrite.length());
            fwrite.writeBoolean(true); // lapide
            fwrite.writeInt(array.length);
            fwrite.write(array);
        }
        
        fread.close();
        fwrite.close();

        System.out.println("Transferência completa do BD");
    }
    
    /**
     * Funcao para transformar uma String em objeto Book
     * @param   s Linha descritiva de um Book
     * @param   csv Possibilitar invercao das variaveis month e day
     *              No csv original: data = MM/dd/uuuu
     *              No toString: data = dd/MM/uuuu
     * @return String convertida em Book
     */
    static private Book toBook(String s){  
        return toBook(s, false);
    }
    static private Book toBook(String s, Boolean csv) {
        Book b = new Book();
        String[] partes = new String[16];
        int pos = 0;

        String aux;

        // separar partes por atributo
        for (int i = 0; i < 16; i++) {
            partes[i] = "";
            if (pos < s.length()-1 && s.charAt(pos) != '"') {
                while(pos < s.length() && s.charAt(pos) != ',') {
                    partes[i] += s.charAt(pos);
                    pos++;
                }
                pos++;
            } else if (pos < s.length()-1 && s.charAt(pos) == '"'){
                pos++;
                while(s.charAt(pos) != '"' || (pos < s.length()-1 && (s.charAt(pos) == '"' && s.charAt(pos+1) == '"'))) {
                    if (s.charAt(pos) == '"' && s.charAt(pos+1) == '"') pos++; // caso acha aspas no meio ""xxx""
                    partes[i] += s.charAt(pos);
                    pos++;
                }
                pos += 2;
            }
            //System.out.println(partes[i]);
        }

        //bookId
        if(partes[0].length() > 0) b.id = Integer.parseInt(partes[0]);
        //title
        for(int i = 0; i < partes[1].length(); i++) b.title += partes[1].charAt(i);
        //series
        for(int i = 0; i < partes[2].length(); i++) b.series += partes[2].charAt(i);
        //author
        for(int i = 0; i < partes[3].length(); i++) b.author += partes[3].charAt(i);

        //rating
        if(partes[4].length() > 0) {
            aux = "";
            for(int j = 0; j < partes[4].length(); j++) {
                if (partes[4].charAt(j) == ',') aux += '.';
                else aux += partes[4].charAt(j);
            }
            b.rating = Float.parseFloat(aux);
        }

        //language
        for(int i = 0; i < partes[5].length(); i++) b.language += partes[5].charAt(i);

        //isbn
        int len = b.isbn.length-1;
        for(int k = len; k >= 0; k--) {
            if(k+1 > partes[6].length()) b.isbn[len-k] = '0';
            else b.isbn[len-k] = partes[6].charAt(partes[6].length() - k -1);
        }

        //genres
        int count = 0;
        pos = 0;

        if(partes[7].length() <= 2) b.genres = new String[0];
        else {
            for(int i = 0; i < partes[7].length(); i++) if(partes[7].charAt(i) == ',') count++;
            b.genres = new String[count+1];
            for(int j = 0; j <= count; j++) {
                b.genres[j] = "";
                pos += 2; // 1) pula colchete e aspas || 2) pula espaco e aspas
                while (partes[7].charAt(pos) != '\'') {
                    b.genres[j] += partes[7].charAt(pos++);
                }
                pos += 2; // pular (aspas e virgula) ou (aspas e colchete)
            }
        }        

        //bookFormat
        for(int i = 0; i < partes[8].length(); i++) b.bookFormat += partes[8].charAt(i);
        //edition
        for(int i = 0; i < partes[9].length(); i++) b.edition += partes[9].charAt(i);
        //pages
        if(partes[10].length() > 0) b.pages = Integer.parseInt(partes[10]);
        //publisher
        for(int i = 0; i < partes[11].length(); i++) b.publisher += partes[11].charAt(i);
        //publishDate
        for(int i = 0; i < partes[12].length(); i++) b.publishDate += partes[12].charAt(i);

        //firstPublishDate
        int day = 0, month = 0, year = 0;
        aux = "";
        if(partes[13].length() > 0) {
            if(partes[13].length() <= 8 && (partes[13].charAt(0) <= '9' && partes[13].charAt(1) >= '0')) { // tipos de data (MM/dd/uu) ou (uuuu)
                if(partes[13].length() == 8) {
                    aux = "";
                    aux += partes[13].charAt(0);
                    aux += partes[13].charAt(1);
                    if(csv) month = Integer.parseInt(aux); 
                    else day = Integer.parseInt(aux);
    
                    aux = "";
                    aux += partes[13].charAt(3);
                    aux += partes[13].charAt(4);
                    if(csv) day = Integer.parseInt(aux); 
                    else month = Integer.parseInt(aux);
    
                    aux = "";
                    aux += partes[13].charAt(6);
                    aux += partes[13].charAt(7);
                    year = Integer.parseInt(aux);
                    year += (year <= LocalDate.now().getYear()-2000) ? 2000 : 1900;
                    
                    b.firstPublishDate = LocalDate.of(year, month, day);
                    
                } else {
                    year = Integer.parseInt(partes[13]);
                    b.firstPublishDate = LocalDate.of(year, 1, 1); // caso so haja registrado o ano de publicacao
                }
            } else { // datas por extenso
                pos = 0;
                aux = "";
                while(partes[13].length()-1 > pos && partes[13].charAt(pos) != ' ') {
                    aux += partes[13].charAt(pos++);
                } //seleciona o mes da data
                switch (aux) {
                    case "January": month = 1; break;
                    case "February": month = 2; break;
                    case "March": month = 3; break;
                    case "April": month = 4; break;
                    case "May": month = 5; break;
                    case "June": month = 6; break;
                    case "July": month = 7; break;
                    case "August": month = 8; break;
                    case "September": month = 9; break;
                    case "October": month = 10; break;
                    case "November": month = 11; break;
                    case "December": month = 12; break;
                }
                
                pos++; //pular espaco
                aux = "";
                if((partes[13].length() - pos-1) <= 4) { // caso n acha registro do dia
                    while(pos < partes[13].length()) {
                        aux += partes[13].charAt(pos++);
                    }
                    year = Integer.parseInt(aux);
                    day = 1;
                } else {
                    aux = "";
                    while((partes[13].charAt(pos) <= '9' && partes[13].charAt(pos) >= '0')) {
                        aux += partes[13].charAt(pos++);
                    }
                    day = Integer.parseInt(aux);
                    pos += 3; // pular pra onde comeca o ano
    
                    aux = "";
                    while(pos < partes[13].length()) {
                        aux += partes[13].charAt(pos++);
                    }
                    year = Integer.parseInt(aux);
                }
                
                b.firstPublishDate = LocalDate.of(year, month, day);
            }
    
        }
        
        //likedPercent
        if(partes[14].length() > 0) b.likedPercent = Short.parseShort(partes[14]);

        //price
        if(partes[15].length() > 0) {
            aux = "";
            for(int j = 0; j < partes[15].length(); j++) {
                if (partes[15].charAt(j) == ',') aux += '.';
                else if (partes[15].charAt(j) == '.') ;
                else aux += partes[15].charAt(j);
            }
            b.price = Float.parseFloat(aux);
        }
        
        
        return b;
    }

    public String toString() { // como linha do csv 
        String str = "";
        DateTimeFormatter datef = DateTimeFormatter.ofPattern("dd/MM/uuuu");
        //DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance();
        //dfs.setDecimalSeparator(',');
        DecimalFormat df = new DecimalFormat("#0.00");

        str += id + ",";
        str += (title.length() > 0) ? title + "," : ",";
        str += series + "," + author; 
        
        str += ",\"" + df.format(rating) + "\",";
        
        str += (language.length() > 0) ? language + "," : ",";
        
        for(int i = 0; i<13; i++) str += isbn[i];
        
        str += ",\"[";
        if (genres.length > 0) str += "'" + genres[0] + "'";
        for(int i = 1; i < genres.length; i++) {
            str += ", '" + genres[i] + "'";
        }

        str += "]\"," + bookFormat + "," + edition + ",";
        
        str += pages + "," + publisher + "," + publishDate + ",";
        str += firstPublishDate.format(datef) + "," + likedPercent + ",\"" + df.format(price) + "\"";

        return str;
    }
    public String toString2() { // personalizado 
        String s = "";
        DateTimeFormatter datef = DateTimeFormatter.ofPattern("dd/MM/uuuu");
        DateTimeFormatter datefAC = DateTimeFormatter.ofPattern("dd/MM");
        DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance();
        dfs.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat("##.00", dfs);

        s += id + "." + title + " - " + author;
        
        if(firstPublishDate.getYear() >= 0) s += " - " + firstPublishDate.format(datef);
        else s += " - " + firstPublishDate.format(datefAC) + '/' + (-1*firstPublishDate.getYear()) + " AC";

        s += (genres.length > 0) ? "\nGêneros: " + genres[0]: "";
        for(int i = 1; i < genres.length; i++) {
            s += ", " + genres[i];
        }
        
        s += "\n" + "Páginas: " + pages + "\tAvaliação: " + df.format(rating) +"\tPreço: " + df.format(price);
        return s;
    }

    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeInt(id);
        dos.writeUTF(title);
        dos.writeUTF(series);
        dos.writeUTF(author);
        dos.writeFloat(rating);
        dos.writeUTF(language);
        for(int i = 0; i<13; i++) dos.writeChar(isbn[i]);

        dos.writeInt(genres.length);
        for(int i = 0; i<genres.length; i++) dos.writeUTF(genres[i]);
        
        dos.writeUTF(bookFormat);
        dos.writeUTF(edition);
        dos.writeInt(pages);
        dos.writeUTF(publisher);
        dos.writeUTF(publishDate);
        dos.writeLong(firstPublishDate.toEpochDay());
        dos.writeShort(likedPercent);
        dos.writeFloat(price);

        return baos.toByteArray();
    }
    /*
     * Atribuicao dos dados a um objeto book
     */
    public void fromByteArray(byte[] array) throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(array);
        DataInputStream dis = new DataInputStream(bais);
        
        id = dis.readInt();
        title = dis.readUTF();
        series = dis.readUTF();
        author = dis.readUTF();
        rating = dis.readFloat();
        language = dis.readUTF();

        for(int i = 0; i<13; i++) isbn[i] = dis.readChar();

        int len = dis.readInt(); // quantidade de generos listados
        genres = new String[len];
        for(int i = 0; i<len; i++) genres[i] = dis.readUTF();
        
        bookFormat = dis.readUTF();
        edition = dis.readUTF();
        pages = dis.readInt();
        publisher = dis.readUTF();
        publishDate = dis.readUTF();
        firstPublishDate = LocalDate.ofEpochDay(dis.readLong());
        likedPercent = dis.readShort();
        price = dis.readFloat();
    }

    public static Book create(Book b) throws Exception {
        RandomAccessFile fileBD = new RandomAccessFile(nomeFileBD, "rw");
        int lid; // ultimo id
        byte[] array;
        
        // atualizar ultimo id
        fileBD.seek(0);
        lid = fileBD.readInt();
        lid++;
        fileBD.seek(0);
        fileBD.writeInt(lid);
        b.id = lid;

        array = b.toByteArray();

        // registrar
        fileBD.seek(fileBD.length());
        fileBD.writeBoolean(true); // lapide        

        fileBD.writeInt(array.length);
        fileBD.write(array);

        fileBD.close();
        return b;
    }
    public static void create(String book) throws Exception {
        RandomAccessFile fileBD = new RandomAccessFile(nomeFileBD, "rw");
        Book b = toBook(book);
        int lid; // ultimo id
        byte[] array;
        
        // atualizar ultimo id
        fileBD.seek(0);
        lid = fileBD.readInt();
        lid++;
        fileBD.seek(0);
        fileBD.writeInt(lid);
        b.id = lid;

        array = b.toByteArray();

        // registrar
        fileBD.seek(fileBD.length());
        fileBD.writeBoolean(true); // lapide
        fileBD.writeInt(array.length);
        fileBD.write(array);

        fileBD.close();
    }
    public static Book read(int id) throws Exception {
        RandomAccessFile fileBD = new RandomAccessFile(nomeFileBD, "r");
        Book b = null;
        boolean achou = false;
        int len = 0, eof = 0; // tamanho do registro, lapide/fim do arquivo
        long pos;
        byte[] array;

        int lid = fileBD.readInt();
        
        if (id <= lid) {
            eof = fileBD.read();
            
            while(!achou && eof != -1) {
                len = fileBD.readInt();
                pos = fileBD.getFilePointer();
                if(eof == 1) {  
                    lid = fileBD.readInt();
                    fileBD.seek(pos);
                    if(lid == id) {
                        b = new Book();
                        array = new byte[len];
                        fileBD.read(array);
                        b.fromByteArray(array);
                        achou = true;
                    } else { fileBD.skipBytes(len); }

                } else {
                    fileBD.skipBytes(len);
                }

                eof = fileBD.read();
            }
        } else {} // id inexistente

        fileBD.close();
        return b;
    }
    public static Book update(int id, Book b) throws Exception {
        RandomAccessFile fileBD = new RandomAccessFile(nomeFileBD, "rw");
        b.id = id;
        boolean achou = false;
        int len = 0, eof = 0; // tamanho do registro, lapide/fim do arquivo
        long posReg, posLap = 0; // posicao de inicio do registro e da lapide do registro
        byte[] array = b.toByteArray();

        int lid = fileBD.readInt();
        
        if (id <= lid) {
            posLap = fileBD.getFilePointer();
            eof = fileBD.read();
            
            while(!achou && eof != -1) {
                len = fileBD.readInt();
                posReg = fileBD.getFilePointer();
                if(eof == 1) {  
                    lid = fileBD.readInt();
                    fileBD.seek(posReg);
                    if(lid == id) {
                        if(array.length > len) {
                            fileBD.seek(posLap);
                            fileBD.writeBoolean(false);
                            fileBD.seek(fileBD.length());
                            fileBD.writeBoolean(true); // lapide
                            fileBD.writeInt(array.length);
                            fileBD.write(array);
                        } else {
                            fileBD.seek(posReg);
                            fileBD.write(array);
                        }
                        achou = true;
                        
                    } else { fileBD.skipBytes(len); }

                } else {
                    fileBD.skipBytes(len);
                }

                posLap = fileBD.getFilePointer();
                eof = fileBD.read();
            }
        } else {} // id inexistente

        fileBD.close();
        return b;
    }
    public static Book delete(int id)  throws Exception {
        RandomAccessFile fileBD = new RandomAccessFile(nomeFileBD, "rw");
        Book b = null;
        boolean achou = false;
        int len = 0, eof = 0; // tamanho do registro, lapide/fim do arquivo
        long posReg, posLap = 0; // posicao de inicio do registro e da lapide do registro
        byte[] array;

        int lid = fileBD.readInt();
        
        if (id <= lid) {
            posLap = fileBD.getFilePointer();
            eof = fileBD.read();
            
            while(!achou && eof != -1) {
                len = fileBD.readInt();
                posReg = fileBD.getFilePointer();
                if(eof == 1) {  
                    lid = fileBD.readInt();
                    fileBD.seek(posReg);
                    if(lid == id) {
                        b = new Book();
                        array = new byte[len];
                        fileBD.read(array);
                        b.fromByteArray(array);
                        achou = true;

                        fileBD.seek(posLap);
                        fileBD.writeBoolean(false);
                        
                    } else { fileBD.skipBytes(len); }

                } else {
                    fileBD.skipBytes(len);
                }

                posLap = fileBD.getFilePointer();
                eof = fileBD.read();
            }
        } else {} // id inexistente

        fileBD.close();
        return b;
    }

    public void SetTitle(String title){ this.title = title; }
    public void SetSeries(String series){ this.series = series; }
    public void SetAuthor(String author){ this.author = author; }
    public void SetRating(float rating){ this.rating = rating; }
    public void SetLanguage(String language){ this.language = language; }
    public void SetIsbn(char[] isbn){ for(int i = 0; i < isbn.length; i++) this.isbn[this.isbn.length-1-i] = isbn[isbn.length-1-i]; }
    public void SetGenres(String[] genres){ this.genres = genres; }
    public void SetBookFormat(String bookFormat){ this.bookFormat = bookFormat; }
    public void SetEdition(String edition){ this.edition = edition; }
    public void SetPages(int pages){ this.pages = pages; }
    public void SetPublisher(String publisher){ this.publisher = publisher; }
    public void SetPublishDate(String publishDate){ this.publishDate = publishDate; }
    public void SetFirstPublishDate(LocalDate firstPublishDate){ this.firstPublishDate = firstPublishDate; }
    public void SetLikedPercent(short likedPercent){ this.likedPercent = likedPercent; }
    public void SetPrice(float price){ this.price = price; }
    
    public int GetId() { return id; }
    public String GetTitle() { return title; }
    public String GetSeries() { return series; }
    public String GetAuthor() { return author; }
    public float GetRating() { return rating; }
    public String GetLanguage() { return language; }
    public char[] GetIsbn() { return isbn; }
    public String[] GetGenres() { return genres; }
    public String GetBookFormat() { return bookFormat; }
    public String GetEdition() { return edition; }
    public int GetPages() { return pages; }
    public String GetPublisher() { return publisher; }
    public String GetPublishDate() { return publishDate; }
    public LocalDate GetFirstPublishDate() { return firstPublishDate; }
    public short GetLikedPercent() { return likedPercent; }
    public float GetPrice() { return price; }

}
