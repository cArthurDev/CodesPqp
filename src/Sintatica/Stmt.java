package Sintatica;

import Lexica.Token;

import java.util.List;

// Classe abstrata para todos os tipos de comandos (statements) da linguagem
public abstract class Stmt {

    // Visitor para padrões do interpretador (Visitor Pattern)
    public interface Visitor<R> {
        R visitPrintStmt(Print stmt);
        R visitVarStmt(Var stmt);
        R visitFunctionStmt(Function stmt);
        R visitReturnStmt(Return stmt);
        R visitIfStmt(If stmt);
        R visitBlockStmt(Block stmt);
        R visitExpressionStmt(Expression stmt);
        R visitWhileStmt(While stmt);
        R visitBreakStmt(Break stmt);
        R visitSwitchStmt(Switch stmt);
        R visitInputStmt(Input stmt);
    }

    public abstract <R> R accept(Visitor<R> visitor);

    // Comando de impressão de valor
    public static class Print extends Stmt {
        public final Expr expression;
        Print(Expr expression) { this.expression = expression; }
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitPrintStmt(this);
        }
    }

    // Declaração de variável
    public static class Var extends Stmt {
        public final Token type;
        public final Token name;
        public final Expr initializer;
        Var(Token type, Token name, Expr initializer) {
            this.type = type;
            this.name = name;
            this.initializer = initializer;
        }
        Var(Token name, Expr initializer) {
            this.type = null;
            this.name = name;
            this.initializer = initializer;
        }
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitVarStmt(this);
        }
    }

    // Definição de função
    public static class Function extends Stmt {
        public final Token name;
        public final List<Token> parameters;
        public final List<Stmt> body;
        Function(Token name, List<Token> parameters, List<Stmt> body) {
            this.name = name;
            this.parameters = parameters;
            this.body = body;
        }
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitFunctionStmt(this);
        }
    }

    // Comando return
    public static class Return extends Stmt {
        public final Token keyword;
        public final Expr value;
        Return(Token keyword, Expr value) {
            this.keyword = keyword;
            this.value = value;
        }
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitReturnStmt(this);
        }
    }

    // Estrutura condicional if/else
    public static class If extends Stmt {
        public final Expr condition;
        public final Stmt thenBranch;
        public final Stmt elseBranch;
        If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitIfStmt(this);
        }
    }

    // Bloco de comandos entre chaves
    public static class Block extends Stmt {
        public final List<Stmt> statements;
        Block(List<Stmt> statements) { this.statements = statements; }
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBlockStmt(this);
        }
    }

    // Comando de expressão simples
    public static class Expression extends Stmt {
        public final Expr expr;
        Expression(Expr expr) { this.expr = expr; }
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitExpressionStmt(this);
        }
    }

    // Laço while tradicional
    public static class While extends Stmt {
        public final Expr condition;
        public final Stmt body;
        While(Expr condition, Stmt body) {
            this.condition = condition;
            this.body = body;
        }
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitWhileStmt(this);
        }
    }

    // Estrutura switch-case completa
    public static class Switch extends Stmt {
        public final Expr expr;
        public final List<Case> cases;
        public final Case defaultCase;
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
        public final Expr value;
        public final Stmt stmt;
        public Case(Expr value, Stmt stmt) {
            this.value = value;
            this.stmt = stmt;
        }
    }

    // Comando de leitura de entrada (input)
    public static class Input extends Stmt {
        public final Token name;
        Input(Token name) { this.name = name; }
        @Override
        public <R> R accept(Visitor<R> visitor) { return visitor.visitInputStmt(this); }
    }

    // Comando break para laço ou switch
    public static class Break extends Stmt {
        public Break() {}
        public <R> R accept(Visitor<R> visitor) { return visitor.visitBreakStmt(this); }
    }
}
