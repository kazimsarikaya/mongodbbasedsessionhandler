/*
 MongoDB Based Session Handler
 Copryright © 2013 Kazım SARIKAYA

 This program is licensed under the terms of Sanal Diyar Software License. Please
 read the license file or visit http://license.sanaldiyar.com
 */
package com.sanaldiyar.projects.nanohttpd.mongodbbasedsessionhandler;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.sanaldiyar.projects.nanohttpd.nanohttpd.Cookie;
import com.sanaldiyar.projects.nanohttpd.nanohttpd.NanoSessionHandler;
import com.sanaldiyar.projects.nanohttpd.nanohttpd.NanoSessionManager;
import com.sanaldiyar.projects.nanohttpd.nanohttpd.Request;
import com.sanaldiyar.projects.nanohttpd.nanohttpd.Response;
import java.math.BigInteger;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Memory Based Session Handler class. Manages session cookie and session
 * managers.
 *
 * @author kazim
 */
public class MongoDBBasedSessionHandler implements NanoSessionHandler {

    private final static String SESSIONCOOKIEID = "__MONGODBBASEDSESSIONHANDLER__";
    private final SecureRandom srng;
    private DB managers;
    private MongoClient mongoClient;

    private final int mdbbasedsh_sesstimeout;

    /**
     * Initilize secure random generator
     *
     * @param properties configuration
     */
    public MongoDBBasedSessionHandler(Properties properties) {
        SecureRandom sr = new SecureRandom();
        srng = new SecureRandom(sr.generateSeed(16));

        String mdbbasedsh_host = properties.getProperty("mdbbasedsh.host");
        int mdbbasedsh_port = Integer.parseInt(properties.getProperty("mdbbasedsh.port"));
        String mdbbasedsh_database = properties.getProperty("mdbbasedsh.database");
        String mdbbasedsh_user = properties.getProperty("mdbbasedsh.user");
        String mdbbasedsh_password = properties.getProperty("mdbbasedsh.password");
        boolean flush = Boolean.parseBoolean(properties.getProperty("mdbbasedsh.flush"));

        mdbbasedsh_sesstimeout = Integer.parseInt(properties.getProperty("mdbbasedsh.sesstimeout"));

        try {
            mongoClient = new MongoClient(mdbbasedsh_host, mdbbasedsh_port);
            managers = mongoClient.getDB(mdbbasedsh_database);
            managers.authenticate(mdbbasedsh_user, mdbbasedsh_password.toCharArray());
            DBCollection sessions = managers.getCollection("sessions");
            if (flush) {
                sessions.drop();
                sessions = managers.getCollection("sessions");
            }
            sessions.ensureIndex(new BasicDBObject("sessionid", 1), "sessionid_idx", true);
            sessions.ensureIndex(new BasicDBObject("expires", 1));
        } catch (UnknownHostException ex) {
        }
    }

    /**
     * Request parser for session. Gets and builds session information
     *
     * @param request the request
     * @return session manager
     */
    @Override
    public NanoSessionManager parseRequest(Request request) {
        MongoDBBasedSessionManager nanoSessionManager = null;
        DBObject session = null;
        String sessionid = null;
        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals(SESSIONCOOKIEID)) {
                sessionid = cookie.getValue();
                break;
            }
        }
        DBCollection sessions = managers.getCollection("sessions");
        if (sessionid != null) {
            DBCursor cursor = sessions.find(new BasicDBObject("sessionid", sessionid));
            List<DBObject> result = cursor.toArray();
            cursor.close();
            if (result.size() == 1) {
                session = result.get(0);
            }
            if (session != null) {
                if (((Date) session.get("expires")).getTime() <= new Date().getTime()) {
                    sessions.remove(new BasicDBObject().append("sessionid", sessionid));
                    session = null;
                }
            }
        }
        if (session == null) {
            do {
                sessionid = new BigInteger(128, srng).toString(32);
            } while (sessions.findOne(new BasicDBObject().append("sessionid", sessionid)) != null && !sessionid.equals("0"));
            session = new BasicDBObject();
            nanoSessionManager = new MongoDBBasedSessionManager(session);
            nanoSessionManager.setSessionID(sessionid);
            sessions.insert(session);
        } else {
            nanoSessionManager = new MongoDBBasedSessionManager(session);
        }
        return nanoSessionManager;
    }

    /**
     * Parse Response for sending session information to the client. Especially
     * cookies
     *
     * @param nanoSessionManager session manager
     * @param response the response
     */
    @Override
    public void parseResponse(NanoSessionManager nanoSessionManager, Response response) {
        if (!(nanoSessionManager instanceof MongoDBBasedSessionManager)) {
            return;
        }
        MongoDBBasedSessionManager mdbbsm = (MongoDBBasedSessionManager) nanoSessionManager;
        String sessionid = mdbbsm.getSessionID();
        Cookie sesscookie = new Cookie(SESSIONCOOKIEID, sessionid, mdbbasedsh_sesstimeout, TimeUnit.SECONDS, response.getRequestURL().getHost(), "/", false, true);
        mdbbsm.setExpires(new Date(new Date().getTime() + sesscookie.getMaxAge() * 1000));
        response.getCookies().add(sesscookie);
        DBCollection sessions = managers.getCollection("sessions");
        try {
            sessions.save(mdbbsm.getSession());
        } catch (Exception ex) {
        }
    }

    /**
     * Session manager cleaner method
     */
    public void cleanSesssionManagers() {
        DBCollection sessions = managers.getCollection("sessions");
        sessions.remove(new BasicDBObject("expires", new BasicDBObject("$lte", new Date())));
    }

    /**
     * closer method
     */
    public void close() {
        mongoClient.close();
    }

}
