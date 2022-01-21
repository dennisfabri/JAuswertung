package de.df.jauswertung.util.times;

public class Swimmer {
    
    private final String firstName;
    private final String lastName;
    private final String organization;
    private final String sex;
    private final int yearOfBirth;
    
    public Swimmer(String firstName, String lastName, String organization, String sex, int yearOfBirth) {
        super();
        this.firstName = firstName;
        this.lastName = lastName;
        this.organization = organization;
        this.sex = sex;
        this.yearOfBirth = yearOfBirth;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getOrganization() {
        return organization;
    }
    
    public String getSex() {
        return sex;
    }

    public int getYearOfBirth() {
        return yearOfBirth;
    }
}
