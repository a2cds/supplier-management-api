package br.a2cds.supplier.model;

import java.util.HashSet;
import java.util.Set;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.validation.constraints.NotBlank;

@Entity
public class Company extends PanacheEntity {
	
    @Column(unique = true, nullable = false)
    @NotBlank
    public String document; // CNPJ

    @NotBlank
    public String tradeName; // Nome Fantasia

    @NotBlank
    public String zipCode;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "company_supplier",
        joinColumns = @JoinColumn(name = "company_id"),
        inverseJoinColumns = @JoinColumn(name = "supplier_id")
    )
    public Set<Supplier> suppliers = new HashSet<>();

}
