import java.util.ArrayList;
import java.util.List;

public class Fragmento {
    List<OpCode> codigo = new ArrayList<>();
    List<Object> constantes = new ArrayList<>();
    List<Integer> operandos = new ArrayList<>();

    public void escribir(OpCode op) {
        codigo.add(op);
        operandos.add(null);
    }

    public int escribir(OpCode op, int operando) {
        codigo.add(op);
        operandos.add(operando);
        return codigo.size() - 1;
    }

    public int agregarConstante(Object valor) {
        constantes.add(valor);
        return constantes.size() - 1;
    }

    public void patch(int offset, int salto) {
        operandos.set(offset, salto);
    }


    public void imprimirDisassembly() {
        System.out.println("=== ByteCode ===");

        for (int i = 0; i < codigo.size(); i++) {
            OpCode op = codigo.get(i);
            Integer operando = operandos.get(i);

            // 1. Endereço (Offset)
            System.out.printf("%04d: ", i);

            // 2. Nome da Instrução
            System.out.printf("%-16s", op);

            // 3. Detalhes do Operando
            if (operando != null) {
                if (op == OpCode.CONSTANTE || op == OpCode.DEFINIR_GLOBAL ||
                        op == OpCode.LEER_GLOBAL || op == OpCode.ASIGNAR_GLOBAL) {

                    try {
                        Object valor = constantes.get(operando);
                        if (valor instanceof String) valor = "\"" + valor + "\"";
                        System.out.printf("#%d  <%s>", operando, valor);
                    } catch (Exception e) {
                        System.out.printf("#%d", operando);
                    }

                } else if (op == OpCode.SALTAR || op == OpCode.SALTAR_SI_FALSO || op == OpCode.LOOP) {
                    System.out.printf("-> %04d", operando);
                } else {
                    System.out.printf("slot %d", operando);
                }
            }
            System.out.println();
        }
        System.out.println("===================");
    }
}