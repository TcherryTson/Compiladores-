import java.util.ArrayList;
import java.util.List;

class AnalizadorSintactico {
    private static class ErrorDeAnalisis extends RuntimeException {
    }


    private final List<Token> tokens;
    private int actual = 0;


    AnalizadorSintactico(List<Token> tokens) {
        this.tokens = tokens;
    }

    List<Sentencia> analizar() {
        List<Sentencia> sentencias = new ArrayList<>();
        while (!esFin()) {
            sentencias.add(declaracionOSentencia());
        }
        return sentencias;
    }

    private Sentencia declaracionOSentencia() {
        try {
            if (coincidir(TokenType.VAR, TokenType.ENTERO, TokenType.FLOTANTE, TokenType.BOOLEANO, TokenType.CADENA)) {
                return declaracionDeTipo();
            }
            if (coincidir(TokenType.IMPRIMIR)) return sentenciaImprimir();
            if (coincidir(TokenType.SI)) return sentenciaSi();
            if (coincidir(TokenType.MIENTRAS)) return sentenciaMientras();
            if (coincidir(TokenType.PARA)) return sentenciaPara();
            if (coincidir(TokenType.LLAVE_IZQUIERDA)) return new Sentencia.Bloque(bloque());


            throw error(ver(), "Declaración inválida.");

        } catch (ErrorDeAnalisis error) {
            sincronizar();
            return null;
        }
    }

    private Sentencia declaracionDeTipo() {

        Token tipo = anterior();
        Token nombre = consumir(TokenType.IDENTIFICADOR, "Se esperaba un nombre de variable.");

        Expr inicializador = null;
        if (coincidir(TokenType.IGUAL)) {
            inicializador = expresion();

            if (inicializador instanceof Expr.Literal) {
                Object valorLiteral = ((Expr.Literal) inicializador).valor;

                if(tipo.tipo == TokenType.CADENA){
                    if(!(valorLiteral instanceof String)){
                        throw error(tipo, "Error de tipo: No se puede asignar un " + valorLiteral.getClass().getSimpleName() + " a un tipo numérico");
                    }
                }
                if (tipo.tipo == TokenType.ENTERO || tipo.tipo == TokenType.FLOTANTE) {
                    if (!(valorLiteral instanceof Double)) {
                        throw error(tipo, "Error de tipo: No se puede asignar un " + valorLiteral.getClass().getSimpleName() + " a un tipo numérico (entero/flotante).");
                    }
                }
                if (tipo.tipo == TokenType.BOOLEANO) {
                    if (!(valorLiteral instanceof Boolean)) {
                        throw error(tipo, "Error de tipo: No se puede asignar un " + valorLiteral.getClass().getSimpleName() + " a un tipo 'booleano'.");
                    }
                }
            }
        }

        consumir(TokenType.PUNTO_Y_COMA, "Se esperaba ';' después de la declaración de la variable.");
        return new Sentencia.Declaracion(tipo, nombre, inicializador);
    }

    private Sentencia sentencia() {
        if (coincidir(TokenType.VAR, TokenType.ENTERO, TokenType.FLOTANTE, TokenType.BOOLEANO, TokenType.CADENA)) {
            return declaracionDeTipo();
        }
        if (coincidir(TokenType.IMPRIMIR)) return sentenciaImprimir();
        if (coincidir(TokenType.SI)) return sentenciaSi();
        if (coincidir(TokenType.MIENTRAS)) return sentenciaMientras();
        if (coincidir(TokenType.PARA)) return sentenciaPara();
        if (coincidir(TokenType.LLAVE_IZQUIERDA)) return new Sentencia.Bloque(bloque());

        return sentenciaDeExpresion();
    }


    private Sentencia sentenciaImprimir() {
        Expr valor = expresion();
        consumir(TokenType.PUNTO_Y_COMA, "Se esperaba ';' después del valor.");
        return new Sentencia.Imprimir(valor);
    }

    private Sentencia sentenciaSi() {
        consumir(TokenType.PARENTESIS_IZQUIERDO, "Se esperaba '(' después de 'si'.");
        Expr condicion = expresion();
        consumir(TokenType.PARENTESIS_DERECHO, "Se esperaba ')' después de la condición del if.");

        Sentencia ramaSi = sentencia();
        Sentencia ramaSino = null;
        if (coincidir(TokenType.SINO)) {
            ramaSino = sentencia();
        }
        return new Sentencia.Si(condicion, ramaSi, ramaSino);
    }

    private Sentencia sentenciaMientras() {
        consumir(TokenType.PARENTESIS_IZQUIERDO, "Se esperaba '(' después de 'mientras'.");
        Expr condicion = expresion();
        consumir(TokenType.PARENTESIS_DERECHO, "Se esperaba ')' después de la condición del while.");
        Sentencia cuerpo = sentencia();
        return new Sentencia.Mientras(condicion, cuerpo);
    }

    private Sentencia sentenciaPara() {

        consumir(TokenType.PARENTESIS_IZQUIERDO, "Se esperaba '(' después de 'para'.");

        Sentencia inicializador;
        if (coincidir(TokenType.PUNTO_Y_COMA)) {
            inicializador = null;
        }
        else if (coincidir(TokenType.VAR, TokenType.ENTERO, TokenType.FLOTANTE, TokenType.BOOLEANO, TokenType.CADENA)) {
            inicializador = declaracionDeTipo();
        } else {
            inicializador = sentenciaDeExpresion();
        }

        Expr condicion = null;
        if (!verificar(TokenType.PUNTO_Y_COMA)) {
            condicion = expresion();
        }
        consumir(TokenType.PUNTO_Y_COMA, "Se esperaba ';' después de la condición del ciclo.");

        Expr incremento = null;
        if (!verificar(TokenType.PARENTESIS_DERECHO)) {
            incremento = expresion();
        }
        consumir(TokenType.PARENTESIS_DERECHO, "Se esperaba ')' después de las cláusulas del for.");

        Sentencia cuerpo = sentencia();

        if (incremento != null) {
            cuerpo = new Sentencia.Bloque(java.util.Arrays.asList(cuerpo, new Sentencia.Expresion(incremento)));
        }

        if (condicion == null)
            condicion = new Expr.Literal(true);
        cuerpo = new Sentencia.Mientras(condicion, cuerpo);

        if (inicializador != null) {
            cuerpo = new Sentencia.Bloque(java.util.Arrays.asList(inicializador, cuerpo));
        }
        return cuerpo;
    }

    private List<Sentencia> bloque() {
        List<Sentencia> sentencias = new ArrayList<>();
        while (!verificar(TokenType.LLAVE_DERECHA) && !esFin()) {
            sentencias.add(declaracionOSentencia());
        }
        consumir(TokenType.LLAVE_DERECHA, "Se esperaba '}' después del bloque.");
        return sentencias;
    }

    private Sentencia sentenciaDeExpresion() {
        Expr expr = expresion();
        consumir(TokenType.PUNTO_Y_COMA, "Se esperaba ';' después de la expresión.");
        return new Sentencia.Expresion(expr);
    }


    private Expr expresion() { return asignacion(); }

    private Expr asignacion() {
        Expr expr = igualdad();
        if (coincidir(TokenType.IGUAL)) {
            Token igual = anterior();
            Expr valor = asignacion();
            if (expr instanceof Expr.Variable) {
                Token nombre = ((Expr.Variable) expr).nombre;
                return new Expr.Asignar(nombre, valor);
            }
            error(igual, "Destino de asignación inválido.");
        }
        return expr;
    }

    private Expr igualdad() {
        Expr expr = comparacion();
        while (coincidir(TokenType.EXCLAMACION_IGUAL, TokenType.IGUAL_IGUAL)) {
            Token operador = anterior();
            Expr derecha = comparacion();
            expr = new Expr.Binario(expr, operador, derecha);
        }
        return expr;
    }

    private Expr comparacion() {
        Expr expr = termino();
        while (coincidir(TokenType.MAYOR, TokenType.MAYOR_IGUAL, TokenType.MENOR, TokenType.MENOR_IGUAL)) {
            Token operador = anterior();
            Expr derecha = termino();
            expr = new Expr.Binario(expr, operador, derecha);
        }
        return expr;
    }

    private Expr termino() {
        Expr expr = factor();
        while (coincidir(TokenType.MENOS, TokenType.MAS)) {
            Token operador = anterior();
            Expr derecha = factor();
            expr = new Expr.Binario(expr, operador, derecha);
        }
        return expr;
    }

    private Expr factor() {
        Expr expr = unario();

        while (coincidir(TokenType.BARRA, TokenType.ASTERISCO)) {
            Token operador = anterior();
            Expr derecha = unario();
            expr = new Expr.Binario(expr, operador, derecha);
        }
        return expr;
    }

    private Expr unario() {

        if (coincidir(TokenType.EXCLAMACION, TokenType.MENOS)) {
            Token operador = anterior(); // Corrigido
            Expr derecha = unario();
            return new Expr.Unario(operador, derecha);
        }
        return llamada();
    }

    private Expr llamada() {
        Expr expr = primario();
        while (true) {

            if (coincidir(TokenType.PARENTESIS_IZQUIERDO)) {
                expr = finalizarLlamada(expr);
            } else {
                break;
            }
        }
        return expr;
    }

    private Expr finalizarLlamada(Expr callee) {
        List<Expr> argumentos = new ArrayList<>();

        if (!verificar(TokenType.PARENTESIS_DERECHO)) {
            do {
                if (argumentos.size() >= 255) {
                    error(ver(), "No se pueden tener más de 255 argumentos.");
                }
                argumentos.add(expresion());
            } while (coincidir(TokenType.COMA));
        }

        Token parentesis = consumir(TokenType.PARENTESIS_DERECHO, "Se esperaba ')' después de los argumentos.");
        return new Expr.Llamada(callee, parentesis, argumentos);
    }

    private Expr primario() {

        if (coincidir(TokenType.FALSO)) return new Expr.Literal(false);
        if (coincidir(TokenType.VERDADERO)) return new Expr.Literal(true);
        if (coincidir(TokenType.NULO)) return new Expr.Literal(null);
        if (coincidir(TokenType.NUMERO, TokenType.CADENA)) {
            return new Expr.Literal(anterior().literal);
        }
        if (coincidir(TokenType.IDENTIFICADOR)) {
            return new Expr.Variable(anterior());
        }
        if (coincidir(TokenType.PARENTESIS_IZQUIERDO)) {
            Expr expr = expresion();
            consumir(TokenType.PARENTESIS_DERECHO, "Se esperaba ')' después de la expresión.");
            return new Expr.Agrupacion(expr);
        }
        throw error(ver(), "Se esperaba una expresión.");
    }

    private boolean coincidir(TokenType... tipos) {
        for (TokenType tipo : tipos) {
            if (verificar(tipo)) {
                avanzar();
                return true;
            }
        }
        return false;
    }


    private Token consumir(TokenType tipo, String mensaje) {
        if (verificar(tipo)) return avanzar();
        throw error(ver(), mensaje);
    }

    private ErrorDeAnalisis error(Token token, String mensaje) {
        Lox.error(token, mensaje);
        return new ErrorDeAnalisis();
    }

    private void sincronizar() {
        avanzar();
        while (!esFin()) {

            if (anterior().tipo == TokenType.PUNTO_Y_COMA) return;
            switch (ver().tipo) {

                case ENTERO:
                case FLOTANTE:
                case BOOLEANO:
                case PARA:
                case SI:
                case MIENTRAS:
                case IMPRIMIR:
                case RETORNAR:
                case CLASE:
                case FUN:
                case VAR:
                case CADENA:
                    return;
            }
            avanzar();
        }
    }


    private boolean verificar(TokenType tipo) {
        if (esFin()) return false;
        return ver().tipo == tipo;
    }


    private Token avanzar() {
        if (!esFin()) actual++;
        return anterior();
    }

    private boolean esFin() {

        return ver().tipo == TokenType.FIN_DE_ARCHIVO;
    }


    private Token ver() {
        return tokens.get(actual);
    }


    private Token anterior() {
        return tokens.get(actual - 1);
    }
}