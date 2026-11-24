package cl.duoc.tiendagas.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

// Convierte los errores en respuestas JSON claras para el frontend:
// { "estado": 400, "mensaje": "...", "errores": { "campo": "mensaje" } }
@RestControllerAdvice
public class ManejadorErrores {

    // Errores de negocio lanzados en los servicios (404, 409, 401...).
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> errorDeNegocio(ResponseStatusException e) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("estado", e.getStatusCode().value());
        cuerpo.put("mensaje", e.getReason());
        return ResponseEntity.status(e.getStatusCode()).body(cuerpo);
    }

    // Errores de validación (@NotBlank, @Email, @Size...) -> 400 con el detalle por campo.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> datosInvalidos(MethodArgumentNotValidException e) {
        Map<String, String> errores = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(error -> errores.putIfAbsent(error.getField(), error.getDefaultMessage()));

        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("estado", 400);
        cuerpo.put("mensaje", "Hay datos inválidos en el formulario");
        cuerpo.put("errores", errores);
        return ResponseEntity.badRequest().body(cuerpo);
    }

    // JSON mal escrito o con un tipo incorrecto (ej: texto en un número).
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> jsonInvalido(HttpMessageNotReadableException e) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("estado", 400);
        cuerpo.put("mensaje", "El cuerpo de la petición no tiene un formato válido");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(cuerpo);
    }
}
