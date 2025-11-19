import java.util.ArrayList;
import java.util.List;

public class GeneradorByteCode implements Expr.Visitante<Void>, Sentencia.Visitante<Void> {
    private final Fragmento fragmento; // Renomeado de Chunk para Fragmento
    private CompilerScope scope = new CompilerScope();

    private static class CompilerScope {
        private static class Local {
            String nombre;
            int profundidad;
            Local(String nombre, int profundidad) {
                this.nombre = nombre;
                this.profundidad = profundidad;
            }
        }
        List<Local> locals = new ArrayList<>();
        int scopeDepth = 0;

        void beginScope() { scopeDepth++; }

        // CORREÇÃO: Retorna o número de variáveis locais removidas.
        int endScope() {
            scopeDepth--;
            int localsRemoved = 0;
            // Remove variáveis que saíram do escopo
            while (!locals.isEmpty() && locals.get(locals.size() - 1).profundidad > scopeDepth) {
                locals.remove(locals.size() - 1);
                localsRemoved++;
            }
            return localsRemoved;
        }

        void addLocal(String name) {
            locals.add(new Local(name, scopeDepth));
        }
        int resolveLocal(String name) {
            for (int i = locals.size() - 1; i >= 0; i--) {
                if (locals.get(i).nombre.equals(name)) {
                    return i;
                }
            }
            return -1;
        }
    }

    public GeneradorByteCode(Fragmento fragmento) {
        this.fragmento = fragmento;
    }

    public void generar(List<Sentencia> sentencias) {
        for (Sentencia sent : sentencias) {
            ejecutar(sent);
        }
        fragmento.escribir(OpCode.RETORNAR);
    }

    private void ejecutar(Sentencia sentencia) {
        sentencia.aceptar(this);
    }

    private void avaliar(Expr expr) {
        expr.aceptar(this);
    }



    @Override
    public Void visitarSentenciaDeclaracion(Sentencia.Declaracion sent) {

        if (sent.inicializador != null) {
            avaliar(sent.inicializador);
        } else {

            int idx = fragmento.agregarConstante(null);
            fragmento.escribir(OpCode.CONSTANTE, idx);
        }


        if (scope.scopeDepth > 0) {
            scope.addLocal(sent.nombre.lexema);
        } else {
            int nameIdx = fragmento.agregarConstante(sent.nombre.lexema);
            fragmento.escribir(OpCode.DEFINIR_GLOBAL, nameIdx);
        }
        return null;
    }

    @Override
    public Void visitarSentenciaBloque(Sentencia.Bloque sent) {
        scope.beginScope();
        for (Sentencia s : sent.sentencias) {
            ejecutar(s);
        }

        // CORREÇÃO APLICADA AQUI: Emite POP para limpar a pilha da MV
        int localsRemoved = scope.endScope();

        for (int i = 0; i < localsRemoved; i++) {
            fragmento.escribir(OpCode.POP);
        }
        return null;
    }

    @Override
    public Void visitarSentenciaSi(Sentencia.Si sent) {
        avaliar(sent.condicion);


        int thenJump = fragmento.escribir(OpCode.SALTAR_SI_FALSO, -1);


        ejecutar(sent.ramaSi);

        int elseJump = fragmento.escribir(OpCode.SALTAR, -1);


        fragmento.patch(thenJump, fragmento.codigo.size());

        if (sent.ramaSino != null) {
            ejecutar(sent.ramaSino);
        }


        fragmento.patch(elseJump, fragmento.codigo.size());

        return null;
    }

    @Override
    public Void visitarSentenciaMientras(Sentencia.Mientras sent) {
        int loopStart = fragmento.codigo.size();

        avaliar(sent.condicion);

        int exitJump = fragmento.escribir(OpCode.SALTAR_SI_FALSO, -1);

        ejecutar(sent.cuerpo);

        fragmento.escribir(OpCode.LOOP, loopStart);

        fragmento.patch(exitJump, fragmento.codigo.size());

        return null;
    }

    @Override
    public Void visitarSentenciaImprimir(Sentencia.Imprimir sent) {
        avaliar(sent.expresion);
        fragmento.escribir(OpCode.IMPRIMIR);
        return null;
    }

    @Override
    public Void visitarSentenciaExpresion(Sentencia.Expresion sent) {

        avaliar(sent.expresion);

        fragmento.escribir(OpCode.POP);

        return null;
    }



    @Override
    public Void visitarExprBinario(Expr.Binario expr) {
        avaliar(expr.izquierda);
        avaliar(expr.derecha);

        switch (expr.operador.tipo) {
            case MAS:       fragmento.escribir(OpCode.SUMAR); break;
            case MENOS:     fragmento.escribir(OpCode.RESTAR); break;
            case ASTERISCO: fragmento.escribir(OpCode.MULTIPLICAR); break;
            case BARRA:     fragmento.escribir(OpCode.DIVIDIR); break;
            case IGUAL_IGUAL: fragmento.escribir(OpCode.IGUAL); break;
            case MAYOR:     fragmento.escribir(OpCode.MAYOR); break;
            case MENOR:     fragmento.escribir(OpCode.MENOR); break;
            default: throw new RuntimeException("Operador desconhecido em bytecode");
        }
        return null;
    }

    @Override
    public Void visitarExprLiteral(Expr.Literal expr) {
        int idx = fragmento.agregarConstante(expr.valor);
        fragmento.escribir(OpCode.CONSTANTE, idx);
        return null;
    }

    @Override
    public Void visitarExprVariable(Expr.Variable expr) {
        int arg = scope.resolveLocal(expr.nombre.lexema);
        if (arg != -1) {
            fragmento.escribir(OpCode.LEER_LOCAL, arg);
        } else {
            arg = fragmento.agregarConstante(expr.nombre.lexema);
            fragmento.escribir(OpCode.LEER_GLOBAL, arg);
        }
        return null;
    }

    @Override
    public Void visitarExprAsignar(Expr.Asignar expr) {
        avaliar(expr.valor);

        int arg = scope.resolveLocal(expr.nombre.lexema);
        if (arg != -1) {
            fragmento.escribir(OpCode.ASIGNAR_LOCAL, arg);
        } else {
            arg = fragmento.agregarConstante(expr.nombre.lexema);
            fragmento.escribir(OpCode.ASIGNAR_GLOBAL, arg);
        }
        return null;
    }


    @Override public Void visitarExprLlamada(Expr.Llamada expr) { return null; }
    @Override
    public Void visitarExprAgrupacion(Expr.Agrupacion expr) {
        avaliar(expr.expresion);
        return null;
    }
    @Override public Void visitarExprUnario(Expr.Unario expr) {
        avaliar(expr.derecha);
        if (expr.operador.tipo == TokenType.MENOS) fragmento.escribir(OpCode.NEGATIVO);
        if (expr.operador.tipo == TokenType.EXCLAMACION) fragmento.escribir(OpCode.NOT);
        return null;
    }
}