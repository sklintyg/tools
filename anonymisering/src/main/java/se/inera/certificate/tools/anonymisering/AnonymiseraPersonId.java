package se.inera.certificate.tools.anonymisering;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Collections;
import java.util.regex.Pattern;

import org.joda.time.LocalDate;
import org.joda.time.format.ISODateTimeFormat;

public class AnonymiseraPersonId {

    private static final String PERSON_NUMBER_WITHOUT_DASH_REGEX = "[0-9]{12}";

    public static final int DAY_INDEX = 6;
    public static final int END_OF_BIRTHDATE = 8;
    private static final int SAMORDNING_OFFSET = 6;
    public static final int BIRTHDAY_RANGE = 1000;
    public static final int SEX_INDEX = 11;

    private final Random random = new Random();
    private final Map<String, String> actualToAnonymized = Collections.synchronizedMap(new HashMap<String, String>());
    private final Set<String> anonymizedSet = Collections.synchronizedSet(new HashSet<String>());

    public String anonymisera(String patientId) {
        String anonymized = actualToAnonymized.get(patientId);
        if (anonymized == null) {
            anonymized = getUniqueRandomPersonid(patientId);
        }
        return anonymized;
    }

    String normalisera(String personnr) {
        if (Pattern.matches(PERSON_NUMBER_WITHOUT_DASH_REGEX, personnr)) {
            return personnr.substring(0, 8) + "-" + personnr.substring(8);
        } else {
            return personnr;
        }
    }
    
    private String getUniqueRandomPersonid(String nummer) {
        String anonymized;
        try {
            do {
                anonymized = newRandomPersonid(nummer);
            } while (anonymizedSet.contains(anonymized) || nummer == anonymized);
        } catch (Exception ee) {
            System.err.println("Unrecognized personid " + nummer);
            anonymized = nummer;
        }
        anonymizedSet.add(anonymized);
        actualToAnonymized.put(nummer, anonymized);
        return anonymized;
    }

    // CHECKSTYLE:OFF MagicNumber
    private String newRandomPersonid(String nummer) {
        LocalDate date;
        boolean samordning = false;
        try {
            date = ISODateTimeFormat.basicDate().parseLocalDate(nummer.substring(0, END_OF_BIRTHDATE));
        } catch (Exception e) {
            StringBuilder b = new StringBuilder(nummer.substring(0, END_OF_BIRTHDATE));
            b.setCharAt(DAY_INDEX, (char) (b.charAt(DAY_INDEX) - SAMORDNING_OFFSET));
                date = ISODateTimeFormat.basicDate().parseLocalDate(b.toString());
                samordning = true;
        }
        int days = random.nextInt(BIRTHDAY_RANGE) - BIRTHDAY_RANGE/2;
        if (days == 0) days = BIRTHDAY_RANGE/2;
        date = date.plusDays(days);
        int extension = random.nextInt(9989);
        // Fix sex if needed
        if (((nummer.charAt(SEX_INDEX) - '0') & 1) != ((extension / 10) & 1)) {
            extension += 10;
        }
        String prefix = date.toString("yyyyMMdd");
        // Make samordning if needed
        if (samordning) {
            StringBuilder b = new StringBuilder(prefix);
            b.setCharAt(DAY_INDEX, (char) (prefix.charAt(DAY_INDEX) + DAY_INDEX));
            prefix = b.toString();
        }

        return prefix + String.format("-%1$04d", extension);
    }
    // CHECKSTYLE:ON MagicNumber

}
