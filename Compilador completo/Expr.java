import java.util.List;

abstract class Expr {

    interface Visitante<R> {
        R visitarExprAsignar(Asignar expr);
        R visitarExprBinario(Binario expr);
        R visitarExprLlamada(Llamada expr);
        R visitarExprAgrupacion(Agrupacion expr);
        R visitarExprLiteral(Literal expr);
        R visitarExprUnario(Unario expr);
        R visitarExprVariable(Variable expr);
    }

    static class Asignar extends Expr {

        Asignar(Token nombre, Expr valor) {
            this.nombre = nombre;
            this.valor = valor;
        }

        @Override
        <R> R aceptar(Visitante<R> visitante) {
            return visitante.visitarExprAsignar(this);
        }

        final Token nombre;
        final Expr valor;
    }

    static class Binario extends Expr {

        Binario(Expr izquierda, Token operador, Expr derecha) {
            this.izquierda = izquierda;
            this.operador = operador;
            this.derecha = derecha;
        }

        @Override
        <R> R aceptar(Visitante<R> visitante) {
            return visitante.visitarExprBinario(this);
        }

        final Expr izquierda;
        final Token operador;
        final Expr derecha;
    }

    static class Llamada extends Expr {

        Llamada(Expr llamado, Token parentesis, List<Expr> argumentos) {
            this.llamado = llamado;
            this.parentesis = parentesis;
            this.argumentos = argumentos;
        }

        @Override
        <R> R aceptar(Visitante<R> visitante) {
            return visitante.visitarExprLlamada(this);
        }

        final Expr llamado;
        final Token parentesis;
        final List<Expr> argumentos;
    }

    static class Agrupacion extends Expr {
        Agrupacion(Expr expresion) {
            this.expresion = expresion;
        }

        @Override
        <R> R aceptar(Visitante<R> visitante) {
            return visitante.visitarExprAgrupacion(this);
        }

        final Expr expresion;
    }

    static class Literal extends Expr {
        Literal(Object valor) {
            this.valor = valor;
        }

        @Override
        <R> R aceptar(Visitante<R> visitante) {
            return visitante.visitarExprLiteral(this);
        }

        final Object valor;
    }

    static class Unario extends Expr {
        Unario(Token operador, Expr derecha) {
            this.operador = operador;
            this.derecha = derecha;
        }

        @Override
        <R> R aceptar(Visitante<R> visitante) {
            return visitante.visitarExprUnario(this);
        }

        final Token operador;
        final Expr derecha;
    }

    static class Variable extends Expr {

        Variable(Token nombre) {
            this.nombre = nombre;
        }

        @Override
        <R> R aceptar(Visitante<R> visitante) {
            return visitante.visitarExprVariable(this);
        }

        final Token nombre;
    }

    abstract <R> R aceptar(Visitante<R> visitante);
}