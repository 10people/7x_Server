package com.manu.dynasty.admin;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qx.persistent.HibernateUtil;

/**
 * Servlet implementation class LoginServlet
 */
public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public static Logger log = LoggerFactory.getLogger(LoginServlet.class);

	public LoginServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	protected void service(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String action = request.getParameter("action");
		if (action == null) {
			log.info("action null");
			return;
		}
		if (action.equals("login")) {
			login(request, response);
		}
	}

	/**
	 * 登录
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	private void login(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String name = request.getParameter("name");
		String pwd = request.getParameter("pwd");
		if (null != name && null != pwd) {
			name = name.replace("'", "/");
			pwd = pwd.replace("'", "/");
			String hql = "where name='" + name + "' and pwd='" + pwd + "'";
			Admin admin = HibernateUtil.find(Admin.class, hql);
			if (null != admin) {
				admin.setPredate(new Date());
				HibernateUtil.save(admin);
				request.getSession().setMaxInactiveInterval(14400);
				request.getSession().setAttribute("admin", admin);
			} else {
				// 跳回登录
				response.sendRedirect("login.jsp?failed=1");
			}
		}
		// 跳到主页
		response.sendRedirect("main.jsp");
	}
}
