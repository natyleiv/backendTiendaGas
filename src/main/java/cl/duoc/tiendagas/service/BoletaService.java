package cl.duoc.tiendagas.service;

import cl.duoc.tiendagas.dto.BoletaResponse;
import cl.duoc.tiendagas.dto.CompraRequest;
import cl.duoc.tiendagas.dto.ItemCompraRequest;
import cl.duoc.tiendagas.model.*;
import cl.duoc.tiendagas.repository.BoletaRepository;
import cl.duoc.tiendagas.repository.ProductoRepository;
import cl.duoc.tiendagas.security.UsuarioActual;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class BoletaService {

    // Cupones válidos: código -> porcentaje de descuento (mismos de la EP1/EP2).
    public static final Map<String, Double> CUPONES = Map.of(
            "GASVOLCAN10", 0.10,
            "BIENVENIDO5", 0.05
    );

    @Autowired
    private BoletaRepository boletaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private UsuarioService usuarioService;

    // Confirmar compra. @Transactional: si algo falla (ej: sin stock),
    // NO se guarda nada a medias (ni la boleta ni el descuento de stock).
    @Transactional
    public BoletaResponse crear(CompraRequest datos, UsuarioActual quien) {
        Usuario usuario = usuarioService.buscarEntidad(quien.getId());

        // El cupón se revisa primero, antes de tocar el stock.
        String cupon = datos.getCupon() == null || datos.getCupon().isBlank()
                ? null : datos.getCupon().trim().toUpperCase();
        if (cupon != null && !CUPONES.containsKey(cupon)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El cupón no es válido");
        }

        // Si el mismo producto viene dos veces, se suman las cantidades.
        Map<Long, Integer> cantidades = new LinkedHashMap<>();
        for (ItemCompraRequest item : datos.getItems()) {
            cantidades.merge(item.getProductoId(), item.getCantidad(), Integer::sum);
        }

        Boleta boleta = new Boleta();
        boleta.setUsuario(usuario);
        boleta.setFecha(LocalDateTime.now());
        boleta.setDireccion(usuario.getDireccion());
        boleta.setEstado(EstadoBoleta.PENDIENTE);

        int subtotal = 0;
        for (Map.Entry<Long, Integer> linea : cantidades.entrySet()) {
            Producto producto = productoService.buscarEntidad(linea.getKey());
            int cantidad = linea.getValue();

            // Validar disponibilidad
            if (cantidad > producto.getStock()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "No hay stock suficiente de \"" + producto.getNombre() + "\" (quedan " + producto.getStock() + ")");
            }

            int precio = producto.precioFinal();
            DetalleBoleta detalle = new DetalleBoleta(null, boleta, producto, producto.getNombre(),
                    cantidad, precio, precio * cantidad);
            boleta.getDetalles().add(detalle);
            subtotal += precio * cantidad;

            // Descontar stock
            producto.setStock(producto.getStock() - cantidad);
            productoRepository.save(producto);
        }

        int descuento = cupon == null ? 0 : (int) Math.round(subtotal * CUPONES.get(cupon));

        boleta.setSubtotal(subtotal);
        boleta.setDescuento(descuento);
        boleta.setTotal(subtotal - descuento);
        boleta.setCupon(cupon);

        return BoletaResponse.desde(boletaRepository.save(boleta));
    }

    // Historial: solo las boletas del usuario que hace la petición.
    @Transactional(readOnly = true)
    public List<BoletaResponse> misBoletas(UsuarioActual quien) {
        return boletaRepository.findByUsuarioIdOrderByFechaDesc(quien.getId())
                .stream().map(BoletaResponse::desde).toList();
    }

    // Todas las órdenes (ADMIN y VENDEDOR).
    @Transactional(readOnly = true)
    public List<BoletaResponse> listar() {
        return boletaRepository.findAllByOrderByFechaDesc().stream().map(BoletaResponse::desde).toList();
    }

    // Un cliente solo puede ver SUS boletas. ADMIN y VENDEDOR pueden ver cualquiera.
    @Transactional(readOnly = true)
    public BoletaResponse buscarPorId(Long id, UsuarioActual quien) {
        Boleta boleta = buscarEntidad(id);
        boolean esDueno = boleta.getUsuario().getId().equals(quien.getId());
        if (!esDueno && !quien.esAdmin() && !quien.esVendedor()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes ver una boleta de otro usuario");
        }
        return BoletaResponse.desde(boleta);
    }

    // Cambiar el estado del pedido (solo ADMIN).
    // ENTREGADA y CANCELADA son finales. Al cancelar, el stock vuelve al inventario.
    @Transactional
    public BoletaResponse cambiarEstado(Long id, EstadoBoleta nuevo) {
        Boleta boleta = buscarEntidad(id);
        EstadoBoleta actual = boleta.getEstado();

        if (actual == EstadoBoleta.ENTREGADA || actual == EstadoBoleta.CANCELADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La boleta ya está " + actual.name().toLowerCase() + " y no puede cambiar de estado");
        }

        if (nuevo == EstadoBoleta.CANCELADA) {
            for (DetalleBoleta detalle : boleta.getDetalles()) {
                Producto producto = detalle.getProducto();
                producto.setStock(producto.getStock() + detalle.getCantidad());
                productoRepository.save(producto);
            }
        }

        boleta.setEstado(nuevo);
        return BoletaResponse.desde(boletaRepository.save(boleta));
    }

    private Boleta buscarEntidad(Long id) {
        return boletaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Boleta no encontrada"));
    }
}
