package br.a2cds.supplier.dto.response;

public record CepResponse(
		String cep,
		String uf,
		String cidade,
		String bairro,
		String logradouro
		) {}
