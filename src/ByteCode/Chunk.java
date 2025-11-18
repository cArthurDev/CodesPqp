package ByteCode;

import java.util.ArrayList;
import java.util.List;

//Armazena uma sequência de bytecode (o programa compilado) e os valores constantes associados.
public class Chunk {

    public final List<Integer> code;

    public final List<Object> constants;

    public final List<Integer> lines;

    public Chunk() {
        this.code = new ArrayList<>();
        this.constants = new ArrayList<>();
        this.lines = new ArrayList<>();
    }

    public void write(OpCode op, int line) {
        // Converte o enum para um valor numérico
        write(op.ordinal(), line);
    }

    public void write(int byteValue, int line) {
        this.code.add(byteValue);
        this.lines.add(line);
    }

    public int addConstant(Object value) {
        this.constants.add(value);
        // Retorna o índice onde foi adicionado (ex: 0, 1, 2...)
        return this.constants.size() - 1;
    }
}