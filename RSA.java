public class RSA {
    static int p = 37, q = 43;
    static int n = p*q;
    static int z = (p-1)*(q-1);
    static int d = 17; // primo em relacao a z
    static int e = 89; // (e*d)%z=1
    static String padrao = "HEXACAMPEAO";

    public static void main(String[] args) {
        System.out.println("Padrao: " + padrao);
        int[] cifra = cifrar(padrao);
        for (int i : cifra) {
            System.out.print(i);
        }
        System.out.println();
        System.out.println(descifrar(cifra));
    }

    /**
     * Exponenciacao modular
     * @param base
     * @param expoente
     * @param modulo
     * @return (base^expoente) % modulo
     */
    private static int expMod(int base, int expoente, int modulo) {
        int resultado = 1;
        base = base % modulo;
        while(expoente > 0) {
            if(expoente % 2 == 1) {
                resultado = (resultado * base) % modulo;
            }
            base = (base * base) % modulo;
            expoente >>= 1;
        }
        return resultado;
    }

    public static int[] cifrar(String texto) {
        int[] cifra = new int[texto.length()];

        for (int i = 0; i < texto.length(); i++) {
            cifra[i] = expMod(texto.charAt(i), e, n);
        }

        return cifra;
    }

    public static String descifrar(int[] cifra) {
        String texto = "";

        for (int i = 0; i < cifra.length; i++) {
            texto += (char) (expMod(cifra[i], d, n));
        }

        return texto;
    }
}
