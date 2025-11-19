import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Propósito: A Máquina Virtual baseada em pilha (Stack-based Virtual Machine), responsável por executar o bytecode gerado.
 * Detalhes Chave: Possui uma stack (pilha) para operações e um mapa (globals) para variáveis globais. O método run() é
 * o loop principal que lê e executa cada OpCode, manipulando a pilha e o ponteiro de instrução (ip).
 */

public class Mv {
    private Fragmento fragmento;
    private final Stack<Object> stack = new Stack<>();
    private final Map<String, Object> globals = new HashMap<>();
    private int ip = 0;

    public Mv() {
    }

    public void interpretar(List<Sentencia> sentencias) {
        this.fragmento = new Fragmento();
        GeneradorByteCode generador = new GeneradorByteCode(this.fragmento);

        try {
            generador.generar(sentencias);
        } catch (Exception e) {
            System.err.println("Erro de Compilação na VM: " + e.getMessage());
            return;
        }

        this.fragmento.imprimirDisassembly();

        this.ip = 0;
        this.stack.clear();
        run();
    }

    private void run() {
        if (fragmento == null || fragmento.codigo.isEmpty()) return;

        while (ip < fragmento.codigo.size()) {
            OpCode instruccion = fragmento.codigo.get(ip);
            Integer operando = fragmento.operandos.get(ip);
            ip++;

            try {
                switch (instruccion) {
                    case CONSTANTE:
                        stack.push(fragmento.constantes.get(operando));
                        break;
                    case SUMAR: {
                        Object b = stack.pop();
                        Object a = stack.pop();
                        if (a instanceof Double && b instanceof Double) {
                            stack.push((Double) a + (Double) b);
                        } else if (a instanceof String || b instanceof String) {
                            stack.push(String.valueOf(a) + String.valueOf(b));
                        }
                        break;
                    }
                    case RESTAR: {
                        double b = (Double) stack.pop();
                        double a = (Double) stack.pop();
                        stack.push(a - b);
                        break;
                    }
                    case MULTIPLICAR: {
                        double b = (Double) stack.pop();
                        double a = (Double) stack.pop();
                        stack.push(a * b);
                        break;
                    }
                    case DIVIDIR: {
                        double b = (Double) stack.pop();
                        double a = (Double) stack.pop();
                        stack.push(a / b);
                        break;
                    }
                    case NEGATIVO:
                        stack.push(-(Double) stack.pop());
                        break;
                    case IGUAL: {
                        Object b = stack.pop();
                        Object a = stack.pop();
                        stack.push(a.equals(b));
                        break;
                    }
                    case MAYOR: {
                        double b = (Double) stack.pop();
                        double a = (Double) stack.pop();
                        stack.push(a > b);
                        break;
                    }
                    case MENOR: {
                        double b = (Double) stack.pop();
                        double a = (Double) stack.pop();
                        stack.push(a < b);
                        break;
                    }
                    case IMPRIMIR:
                        System.out.println(stack.pop());
                        break;
                    case DEFINIR_GLOBAL: {
                        String name = (String) fragmento.constantes.get(operando);
                        globals.put(name, stack.pop());
                        break;
                    }
                    case LEER_GLOBAL: {
                        String name = (String) fragmento.constantes.get(operando);
                        if (!globals.containsKey(name)) {
                            throw new RuntimeException("Variable global no definida: " + name);
                        }
                        stack.push(globals.get(name));
                        break;
                    }
                    case ASIGNAR_GLOBAL: {
                        String name = (String) fragmento.constantes.get(operando);
                        if (!globals.containsKey(name)) {
                            throw new RuntimeException("Variable global no definida: " + name);
                        }
                        globals.put(name, stack.peek());
                        break;
                    }
                    case LEER_LOCAL: {
                        stack.push(stack.get(operando));
                        break;
                    }
                    case ASIGNAR_LOCAL: {
                        stack.set(operando, stack.peek());
                        break;
                    }
                    case POP: {
                        stack.pop();
                        break;
                    }
                    case SALTAR_SI_FALSO: {
                        Boolean condicion = (Boolean) stack.pop();
                        if (!condicion) ip = operando;
                        break;
                    }
                    case SALTAR: {
                        ip = operando;
                        break;
                    }
                    case LOOP: {
                        ip = operando;
                        break;
                    }
                    case RETORNAR:
                        return;
                }
            } catch (Exception e) {
                throw new RuntimeException("Erro de execução: " + e.getMessage());
            }
        }
    }

}
