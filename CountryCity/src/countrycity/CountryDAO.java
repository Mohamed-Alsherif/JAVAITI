package countrycity;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class CountryDAO {
 
    public ArrayList<Country> getCountriesData(){
        
        Map<String, ArrayList<City>> countries_cities_map = new HashMap<>();
        ArrayList<Country> countries_list = new ArrayList<>();
        FileReader city_fr = null;
        BufferedReader city_br = null;
        FileReader country_fr = null;
        BufferedReader country_br = null;
        try {

            city_fr = new FileReader("city.csv");
            city_br = new BufferedReader(city_fr);
            String row;
            while ((row = city_br.readLine()) != null) {
                String[] city_data = row.split(",");
                String city_name = city_data[0];
                if (!"City name".equals(city_name)) {
                    String city_code = city_data[1];
                    String city_continent = city_data[2];
                    float city_area = Float.parseFloat(city_data[3]);
                    int city_population = Integer.parseInt(city_data[4]);
                    String city_country_code = city_data[5];
                    City currentCity = new City(city_code, city_name, city_continent, city_area, city_population, city_country_code);
                    if (countries_cities_map.containsKey(city_country_code)) {
                        countries_cities_map.get(city_country_code).add(currentCity);
                    } else {
                        ArrayList<City> city_list = new ArrayList<>();
                        city_list.add(currentCity);
                        countries_cities_map.put(city_country_code, city_list);
                    }
                }
            }

            country_fr = new FileReader("country.csv");
            country_br = new BufferedReader(country_fr);
            String row1;
            while ((row1 = country_br.readLine()) != null) {
                String[] country_data = row1.split(",");
                String country_name = country_data[0];
                if (!"Country name".equals(country_name)) {
                    String country_code = country_data[1];
                    ArrayList<City> country_cities_list;
                    if (countries_cities_map.containsKey(country_code)) {
                        country_cities_list = countries_cities_map.get(country_code);
                    } else {
                        country_cities_list = new ArrayList<>();
                    }
                    Country currentCountry = new Country(country_name, country_code, country_cities_list);
                    countries_list.add(currentCountry);
                }
            }
            

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                city_br.close();
                city_fr.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return countries_list;
    }
    public Map<String, ArrayList<City>> getCountriesMap(){
        
        Map<String, ArrayList<City>> countries_cities_map = new HashMap<>();
        ArrayList<Country> countries_list = new ArrayList<>();
        // TODO code application logic here
        FileReader city_fr = null;
        BufferedReader city_br = null;
        FileReader country_fr = null;
        BufferedReader country_br = null;
        try {

            city_fr = new FileReader("city.csv");
            city_br = new BufferedReader(city_fr);
            String row;
            while ((row = city_br.readLine()) != null) {
                String[] city_data = row.split(",");
                String city_name = city_data[0];
                if (!"City name".equals(city_name)) {
                    String city_code = city_data[1];
                    String city_continent = city_data[2];
                    float city_area = Float.parseFloat(city_data[3]);
                    int city_population = Integer.parseInt(city_data[4]);
                    String city_country_code = city_data[5];
                    City currentCity = new City(city_code, city_name, city_continent, city_area, city_population, city_country_code);
                    if (countries_cities_map.containsKey(city_country_code)) {
                        countries_cities_map.get(city_country_code).add(currentCity);
                    } else {
                        ArrayList<City> city_list = new ArrayList<>();
                        city_list.add(currentCity);
                        countries_cities_map.put(city_country_code, city_list);
                    }
                }
            }

            country_fr = new FileReader("country.csv");
            country_br = new BufferedReader(country_fr);
            String row1;
            while ((row1 = country_br.readLine()) != null) {
                String[] country_data = row1.split(",");
                String country_name = country_data[0];
                if (!"Country name".equals(country_name)) {
                    String country_code = country_data[1];
                    ArrayList<City> country_cities_list;
                    if (countries_cities_map.containsKey(country_code)) {
                        country_cities_list = countries_cities_map.get(country_code);
                    } else {
                        country_cities_list = new ArrayList<>();
                    }
                    Country currentCountry = new Country(country_name, country_code, country_cities_list);
                    countries_list.add(currentCountry);
                }
            }
            

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                city_br.close();
                city_fr.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return countries_cities_map;
    }
    public ArrayList<City>getCountryCitiesOrdered(String country_code){
        Map<String, ArrayList<City>> countriesMap = getCountriesMap();
        ArrayList<City> cityList = countriesMap.get(country_code);
        Map<Integer,City>sortedCities=new HashMap<>();
        cityList.forEach(city->{
            int population = city.getPopulation();
            sortedCities.put(population, city);
        });
        
        Map<Integer, City> treeMap = new TreeMap<Integer, City>(
                new Comparator<Integer>() {

                    @Override
                    public int compare(Integer o1, Integer o2) {
                        return o2.compareTo(o1);
                    }

                });

        treeMap.putAll(sortedCities);

        ArrayList<City> orderedCoties = new ArrayList<>( treeMap.values());
        return orderedCoties;
    }
}