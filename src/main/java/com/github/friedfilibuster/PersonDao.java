package com.github.friedfilibuster;

import com.google.appengine.api.datastore.*;

import java.util.List;

/**
 * Created by Jesse on 5/27/2017.
 */
public class PersonDao {
    private final DatastoreService datastore = DatastoreServiceFactory
            .getDatastoreService();

    public Entity getOrCreatePerson(Key groupKey, String personId) {
        Key personKey = KeyFactory.createKey(groupKey, "Person", personId);
        Entity person;
        try {
            person = datastore.get(personKey);
        } catch (EntityNotFoundException e) {
            person = new Entity("Person", personId, groupKey);
            person.setProperty("status", "unknown");
            datastore.put(person);
        }
        return person;
    }

    public void save(Entity person) {
        datastore.put(person);
    }

    public int countOtherNotAwayUsers(Entity notThisPerson) {
        final Key groupKey = notThisPerson.getParent();
        final Key notThisPersonKey = notThisPerson.getKey();

        Query.FilterPredicate notThisUserFilter = new Query.FilterPredicate(Entity.KEY_RESERVED_PROPERTY,
                Query.FilterOperator.NOT_EQUAL, notThisPersonKey);
        Query.FilterPredicate statusIsNotAway = new Query.FilterPredicate("status", Query.FilterOperator.EQUAL,
                "notaway");
        Query q = new Query("Person").setAncestor(groupKey).setFilter(
                Query.CompositeFilterOperator.and(//
                        notThisUserFilter, //
                        statusIsNotAway));

        PreparedQuery ps = datastore.prepare(q);
        List<Entity> otherNotAwayPeople = ps.asList(FetchOptions.Builder.withDefaults());
        return otherNotAwayPeople.size();
    }
}
