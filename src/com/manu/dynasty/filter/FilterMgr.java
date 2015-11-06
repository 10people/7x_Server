package com.manu.dynasty.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.manu.dynasty.admin.Admin;

/**
 * Servlet Filter implementation class FilterMgr
 */
public class FilterMgr {
	public static List<String> exclusion;
	
	public FilterMgr(){
		initData();
	}
	
	public void initData(){
		exclusion = new ArrayList<String>();
		exclusion.add("login.jsp");// 登录jsp
		exclusion.add("login");// 登录处理
		exclusion.add("gm");// gm管理
	}
	
	/** 
	 * @Title: isExclusion 
	 * @Description: 是否属于例外
	 * @return
	 * @return boolean
	 * @throws 
	 */
	public boolean isExclusion(String url){
		for (String str: exclusion) {
			if(url.endsWith(str)){
				return true;
			}
		}
		return false;
	}
	
	public void doFilter4Authority(ServletRequest request, ServletResponse response,FilterChain chain,boolean ignore,String encoding) throws IOException, ServletException {
		try {
		    if ((ignore) || (request.getCharacterEncoding() == null)) {
				if (encoding != null) {
					request.setCharacterEncoding(encoding);
					response.setCharacterEncoding("UTF-8");
				}
		    }
		    chain.doFilter(request, response);
		}catch (Exception e) {
			e.printStackTrace();
		}
	    	
	}
	
	public void doFilter4Security(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		HttpSession session = request.getSession(true);
		// 从session里取的用户信息
		Admin admin = (Admin) session.getAttribute("admin");
		String uri = ((HttpServletRequest) request).getRequestURI();
		// 判断如果没有取到用户信息,就跳转到登陆页面
		if (admin != null || isExclusion(uri)) {
			chain.doFilter(request, response);
		} else {
			// 跳转到登陆页面
			response.sendRedirect("login.jsp");
		}
	}
}
