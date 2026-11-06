package edu.llibreries.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.IOException;
import java.util.List;

/**
 * Crea un document PDF d'una pàgina amb un títol i unes quantes línies de text.
 * Utilitza Apache PDFBox 3.x.
 */
public class GeneradorPdf {

    private static final float MARGE = 60f;
    private static final float MIDA_TITOL = 18f;
    private static final float MIDA_TEXT = 12f;
    private static final float INTERLINEAT = 16f;

    public void crea(String fitxer, String titol, List<String> linies) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage pagina = new PDPage(PDRectangle.A4);
            document.addPage(pagina);

            PDType1Font fontTitol = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font fontText = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            float y = pagina.getMediaBox().getHeight() - MARGE;

            try (PDPageContentStream contingut = new PDPageContentStream(document, pagina)) {
                contingut.beginText();
                contingut.setFont(fontTitol, MIDA_TITOL);
                contingut.newLineAtOffset(MARGE, y);
                contingut.showText(titol);

                contingut.setFont(fontText, MIDA_TEXT);
                contingut.setLeading(INTERLINEAT);
                contingut.newLine();
                contingut.newLine();
                for (String linia : linies) {
                    contingut.showText(linia);
                    contingut.newLine();
                }
                contingut.endText();
            }

            document.save(fitxer);
        }
    }
}
