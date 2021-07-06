/*
Programmed by:
Mohamed Alsherif - Abdallah Omar
ITI AI-pro

Java Final Project:
-------------------------------------------------------------------------------------------------
Task: 
• Build all java needed classes (POJO , DAO, web service and a tester client for the web service)
• Make a web service to get the following from the data set:
1. Read data set and convert it to dataframe or Spark RDD and display some from it.
2. Display structure and summary of the data.
3. Clean the data (null, duplications)
4. Count the jobs for each company and display that in order (What are the most demanding companies for jobs?)
5. Show step 4 in a pie chart 
6. Find out What are it the most popular job titles? 
7. Show step 6 in bar chart 
8. Find out the most popular areas?
9. Show step 8 in bar chart 
10. Print skills one by one and how many each repeated and order the output to find out the most important skills required?
11. Factorize the YearsExp feature and convert it to numbers in new col. (Bounce )
12. Apply K-means for job title and companies (Bounce )
-------------------------------------------------------------------------------------------------
Team: 
Group of three students.
-------------------------------------------------------------------------------------------------
Deliverables: 
• Each team must share with us a git hub link for a maven EE application.
• Each team must be ready to present his work on 6th of July
-------------------------------------------------------------------------------------------------
Wuzzuf jobs in Egypt data set at Kaggle
https://www.kaggle.com/omarhanyy/wuzzuf-jobs
*/

package Wuzzuf_Data;
//libraries:
import java.io.IOException;
import java.util.*;
import smile.data.DataFrame;
import smile.data.type.StructType;

public class Tester_Client 
{
    static String jobPath = "src/main/resources/data/Wuzzuf_Jobs_modified.csv";
    public static void main(String[] args) throws IOException 
    {
//1st:(Read data set and convert it to dataframe or Spark RDD and display some from it. create dataFrame)
        System.out.println("---------------------------------------------------------------");
        System.out.println(">>> 1st requirment: Read data set and convert it to dataframe or Spark RDD and display some from it");
        System.out.println("    ---------------");
        DAO_Wuzzuf_Jobs dao = new DAO_Wuzzuf_Jobs();
        System.out.println("Done");
        System.out.println("---------------------------------------------------------------");
        DataFrame jobData = dao.readCSV(jobPath);
//2nd:(Display structure and summary of the data)
        System.out.println(">>> 2nd requirment: Display structure and summary of the data");
        System.out.println("    ---------------"); 
        System.out.println("Data Structure-----------------------------");
        System.out.println(jobData.structure ()); 
        StructType schema = jobData.schema();                    
        System.out.println("Data Summary-----------------------------");
        System.out.println(jobData.summary());      
        System.out.println("Sample of DataFrame---------------------");
        System.out.println(jobData.toString(5));
        System.out.println("---------------------------------------------------------------");
//3rd: (Clean the data (null, duplications))      
        // Cleanning using external Software
        System.out.println(">>> 3rd requirment: Cleanning using external Software");
        System.out.println("    ---------------"); 
        System.out.println("---------------------------------------------------------------");
//4th: (Count the jobs for each company and display that in order(What are the most demanding companies for jobs?))
        System.out.println(">>> 4th requirment: What are the most demanding companies for jobs?");
        System.out.println("    ---------------"); 
        Map<String, Long> demandingCompanies =dao.demandingCompanies();
        demandingCompanies.forEach((l,b)->System.out.println(l+" company has " + b + " available jobs."));
        System.out.println("---------------------------------------------------------------");
//5th: (Show the most demanding companies for jobs in a pie chart)
        System.out.println(">>> 5th requirment: Show the most demanding companies for jobs in a pie chart");
        System.out.println("    ---------------");
        System.out.println("Done");
        dao.graphPieChart(demandingCompanies,"The most demanding companies for jobs");
        System.out.println("---------------------------------------------------------------");  
//6th: (Find the most popular job titles?)
        System.out.println(">>> 6th requirment: Find the most popular job titles?");
        System.out.println("    ---------------");
        Map<String, Long> popularJobTitles = dao.popularJobTitle();
        popularJobTitles.forEach((l,b)->System.out.println(l+" is repeated "+ b +" times "));
        System.out.println("---------------------------------------------------------------");
//7th: (Show the most popular job titles in bar chart)
        System.out.println(">>> 7th requirment: Show the most popular job titles in bar chart");
        System.out.println("    ---------------");
        System.out.println("Done");
        dao.graphBarChart(popularJobTitles,"The most popular job titles","job titles","count");
        System.out.println("---------------------------------------------------------------");
//8th: (Find out the most popular areas?)
        System.out.println(">>> 8th requirment: Find out the most popular areas?");
        System.out.println("    ---------------");
        Map<String, Long> popularAreas = dao.popularAreas();
        popularAreas.forEach((l,b)->System.out.println(l + " Area is repeated "+ b + " times"));
        System.out.println("---------------------------------------------------------------");        
//9th: (Show the most popular areas in bar chart)
        System.out.println(">>> 9th requirment: Show the most popular areas in bar chart");
        System.out.println("    ---------------");
        System.out.println("Done");
        dao.graphBarChart(popularAreas,"The most popular Areas","Areas","count");
        System.out.println("---------------------------------------------------------------");        
    }
}