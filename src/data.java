import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class data {
	// Search for company name on server folders
	public static String CompanyName(String ArquivoProcessado, String pathtosrv) {

		File pasta = new File(pathtosrv);
		File[] pastas_SRV = pasta.listFiles();
		String CompanyName = null;
		String serverCompanyName = null;

		for (int index = 0; index < pastas_SRV.length; index++) {

			serverCompanyName = pastas_SRV[index].getName();
//			serverCompanyName = NamingNormalization(pastas_SRV[index].getName(),"COMPANY_COMMON");

			if (ArquivoProcessado.contains(serverCompanyName)) {
				CompanyName = pastas_SRV[index].getName();
			}
		}

		return CompanyName;
	}

	// Treat naming exceptions, and simplify comp[any naming
	public static String NamingNormalization(String parsedText, String TipoFile) {
		  	
		String replaceFrom = null;
		String replaceTo = null;
		Properties prop = new Properties();       

		// Select file based on type
		try {
			FileInputStream exeptions = new FileInputStream("\\\\10.1.20.30\\excecoesOrganizador\\" + TipoFile + ".properties");
	
			// Load file properties
			prop.load(exeptions);
		} catch (Exception e){}
		
		for (int exceptionIndex = 1; exceptionIndex <= prop.size() / 2; exceptionIndex++) {
			// Replace properti A to B
			replaceFrom = prop.getProperty(Integer.toString(exceptionIndex) + "A");
			replaceTo   = prop.getProperty(Integer.toString(exceptionIndex) + "B");
			
	        parsedText = parsedText.replaceAll(replaceFrom,replaceTo);
		}
			
		// Return normalized text
		return parsedText;
	}

	public static String fileType(String parsedText) {
		String Document_Type = null;

		// Destda - Antonio Prado
		if (parsedText.contains("DeSTDA"))
			Document_Type = "DESTDA";

		// Simples nacional - Declaracao mensal
		else if (parsedText.contains("PGDAS-D"))
			Document_Type = "DSN";

		// Simples naciona - opcao anual
		else if (parsedText.contains("Opcao pelo Regime de Apuracao de Receitas"))
			Document_Type = "OPCAO SIMPLES";

		// ISS - Antonio Prado
		else if (parsedText.contains("DEISS"))
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

		// DMS
		else if (parsedText.contains("DMS"))
			Document_Type = "DMS";

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
			Document_Type = "nulo";
		}
		return Document_Type;
	}
}
