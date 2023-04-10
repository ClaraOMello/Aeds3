# Decisões - [Árvore B](../ArvoreB.java)
* Cada nó da árvore é composto por:
  - `n` - quantidade de registros escritos
  - `p` - endereço de outro nó
  - `r` - registros

 Os registros possuem a chave (id) e o endereço desse registro no banco de dados

      Registro
       __________
      | id | end |
       ¨¨¨¨¨¨¨¨¨¨

       No
       ______________________________________________________________________________
      | n | p1 | r1 | p2 | r2 | p3 | r3 | p4 | r4 | p5 | r5 | p6 | r6 | p7 | r7 | p8 |
       ¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨

* Nós criados a partir da divisão de outros são criados à direita, ou seja, nó indicado pelo ponteiro da esquerda do registro promovido possui mais itens
* Registro promovido corresponde ao valor [ordem/2] dos registros de um nó:
  - Considerando uma árvore de ordem 8 (como é feito na classe), registro promovido será o quarto

Nó inicial 

       _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ 
      | . 1 . 2 . 3 . 4 . 5 . 6 . 7 . |
       ¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨ 

Acrescentar o 8

                           _ _ _ _ _ _ _ _ _
                          |. 5 . . . . . . .|
                           ¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨
       _ _ _ _ _ _ _ _ _ _ _ _ _         _ _ _ _ _ _ _ _ _ _ _ _ 
      | . 1 . 2 . 3 . 4 . . . . |       | . 6 . 7 . 8 . . . . . | 
       ¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨         ¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨
