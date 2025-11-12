import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class    Lox {

    static boolean tuvoError = false;

    public static void main(String[] args) throws IOException {
        ejecutarArchivo("pasta.txt");
    }

    private static void ejecutarArchivo(String ruta) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(ruta));
        ejecutar(new String(bytes, Charset.defaultCharset()));

        if (tuvoError) {
            System.exit(65);
        }
    }

    private static void ejecutar(String fuente) {
        Escaner escaner = new Escaner(fuente);
        List<Token> tokens = escaner.escanearTokens();

        if (tokens.size() == 1 && !tuvoError) {

            reportar(tokens.get(0).linea, "", "El archivo de código fuente está vacío o no contiene código.");
            return;
        }



        if (tuvoError) return;
        AnalizadorSintactico analizador = new AnalizadorSintactico(tokens);
        List<Sentencia> sentencias = analizador.analizar();

        if (tuvoError) return;

        System.out.println("Escaner y Analizador Sintáctico terminaron sin errores.");
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

    private static void reportar(int linea, String donde,
                                 String mensaje) {
        System.err.println(
                "[línea " + linea + "] Error" + donde + ": " + mensaje);
        tuvoError = true;
    }
}