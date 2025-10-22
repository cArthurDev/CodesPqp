import java.util.*;

public class ExprCollector implements Stmt.Visitor<Void> {
    private final List<Expr> exprs = new ArrayList<>();

    public List<Expr> getExprs() {
        return exprs;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        exprs.add(stmt.expression);
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        if (stmt.initializer != null)
            exprs.add(stmt.initializer);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        for (Stmt s : stmt.body)
            s.accept(this);
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        if (stmt.value != null)
            exprs.add(stmt.value);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        exprs.add(stmt.condition);
        stmt.thenBranch.accept(this);
        if (stmt.elseBranch != null)
            stmt.elseBranch.accept(this);
        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        for (Stmt s : stmt.statements)
            s.accept(this);
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        exprs.add(stmt.expr);
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        exprs.add(stmt.condition);
        stmt.body.accept(this);
        return null;
    }

    @Override
    public Void visitSwitchStmt(Stmt.Switch stmt) {
        exprs.add(stmt.expr);
        for (Stmt.Case c : stmt.cases) {
            if (c.value != null) exprs.add(c.value);
            c.stmt.accept(this);
        }
        if (stmt.defaultCase != null)
            stmt.defaultCase.stmt.accept(this);
        return null;
    }

    @Override
    public Void visitInputStmt(Stmt.Input stmt) {
        return null; // Não possui expressão
    }

    @Override
    public Void visitBreakStmt(Stmt.Break stmt) {
        return null; // Não possui expressão
    }
}
