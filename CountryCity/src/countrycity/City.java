package countrycity;

public class City {
    private String code;
    private String name;
    private String continent;
    private float area;
    private int population;
    private String country_code;

    public City(String code, String name, String continent, float area, int population, String country_code) {
        this.code = code;
        this.name = name;
        this.continent = continent;
        this.area = area;
        this.population = population;
        this.country_code = country_code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) 
    {
        this.name = name;
    }

    public String getContinent() 
    {
        return continent;
    }

    public void setContinent(String continent) 
    {
        this.continent = continent;
    }

    public float getArea() 
    {
        return area;
    }

    public void setArea(float area) 
    {
        this.area = area;
    }

    public int getPopulation() 
    {
        return population;
    }

    public void setPopulation(int population) 
    {
        this.population = population;
    }

    public String getCountry_code() 
    {
        return country_code;
    }

    public void setCountry_code(String country_code) 
    {
        this.country_code = country_code;
    }
}
