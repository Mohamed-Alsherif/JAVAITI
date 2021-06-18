package countrycity;

import java.util.ArrayList;

public class Country 
{
    private String country_code;
    private String country_name;
    private ArrayList<City> city_list;

    public Country(String country_name,String country_code, ArrayList<City> city_list) {
        this.country_code = country_code;
        this.city_list = city_list;
        this.country_name=country_name;
    }

    public String getCountry_code() 
    {
        return country_code;
    }

    public void setCountry_code(String country_code) 
    {
        this.country_code = country_code;
    }

    public ArrayList<City> getCity_list() 
    {
        return city_list;
    }

    public void setCity_list(ArrayList<City> city_list) 
    {
        this.city_list = city_list;
    }

    public String getCountry_name() 
    {
        return country_name;
    }

    public void setCountry_name(String country_name) 
    {
        this.country_name = country_name;
    }
}
