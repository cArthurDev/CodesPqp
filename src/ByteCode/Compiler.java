package ByteCode;

import Sintatica.Expr;
import Sintatica.Stmt;
import Lexica.Token;
import java.util.List;

public class Compiler implements Expr.Visitor<Void>, Stmt.Visitor<Void> {

    private Chunk currentChunk;

    private int getCurrentLine(Token token) {
        return (token != null) ? token.line : 0;
    }

    public Compiler() {
        this.currentChunk = null;
    }

    public Chunk compile(List<Stmt> statements) {
        this.currentChunk = new Chunk();

        try {
            for (Stmt stmt : statements) {
                stmt.accept(this);
            }

            currentChunk.write(OpCode.OP_RETURN, 0);
            return currentChunk;

        } catch (Exception e) {
            System.err.println("Erro de compilação: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // --- VISITORS DE COMANDO (Stmt) ---

    @Override
    public Void visitInputStmt(Stmt.Input stmt) {
        int line = getCurrentLine(stmt.name);
        // A VM colocará o valor lido no topo da pilha.
        currentChunk.write(OpCode.OP_INPUT, line);

        // Emite o opcode para guardar esse valor na variável global.
        int constIndex = currentChunk.addConstant(stmt.name.lexeme);
        currentChunk.write(OpCode.OP_SET_GLOBAL, line);
        currentChunk.write(constIndex, line);
        // e o OP_SET_GLOBAL apenas faz 'peek()'.
        currentChunk.write(OpCode.OP_POP, line);

        return null;
    }

    @Override public Void visitCallExpr(Expr.Call expr) { /* TODO */ return null; }
    @Override public Void visitFunctionStmt(Stmt.Function stmt) { /* TODO */ return null; }
    @Override public Void visitReturnStmt(Stmt.Return stmt) { /* TODO */ return null; }
    @Override public Void visitBreakStmt(Stmt.Break stmt) { /* TODO */ return null; }
    @Override public Void visitSwitchStmt(Stmt.Switch stmt) { /* TODO */ return null; }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        int line = 0;
        stmt.condition.accept(this);
        int thenJump = emitJump(OpCode.OP_JUMP_IF_FALSE, line);
        stmt.thenBranch.accept(this);
        int elseJump = emitJump(OpCode.OP_JUMP, line);
        patchJump(thenJump);
        if (stmt.elseBranch != null) {
            stmt.elseBranch.accept(this);
        }
        patchJump(elseJump);
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        int line = 0;
        int loopStart = currentChunk.code.size();
        stmt.condition.accept(this);
        int exitJump = emitJump(OpCode.OP_JUMP_IF_FALSE, line);
        stmt.body.accept(this);
        emitLoop(loopStart, line);
        patchJump(exitJump);
        return null;
    }

    @Override public Void visitBlockStmt(Stmt.Block stmt) {
        for (Stmt statement : stmt.statements) {
            statement.accept(this);
        }
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        if (stmt.initializer != null) {
            stmt.initializer.accept(this);
        } else {
            currentChunk.write(OpCode.OP_NIL, getCurrentLine(stmt.name));
        }
        int constIndex = currentChunk.addConstant(stmt.name.lexeme);
        currentChunk.write(OpCode.OP_DEFINE_GLOBAL, getCurrentLine(stmt.name));
        currentChunk.write(constIndex, getCurrentLine(stmt.name));
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        stmt.expr.accept(this);
        currentChunk.write(OpCode.OP_POP, 0);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        stmt.expression.accept(this);
        currentChunk.write(OpCode.OP_PRINT, 0);
        return null;
    }

    @Override
    public Void visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null) { currentChunk.write(OpCode.OP_NIL, 0); }
        else if (expr.value instanceof Boolean) { currentChunk.write(((Boolean) expr.value) ? OpCode.OP_TRUE : OpCode.OP_FALSE, 0); }
        else {
            int constIndex = currentChunk.addConstant(expr.value);
            currentChunk.write(OpCode.OP_CONSTANT, 0);
            currentChunk.write(constIndex, 0);
        }
        return null;
    }

    @Override
    public Void visitBinaryExpr(Expr.Binary expr) {
        expr.left.accept(this);
        expr.right.accept(this);
        int line = getCurrentLine(expr.operator);
        switch (expr.operator.type) {
            case PLUS:      currentChunk.write(OpCode.OP_ADD, line); break;
            case MINUS:     currentChunk.write(OpCode.OP_SUBTRACT, line); break;
            case STAR:      currentChunk.write(OpCode.OP_MULTIPLY, line); break;
            case SLASH:     currentChunk.write(OpCode.OP_DIVIDE, line); break;
            case EQUALEQUAL:currentChunk.write(OpCode.OP_EQUAL, line); break;
            case BANGEQUAL: currentChunk.write(OpCode.OP_EQUAL, line); currentChunk.write(OpCode.OP_NOT, line); break;
            case GREATER:   currentChunk.write(OpCode.OP_GREATER, line); break;
            case LESSEQUAL: currentChunk.write(OpCode.OP_GREATER, line); currentChunk.write(OpCode.OP_NOT, line); break;
            case LESS:      currentChunk.write(OpCode.OP_LESS, line); break;
            case GREATEREQUAL:currentChunk.write(OpCode.OP_LESS, line); currentChunk.write(OpCode.OP_NOT, line); break;
            default: throw new RuntimeException("Operador binário desconhecido: " + expr.operator.type);
        }
        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary expr) {
        expr.right.accept(this);
        int line = getCurrentLine(expr.operator);
        switch (expr.operator.type) {
            case MINUS: currentChunk.write(OpCode.OP_NEGATE, line); break;
            case BANG:  currentChunk.write(OpCode.OP_NOT, line); break;
            default: throw new RuntimeException("Operador unário desconhecido: " + expr.operator.type);
        }
        return null;
    }

    @Override
    public Void visitGroupingExpr(Expr.Grouping expr) {
        expr.expression.accept(this);
        return null;
    }

    @Override
    public Void visitVariableExpr(Expr.Variable expr) {
        int constIndex = currentChunk.addConstant(expr.name.lexeme);
        currentChunk.write(OpCode.OP_GET_GLOBAL, getCurrentLine(expr.name));
        currentChunk.write(constIndex, getCurrentLine(expr.name));
        return null;
    }

    @Override
    public Void visitAssignExpr(Expr.Assign expr) {
        expr.value.accept(this);
        int constIndex = currentChunk.addConstant(expr.name.lexeme);
        currentChunk.write(OpCode.OP_SET_GLOBAL, getCurrentLine(expr.name));
        currentChunk.write(constIndex, getCurrentLine(expr.name));
        return null;
    }

    // --- NOVOS VISITORS: Incremento/Decremento ---
    @Override
    public Void visitIncrementoExpr(Expr.Incremento expr) {
        // Operação: variável = variável + 1
        int constIndex = currentChunk.addConstant(expr.name.lexeme);

        // Obter valor atual
        currentChunk.write(OpCode.OP_GET_GLOBAL, getCurrentLine(expr.name));
        currentChunk.write(constIndex, getCurrentLine(expr.name));

        // Carregar constante 1
        currentChunk.write(OpCode.OP_CONSTANT, getCurrentLine(expr.name));
        int oneIdx = currentChunk.addConstant(1);
        currentChunk.write(oneIdx, getCurrentLine(expr.name));

        // Soma
        currentChunk.write(OpCode.OP_ADD, getCurrentLine(expr.name));

        // Salvar na variável
        currentChunk.write(OpCode.OP_SET_GLOBAL, getCurrentLine(expr.name));
        currentChunk.write(constIndex, getCurrentLine(expr.name));
        return null;
    }

    @Override
    public Void visitDecrementoExpr(Expr.Decremento expr) {
        // Operação: variável = variável - 1
        int constIndex = currentChunk.addConstant(expr.name.lexeme);

        // Obter valor atual
        currentChunk.write(OpCode.OP_GET_GLOBAL, getCurrentLine(expr.name));
        currentChunk.write(constIndex, getCurrentLine(expr.name));

        // Carregar constante 1
        currentChunk.write(OpCode.OP_CONSTANT, getCurrentLine(expr.name));
        int oneIdx = currentChunk.addConstant(1);
        currentChunk.write(oneIdx, getCurrentLine(expr.name));

        // Subtração
        currentChunk.write(OpCode.OP_SUBTRACT, getCurrentLine(expr.name));

        // Salvar na variável
        currentChunk.write(OpCode.OP_SET_GLOBAL, getCurrentLine(expr.name));
        currentChunk.write(constIndex, getCurrentLine(expr.name));
        return null;
    }

    // --- Auxiliares para jumps ---
    private int emitJump(OpCode jumpOpcode, int line) {
        currentChunk.write(jumpOpcode, line);
        currentChunk.write(0xFF, line);
        currentChunk.write(0xFF, line);
        return currentChunk.code.size() - 2;
    }
    private void patchJump(int offset) {
        int jump = currentChunk.code.size() - offset - 2;
        if (jump > 65535) {
            throw new RuntimeException("Salto muito longo para o bytecode.");
        }
        currentChunk.code.set(offset, (jump >> 8) & 0xFF);
        currentChunk.code.set(offset + 1, jump & 0xFF);
    }
    private void emitLoop(int loopStart, int line) {
        currentChunk.write(OpCode.OP_LOOP, line);
        int offset = currentChunk.code.size() - loopStart + 2;
        if (offset > 65535) {
            throw new RuntimeException("Loop muito longo para o bytecode.");
        }
        currentChunk.write((offset >> 8) & 0xFF, line);
        currentChunk.write(offset & 0xFF, line);
    }
}
