package org.ccpm.tablero.seguridad;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * BCrypt para claves nuevas. Acepta las claves en texto plano de la v1 (que las pasaba a minusculas)
 * y, si migrar=true, pide a Spring Security que las re-encripte en el primer login exitoso.
 */
public class ClaveEncoder implements PasswordEncoder {
    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
    private final boolean migrar;

    public ClaveEncoder(boolean migrar) {
        this.migrar = migrar;
    }

    @Override
    public String encode(CharSequence clave) {
        return bcrypt.encode(clave);
    }

    @Override
    public boolean matches(CharSequence clave, String guardada) {
        if (clave == null || guardada == null || guardada.isEmpty()) return false;
        if (esBcrypt(guardada)) return bcrypt.matches(clave, guardada);
        String c = clave.toString();
        return iguales(c, guardada) || iguales(c.toLowerCase(), guardada);
    }

    @Override
    public boolean upgradeEncoding(String guardada) {
        return migrar && guardada != null && !esBcrypt(guardada);
    }

    static boolean esBcrypt(String s) {
        return s.matches("^\\$2[aby]?\\$\\d\\d\\$.{53}$");
    }

    private static boolean iguales(String a, String b) {
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }
}
