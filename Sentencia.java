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
        <R> R aceptar(Visitante<R> visitante) {
            return visitante.visitarSentenciaBloque(this);
        }
        final List<Sentencia> sentencias;
    }

    static class Expresion extends Sentencia {
        Expresion(Expr expresion) {
            this.expresion = expresion;
        }
        @Override
        <R> R aceptar(Visitante<R> visitante) {
            return visitante.visitarSentenciaExpresion(this);
        }
        final Expr expresion;
    }

    static class Si extends Sentencia {
        Si(Token tokenSi, Expr condicion, Sentencia ramaSi, Sentencia ramaSino) {
            this.tokenSi = tokenSi;
            this.condicion = condicion;
            this.ramaSi = ramaSi;
            this.ramaSino = ramaSino;
        }
        @Override
        <R> R aceptar(Visitante<R> visitante) {
            return visitante.visitarSentenciaSi(this);
        }
        final Token tokenSi;
        final Expr condicion;
        final Sentencia ramaSi;
        final Sentencia ramaSino;
    }

    static class Imprimir extends Sentencia {
        Imprimir(Expr expresion) {
            this.expresion = expresion;
        }
        @Override
        <R> R aceptar(Visitante<R> visitante) {
            return visitante.visitarSentenciaImprimir(this);
        }
        final Expr expresion;
    }

    static class Mientras extends Sentencia {
        Mientras(Token tokenMientras, Expr condicion, Sentencia cuerpo) {
            this.tokenMientras = tokenMientras;
            this.condicion = condicion;
            this.cuerpo = cuerpo;
        }
        @Override
        <R> R aceptar(Visitante<R> visitante) {
            return visitante.visitarSentenciaMientras(this);
        }
        final Token tokenMientras;
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
        <R> R aceptar(Visitante<R> visitante) {
            return visitante.visitarSentenciaDeclaracion(this);
        }

        final Token tipo;
        final Token nombre;
        final Expr inicializador;
    }

    abstract <R> R aceptar(Visitante<R> visitante);
}