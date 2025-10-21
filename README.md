# Codes Pqp

Bem-vindo ao repositório da **Codes Pqp** — Programa Qualitativo Perfeito, uma linguagem de programação brasileira feita para ser acessível, divertida e didática!

![Logo Codes Pqp](codespqp_logo.png)

---

## ✨ Sobre

**Codes Pqp** é uma linguagem de programação criada para unir simplicidade, expressividade e identidade nacional. Sua sintaxe é baseada no português, usando comandos fáceis de entender e mensagens amigáveis, pensadas tanto para quem está aprendendo quanto para quem gosta de praticidade.

## 🧰 O que o Codes PQP tem

- **Comandos de declaração de variável**
  - `VAR x = valor;`
  - Suporte a inicialização opcional

- **Impressão na tela**
  - `ESCREVEAI valor;`

- **Leitura de entrada**
  - `LEAI nomeVariavel;`

- **Estrutura condicional**
  - `SE (condicao) { ... } SENAO { ... }`
  - IF/ELSE com blocos ou comandos simples

- **Laço de repetição**
  - `VOLTAINFINITA (condicao) { ... }`
  - Comando `PAREI;` para sair do loop

- **Atribuição**
  - `x = valor;`

- **Blocos de código**
  - `{ ... }`

- **Funções do usuário**
  - `FUNCAO nome(parâmetros) { corpo }`
  - Parâmetros, escopo local e `RETOR valor;`

- **Switch-case**
  - `ESCOLHEAI expr { CASO valor: bloco PADRAO: bloco }`

- **Tipos suportados**
  - Inteiro (`INTEIRO`), Float (`QUEBRADO`), Booleano (`ISSOAI`/`MENTIRA`), String (`"texto"`), Nulo (`NULO`)

- **Operadores e expressões**
  - `+`, `-`, `*`, `/`, `%`, `==`, `!=`, `>`, `<`, `>=`, `<=`, `!`, parênteses

- **Mensagens de erro em português**

- **Árvore de sintaxe abstrata (AST) já implementada**

- **Interpretador completo (controle de variáveis, escopo e funções)**

---

## ❌ Adições Futuras

- Suporte a arrays, listas ou matrizes
- Objetos ou estrutura de dados complexos
- Operações com arquivos, rede ou biblioteca padrão avançada

---

## 🚀 Exemplo de código

<pre> ```// Declaração de variáveis
VAR x = 10;
VAR y = 5;

// Operação aritmética e impressão
VAR soma = x + y;
ESCREVEAI "Soma: " + soma;

// Estrutura condicional
SE (x > y) {
ESCREVEAI "x é maior que y";
} SENAO {
ESCREVEAI "y é maior ou igual a x";
}

// Laço de repetição
VAR i = 0;
VOLTAINFINITA (i < 3) {
ESCREVEAI "Contador: " + i;
i = i + 1;
SE (i == 3) {
ESCREVEAI "Fim da contagem!";
PAREI;
}
}

// Função simples e chamada
FUNCAO dobro(n) {
RETOR n * 2;
}

ESCREVEAI "Dobro de y: " + dobro(y);
``` </pre>
---

## 📚 Estrutura do Projeto

- `Scanner.java` — análise léxica (transforma texto em tokens)
- `Token.java` / `TokenType.java` — estrutura dos tokens da linguagem
- `Parser.java` — análise sintática (criação da AST)
- `Expr.java` / `Stmt.java` — definição da árvore sintática abstrata
- `Interpreter.java` — execução e análise semântica; faz a linguagem funcionar!

---

## 🏗️ Instalação e uso básico

1. Clone este repositório:
git clone https://github.com/seu-usuario/codes-pqp.git
cd codes-pqp

2. Compile os arquivos Java:
javac *.java

3. Rode o interpretador (ajuste o comando conforme sua classe principal):
java Main

4. Escreva seus códigos Codes Pqp no programa.txt e execute!

---

## 🎯 Por que “Pqp”?

O “Pqp” de Codes Pqp significa **Programa Qualitativo Perfeito**, porque a meta é: código simples, experiência marcante, e orgulho brasileiro!

---

## 🎨 Logo

Logo moderna que mistura programação, criatividade e tecnologia, com um mascote astronauta usando notebook em tons de roxo.

---

## 👨‍💻 Autor

Feita por [cArthurDev], [Maria-Cassis], [WaisGH]  
2025  
Inspirada por um trabalho da Matéria `Compiladores` o jeitinho brasileiro de ensinar e resolver problemas.

---

## 📝 Licença

Este projeto é open-source, pode ser adaptado, estudado e remixado para fins didáticos e de aprendizado.

---

## 📢 Contato

Sugestões, ideias ou bugs? Abra uma issue ou envie um pull request!
