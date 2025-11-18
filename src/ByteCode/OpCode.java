package ByteCode;

public enum OpCode {
    // --- Opcodes Essenciais ---
    OP_RETURN,      // Retorna (finaliza a execução)
    OP_CONSTANT,    // Carrega um valor constante (ex: 5, "ola", true)
    OP_POP,         // Descarta o valor no topo da pilha

    // --- Opcodes Aritméticos ---
    OP_NEGATE,      // Para o '-' unário (ex: -10)
    OP_ADD,         // +
    OP_SUBTRACT,    // -
    OP_MULTIPLY,    // *
    OP_DIVIDE,      // /

    // --- Opcodes Lógicos ---
    OP_NIL,         // Valor nulo
    OP_TRUE,        // Valor booleano true
    OP_FALSE,       // Valor booleano false
    OP_NOT,         // ! (negação lógica)
    OP_EQUAL,       // ==
    OP_GREATER,     // >
    OP_LESS,        // <

    // --- Opcodes de Variáveis e Escopo ---
    OP_DEFINE_GLOBAL, // Define uma nova variável global
    OP_GET_GLOBAL,    // Lê o valor de uma variável global
    OP_SET_GLOBAL,    // Atribui um valor a uma variável global
    OP_GET_LOCAL,     // Lê uma variável local (da pilha)
    OP_SET_LOCAL,     // Atribui a uma variável local (da pilha)

    // --- Opcodes de Controlo de Fluxo (Condicionais e Laços) ---
    OP_JUMP,            // Salto incondicional (usado no 'else')
    OP_JUMP_IF_FALSE,   // Salta se o topo da pilha for falso (usado no 'SE')
    OP_LOOP,            // Salto para trás (usado em laços 'VOLTAINFINITA')

    // --- Opcodes de Ação ---
    OP_PRINT,           // Imprime o valor no topo da pilha
    OP_INPUT            // NOVO: Lê uma entrada do utilizador
}