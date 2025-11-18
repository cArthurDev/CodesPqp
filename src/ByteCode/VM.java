package ByteCode;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.Scanner;

//A Máquina Virtual (VM) que executa o bytecode.

public class VM {

    private Chunk chunk;
    private int ip; // Instruction Pointer
    private Stack<Object> stack;
    private Map<String, Object> globals;

    // NOVO: Scanner para ler a entrada do console
    private final Scanner consoleInput;

    public VM() {
        this.stack = new Stack<>();
        this.globals = new HashMap<>();
        this.consoleInput = new Scanner(System.in); // Inicializa o scanner
    }

    public boolean interpret(Chunk chunk) {
        this.chunk = chunk;
        this.ip = 0;

        while (true) {
            int instruction;
            try {
                instruction = chunk.code.get(ip++);
            } catch (IndexOutOfBoundsException e) {
                return true; // Fim do bytecode
            }

            OpCode op = OpCode.values()[instruction];

            // ESTE É O SWITCH COMPLETO COM TODOS OS CASES
            switch (op) {
                // --- Essenciais ---
                case OP_RETURN: {
                    System.out.println("VM: Execução terminada.");
                    return true;
                }
                case OP_CONSTANT: { // <-- O OPCODE QUE ESTAVA A FALTAR
                    int constIndex = chunk.code.get(ip++);
                    stack.push(chunk.constants.get(constIndex));
                    break;
                }
                case OP_POP: stack.pop(); break;

                // --- Literais ---
                case OP_NIL: stack.push(null); break;
                case OP_TRUE: stack.push(true); break;
                case OP_FALSE: stack.push(false); break;

                // --- Aritmética ---
                case OP_NEGATE: {
                    Object value = stack.pop();
                    if (value instanceof Double) stack.push(-(Double) value);
                    else if (value instanceof Integer) stack.push(-(Integer) value);
                    else { runtimeError("Operando deve ser um número."); }
                    break;
                }
                case OP_ADD:      binaryOp("+"); break;
                case OP_SUBTRACT: binaryOp("-"); break;
                case OP_MULTIPLY: binaryOp("*"); break;
                case OP_DIVIDE:   binaryOp("/"); break;

                // --- Lógica ---
                case OP_GREATER:  binaryOp(">"); break;
                case OP_LESS:     binaryOp("<"); break;
                case OP_EQUAL: {
                    Object b = stack.pop();
                    Object a = stack.pop();
                    stack.push(isEqual(a, b));
                    break;
                }
                case OP_NOT:
                    stack.push(!isTruthy(stack.pop()));
                    break;

                // --- Variáveis Globais ---
                case OP_DEFINE_GLOBAL: {
                    int constIndex = chunk.code.get(ip++);
                    String varName = (String) chunk.constants.get(constIndex);
                    globals.put(varName, stack.pop());
                    break;
                }
                case OP_GET_GLOBAL: {
                    int constIndex = chunk.code.get(ip++);
                    String varName = (String) chunk.constants.get(constIndex);
                    if (!globals.containsKey(varName)) {
                        runtimeError("Variável '" + varName + "' não definida.");
                        return false;
                    }
                    stack.push(globals.get(varName));
                    break;
                }
                case OP_SET_GLOBAL: {
                    int constIndex = chunk.code.get(ip++);
                    String varName = (String) chunk.constants.get(constIndex);
                    if (!globals.containsKey(varName)) {
                        runtimeError("Variável '" + varName + "' não definida. Não é possível atribuir.");
                        return false;
                    }
                    globals.put(varName, stack.peek());
                    break;
                }

                // --- Ação ---
                case OP_PRINT: {
                    System.out.println(stringify(stack.pop()));
                    break;
                }
                case OP_INPUT: { // <-- O NOVO OPCODE (LEIA)
                    System.out.print("> ");
                    String line = consoleInput.nextLine();
                    Object value;
                    try {
                        value = Integer.parseInt(line);
                    } catch (NumberFormatException e1) {
                        try {
                            value = Double.parseDouble(line);
                        } catch (NumberFormatException e2) {
                            value = line;
                        }
                    }
                    stack.push(value);
                    break;
                }

                // --- Controlo de Fluxo ---
                case OP_JUMP_IF_FALSE: {
                    int offset = readShort();
                    if (!isTruthy(stack.peek())) {
                        ip += offset;
                    }
                    break;
                }
                case OP_JUMP: {
                    int offset = readShort();
                    ip += offset;
                    break;
                }
                case OP_LOOP: {
                    int offset = readShort();
                    ip -= offset;
                    break;
                }

                default:
                    System.err.println("VM Erro: Opcode desconhecido " + op);
                    return false;
            }
        }
    }

    // --- Funções Auxiliares da VM ---

    private void binaryOp(String op) {
        Object b = stack.pop();
        Object a = stack.pop();

        // Lógica de String para OP_ADD
        if (op.equals("+")) {
            if (a instanceof String || b instanceof String) {
                stack.push(stringify(a) + stringify(b));
                return;
            }
        }

        // Lógica de Números (para todas as ops)
        if (!(a instanceof Number) || !(b instanceof Number)) {
            runtimeError("Operandos devem ser números para a operação '" + op + "'.");
            stack.push(null); // Evita falha
            return;
        }

        // Lógica de conversão Double/Integer
        if (a instanceof Double || b instanceof Double) {
            double valA = (a instanceof Integer) ? ((Integer) a).doubleValue() : (Double) a;
            double valB = (b instanceof Integer) ? ((Integer) b).doubleValue() : (Double) b;
            switch(op) {
                case "+": stack.push(valA + valB); break;
                case "-": stack.push(valA - valB); break;
                case "*": stack.push(valA * valB); break;
                case "/": // (TODO: Adicionar checagem de divisão por zero)
                    stack.push(valA / valB); break;
                case ">": stack.push(valA > valB); break;
                case "<": stack.push(valA < valB); break;
            }
        } else {
            // Ambos são Inteiros
            int valA = (Integer) a;
            int valB = (Integer) b;
            switch(op) {
                case "+": stack.push(valA + valB); break;
                case "-": stack.push(valA - valB); break;
                case "*": stack.push(valA * valB); break;
                case "/": // (TODO: Adicionar checagem de divisão por zero)
                    stack.push(valA / valB); break;
                case ">": stack.push(valA > valB); break;
                case "<": stack.push(valA < valB); break;
            }
        }
    }

    private int readShort() {
        int high = chunk.code.get(ip++) & 0xFF;
        int low = chunk.code.get(ip++) & 0xFF;
        return (high << 8) | low;
    }

    private void runtimeError(String message) {
        System.err.println(message + " [linha " + chunk.lines.get(ip - 1) + "]");
    }

    private String stringify(Object object) {
        if (object == null) return "nulo";
        if (object instanceof Boolean) return (Boolean) object ? "verdadeiro" : "falso";
        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                return text.substring(0, text.length() - 2);
            }
        }
        return object.toString();
    }

    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean) object;
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;
        return a.equals(b);
    }

    // --- Casos de Switch que omiti por brevidade ---

    private void copyPasteHelper() {
        // de colar o código que estava igual
        OpCode op = OpCode.OP_NIL;
        switch(op) {
            case OP_RETURN: break;
            case OP_CONSTANT: break;
            case OP_POP: break;
            case OP_NIL: break;
            case OP_TRUE: break;
            case OP_FALSE: break;
            case OP_NEGATE: break;
            case OP_EQUAL: break;
            case OP_NOT: break;
            case OP_DEFINE_GLOBAL: break;
            case OP_GET_GLOBAL: break;
            case OP_SET_GLOBAL: break;
            case OP_JUMP_IF_FALSE: break;
            case OP_JUMP: break;
            case OP_LOOP: break;
        }
    }
}