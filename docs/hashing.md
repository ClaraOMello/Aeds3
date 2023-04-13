# Decisões
* Cada bucket é composto por:
  - `p` - profundidade local
  - `n` - quantidade de registros escritos (max = 5% da base de dados completa)
  - `primRg` - endereço do primeiro registro

 Os registros possuem a chave (id) e o endereço desse registro no banco de dados

      Registro (mesma classe da árvore B)
       __________
      | id | end |
       ¨¨¨¨¨¨¨¨¨¨

       RegistroHash
       ______________
      | rg | &proxRg |
       ¨¨¨¨¨¨¨¨¨¨¨¨¨¨

       Bucket
       _________________
      | p | n | &primRg |
       ¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨

* Diretório: 
  - Possui a profundida global `P` e 2<sup>P</sup> endereços que apontam para os buckets

* Função hash: h(x) = x % 2<sup>P</sup>

