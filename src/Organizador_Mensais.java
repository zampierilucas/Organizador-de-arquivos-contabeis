import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.time.Year;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

public class Organizador_Mensais
{
	public static final String DATE_FORMAT_NOW = "yyyyMMdd HHmm";


	public static void main(String[] args) throws IOException { 
	int REC = 0;
	String FullPath = null;
	String RECFILE = null;
	
	// Windows path
	String pathtoread = "\\\\10.1.20.30\\Organizador\\";
	String pathtosrv = "\\\\10.1.20.13\\setores\\GERAL\\Documentos empresariais\\Documentos\\";
	
	// linux path
	//String pathtosrv = "smb://10.1.20.13/setores/GERAL/Documentos empresariais/Documentos/";
	
	String logPath = pathtoread + "log\\" + "log " + data_agora() + ".txt";
	String EmpresaNome = null;
	String Datadodocumento = null;
	BufferedWriter bw = null;
	FileWriter fw = null;
	PDFParser parser = null;
	PDDocument pdDoc = null;
	COSDocument cosDoc = null;
	String TipoFile = null;
	String parsedText = "";    
	fw = new FileWriter(logPath);
	bw = new BufferedWriter(fw);
	int iteracaoComSucesso = 0, iteracaoComErros = 0, iteracaoTotal = 0, counter = 0;
	String anoDocumento = null;    
	File suposicao = null;
	boolean SPED_hasExtraFIles = false;

	File directory = new File(pathtoread);
	File[] listOfFiles = directory.listFiles();

	EscreveTopoLog(bw, data_agora());

	for (int i = 0; i < listOfFiles.length; i++) {
		if ((listOfFiles[i].isFile()) && (listOfFiles[i].getName().matches("(?i).*\\.(pdf|PDF|txt|TXT)$")))
		{
			File file = new File(listOfFiles[i].getAbsolutePath());
			try
			{
				if (listOfFiles[i].getName().matches("(?i).*\\.(txt|TXT)$")) {
					BufferedReader br = new BufferedReader(new FileReader(listOfFiles[i]));
					while (counter < 10) {
						parsedText = parsedText + br.readLine() + System.lineSeparator();
						counter++;
					}
					br.close();
					counter = 0;
				} else {
					parser = new PDFParser(new FileInputStream(file));

				parser.parse();
				cosDoc = parser.getDocument();
				PDFTextStripper pdfStripper = new PDFTextStripper();
				pdDoc = new PDDocument(cosDoc);
				
				parsedText = pdfStripper.getText(pdDoc);
				parsedText = normalize(parsedText);

			}

			TipoFile = get_fileType(parsedText, TipoFile);
			pdDoc.close();

			parsedText = excecoes(parsedText, TipoFile);
				
			String[] ret = data_mes_ano(parsedText, TipoFile);
				
			anoDocumento = ret[0];
			Datadodocumento = ret[1];


			EmpresaNome = EmpresaGetName(parsedText, pathtosrv);
			EmpresaNome = filial(parsedText, pathtosrv, EmpresaNome);

			if (listOfFiles[i].getName().matches("(?i).*\\.(txt|TXT)$")) {
				FullPath = 
				pathtosrv + EmpresaNome + "\\" + TipoFile + "\\" + anoDocumento + "\\" + listOfFiles[i].getName();
				RECFILE = pathtosrv + EmpresaNome + "\\" + TipoFile + "\\" + anoDocumento + "\\" + 
				listOfFiles[i].getName().replaceFirst("[.][^.]+$", ".REC");

				suposicao = new File(listOfFiles[i].getAbsolutePath().replaceFirst("[.][^.]+$", ".REC"));
				REC = 1;
				FullPath = naoSobrescrever(FullPath, listOfFiles, i, REC);
				REC = 2;
				RECFILE = naoSobrescrever(RECFILE, listOfFiles, i, REC);
				if (suposicao.exists()) {
					SPED_hasExtraFIles = true;
				}
			}

			if (listOfFiles[i].getName().matches("(?i).*\\.(pdf|PDF)$")) {
				REC = 0;

				FullPath = pathtosrv + EmpresaNome + "\\" + TipoFile + "\\" + anoDocumento + "\\" + 
						TipoFile + " " + Datadodocumento + ".pdf";
				FullPath = naoSobrescrever(FullPath, listOfFiles, i, REC);
			}


			bw.newLine();
			bw.write("Empresa: " + EmpresaNome);
			bw.newLine();
	        bw.write("Tipo: " + TipoFile);
	        bw.newLine();
			bw.write("Origem:  " + listOfFiles[i].getAbsolutePath());
			bw.newLine();
			bw.write("Destino: " + FullPath);
			bw.newLine();



			if (TipoFile == "OPCAO SIMPLES") {
				TipoFile = "OP��O SIMPLES";
			}
			File PathWOFileF = new File(pathtosrv + EmpresaNome + "\\" + TipoFile + "\\" + anoDocumento + "\\");
			if (FullPath.contains("null")) {
				ErroArquivo(bw, parsedText);
			} else if (!PathWOFileF.exists()) {
				boolean result = false;
				try {
					PathWOFileF.mkdirs();
					result = true;
				}
				catch (SecurityException localSecurityException) {}
				if (result) {
					bw.write("Diretorio criado com sucesso");
					bw.newLine();
				}
			}

			File source = new File(listOfFiles[i].getAbsolutePath());

			if ((SPED_hasExtraFIles) && (listOfFiles[i].getName().matches("(?i).*\\.(txt|TXT|reg|REG)$"))) {
				bw.write("Destino: " + RECFILE);
				bw.newLine();
			}
			try
			{
			    // If all identification is correct, move file renaming and delet from source
				if ((!FullPath.contains("null")) && (source.exists())) {
					File destination = new File(FullPath);
					copyFile(source, destination);
					System.out.println("this is source " + source);
					source.delete();
					iteracaoComSucesso++;
				}
				// If is SPED, try to move extra files with it
				if (SPED_hasExtraFIles) {
					File destinationREC = new File(RECFILE);
					copyFile(suposicao, destinationREC);
					suposicao.delete();
					iteracaoComSucesso++;
					iteracaoTotal++;
				}
			}
			catch (Exception e55)
			{
				e55.printStackTrace();
			}
			bw.write(
					"------------------------------------------------------------------------------------------------------------------------------------------------------------------");
			iteracaoTotal++;

			parsedText = "";
			EmpresaNome = null;
			anoDocumento = null;
			TipoFile = "";
			source = null;
			suposicao = null;
			FullPath = null;
			SPED_hasExtraFIles = false;
			pdDoc.close();
			cosDoc.close();
			}
			catch (Exception e)
			{
			  e.printStackTrace();
			}
		}
	}

	
	bw.close();
	fw.close();
	if (listOfFiles.length > 0) 
		write_statystics(iteracaoComSucesso, iteracaoComErros, iteracaoTotal, logPath);
	 
	else {
		File log = new File(logPath);
		log.delete();
	}
		System.out.println("END OF RUN");
	}


	// Normalize and clean up pdf
	private static String normalize(String parsedText) {
		
		parsedText = java.text.Normalizer.normalize(parsedText, java.text.Normalizer.Form.NFD);
		parsedText = parsedText.replaceAll("[^\\p{ASCII}]", "");
		parsedText = parsedText.replaceAll("&", "E");
		
		return parsedText;
	}


	// Search for company name on file
	private static String EmpresaGetName(String ArquivoProcessado, String pathtosrv) {
		
		File pasta = new File(pathtosrv);
		File[] pastas_SRV = pasta.listFiles();
		String CompanyName = null;

		for (int index = 0; index < pastas_SRV.length; index++) {
			if (ArquivoProcessado.contains(pastas_SRV[index].getName())) {
				CompanyName = pastas_SRV[index].getName();
			}
		}
		return CompanyName;
	}

	private static String filial(String ArquivoProcessado, String pathtosrv, String EmpresaNome) throws IOException {
		
		if (ArquivoProcessado.contains("LUIZ ALBERTO PELLIN")) {
			EmpresaNome = EmpresaNome + "\\1 MATRIZ";
		}
		if (ArquivoProcessado.contains("12.883.198/0001-88")) {
			EmpresaNome = EmpresaNome + "\\CONFIDENZA NOVO";
		}
		if (ArquivoProcessado.contains("05.242.070/0001-70")) {
			EmpresaNome = EmpresaNome + "\\CONFIDENZA VELHO";
		}

		return EmpresaNome;
	}

	public static void copyFile(File sourceFile, File destFile)
			throws IOException
	{
		if (!destFile.exists()) {
			destFile.createNewFile();
		}

		FileChannel source = null;
		FileChannel destination = null;
		try {
			
			// Load source and destination 
			source = new RandomAccessFile(sourceFile, "rw").getChannel();
			destination = new RandomAccessFile(destFile, "rw").getChannel();
			
			// Move file
			source.transferTo(0L, source.size(), destination);
		}
		finally {
			source.close();
			destination.close();
		}
	}

	// Treat naming exceptions, and simply naming
	public static String excecoes(String parsedText, String TipoFile) {
		
		try {
			Properties prop = new Properties();
			FileInputStream exeptions = new FileInputStream(
					"\\\\10.1.20.30\\excecoesOrganizador\\" + TipoFile + ".properties");
			prop.load(exeptions);
			
	        String replaceFrom;
	        String replaceTo;
	            
			for (int exceptionIndex = 1; exceptionIndex <= prop.size() / 2; exceptionIndex++) {
    			replaceFrom = prop.getProperty(Integer.toString(exceptionIndex) + "A");
    			replaceTo   = prop.getProperty(Integer.toString(exceptionIndex) + "B");
    			
    	        parsedText = parsedText.replaceAll(replaceFrom,replaceTo);
	        
			}
			exeptions.close();
		}
		catch (IOException localIOException) {}
		return parsedText;
	}

	public static String[] data_mes_ano(String parsedText, String TipoFile) {
		
		int tentativa = 0;

		Calendar calendar = Calendar.getInstance();


		Date today = new Date();
		calendar.setTime(today);
		calendar.set(2, calendar.get(2));
		calendar.set(5, 1);
		calendar.add(5, -1);
		
		String anoDocumento = null;
		String Datadodocumento = null;

		String[] mes0A = { "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12" };
		String[] mes0B = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12" };
		String[] mes0C = { "Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez" };
		String[] mes0D = { "Janeiro", "Fevereiro", "Marco", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro" };

		String Formatador_1;
		String Formatador_2;
		String dateFormatter;

		for (int ano = 1950; ano < 2100; ano++) {
			for (int mesI0 = 0; mesI0 < 12; mesI0++) {
			  for(int mesI1 = 0; mesI1 < 12; mesI1++) {
				for (int dia = 0; dia < 32; dia++)
				{
				    // Teste o resultado mais possivel, no caso mes anterior.
					if (tentativa < 1) {
						ano = Year.now().getValue();
						mesI0 = calendar.get(2);
						dia =  Calendar.getInstance().getActualMaximum(5);
						tentativa = 1;
					}
					
					String searchOnDoc = null;
					
					switch(TipoFile)
					{
    					case "DESTDA":
    		                Formatador_1 = "Mes Referencia %s/%d";
    		                dateFormatter = "%s.%d";
    		                
                            searchOnDoc = createString(Formatador_1,mes0A[mesI0], toInt(ano));     
                            
                            if (parsedText.contains(searchOnDoc)){
    						    anoDocumento = set_AnoDocumento(ano);
                                Datadodocumento = set_DataDocumentoAnoMes(dateFormatter,mes0A,mesI0,ano);
    						}
    						break;
    					
    					case "DSN":
    		                Formatador_1 = "Principal\r\n%s/%d";
    		                Formatador_2 = "Exigivel\r\n%s/%d";
    		                dateFormatter = "%s.%d";
                                                        
                            if (parsedText.contains(createString(Formatador_1, mes0A[mesI0], toInt(ano)))){
    						    anoDocumento = set_AnoDocumento(ano);
                                Datadodocumento = set_DataDocumentoAnoMes(dateFormatter,mes0A,mesI0,ano);
    						}
                            else if(parsedText.contains(createString(Formatador_2, mes0A[mesI0], toInt(ano)))){
                                anoDocumento = set_AnoDocumento(ano);
                                Datadodocumento = set_DataDocumentoAnoMes(dateFormatter,mes0A,mesI0,ano);
                            }
    						break;
    					
    					case "NFG":
    		                Formatador_1 = "periodo de 01/%s/%d a %d/%s/%d";
    		                dateFormatter = "%s.%d";
    						
                            searchOnDoc = createString(Formatador_1, mes0A[mesI0], toInt(ano), toInt(dia), mes0A[mesI0], toInt(ano));     
                            
                            if (parsedText.contains(searchOnDoc)){
    						    anoDocumento = set_AnoDocumento(ano);
                                Datadodocumento = set_DataDocumentoAnoMes(dateFormatter,mes0A,mesI0,ano);
    						}
                            
    						break;
    					
    					case "SPED CONTRIBUICOES":
    		                Formatador_1 = "%d/%s/%d";
    		                Formatador_2 = "%d%s%d";
    		                dateFormatter = "%s.%d";
    		                
                            if (parsedText.contains(createString(Formatador_1,toInt(dia), mes0A[mesI0], toInt(ano)))){
                              anoDocumento = set_AnoDocumento(ano);
                              Datadodocumento = set_DataDocumentoAnoMes(dateFormatter,mes0A,mesI0,ano);
                            }
                            else if(parsedText.contains(createString(Formatador_2, toInt(dia), mes0A[mesI0], toInt(ano)))){
  						        anoDocumento = set_AnoDocumento(ano);
                                Datadodocumento = set_DataDocumentoAnoMes(dateFormatter,mes0A,mesI0,ano);
    						}
    						break;
    					
    					case "RDI":
    		                Formatador_1 = "Periodo: %s/%d";
    		                dateFormatter = "%s.%d";
    					  
                            searchOnDoc = createString(Formatador_1, mes0D[mesI0], toInt(ano));     
                            
                            if (parsedText.contains(searchOnDoc)){
  						        anoDocumento = set_AnoDocumento(ano);
                                Datadodocumento = set_DataDocumentoAnoMes(dateFormatter,mes0A,mesI0,ano);
    						}
    						break;
    					
    					case "DEISS":
    		                Formatador_1 = "Ano e Mes de Referencia:    %d/  %s";
    		                dateFormatter = "%s.%d";

                            searchOnDoc = createString(Formatador_1,toInt(ano), mes0B[mesI0]);     
                                                        
                            if (parsedText.contains(searchOnDoc)){
    						    anoDocumento = set_AnoDocumento(ano);
                                Datadodocumento = set_DataDocumentoAnoMes(dateFormatter,mes0A,mesI0,ano);
    						}
    						break;
    						
    					case "DMS":
    		                Formatador_1 = "%s/%d\r\nSem Movimento de ISS";
    		                Formatador_2 = "Contribuinte:\r\n%s/%d";
    		                dateFormatter = "%s.%d";
    						
                            if (parsedText.contains(createString(Formatador_1, mes0A[mesI0], toInt(ano)))){
    	                        anoDocumento = set_AnoDocumento(ano);
    	                        Datadodocumento = set_DataDocumentoAnoMes(dateFormatter,mes0A,mesI0,ano);
    						}
                       
                            else if (parsedText.contains(createString(Formatador_2, mes0A[mesI0], toInt(ano)))){
                                anoDocumento = set_AnoDocumento(ano);
                                Datadodocumento = set_DataDocumentoAnoMes(dateFormatter,mes0A,mesI0,ano);
                           
                            }
    						break;
    						
                        case "OPCAO SIMPLES":
                            Formatador_1 = "Ano-calendario: %d";
                            dateFormatter = "%d";
                            
                            searchOnDoc = createString(Formatador_1,toInt(ano));     
                            
                            if (parsedText.contains(searchOnDoc)){
                                anoDocumento = set_AnoDocumento(ano);
                                Datadodocumento = set_DataDocumentoAno(dateFormatter,ano);
                            }
                            break;
                            
                        case "DEFIS":
                            // Periodo abrangido pela Declaracao: 01/01/2019 a 26/06/2019
                            // Periodo abrangido pela Declaracao: 01/04/2018 a 31/12/2018
                            Formatador_1 = "Periodo abrangido pela Declaracao: 01/%s/%d a %d/%s/%d";
                            dateFormatter = "%d";
                            
                            searchOnDoc = createString(Formatador_1, mes0A[mesI1], toInt(ano), toInt(dia), mes0A[mesI0], toInt(ano));

                            if (parsedText.contains(searchOnDoc)){
                                anoDocumento = set_AnoDocumento(ano);
                                Datadodocumento = set_DataDocumentoAno(dateFormatter,ano);
                            }

                            break;
                        
                        case "DME":
                        // Ano e Mes de Referencia:    2019/  5
                        Formatador_1 = "Ano e Mes de Referencia:    %d/  %d";
                        dateFormatter = "%d";
                        searchOnDoc = createString(Formatador_1, toInt(ano) ,toInt(mesI0));
                        
                        if (parsedText.contains(searchOnDoc)){
                            anoDocumento = set_AnoDocumento(ano);
                            Datadodocumento = set_DataDocumentoAno(dateFormatter,ano);
                        }
                        break;
      					
    					default: 
    						System.out.println("Identificador de tipo de arquivo nao encontrado.");
    						break;
					}
					
					if (tentativa == 1) {
						ano = 1950;
						dia = 27;
						mesI0 = 0;
						tentativa = 2;
					}
				}
			  }
			}
		}
		return new String[] { anoDocumento, Datadodocumento };
	}
	
    // Cria String para ser pesquisada no documento
    static String createString(String formatador, Object... obj) {
    	
      return (String.format(formatador, obj));
    }
    
    static int toInt(int integer_parameter) {
      return Integer.valueOf(integer_parameter);
    }
	
    public static void EscreveTopoLog(BufferedWriter bw, String data_atual) throws IOException {

    	bw.write("Data: " + data_atual);
        bw.newLine();
        bw.write("Arquivos processadas: L+2");
        bw.newLine();
        bw.write("Arquivos processados com sucesso: L+3");
        bw.newLine();
        bw.write("Arquivos processados com erros: L+4");
        bw.newLine();
        bw.write("Tempo poupado: L+5 minutos");
        bw.newLine();
        bw.newLine();
    }

    public static void ErroArquivo(BufferedWriter bw, String parsedText) throws IOException {
        
    	bw.newLine();
        bw.write("Erro no doc:");
        bw.newLine();
        bw.newLine();
        bw.write(parsedText);
        bw.newLine();
        bw.write("Fim do Erro no doc.");
        bw.newLine();
        bw.write("-------------------------------------------------------------------------------");
    }

    public static String get_fileType(String parsedText, String Document_Type) {
    	
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
        
        // Sped Contribuicões
        else if (parsedText.contains("RECIBO DE ENTREGA DE ESCRITURACAO FISCAL DIGITAL - CONTRIBUICOES") ||
                 parsedText.contains("|0100|EVANDRO ZANOTTO|64791602072"))
            Document_Type = "SPED CONTRIBUICOES";
        
        // DME - Vacaria
        else if (parsedText.contains("Protocolo Entrega DME"))
            Document_Type = "DME";
        
        // Nao encontrado
        else 
            System.out.println("Tipo nao encontrado");
        
        return Document_Type;
    }

    public static String naoSobrescrever(String Destination, File[] listOfFiles, int i, int REC) {
        int fileNo = 0;
        File DestTest = new File(Destination);
        String DestStr = Destination;
        while (DestTest.exists()) {
            fileNo++;

            if ((Destination.matches("(?i).*\\.(pdf|PDF)$")) && (REC == 0)) {
                DestStr = Destination.replaceAll(".pdf", "(" + fileNo + ").pdf");
            }
            if ((listOfFiles[i].getName().matches("(?i).*\\.(txt|TXT)$")) && (REC == 1)) {
                DestStr = Destination.replaceAll(".txt", "(" + fileNo + ").txt");
            }
            if ((Destination.matches("(?i).*\\.(rec|REC)$")) && (REC == 2)) {
                DestStr = Destination.replaceAll(".REC", "(" + fileNo + ").REC");
            }
            DestTest = new File(DestStr);
        }
        fileNo = 0;
        Destination = DestStr;
        return Destination;
    }

    public static void write_statystics(int iteracaoComSucesso, int iteracaoComErros, int iteracaoTotal, String logPath) throws IOException {
        String input = "";

        BufferedReader readLog = new BufferedReader(new FileReader(logPath));
        String line;
        while ((line = readLog.readLine()) != null)  
            input = input + line + System.lineSeparator();

        int min = iteracaoTotal * 20 / 60;
        int seg = iteracaoTotal * 20 - min * 60;
        String iteracaoMin = min + ":" + seg;

        iteracaoComErros = iteracaoTotal - iteracaoComSucesso;

        String Sucesso = Integer.toString(iteracaoComSucesso);
        String Erros = Integer.toString(iteracaoComErros);
        String Total = Integer.toString(iteracaoTotal);

        if (iteracaoTotal == 0) {
            FileOutputStream out = new FileOutputStream(logPath);
            readLog.close();
            out.write(input.getBytes());
            out.close();
            System.exit(0);
        }
        else if ((min == 0) && (seg > 0)) {
            input = input.replace("L+2", Total);
            input = input.replace("L+3", Sucesso);
            input = input.replace("L+4", Erros);
            input = input.replace("L+5 minutos", iteracaoMin + " segundos");
        } else {
            input = input.replace("L+2", Total);
            input = input.replace("L+3", Sucesso);
            input = input.replace("L+4", Erros);
            input = input.replace("L+5", iteracaoMin);
        }
        FileOutputStream out = new FileOutputStream(logPath);
        out.write(input.getBytes());
        out.close();
        readLog.close();
        System.out.println(input);
    }

    public static String data_agora() {
        String DATE_FORMAT_NOW = "yyyyMMdd_HHmm";
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        return sdf.format(cal.getTime());
    }

    public static String data_dia() {
        String DATE_FORMAT_DIA = "dd";
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_DIA);
        return sdf.format(cal.getTime());
    }

    private static String set_DataDocumentoAno(String dateFormatter, int ano) {
      return String.format(dateFormatter, new Object[] { toInt(ano) });
    }
  
    private static String set_AnoDocumento(int ano) {
      return String.valueOf(ano);
    }       
  
    private static String set_DataDocumentoAnoMes(String dateFormatter, String[] mes0A, int mesI, int ano) {
      return String.format(dateFormatter, new Object[] { mes0A[mesI], toInt(ano) });
    }
	
}