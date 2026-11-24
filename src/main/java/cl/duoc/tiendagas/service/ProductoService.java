package cl.duoc.tiendagas.service;

import cl.duoc.tiendagas.dto.ProductoRequest;
import cl.duoc.tiendagas.dto.ProductoResponse;
import cl.duoc.tiendagas.model.Categoria;
import cl.duoc.tiendagas.model.Producto;
import cl.duoc.tiendagas.repository.DetalleBoletaRepository;
import cl.duoc.tiendagas.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private DetalleBoletaRepository detalleBoletaRepository;

    @Autowired
    private CategoriaService categoriaService;

    // Lista todos, o solo los de una categoría si viene categoriaId.
    public List<ProductoResponse> listar(Long categoriaId) {
        List<Producto> productos = categoriaId == null
                ? productoRepository.findAll()
                : productoRepository.findByCategoriaId(categoriaId);
        return productos.stream().map(ProductoResponse::desde).toList();
    }

    public Producto buscarEntidad(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
    }

    public ProductoResponse buscarPorId(Long id) {
        return ProductoResponse.desde(buscarEntidad(id));
    }

    public ProductoResponse crear(ProductoRequest datos) {
        validar(datos);
        String codigo = datos.getCodigo().trim().toUpperCase();
        if (productoRepository.existsByCodigo(codigo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un producto con ese código");
        }
        Producto producto = new Producto();
        copiarDatos(datos, producto);
        return ProductoResponse.desde(productoRepository.save(producto));
    }

    public ProductoResponse actualizar(Long id, ProductoRequest datos) {
        Producto producto = buscarEntidad(id);
        validar(datos);
        String codigo = datos.getCodigo().trim().toUpperCase();
        if (productoRepository.existsByCodigoAndIdNot(codigo, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un producto con ese código");
        }
        copiarDatos(datos, producto);
        return ProductoResponse.desde(productoRepository.save(producto));
    }

    // Un producto que ya aparece en boletas no se borra, para no romper el historial.
    public void eliminar(Long id) {
        Producto producto = buscarEntidad(id);
        if (detalleBoletaRepository.existsByProductoId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede eliminar: el producto ya aparece en compras. Puedes dejarlo con stock 0.");
        }
        productoRepository.delete(producto);
    }

    private void validar(ProductoRequest datos) {
        if (datos.getPrecioOferta() != null && datos.getPrecioOferta() >= datos.getPrecio()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El precio de oferta debe ser menor que el precio normal");
        }
    }

    private void copiarDatos(ProductoRequest datos, Producto producto) {
        Categoria categoria = categoriaService.buscarPorId(datos.getCategoriaId());
        producto.setCodigo(datos.getCodigo().trim().toUpperCase());
        producto.setNombre(datos.getNombre().trim());
        producto.setCategoria(categoria);
        producto.setPrecio(datos.getPrecio());
        producto.setPrecioOferta(datos.getPrecioOferta());
        producto.setImagen(datos.getImagen().trim());
        producto.setStock(datos.getStock());
        producto.setStockCritico(datos.getStockCritico());
        producto.setDescripcion(datos.getDescripcion().trim());
    }
}
