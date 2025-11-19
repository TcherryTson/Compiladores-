import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class AnalizadorSemantico implements Expr.Visitante<Tipo>, Sentencia.Visitante<Void> {

    private final Stack<Map<String, Tipo>> ambitos = new Stack<>();

    static class ErrorDeTipo extends RuntimeException {
        ErrorDeTipo(Token token, String mensaje) {
            super(mensaje);
            Lox.error(token, mensaje);
        }
    }

    public void analizar(List<Sentencia> sentencias) {
        iniciarAmbito();
        for (Sentencia sent : sentencias) {
            resolverSentencia(sent);
        }
        finalizarAmbito();
    }

    private void iniciarAmbito() {
        ambitos.push(new HashMap<String, Tipo>());
    }

    private void finalizarAmbito() {
        ambitos.pop();
    }

    private void declarar(Token nombre, Tipo tipo) {
        if (ambitos.isEmpty()) return;
        ambitos.peek().put(nombre.lexema, tipo);
    }

    private Tipo obtenerTipo(Token nombre) {
        for (int i = ambitos.size() - 1; i >= 0; i--) {
            if (ambitos.get(i).containsKey(nombre.lexema)) {
                return ambitos.get(i).get(nombre.lexema);
            }
        }
        throw new ErrorDeTipo(nombre, "Variable '" + nombre.lexema + "' no definida.");
    }

    @Override
    public Void visitarSentenciaDeclaracion(Sentencia.Declaracion sent) {
        Tipo tipoDeclarado = Tipo.desdeToken(sent.tipo);
        Tipo tipoInicializador = Tipo.NULO;

        if (sent.inicializador != null) {
            tipoInicializador = resolverExpresion(sent.inicializador);
        }

        if (tipoDeclarado == Tipo.INDEFINIDO) {
            if (tipoInicializador == Tipo.NULO) {
                throw new ErrorDeTipo(sent.tipo, "Variables 'var' deben ser inicializadas.");
            }
            tipoDeclarado = tipoInicializador;
        }

        if (tipoInicializador != Tipo.NULO && tipoDeclarado != tipoInicializador) {
            boolean conversionImplicita = (tipoDeclarado == Tipo.FLOTANTE && tipoInicializador == Tipo.ENTERO);

            if (!conversionImplicita) {
                throw new ErrorDeTipo(sent.nombre, "Error de tipo: No se puede asignar " +
                        tipoInicializador + " a una variable de tipo " + tipoDeclarado + ".");
            }
        }

        declarar(sent.nombre, tipoDeclarado);
        return null;
    }

    @Override
    public Void visitarSentenciaBloque(Sentencia.Bloque sent) {
        iniciarAmbito();
        for (Sentencia s : sent.sentencias) {
            resolverSentencia(s);
        }
        finalizarAmbito();
        return null;
    }

    @Override
    public Tipo visitarExprLiteral(Expr.Literal expr) {
        if (expr.valor == null) return Tipo.NULO;
        if (expr.valor instanceof Double) {
            if ((Double) expr.valor % 1 == 0) return Tipo.ENTERO;
            return Tipo.FLOTANTE;
        }
        if (expr.valor instanceof Boolean) return Tipo.BOOLEANO;
        if (expr.valor instanceof String) return Tipo.CADENA;
        return Tipo.INDEFINIDO;
    }

    @Override
    public Tipo visitarExprVariable(Expr.Variable expr) {
        return obtenerTipo(expr.nombre);
    }

    @Override
    public Tipo visitarExprBinario(Expr.Binario expr) {
        Tipo izq = resolverExpresion(expr.izquierda);
        Tipo der = resolverExpresion(expr.derecha);

        if ((izq == Tipo.ENTERO || izq == Tipo.FLOTANTE) &&
                (der == Tipo.ENTERO || der == Tipo.FLOTANTE)) {

            switch (expr.operador.tipo) {
                case MAYOR:
                case MAYOR_IGUAL:
                case MENOR:
                case MENOR_IGUAL:
                    return Tipo.BOOLEANO;

                case MENOS:
                case ASTERISCO:
                case BARRA:
                    if (izq == Tipo.FLOTANTE || der == Tipo.FLOTANTE) return Tipo.FLOTANTE;
                    return Tipo.ENTERO;
                case MAS:
                    if (izq == Tipo.FLOTANTE || der == Tipo.FLOTANTE) return Tipo.FLOTANTE;
                    return Tipo.ENTERO;
            }
        }

        if (expr.operador.tipo == TokenType.MAS) {
            if (izq == Tipo.CADENA && der == Tipo.CADENA) {
                return Tipo.CADENA;
            }
        }


        switch (expr.operador.tipo) {
            case IGUAL_IGUAL:
            case EXCLAMACION_IGUAL:
                if (izq == der) return Tipo.BOOLEANO;
                if ((izq == Tipo.ENTERO || izq == Tipo.FLOTANTE) && (der == Tipo.ENTERO || der == Tipo.FLOTANTE))
                    return Tipo.BOOLEANO;
                break;
        }

        throw new ErrorDeTipo(expr.operador, "Operación inválida entre " + izq + " y " + der);
    }

    private Tipo resolverExpresion(Expr expr) {
        return expr.aceptar(this);
    }

    private void resolverSentencia(Sentencia sent) {
        sent.aceptar(this);
    }

    @Override
    public Void visitarSentenciaImprimir(Sentencia.Imprimir sent) {
        resolverExpresion(sent.expresion);
        return null;
    }

    @Override
    public Void visitarSentenciaExpresion(Sentencia.Expresion sent) {
        resolverExpresion(sent.expresion);
        return null;
    }

    @Override
    public Void visitarSentenciaSi(Sentencia.Si sent) {
        Tipo condicion = resolverExpresion(sent.condicion);
        if (condicion != Tipo.BOOLEANO) {
            throw new ErrorDeTipo(sent.tokenSi, "La condición del 'si' debe ser un booleano.");
        }
        resolverSentencia(sent.ramaSi);
        if (sent.ramaSino != null) resolverSentencia(sent.ramaSino);
        return null;
    }

    @Override
    public Void visitarSentenciaMientras(Sentencia.Mientras sent) {
        Tipo condicion = resolverExpresion(sent.condicion);
        if (condicion != Tipo.BOOLEANO) {
            throw new ErrorDeTipo(sent.tokenMientras, "La condición del 'mientras' debe ser un booleano.");
        }
        resolverSentencia(sent.cuerpo);
        return null;
    }

    @Override
    public Tipo visitarExprAsignar(Expr.Asignar expr) {
        Tipo tipoVariable = obtenerTipo(expr.nombre);
        Tipo tipoValor = resolverExpresion(expr.valor);
        if (tipoVariable != tipoValor) {
            boolean conversionImplicita = (tipoVariable == Tipo.FLOTANTE && tipoValor == Tipo.ENTERO);

            if (!conversionImplicita) {
                throw new ErrorDeTipo(expr.nombre, "Error de asignación: No se puede asignar un valor de tipo " +
                        tipoValor + " a una variable de tipo " + tipoVariable + ".");
            }
        }

        return tipoValor;
    }

    @Override
    public Tipo visitarExprUnario(Expr.Unario expr) {
        Tipo derecha = resolverExpresion(expr.derecha);

        switch (expr.operador.tipo) {
            case MENOS:
                if (derecha == Tipo.ENTERO) return Tipo.ENTERO;
                if (derecha == Tipo.FLOTANTE) return Tipo.FLOTANTE;
                throw new ErrorDeTipo(expr.operador, "El operador '-' solo se puede usar con números.");

            case EXCLAMACION:
                if (derecha == Tipo.BOOLEANO) return Tipo.BOOLEANO;
                throw new ErrorDeTipo(expr.operador, "El operador '!' solo se puede usar con booleanos.");
        }

        return null;
    }

    @Override
    public Tipo visitarExprLlamada(Expr.Llamada expr) {
        resolverExpresion(expr.llamado);

        for (Expr argumento : expr.argumentos) {
            resolverExpresion(argumento);
        }
        return Tipo.NULO;
    }

    @Override
    public Tipo visitarExprAgrupacion(Expr.Agrupacion expr) {
        return resolverExpresion(expr.expresion);
    }
}
