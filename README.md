# Backend – Distribuidora de Gas El Volcán

API REST de la tienda online (DSY1104 – Evaluación Parcial 3), hecha con **Spring Boot 4.1.2**, **Java 21**, **Spring Data JPA**, **MySQL**, **Spring Security + JWT** y **Swagger**.

El frontend (React) está en: https://github.com/natyleiv/tienda-gas-react

---

## Requisitos

1. **JDK 21** (`java -version` debe decir 21 o más).
2. **XAMPP** con **MySQL** encendido (usuario `root`, sin contraseña).
3. Un IDE (IntelliJ IDEA o VS Code con el *Extension Pack for Java*). Maven no hace falta: viene el wrapper `mvnw`.

## Cómo ejecutarlo

1. Enciende MySQL en XAMPP. La base `bd_tienda_gas` **se crea sola** al iniciar (`createDatabaseIfNotExist=true`), y las tablas también (`ddl-auto=update`).
2. Crea el archivo de claves (no se sube a GitHub):
   - Copia `src/main/resources/secretos.example.properties` como `src/main/resources/secretos.properties`.
   - Cambia `app.jwt.secret` por una clave tuya de al menos 32 caracteres.
3. Ejecuta:
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```
   (o el botón *Run* de `TiendagasApplication` en el IDE).
4. Abre Swagger: http://localhost:8080/swagger-ui.html

La primera vez se cargan datos de prueba: 3 categorías, 12 productos, 4 usuarios y 5 compras de ejemplo.

## Usuarios de prueba

| Rol | Correo | Contraseña |
|---|---|---|
| Administrador | admin@duoc.cl | admin123 |
| Vendedor | vendedor@duoc.cl | vende123 |
| Cliente | cliente@duoc.cl | cliente123 |
| Cliente | jorge@duoc.cl | jorge123 |

Las contraseñas se guardan con hash **BCrypt** (en la tabla `usuario` se ve algo como `$2a$10$...`).

## Estructura

```text
src/main/java/cl/duoc/tiendagas
├── config/       DataInitializer (datos de prueba) y SwaggerConfig
├── controller/   Endpoints REST /api/v1/...
├── dto/          Lo que entra y sale de la API (sin exponer contraseñas)
├── exception/    ManejadorErrores: errores en JSON { estado, mensaje, errores }
├── model/        Entidades JPA: Usuario, Categoria, Producto, Boleta, DetalleBoleta, Consulta
├── repository/   Interfaces JpaRepository
├── security/     SecurityConfig (reglas por rol, CORS) y JwtService
└── service/      Lógica de negocio
```

## Modelo de datos

```text
USUARIO 1 ──< BOLETA 1 ──< DETALLE_BOLETA >── 1 PRODUCTO >── 1 CATEGORIA
```

- `producto.categoria_id` → `categoria`
- `boleta.usuario_id` → `usuario`
- `detalle_boleta.boleta_id` → `boleta` y `detalle_boleta.producto_id` → `producto`
- `consulta` (mensajes de contacto) es independiente.

## Seguridad

- **Login** (`POST /api/v1/auth/login`) entrega dos tokens JWT firmados con HS256:
  - `token` (acceso): dura **30 minutos** y se envía en cada petición como `Authorization: Bearer <token>`.
  - `refreshToken`: dura **7 días** y solo sirve para pedir tokens nuevos en `POST /api/v1/auth/refresh`.
- El token lleva `sub` (correo), `id`, `nombre`, `rol` y `tipo`. Spring valida **firma, vencimiento, emisor y tipo** en cada petición; un refreshToken no sirve para entrar a los endpoints.
- Bloqueo a los **3 intentos fallidos**; solo el administrador desbloquea.
- Sin token → **401**. Con token pero sin permiso → **403**.
- La clave JWT va en `secretos.properties`, que está en `.gitignore`.

| Rol | Puede |
|---|---|
| ADMINISTRADOR | Todo |
| VENDEDOR | Ver productos y su detalle, ver órdenes y su detalle |
| CLIENTE | Comprar, ver su historial y editar su perfil |

## Endpoints (v1)

| Método | Ruta | Descripción | Acceso | Respuestas |
|---|---|---|---|---|
| POST | /api/v1/auth/login | Inicia sesión | Público | 200, 400, 401, 403 |
| POST | /api/v1/auth/register | Registro de clientes (@duoc.cl) | Público | 201, 400, 409 |
| POST | /api/v1/auth/refresh | Renueva el token | Público (con refreshToken) | 200, 401 |
| GET | /api/v1/auth/me | Usuario del token | Con sesión | 200, 401 |
| GET | /api/v1/productos | Lista productos (`?categoriaId=`) | Público | 200 |
| GET | /api/v1/productos/{id} | Detalle de producto | Público | 200, 404 |
| POST | /api/v1/productos | Crea producto | ADMIN | 201, 400, 401, 403, 409 |
| PUT | /api/v1/productos/{id} | Actualiza producto | ADMIN | 200, 400, 404, 409 |
| DELETE | /api/v1/productos/{id} | Elimina producto | ADMIN | 204, 404, 409 |
| GET | /api/v1/categorias | Lista categorías | Público | 200 |
| GET | /api/v1/categorias/{id} | Detalle de categoría | Público | 200, 404 |
| POST | /api/v1/categorias | Crea categoría | ADMIN | 201, 400, 409 |
| PUT | /api/v1/categorias/{id} | Actualiza categoría | ADMIN | 200, 400, 404, 409 |
| DELETE | /api/v1/categorias/{id} | Elimina categoría (sin productos) | ADMIN | 204, 404, 409 |
| GET | /api/v1/usuarios | Lista usuarios | ADMIN | 200, 403 |
| GET | /api/v1/usuarios/{id} | Detalle de usuario | ADMIN | 200, 404 |
| POST | /api/v1/usuarios | Crea usuario con rol | ADMIN | 201, 400, 409 |
| PUT | /api/v1/usuarios/{id} | Actualiza usuario | ADMIN | 200, 400, 404, 409 |
| DELETE | /api/v1/usuarios/{id} | Elimina usuario | ADMIN | 204, 404, 409 |
| PATCH | /api/v1/usuarios/{id}/desbloquear | Desbloquea cuenta | ADMIN | 200, 404 |
| PATCH | /api/v1/usuarios/{id}/bloquear | Bloquea (desactiva) cuenta | ADMIN | 200, 404, 409 |
| GET | /api/v1/usuarios/me | Ver mi perfil | Con sesión | 200, 401 |
| PUT | /api/v1/usuarios/me | Editar mi perfil (sin rol) | Con sesión | 200, 400, 409 |
| POST | /api/v1/boletas | Confirmar compra | Con sesión | 201, 400, 404, 409 |
| GET | /api/v1/boletas/mis-boletas | Historial propio | Con sesión | 200, 401 |
| GET | /api/v1/boletas | Todas las órdenes | ADMIN, VENDEDOR | 200, 403 |
| GET | /api/v1/boletas/{id} | Detalle (cliente: solo las suyas) | Con sesión | 200, 403, 404 |
| PATCH | /api/v1/boletas/{id}/estado | Cambia estado | ADMIN | 200, 400, 404, 409 |
| GET | /api/v1/reportes/resumen | Indicadores (`?periodo=semana\|mes\|anio\|todo`) | ADMIN | 200, 400, 403 |
| POST | /api/v1/consultas | Mensaje de contacto | Público | 201, 400 |
| GET | /api/v1/consultas | Lista mensajes | ADMIN | 200, 403 |

Estados de una boleta: `PENDIENTE → EN_REPARTO → ENTREGADA`, o `CANCELADA` (devuelve el stock). `ENTREGADA` y `CANCELADA` son finales.

Cupones: `GASVOLCAN10` (10%) y `BIENVENIDO5` (5%).

## Pruebas de integración

```powershell
.\mvnw.cmd test
```

`IntegracionApiTests` levanta la API completa con una base H2 en memoria (no toca MySQL) y prueba 18 casos: login correcto e incorrecto, bloqueo, hash BCrypt, renovación del token, peticiones sin token, registro, CRUD por rol, validaciones, compra con descuento de stock, compra sin stock, historial privado, órdenes y estados, reportes, perfil y Swagger.
