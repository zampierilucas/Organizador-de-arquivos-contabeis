import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.time.Year;
import java.util.Calendar;
import java.util.Date;


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
	
	String logPath = pathtoread + "log\\" + "log " + date.now() + ".txt";
	String EmpresaNome = null;
	String Datadodocumento = null;
	BufferedWriter bw = null;
	FileWriter fw = null;
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

	log.Header(bw, date.now());

	for (int i = 0; i < listOfFiles.length; i++) {
		if ((listOfFiles[i].isFile()) && (listOfFiles[i].getName().matches("(?i).*\\.(pdf|PDF|txt|TXT)$")))
		{
			// Open Current file
			File file = new File(listOfFiles[i].getAbsolutePath());
			
			try
			{
				// Is TXT
				if (listOfFiles[i].getName().matches("(?i).*\\.(txt|TXT)$")) {
					BufferedReader br = new BufferedReader(new FileReader(listOfFiles[i]));
					while (counter < 10) {
						parsedText = parsedText + br.readLine() + System.lineSeparator();
						counter++;
					}
					br.close();
					counter = 0;
				// Is PDF
				} else {

					parsedText = pdf.to_string(file);
					
					// Normalize document
					parsedText = normalize(parsedText);

				}

			TipoFile = data.fileType(parsedText);
			
			// Clean up exceptions of current file type
			parsedText = data.NamingNormalization(parsedText, TipoFile);
				
			String[] ret = data_mes_ano(parsedText, TipoFile);
				
			anoDocumento = ret[0];
			Datadodocumento = ret[1];

			// Get company name
			EmpresaNome = data.CompanyName(parsedText,TipoFile, pathtosrv);
			
			// Verify for 'filial'
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


			// Add to log current document information
			log.File(bw, EmpresaNome, TipoFile,  listOfFiles[i].getAbsolutePath(), FullPath);


			File PathWOFileF = new File(pathtosrv + EmpresaNome + "\\" + TipoFile + "\\" + anoDocumento + "\\");
			if (FullPath.matches("(?i).*\\.(nulo|null)$")) { 
				log.ErrorFound(bw, parsedText);
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
//					System.out.println("source " + source + "destination" + destination);
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
		log.statystics(iteracaoComSucesso, iteracaoComErros, iteracaoTotal, logPath);
	 
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
		parsedText = parsedText.replaceAll("'", "");
		
		return parsedText;
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
		FileChannel source = null;
		FileChannel destination = null;
				
		// Create if already doest exists
		if (!destFile.exists()) 
			destFile.createNewFile();
		
		RandomAccessFile SourceAcess = new RandomAccessFile(sourceFile, "rw");
		RandomAccessFile DestinationAcess = new RandomAccessFile(destFile, "rw");

		// Get full path of file
		source = SourceAcess.getChannel();
		destination = DestinationAcess.getChannel();
		

		// Move file
		source.transferTo(0L, source.size(), destination);
			
		// Close files
		SourceAcess.close();
		DestinationAcess.close();
		source.close();
		destination.close();
		
	}



	public static String[] data_mes_ano(String parsedText, String TipoFile) {
		
		int tentativa = 0;

		Calendar calendar = Calendar.getInstance();

		if (TipoFile == null)
			return null;
		
		Date today = new Date();
		calendar.setTime(today);
		calendar.set(2, calendar.get(2));
		calendar.set(5, 1);
		calendar.add(5, -1);
		
		String anoDocumento = null;
		String Datadodocumento = null;

		String[] mes0A = { "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12" };
		String[] mes0B = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12" };
//		String[] mes0C = { "Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez" };
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
    		                
                            searchOnDoc = createString(Formatador_1,mes0A[mesI0], Integer.valueOf(ano));     
                            
                            if (parsedText.contains(searchOnDoc)){
    						    anoDocumento = String.valueOf(ano);
                                Datadodocumento = date.ConvertDate(dateFormatter,ano,mes0A,mesI0);
    						}
    						break;
    					
    					case "DSN":
    		                Formatador_1 = "Principal\r\n%s/%d";
    		                Formatador_2 = "Exigivel\r\n%s/%d";
    		                dateFormatter = "%s.%d";
                                                        
                            if (parsedText.contains(createString(Formatador_1, mes0A[mesI0], Integer.valueOf(ano)))){
								anoDocumento = String.valueOf(ano);
                                Datadodocumento = date.ConvertDate(dateFormatter,ano,mes0A,mesI0);
    						}
                            else if(parsedText.contains(createString(Formatador_2, mes0A[mesI0], Integer.valueOf(ano)))){
                                anoDocumento = String.valueOf(ano);
                                Datadodocumento = date.ConvertDate(dateFormatter,ano,mes0A,mesI0);
                            }
    						break;
    					
    					case "NFG":
    		                Formatador_1 = "periodo de 01/%s/%d a %d/%s/%d";
    		                dateFormatter = "%s.%d";
    						
                            searchOnDoc = createString(Formatador_1, mes0A[mesI0], Integer.valueOf(ano), Integer.valueOf(dia), mes0A[mesI0], Integer.valueOf(ano));     
                            
                            if (parsedText.contains(searchOnDoc)){
    						    anoDocumento = String.valueOf(ano);
                                Datadodocumento = date.ConvertDate(dateFormatter,ano,mes0A,mesI0);
    						}
                            
    						break;
    					
    					case "SPED CONTRIBUICOES":
    		                Formatador_1 = "%d/%s/%d";
    		                Formatador_2 = "%d%s%d";
    		                dateFormatter = "%s.%d";
    		                
                            if (parsedText.contains(createString(Formatador_1,Integer.valueOf(dia), mes0A[mesI0], Integer.valueOf(ano)))){
                              anoDocumento = String.valueOf(ano);
                              Datadodocumento = date.ConvertDate(dateFormatter,ano,mes0A,mesI0);
                            }
                            else if(parsedText.contains(createString(Formatador_2, Integer.valueOf(dia), mes0A[mesI0], Integer.valueOf(ano)))){
  						        anoDocumento = String.valueOf(ano);
                                Datadodocumento = date.ConvertDate(dateFormatter,ano,mes0A,mesI0);
    						}
    						break;
    					
    					case "RDI":
    		                Formatador_1 = "Periodo: %s/%d";
    		                dateFormatter = "%s.%d";
    					  
                            searchOnDoc = createString(Formatador_1, mes0D[mesI0], Integer.valueOf(ano));     
                            
                            if (parsedText.contains(searchOnDoc)){
  						        anoDocumento = String.valueOf(ano);
                                Datadodocumento = date.ConvertDate(dateFormatter,ano,mes0A,mesI0);
    						}
    						break;
    					
    					case "DEISS":
    		                Formatador_1 = "Ano e Mes de Referencia:    %d/  %s";
    		                dateFormatter = "%s.%d";

                            searchOnDoc = createString(Formatador_1,Integer.valueOf(ano), mes0B[mesI0]);     
                                                        
                            if (parsedText.contains(searchOnDoc)){
    						    anoDocumento = String.valueOf(ano);
                                Datadodocumento = date.ConvertDate(dateFormatter,ano,mes0A,mesI0);
    						}
    						break;
    						
    					case "DMS":
    		                Formatador_1 = "%s/%d\r\nSem Movimento de ISS";
    		                Formatador_2 = "Contribuinte:\r\n%s/%d";
    		                dateFormatter = "%s.%d";
    						
                            if (parsedText.contains(createString(Formatador_1, mes0A[mesI0], Integer.valueOf(ano)))){
    	                        anoDocumento = String.valueOf(ano);
    	                        Datadodocumento = date.ConvertDate(dateFormatter,ano,mes0A,mesI0);
    						}
                       
                            else if (parsedText.contains(createString(Formatador_2, mes0A[mesI0], Integer.valueOf(ano)))){
                                anoDocumento = String.valueOf(ano);
                                Datadodocumento = date.ConvertDate(dateFormatter,ano,mes0A,mesI0);
                           
                            }
    						break;
    						
                        case "OPCAO SIMPLES":
                            Formatador_1 = "Ano-calendario: %d";
                            dateFormatter = "%d";
                            
                            searchOnDoc = createString(Formatador_1,Integer.valueOf(ano));     
                            
                            if (parsedText.contains(searchOnDoc)){
                                anoDocumento = String.valueOf(ano);
                                Datadodocumento = date.ConvertDate(dateFormatter,ano);
                            }
                            break;
                            
                        case "DEFIS":
                            // Periodo abrangido pela Declaracao: 01/01/2019 a 26/06/2019
                            // Periodo abrangido pela Declaracao: 01/04/2018 a 31/12/2018
                            Formatador_1 = "Periodo abrangido pela Declaracao: 01/%s/%d a %d/%s/%d";
                            dateFormatter = "%d";
                            
                            searchOnDoc = createString(Formatador_1, mes0A[mesI1], Integer.valueOf(ano), Integer.valueOf(dia), mes0A[mesI0], Integer.valueOf(ano));

                            if (parsedText.contains(searchOnDoc)){
                                anoDocumento = String.valueOf(ano);
                                Datadodocumento = date.ConvertDate(dateFormatter,ano);
                            }

                            break;
                        
                        case "DME":
	                        // Ano e Mes de Referencia:    2019/  5
	                        Formatador_1 = "Ano e Mes de Referencia:    %d/  %d";
	                        dateFormatter = "%d";
	                        searchOnDoc = createString(Formatador_1, Integer.valueOf(ano) ,Integer.valueOf(mesI0));
	                        
	                        if (parsedText.contains(searchOnDoc)){
	                            anoDocumento = String.valueOf(ano);
	                            Datadodocumento = date.ConvertDate(dateFormatter,ano);
	                        }
                        break;
      					
    					default: 
    						
    						System.out.println("Identificador de tipo de arquivo nao encontrado.");
    						return new String[] { (String)null, (String)null };
    						
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
		return new String[] { (String)anoDocumento, (String)Datadodocumento };
	}
	
    // Create String to be searched on document
    static String createString(String formatador, Object... obj) {
      return (String.format(formatador, obj));
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





	
}