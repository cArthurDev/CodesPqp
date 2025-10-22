package Sintatica;

// Enum que define os níveis de precedência dos operadores
public enum Precedence {
    NONE,         // Nenhuma precedência (usado para tokens que não participam de expressões)
    ASSIGNMENT,   // Atribuição (ex: =)
    EQUALITY,     // Igualdade e diferença (ex: ==, !=)
    COMPARISON,   // Comparação (ex: <, >, <=, >=)
    TERM,         // Soma e subtração (ex: +, -)
    FACTOR,       // Multiplicação, divisão e resto (ex: *, /, %)
    UNARY,        // Operadores unários (ex: !, -)
    CALL,         // Chamada de função/metodo e acesso
    PRIMARY       // Literais, identificadores, expressões agrupadas
}

