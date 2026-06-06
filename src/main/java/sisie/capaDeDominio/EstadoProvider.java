package sisie.capaDeDominio;

public class EstadoProvider {
    private static EstadoEnvio pendiente;
    private static EstadoEnvio enProceso;
    private static EstadoEnvio enTransito;
    private static EstadoEnvio entregado;
    private static EstadoEnvio noEntregado;

    public static void init(EstadoEnvio pendiente, EstadoEnvio enProceso, EstadoEnvio enTransito, EstadoEnvio entregado, EstadoEnvio noEntregado) {
        EstadoProvider.pendiente = pendiente;
        EstadoProvider.enProceso = enProceso;
        EstadoProvider.enTransito = enTransito;
        EstadoProvider.entregado = entregado;
        EstadoProvider.noEntregado = noEntregado;
    }

    public static EstadoEnvio getPendiente() {
        return pendiente;
    }

    public static EstadoEnvio getEnProceso() {
        return enProceso;
    }

    public static EstadoEnvio getEnTransito() {
        return enTransito;
    }

    public static EstadoEnvio getEntregado() {
        return entregado;
    }

    public static EstadoEnvio getNoEntregado() {
        return noEntregado;
    }
}
