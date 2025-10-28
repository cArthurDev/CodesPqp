package Sintatica;

import Lexica.Token;
import Lexica.TokenType;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

// Sintatica.Parser que transforma tokens em comandos e expressões (AST).
// Suporta precedência de operadores, controle de fluxo (if, while, switch),
// e declarações (variáveis, print, input, etc).

public class Parser {
    private final List<Token> tokens;  // Lista de tokens de entrada
    private int current = 0;           // Posição do parser nos tokens


    // Interfaces funcionais para regras prefix (iniciam expressão) e infix (entre dois lados)
    private interface ParseFnPrefix { Expr parse(Parser parser); }
    private interface ParseFnInfix { Expr parse(Parser parser, Expr left); }

    // Cada regra define como um token pode aparecer numa expressão.
    private static class ParseRule {
        final ParseFnPrefix prefix;
        final ParseFnInfix infix;
        final Precedence precedence;
        ParseRule(ParseFnPrefix prefix, ParseFnInfix infix, Precedence precedence) {
            this.prefix = prefix;
            this.infix = infix;
            this.precedence = precedence;
        }
    }

    // Mapeamento de cada tipo de token para uma regra sintática
    private static final ParseRule[] rules = new ParseRule[TokenType.values().length];
    static {
        rules[TokenType.LEFTPAREN.ordinal()]    = new ParseRule(Parser::grouping, Parser::call, Precedence.CALL);
        rules[TokenType.RIGHTPAREN.ordinal()]   = new ParseRule(null, null, Precedence.NONE);
        rules[TokenType.LEFTBRACE.ordinal()]    = new ParseRule(null, null, Precedence.NONE);
        rules[TokenType.RIGHTBRACE.ordinal()]   = new ParseRule(null, null, Precedence.NONE);
        rules[TokenType.COMMA.ordinal()]        = new ParseRule(null, null, Precedence.NONE);
        rules[TokenType.DOT.ordinal()]          = new ParseRule(null, null, Precedence.NONE);
        rules[TokenType.MINUS.ordinal()]        = new ParseRule(Parser::unary, Parser::binary, Precedence.TERM);
        rules[TokenType.PLUS.ordinal()]         = new ParseRule(null, Parser::binary, Precedence.TERM);
        rules[TokenType.SEMICOLON.ordinal()]    = new ParseRule(null, null, Precedence.NONE);
        rules[TokenType.COLON.ordinal()]        = new ParseRule(null, null, Precedence.NONE);
        rules[TokenType.SLASH.ordinal()]        = new ParseRule(null, Parser::binary, Precedence.FACTOR);
        rules[TokenType.STAR.ordinal()]         = new ParseRule(null, Parser::binary, Precedence.FACTOR);
        rules[TokenType.BANG.ordinal()]         = new ParseRule(Parser::unary, null, Precedence.UNARY);
        rules[TokenType.BANGEQUAL.ordinal()]    = new ParseRule(null, Parser::binary, Precedence.EQUALITY);
        rules[TokenType.EQUALEQUAL.ordinal()]   = new ParseRule(null, Parser::binary, Precedence.EQUALITY);
        rules[TokenType.GREATER.ordinal()]      = new ParseRule(null, Parser::binary, Precedence.COMPARISON);
        rules[TokenType.GREATEREQUAL.ordinal()] = new ParseRule(null, Parser::binary, Precedence.COMPARISON);
        rules[TokenType.LESS.ordinal()]         = new ParseRule(null, Parser::binary, Precedence.COMPARISON);
        rules[TokenType.LESSEQUAL.ordinal()]    = new ParseRule(null, Parser::binary, Precedence.COMPARISON);
        rules[TokenType.PERCENT.ordinal()]      = new ParseRule(null, Parser::binary, Precedence.FACTOR);
        rules[TokenType.IDENTIFIER.ordinal()]   = new ParseRule(Parser::variable, null, Precedence.NONE);
        rules[TokenType.EQUAL.ordinal()]        = new ParseRule(null, Parser::assign, Precedence.ASSIGNMENT);
        rules[TokenType.NUMBER.ordinal()]       = new ParseRule(Parser::number, null, Precedence.NONE);
        rules[TokenType.STRING.ordinal()]       = new ParseRule(Parser::string, null, Precedence.NONE);
        rules[TokenType.TRUE.ordinal()]         = new ParseRule(Parser::literal, null, Precedence.NONE);
        rules[TokenType.FALSE.ordinal()]        = new ParseRule(Parser::literal, null, Precedence.NONE);
        rules[TokenType.NIL.ordinal()]          = new ParseRule(Parser::literal, null, Precedence.NONE);
        // Palavras-chave, que não iniciam expressão
        rules[TokenType.PRINT.ordinal()]        = new ParseRule(null, null, Precedence.NONE);
        rules[TokenType.VAR.ordinal()]          = new ParseRule(null, null, Precedence.NONE);
        rules[TokenType.INT.ordinal()]          = new ParseRule(null, null, Precedence.NONE);
        rules[TokenType.FLOAT.ordinal()]        = new ParseRule(null, null, Precedence.NONE);
        rules[TokenType.BOOL.ordinal()]         = new ParseRule(null, null, Precedence.NONE);
        rules[TokenType.FUN.ordinal()]          = new ParseRule(null, null, Precedence.NONE);
        rules[TokenType.RETURN.ordinal()]       = new ParseRule(null, null, Precedence.NONE);
        rules[TokenType.IF.ordinal()]           = new ParseRule(null, null, Precedence.NONE);
        rules[TokenType.ELSE.ordinal()]         = new ParseRule(null, null, Precedence.NONE);
        rules[TokenType.WHILE.ordinal()]        = new ParseRule(null, null, Precedence.NONE);
        rules[TokenType.FOR.ordinal()]          = new ParseRule(null, null, Precedence.NONE);
        rules[TokenType.EOF.ordinal()]          = new ParseRule(null, null, Precedence.NONE);
    }

    public Parser(List<Token> tokens) { this.tokens = tokens; }

    // Roda o parse, retornando lista de comandos (statements)
    public List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            try {
                statements.add(declaration());
            } catch (RuntimeException e) {
                System.err.println("Erro sintático: " + e.getMessage());
                synchronize();
            }
        }
        return statements;
    }

    // Sincronização após erro sintático para não "engasgar" parsing
    private void synchronize() {
        advance();
        while (!isAtEnd()) {
            if (previous().type == TokenType.SEMICOLON) return;
            switch (peek().type) {
                case VAR: case FUN: case FOR: case IF: case WHILE:
                case PRINT: case RETURN: case ELSE:
                    return;
            }
            advance();
        }
    }

    // Escolhe qual tipo de declaração será parseada.
    private Stmt declaration() {
        if (match(TokenType.VAR))    return varDeclaration();
        if (match(TokenType.WHILE))  return whileStatement();
        if (match(TokenType.PRINT))  return printStatement();
        if (match(TokenType.IF))     return ifStatement();
        if (match(TokenType.INPUT))  return inputStatement();
        if (match(TokenType.SWITCH)) return switchStatement();
        if (match(TokenType.BREAK))  return breakStatement();
        return expressionStatement();
    }

    private Stmt ifStatement() {
        consume(TokenType.LEFTPAREN, "Esperava '(' após SE.");
        Expr condition = expression();
        consume(TokenType.RIGHTPAREN, "Esperava ')' após condição do SE.");
        Stmt thenBranch = statementBlocoOuSimples();
        Stmt elseBranch = null;
        if (match(TokenType.ELSE)) elseBranch = statementBlocoOuSimples();
        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    // Retorna bloco (se abre '{') ou uma declaração simples.
    private Stmt statementBlocoOuSimples() {
        if (match(TokenType.LEFTBRACE)) return block();
        else return declaration();
    }

    // Parse do comando de input (ex: LEAI x;)
    private Stmt inputStatement() {
        Token name = consume(TokenType.IDENTIFIER, "Esperava nome da variável após 'LEAI'.");
        consume(TokenType.SEMICOLON, "Ou te falar ce esqueceu o ';' após LEAI.");
        return new Stmt.Input(name);
    }

    // Parse de bloco: várias declarações entre {}
    private Stmt block() {
        List<Stmt> statements = new ArrayList<>();
        while (!check(TokenType.RIGHTBRACE) && !isAtEnd()) {
            statements.add(declaration());
        }
        consume(TokenType.RIGHTBRACE, "Esperava '}' após bloco.");
        return new Stmt.Block(statements);
    }

    // While clássico: WHILE (cond) { ... }
    private Stmt whileStatement() {
        consume(TokenType.LEFTPAREN, "Esperava '(' após while.");
        Expr condition = expression();
        consume(TokenType.RIGHTPAREN, "Esperava ')' após condição.");
        consume(TokenType.LEFTBRACE, "Esperava '{' após while.");
        Stmt body = block();
        return new Stmt.While(condition, body);
    }

    // SWITCH-case: ESCOLHEAI expr { CASO val: ... PADRAO: ... }
    private Stmt switchStatement() {
        Expr expr = expression();
        consume(TokenType.LEFTBRACE, "Esperava '{' após expressão do ESCOLHEAI.");
        List<Stmt.Case> cases = new ArrayList<>();
        Stmt.Case defaultCase = null;
        while (!check(TokenType.RIGHTBRACE) && !isAtEnd()) {
            if (match(TokenType.CASE)) {
                Expr value = expression();
                consume(TokenType.COLON, "Esperava ':' após valor do CASO.");
                Stmt stmt = statementBlocoOuSimples();
                cases.add(new Stmt.Case(value, stmt));
            } else if (match(TokenType.DEFAULT)) {
                consume(TokenType.COLON, "Esperava ':' após PADRAO.");
                Stmt stmt = statementBlocoOuSimples();
                defaultCase = new Stmt.Case(null, stmt);
            } else {
                throw new RuntimeException("Erro na linha " + peek().line + ": Esperava 'CASO' ou 'PADRAO'. (encontrado: " + peek().lexeme + ")");
            }
        }
        consume(TokenType.RIGHTBRACE, "Esperava '}' após ESCOLHEAI.");
        return new Stmt.Switch(expr, cases, defaultCase);
    }

    private Stmt breakStatement() {
        consume(TokenType.SEMICOLON, "Esperava ';' após PAREI.");
        return new Stmt.Break();
    }

    // VAR declaração: var x = 5;
    private Stmt varDeclaration() {
        Token name = consume(TokenType.IDENTIFIER, "Ou ce esqueceu o nome da variável.");
        Expr initializer = null;
        if (match(TokenType.EQUAL)) initializer = expression();
        consume(TokenType.SEMICOLON, "Ou te falar ce esqueceu o ';'.");
        return new Stmt.Var(name, initializer);
    }

    // PRINT expressão; (escreve na tela)
    private Stmt printStatement() {
        Expr value = expression();
        consume(TokenType.SEMICOLON, "Ou te falar ce esqueceu o ';'.");
        return new Stmt.Print(value);
    }

    // Declaração para uma expressão simples;
    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(TokenType.SEMICOLON, "Ou te falar ce esqueceu o ';'.");
        return new Stmt.Expression(expr);
    }

    // Avalia uma expressão, respeitando precedência dos operadores.
    private Expr expression() {return parsePrecedence(Precedence.ASSIGNMENT); }

    // Avalia uma expressão pelo nível de precedência.
    private Expr parsePrecedence(Precedence precedence) {
        ParseRule prefixRule = getRule(peek().type);
        advance();
        if (prefixRule == null || prefixRule.prefix == null)
            throw new RuntimeException("Esperava expressão, obtido " + previous().lexeme);

        Expr expr = prefixRule.prefix.parse(this);

        while (!isAtEnd() && precedence.ordinal() <= getRule(peek().type).precedence.ordinal()) {
            ParseRule infixRule = getRule(peek().type);
            advance();
            if (infixRule == null || infixRule.infix == null) return expr;
            expr = infixRule.infix.parse(this, expr);
        }

        return expr;
    }

    // Exceções sintáticas mostrando contexto e linha do erro.
    private void error(Token token, String message) {
        throw new RuntimeException("Erro na linha " + token.line + ": " + message + " (encontrado: " + token.lexeme + ")");
    }

    // ****** Métodos estáticos para regras (prefix/infix) ******

    private static Expr number(Parser parser) {
        return new Expr.Literal(parser.previous().literal);
    }

    private static Expr binary(Parser parser, Expr left) {
        Token operator = parser.previous();
        Precedence precedence = getRule(operator.type).precedence;
        Expr right = parser.parsePrecedence(Precedence.values()[precedence.ordinal() + 1]);
        return new Expr.Binary(left, operator, right);
    }

    private static Expr assign(Parser parser, Expr left) {
        if (!(left instanceof Expr.Variable))
            throw new RuntimeException("Alvo de atribuição inválido!");
        Token name = ((Expr.Variable) left).name;
        Expr value = parser.parsePrecedence(Precedence.ASSIGNMENT);
        return new Expr.Assign(name, value);
    }

    private static Expr grouping(Parser parser) {
        Expr expr = parser.expression();
        parser.consume(TokenType.RIGHTPAREN, "Esperava ')' após expressão.");
        return new Expr.Grouping(expr);
    }

    private static Expr unary(Parser parser) {
        Token operator = parser.previous();
        Expr right = parser.parsePrecedence(Precedence.UNARY);
        return new Expr.Unary(operator, right);
    }

    private static Expr variable(Parser parser) {
        return new Expr.Variable(parser.previous());
    }

    private static Expr string(Parser parser) {
        return new Expr.Literal(parser.previous().literal);
    }

    private static Expr literal(Parser parser) {
        Token token = parser.previous();
        if (token.type == TokenType.TRUE) return new Expr.Literal(Optional.of(true));
        if (token.type == TokenType.FALSE) return new Expr.Literal(Optional.of(false));
        if (token.type == TokenType.NIL) return new Expr.Literal(null);
        return new Expr.Literal(token.literal);
    }

    private static Expr call(Parser parser, Expr callee) {
        List<Expr> arguments = new ArrayList<>();
        if (!parser.check(TokenType.RIGHTPAREN)) {
            do {
                arguments.add(parser.expression());
            } while (parser.match(TokenType.COMMA));
        }
        Token paren = parser.consume(TokenType.RIGHTPAREN, "Esperava ')' após os argumentos da chamada.");
        return new Expr.Call(callee, paren, arguments);
    }

    // ****** Utilitários para navegação de tokens ******
    private Token previous() { return tokens.get(current - 1); }
    private Token peek()     { return tokens.get(current); }
    private Token advance()  { if (!isAtEnd()) current++; return previous(); }
    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw new RuntimeException("Erro na linha " + previous().line + ": " + message + " (encontrado: " + previous().lexeme + ")");
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    private static ParseRule getRule(TokenType type) {
        return rules[type.ordinal()];
    }


}
