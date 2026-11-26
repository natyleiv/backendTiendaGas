# Resultados de las pruebas – Backend Gas El Volcán

Pruebas de integración de la API (`src/test/java/cl/duoc/tiendagas/IntegracionApiTests.java`), hechas con **JUnit 5 + MockMvc**.
Levantan la aplicación completa con una base **H2 en memoria**, así que no modifican los datos de MySQL.

## Cómo ejecutarlas

```powershell
.\mvnw.cmd test
```

## Resultado

**Tests run: 18, Failures: 0, Errors: 0, Skipped: 0** (tiempo total: 31.3 s)

| N° | Prueba | Resultado | Tiempo |
|---|---|---|---|
| 1 | Login correcto entrega token, refreshToken y datos del usuario sin contraseña | Correcta | 0.27 s |
| 2 | Contraseña incorrecta: 401, y al tercer intento la cuenta queda bloqueada (403) | Correcta | 0.29 s |
| 3 | Las contraseñas se guardan con hash BCrypt | Correcta | 0.01 s |
| 4 | Renovación: el refreshToken entrega un token nuevo, pero no sirve como token de acceso | Correcta | 0.10 s |
| 5 | Sin token o con token falso: 401 | Correcta | 0.19 s |
| 6 | Registro público crea siempre un CLIENTE y valida RUN y dominio | Correcta | 0.13 s |
| 7 | El catálogo es público | Correcta | 0.06 s |
| 8 | CRUD de productos: solo ADMIN. Cliente y vendedor reciben 403 | Correcta | 0.36 s |
| 9 | Validaciones: datos vacíos devuelven 400 con el detalle por campo | Correcta | 0.12 s |
| 10 | No se puede eliminar una categoría con productos (409) | Correcta | 0.10 s |
| 11 | Usuarios: solo ADMIN. El vendedor no puede listarlos | Correcta | 0.24 s |
| 12 | Compra: crea la boleta del usuario del token, aplica cupón y descuenta stock | Correcta | 0.25 s |
| 13 | Compra sin stock suficiente: 409 y el stock no cambia | Correcta | 0.14 s |
| 14 | Historial: cada cliente ve solo sus boletas y no puede ver las de otro (403) | Correcta | 0.22 s |
| 15 | Órdenes: el vendedor las ve, pero solo el ADMIN cambia el estado. Cancelar devuelve stock | Correcta | 0.27 s |
| 16 | Reportes: solo ADMIN, calculados con las boletas de la BD | Correcta | 0.17 s |
| 17 | Perfil: el usuario edita sus datos pero no puede cambiar su rol | Correcta | 0.24 s |
| 18 | Swagger está disponible | Correcta | 1.18 s |

## Qué cubren

- **Autenticación y JWT:** login, bloqueo a los 3 intentos, hash BCrypt, renovación con refreshToken y rechazo de tokens falsos (401).
- **Autorización por rol:** el CRUD de productos y la gestión de usuarios son solo para ADMIN; cliente y vendedor reciben 403.
- **Validaciones:** datos vacíos o inválidos devuelven 400 con el detalle por campo.
- **Reglas de negocio:** categoría con productos (409), compra con cupón y descuento de stock, compra sin stock (409 sin cambios), historial privado, cambio de estado de órdenes y devolución de stock al cancelar, reportes y perfil sin cambio de rol.
- **Documentación:** Swagger disponible.

Las pruebas manuales del sistema integrado (frontend + backend + MySQL) están en el documento `Anexo-2-Documentacion-APIs-e-Integracion.docx`, sección "Pruebas y resultados de la comunicación".
