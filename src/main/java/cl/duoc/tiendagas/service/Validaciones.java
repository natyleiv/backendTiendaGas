package cl.duoc.tiendagas.service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

// Mismas reglas que el frontend (validaciones.ts), ahora también en el backend:
// nunca hay que confiar solo en la validación del navegador.
public class Validaciones {

    public static final List<String> DOMINIOS_GENERALES = List.of("duoc.cl", "profesor.duoc.cl", "gmail.com");
    public static final List<String> DOMINIOS_REGISTRO = List.of("duoc.cl");
    public static final int EDAD_MINIMA = 14;

    private Validaciones() {
    }

    // RUN con dígito verificador (módulo 11). Acepta 12345678-5, 12.345.678-5 o 123456785.
    public static boolean runValido(String run) {
        if (run == null) return false;
        String limpio = run.replace(".", "").replace("-", "").replace(" ", "").toUpperCase();
        if (!limpio.matches("\\d{7,8}[0-9K]")) return false;

        String cuerpo = limpio.substring(0, limpio.length() - 1);
        char dv = limpio.charAt(limpio.length() - 1);

        int suma = 0;
        int factor = 2;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += Character.getNumericValue(cuerpo.charAt(i)) * factor;
            factor = factor == 7 ? 2 : factor + 1;
        }
        int resto = 11 - (suma % 11);
        char esperado = resto == 11 ? '0' : resto == 10 ? 'K' : Character.forDigit(resto, 10);
        return dv == esperado;
    }

    // Deja el RUN siempre igual: 12345678-5
    public static String normalizarRun(String run) {
        String limpio = run.replace(".", "").replace("-", "").replace(" ", "").toUpperCase();
        return limpio.substring(0, limpio.length() - 1) + "-" + limpio.charAt(limpio.length() - 1);
    }

    public static boolean dominioPermitido(String correo, List<String> dominios) {
        int arroba = correo.lastIndexOf('@');
        return arroba > 0 && dominios.contains(correo.substring(arroba + 1).toLowerCase());
    }

    public static int edad(LocalDate fechaNacimiento) {
        return Period.between(fechaNacimiento, LocalDate.now()).getYears();
    }
}
