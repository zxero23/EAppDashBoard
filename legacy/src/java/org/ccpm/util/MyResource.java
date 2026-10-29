/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ccpm.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.faces.application.Resource;
import javax.faces.context.FacesContext;

/**
 *
 * @author Sector7G
 */
public class MyResource extends Resource implements java.io.Serializable {

        private String path = "";
        private HashMap<String, String> headers;
        private byte[] bytes;
        
        public MyResource(byte[] bytes) {
            this.bytes = bytes;
            this.headers = new HashMap<String, String>();
        }
        
        public InputStream getInputStream() {
            return new ByteArrayInputStream(this.bytes);
        }

        public String getRequestPath() {
            return path;
        }
        
        public void setRequestPath(String path) {
            this.path = path;
        }

        public Map<String, String> getResponseHeaders() {
            return headers;
        }

        public URL  getURL() {
            return null;
        }

        public boolean userAgentNeedsUpdate(FacesContext context) {
            return false;
        }
        
        public static byte[] readIntoByteArray(InputStream in) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try{
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                out.flush();
                
            }catch (Exception e){
                
            }
            return out.toByteArray();
        }
    }