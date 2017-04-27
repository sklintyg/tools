package se.inera.intyg.common.tools.anonymisering;

import java.time.Period;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
import java.util.regex.Pattern;
import java.time.LocalDate;
import java.security.MessageDigest;

public class AnonymiseraConsistentPersonId {

    private static final String PERSON_NUMBER_WITHOUT_DASH_REGEX = "[0-9]{12}";

    private Map<String, String> actualToAnonymized = Collections.synchronizedMap(new HashMap<String, String>());

    // CHECKSTYLE:OFF MagicNumber
    public String anonymisera(String patientId) {
        String origID = patientId;
        patientId = normalisera(patientId);
        String anonymized = actualToAnonymized.get(patientId);
        if (anonymized == null) {
            anonymized = hashPersonnummer(patientId);
        }
        return anonymized;
    }

    public static String determineSexFromPersonnummer(String patientId) {
        String normaliserat = normalisera(patientId);
        int sexNumber = normaliserat.charAt(11);
        return sexNumber % 2 == 0 ? "female" : "male";
    }

    public static String determineAgeFromPersonnummer(String patientId) {
        String normaliserat = normalisera(patientId);
        int year = Integer.parseInt(normaliserat.substring(0,4));
        int month = Integer.parseInt(normaliserat.substring(4,6));
        int day = Integer.parseInt(normaliserat.substring(6,8));

        return Integer.toString(Period.between(LocalDate.of(year, month, day), LocalDate.now()).getYears());
    }

    static String normalisera(String personnr) {
        if (Pattern.matches(PERSON_NUMBER_WITHOUT_DASH_REGEX, personnr)) {
            return personnr.substring(0, 8) + "-" + personnr.substring(8);
        } else {
            return personnr;
        }
    }

    private String hashPersonnummer(String nummer) {
        String hashed = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(nummer.getBytes("UTF-8"));
            byte[] digest = md.digest();
            hashed = String.format("%064x", new java.math.BigInteger(1, digest));
            actualToAnonymized.put(nummer, hashed);
        } catch (Exception e ) {
        }
        return hashed;
    }

    // CHECKSTYLE:ON MagicNumber
}
