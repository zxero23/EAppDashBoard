package org.ccpm.sysse.web;

import jakarta.servlet.http.Part;
import org.apache.commons.fileupload2.core.DiskFileItem;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;

/**
 * Adaptador de Apache Commons FileUpload2 DiskFileItem a jakarta.servlet.http.Part
 * para que el request envuelto por MultipartFilter exponga getPart()/getParts().
 */
public class FileItemPart implements Part {

    private final DiskFileItem fileItem;

    public FileItemPart(DiskFileItem fileItem) {
        this.fileItem = fileItem;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        try {
            return fileItem.getInputStream();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public String getContentType() {
        return fileItem.getContentType();
    }

    @Override
    public String getName() {
        return fileItem.getFieldName();
    }

    @Override
    public String getSubmittedFileName() {
        String name = fileItem.getName();
        if (name == null) {
            return null;
        }
        int slash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        return slash >= 0 ? name.substring(slash + 1) : name;
    }

    @Override
    public long getSize() {
        return fileItem.getSize();
    }

    @Override
    public void write(String fileName) throws IOException {
        try {
            fileItem.write(java.nio.file.Path.of(fileName));
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public void delete() throws IOException {
        try {
            fileItem.delete();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public String getHeader(String name) {
        return null;
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return Collections.emptyList();
    }

    @Override
    public Collection<String> getHeaderNames() {
        return Collections.emptyList();
    }
}
