package cl.duoc.tiendagas.service;

import cl.duoc.tiendagas.dto.ConsultaRequest;
import cl.duoc.tiendagas.model.Consulta;
import cl.duoc.tiendagas.repository.ConsultaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ConsultaService {

    @Autowired
    private ConsultaRepository consultaRepository;

    public Consulta crear(ConsultaRequest datos) {
        Consulta consulta = new Consulta(null, datos.getNombre().trim(),
                UsuarioService.normalizarCorreo(datos.getCorreo()), datos.getMensaje().trim(), LocalDateTime.now());
        return consultaRepository.save(consulta);
    }

    public List<Consulta> listar() {
        return consultaRepository.findAllByOrderByFechaDesc();
    }
}
