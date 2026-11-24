package cl.duoc.tiendagas.model;

// Roles del sistema (Anexo 1):
// ADMINISTRADOR: acceso total. VENDEDOR: ve productos y órdenes. CLIENTE: solo la tienda.
public enum Rol {
    ADMINISTRADOR,
    VENDEDOR,
    CLIENTE
}
