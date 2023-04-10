# Decisões - [Listas Invertidas](../ListaInvertida.java)
* Atributos `genres` e `title` 
* Tanto as palavras/ termos, quanto os registros que possuem tal palavra do dicionário serão tratados como uma lista encadeada
  - Palavras possuirão o endereço da próxima palavra no arquivo em ordem alfabética
    
        Palavra
         _____________________________________
        | termo | &primeiroReg | &proxPalavra |
         ¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨

        Registro
         _____________________________
        | &registroBD | &proxRegistro |
         ¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨


* Quando um registro for apagado, seu endereço na lista será colocado no final do arquivo
  - No início do arquivo haverá a posição de onde começa os espaços vazios

        Arquivo
         ____________________________________________________________________________________________
        | posLixo | posPrimeiraPalavra | tamPalavra|Palavra | Registro | ... | &lixo1 | &lixo2 | ... |
         ¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨
      

* Na inserção de uma nova palavra, os endereços dos lixos serão alocados para a memória primária e posteriormente adicionados ao final do arquivo
* Na remoção de uma palavra, a palavra permanecerá no banco até que seja feita a limpeza do arquivo
* O método de limpeza implica na remoção de todos os lixos que estão endereçados no fim do arquivo além da exclusão definitiva das palavras com 'primeiroReg' igual a nulo `não implementado`
