package org.acme.data.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.data.entity.Contact;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class ContactRepository implements PanacheRepository<Contact> {

    public List<Contact> search(String searchTerm) {
        return this.stream("""
                select c from Contact c
                  where lower(c.firstName) like lower(concat('%', :searchTerm, '%'))
                    or lower(c.lastName) like lower(concat('%', :searchTerm, '%'))
                """, Map.of("searchTerm", searchTerm)).toList();
    }
}
