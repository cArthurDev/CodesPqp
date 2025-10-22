package Utils;

import Sintatica.Expr;
import Sintatica.Stmt;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class AstHtmlPrinter implements Expr.Visitor<String>, Stmt.Visitor<String> {

    // EXPRESSÕES
    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return tag("Binária (" + expr.operator.lexeme + ")",
                expr.left.accept(this) + expr.right.accept(this));
    }
    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return tag("Agrupamento", expr.expression.accept(this));
    }
    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        return tag("Literal: " + expr.value, "");
    }
    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return tag("Unária (" + expr.operator.lexeme + ")", expr.right.accept(this));
    }
    @Override
    public String visitVariableExpr(Expr.Variable expr) {
        return tag("Variável: " + expr.name.lexeme, "");
    }
    @Override
    public String visitAssignExpr(Expr.Assign expr) {
        return tag("Atribuição: " + expr.name.lexeme, expr.value.accept(this));
    }
    @Override
    public String visitCallExpr(Expr.Call expr) {
        StringBuilder args = new StringBuilder();
        for (Expr e : expr.arguments) args.append(e.accept(this));
        return tag("Chamada de função", expr.callee.accept(this) + args);
    }

    // COMANDOS
    @Override
    public String visitPrintStmt(Stmt.Print stmt) {
        return tag("Print", stmt.expression.accept(this));
    }
    @Override
    public String visitVarStmt(Stmt.Var stmt) {
        String init = stmt.initializer != null ? stmt.initializer.accept(this) : "";
        return tag("Variável: " + stmt.name.lexeme, init);
    }
    @Override
    public String visitFunctionStmt(Stmt.Function stmt) {
        StringBuilder body = new StringBuilder();
        for (Stmt s : stmt.body) body.append(s.accept(this));
        return tag("Função: " + stmt.name.lexeme, body.toString());
    }
    @Override
    public String visitReturnStmt(Stmt.Return stmt) {
        return tag("Return", stmt.value != null ? stmt.value.accept(this) : "");
    }
    @Override
    public String visitIfStmt(Stmt.If stmt) {
        String cond = stmt.condition.accept(this);
        String thenB = stmt.thenBranch.accept(this);
        String elseB = stmt.elseBranch != null ? stmt.elseBranch.accept(this) : "";
        return tag("If", cond + thenB + elseB);
    }
    @Override
    public String visitBlockStmt(Stmt.Block stmt) {
        StringBuilder sb = new StringBuilder();
        for (Stmt s : stmt.statements) sb.append(s.accept(this));
        return tag("Bloco", sb.toString());
    }
    @Override
    public String visitExpressionStmt(Stmt.Expression stmt) {
        return tag("Expressão", stmt.expr.accept(this));
    }
    @Override
    public String visitWhileStmt(Stmt.While stmt) {
        return tag("While", stmt.condition.accept(this) + stmt.body.accept(this));
    }
    @Override
    public String visitBreakStmt(Stmt.Break stmt) {
        return tag("Break", "");
    }
    @Override
    public String visitSwitchStmt(Stmt.Switch stmt) {
        StringBuilder sb = new StringBuilder();
        sb.append(stmt.expr.accept(this));
        if (stmt.cases != null)
            for (Stmt.Case c : stmt.cases) sb.append(tag("Case", c.stmt.accept(this)));
        if (stmt.defaultCase != null)
            sb.append(tag("Default", stmt.defaultCase.stmt.accept(this)));
        return tag("Switch", sb.toString());
    }
    @Override
    public String visitInputStmt(Stmt.Input stmt) {
        return tag("Input: " + stmt.name.lexeme, "");
    }

    // Helper para HTML indentado básico
    private String tag(String title, String children) {
        return "<li><b>" + title + "</b>" +
                (children.isEmpty() ? "" : "<ul>" + children + "</ul>") +
                "</li>";
    }

    // Gera HTML simples, indentado
    public void gerarHtml(List<Stmt> statements, String caminhoArquivo) {
        StringBuilder html = new StringBuilder();
        html.append("<html><head><meta charset='UTF-8'><style>");
        html.append("body{font-family:Arial,sans-serif;background:#FFF;} ul{margin-left:30px;}");
        html.append("li{margin-bottom:6px;font-size:16px;} b{font-weight:600;color:#222;}");
        html.append("</style></head>");
        html.append("<body><h2>Árvore Sintática (AST)</h2><ul>");
        for (Stmt s : statements) html.append(s.accept(this));
        html.append("</ul></body></html>");
        try (FileWriter file = new FileWriter(caminhoArquivo)) {
            file.write(html.toString());
            System.out.println("AST inicial (indentada) gerada em: " + caminhoArquivo);
        } catch (IOException e) {
            System.err.println("Erro ao salvar HTML: " + e.getMessage());
        }
    }
}
