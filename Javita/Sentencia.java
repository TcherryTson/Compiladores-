import java.util.List;

abstract class Sentencia {

    interface Visitante<R> {
        R visitarSentenciaBloque(Bloque sentencia);
        R visitarSentenciaImprimir(Imprimir sentencia);
        R visitarSentenciaExpresion(Expresion sentencia);
        R visitarSentenciaSi(Si sentencia);
        R visitarSentenciaMientras(Mientras sentencia);
        R visitarSentenciaDeclaracion(Declaracion sentencia);
    }

    static class Bloque extends Sentencia {
        Bloque(List<Sentencia> sentencias) {
            this.sentencias = sentencias;
        }
        @Override
        <R> R accept(Visitante<R> visitante) {
            return visitante.visitarSentenciaBloque(this);
        }
        final List<Sentencia> sentencias;
    }

    static class Expresion extends Sentencia {
        Expresion(Expr expresion) {
            this.expresion = expresion;
        }
        @Override
        <R> R accept(Visitante<R> visitante) {
            return visitante.visitarSentenciaExpresion(this);
        }
        final Expr expresion;
    }

    static class Si extends Sentencia {
        Si(Expr condicion, Sentencia ramaSi, Sentencia ramaSino) {
            this.condicion = condicion;
            this.ramaSi = ramaSi;
            this.ramaSino = ramaSino;
        }
        @Override
        <R> R accept(Visitante<R> visitante) {
            return visitante.visitarSentenciaSi(this);
        }
        final Expr condicion;
        final Sentencia ramaSi;
        final Sentencia ramaSino;
    }

    static class Imprimir extends Sentencia {
        Imprimir(Expr expresion) {
            this.expresion = expresion;
        }
        @Override
        <R> R accept(Visitante<R> visitante) {
            return visitante.visitarSentenciaImprimir(this);
        }
        final Expr expresion;
    }

    static class Mientras extends Sentencia {
        Mientras(Expr condicion, Sentencia cuerpo) {
            this.condicion = condicion;
            this.cuerpo = cuerpo;
        }
        @Override
        <R> R accept(Visitante<R> visitante) {
            return visitante.visitarSentenciaMientras(this);
        }
        final Expr condicion;
        final Sentencia cuerpo;
    }

    static class Declaracion extends Sentencia {
        Declaracion(Token tipo, Token nombre, Expr inicializador) {
            this.tipo = tipo;
            this.nombre = nombre;
            this.inicializador = inicializador;
        }

        @Override
        <R> R accept(Visitante<R> visitante) {
            return visitante.visitarSentenciaDeclaracion(this);
        }

        final Token tipo;
        final Token nombre;
        final Expr inicializador;
    }

    abstract <R> R accept(Visitante<R> visitante);
}