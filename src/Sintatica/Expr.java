package Sintatica;

import Lexica.Token;

import java.util.List;

// Representa uma expressão na árvore sintática abstrata (AST).
public abstract class Expr {

    // Interface Visitor expandida para todas as expressões
    public interface Visitor<R> {
        R visitAssignExpr(Assign expr);
        R visitBinaryExpr(Binary expr);
        R visitCallExpr(Call expr);
        R visitGroupingExpr(Grouping expr);
        R visitLiteralExpr(Literal expr);
        R visitUnaryExpr(Unary expr);
        R visitVariableExpr(Variable expr);
        R visitIncrementoExpr(Incremento expr);
        R visitDecrementoExpr(Decremento expr);
    }

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

    // Incremento: i++
    public static class Incremento extends Expr {
        public final Token name, operator;
        public final boolean prefix;
        public Incremento(Token name, Token operator, boolean prefix) {
            this.name = name;
            this.operator = operator;
            this.prefix = prefix;
        }
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitIncrementoExpr(this);
        }
    }

    // Decremento: i--
    public static class Decremento extends Expr {
        public final Token name, operator;
        public final boolean prefix;
        public Decremento(Token name, Token operator, boolean prefix) {
            this.name = name;
            this.operator = operator;
            this.prefix = prefix;
        }
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitDecrementoExpr(this);
        }
    }
}
