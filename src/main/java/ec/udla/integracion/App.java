package ec.udla.integracion;

import org.apache.camel.main.Main;

/**
 * Punto de entrada: arranca Camel en modo standalone.
 */
public class App {

    public static void main(String[] args) throws Exception {
        Main main = new Main();
        main.configure().addRoutesBuilder(new FileTransferRoute());
        main.run(args);
    }
}
