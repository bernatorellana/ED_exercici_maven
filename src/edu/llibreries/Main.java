package edu.llibreries;

import edu.llibreries.pdf.GeneradorPdf;
import edu.llibreries.text.FormatadorText;

import java.io.IOException;
import java.util.List;

/**
 * Punt d'entrada de l'aplicació.
 *
 * Ús:
 *   java edu.llibreries.Main [fitxer_sortida.pdf] [text ...]
 *
 * Si no es passen arguments, genera "sortida.pdf" amb un text per defecte.
 */
public class Main {

    private static final String FITXER_PER_DEFECTE = "sortida.pdf";

    private static final String TEXT_PER_DEFECTE =
            "hola món des de java. Aquest document s'ha generat automàticament "
          + "fent servir llibreries externes: una per crear el PDF i una altra "
          + "per donar format al text. Si estàs llegint això, vol dir que has "
          + "aconseguit posar totes les dependències al classpath.";

    public static void main(String[] args) {
        String fitxer = args.length > 0 ? args[0] : FITXER_PER_DEFECTE;
        String text = args.length > 1
                ? String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length))
                : TEXT_PER_DEFECTE;

        FormatadorText formatador = new FormatadorText(70);
        String titol = formatador.titol("exercici de llibreries externes");
        List<String> linies = formatador.parteixEnLinies(text);

        GeneradorPdf generador = new GeneradorPdf();
        try {
            generador.crea(fitxer, titol, linies);
            System.out.println("PDF creat correctament: " + fitxer);
        } catch (IOException e) {
            System.err.println("Error creant el PDF: " + e.getMessage());
            System.exit(1);
        }
    }
}
