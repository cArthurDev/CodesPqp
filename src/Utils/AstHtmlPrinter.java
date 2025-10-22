package Utils;

import Sintatica.Expr;
import Sintatica.Stmt;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class AstHtmlPrinter implements Expr.Visitor<String>, Stmt.Visitor<String> {

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        String operador = "<li><span class='node'>Operador: " + expr.operator.lexeme + "</span></li>";
        return li("Binária", operador + expr.left.accept(this) + expr.right.accept(this));
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return li("Agrupamento", expr.expression.accept(this));
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        return li("Literal: " + expr.value, "");
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return li("Unária (" + expr.operator.lexeme + ")", expr.right.accept(this));
    }

    @Override
    public String visitVariableExpr(Expr.Variable expr) {
        return li("Variável: " + expr.name.lexeme, "");
    }

    @Override
    public String visitAssignExpr(Expr.Assign expr) {
        return li("Atribuição: " + expr.name.lexeme, expr.value.accept(this));
    }

    @Override
    public String visitCallExpr(Expr.Call expr) {
        StringBuilder args = new StringBuilder();
        for (Expr e : expr.arguments) args.append(e.accept(this));
        return li("Chamada de função", expr.callee.accept(this) + args);
    }


    @Override
    public String visitPrintStmt(Stmt.Print stmt) {
        return li("Print", stmt.expression.accept(this));
    }

    @Override
    public String visitVarStmt(Stmt.Var stmt) {
        String init = stmt.initializer != null ? stmt.initializer.accept(this) : "";
        return li("Variável: " + stmt.name.lexeme, init);
    }

    @Override
    public String visitFunctionStmt(Stmt.Function stmt) {
        StringBuilder params = new StringBuilder("Parâmetros: ");
        for (var param : stmt.parameters) params.append(param.lexeme).append(" ");
        StringBuilder body = new StringBuilder();
        for (Stmt s : stmt.body) body.append(s.accept(this));
        return li("Função: " + stmt.name.lexeme + " (" + params + ")", body.toString());
    }

    @Override
    public String visitReturnStmt(Stmt.Return stmt) {
        if (stmt.value == null) return li("Return", "");
        return li("Return", stmt.value.accept(this));
    }

    @Override
    public String visitIfStmt(Stmt.If stmt) {
        String cond = stmt.condition.accept(this);
        String thenB = stmt.thenBranch.accept(this);
        String elseB = stmt.elseBranch != null ? stmt.elseBranch.accept(this) : "";
        return li("If", cond + thenB + elseB);
    }

    @Override
    public String visitBlockStmt(Stmt.Block stmt) {
        StringBuilder sb = new StringBuilder();
        for (Stmt s : stmt.statements) sb.append(s.accept(this));
        return li("Bloco", sb.toString());
    }

    @Override
    public String visitExpressionStmt(Stmt.Expression stmt) {
        return li("Expressão", stmt.expr.accept(this));
    }

    @Override
    public String visitWhileStmt(Stmt.While stmt) {
        return li("While", stmt.condition.accept(this) + stmt.body.accept(this));
    }

    @Override
    public String visitBreakStmt(Stmt.Break stmt) {
        return li("Break", "");
    }

    @Override
    public String visitSwitchStmt(Stmt.Switch stmt) {
        StringBuilder sb = new StringBuilder();
        sb.append(stmt.expr.accept(this));
        for (Stmt.Case c : stmt.cases) sb.append(li("Case", c.stmt.accept(this)));
        if (stmt.defaultCase != null) sb.append(li("Default", stmt.defaultCase.stmt.accept(this)));
        return li("Switch", sb.toString());
    }

    @Override
    public String visitInputStmt(Stmt.Input stmt) {
        return li("Input: " + stmt.name.lexeme, "");
    }

    private String li(String title, String children) {
        return "<li><span class='node'>" + title + "</span>" +
                (children.isEmpty() ? "" : "<ul>" + children + "</ul>") +
                "</li>";
    }

    public void gerarHtml(List<Stmt> statements, String caminhoArquivo) {
        StringBuilder html = new StringBuilder();
        html.append("<html><head><meta charset='UTF-8'>");
        html.append("<style>");
        html.append("body { font-family: Arial; }");
        html.append("ul { list-style-type:none; }");
        html.append(".node { color:#008; font-weight:bold; cursor:pointer; }");
        html.append(".node:hover { color:#d00; }");
        html.append("</style>");
        html.append("</head><body><h2>Árvore Sintática (AST)</h2><ul>");

        for (Stmt s : statements) html.append(s.accept(this));

        html.append("</ul>");
        html.append("<script>");
        html.append("document.querySelectorAll('.node').forEach(n=>n.addEventListener('click',()=>{");
        html.append("let ul=n.parentElement.querySelector('ul'); if(ul) ul.style.display=(ul.style.display=='none'?'block':'none');");
        html.append("}));");
        html.append("</script></body></html>");

        try (FileWriter file = new FileWriter(caminhoArquivo)) {
            file.write(html.toString());
            System.out.println("AST gerada com sucesso em " + caminhoArquivo);
        } catch (IOException e) {
            System.err.println("Erro ao salvar HTML: " + e.getMessage());
        }
    }
}
