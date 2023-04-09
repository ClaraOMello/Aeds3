# Decisões - [Árvore B](../ArvoreB.java)
1. Nós criados a partir da divisão de outros são criados à direita, ou seja, nó indicado pelo ponteiro da esquerda do registro promovido possui mais itens
2. Registro promovido corresponde ao valor [ordem/2] dos registros de um nó:
  - Considerando uma árvore de ordem 8 (como é feito na classe), registro promovido será o quarto

 
- Nó inicial 
     _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ 
    | . 1 . 2 . 3 . 4 . 5 . 6 . 7 . |
     ¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨ 

- Acrescentar o 8
                         _ _ _ _ _ _ _ _ _
                        |. 5 . . . . . . .|
                         ¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨
     _ _ _ _ _ _ _ _ _ _ _ _ _         _ _ _ _ _ _ _ _ _ _ _ _ 
    | . 1 . 2 . 3 . 4 . . . . |       | . 6 . 7 . 8 . . . . . | 
     ¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨         ¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨

