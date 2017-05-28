package com.github.friedfilibuster;

import com.google.appengine.api.datastore.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Created by Jesse on 5/27/2017.
 */
public class ThermostatService extends HttpServlet {
    private static final Logger logger = Logger.getLogger(ThermostatService.class.getName());
    private static final Pattern COMMA_PATTERN = Pattern.compile(",");

    private final GroupDao groupDao = new GroupDao();
    private final PersonDao personDao = new PersonDao();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String body = readBody(req);
        logger.info("Body: " + body);

        String[] pieces = COMMA_PATTERN.split(",");
        if (pieces.length == 3) {
            String groupId = pieces[0];
            String personId = pieces[1];
            String action = pieces[2];

            Entity group = groupDao.getOrCreateGroup(groupId);
            Entity person = personDao.getOrCreatePerson(group.getKey(), personId);

            if ("entered".equals(action)) {
                person.setProperty("status", "notaway");
                personDao.save(person);
                // set thermostat to home
                resp.getOutputStream().println("Set home");
            } else if ("exited".equals(action)) {
                person.setProperty("status", "away");
                personDao.save(person);

                if (personDao.countOtherNotAwayUsers(person) == 0) {
                    // set away
                    resp.getOutputStream().println("Set away");
                } else {
                    resp.getOutputStream().println("Others are not away");
                }
            } else {
                resp.getOutputStream().println("Unrecognized action: " + action);
            }
        } else {
            resp.getOutputStream().println("Unrecognized input: " + body);
        }
    }

    private String readBody(HttpServletRequest req) throws IOException {
        StringBuilder body = new StringBuilder();
        BufferedReader reader = req.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            body.append(line);
        }
        return body.toString();
    }
}
