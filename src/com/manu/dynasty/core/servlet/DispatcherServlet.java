package com.manu.dynasty.core.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DispatcherServlet extends org.springframework.web.servlet.DispatcherServlet{
	public void doDispatch(final HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			super.doDispatch(request, response);
		}catch (Exception e) {
			e.printStackTrace();
		}finally{
			
		}	
	}
}
