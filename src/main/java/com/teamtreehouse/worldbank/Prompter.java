package com.teamtreehouse.worldbank;

import com.teamtreehouse.worldbank.model.Country;
import com.teamtreehouse.worldbank.model.Country.CountryBuilder;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class Prompter {

    // Instantiate menu, reader object, and session factory
    private Map<String, String> menu = new TreeMap<>();
    private BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    private static final SessionFactory sessionFactory = buildSessionFactory();
    private String regexDecimal = "^-?\\d*\\.\\d+$";
    private String regexInteger = "^-?\\d+$";
    private String regexDouble = regexDecimal + "|" + regexInteger;
    // Build Session Factory
    private static SessionFactory buildSessionFactory() {
        final StandardServiceRegistry registry = new StandardServiceRegistryBuilder().configure().build();
        return new MetadataSources(registry).buildMetadata().buildSessionFactory();
    }


    Prompter() {
        // Initialize Menu
        menu.put("1. Data Table", "View Data Table");
        menu.put("2. Statistics", "View Statistics");
        menu.put("3. Add Country", "Add a Country to the Database");
        menu.put("4. Edit Country", "Edit Country Data");
        menu.put("5. Delete Country", "Delete a Country from the Database");
        menu.put("6. Exit", "Exit");
    }

    void run() {
        int choice;
        boolean keepGoing = true;

        // Display menu and prompt for choice
        System.out.println("Welcome! Here are your menu options:");
        try {
            do {
                choice = promptAction(menu);
                switch (choice) {
                    case 1:
                        System.out.println("\nYou chose to see all country data:");
                        showCountryData();
                        System.out.println("\nWhat would you like to do now:");
                        break;
                    case 2:
                        System.out.println("\nYou chose to see a list of statistics:");
                        showStatistics();
                        System.out.println("\nWhat would you like to do now:");
                        break;
                    case 3:
                        System.out.println("\nYou chose to add a country:");
                        addCountry();
                        System.out.println("\nWhat would you like to do now:");
                        break;
                    case 4:
                        System.out.println("\nYou chose to update a country:");
                        updateCountry();
                        System.out.println("\nWhat would you like to do now:");
                        break;
                    case 5:
                        System.out.println("\nYou chose to delete a country:");
                        deleteCountry();
                        System.out.println("\nWhat would you like to do now:");
                        break;
                    case 6:
                        System.out.println("Have a great day!");
                        keepGoing = false;
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Not a valid choice");
                        break;
                }
            } while (keepGoing);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void showCountryData() {
        final int[] counter = {0};
        System.out.printf("%s\t%-32s\t%s\t\t%s%n",
                "Country Code", "Country Name", "Internet Users", "Adult Literacy Rate");
        System.out.println("-------------------------------------------------------------------------------------------");
        fetchAllCountries().stream().forEach(System.out::println);
    }

    private void showStatistics() {
        List<Country> noNullInternet = new ArrayList<>();
        List<Country> noNullLiteracy = new ArrayList<>();
        for (Country c : fetchAllCountries()) {
            if (c.getInternetUsers() != null) {
                noNullInternet.add(c);
            }
            if (c.getAdultLiteracyRate() != null) {
                noNullLiteracy.add(c);
            }
        }
        final Comparator<Country> userComparator = Comparator.comparingDouble(Country::getInternetUsers);
        Country mostUsers = noNullInternet.stream()
                .max(userComparator)
                .get();

        Country leastUsers = noNullInternet.stream()
                .min(userComparator)
                .get();
        System.out.printf("%nCountry with the highest number of internet users: %s\t%.2f%n", mostUsers.getName(), mostUsers.getInternetUsers().doubleValue());
        System.out.printf("Country with the lowest number of internet users: %s\t%.2f%n", leastUsers.getName(), leastUsers.getInternetUsers().doubleValue());

        final Comparator<Country> literacyComparator = Comparator.comparingDouble(Country::getAdultLiteracyRate);
        Country highestLiteracy = noNullLiteracy.stream()
                .max(literacyComparator)
                .get();

        Country lowestLiteracy = noNullLiteracy.stream()
                .min(literacyComparator)
                .get();
        System.out.printf("Country with the highest adult literacy rate: %s\t%.2f%n", highestLiteracy.getName(), highestLiteracy.getAdultLiteracyRate().doubleValue());
        System.out.printf("Country with the lowest adult literacy rate: %s\t%.2f%n", lowestLiteracy.getName(), lowestLiteracy.getAdultLiteracyRate().doubleValue());

        System.out.println("Correlation coefficient = " + calculateCoefficient());
    }

    private Country addCountry() throws IOException {
        String countryCode = setCountryCode();
        String countryName = setCountryName();
        Double internetUsers = setInternetUsers();
        Double adultLiteracy = setAdultLiteracy();

        Country country = new CountryBuilder()
                .withCode(countryCode)
                .withName(countryName)
                .withInternet(internetUsers)
                .withLiteracy(adultLiteracy)
                .build();

        // Open a session
        Session session = sessionFactory.openSession();

        // Begin a transaction
        session.beginTransaction();

        //Use the session to save country
        session.save(country);

        //Commit the transaction
        session.getTransaction().commit();

        //Close the session
        session.close();

        return country;

    }

    private void updateCountry() throws IOException {
        Set<String> updateMenu = new LinkedHashSet<>();
        boolean isValid = true;
        updateMenu.add("Code");
        updateMenu.add("Name");
        updateMenu.add("Internet");
        updateMenu.add("Literacy");
        Country country = getCountryCode();
        System.out.println("You chose " + country.getName());
        System.out.println("Your choices are: ");
        updateMenu.stream().forEach(System.out::println);
        System.out.print("\nWhat would you like to update? ");
        do {
            String choice = reader.readLine();
            switch (choice.toUpperCase()) {
                case "CODE":
                    System.out.println("You selected to update the country code for " + country.getName() + ".");
                    System.out.print("What would you like to update the country code to? ");
                    String oldCode = country.getCode();
                    String newCode = setCountryCode();
                    country.setCode(newCode);
                    update(country);
                    System.out.println("You updated the code from " + oldCode + " to " + newCode + ".");
                    break;
                case "NAME":
                    System.out.println("You selected to update the country name for " + country.getName() + ".");
                    System.out.print("What would you like to update the name to? ");
                    String oldName = country.getName();
                    String newName = setCountryName();
                    country.setName(newName);
                    update(country);
                    System.out.println("You updated the name from " + oldName + " to " + newName);
                    break;
                case "INTERNET":
                    System.out.println("You selected to update the percentage of internet users for " + country.getName() + ".");
                    System.out.print("What would you like to update the percentage of internet users to?");
                    Double oldInternet = country.getInternetUsers();
                    Double newInternet = setInternetUsers();
                    country.setInternetUsers(newInternet);
                    update(country);
                    System.out.println("You updated the percentage of internet users from " + oldInternet + " to " + newInternet);
                    break;
                case "LITERACY":
                    System.out.println("You selected to update the adult literacy rate for " + country.getName() + ".");
                    System.out.print("What would you like to update the literacy rate to? ");
                    Double oldRate = country.getAdultLiteracyRate();
                    Double newRate = setAdultLiteracy();
                    country.setAdultLiteracyRate(newRate);
                    update(country);
                    System.out.println("You updated the literacy rate from " + oldRate + " to " + newRate);
                    break;
                default:
                    System.out.println("Invalid choice");
                    System.out.print("\nWhat would you like to update? ");
                    isValid = false;
                    break;
            }
        }while(!isValid);

    }

    private void deleteCountry() throws IOException {
       Country country = getCountryCode();
       boolean isValid = false;

        System.out.println("You chose to delete " + country.getName() + " from the database.");
        System.out.print("Are you sure? (Y/N)");
        do {
            String choice = reader.readLine();
            switch (choice.toUpperCase()){
                case "Y":
                    // Open Session
                    Session session = sessionFactory.openSession();

                    //Begin Transaction5
                    session.beginTransaction();

                    //use session to delete contact
                    session.delete(country);

                    // commit
                    session.getTransaction().commit();

                    // close
                    session.close();
                    System.out.println(country.getName() + " has been deleted from database.");
                    isValid = true;
                    break;
                case "N":
                    System.out.println(country.getName() + " will not be deleted.");
                    isValid = true;
                    break;
                default:
                    System.out.println("Invalid choice.");
                    System.out.print("\nAre you sure? (Y/N)");
                    isValid = false;
                    break;
            }
        }while (!isValid);

    }

    private Country findCountryByCode(String countryCode) {
        Session session = sessionFactory.openSession();

        Country country = session.get(Country.class,countryCode);

        session.close();

        return country;
    }


    private double calculateCoefficient() {
        // Grab countries with both values to determine coorelation
        List<Country> coefficient = new ArrayList<>();
        for(Country c : fetchAllCountries()){
            if((c.getAdultLiteracyRate() != null) && (c.getInternetUsers() != null)){
                coefficient.add(c);
            }
        }

        // Find the mean of the x (Internet Users) and y (Literacy Rate) values
        double totalInternet = 0;
        double totalLiteracy = 0;
        for (Country c : coefficient){
            totalInternet += c.getInternetUsers().doubleValue();
            totalLiteracy += c.getAdultLiteracyRate().doubleValue();
        }
        double internetMean = totalInternet/(double)coefficient.size();
        double literacyMean = totalLiteracy/(double)coefficient.size();

        //Find Standard Deviations

        double [] internetMeanSquare = new double[coefficient.size()];
        int i = 0;
        // Subtract the mean from numbers and square the result
        for (Country c: coefficient) {
            internetMeanSquare[i] = Math.pow((c.getInternetUsers().doubleValue() - internetMean),2);
            i++;
        }

        double totSqrRt = 0;
        // Find mean of squared differences and take square root of result
        for(double d : internetMeanSquare){
            totSqrRt += d;
        }
        double internetStandardDeviation = Math.sqrt(totSqrRt/(double)coefficient.size());

        // repeat for y
        double []yStandardDev = new double[coefficient.size()];
        int j = 0;
        for (Country c: coefficient) {
            yStandardDev[j] = Math.pow((c.getAdultLiteracyRate().doubleValue() - literacyMean),2);
            j++;
        }

        totSqrRt = 0;
        for(double d : yStandardDev){
            totSqrRt += d;
        }
        double literacyStandardDeviation = Math.sqrt(totSqrRt/(double)coefficient.size());

        //for each n pair (x,y) take (x - xmean)(y-ymean) then add n results
        double nPairs = 0;
        for (Country c : coefficient){
            nPairs += ((c.getInternetUsers() - internetMean)*(c.getAdultLiteracyRate() - literacyMean));
        }

        // divide sum by xStandardDeviation * yStandardDeviation
        double divideNPairsByDeviation = nPairs/(internetStandardDeviation * literacyStandardDeviation);

        // divide result by n-1 (n is number of (x,y) pairs)
        return divideNPairsByDeviation/((double)coefficient.size()- 1);

    }

    private int promptAction(Map<String, String> menu) throws IOException {
        boolean valid = false;
        String choiceAsString;

        System.out.println();
        for(Map.Entry<String, String> option : menu.entrySet()){
            System.out.printf("%s - %s %n",
                    option.getKey(),
                    option.getValue());
        }
        do {
            System.out.printf("\nPlease enter your choice: (Please enter a number 1- %d) ", menu.size());
            choiceAsString = reader.readLine();

                if (!choiceAsString.matches("[-+]?\\d*\\.?\\d+")) {
                    System.out.println("Invalid entry. Not a number.");
                } else if (Integer.parseInt(choiceAsString) <= 0 || Integer.parseInt(choiceAsString) > menu.size()) {
                    System.out.println("Invalid entry. Please select a number that corresponds to a menu item.");
                } else {
                    valid = true;
                }
            }while (!valid);
        int choice = Integer.parseInt(choiceAsString);

        return choice;
    }

    private String setCountryCode() throws IOException {
        String countryCode;
        boolean validEntry = true;
        do {
            countryCode = reader.readLine().toUpperCase();
            if (countryCode.length() != 3) {
                System.out.println("Enter a 3 digit country code");
                validEntry = false;
            }
            for (Country c : fetchAllCountries()) {
                if (c.getCode().equals(countryCode)) {
                    System.out.println("Country code already exists");
                    validEntry = false;
                }
            }
        }while(!validEntry);
        return countryCode;
    }

    private String setCountryName() throws IOException {
        boolean validEntry = true;
        String countryName;
        do {
            countryName = reader.readLine();
            if (countryName.matches("[0-9]+")) {
                System.out.println("Country name should not contain a number");
                System.out.print("Enter the country name: ");
                validEntry = false;
            }
        } while (!validEntry);
        return countryName;
    }

    private Double setInternetUsers() throws IOException {
        Double internetUsers = null;
        boolean validEntry = true;
        do {
            String entry = reader.readLine();
            if ((!entry.matches(regexDouble)) && (!entry.isEmpty())) {
                System.out.println("Invalid entry. Not a double.");
                System.out.print("Enter the percentage of internet users: ");
                validEntry = false;
                continue;
            }
            if (entry.isEmpty()) {
                internetUsers = null;
            } else {
                internetUsers = Double.parseDouble(entry);
            }
        } while (!validEntry);
        return internetUsers;
    }


    private Double setAdultLiteracy() throws IOException {
        boolean validEntry = true;
        Double adultLiteracy = null;
        do {
            String entry = reader.readLine();
            if (!entry.matches(regexDouble) && (!entry.isEmpty())) {
                System.out.println("Invalid entry. Not a double.");
                System.out.print("Enter the percentage of literate adults: ");
                validEntry = false;
                continue;
            }
            if (entry.isEmpty()) {
                adultLiteracy = null;
            } else {
                adultLiteracy = Double.parseDouble(entry);
            }
        } while (!validEntry);

        return adultLiteracy;
    }


    private void update(Country country) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        session.update(country);
        session.getTransaction().commit();
        session.close();
    }

    private Country getCountryCode() throws IOException {
        String countryCode;
        boolean validEntry;
        Country country = new Country();
        do{
            System.out.print("Enter the code of the country you would like to update/delete: ");
            System.out.print("Enter the country code: ");
            countryCode = reader.readLine().toUpperCase();
            if (countryCode.length() != 3){
                System.out.println("Enter a 3 digit country code");
                validEntry = false;
            }else {
                country = findCountryByCode(countryCode);
                validEntry = true;
            }

            if(country == null) {
                System.out.println("Country not found.");
                validEntry = false;
            }
        }
        while (!validEntry);

        return country;
    }

    private static List<Country> fetchAllCountries(){

        //Open a session
        Session session = sessionFactory.openSession();

        //Create CriteriaBuilder
        CriteriaBuilder builder = session.getCriteriaBuilder();

        //Create CriteriaQuery
        CriteriaQuery<Country> criteria = builder.createQuery(Country.class);

        //Specify criteria root
        criteria.from(Country.class);

        //Execute query
        List<Country> countries = session.createQuery(criteria).getResultList();

        //Close the session
        session.close();

        return countries;
    }


}
