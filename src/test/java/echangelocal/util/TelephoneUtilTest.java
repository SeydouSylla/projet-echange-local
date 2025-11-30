package echangelocal.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TelephoneUtilTest {

    @Test
    void estNumeroTelephoneValide_DoitRetournerVrai_PourNumerosValides() {
        assertTrue(TelephoneUtil.estNumeroTelephoneValide("0123456789"));
        assertTrue(TelephoneUtil.estNumeroTelephoneValide("0612345678"));
        assertTrue(TelephoneUtil.estNumeroTelephoneValide("0712345678"));
        assertTrue(TelephoneUtil.estNumeroTelephoneValide("+33123456789"));
        assertTrue(TelephoneUtil.estNumeroTelephoneValide("01 23 45 67 89"));
    }

    @Test
    void estNumeroTelephoneValide_DoitRetournerFaux_PourNumerosInvalides() {
        assertFalse(TelephoneUtil.estNumeroTelephoneValide("12345"));
        assertFalse(TelephoneUtil.estNumeroTelephoneValide("01234567890"));
        assertFalse(TelephoneUtil.estNumeroTelephoneValide("abcdefghij"));
        assertFalse(TelephoneUtil.estNumeroTelephoneValide(""));
        assertFalse(TelephoneUtil.estNumeroTelephoneValide(null));
    }

    @Test
    void formaterNumeroTelephone_DoitFormaterCorrectement() {
        assertEquals("01 23 45 67 89", TelephoneUtil.formaterNumeroTelephone("0123456789"));
        assertEquals("06 12 34 56 78", TelephoneUtil.formaterNumeroTelephone("0612345678"));
        assertEquals("01 23 45 67 89", TelephoneUtil.formaterNumeroTelephone("+33123456789"));
    }
}