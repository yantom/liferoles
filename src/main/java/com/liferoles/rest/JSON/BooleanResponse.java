package com.liferoles.rest.JSON;

public class BooleanResponse {
	private boolean response;

	public BooleanResponse(){};
	public BooleanResponse(boolean response){
		this.response = response;
	}
	public boolean isResponse() {
		return response;
	}

	public void setResponse(boolean response) {
		this.response = response;
	}

}
