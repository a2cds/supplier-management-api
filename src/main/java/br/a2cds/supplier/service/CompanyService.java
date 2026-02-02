package br.a2cds.supplier.service;

import java.time.LocalDate;
import java.time.Period;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import br.a2cds.supplier.client.CepClient;
import br.a2cds.supplier.dto.response.CepResponse;
import br.a2cds.supplier.model.Company;
import br.a2cds.supplier.model.Supplier;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;

@ApplicationScoped
public class CompanyService {
	
    @Inject
    @RestClient
    CepClient cepClient;

    @Transactional
    public Company save(Company company) {
        validateZipCode(company.zipCode);
        company.persist();
        return company;
    }

    @Transactional
    public void addSupplier(Long companyId, Long supplierId) {
        Company company = Company.findById(companyId);
        Supplier supplier = Supplier.findById(supplierId);

        if (company == null || supplier == null) {
            throw new WebApplicationException("Entity not found", 404);
        }

        validateParanaRule(company, supplier);

        company.suppliers.add(supplier);
        supplier.companies.add(company);
    }

    private void validateZipCode(String zipCode) {
        try {
            CepResponse res = cepClient.consultarCep(zipCode, "application/json");
            if (res == null || res.uf() == null) throw new IllegalArgumentException("Invalid Zip Code");
        } catch (Exception e) {
            throw new WebApplicationException("Zip Code validation error: " + e.getMessage(), 400);
        }
    }

    private void validateParanaRule(Company company, Supplier supplier) {
        if (supplier.isIndividual()) {
            CepResponse companyAddress = cepClient.consultarCep(company.zipCode, "application/json");
            
            // Regra: Se empresa do PR, fornecedor PF deve ser maior de idade
            if ("PR".equalsIgnoreCase(companyAddress.uf())) {
                if (supplier.birthDate == null) {
                     throw new WebApplicationException("Birth date is required for individuals", 400);
                }
                int age = Period.between(supplier.birthDate, LocalDate.now()).getYears();
                if (age < 18) {
                    throw new WebApplicationException("Companies from Paraná cannot hire underage suppliers.", 400);
                }
            }
        }
    }	

}
