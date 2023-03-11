# Descrição do [Banco de Dados](BD.csv)

## Atributos Selecionados

* bookId
* likedPercent
* pages
* title
* series
* author
* language
* bookFormat
* edition
* publisher
* publishDate
* rating, price
* isbn
* genres
* firstPublishDate

## Tipos de dados escolhidos para cada atributo

    `short` likedPercent
    `int` id[^1], pages
    `String` title, series, author, language, bookFormat, edition, publisher, publishDate
    `float` rating, price
    `char[]` isbn
    `String[]` genres
    `LocalDate` firstPublishDate

[^1] Alteração do bookId para um novo identificador para que fosse possível a utilização dele como um valor numérico

## Representação no arquivo '.bd'

    Primeiros 4 bytes: ultimo id utilizado

    Para cada registro:
    `boolean` lápide (identifica exclusão ou não de registro) 1 byte
    `int` tamanho total do registro 4 bytes
        Atributos:
        `short` 2 bytes
        `int` 4 bytes
        `String` 4 bytes para identificar tamanho da String + a String
        `float` 4 bytes
        `char[]` 13 bytes
        `String[]` 4 bytes para quantidade de elementos no vetor + (4 bytes para identificar tamanho da String + a String) para cada elemento
        `LocalDate` representado no bd como tipo `long` 8 bytes

