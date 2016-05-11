package com.liferoles.rest.JSON.objects;

public class IdResponse {
	private Long id;
	
	public IdResponse(){}

	public IdResponse(long id){
		this.id = id;
	}
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
}
