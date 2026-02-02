package br.a2cds.supplier.resource;

import java.net.URI;
import java.util.List;

import br.a2cds.supplier.model.Company;
import br.a2cds.supplier.service.CompanyService;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

@Path("/v1/companies")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CompanyResource {
	
    @Inject
    CompanyService service;

    /**
     * Listar todas as empresas.
     * Método simplificado usando Active Record do Panache.
     */
    @GET
    public List<Company> listAll() {
        return Company.listAll();
    }

    /**
     * Buscar empresa por ID.
     */
    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        return Company.findByIdOptional(id)
                .map(company -> Response.ok(company).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    /**
     * Criar nova empresa.
     * Delega para o serviço para validar CEP e regras.
     */
    @POST
    public Response create(Company company) {
        // Garante que o ID é nulo para criar um novo registro
        company.id = null;
        
        Company savedCompany = service.save(company);
        
        URI uri = UriBuilder.fromResource(CompanyResource.class)
                .path(Long.toString(savedCompany.id))
                .build();
        
        return Response.created(uri).entity(savedCompany).build();
    }

    /**
     * Atualizar empresa existente.
     */
    @PUT
    @Path("/{id}")
    @Transactional
    public Response update(@PathParam("id") Long id, Company companyData) {
        Company entity = Company.findById(id);
        if (entity == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Atualiza os campos
        entity.tradeName = companyData.tradeName;
        entity.document = companyData.document;
        
        // Se o CEP mudou, precisamos revalidar e salvar via serviço
        if (!entity.zipCode.equals(companyData.zipCode)) {
            entity.zipCode = companyData.zipCode;
            service.save(entity); // Valida novo CEP
        } else {
            entity.persist(); // Apenas persiste alterações simples
        }

        return Response.ok(entity).build();
    }

    /**
     * Excluir empresa.
     */
    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") Long id) {
        boolean deleted = Company.deleteById(id);
        return deleted 
            ? Response.noContent().build() 
            : Response.status(Response.Status.NOT_FOUND).build();
    }

    /**
     * Associar um fornecedor a uma empresa.
     * Endpoint específico para lidar com o relacionamento ManyToMany.
     * É aqui que a regra do "Paraná vs Menor de Idade" será disparada pelo Service.
     */
    @POST
    @Path("/{id}/suppliers/{supplierId}")
    public Response addSupplier(@PathParam("id") Long companyId, @PathParam("supplierId") Long supplierId) {
        try {
            service.addSupplier(companyId, supplierId);
            return Response.noContent().build();
        } catch (IllegalArgumentException e) {
            // Retorna 404 se um dos IDs não existir
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (WebApplicationException e) {
            // Retorna o erro específico de negócio (Ex: Regra do Paraná - 400 Bad Request)
            return Response.status(e.getResponse().getStatus())
                           .entity(new ErrorResponse(e.getMessage()))
                           .build();
        }
    }

    // Record interno simples para padronizar erros JSON caso não use um ExceptionMapper global
    public record ErrorResponse(String message) {}

}
