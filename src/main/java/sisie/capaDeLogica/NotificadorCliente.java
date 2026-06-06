package sisie.capaDeLogica;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import sisie.capaDeDominio.Envio;
import sisie.capaDeDominio.ObservadorEnvio;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
public class NotificadorCliente implements ObservadorEnvio {

    @Value("${n8n.webhook.url:}")
    private String webhookUrl;

    private final RestTemplate restTemplate;

    public NotificadorCliente() {
        // Configurar un RestTemplate con timeouts de 3 segundos para evitar bloqueos
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(3000);
        this.restTemplate = new RestTemplate(factory);
    }

    @Override
    public void actualizar(Envio envio) {
        String estadoNombre = envio.getEstadoActual() != null ? envio.getEstadoActual().getNombre() : "Desconocido";
        String clienteNombre = "Desconocido";

        if (envio.getVenta() != null && envio.getVenta().getCliente() != null) {
            clienteNombre = envio.getVenta().getCliente().getNombre() + " "
                    + envio.getVenta().getCliente().getApellido();
        }

        // Siempre mostramos log local para seguimiento
        System.out.println("[NotificadorCliente] Cambio de estado detectado. Envío ID "
                + envio.getIdEnvio() + " -> " + estadoNombre + " (Cliente: " + clienteNombre + ")");

        // Si el webhook está configurado, realizamos la petición HTTP a n8n
        if (webhookUrl != null && !webhookUrl.trim().isEmpty()) {
            enviarWebhookN8N(envio, estadoNombre);
        } else {
            System.out.println("[NotificadorCliente - SIMULACIÓN] Webhook n8n no configurado. Notificación simulada.");
        }
    }

    private void enviarWebhookN8N(Envio envio, String estadoNombre) {
        CompletableFuture.runAsync(() -> {
            try {
                Map<String, Object> payload = new HashMap<>();
                payload.put("idEnvio", envio.getIdEnvio());
                payload.put("codSeguimiento", envio.getCodSeguimiento());
                payload.put("costo", envio.getCosto());
                payload.put("estado", estadoNombre);
                payload.put("fecha", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                payload.put("motivo", envio.getMotivoTransicion());

                if (envio.getTransporte() != null) {
                    payload.put("transporte", envio.getTransporte().getNombre());
                }

                if (envio.getVenta() != null) {
                    payload.put("idVenta", envio.getVenta().getIdVenta());
                    if (envio.getVenta().getCliente() != null) {
                        Map<String, Object> clienteMap = new HashMap<>();
                        clienteMap.put("nombre", envio.getVenta().getCliente().getNombre());
                        clienteMap.put("apellido", envio.getVenta().getCliente().getApellido());
                        clienteMap.put("dni", envio.getVenta().getCliente().getDni());
                        clienteMap.put("email", envio.getVenta().getCliente().getEmail());
                        payload.put("cliente", clienteMap);
                    }
                }

                if (envio.getDireccion() != null) {
                    Map<String, Object> dirMap = new HashMap<>();
                    dirMap.put("calle", envio.getDireccion().getCalle());
                    dirMap.put("altura", envio.getDireccion().getAltura());
                    dirMap.put("codigoPostal", envio.getDireccion().getCodigoPostal());
                    if (envio.getDireccion().getCiudad() != null) {
                        dirMap.put("ciudad", envio.getDireccion().getCiudad().getNombre());
                        if (envio.getDireccion().getCiudad().getProvincia() != null) {
                            dirMap.put("provincia", envio.getDireccion().getCiudad().getProvincia().getNombre());
                        }
                    }
                    payload.put("direccion", dirMap);
                }

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

                System.out.println("[NotificadorCliente] Enviando POST Webhook a n8n: " + webhookUrl);
                restTemplate.postForEntity(webhookUrl, request, String.class);
                System.out.println(
                        "[NotificadorCliente] Webhook n8n enviado con éxito para envío ID: " + envio.getIdEnvio());

            } catch (Exception e) {
                System.err
                        .println("[NotificadorCliente - ERROR] Error al enviar notificación a n8n: " + e.getMessage());
            }
        });
    }
}
