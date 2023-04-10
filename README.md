# Aeds3 - Trabalho Prático

Base de dados: [Kaggle - GoodReads Best books](https://www.kaggle.com/datasets/thedevastator/comprehensive-overview-of-52478-goodreads-best-b)

## Tp1 - CRUD e ordenação externa

[Book](Book.java)
* Construção da base de dados através de um csv
* Criação, leitura, atualização e remoção de registros

[Ordenação Externa](OrdenacaoExterna.java)
* Intercalação balanceada comum
* Intercalação balanceada com blocos de tamanho variável
* Intercalação balanceada com seleção de substituição

## Tp2 - Indexação

[Índice Árvore B de ordem 8](ArvoreB.java)
* Acesso a registro nessa indexação vai ser por item, ou seja, não haverá leitura sequencial (o que justificaria o uso de uma árvore B+ ou B*)

[Índice Hashing Estendido](Hashing.java)

[Índice Lista Invertida](ListaInvertida.java)
* Campos escolhidos: título e gêneros
