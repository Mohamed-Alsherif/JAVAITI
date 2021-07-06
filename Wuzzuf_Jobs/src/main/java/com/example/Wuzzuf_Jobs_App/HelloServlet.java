package com.example.Wuzzuf_Jobs_App;

import Wuzzuf_Data.DAO_Wuzzuf_Jobs;
import Wuzzuf_Data.Tester_Client;
import Wuzzuf_Data.Wuzzuf_Jobs;
import java.io.*;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import javax.servlet.annotation.*;

@WebServlet(name = "helloServlet", value = "/hello-servlet")
public class HelloServlet extends HttpServlet 
{
    private String message;

    public void init() 
    {
        message = "Hello World!";
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException 
    {
        response.setContentType("text/html");
        // Hello
        PrintWriter out = response.getWriter();
        out.println("<html><body>");
        out.println("<h1>" + message + "</h1>");
        out.println("</body></html>");
        Tester_Client test = new Tester_Client();
        out.println(test);
        
//        return test;
    }

    
// still under construction    
    public class PersonServlet extends HttpServlet 
    {

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
        {	
		String requestUrl = request.getRequestURI();
		String name = requestUrl.substring("/people/".length());
                Tester_Client test = new Tester_Client();
        	response.getOutputStream().println("{}");
	}
    }

	@Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException 
    {
		String title = request.getParameter("title");
		String company = request.getParameter("company");
		String location = request.getParameter("location");
		String type = request.getParameter("type");
		String level = request.getParameter("level");
		String yearsExp = request.getParameter("yearsExp");
		String country = request.getParameter("country");
		String skills = request.getParameter("skills");
//                DAO_Wuzzuf_Jobs.getInstance(new Wuzzuf_Jobs(title,company,location,type,level,yearsExp,country,skills));
    }    
}