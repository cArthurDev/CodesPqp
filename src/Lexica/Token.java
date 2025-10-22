package Lexica;

// Classe que representa um token reconhecido pelo analisador léxico (Lexica.Scanner)
public class Token {
    public final TokenType type; // Tipo do token (ex: IDENTIFIER, PLUS, IF, NUMBER, etc)
    public final String lexeme; // Lexema: o texto literal do token no código-fonte
    public final Object literal; // Valor literal (caso seja um número, string, etc) - pode ser null
    public final int line; // Número da linha onde o token foi encontrado
    public final int column; // Coluna onde começa o token na linha

    // Construtor: inicializa todos os campos do token
    public Token(TokenType type, String lexeme, Object literal, int line, int column) {
        this.type = type;         // Tipo do token
        this.lexeme = lexeme;     // Texto do token
        this.literal = literal;   // Valor literal, se existir
        this.line = line;         // Linha de origem
        this.column = column;     // Coluna de origem
    }

    // Representação em string do token para debug e mensagens de erro
    @Override
    public String toString() {
        return type + " " + lexeme + " " + literal + " linha=" + line + " coluna=" + column;
    }
}


