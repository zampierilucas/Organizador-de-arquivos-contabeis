import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.time.Year;

public class Organizador_Mensais {
	public static void main(String[] args) throws IOException {

		// Windows path
		String pathtoread = "\\\\10.1.20.30\\Organizador";
		String pathtosrv = "\\\\10.1.20.13\\setores\\GERAL\\Documentos empresariais\\Documentos";

		// Log path
		String logDocument = "log\\log_" + date.now() + ".txt";
		String logPath = mountPath("win", false, pathtoread, logDocument);

		// Log R/W
		FileWriter logCreate = new FileWriter(logPath);
		BufferedWriter logWrite = new BufferedWriter(logCreate);

		// Get source files
		File source_directory = new File(pathtoread);
		File[] source_files = source_directory.listFiles();

		// Add log Header
		log.Header(logWrite, date.now());

		// Init statistics
		int iteracaoComSucesso = 0;
		int iteracaoComErros = 0;
		int iteracaoTotal = 0;
		int counter = 0;

		// Main loop
		for (int indexOfFiles = 0; indexOfFiles < source_files.length; indexOfFiles++) {

			// Run variables
			String FullPath = null;
			String EmpresaNome = null;
			String Datadodocumento = null;
			String ParsedText = "";
			String TipoFile = null;
			String anoDocumento = null;
			boolean ExtraSpedFiles = false;

			// Create source aliases
			String file_name = source_files[indexOfFiles].getName();
			String file_path = source_files[indexOfFiles].getAbsolutePath();

			if (file_name.matches("(?i).*\\.(pdf|PDF|txt|TXT)$")) {
				// Open Current file
				File file = new File(file_path);

				try {
					// Is TXT
					if (file_name.matches("(?i).*\\.(txt|TXT)$")) {
						BufferedReader br = new BufferedReader(new FileReader(source_files[indexOfFiles]));
						while (counter < 10) {
							ParsedText = ParsedText + br.readLine() + System.lineSeparator();
							counter++;
						}
						br.close();
						counter = 0;
						// Is PDF
					} else {

						ParsedText = pdf.to_string(file);

						// Normalize document
						ParsedText = data.normalize(ParsedText);

					}

					// Get file type
					TipoFile = data.fileType(ParsedText);

					// Clean up exceptions of current file type
					ParsedText = data.NamingNormalization(ParsedText, TipoFile);

					// YES this is a method that returns one array, crucify me, I know
					String[] ret = data_mes_ano(ParsedText, TipoFile);

					anoDocumento = ret[0];
					Datadodocumento = ret[1];

					// Get company name
					EmpresaNome = data.CompanyName(ParsedText, TipoFile, pathtosrv);

					// Verify for 'filial'
					EmpresaNome = filial(ParsedText, pathtosrv, EmpresaNome);

					// If is a TXT file
					if (file_name.matches("(?i).*\\.(txt|TXT)$")) {

						// Get txt path
						FullPath = mountPath("win", true, pathtosrv, EmpresaNome, TipoFile, anoDocumento, file_name);

						// Generate REC path
						File sourceREC = new File(file_path.replaceFirst("[.][^.]+$", ".REC"));

						// Check if direcory contains a rec file
						if (sourceREC.exists())
							ExtraSpedFiles = true;

					}

					// If is a pdf File
					else if (file_name.matches("(?i).*\\.(pdf|PDF)$")) {

						String Document = TipoFile + " " + Datadodocumento + ".pdf";

						// Create path to document server
						FullPath = mountPath("win", true, pathtosrv, EmpresaNome, TipoFile, anoDocumento, Document);
					}

					// Add to log current document information
					log.File(logWrite, EmpresaNome, TipoFile, file_path, FullPath);

					// Create folder path
					String directoryPath = mountPath("win", false, pathtosrv, EmpresaNome, TipoFile, anoDocumento);

					File documentServer_Directory = new File(directoryPath);

					// Check for errors
					if (FullPath.matches("(?i).*\\.(nulo|null)$")) {
						log.ErrorFound(logWrite, ParsedText);
					}
					// If path does not exists, create-it
					else if (!documentServer_Directory.exists()) {

						boolean created = false;

						// Try to create dirs
						try {
							documentServer_Directory.mkdirs();
							created = true;
						}

						// Log access error
						catch (SecurityException SE) {
							logWrite.write("Não foi possivel acessar o diretório");
							logWrite.newLine();
						}

						// Log creation success
						if (created) {
							logWrite.write("Diretorio criado com sucesso");
							logWrite.newLine();
						}
					}

					// Get source file
					File source = new File(file_path);

					try {
						// If all identification is correct, move file renaming and delet from source
						if (!FullPath.contains("null") && source.exists()) {

							File destination = new File(FullPath);
							copyFile(source, destination);

							System.out.println("File was sucessfull " + source);
							source.delete();
							iteracaoComSucesso++;
						}

						// If is SPED, try to move extra files with it
						if (ExtraSpedFiles) {

							File sourceREC = new File(file_path.replaceFirst("[.][^.]+$", ".REC"));
							File destinationREC = new File(FullPath.replaceFirst("[.][^.]+$", ".REC"));

							logWrite.write("Destino: " + destinationREC);
							logWrite.newLine();

							copyFile(sourceREC, destinationREC);
							sourceREC.delete();

							iteracaoComSucesso++;
							iteracaoTotal++;
						}
					} catch (Exception e55) {
						e55.printStackTrace();
					}
					logWrite.write(
							"------------------------------------------------------------------------------------------------------------------------------------------------------------------");
					iteracaoTotal++;
					source = null;

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		logWrite.close();
		logCreate.close();

		// Write Log header data
		if (source_files.length > 0 && iteracaoTotal > 0)
			log.statystics(iteracaoComSucesso, iteracaoComErros, iteracaoTotal, logPath);

		// Delete generated log
		else {
			File log = new File(logPath);
			log.delete();
		}
		System.out.println("END OF RUN");
	}

	public static String mountPath(String System, Boolean checkPath, Object... obj) {

		String path = null;
		String bar = System == "win" ? "\\" : "//";

		path = (String) obj[0];
		for (int i = 1; i < obj.length; i++)
			path += bar + (String) obj[i];

		// Check if path is not in use
		if (checkPath)
			path = checkIfExists(path);

		return path;
	}

	private static String filial(String ArquivoProcessado, String pathtosrv, String EmpresaNome) throws IOException {

		if (ArquivoProcessado.contains("LUIZ ALBERTO PELLIN")) {
			EmpresaNome = EmpresaNome + "\\1 MATRIZ";
		}
		if (ArquivoProcessado.contains("12.883.198/0001-88")) {
			EmpresaNome = EmpresaNome + "\\CONFIDENZA NOVO";
		}
		if (ArquivoProcessado.contains("05.242.070/0001-70")) {
			EmpresaNome = EmpresaNome + "\\CONIDENZA VELHO";
		}

		return EmpresaNome;
	}

	public static void copyFile(File sourceFile, File destFile) throws IOException {
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

		if (TipoFile == null)
			return null;

		String anoDocumento = null;
		String Datadodocumento = null;

		String[] mes0A = { null, "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12" };
		String[] mes0B = { null, "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12" };
//		String[] mes0C = { null, "Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez" };
		String[] mes0D = { null, "Janeiro", "Fevereiro", "Marco", "Abril", "Maio", "Junho", "Julho", "Agosto",
				"Setembro", "Outubro", "Novembro", "Dezembro" };

		String Formatador_1;
		String Formatador_2;
		String dateFormatter;

		for (int ano = Year.now().getValue(); ano > 1950; ano--) {
			for (int mesI0 = 1; mesI0 <= 12; mesI0++) {
				for (int dia = 1; dia <= 31; dia++) {

					String searchOnDoc = null;

					switch (TipoFile) {
					case "DESTDA":
						Formatador_1 = "Mes Referencia %s/%d";
						dateFormatter = "%s.%d";

						searchOnDoc = createString(Formatador_1, mes0A[mesI0], Integer.valueOf(ano));

						if (parsedText.contains(searchOnDoc)) {
							anoDocumento = String.valueOf(ano);
							Datadodocumento = date.ConvertDate(dateFormatter, ano, mes0A, mesI0);
						}
						break;

					case "DSN":
						Formatador_1 = "Principal\r\n%s/%d";
						Formatador_2 = "Exigivel\r\n%s/%d";
						dateFormatter = "%s.%d";

						if (parsedText.contains(createString(Formatador_1, mes0A[mesI0], Integer.valueOf(ano)))) {
							anoDocumento = String.valueOf(ano);
							Datadodocumento = date.ConvertDate(dateFormatter, ano, mes0A, mesI0);
						} else if (parsedText
								.contains(createString(Formatador_2, mes0A[mesI0], Integer.valueOf(ano)))) {
							anoDocumento = String.valueOf(ano);
							Datadodocumento = date.ConvertDate(dateFormatter, ano, mes0A, mesI0);
						}
						break;

					case "NFG":
						Formatador_1 = "periodo de 01/%s/%d a %d/%s/%d";
						dateFormatter = "%s.%d";

						searchOnDoc = createString(Formatador_1, mes0A[mesI0], Integer.valueOf(ano),
								Integer.valueOf(dia), mes0A[mesI0], Integer.valueOf(ano));

						if (parsedText.contains(searchOnDoc)) {
							anoDocumento = String.valueOf(ano);
							Datadodocumento = date.ConvertDate(dateFormatter, ano, mes0A, mesI0);
						}

						break;

					case "SPED CONTRIBUICOES":
						Formatador_1 = "%d/%s/%d";
						Formatador_2 = "%d%s%d";
						dateFormatter = "%s.%d";

						if (parsedText.contains(
								createString(Formatador_1, Integer.valueOf(dia), mes0A[mesI0], Integer.valueOf(ano)))) {
							anoDocumento = String.valueOf(ano);
							Datadodocumento = date.ConvertDate(dateFormatter, ano, mes0A, mesI0);
						} else if (parsedText.contains(
								createString(Formatador_2, Integer.valueOf(dia), mes0A[mesI0], Integer.valueOf(ano)))) {
							anoDocumento = String.valueOf(ano);
							Datadodocumento = date.ConvertDate(dateFormatter, ano, mes0A, mesI0);
						}
						break;

					case "RDI":
						Formatador_1 = "Periodo: %s/%d";
						dateFormatter = "%s.%d";

						searchOnDoc = createString(Formatador_1, mes0D[mesI0], Integer.valueOf(ano));

						if (parsedText.contains(searchOnDoc)) {
							anoDocumento = String.valueOf(ano);
							Datadodocumento = date.ConvertDate(dateFormatter, ano, mes0A, mesI0);
						}
						break;

					case "DEISS":
						Formatador_1 = "Ano e Mes de Referencia:    %d/  %s";
						dateFormatter = "%s.%d";

						searchOnDoc = createString(Formatador_1, Integer.valueOf(ano), mes0B[mesI0]);

						if (parsedText.contains(searchOnDoc)) {
							anoDocumento = String.valueOf(ano);
							Datadodocumento = date.ConvertDate(dateFormatter, ano, mes0A, mesI0);
						}
						break;

					case "DMS":
						Formatador_1 = "%s/%d\r\nSem Movimento de ISS";
						Formatador_2 = "Contribuinte:\r\n%s/%d";
						dateFormatter = "%s.%d";

						if (parsedText.contains(createString(Formatador_1, mes0A[mesI0], Integer.valueOf(ano)))) {
							anoDocumento = String.valueOf(ano);
							Datadodocumento = date.ConvertDate(dateFormatter, ano, mes0A, mesI0);
						}

						else if (parsedText.contains(createString(Formatador_2, mes0A[mesI0], Integer.valueOf(ano)))) {
							anoDocumento = String.valueOf(ano);
							Datadodocumento = date.ConvertDate(dateFormatter, ano, mes0A, mesI0);

						}
						break;

					case "OPCAO SIMPLES":
						Formatador_1 = "Ano-calendario: %d";
						dateFormatter = "%d";

						searchOnDoc = createString(Formatador_1, Integer.valueOf(ano));

						if (parsedText.contains(searchOnDoc)) {
							anoDocumento = String.valueOf(ano);
							Datadodocumento = date.ConvertDate(dateFormatter, ano);
						}
						break;

					case "DEFIS":
						// Periodo abrangido pela Declaracao: 01/01/2019 a 26/06/2019
						// Periodo abrangido pela Declaracao: 01/04/2018 a 31/12/2018
						Formatador_1 = "Periodo abrangido pela Declaracao: 01/%s/%d a %d";
						dateFormatter = "%d";

						searchOnDoc = createString(Formatador_1, mes0A[mesI0], Integer.valueOf(ano),
								Integer.valueOf(dia));

						if (parsedText.contains(searchOnDoc)) {
							anoDocumento = String.valueOf(ano);
							Datadodocumento = date.ConvertDate(dateFormatter, ano);
						}

						break;

					case "DME":
						// Ano e Mes de Referencia: 2019/ 5
						Formatador_1 = "Ano e Mes de Referencia:    %d/  %d";
						dateFormatter = "%d";
						searchOnDoc = createString(Formatador_1, Integer.valueOf(ano), Integer.valueOf(mesI0));

						if (parsedText.contains(searchOnDoc)) {
							anoDocumento = String.valueOf(ano);
							Datadodocumento = date.ConvertDate(dateFormatter, ano);
						}
						break;

					default:

						System.out.println("Identificador de tipo de arquivo nao encontrado.");
						return new String[] { (String) null, (String) null };

					}

				}
			}
		}
		return new String[] { (String) anoDocumento, (String) Datadodocumento };
	}

	// Create String to be searched on document
	static String createString(String formatador, Object... obj) {
		return (String.format(formatador, obj));
	}

	public static String checkIfExists(String Destination) {
		int fileNo = 0;
		String destinationVerifier = Destination;
		File DestTest = new File(Destination);

		// Increase index number untill is not overything another file
		while (DestTest.exists()) {
			fileNo++;

			int index = Destination.lastIndexOf(".");
			destinationVerifier = Destination.substring(0, index) + "(" + fileNo + ")" + Destination.substring(index);

			// Check if this file exists
			DestTest = new File(destinationVerifier);
		}

		return destinationVerifier;
	}

}