/*
 MongoDB Based Session Handler
 Copryright © 2013 Kazım SARIKAYA

 This program is licensed under the terms of Sanal Diyar Software License. Please
 read the license file or visit http://license.sanaldiyar.com
 */
package com.sanaldiyar.projects.nanohttpd.mongodbbasedsessionhandler;

import com.mongodb.DBObject;
import com.sanaldiyar.projects.nanohttpd.nanohttpd.NanoSessionManager;
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
            return (T) session.get(key);
        } catch (Exception ex) {
        }
        return null;
    }

    @Override
    public void set(String key, Object value) {
        try {
            session.put(key, value);
        } catch (Exception ex) {
        }
    }

    @Override
    public void delete(String key) {
        session.removeField(key);
    }

}
