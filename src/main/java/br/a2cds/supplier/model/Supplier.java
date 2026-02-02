package br.a2cds.supplier.model;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Entity
public class Supplier extends PanacheEntity {

    @Column(unique = true, nullable = false)
    @NotBlank
    public String document; // CPF or CNPJ

    @NotBlank
    public String name;

    @Email
    public String email;

    @NotBlank
    public String zipCode; // CEP

    // Fields for Individual (PF)
    public String rg;
    public LocalDate birthDate;

    @ManyToMany(mappedBy = "suppliers")
    public Set<Company> companies = new HashSet<>();

    public boolean isIndividual() {
        // Regra: CPF tem 11 dígitos
        return document != null && document.replaceAll("\\D", "").length() == 11;
    }
	
}
