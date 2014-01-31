/*
 MongoDB Based Session Handler
 Copryright © 2013 Kazım SARIKAYA

 This program is licensed under the terms of Sanal Diyar Software License. Please
 read the license file or visit http://license.sanaldiyar.com
 */
package com.sanaldiyar.projects.nanohttpd.mongodbbasedsessionhandler;

import com.mongodb.DBObject;
import com.sanaldiyar.projects.nanohttpd.nanohttpd.NanoSessionManager;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;

/**
 * MongoDB Based Session Manager. Stores session data inside mongodb
 *
 * @author kazim
 */
class MongoDBBasedSessionManager implements NanoSessionManager {

    private final DBObject session;

    public MongoDBBasedSessionManager(DBObject session) {
        this.session = session;
    }

    public DBObject getSession() {
        return session;
    }

    Date getExpires() {
        return (Date) session.get("expires");
    }

    void setExpires(Date expires) {
        session.put("expires", expires);
    }

    public String getSessionID() {
        return (String) session.get("sessionid");
    }

    public void setSessionID(String sessionID) {
        session.put("sessionid", sessionID);
    }

    @Override
    public <T> T get(String key, Class<T> clazz) {
        try {
            byte[] data = (byte[]) session.get(key);
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            ObjectInputStream ois = new ObjectInputStream(bais);
            Object value = ois.readObject();
            ois.close();
            return (T) value;
        } catch (Exception ex) {
        }
        return null;
    }

    @Override
    public void set(String key, Object value) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(value);
            oos.close();
            session.put(key, baos.toByteArray());
        } catch (Exception ex) {
        }
    }

    @Override
    public void delete(String key) {
        session.removeField(key);
    }

}
