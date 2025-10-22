package Lexica;

import java.util.*;

// Classe Lexica.Scanner: responsável por analisar o código-fonte e gerar a lista de tokens
public class Scanner {
    private final String source; // Armazena o texto do código-fonte a ser analisado
    private final List<Token> tokens = new ArrayList<>(); // Lista de tokens reconhecidos
    private int start = 0;    // Indica o início do token atual
    private int current = 0;  // Indica a posição atual de leitura no código-fonte
    private int line = 1;     // Indica o número da linha atual para relatórios de erro
    private static final Map<String, TokenType> keywords; // Mapeia palavras-chave para tipos de token


    // Bloco static para inicializar o mapa de palavras-chave e seus tipos
    static {
        keywords = new HashMap<>();
        keywords.put("VAR", TokenType.VAR);                   // Declaração de variável
        keywords.put("FUNCAO", TokenType.FUN);                // Declaração de função
        keywords.put("RETORNA", TokenType.RETURN);              // Retorno de função
        keywords.put("ESCREVEAI", TokenType.PRINT);           // Comando de impressão
        keywords.put("FAZAVOLTA", TokenType.FOR);             // Laço for
        keywords.put("VOLTAINFINITA", TokenType.WHILE);       // Laço while
        keywords.put("INTEIRO", TokenType.INT);               // Tipo inteiro
        keywords.put("QUEBRADO", TokenType.FLOAT);            // Tipo float
        keywords.put("SE", TokenType.IF);                     // Estrutura de decisão
        keywords.put("SENAO", TokenType.ELSE);                // Else de decisão
        keywords.put("ISSOAI", TokenType.TRUE);               // Valor booleano true
        keywords.put("MENTIRA", TokenType.FALSE);             // Valor booleano false
        keywords.put("NULO", TokenType.NIL);                  // Valor nulo
        keywords.put("ESCOLHEAI", TokenType.SWITCH);          // Estrutura switch
        keywords.put("CASO", TokenType.CASE);                 // case do switch
        keywords.put("PADRAO", TokenType.DEFAULT);            // default do switch
        keywords.put("LEIA", TokenType.INPUT);                // Leitura de entrada
        keywords.put("PAREI", TokenType.BREAK);               // break de laço/switch
    }

    // Construtor: recebe o código-fonte a ser analisado
    public Scanner(String source) {
        this.source = source;
    }

    // Metodo principal que varre toda o source e gera os tokens encontrados.
    public List<Token> scanTokens() {
        while (!isAtEnd()) {         // Enquanto não chegou ao fim do código-fonte...
            start = current;         // Marca início do próximo token
            scanToken();             // Tenta identificar o token atual
        }
        // Ao final, adiciona um token especial para indicar o fim do arquivo (EOF)
        tokens.add(new Token(TokenType.EOF, "", null, line, current));
        return tokens;
    }

    // Analisa o próximo caractere e adiciona o token correspondente
    private void scanToken() {
        char c = advance(); // Avança para o próximo caractere e retorna ele
        switch (c) {
            case ':': addToken(TokenType.COLON); break;         // Dois pontos
            case ' ': case '\r': case '\t': break;              // Espaços em branco são ignorados
            case '\n': line++; break;                           // Quebra de linha: incrementa o contador de linhas
            case '/':
                if (match('/')) {                               // Comentário de linha
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else if (match('*')) {                        // Comentário de bloco
                    while (!(peek() == '*' && peekNext() == '/') && !isAtEnd()) advance();
                    if (!isAtEnd()) { advance(); advance(); }   // Consome '*/' que finaliza o comentário de bloco
                } else addToken(TokenType.SLASH);               // Sinon, operador barra normal
                break;
            case '(': addToken(TokenType.LEFTPAREN); break;     // Parêntese esquerdo
            case ')': addToken(TokenType.RIGHTPAREN); break;    // Parêntese direito
            case '{': addToken(TokenType.LEFTBRACE); break;     // Chave esquerda
            case '}': addToken(TokenType.RIGHTBRACE); break;    // Chave direita
            case '[': addToken(TokenType.LEFT_BRACKET); break;  // Colchete esquerdo
            case ']': addToken(TokenType.RIGHT_BRACKET); break; // Colchete direito
            case ',': addToken(TokenType.COMMA); break;         // Vírgula
            case ';': addToken(TokenType.SEMICOLON); break;     // Ponto e vírgula
            case '=': addToken(match('=') ? TokenType.EQUALEQUAL : TokenType.EQUAL); break; // Igual ou igual duplo
            case '!': addToken(match('=') ? TokenType.BANGEQUAL : TokenType.BANG); break;   // Diferente (!) ou (!=)
            case '+': addToken(TokenType.PLUS); break;          // Mais
            case '-': addToken(TokenType.MINUS); break;         // Menos
            case '*': addToken(TokenType.STAR); break;          // Multiplicação
            case '>': addToken(match('=') ? TokenType.GREATEREQUAL : TokenType.GREATER); break; // Maior ou maior igual
            case '<': addToken(match('=') ? TokenType.LESSEQUAL : TokenType.LESS); break;       // Menor ou menor igual
            case '"': string(); break;                          // Início de string
            case '%': addToken(TokenType.PERCENT); break;       // Porcentagem
            default:
                if (Character.isLetter(c)) identifier();        // Se for letra, começa identificador ou palavra-chave
                else if (isDigit(c)) number();                  // Se for dígito, analisa número
                else System.err.println("Caractere inválido (" + c + ") na linha " + line); // Outro caractere: erro
        }
    }

    // Analisa um identificador ou palavra-chave
    private void identifier() {
        while (Character.isLetterOrDigit(peek())) advance(); // Consome letras/dígitos
        String text = source.substring(start, current);      // Pega o texto
        TokenType type = keywords.get(text);                 // É palavra-chave ou identificador normal?
        if (type != null) {
            addToken(type);
        }
        else {
            addToken(TokenType.IDENTIFIER);
        }
    }

    // Retorna se o caractere é um dígito
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    // Analisa e adiciona um número (inteiro ou decimal)
    private void number() {
        while (isDigit(peek())) advance(); // Consome parte inteira
        if (peek() == '.' && isDigit(peekNext())) {
            advance();                     // Consome o ponto
            while (isDigit(peek())) advance(); // Consome parte decimal
        }
        String texto = source.substring(start, current);
        if (texto.contains(".")) {
            addToken(TokenType.NUMBER, Double.parseDouble(texto)); // Número decimal
        }
        else addToken(TokenType.NUMBER, Integer.parseInt(texto));                       // Número inteiro
    }

    // Analisa e adiciona uma string entre aspas
    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++; // Contabiliza novas linhas em strings multi-linhas
            advance();
        }
        if (isAtEnd()) { // Se acabou o código e não fechou aspas, erro
            System.err.println("String não finalizada na linha " + line);
            return;
        }
        advance(); // Consome a aspas final
        String value = source.substring(start + 1, current - 1);
        addToken(TokenType.STRING, value); // Adiciona token do tipo string
    }

    // Verifica se o próximo caractere corresponde ao esperado e avança
    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;
        current++;
        return true;
    }

    // Retorna o caractere atual sem consumir
    private char peek() {
        if (isAtEnd()) {
            return '\0';
        }
        return source.charAt(current);
    }

    // Retorna o caractere seguinte sem consumir
    private char peekNext() {
        if (current + 1 >= source.length()){
            return '\0';
        }
        return source.charAt(current + 1);
    }

    // Consome e retorna o próximo caractere do source
    private char advance() {
        return source.charAt(current++);
    }

    // Retorna true se o fim do texto foi alcançado
    private boolean isAtEnd() {
        return current >= source.length();
    }

    // Adiciona um token simples (sem valor literal)
    private void addToken(TokenType type) {
        addToken(type, null);
    }

    // Adiciona um token ao vetor de tokens, informando tipo, lexema, valor, linha e coluna
    private void addToken(TokenType type, Object literal) {
        String lexeme = source.substring(start, current); // O texto do token no source
        int column = start;                               // Coluna: início do token
        tokens.add(new Token(type, lexeme, literal, line, column)); // Adiciona token na lista
    }
}
