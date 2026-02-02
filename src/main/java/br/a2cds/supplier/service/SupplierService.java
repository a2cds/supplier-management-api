package br.a2cds.supplier.service;

import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import br.a2cds.supplier.client.CepClient;
import br.a2cds.supplier.dto.response.CepResponse;
import br.a2cds.supplier.model.Supplier;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class SupplierService {
	
    @Inject
    @RestClient
    CepClient cepClient;

    /**
     * Lista fornecedores com filtro opcional por Nome ou Documento.
     */
    public List<Supplier> list(String filter) {
        if (filter != null && !filter.isBlank()) {
            String term = "%" + filter.toLowerCase() + "%";
            return Supplier.list("lower(name) like ?1 or document like ?1", term);
        }
        return Supplier.listAll();
    }

    @Transactional
    public Supplier create(Supplier supplier) {
        // Regra: Documento Único
        if (Supplier.count("document", supplier.document) > 0) {
            throw new WebApplicationException("Document (CPF/CNPJ) already exists.", Response.Status.CONFLICT);
        }

        validateRules(supplier);
        
        supplier.persist();
        return supplier;
    }

    @Transactional
    public Supplier update(Long id, Supplier data) {
        Supplier entity = Supplier.findById(id);
        if (entity == null) {
            throw new WebApplicationException("Supplier not found.", Response.Status.NOT_FOUND);
        }

        // Regra: Documento Único (exceto se for o mesmo ID)
        if (!entity.document.equals(data.document)) {
            long count = Supplier.count("document = :doc and id != :id", 
                Parameters.with("doc", data.document).and("id", id));
            
            if (count > 0) {
                throw new WebApplicationException("Document already in use by another supplier.", Response.Status.CONFLICT);
            }
        }

        // Atualiza campos
        entity.name = data.name;
        entity.document = data.document;
        entity.email = data.email;
        entity.zipCode = data.zipCode;
        entity.rg = data.rg;
        entity.birthDate = data.birthDate;

        validateRules(entity);
        
        entity.persist();
        return entity;
    }

    @Transactional
    public void delete(Long id) {
        boolean deleted = Supplier.deleteById(id);
        if (!deleted) {
            throw new WebApplicationException("Supplier not found.", Response.Status.NOT_FOUND);
        }
    }

    /**
     * Validações centrais:
     * 1. CEP válido
     * 2. Dados obrigatórios para Pessoa Física (CPF)
     */
    private void validateRules(Supplier supplier) {
        // Validação de CEP
        try {
            CepResponse res = cepClient.consultarCep(supplier.zipCode, "application/json");
            if (res == null || res.uf() == null) {
                throw new WebApplicationException("Invalid Zip Code (CEP).", Response.Status.BAD_REQUEST);
            }
        } catch (Exception e) {
            // Caso a API esteja fora ou dê erro 404
            throw new WebApplicationException("Error validating Zip Code: " + e.getMessage(), Response.Status.BAD_REQUEST);
        }

        // Regra: Se for Pessoa Física (CPF 11 dígitos), RG e Data Nascimento são obrigatórios
        if (supplier.isIndividual()) {
            if (supplier.rg == null || supplier.rg.isBlank()) {
                throw new WebApplicationException("RG is required for individuals.", Response.Status.BAD_REQUEST);
            }
            if (supplier.birthDate == null) {
                throw new WebApplicationException("Birth Date is required for individuals.", Response.Status.BAD_REQUEST);
            }
        }
    }

}
