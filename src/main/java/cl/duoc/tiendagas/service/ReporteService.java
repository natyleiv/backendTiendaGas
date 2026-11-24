package cl.duoc.tiendagas.service;

import cl.duoc.tiendagas.dto.ProductoVendidoResponse;
import cl.duoc.tiendagas.dto.ReporteResponse;
import cl.duoc.tiendagas.model.Boleta;
import cl.duoc.tiendagas.model.DetalleBoleta;
import cl.duoc.tiendagas.model.EstadoBoleta;
import cl.duoc.tiendagas.repository.BoletaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

// Indicadores del dashboard, calculados con los datos reales de la BD.
@Service
public class ReporteService {

    @Autowired
    private BoletaRepository boletaRepository;

    @Transactional(readOnly = true)
    public ReporteResponse resumen(String periodo) {
        LocalDateTime ahora = LocalDateTime.now();
        List<Boleta> boletas = switch (periodo) {
            case "semana" -> boletaRepository.findByFechaGreaterThanEqual(ahora.minusWeeks(1));
            case "mes" -> boletaRepository.findByFechaGreaterThanEqual(ahora.minusMonths(1));
            case "anio" -> boletaRepository.findByFechaGreaterThanEqual(ahora.minusYears(1));
            case "todo" -> boletaRepository.findAll();
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Periodo inválido. Usa: semana, mes, anio o todo");
        };

        int montoTotal = 0;
        int unidades = 0;
        Map<Long, ProductoVendidoResponse> porProducto = new HashMap<>();
        Map<String, Integer> porEstado = new LinkedHashMap<>();
        for (EstadoBoleta estado : EstadoBoleta.values()) {
            porEstado.put(estado.name(), 0);
        }

        for (Boleta boleta : boletas) {
            porEstado.merge(boleta.getEstado().name(), 1, Integer::sum);
            // Las boletas canceladas no cuentan como venta.
            if (boleta.getEstado() == EstadoBoleta.CANCELADA) continue;

            montoTotal += boleta.getTotal();
            for (DetalleBoleta d : boleta.getDetalles()) {
                unidades += d.getCantidad();
                ProductoVendidoResponse p = porProducto.computeIfAbsent(d.getProducto().getId(),
                        id -> new ProductoVendidoResponse(id, d.getNombreProducto(), 0, 0));
                p.setUnidades(p.getUnidades() + d.getCantidad());
                p.setMonto(p.getMonto() + d.getSubtotal());
            }
        }

        List<ProductoVendidoResponse> top = porProducto.values().stream()
                .sorted(Comparator.comparingInt(ProductoVendidoResponse::getUnidades).reversed())
                .limit(5)
                .toList();

        return new ReporteResponse(periodo, boletas.size(), montoTotal, unidades, top, porEstado);
    }
}
