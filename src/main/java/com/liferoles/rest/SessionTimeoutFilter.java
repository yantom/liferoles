package com.liferoles.rest;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class SessionTimeoutFilter implements Filter {
	
	public void init(FilterConfig filterConfig) throws ServletException { 
    }
	
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException{
		HttpServletRequest hsr = (HttpServletRequest)request;
		//if not jwt request
		if((hsr.getHeader("Authorization") == null)){
			HttpSession session = hsr.getSession(false);
			if(session != null && !session.isNew()) {
				System.out.println("notout");
			    chain.doFilter(request, response);
			} else {
				System.out.println("yesout");
			    ((HttpServletResponse)response).setStatus(401);
			}
		}
	}
	
	public void destroy() {
    }
}
