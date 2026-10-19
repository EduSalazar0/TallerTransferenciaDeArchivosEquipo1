package ec.udla.integracion;

import org.apache.camel.main.Main;

/**
 * Punto de entrada: arranca Camel en modo standalone.
 * Las rutas se registran a partir del incremento 1.
 */
public class App {

    public static void main(String[] args) throws Exception {
        Main main = new Main();
        main.run(args);
    }
}
