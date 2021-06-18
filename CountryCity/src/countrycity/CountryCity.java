package countrycity;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CountryCity 
{
    public static void main(String[] args) {
        CountryDAO countryDAO = new CountryDAO();
        ArrayList<Country> countriesDataList = countryDAO.getCountriesData();
        countriesDataList.forEach(country -> {
            String country_code = country.getCountry_code();
            String country_name = country.getCountry_name();
            ArrayList<City> city_list = country.getCity_list();
            System.out.println(String.format("code country : %1$s, country : %2$s", country_code, country_name));
            city_list.forEach(city -> {
                System.out.println(String.format("code city : %1$s, city : %2$s", city.getCode(), city.getName()));
            });
            System.out.println(" ");
        });
        
        ArrayList<City> countryCitiesOrdered = countryDAO.getCountryCitiesOrdered("50010");
        countryCitiesOrdered.forEach(city->{
            System.out.println(city.getName()+" , "+city.getPopulation());
        });
    }
}
