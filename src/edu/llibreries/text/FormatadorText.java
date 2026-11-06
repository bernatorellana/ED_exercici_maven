package edu.llibreries.text;

import org.apache.commons.text.WordUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Prepara el text abans d'escriure'l al PDF.
 * Utilitza Apache Commons Text.
 */
public class FormatadorText {

    private final int ampleLinia;

    public FormatadorText(int ampleLinia) {
        this.ampleLinia = ampleLinia;
    }

    /** Posa en majúscula la primera lletra de cada paraula. */
    public String titol(String text) {
        return WordUtils.capitalizeFully(text);
    }

    /**
     * Parteix el text en línies de com a màxim 'ampleLinia' caràcters,
     * sense tallar paraules.
     */
    public List<String> parteixEnLinies(String text) {
        String ajustat = WordUtils.wrap(text, ampleLinia, "\n", true);
        return Arrays.asList(ajustat.split("\n"));
    }
}
