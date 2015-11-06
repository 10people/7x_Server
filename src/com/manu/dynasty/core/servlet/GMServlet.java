package com.manu.dynasty.core.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qx.gm.GMSwitch;
import com.qx.util.JsonUtils;

/**
 * Servlet implementation class GMServlet
 */
public class GMServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public static Logger logger = LoggerFactory.getLogger(GMServlet.class);

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public GMServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		ServletInputStream is = request.getInputStream();
		BufferedReader bReader = new BufferedReader(new InputStreamReader(is));
		StringBuffer buffer = new StringBuffer();
		do {
			String line = bReader.readLine();
			if (line == null)
				break;
			buffer.append(line);
		} while (true);
		is.close();
		if(buffer.length()<=0){
			logger.info("gm received : null");
		}else{
			logger.info("gm received : " + buffer.toString().substring(5));
			GMSwitch.getInstance().route(buffer.toString().substring(5),
					response.getWriter());// 去掉前面的"data="5个字符
		}
	}

	/**
	 * @Title: write
	 * @Description: 回发消息
	 * @param response
	 * @param writer
	 * @return void
	 * @throws
	 */
	public static void write(Object response, PrintWriter writer) {
		String result = null;
		if (response instanceof String) {// 直接发送json串
			result = (String) response;
		} else {
			result = JsonUtils.objectToStr(response);
		}
		logger.info("gm sent : " + result);
		writer.write(result);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
