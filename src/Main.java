import java.util.List;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            String programa = new String(Files.readAllBytes(Paths.get("C:/Users/arthu/IdeaProjects/compiladorcodigo/src/programa.txt")));
            Scanner scanner = new Scanner(programa);

            List<Token> tokens = scanner.scanTokens();
            Parser parser = new Parser(tokens);
            List<Stmt> statements = parser.parse();
            Interpreter interpreter = new Interpreter();
            interpreter.interpret(statements);
        } catch (IOException e) {
            System.err.println("Erro ao ler programa.txt: " + e.getMessage());
        }
    }
}
