package org.acme.data.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.acme.data.entity.Company;
import org.acme.data.entity.Contact;
import org.acme.data.entity.Status;
import org.acme.data.repository.CompanyRepository;
import org.acme.data.repository.ContactRepository;
import org.acme.data.repository.StatusRepository;

import java.util.List;

@ApplicationScoped
public class CrmService {

    private final ContactRepository contactRepository;
    private final CompanyRepository companyRepository;
    private final StatusRepository statusRepository;

    public CrmService(ContactRepository contactRepository,
                      CompanyRepository companyRepository,
                      StatusRepository statusRepository) { 
        this.contactRepository = contactRepository;
        this.companyRepository = companyRepository;
        this.statusRepository = statusRepository;
    }

    public List<Contact> findAllContacts(String stringFilter) {
        if (stringFilter == null || stringFilter.isEmpty()) {
            return contactRepository.findAll().list();
        } else {
            return contactRepository.search(stringFilter);
        }
    }

    public long countContacts() {
        return contactRepository.count();
    }

    @Transactional
    public void deleteContact(Contact contact) {
        contactRepository.delete(contact);
    }

    @Transactional
    public void saveContact(Contact contact) {
        if (contact == null) { 
            System.err.println("Contact is null. Are you sure you have connected your form to the application?");
            return;
        }
        contactRepository.getEntityManager().merge(contact);
    }

    public List<Company> findAllCompanies() {
        return companyRepository.findAll().list();
    }

    public List<Status> findAllStatuses(){
        return statusRepository.findAll().list();
    }
}