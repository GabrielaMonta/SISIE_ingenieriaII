package sisie.capaDeDominio;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "envios")
@Data
@ToString(exclude = {"observadores"})
public class Envio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_envio")
    private Integer idEnvio;

    @Column(name = "cod_seguimiento", length = 50, unique = true)
    private String codSeguimiento;

    @Column(name = "costo", precision = 10, scale = 2)
    private BigDecimal costo;

    @ManyToOne
    @JoinColumn(name = "id_transporte", nullable = false)
    private Transporte transporte;

    @ManyToOne
    @JoinColumn(name = "id_venta", nullable = false)
    private Venta venta;

    @ManyToOne
    @JoinColumn(name = "id_direccion", nullable = false)
    private Direccion direccion;

    @ManyToOne
    @JoinColumn(name = "id_estado", nullable = false)
    private EstadoEnvio estadoActual;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    public Envio() {}

    public Envio(Venta venta, Direccion direccion, EstadoEnvio estadoActual, Transporte transporte, BigDecimal costo) {
        this.venta = venta;
        this.direccion = direccion;
        this.estadoActual = estadoActual;
        this.transporte = transporte;
        this.costo = costo;
        this.fechaCreacion = LocalDateTime.now();
    }

    @Transient
    private final List<ObservadorEnvio> observadores = new ArrayList<>();

    @Transient
    private String motivoTransicion;

    // Patrón Observer
    public void agregarObservador(ObservadorEnvio observador) {
        if (!observadores.contains(observador)) {
            observadores.add(observador);
        }
    }

    public void eliminarObservador(ObservadorEnvio observador) {
        observadores.remove(observador);
    }

    public void notificarObservadores() {
        for (ObservadorEnvio obs : observadores) {
            obs.actualizar(this);
        }
    }

    // Patrón State
    public void transicionar() {
        if (this.estadoActual != null) {
            this.estadoActual.transicionar(this);
            notificarObservadores();
        }
    }

    public void IniciarGestion() {
        if (!(this.estadoActual instanceof EstadoPendiente)) {
            throw new IllegalStateException("Solo se puede iniciar la gestión de un envío que esté en estado Pendiente.");
        }
        this.transicionar();
    }

    public void registrarResultado(EstadoEnvio estadoFinal) {
        if (!(this.estadoActual instanceof EstadoEnTransito)) {
            throw new IllegalStateException("Solo se puede registrar el resultado final desde el estado En tránsito.");
        }
        if (!(estadoFinal instanceof EstadoEntregado) && !(estadoFinal instanceof EstadoNoEntregado)) {
            throw new IllegalArgumentException("El estado final debe ser Entregado o No entregado.");
        }
        this.estadoActual = estadoFinal;
        notificarObservadores();
    }
}