import java.util.List;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            String programa = new String(Files.readAllBytes(Paths.get("C:/Users/arthu/IdeaProjects/compiladorcodigo/src/programa.cpqp")));

            Scanner scanner = new Scanner(programa);
            List<Token> tokens = scanner.scanTokens();

            Parser parser = new Parser(tokens);
            List<Stmt> statements = parser.parse();

            // COLETA E IMPRESSÃO DAS EXPRESSÕES
            ExprCollector collector = new ExprCollector();
            for (Stmt stmt : statements) {
                stmt.accept(collector);
            }
            System.out.println("Expressões presentes no programa:");
            for (Expr expr : collector.getExprs()) {
                System.out.println(
                        "Tipo: " + expr.getTypeName() +
                        ", Linha: " + expr.getLine() +
                        ", Expressão: " + expr.toString()
                );

            }
            System.out.println("-----");

            Interpreter interpreter = new Interpreter();
            interpreter.interpret(statements);
        } catch (IOException e) {
            System.err.println("Erro ao ler programa.cpqp: " + e.getMessage());
        }
    }
}
