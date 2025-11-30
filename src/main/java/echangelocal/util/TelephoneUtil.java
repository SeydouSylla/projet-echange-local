package echangelocal.util;

import java.util.regex.Pattern;

public class TelephoneUtil {

    private static final Pattern PATTERN_TELEPHONE_FR =
            Pattern.compile("^(\\+33|0)[1-9](\\d{2}){4}$");

    private TelephoneUtil() {
        // Utilitaire class
    }

    public static boolean estNumeroTelephoneValide(String telephone) {
        if (telephone == null || telephone.trim().isEmpty()) {
            return false;
        }

        String telephoneNettoye = telephone.replaceAll("\\s+", "");
        return PATTERN_TELEPHONE_FR.matcher(telephoneNettoye).matches();
    }

    public static String formaterNumeroTelephone(String telephone) {
        if (telephone == null) return null;

        String telephoneNettoye = telephone.replaceAll("\\s+", "");

        if (telephoneNettoye.startsWith("+33")) {
            telephoneNettoye = "0" + telephoneNettoye.substring(3);
        }

        if (telephoneNettoye.length() == 10) {
            return String.format("%s %s %s %s %s",
                    telephoneNettoye.substring(0, 2),
                    telephoneNettoye.substring(2, 4),
                    telephoneNettoye.substring(4, 6),
                    telephoneNettoye.substring(6, 8),
                    telephoneNettoye.substring(8, 10));
        }

        return telephone;
    }
}