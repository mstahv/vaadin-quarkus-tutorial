package org.acme.data.repository;


import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.data.entity.Company;

@ApplicationScoped
public class CompanyRepository implements PanacheRepository<Company> {

}
