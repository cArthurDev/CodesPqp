import ByteCode.Chunk;
import ByteCode.Compiler;
import ByteCode.VM;
import Lexica.Scanner;
import Lexica.Token;
import Sintatica.Parser;
import Sintatica.Stmt;
import Utils.AstHtml;



import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        // Usar o caminho fixo
        String caminhoPrograma = "C:\\Users\\arthu\\OneDrive\\Desktop\\CodesPQP\\CodesPqp\\src\\programa.cpqp";

        String caminhoHtml = "src\\arvore.html";

        try {
            //Ler o ficheiro
            String programa = new String(Files.readAllBytes(Paths.get(caminhoPrograma)));

            //Análise Léxica (Scanner)
            Scanner scanner = new Scanner(programa);
            List<Token> tokens = scanner.scanTokens();
            System.out.println("Análise léxica concluída: " + tokens.size() + " tokens.");

            //Análise Sintática (Parser -> AST)
            Parser parser = new Parser(tokens);
            List<Stmt> statements = parser.parse();

            if (statements == null || statements.isEmpty()) {
                System.err.println("Nenhum comando válido encontrado no código.");
                return;
            }
            System.out.println("Análise sintática concluída. (" + statements.size() + " statements)");

            //Gerar visualização da AST
            AstHtml printer = new AstHtml();
            printer.gerarHtml(statements, caminhoHtml);
            System.out.println("Arquivo HTML da AST salvo em: " + caminhoHtml);

            //Compilação (AST -> Bytecode)
            Compiler compiler = new Compiler();
            Chunk chunk = compiler.compile(statements);

            if (chunk == null) {
                System.err.println("Falha na compilação.");
                return;
            }
            System.out.println("Compilação para bytecode concluída.");
            VM vm = new VM();
            System.out.println("\n--------- EXECUÇÃO DA VM ---------");
            vm.interpret(chunk);

        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo do programa: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erro inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }
}