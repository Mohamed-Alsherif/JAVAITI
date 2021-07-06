package com.example.Wuzzuf_Jobs_App;

import Wuzzuf_Data.Tester_Client;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/Wuzzuf_Data")
public class HelloResource 
{
    @GET
    @Produces("text/plain")
    public Tester_Client hello() 
    {
        Tester_Client test = new Tester_Client();
        return test;
    }

}