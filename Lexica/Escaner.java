import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Propósito: É o scanner da linguagem, responsável por ler o código-fonte (String) e produzir uma lista de objetos Token.
 * Detalhes Chave: Usa um mapa estático (palabrasClave) para identificar palavras reservadas. Implementa métodos como
 * cadena() e numero() para tratar literais. O método principal é escanearTokens().
 */
public class Escaner {

    private final String fuente;
    private final List<Token> tokens = new ArrayList<>();

    private static final Map<String, TokenType> palabrasClave;

    private int inicio = 0;
    private int actual = 0;
    private int linea = 1;

    Escaner(String fuente) {
        this.fuente = fuente;
    }
    static {
        palabrasClave = new HashMap<>();
        palabrasClave.put("y",       TokenType.Y);
        palabrasClave.put("clase",   TokenType.CLASE);
        palabrasClave.put("sino",    TokenType.SINO);
        palabrasClave.put("falso",   TokenType.FALSO);
        palabrasClave.put("para",    TokenType.PARA);
        palabrasClave.put("fun",     TokenType.FUN);
        palabrasClave.put("si",      TokenType.SI);
        palabrasClave.put("nulo",    TokenType.NULO);
        palabrasClave.put("o",       TokenType.O);
        palabrasClave.put("imprimir",TokenType.IMPRIMIR);
        palabrasClave.put("retornar",TokenType.RETORNAR);
        palabrasClave.put("super",   TokenType.SUPER);
        palabrasClave.put("este",    TokenType.ESTE);
        palabrasClave.put("verdadero", TokenType.VERDADERO);
        palabrasClave.put("var",     TokenType.VAR);
        palabrasClave.put("mientras",TokenType.MIENTRAS);
        palabrasClave.put("entero",   TokenType.ENTERO);
        palabrasClave.put("flotante", TokenType.FLOTANTE);
        palabrasClave.put("booleano", TokenType.BOOLEANO);
    }

    List<Token> escanearTokens() {
        while (!esFin()) {
            inicio = actual;
            escanearToken();
        }

        tokens.add(new Token(TokenType.FIN_DE_ARCHIVO, "", null, linea));
        return tokens;
    }

    private void escanearToken() {
        char c = avanzar();
        switch (c) {
            case '(': agregarToken(TokenType.PARENTESIS_IZQUIERDO); break;
            case ')': agregarToken(TokenType.PARENTESIS_DERECHO); break;
            case '{': agregarToken(TokenType.LLAVE_IZQUIERDA); break;
            case '}': agregarToken(TokenType.LLAVE_DERECHA); break;
            case ',': agregarToken(TokenType.COMA); break;
            case '.': agregarToken(TokenType.PUNTO); break;
            case '-': agregarToken(TokenType.MENOS); break;
            case '+': agregarToken(TokenType.MAS); break;
            case ';': agregarToken(TokenType.PUNTO_Y_COMA); break;
            case '*': agregarToken(TokenType.ASTERISCO); break;
            case '!':
                agregarToken(coincidir('=') ? TokenType.EXCLAMACION_IGUAL : TokenType.EXCLAMACION);
                break;
            case '=':
                agregarToken(coincidir('=') ? TokenType.IGUAL_IGUAL : TokenType.IGUAL);
                break;
            case '<':
                agregarToken(coincidir('=') ? TokenType.MENOR_IGUAL : TokenType.MENOR);
                break;
            case '>':
                agregarToken(coincidir('=') ? TokenType.MAYOR_IGUAL : TokenType.MAYOR);
                break;
            case '/':
                if (coincidir('/')) {
                    while (ver() != '\n' && !esFin()) avanzar();
                } else {
                    agregarToken(TokenType.BARRA);
                }
                break;

            case ' ':
            case '\r':
            case '\t':
                break;
            case '\n':
                linea++;
                break;
            case '"':
                cadena();
                break;
            default:
                if (esDigito(c)) {
                    numero();
                } else if (esAlfa(c)) {
                    identificador();
                } else {
                    Lox.error(linea, "Carácter inesperado.");
                }
        }
    }

    private void identificador() {
        while (esAlfanumerico(ver())) avanzar();

        String texto = fuente.substring(inicio, actual);

        TokenType tipo = palabrasClave.get(texto);
        if (tipo == null) tipo = TokenType.IDENTIFICADOR;

        agregarToken(tipo);
    }

    private void numero() {
        while (esDigito(ver())) avanzar();

        if (ver() == '.' && esDigito(verSiguiente())) {
            avanzar();
            while (esDigito(ver())) avanzar();
        }

        agregarToken(TokenType.NUMERO,
                Double.parseDouble(fuente.substring(inicio, actual)));
    }

    private void cadena() {
        while (ver() != '"' && !esFin()) {
            if (ver() == '\n') linea++;
            avanzar();
        }

        if (esFin()) {
            Lox.error(linea, "Cadena de texto no terminada.");
            return;
        }

        avanzar();

        String valor = fuente.substring(inicio + 1, actual - 1);

        agregarToken(TokenType.CADENA, valor);
    }

    private boolean coincidir(char esperado) {
        if (esFin()) return false;
        if (fuente.charAt(actual) != esperado) return false;
        actual++;
        return true;
    }

    private char ver() {
        if (esFin()) return '\0';
        return fuente.charAt(actual);
    }

    private char verSiguiente() {
        if (actual + 1 >= fuente.length()) return '\0';
        return fuente.charAt(actual + 1);
    }

    private boolean esAlfa(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean esAlfanumerico(char c) {
        return esAlfa(c) || esDigito(c);
    }

    private boolean esDigito(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean esFin() {
        return actual >= fuente.length();
    }

    private char avanzar() {
        return fuente.charAt(actual++);
    }


    private void agregarToken(TokenType tipo) {
        agregarToken(tipo, null);
    }

    private void agregarToken(TokenType tipo, Object literal) {
        String texto = fuente.substring(inicio, actual);

        tokens.add(new Token(tipo, texto, literal, linea));
    }
}
