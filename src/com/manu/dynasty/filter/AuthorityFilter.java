package com.manu.dynasty.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

//import com.youxigu.cache.CacheFacade;

public class AuthorityFilter implements Filter {
	public String encoding = null;
	public FilterConfig filterConfig = null;
	public boolean ignore = true;
	public static FilterMgr filterMgr = new FilterMgr();
	
	public void destroy() {
		encoding = null;
		filterConfig = null;
	}

	public void doFilter(ServletRequest request, ServletResponse response,FilterChain chain) throws IOException, ServletException {
		filterMgr.doFilter4Authority(request, response, chain, ignore, selectEncoding(request));
	}

	public void init(FilterConfig filterConfig) throws ServletException {
		this.filterConfig = filterConfig;
		this.encoding = filterConfig.getInitParameter("encoding");
		String value = filterConfig.getInitParameter("ignore");
		if (value == null)
			this.ignore = true;
		else if (value.equalsIgnoreCase("true"))
			this.ignore = true;
		else if (value.equalsIgnoreCase("yes"))
			this.ignore = true;
		else
			this.ignore = false;
	}

	public String selectEncoding(ServletRequest request) {
		return encoding;
	}
}