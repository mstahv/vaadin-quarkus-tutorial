package org.acme.data.repository;


import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.data.entity.Status;

@ApplicationScoped
public class StatusRepository implements PanacheRepository<Status> {

}
