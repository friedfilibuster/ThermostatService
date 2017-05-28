package com.github.friedfilibuster;

import com.google.appengine.api.datastore.*;

/**
 * Created by Jesse on 5/27/2017.
 */
public class GroupDao {
    private final DatastoreService datastore = DatastoreServiceFactory
            .getDatastoreService();

    public Entity getOrCreateGroup(String groupId) {
        Key groupKey = KeyFactory.createKey("Group", groupId);
        Entity group;
        try {
            group = datastore.get(groupKey);
        } catch (EntityNotFoundException e) {
            group = new Entity("Group", groupId);
            datastore.put(group);
        }
        return group;
    }
}
