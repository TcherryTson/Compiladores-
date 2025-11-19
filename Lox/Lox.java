import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {

    static boolean tuvoError = false;
    static boolean tuvoErrorRuntime = false;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Uso: jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            ejecutarArchivo(args[0]);
        } else {
            ejecutarArchivo("teste.txt");
        }
    }

    private static void ejecutarArchivo(String ruta) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(ruta));
        ejecutar(new String(bytes, Charset.defaultCharset()));

        if (tuvoError) System.exit(65);
        if (tuvoErrorRuntime) System.exit(70);
    }

    private static void ejecutar(String fuente) {

        Escaner escaner = new Escaner(fuente);
        List<Token> tokens = escaner.escanearTokens();

        System.out.println("=== 1. ANÁLISIS LÉXICO (TOKENS) ===");
        for (Token token : tokens) {
            System.out.println(token); //
        }
        System.out.println("===================================");

        if (tokens.size() == 1 && !tuvoError) {
            error(tokens.get(0), "El archivo de código fuente está vacío o no contiene código válido.");
            return;
        }

        if (tuvoError) return;

        AnalizadorSintactico analizador = new AnalizadorSintactico(tokens);
        List<Sentencia> sentencias = analizador.analizar();

        if (tuvoError) return;
        System.out.println(">> 2. Análisis Sintáctico: CORRECTO");

        AnalizadorSemantico semantico = new AnalizadorSemantico();
        try {
            semantico.analizar(sentencias);
        } catch (AnalizadorSemantico.ErrorDeTipo error) {
            return;
        }

        if (tuvoError) return;
        System.out.println(">> 3. Análisis Semántico: CORRECTO");

        Mv vm = new Mv();
        vm.interpretar(sentencias);
    }

    static void error(int linea, String mensaje) {
        reportar(linea, "", mensaje);
    }

    static void error(Token token, String mensaje) {
        if (token.tipo == TokenType.FIN_DE_ARCHIVO) {
            reportar(token.linea, " en el final", mensaje);
        } else {
            reportar(token.linea, " en '" + token.lexema + "'", mensaje);
        }
    }

    private static void reportar(int linea, String donde, String mensaje) {
        System.err.println("[línea " + linea + "] Error" + donde + ": " + mensaje);
        tuvoError = true;
    }
}