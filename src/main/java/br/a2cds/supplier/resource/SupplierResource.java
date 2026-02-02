package br.a2cds.supplier.resource;

import java.net.URI;
import java.util.List;

import br.a2cds.supplier.model.Supplier;
import br.a2cds.supplier.service.SupplierService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

@Path("/v1/suppliers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SupplierResource {
	
    @Inject
    SupplierService service;

    /**
     * Listar fornecedores.
     * Suporta filtro por Nome ou Documento via query param '?filter=valor'
     */
    @GET
    public List<Supplier> list(@QueryParam("filter") String filter) {
        return service.list(filter);
    }

    /**
     * Buscar por ID.
     */
    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        return Supplier.findByIdOptional(id)
                .map(supplier -> Response.ok(supplier).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    /**
     * Criar novo fornecedor.
     */
    @POST
    public Response create(Supplier supplier) {
        Supplier created = service.create(supplier);
        
        URI uri = UriBuilder.fromResource(SupplierResource.class)
                .path(Long.toString(created.id))
                .build();
        
        return Response.created(uri).entity(created).build();
    }

    /**
     * Atualizar fornecedor existente.
     */
    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Long id, Supplier supplier) {
        Supplier updated = service.update(id, supplier);
        return Response.ok(updated).build();
    }

    /**
     * Remover fornecedor.
     */
    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        service.delete(id);
        return Response.noContent().build();
    }

}
