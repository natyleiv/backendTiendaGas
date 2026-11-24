package cl.duoc.tiendagas;

import cl.duoc.tiendagas.repository.ProductoRepository;
import cl.duoc.tiendagas.repository.UsuarioRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Pruebas de integración: levantan la API completa (controladores + servicios +
// repositorios + seguridad) con una base H2 en memoria y hacen peticiones HTTP reales.
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class IntegracionApiTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private String login(String correo, String password) throws Exception {
        String json = mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"" + correo + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(json, "$.token");
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    // ---------- Autenticación ----------

    @Test
    @DisplayName("Login correcto entrega token, refreshToken y datos del usuario sin contraseña")
    void loginCorrecto() throws Exception {
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"admin@duoc.cl\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.usuario.rol").value("ADMINISTRADOR"))
                .andExpect(jsonPath("$.usuario.password").doesNotExist());
    }

    @Test
    @DisplayName("Contraseña incorrecta: 401, y al tercer intento la cuenta queda bloqueada (403)")
    void loginIncorrectoYBloqueo() throws Exception {
        String malo = "{\"correo\":\"cliente@duoc.cl\",\"password\":\"mala\"}";
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(malo))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje", containsString("Intentos restantes: 2")));
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(malo))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(malo))
                .andExpect(status().isForbidden());
        // Ni con la contraseña correcta puede entrar
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"cliente@duoc.cl\",\"password\":\"cliente123\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Las contraseñas se guardan con hash BCrypt")
    void passwordConHash() {
        String hash = usuarioRepository.findByCorreo("admin@duoc.cl").orElseThrow().getPassword();
        assertThat(hash).startsWith("$2").isNotEqualTo("admin123");
    }

    @Test
    @DisplayName("Renovación: el refreshToken entrega un token nuevo, pero no sirve como token de acceso")
    void renovarToken() throws Exception {
        String json = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"cliente@duoc.cl\",\"password\":\"cliente123\"}"))
                .andReturn().getResponse().getContentAsString();
        String refresh = JsonPath.read(json, "$.refreshToken");

        mvc.perform(post("/api/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refresh + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());

        mvc.perform(get("/api/v1/boletas/mis-boletas").header("Authorization", bearer(refresh)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Sin token o con token falso: 401")
    void sinTokenValido() throws Exception {
        mvc.perform(get("/api/v1/boletas/mis-boletas")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/boletas/mis-boletas").header("Authorization", "Bearer token.falso.123"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"no.es.valido\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Registro público crea siempre un CLIENTE y valida RUN y dominio")
    void registro() throws Exception {
        String datos = """
                {"run":"20202020-0","nombre":"Ana","apellidos":"Pérez","correo":"ana@duoc.cl","password":"ana1234",
                 "fechaNacimiento":"2001-05-10","region":"Ñuble","direccion":"Calle 1, Chillán","rol":"ADMINISTRADOR"}
                """;
        // RUN inválido
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(datos))
                .andExpect(status().isBadRequest());

        String run = calcularRun("20202020");
        String valido = datos.replace("20202020-0", run);
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(valido))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("CLIENTE"));

        // Correo de otro dominio no permitido en el registro
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(valido.replace("ana@duoc.cl", "ana@gmail.com").replace(run, calcularRun("21212121"))))
                .andExpect(status().isBadRequest());
    }

    // ---------- Catálogo y roles ----------

    @Test
    @DisplayName("El catálogo es público")
    void catalogoPublico() throws Exception {
        mvc.perform(get("/api/v1/productos")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(12)));
        mvc.perform(get("/api/v1/categorias")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(3)));
        mvc.perform(get("/api/v1/productos/999")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("CRUD de productos: solo ADMIN. Cliente y vendedor reciben 403")
    void crudProductosPorRol() throws Exception {
        String nuevo = """
                {"codigo":"ACC-LLA","nombre":"Llave de paso","categoriaId":2,"precio":3990,
                 "imagen":"/img/regulador.jpg","stock":10,"stockCritico":2,"descripcion":"Llave de paso de gas"}
                """;
        mvc.perform(post("/api/v1/productos").contentType(MediaType.APPLICATION_JSON).content(nuevo))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/productos").header("Authorization", bearer(login("cliente@duoc.cl", "cliente123")))
                        .contentType(MediaType.APPLICATION_JSON).content(nuevo))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/productos").header("Authorization", bearer(login("vendedor@duoc.cl", "vende123")))
                        .contentType(MediaType.APPLICATION_JSON).content(nuevo))
                .andExpect(status().isForbidden());

        String admin = bearer(login("admin@duoc.cl", "admin123"));
        String creado = mvc.perform(post("/api/v1/productos").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content(nuevo))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Integer id = JsonPath.read(creado, "$.id");

        mvc.perform(put("/api/v1/productos/" + id).header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content(nuevo.replace("3990", "4490")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.precio").value(4490));

        // Oferta mayor que el precio: 400
        mvc.perform(put("/api/v1/productos/" + id).header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content(nuevo.replace("\"precio\":3990", "\"precio\":3990,\"precioOferta\":5000")))
                .andExpect(status().isBadRequest());

        mvc.perform(delete("/api/v1/productos/" + id).header("Authorization", admin))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/productos/" + id)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Validaciones: datos vacíos devuelven 400 con el detalle por campo")
    void validacionDeCampos() throws Exception {
        mvc.perform(post("/api/v1/productos").header("Authorization", bearer(login("admin@duoc.cl", "admin123")))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombre").exists());
    }

    @Test
    @DisplayName("No se puede eliminar una categoría con productos (409)")
    void categoriaConProductos() throws Exception {
        mvc.perform(delete("/api/v1/categorias/1").header("Authorization", bearer(login("admin@duoc.cl", "admin123"))))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Usuarios: solo ADMIN. El vendedor no puede listarlos")
    void usuariosSoloAdmin() throws Exception {
        mvc.perform(get("/api/v1/usuarios").header("Authorization", bearer(login("vendedor@duoc.cl", "vende123"))))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/usuarios").header("Authorization", bearer(login("admin@duoc.cl", "admin123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }

    // ---------- Compras e historial ----------

    @Test
    @DisplayName("Compra: crea la boleta del usuario del token, aplica cupón y descuenta stock")
    void compraDescuentaStock() throws Exception {
        int stockAntes = productoRepository.findById(2L).orElseThrow().getStock();
        String cliente = bearer(login("cliente@duoc.cl", "cliente123"));

        mvc.perform(post("/api/v1/boletas").header("Authorization", cliente).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"productoId\":2,\"cantidad\":2}],\"cupon\":\"GASVOLCAN10\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.subtotal").value(29980))
                .andExpect(jsonPath("$.descuento").value(2998))
                .andExpect(jsonPath("$.total").value(26982))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.correo").value("cliente@duoc.cl"));

        assertThat(productoRepository.findById(2L).orElseThrow().getStock()).isEqualTo(stockAntes - 2);
    }

    @Test
    @DisplayName("Compra sin stock suficiente: 409 y el stock no cambia")
    void compraSinStock() throws Exception {
        String cliente = bearer(login("cliente@duoc.cl", "cliente123"));
        mvc.perform(post("/api/v1/boletas").header("Authorization", cliente).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"productoId\":2,\"cantidad\":1},{\"productoId\":7,\"cantidad\":1}]}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje", containsString("Abrazaderas")));
        // Gracias a @Transactional, el cilindro (que sí tenía stock) tampoco se descontó
        assertThat(productoRepository.findById(2L).orElseThrow().getStock()).isEqualTo(60);
    }

    @Test
    @DisplayName("Historial: cada cliente ve solo sus boletas y no puede ver las de otro (403)")
    void historialPrivado() throws Exception {
        String camila = bearer(login("cliente@duoc.cl", "cliente123"));
        String jorge = bearer(login("jorge@duoc.cl", "jorge123"));

        mvc.perform(get("/api/v1/boletas/mis-boletas").header("Authorization", camila))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].correo", everyItem(is("cliente@duoc.cl"))));

        // La boleta 3 es de Jorge
        mvc.perform(get("/api/v1/boletas/3").header("Authorization", camila)).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/boletas/3").header("Authorization", jorge)).andExpect(status().isOk());
        // El cliente no puede listar todas las órdenes
        mvc.perform(get("/api/v1/boletas").header("Authorization", camila)).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Órdenes: el vendedor las ve, pero solo el ADMIN cambia el estado. Cancelar devuelve stock")
    void ordenesYEstados() throws Exception {
        String vendedor = bearer(login("vendedor@duoc.cl", "vende123"));
        String admin = bearer(login("admin@duoc.cl", "admin123"));

        mvc.perform(get("/api/v1/boletas").header("Authorization", vendedor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)));

        mvc.perform(patch("/api/v1/boletas/1/estado").header("Authorization", vendedor)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"EN_REPARTO\"}"))
                .andExpect(status().isForbidden());

        int stockAntes = productoRepository.findById(2L).orElseThrow().getStock();
        mvc.perform(patch("/api/v1/boletas/1/estado").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"CANCELADA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADA"));
        assertThat(productoRepository.findById(2L).orElseThrow().getStock()).isEqualTo(stockAntes + 2);

        // Una boleta cancelada ya no cambia
        mvc.perform(patch("/api/v1/boletas/1/estado").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"ENTREGADA\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Reportes: solo ADMIN, calculados con las boletas de la BD")
    void reportes() throws Exception {
        mvc.perform(get("/api/v1/reportes/resumen?periodo=todo").header("Authorization", bearer(login("admin@duoc.cl", "admin123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBoletas").value(5))
                .andExpect(jsonPath("$.boletasPorEstado.CANCELADA").value(1))
                .andExpect(jsonPath("$.masVendidos[0].nombre").value("Cilindro de Gas 11 kg"));
        mvc.perform(get("/api/v1/reportes/resumen").header("Authorization", bearer(login("vendedor@duoc.cl", "vende123"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Perfil: el usuario edita sus datos pero no puede cambiar su rol")
    void perfilSinCambioDeRol() throws Exception {
        String cliente = bearer(login("cliente@duoc.cl", "cliente123"));
        mvc.perform(put("/api/v1/usuarios/me").header("Authorization", cliente).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"run":"18765432-7","nombre":"Camila Andrea","apellidos":"Fuentes","correo":"cliente@duoc.cl",
                                 "fechaNacimiento":"2000-01-15","region":"Biobío","direccion":"Nueva 123","rol":"ADMINISTRADOR"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Camila Andrea"))
                .andExpect(jsonPath("$.rol").value("CLIENTE"));
        // Sigue pudiendo entrar con su contraseña (no se borró al no enviarla)
        login("cliente@duoc.cl", "cliente123");
    }

    @Test
    @DisplayName("Swagger está disponible")
    void swagger() throws Exception {
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/productos']").exists());
    }

    private static String calcularRun(String cuerpo) {
        int suma = 0, factor = 2;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += Character.getNumericValue(cuerpo.charAt(i)) * factor;
            factor = factor == 7 ? 2 : factor + 1;
        }
        int resto = 11 - (suma % 11);
        String dv = resto == 11 ? "0" : resto == 10 ? "K" : String.valueOf(resto);
        return cuerpo + "-" + dv;
    }
}
