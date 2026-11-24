package cl.duoc.tiendagas.config;

import cl.duoc.tiendagas.model.*;
import cl.duoc.tiendagas.repository.BoletaRepository;
import cl.duoc.tiendagas.repository.CategoriaRepository;
import cl.duoc.tiendagas.repository.ProductoRepository;
import cl.duoc.tiendagas.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// Datos de prueba: se cargan SOLO si la base está vacía (la primera vez que se ejecuta).
@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner cargarDatos(CategoriaRepository categoriaRepository,
                                  ProductoRepository productoRepository,
                                  UsuarioRepository usuarioRepository,
                                  BoletaRepository boletaRepository,
                                  PasswordEncoder encoder) {
        return args -> {
            if (categoriaRepository.count() > 0) {
                return; // ya hay datos
            }

            // ---- Categorías ----
            Categoria cilindros = categoriaRepository.save(new Categoria(null, "Cilindros de Gas"));
            Categoria accesorios = categoriaRepository.save(new Categoria(null, "Accesorios"));
            Categoria artefactos = categoriaRepository.save(new Categoria(null, "Artefactos a Gas"));

            // ---- Productos (mismo catálogo de la EP2; las imágenes están en el frontend) ----
            Producto cil5 = producto("CIL-5", "Cilindro de Gas 5 kg", cilindros, 8990, null, "/img/cilindro5.jpg", 40, 8,
                    "Cilindro de gas licuado de 5 kg, ideal para cocinillas portátiles.");
            Producto cil11 = producto("CIL-11", "Cilindro de Gas 11 kg", cilindros, 14990, null, "/img/cilindro11.jpg", 60, 10,
                    "El formato más usado en los hogares. Rinde para cocina y agua caliente.");
            Producto cil15 = producto("CIL-15", "Cilindro de Gas 15 kg", cilindros, 18990, null, "/img/cilindro15.jpg", 35, 8,
                    "Mayor duración para familias grandes o uso en cocina y calefacción.");
            Producto cil45 = producto("CIL-45", "Cilindro de Gas 45 kg", cilindros, 54990, null, "/img/cilindro45.jpg", 6, 6,
                    "Formato industrial para locales comerciales y restaurantes.");
            Producto regulador = producto("ACC-REG", "Regulador de Gas", accesorios, 6990, null, "/img/regulador.jpg", 50, 10,
                    "Regulador compatible con cilindros de 5, 11, 15 y 45 kg.");
            Producto manguera = producto("ACC-MAN", "Manguera de Gas 1,5 m", accesorios, 4990, null, "/img/manguera.jpg", 45, 10,
                    "Manguera flexible reforzada de 1,5 metros.");
            Producto abrazaderas = producto("ACC-ABR", "Abrazaderas (par)", accesorios, 2490, null, "/img/abrazadera.jpg", 0, 5,
                    "Par de abrazaderas metálicas para asegurar la manguera.");
            Producto detector = producto("ACC-DET", "Detector de Fuga de Gas", accesorios, 15990, 12990, "/img/detector.jpg", 20, 4,
                    "Alarma sonora que avisa si hay gas en el ambiente.");
            Producto cocinilla = producto("ART-COC", "Cocinilla a Gas 2 Platos", artefactos, 34990, null, "/img/anafe.jpg", 15, 3,
                    "Cocinilla de 2 platos con encendido automático.");
            Producto calefont = producto("ART-CAL", "Calefont a Gas 5 L", artefactos, 149990, null, "/img/calefont.jpg", 3, 2,
                    "Calefont instantáneo de 5 litros por minuto.");
            Producto estufa = producto("ART-EST", "Estufa a Gas Catalítica", artefactos, 69990, null, "/img/estufa.jpg", 0, 3,
                    "Calefacción silenciosa, sin llama visible.");
            Producto anafe = producto("ART-ANA", "Anafe Portátil", artefactos, 27990, 22990, "/img/cocinilla.jpg", 22, 5,
                    "Anafe compacto, perfecto para camping o como respaldo.");
            productoRepository.saveAll(List.of(cil5, cil11, cil15, cil45, regulador, manguera, abrazaderas,
                    detector, cocinilla, calefont, estufa, anafe));

            // ---- Usuarios (contraseñas guardadas con BCrypt) ----
            Usuario admin = usuario("12345678-5", "Marcela", "Soto", "admin@duoc.cl", encoder.encode("admin123"),
                    LocalDate.of(1985, 4, 12), "Ñuble", "Camino a Confluencia 450, Chillán", Rol.ADMINISTRADOR);
            Usuario vendedor = usuario("11111111-1", "Pedro", "Rojas", "vendedor@duoc.cl", encoder.encode("vende123"),
                    LocalDate.of(1992, 9, 30), "Ñuble", "Av. Collín 1200, Chillán", Rol.VENDEDOR);
            Usuario camila = usuario("18765432-7", "Camila", "Fuentes", "cliente@duoc.cl", encoder.encode("cliente123"),
                    LocalDate.of(2000, 1, 15), "Biobío", "Calle Los Aromos 321, Concepción", Rol.CLIENTE);
            Usuario jorge = usuario("15432198-5", "Jorge", "Muñoz", "jorge@duoc.cl", encoder.encode("jorge123"),
                    LocalDate.of(1978, 6, 3), "Ñuble", "Pasaje El Roble 87, San Carlos", Rol.CLIENTE);
            usuarioRepository.saveAll(List.of(admin, vendedor, camila, jorge));

            // ---- Compras de ejemplo (para el historial y el dashboard) ----
            LocalDateTime ahora = LocalDateTime.now();
            boletaRepository.saveAll(List.of(
                    boleta(camila, ahora.minusDays(2), EstadoBoleta.PENDIENTE, null,
                            new Object[]{cil11, 2}, new Object[]{regulador, 1}),
                    boleta(camila, ahora.minusDays(20), EstadoBoleta.ENTREGADA, "GASVOLCAN10",
                            new Object[]{cil15, 1}, new Object[]{detector, 1}),
                    boleta(jorge, ahora.minusDays(5), EstadoBoleta.EN_REPARTO, null,
                            new Object[]{cil45, 1}),
                    boleta(jorge, ahora.minusMonths(3), EstadoBoleta.ENTREGADA, "BIENVENIDO5",
                            new Object[]{cil11, 3}, new Object[]{manguera, 2}),
                    boleta(camila, ahora.minusMonths(6), EstadoBoleta.CANCELADA, null,
                            new Object[]{anafe, 1})
            ));
        };
    }

    private Producto producto(String codigo, String nombre, Categoria categoria, int precio, Integer oferta,
                              String imagen, int stock, int stockCritico, String descripcion) {
        return new Producto(null, codigo, nombre, categoria, precio, oferta, imagen, stock, stockCritico, descripcion);
    }

    private Usuario usuario(String run, String nombre, String apellidos, String correo, String hash,
                            LocalDate nacimiento, String region, String direccion, Rol rol) {
        return new Usuario(null, run, nombre, apellidos, correo, hash, nacimiento, region, direccion, rol, 0, false);
    }

    // Arma una boleta histórica. Cada línea es {producto, cantidad}.
    private Boleta boleta(Usuario usuario, LocalDateTime fecha, EstadoBoleta estado, String cupon, Object[]... lineas) {
        Boleta boleta = new Boleta();
        boleta.setUsuario(usuario);
        boleta.setFecha(fecha);
        boleta.setDireccion(usuario.getDireccion());
        boleta.setEstado(estado);
        boleta.setCupon(cupon);

        int subtotal = 0;
        for (Object[] linea : lineas) {
            Producto producto = (Producto) linea[0];
            int cantidad = (Integer) linea[1];
            int precio = producto.precioFinal();
            boleta.getDetalles().add(new DetalleBoleta(null, boleta, producto, producto.getNombre(),
                    cantidad, precio, precio * cantidad));
            subtotal += precio * cantidad;
        }
        double porcentaje = cupon == null ? 0 : cl.duoc.tiendagas.service.BoletaService.CUPONES.get(cupon);
        int descuento = (int) Math.round(subtotal * porcentaje);
        boleta.setSubtotal(subtotal);
        boleta.setDescuento(descuento);
        boleta.setTotal(subtotal - descuento);
        return boleta;
    }
}
