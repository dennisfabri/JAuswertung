package de.df.jauswertung.timesextractor.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.df.jauswertung.timesextractor.codes.CountryCodes;
import de.df.jauswertung.timesextractor.codes.CountryCodesController;
import lombok.Value;

@Value
public class JAuswertungEntry {

    private static final CountryCodesController codes = CountryCodesController.getInstance();

    private static final Pattern pattern = Pattern.compile("^(.*) [(](.*)[)]$");

    private final String competitorId;
    private final String startnumber;
    private final String name;
    private final String organization;
    private final String nationality;
    private final int value;
    private final JAuswertungPenalty[] penalties;
    private final JAuswertungSwimmer[] swimmer;
    private final JAuswertungStart start;

    public JAuswertungEntry(String competitorId, String startnumber, String name, String organization,
            String nationality, int value, JAuswertungPenalty[] penalties, JAuswertungSwimmer[] swimmer,
            JAuswertungStart start) {

        String currentOrganization = organization;
        String currentNationality = nationality;
        if (nationality == null || nationality.isBlank()) {
            currentNationality = "DE";
            if (organization == null || organization.isBlank()) {
            } else {
                Matcher matcher = pattern.matcher(organization);
                if (matcher.find()) {
                    String newOrganization = matcher.group(1);
                    String newNationality = matcher.group(2);
                    CountryCodes code = codes.getByIOCCode(newNationality);
                    if (code != null) {
                        currentOrganization = newOrganization;
                        currentNationality = code.alpha2Code();
                    }
                }
            }
        }

        this.competitorId = competitorId == null ? "" : competitorId;
        this.startnumber = startnumber;
        this.name = name;
        this.organization = currentOrganization;
        this.nationality = currentNationality;
        this.value = value;
        this.penalties = penalties;
        this.swimmer = swimmer;
        this.start = start;
    }
}
