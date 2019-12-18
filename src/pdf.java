import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

public class pdf {

	public static String to_string(File file) throws FileNotFoundException, IOException {
		 	
		String textOfPdf = null;
		PDFParser parser = null;
		PDFTextStripper pdfStripper = null;
		PDDocument PDFStructure = null;
		
		// Init new pdf Stripper
		pdfStripper = new PDFTextStripper();
				
		// Open file in pdf parse
		parser = new PDFParser(new FileInputStream(file));
		
		
		// Get all data of file
		parser.parse();
		
		// Create structure
		PDFStructure = new PDDocument( parser.getDocument());
		
		// Convert to text and break lines
		textOfPdf = pdfStripper.getText(PDFStructure);
			
		// Close pdf structure
		PDFStructure.close();
		
		return textOfPdf;
	 }
	    
}
