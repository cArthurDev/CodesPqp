import java.util.List;

// Representa uma expressão na árvore sintática abstrata (AST).
public abstract class Expr {

    // Interface Visitor: define métodos para cada tipo de expressão.
    public interface Visitor<R> {
        R visitAssignExpr(Assign expr);        // Para atribuição
        R visitBinaryExpr(Binary expr);        // Para operações binárias (+, -, etc)
        R visitCallExpr(Call expr);            // Para chamada de função/metodo
        R visitGroupingExpr(Grouping expr);    // Para expressões agrupadas (parenteses)
        R visitLiteralExpr(Literal expr);      // Para valores literais
        R visitUnaryExpr(Unary expr);          // Para operações unárias (-, !, etc)
        R visitVariableExpr(Variable expr);    // Para uso de variáveis
    }

    // Cada expressão sabe como "aceitar" um visitante.
    public abstract <R> R accept(Visitor<R> visitor);

    // Expressão de atribuição: x = 2
    public static class Assign extends Expr {
        public final Token name;
        public final Expr value;
        public Assign(Token name, Expr value) {
            this.name = name;
            this.value = value;
        }
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitAssignExpr(this);
        }
    }

    // Expressão binária: x + y
    public static class Binary extends Expr {
        public final Expr left;
        public final Token operator;
        public final Expr right;
        public Binary(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }
    }

    // Chamada de função: f(x, y)
    public static class Call extends Expr {
        public final Expr callee;
        public final Token paren;
        public final List<Expr> arguments;
        public Call(Expr callee, Token paren, List<Expr> arguments) {
            this.callee = callee;
            this.paren = paren;
            this.arguments = arguments;
        }
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitCallExpr(this);
        }
    }

    // Agrupamento: (x + y)
    public static class Grouping extends Expr {
        public final Expr expression;
        public Grouping(Expr expression) {
            this.expression = expression;
        }
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitGroupingExpr(this);
        }
    }

    // Valor literal: 10, "texto", true
    public static class Literal extends Expr {
        public final Object value;
        public Literal(Object value) {
            this.value = value;
        }
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpr(this);
        }
    }

    // Operação unária: -x, !flag
    public static class Unary extends Expr {
        public final Token operator;
        public final Expr right;
        public Unary(Token operator, Expr right) {
            this.operator = operator;
            this.right = right;
        }
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpr(this);
        }
    }

    // Uso de variável: x
    public static class Variable extends Expr {
        public final Token name;
        public Variable(Token name) {
            this.name = name;
        }
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitVariableExpr(this);
        }
    }

}
