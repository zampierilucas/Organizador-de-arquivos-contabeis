import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Year;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

public class data {
		
	// Search on pdf File for CNPJ
		public static String CompanyCNPJ(String CurrentFile) {
			
			String FoundCNPJ = null;
			int i = 0;
			
			Pattern CNPJ_Rgx = Pattern.compile("([0-9]{2}\\.[0-9]{3}\\.[0-9]{3}\\/[0-9]{4}\\-[0-9]{2})|([^0-9][0-9]{14}[^0-9])|([^0-9][0-9]{13}[^0-9])|([^0-9][0-9]{12}[^0-9])");
			 
			Matcher CNPJ = CNPJ_Rgx.matcher(CurrentFile);
			
			while(CNPJ.find() && i<10)
			{
			    FoundCNPJ = CNPJ.group().replaceAll("[^0-9]","");
			    
			    if (Long.parseLong(FoundCNPJ) != 0)
				    return FoundCNPJ;
			    else
			    	i++;
			}

			return null;
		}
		

	public static String fileType(String parsedText) {
		String Document_Type = null;

		// Destda - Antonio Prado
		if (parsedText.contains("DeSTDA"))
			Document_Type = "DESTDA";

		// Simples nacional - Declaracao mensal
		else if (parsedText.contains("PGDAS-D"))
			Document_Type = "DSN";

		// Simples naciona - opcão anual
		else if (parsedText.contains("Opcao pelo Regime de Apuracao de Receitas"))
			Document_Type = "OPCAO SIMPLES";

		// ISS - Antonio Prado E Vacaria
		else if (parsedText.contains("DEISS") || parsedText.contains("Prefeitura Municipal de Vacaria"))
			Document_Type = "DEISS";

		// ISS - Ipê
		else if (parsedText.contains("Recibo de declaracao de ISS"))
			Document_Type = "RDI";

		// DEFIS
		else if (parsedText.contains("DEFIS"))
			Document_Type = "DEFIS";

		// SPED Contabil
		else if (parsedText.contains("Valor total do ICMS a recolher"))
			Document_Type = "EFD ICMS IPI";

		// Nota Fiscal Gaucha
		else if (parsedText.contains("NFG"))
			Document_Type = "NFG";

		// Sped ContribuicÃµes
		else if (parsedText.contains("RECIBO DE ENTREGA DE ESCRITURACAO FISCAL DIGITAL - CONTRIBUICOES")
				|| parsedText.contains("|0100|EVANDRO ZANOTTO|64791602072"))
			Document_Type = "SPED CONTRIBUICOES";

		// DME - Vacaria
		else if (parsedText.contains("Protocolo Entrega DME"))
			Document_Type = "DME";

		// Nao encontrado
		else {
			System.out.println("Tipo nao encontrado");
			Document_Type = null;
		}
		return Document_Type;
	}

	// Normalize and clean up pdf
	static String normalize(String parsedText) {

		parsedText = java.text.Normalizer.normalize(parsedText, java.text.Normalizer.Form.NFD);
		parsedText = parsedText.replaceAll("[^\\p{ASCII}]", "");
		parsedText = parsedText.replaceAll("&", "E");
		parsedText = parsedText.replaceAll("'", "");

		return parsedText;
	}
	
	public static String PDFToString(File file) throws FileNotFoundException, IOException {

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
		PDFStructure = new PDDocument(parser.getDocument());

		// Convert to text and break lines
		textOfPdf = pdfStripper.getText(PDFStructure);

		// Close pdf structure
		PDFStructure.close();

		return textOfPdf;
	}
 
	public static String ConvertCNPJtoPath(String companyCNPJ ,String PathToServer) {
		
		File pasta = new File(PathToServer);
		File[] pastas_SRV = pasta.listFiles();

		// Search in all company folders
		for (int index = 0; index < pastas_SRV.length; index++) {

			// Server folder name matches to CNPJ
			if (pastas_SRV[index].getAbsolutePath().contains(companyCNPJ)) {
				return pastas_SRV[index].getAbsolutePath();
			}
		}
		return null;
	}
	
	public static date DocumentPeriod(String parsedText, String TipoFile) {

		if (TipoFile == null)
			return null;

		date DocumentDate = new date();

		String[] mes0A = { null, "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12" };
		String[] mes0B = { null, "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12" };
//		String[] mes0C = { null, "Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez" };
		String[] mes0D = { null, "Janeiro", "Fevereiro", "Marco", "Abril", "Maio", "Junho", "Julho", "Agosto",
				"Setembro", "Outubro", "Novembro", "Dezembro" };

		String Formatador_1;
		String Formatador_2;
		String dateFormatter;

		for (int IndexOfYear = Year.now().getValue(); IndexOfYear > 1950; IndexOfYear--) {
			for (int IndexOfMonth = 1; IndexOfMonth <= 12; IndexOfMonth++) {
				for (int IndexOfDay = 1; IndexOfDay <= 31; IndexOfDay++) {

					String searchOnDoc = null;

					switch (TipoFile) {
					case "DESTDA":
						Formatador_1 = "Mes Referencia %s/%d";
						dateFormatter = "%s.%d";

						searchOnDoc = createString(Formatador_1, mes0A[IndexOfMonth], Integer.valueOf(IndexOfYear));

						if (parsedText.contains(searchOnDoc)) {		
							DocumentDate.setObj(String.valueOf(IndexOfYear),date.ConvertDate(dateFormatter, IndexOfYear, mes0A, IndexOfMonth));
							return DocumentDate;
							
						}
						break;

					case "DSN":
						Formatador_1 = "Principal\r\n%s/%d";
						Formatador_2 = "Exigivel\r\n%s/%d";
						dateFormatter = "%s.%d";

						if (parsedText.contains(createString(Formatador_1, mes0A[IndexOfMonth], Integer.valueOf(IndexOfYear)))) {				
							DocumentDate.setObj(String.valueOf(IndexOfYear),date.ConvertDate(dateFormatter, IndexOfYear, mes0A, IndexOfMonth));
							return DocumentDate;
							
						} else if (parsedText.contains(createString(Formatador_2, mes0A[IndexOfMonth], Integer.valueOf(IndexOfYear)))) {
							DocumentDate.setObj(String.valueOf(IndexOfYear),date.ConvertDate(dateFormatter, IndexOfYear, mes0A, IndexOfMonth));
							return DocumentDate;
						}
						break;

					case "NFG":
						Formatador_1 = "periodo de 01/%s/%d a %d/%s/%d";
						dateFormatter = "%s.%d";

						searchOnDoc = createString(Formatador_1, mes0A[IndexOfMonth], Integer.valueOf(IndexOfYear), Integer.valueOf(IndexOfDay), mes0A[IndexOfMonth], Integer.valueOf(IndexOfYear));

						if (parsedText.contains(searchOnDoc)) {
							DocumentDate.setObj(String.valueOf(IndexOfYear),date.ConvertDate(dateFormatter, IndexOfYear, mes0A, IndexOfMonth));
							return DocumentDate;
	
						}

						break;

					case "SPED CONTRIBUICOES":
						Formatador_1 = "%d/%s/%d";
						Formatador_2 = "%d%s%d";
						dateFormatter = "%s.%d";

						if (parsedText.contains(createString(Formatador_1, Integer.valueOf(IndexOfDay), mes0A[IndexOfMonth], Integer.valueOf(IndexOfYear)))) {
							DocumentDate.setObj(String.valueOf(IndexOfYear),date.ConvertDate(dateFormatter, IndexOfYear, mes0A, IndexOfMonth));
							return DocumentDate;
							
						} else if (parsedText.contains(createString(Formatador_2, Integer.valueOf(IndexOfDay), mes0A[IndexOfMonth], Integer.valueOf(IndexOfYear)))) {
							DocumentDate.setObj(String.valueOf(IndexOfYear),date.ConvertDate(dateFormatter, IndexOfYear, mes0A, IndexOfMonth));
							return DocumentDate;
							
						}
						break;

					case "RDI":
						Formatador_1 = "Periodo: %s/%d";
						dateFormatter = "%s.%d";

						searchOnDoc = createString(Formatador_1, mes0D[IndexOfMonth], Integer.valueOf(IndexOfYear));

						if (parsedText.contains(searchOnDoc)) {
							DocumentDate.setObj(String.valueOf(IndexOfYear),date.ConvertDate(dateFormatter, IndexOfYear, mes0A, IndexOfMonth));
							return DocumentDate;
							
						}
						break;

					case "DEISS":
						Formatador_1 = "Ano e Mes de Referencia:    %d/  %s";
						dateFormatter = "%s.%d";

						searchOnDoc = createString(Formatador_1, Integer.valueOf(IndexOfYear), mes0B[IndexOfMonth]);

						if (parsedText.contains(searchOnDoc)) {
							DocumentDate.setObj(String.valueOf(IndexOfYear),date.ConvertDate(dateFormatter, IndexOfYear, mes0A, IndexOfMonth));
							return DocumentDate;
						}
						break;

					case "DMS":
						Formatador_1 = "%s/%d\r\nSem Movimento de ISS";
						Formatador_2 = "Contribuinte:\r\n%s/%d";
						dateFormatter = "%s.%d";

						if (parsedText.contains(createString(Formatador_1, mes0A[IndexOfMonth], Integer.valueOf(IndexOfYear)))) {
							DocumentDate.setObj(String.valueOf(IndexOfYear),date.ConvertDate(dateFormatter, IndexOfYear, mes0A, IndexOfMonth));
							return DocumentDate;
						}

						else if (parsedText.contains(createString(Formatador_2, mes0A[IndexOfMonth], Integer.valueOf(IndexOfYear)))) {
							DocumentDate.setObj(String.valueOf(IndexOfYear),date.ConvertDate(dateFormatter, IndexOfYear, mes0A, IndexOfMonth));
							return DocumentDate;

						}
						break;

					case "OPCAO SIMPLES":
						Formatador_1 = "Ano-calendario: %d";
						dateFormatter = "%d";

						searchOnDoc = createString(Formatador_1, Integer.valueOf(IndexOfYear));

						if (parsedText.contains(searchOnDoc)) {
							DocumentDate.setObj(String.valueOf(IndexOfYear),date.ConvertDate(dateFormatter, IndexOfYear));
							return DocumentDate;
							
						}
						break;

					case "DEFIS":
						Formatador_1 = "Periodo abrangido pela Declaracao: 01/%s/%d a %d";
						dateFormatter = "%d";

						searchOnDoc = createString(Formatador_1, mes0A[IndexOfMonth], Integer.valueOf(IndexOfYear), Integer.valueOf(IndexOfDay));

						if (parsedText.contains(searchOnDoc)) {
							DocumentDate.setObj(String.valueOf(IndexOfYear),date.ConvertDate(dateFormatter, IndexOfYear));
							return DocumentDate;
							
						}

						break;

					case "DME":
						Formatador_1 = "Ano e Mes de Referencia:    %d/  %d";
						dateFormatter = "%d";
						searchOnDoc = createString(Formatador_1, Integer.valueOf(IndexOfYear), Integer.valueOf(IndexOfMonth));

						if (parsedText.contains(searchOnDoc)) {
							DocumentDate.setObj(String.valueOf(IndexOfYear),date.ConvertDate(dateFormatter, IndexOfYear));
							return DocumentDate;
						}
						break;

					default:

						System.out.println("Identificador de tipo de arquivo nao encontrado.");
						return null;

					}
				}
			}
		}
		return null;
	}
	
	// Create String to be searched on document
		static String createString(String formatador, Object... obj) {
			return (String.format(formatador, obj));
		}
		
}
