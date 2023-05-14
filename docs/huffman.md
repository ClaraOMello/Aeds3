# Decisões - Codificação [Huffman](../Huffman.java)

### Arquivo compactado
- Início: hashMap de frequência de símbolo escrito
  - tamanho do hashMap
  - chaves seguidas de seus valores (simbolo e quantas vezes apareceu)
- Último byte do arquivo representa a quantidade de bits válidos no último byte da codificação

       ______________________________________________________________________________________________________________
      | TamanhoHashMapFreq(int) | Simbolo(byte) | FrequenciaDoSimbolo(int) | ...codificacao... | UltimoByteLen(Byte) |
       ¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨
