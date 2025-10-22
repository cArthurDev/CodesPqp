package Semantica;

import Lexica.Scanner;
import Lexica.Token;
import Sintatica.Expr;
import Sintatica.Parser;
import Sintatica.Stmt;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

// Classe responsável por interpretar as expressões e comandos da linguagem.
// Implementa os visitantes para avaliações de expressões e execução de comandos.
public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

    // Ambiente atual com variáveis e seus valores, suporta escopos aninhados
    private Environment environment = new Environment();

    // Leitor de entrada padrão para comandos de entrada do usuário
    private final java.util.Scanner consoleInput = new java.util.Scanner(System.in);

    // Interpreta uma lista de comandos (statements).
    // O Parametro statements Lista de comandos a executar.

    public void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeException error) {
            System.err.println("Erro de execução: " + error.getMessage());
        }
    }

    // Avalia expressões binárias (+, -, *, /, etc)
    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);
        switch (expr.operator.type) {
            case PLUS:
                if (left instanceof Integer && right instanceof Integer) {
                    return (Integer) left + (Integer) right;
                }
                if (left instanceof Number && right instanceof Number) {
                    return ((Number) left).doubleValue() + ((Number) right).doubleValue();
                }
                if (left instanceof String && right instanceof String) {
                    return (String) left + (String) right;
                }
                throw new RuntimeException("Operadores '+' exigem dois números ou duas strings.");

            case MINUS:
                if (left instanceof Integer && right instanceof Integer) {
                    return (Integer) left - (Integer) right;
                }
                if (left instanceof Number && right instanceof Number) {
                    return ((Number) left).doubleValue() - ((Number) right).doubleValue();
                }
                throw new RuntimeException("Operador '-' exige números.");

            case STAR:
                if (left instanceof Integer && right instanceof Integer) {
                    return (Integer) left * (Integer) right;
                }
                if (left instanceof Number && right instanceof Number) {
                    return ((Number) left).doubleValue() * ((Number) right).doubleValue();
                }
                throw new RuntimeException("Operador '*' exige números.");

            case SLASH:
                if (left instanceof Number && right instanceof Number) {
                    double denominator = ((Number) right).doubleValue();
                    if (denominator == 0)
                        throw new RuntimeException("Divisão por zero.");
                    return ((Number) left).doubleValue() / denominator;
                }
                throw new RuntimeException("Operador '/' exige números.");

            case LESS:
                return toDouble(left) < toDouble(right);
            case GREATER:
                return toDouble(left) > toDouble(right);
            case LESSEQUAL:
                return toDouble(left) <= toDouble(right);
            case GREATEREQUAL:
                return toDouble(left) >= toDouble(right);

            case EQUALEQUAL:
                return isEqual(left, right);
            case BANGEQUAL:
                return !isEqual(left, right);

            case PERCENT:
                if (left instanceof Integer && right instanceof Integer) {
                    return (Integer) left % (Integer) right;
                }
                if (left instanceof Number && right instanceof Number) {
                    return ((Number) left).doubleValue() % ((Number) right).doubleValue();
                }
                throw new RuntimeException("Operador '%' exige números.");

            default:
                throw new RuntimeException("Operador binário desconhecido: " + expr.operator.type);
        }
    }

    // Converte objeto do tipo Number para double, para operações de comparação.
    // O Parametro "o" Objeto a converter.
    // Retorna um Valor double do objeto.

    private double toDouble(Object o) {
        if (o instanceof Number) return ((Number) o).doubleValue();
        throw new RuntimeException("Operador de comparação exige números.");
    }

    // Atribui valor a uma variável no ambiente
    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        environment.assign(expr.name.lexeme, value);
        return value;
    }

    // Avalia chamada de função/metodo
    @Override
    public Object visitCallExpr(Expr.Call expr) {
        Object callee = evaluate(expr.callee);
        if (!(callee instanceof LoxCallable)) {
            throw new RuntimeException("Só é possível chamar funções.");
        }

        List<Object> arguments = new java.util.ArrayList<>();
        for (Expr argument : expr.arguments) {
            arguments.add(evaluate(argument));
        }

        LoxCallable function = (LoxCallable) callee;
        if (arguments.size() != function.arity()) {
            throw new RuntimeException(
                    "Função espera " + function.arity() + " argumentos, recebidos " + arguments.size() + ".");
        }
        return function.call(this, arguments);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    // Busca o valor da variável no ambiente
    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return environment.get(expr.name);
    }

    // Avalia expressão agrupada (como entre parênteses)
    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    // Avalia expressão unária (!, -)
    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);
        switch (expr.operator.type) {
            case MINUS:
                if (right instanceof Integer) return -(Integer) right;
                if (right instanceof Number) return -((Number) right).doubleValue();
                throw new RuntimeException("Operador '-' exige número.");
            case BANG:
                return !isTruthy(right);
            default:
                throw new RuntimeException("Operador unário desconhecido: " + expr.operator.type);
        }
    }

    // Executa comando "print"
    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    // Declaração de variável
    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }
        environment.define(stmt.name.lexeme, value);
        return null;
    }

    // Declaração de função
    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        LoxFunction function = new LoxFunction(stmt, environment);
        environment.define(stmt.name.lexeme, function);
        return null;
    }

    // Retorna um valor da função (usando exceção para controle de fluxo)
    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        Object value = null;
        if (stmt.value != null) value = evaluate(stmt.value);
        throw new ReturnException(value);
    }

    // Executa um comando "if"
    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }

    // Avalia uma expressão simples (usada como stmt)
    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expr);
        return null;
    }

    // Executa um bloco de comandos com escopo local
    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    // Laço while com suporte a break
    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        while (isTruthy(evaluate(stmt.condition))) {
            try {
                execute(stmt.body);
            } catch (BreakException e) {
                break;
            }
        }
        return null;
    }

    // Implementa o comando switch-case com break
    @Override
    public Void visitSwitchStmt(Stmt.Switch stmt) {
        Object switchValue = evaluate(stmt.expr);
        boolean found = false;
        if (stmt.cases != null) {
            for (Stmt.Case cs : stmt.cases) {
                Object caseValue = evaluate(cs.value);
                if (isEqual(switchValue, caseValue)) {
                    try {
                        execute(cs.stmt);
                    } catch (BreakException e) {
                        return null;
                    }
                    found = true;
                    break;
                }
            }
        }
        if (!found && stmt.defaultCase != null) {
            try {
                execute(stmt.defaultCase.stmt);
            } catch (BreakException e) {
                return null;
            }
        }
        return null;
    }

    // Comando break interrompe loops (throw para controle de fluxo)
    public Void visitBreakStmt(Stmt.Break stmt) {
        throw new BreakException();
    }

    // Comando input para leitura do usuário com inferência simples de tipo
    @Override
    public Void visitInputStmt(Stmt.Input stmt) {
        System.out.print("> ");
        String linha = consoleInput.nextLine();
        Object valor = null;
        boolean atribuiu = false;

        // Tenta interpretar a linha como uma atribuição da linguagem
        try {
            String fakeProg = stmt.name.lexeme + " = " + linha + ";";
            Scanner fakeScanner = new Scanner(fakeProg);
            List<Token> fakeTokens = fakeScanner.scanTokens();
            Parser fakeParser = new Parser(fakeTokens);
            List<Stmt> fakeStatements = fakeParser.parse();
            for (Stmt s : fakeStatements) {
                this.execute(s);
                atribuiu = true;
            }
        } catch (Exception exc) {
            // Ignora falhas de parsing/execution para fallback abaixo
        }

        if (!atribuiu) {
            // Converte para int, depois double, senão mantém string
            try {
                valor = Integer.parseInt(linha);
            } catch (NumberFormatException e1) {
                try {
                    valor = Double.parseDouble(linha);
                } catch (NumberFormatException e2) {
                    valor = linha;
                }
            }
            environment.assign(stmt.name.lexeme, valor);
        }
        return null;
    }

    // Avalia uma expressão chamando o metodo accept do padrão visitor
    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    // Executa um comando chamando accept
    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    // Executa um bloco de comandos em um ambiente/local escopo, O Parametro statements Lista de comandos a executar
    // e o Parametro environment Ambiente que representa o novo escopo local.

    void executeBlock(List<Stmt> statements, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;
            for (Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            // Restaura o ambiente anterior ao sair do bloco
            this.environment = previous;
        }
    }

    // Converte objeto para string, com tratamento especial para nulo e booleanos
    private String stringify(Object object) {
        if (object == null) return "nulo";
        if (object instanceof Boolean) return (Boolean) object ? "verdadeiro" : "falso";
        return object.toString();
    }

    // Checa o que é considerado "verdadeiro" na linguagem
    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean) object;
        return true;
    }

    // Compara igualdade de dois objetos, tratando nulos
    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;
        return a.equals(b);
    }

    // Interface para funções e métodos chamados na linguagem.
    interface LoxCallable {
        int arity(); // quantidade de parâmetros
        Object call(Interpreter interpreter, List<Object> arguments); // executa a função
    }

    // Implementação de uma função definida pelo usuário.
    class LoxFunction implements LoxCallable {
        private final Stmt.Function declaration;
        private final Environment closure;

        LoxFunction(Stmt.Function declaration, Environment closure) {
            this.declaration = declaration;
            this.closure = closure;
        }

        @Override
        public int arity() {
            return declaration.parameters.size();
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            Environment environment = new Environment(closure);
            for (int i = 0; i < declaration.parameters.size(); i++) {
                environment.define(declaration.parameters.get(i).lexeme, arguments.get(i));
            }
            try {
                interpreter.executeBlock(declaration.body, environment);
            } catch (ReturnException returnValue) {
                return returnValue.value;
            }
            return null;
        }
    }

    // Exceção usada para retorno de funções (controle de fluxo)
    static class ReturnException extends RuntimeException {
        final Object value;
        ReturnException(Object value) {
            super(null, null, false, false);
            this.value = value;
        }
    }

    // Exceção usada para comando break em loops
    static class BreakException extends RuntimeException {
    }

    // Ambiente que representa variáveis definidas e seus valores.
    class Environment {
        private final Map<String, Object> values = new HashMap<>();
        private final Environment enclosing; // ambiente pai para escopos aninhados

        Environment() {
            this.enclosing = null;
        }

        Environment(Environment enclosing) {
            this.enclosing = enclosing;
        }

        // Define uma nova variável no ambiente atual
        void define(String name, Object value) {
            values.put(name, value);
        }

        // Recupera o valor da variável, buscando recursivamente em ambientes pai
        Object get(Token name) {
            if (values.containsKey(name.lexeme)) {
                return values.get(name.lexeme);
            }
            if (enclosing != null) return enclosing.get(name);
            throw new RuntimeException("Variável '" + name.lexeme + "' não definida.");
        }

        // Atribui valor a variável existente, buscando recursivamente em ambientes pai
        void assign(String name, Object value) {
            if (values.containsKey(name)) {
                values.put(name, value);
                return;
            }
            if (enclosing != null) {
                enclosing.assign(name, value);
                return;
            }
            throw new RuntimeException("Variável '" + name + "' não definida.");
        }
    }
}
