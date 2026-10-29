package org.ccpm.tablero.modelo;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.io.*;

/**
 * String[] serializado con Java (bytea), como lo guardaba EclipseLink en la v1.
 * Solo acepta String y String[] al deserializar: cualquier otra clase se rechaza.
 */
@Converter
public class StringArrayConverter implements AttributeConverter<String[], byte[]> {
    private static final ObjectInputFilter SOLO_STRINGS =
            ObjectInputFilter.Config.createFilter("java.lang.String;[Ljava.lang.String;maxdepth=2;maxarray=100000;!*");

    @Override
    public byte[] convertToDatabaseColumn(String[] valor) {
        if (valor == null) return null;
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        try (ObjectOutputStream o = new ObjectOutputStream(b)) {
            o.writeObject(valor);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return b.toByteArray();
    }

    @Override
    public String[] convertToEntityAttribute(byte[] datos) {
        if (datos == null || datos.length == 0) return null;
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(datos))) {
            in.setObjectInputFilter(SOLO_STRINGS);
            return (String[]) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException("Indicador con datos serializados invalidos", e);
        }
    }
}
