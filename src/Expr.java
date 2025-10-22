import java.util.List;

// Representa uma expressão na árvore sintática abstrata (AST).
public abstract class Expr {
    // Adicione campo para a linha na raiz (pode ser sobrescrito)
    public abstract int getLine();

    // Método para obter nome do tipo da expressão
    public abstract String getTypeName();

    // Interface Visitor: define métodos para cada tipo de expressão.
    public interface Visitor<R> {
        R visitAssignExpr(Assign expr);
        R visitBinaryExpr(Binary expr);
        R visitCallExpr(Call expr);
        R visitGroupingExpr(Grouping expr);
        R visitLiteralExpr(Literal expr);
        R visitUnaryExpr(Unary expr);
        R visitVariableExpr(Variable expr);
    }

    // Cada expressão sabe como "aceitar" um visitante.
    public abstract <R> R accept(Visitor<R> visitor);

    public static class Assign extends Expr {
        public final Token name;
        public final Expr value;
        public Assign(Token name, Expr value) {
            this.name = name;
            this.value = value;
        }
        public <R> R accept(Visitor<R> visitor) { return visitor.visitAssignExpr(this); }
        @Override public int getLine() { return name.line; }
        @Override public String getTypeName() { return "Atribuição"; }
        @Override public String toString() { return name.lexeme + " = " + value; }
    }

    public static class Binary extends Expr {
        public final Expr left;
        public final Token operator;
        public final Expr right;
        public Binary(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }
        public <R> R accept(Visitor<R> visitor) { return visitor.visitBinaryExpr(this); }
        @Override public int getLine() { return operator.line; }
        @Override public String getTypeName() { return "Binária"; }
        @Override public String toString() { return left + " " + operator.lexeme + " " + right; }
    }

    public static class Call extends Expr {
        public final Expr callee;
        public final Token paren;
        public final List<Expr> arguments;
        public Call(Expr callee, Token paren, List<Expr> arguments) {
            this.callee = callee;
            this.paren = paren;
            this.arguments = arguments;
        }
        public <R> R accept(Visitor<R> visitor) { return visitor.visitCallExpr(this); }
        @Override public int getLine() { return paren.line; }
        @Override public String getTypeName() { return "Chamada de Função"; }
        @Override public String toString() { return callee + "(" + arguments + ")"; }
    }

    public static class Grouping extends Expr {
        public final Expr expression;
        public Grouping(Expr expression) { this.expression = expression; }
        public <R> R accept(Visitor<R> visitor) { return visitor.visitGroupingExpr(this); }
        @Override public int getLine() { return expression.getLine(); }
        @Override public String getTypeName() { return "Agrupamento"; }
        @Override public String toString() { return "(" + expression + ")"; }
    }

    public static class Literal extends Expr {
        public final Object value;
        public Literal(Object value) { this.value = value; }
        public <R> R accept(Visitor<R> visitor) { return visitor.visitLiteralExpr(this); }
        @Override public int getLine() { return -1; } // Se quiser, pode atribuir o token literal para ter linha
        @Override public String getTypeName() { return "Literal"; }
        @Override public String toString() { return value.toString(); }
    }

    public static class Unary extends Expr {
        public final Token operator;
        public final Expr right;
        public Unary(Token operator, Expr right) {
            this.operator = operator;
            this.right = right;
        }
        public <R> R accept(Visitor<R> visitor) { return visitor.visitUnaryExpr(this); }
        @Override public int getLine() { return operator.line; }
        @Override public String getTypeName() { return "Unária"; }
        @Override public String toString() { return operator.lexeme + right; }
    }

    public static class Variable extends Expr {
        public final Token name;
        public Variable(Token name) { this.name = name; }
        public <R> R accept(Visitor<R> visitor) { return visitor.visitVariableExpr(this); }
        @Override public int getLine() { return name.line; }
        @Override public String getTypeName() { return "Variável"; }
        @Override public String toString() { return name.lexeme; }
    }
}
