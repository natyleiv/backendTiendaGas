package cl.duoc.tiendagas.service;

import cl.duoc.tiendagas.dto.CategoriaRequest;
import cl.duoc.tiendagas.model.Categoria;
import cl.duoc.tiendagas.repository.CategoriaRepository;
import cl.duoc.tiendagas.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    public List<Categoria> listar() {
        return categoriaRepository.findAll();
    }

    public Categoria buscarPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría no encontrada"));
    }

    public Categoria crear(CategoriaRequest datos) {
        String nombre = datos.getNombre().trim();
        if (categoriaRepository.existsByNombreIgnoreCase(nombre)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una categoría con ese nombre");
        }
        return categoriaRepository.save(new Categoria(null, nombre));
    }

    public Categoria actualizar(Long id, CategoriaRequest datos) {
        Categoria categoria = buscarPorId(id);
        String nombre = datos.getNombre().trim();
        if (categoriaRepository.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una categoría con ese nombre");
        }
        categoria.setNombre(nombre);
        return categoriaRepository.save(categoria);
    }

    // Regla de la EP2: no se puede borrar una categoría que tiene productos.
    public void eliminar(Long id) {
        Categoria categoria = buscarPorId(id);
        if (productoRepository.existsByCategoriaId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede eliminar: hay productos en \"" + categoria.getNombre() + "\"");
        }
        categoriaRepository.delete(categoria);
    }
}
