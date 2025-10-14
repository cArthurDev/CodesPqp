import java.util.List;

// Classe abstrata para todos os tipos de comandos (statements) da linguagem
abstract class Stmt {

    // Visitor para padrões do interpretador (Visitor Pattern)
    interface Visitor<R> {
        R visitPrintStmt(Print stmt);         // Comando de impressão
        R visitVarStmt(Var stmt);             // Declaração de variável
        R visitFunctionStmt(Function stmt);   // Declaração de função
        R visitReturnStmt(Return stmt);       // Comando return
        R visitIfStmt(If stmt);               // Condicional if/else
        R visitBlockStmt(Block stmt);         // Bloco de comandos
        R visitExpressionStmt(Expression stmt); // Comando de expressão simples
        R visitWhileStmt(While stmt);         // Laço while
        R visitBreakStmt(Break stmt);         // break para laço/switch
        R visitSwitchStmt(Switch stmt);       // switch-case
        R visitInputStmt(Input stmt);         // Leitura de entrada
    }

    // Qualquer comando deve aceitar um visitor
    abstract <R> R accept(Visitor<R> visitor);

    // Comando de impressão de valor
    static class Print extends Stmt {
        final Expr expression; // Expressão a ser impressa
        Print(Expr expression) { this.expression = expression; }
        @Override <R> R accept(Visitor<R> visitor) {
            return visitor.visitPrintStmt(this);
        }
    }

    // Declaração de variável
    static class Var extends Stmt {
        final Token type;           // Tipo da variável (pode ser nulo)
        final Token name;           // Nome da variável
        final Expr initializer;     // Expressão de inicialização

        // Construtor para var explícita: ex: inteiro x = 5;
        Var(Token type, Token name, Expr initializer) {
            this.type = type;
            this.name = name;
            this.initializer = initializer;
        }

        // Construtor para var implícita (sem tipo): ex: VAR x = 5;
        Var(Token name, Expr initializer) {
            this.type = null;
            this.name = name;
            this.initializer = initializer;
        }

        @Override <R> R accept(Visitor<R> visitor) {
            return visitor.visitVarStmt(this);
        }
    }

    // Definição de função
    static class Function extends Stmt {
        final Token name;                // Nome da função
        final List<Token> parameters;    // Lista de parâmetros
        final List<Stmt> body;           // Corpo da função (bloco de comandos)
        Function(Token name, List<Token> parameters, List<Stmt> body) {
            this.name = name;
            this.parameters = parameters;
            this.body = body;
        }
        @Override <R> R accept(Visitor<R> visitor) {
            return visitor.visitFunctionStmt(this);
        }
    }

    // Comando return
    static class Return extends Stmt {
        final Token keyword;   // Token RETURN
        final Expr value;      // Valor a retornar (pode ser nulo)
        Return(Token keyword, Expr value) {
            this.keyword = keyword;
            this.value = value;
        }
        @Override <R> R accept(Visitor<R> visitor) {
            return visitor.visitReturnStmt(this);
        }
    }

    // Estrutura condicional if/else
    static class If extends Stmt {
        final Expr condition;         // Expressão condicional
        final Stmt thenBranch;        // Bloco do if
        final Stmt elseBranch;        // Bloco do else (pode ser nulo)
        If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }
        @Override <R> R accept(Visitor<R> visitor) {
            return visitor.visitIfStmt(this);
        }
    }

    // Bloco de comandos entre chaves
    static class Block extends Stmt {
        final List<Stmt> statements; // Lista de comandos do bloco
        Block(List<Stmt> statements) { this.statements = statements; }
        @Override <R> R accept(Visitor<R> visitor) {
            return visitor.visitBlockStmt(this);
        }
    }

    // Comando de expressão simples (ex: chamada de função ou expressão isolada)
    static class Expression extends Stmt {
        final Expr expr; // Expressão a ser avaliada
        Expression(Expr expr) { this.expr = expr; }
        @Override <R> R accept(Visitor<R> visitor) {
            return visitor.visitExpressionStmt(this);
        }
    }

    // Laço while tradicional
    static class While extends Stmt {
        final Expr condition; // Condição do laço
        final Stmt body;      // Corpo do laço
        While(Expr condition, Stmt body) {
            this.condition = condition;
            this.body = body;
        }
        @Override <R> R accept(Visitor<R> visitor) {
            return visitor.visitWhileStmt(this);
        }
    }

    // Estrutura switch-case completa
    public static class Switch extends Stmt {
        public final Expr expr;                // Expressão a ser comparada
        public final List<Case> cases;         // Lista de casos
        public final Case defaultCase;         // Caso padrão
        public Switch(Expr expr, List<Case> cases, Case defaultCase) {
            this.expr = expr;
            this.cases = cases;
            this.defaultCase = defaultCase;
        }
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSwitchStmt(this);
        }
    }

    // Define um caso de switch-case (CASO ou PADRÃO)
    public static class Case {
        public final Expr value; // Valor que ativa o caso (null se PADRÃO)
        public final Stmt stmt;  // Bloco de comandos a ser executado
        public Case(Expr value, Stmt stmt) {
            this.value = value;
            this.stmt = stmt;
        }
    }

    // Comando de leitura de entrada (input)
    static class Input extends Stmt {
        final Token name; // Nome da variável de destino
        Input(Token name) { this.name = name; }
        @Override <R> R accept(Visitor<R> visitor) { return visitor.visitInputStmt(this); }
    }

    // Comando break para laço ou switch
    public static class Break extends Stmt {
        public Break() {}
        public <R> R accept(Visitor<R> visitor) { return visitor.visitBreakStmt(this); }
    }
}
