package br.a2cds.supplier.client;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import br.a2cds.supplier.dto.response.CepResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@RegisterRestClient(configKey = "cep-api")
public interface CepClient {

    /**
     * Consulta o CEP na API externa.
     * É necessário enviar o Header 'Accept: application/json' pois o cep.la
     * retorna HTML por padrão se não for especificado.
     *
     * @param cep O CEP a ser consultado
     * @param accept O tipo de conteúdo esperado (application/json)
     * @return Dados do endereço
     */
    @GET
    @Path("/{cep}")
    @Produces(MediaType.APPLICATION_JSON)
    CepResponse consultarCep(@PathParam("cep") String cep, @HeaderParam("Accept") String accept);
	
}
