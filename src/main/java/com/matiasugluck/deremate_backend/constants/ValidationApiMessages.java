package com.matiasugluck.deremate_backend.constants;

public final class ValidationApiMessages {

    // --- Mensajes Generales ---
    // Para @NotNull
    public static final String FIELD_CANNOT_BE_NULL = "El campo no puede ser nulo.";
    // Para @NotBlank
    public static final String FIELD_CANNOT_BE_BLANK = "El campo no puede estar en blanco";
    // Para @NotEmpty
    public static final String FIELD_CANNOT_BE_EMPTY = "El campo no puede estar vacio.";

    // --- Mensajes Específicos ---
    public static final String INVALID_EMAIL_FORMAT = "El email no es valido.";
    public static final String GENERIC_SIZE_NOT_MET = "El campo debe tener entre {min} y {max} caracteres.";
    public static final String PASSWORD_TOO_SHORT = "La contraseña debe tener como minimo {min} caracteres.";
    public static final String PASSWORD_REQUIREMENTS = "La contraseña debe tener como minimo un caracter mayuscula y un numero.";
    public static final String INVALID_VALUE_PATTERN = "Campo invalido.";

    private ValidationApiMessages() {
        throw new IllegalStateException("Utility class");
    }
}
