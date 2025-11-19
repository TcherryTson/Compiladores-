# ⚙️ Compilador e Máquina Virtual (VM) para Linguagem Simples

Este projeto consiste em um compilador e uma Máquina Virtual (VM) para uma linguagem de programação simples, implementado em Java. O compilador segue as etapas clássicas de front-end (Análise Léxica, Sintática e Semântica) e back-end (Geração de Bytecode), finalizando com a execução do código pela Máquina Virtual baseada em pilha.

## 🛠️ Tecnologias Utilizadas

* **Linguagem de Implementação:** Java

## 🗺️ Estrutura do Projeto

O código está organizado logicamente em fases de compilação, facilitando a compreensão do fluxo de execução.

### 1. Análise Léxica (Scanning)

A fase léxica é responsável por transformar a sequência de caracteres do código-fonte em uma sequência de *Tokens*.

| Arquivo | Descrição |
| :--- | :--- |
| **`Escaner.java`** | Implementa o *scanner* que lê o código-fonte, identifica palavras-chave (como `si`, `mientras`, `imprimir`), identificadores e literais, e os converte em tokens. |
| **`TokenType.java`** | Enumeração que define todos os tipos de *tokens* válidos no idioma (operadores, delimitadores, palavras-chave, etc.). |
| **`Token.java`** | Classe que representa uma unidade léxica, armazenando seu tipo, *lexema*, valor literal e linha no código-fonte. |

---

### 2. Análise Sintática e Semântica (Parsing & Resolving)

Esta fase recebe os *tokens* e constrói a Árvore de Sintaxe Abstrata (AST), verificando a estrutura gramatical e a validade contextual (tipos e escopo).

| Arquivo | Descrição |
| :--- | :--- |
| **`AnalizadorSintactico.java`** | Implementa o *parser* que consome os *tokens* para criar a AST, seguindo a gramática da linguagem (regras como `declaracionOSentencia`, `expresion`, `sentenciaSi`). |
| **`Expr.java`** | Define a estrutura das **Expressões** na AST (como `Binario`, `Literal`, `Asignar`), usando o padrão *Visitor*. |
| **`Sentencia.java`** | Define a estrutura das **Sentenças** na AST (como `Declaracion`, `Bloque`, `Si`, `Mientras`, `Imprimir`), também usando o padrão *Visitor*. |
| **`AnalizadorSemantico.java`** | Realiza a análise semântica na AST, verificando a compatibilidade de tipos em operações (ex: entre `ENTERO` e `FLOTANTE`) e garantindo que as variáveis sejam usadas corretamente dentro de seus escopos. |
| **`Tipo.java`** | Enumeração dos tipos de dados primitivos suportados pela linguagem (`ENTERO`, `FLOTANTE`, `BOOLEANO`, `CADENA`, etc.). |

---

### 3. Geração de Bytecode e Máquina Virtual (VM)

A fase de *back-end* traduz a AST validada em instruções de baixo nível e as executa.

| Arquivo | Descrição |
| :--- | :--- |
| **`GeneradorByteCode.java`** | Percorre a AST e emite as instruções (`OpCode`) e operandos para o `Fragmento`, tratando a lógica de variáveis globais e locais, e implementando estruturas de controle (`Si`, `Mientras`) com instruções de salto (`SALTAR_SI_FALSO`, `LOOP`). |
| **`OpCode.java`** | Enumeração de todas as instruções da Máquina Virtual, como `SUMAR`, `CONSTANTE`, `DEFINIR_GLOBAL`, `SALTAR` e `RETORNAR`. |
| **`Fragmento.java`** | Estrutura que armazena o código de bytecode (`codigo`), as constantes e os operandos. Contém também o método `patch` para resolver saltos e `imprimirDisassembly` para depuração. |
| **`Mv.java`** | A **Máquina Virtual** baseada em pilha. Ela carrega o `Fragmento` de bytecode, usa um ponteiro de instrução (`ip`) e executa as operações lendo e manipulando a pilha (`stack`) e o mapa de variáveis globais (`globals`). |

---

### 4. Principal (Core)

| Arquivo | Descrição |
| :--- | :--- |
| **`Lox.java`** | Classe principal e ponto de entrada do programa. Orquestra a execução de todas as fases: léxica, sintática, semântica e interpretação pela VM. Lida também com o relatório de erros. |

## ▶️ Como Executar

Para rodar o compilador, execute a classe principal `Lox.java`, passando o caminho para o arquivo de código-fonte como argumento. Por padrão, ele tenta executar o arquivo `teste.txt` se nenhum argumento for fornecido.

```bash
# Executa o arquivo 'teste.txt' (assumindo que ele está na raiz do projeto)
java Lox.java

# Ou, especificando um arquivo:
java Lox.java meu_codigo.lox
