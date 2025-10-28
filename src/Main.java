import java.util.List;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import Lexica.Scanner;
import Lexica.Token;
import Sintatica.Parser;
import Sintatica.Stmt;
import Semantica.Interpreter;
import Utils.AstHtml;

public class Main {
    public static void main(String[] args) {
        try {
            String caminhoPrograma = "C:/Users/arthu/IdeaProjects/compiladorcodigo/src/programa.cpqp";

            String programa = new String(Files.readAllBytes(Paths.get(caminhoPrograma)));

            Scanner scanner = new Scanner(programa);
            List<Token> tokens = scanner.scanTokens();
            System.out.println("Análise léxica concluída: " + tokens.size() + " tokens.");

            Parser parser = new Parser(tokens);
            List<Stmt> statements = parser.parse();

            if (statements == null || statements.isEmpty()) {
                System.err.println("Nenhum comando válido encontrado no código.");
                return;
            }
            System.out.println("Análise sintática concluída. (" + statements.size() + " statements)");

            String caminhoHtml = "C:/Users/arthu/IdeaProjects/compiladorcodigo/src/arvore.html";
            AstHtml printer = new AstHtml();
            printer.gerarHtml(statements, caminhoHtml);
            System.out.println("Arquivo HTML da AST salvo em: " + caminhoHtml);

            Interpreter interpreter = new Interpreter();
            System.out.println("\n--------- EXECUÇÃO DO PROGRAMA ---------");
            interpreter.interpret(statements);

        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo do programa: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erro inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
