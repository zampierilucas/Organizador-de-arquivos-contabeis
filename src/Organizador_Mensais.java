import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Organizador_Mensais {
	public static void main(String[] args) throws IOException {

		System.out.println("Organizador de arquivos");
		
		// Windows path
		String pathtoread = "\\\\10.1.20.30\\Organizador";
		String PathToSrv = "\\\\10.1.20.13\\setores\\GERAL\\Documentos empresariais\\Documentos";

		// Log path
		String logDocument = "log\\log_" + date.now() + ".txt";
		String logPath = IO.mountPath("win", false, pathtoread, logDocument);

		// Log R/W
		FileWriter logCreate = new FileWriter(logPath);
		BufferedWriter logWrite = new BufferedWriter(logCreate);

		// Get source files
		File source_directory = new File(pathtoread);
		File[] source_files = source_directory.listFiles();

		// Add log Header
		log.Header(logWrite, date.now());

		// Init log statistics
		int LOG_Success = 0;
		int LOG_Error = 0;

		// Main loop
		for (int indexOfFiles = 0; indexOfFiles < source_files.length; indexOfFiles++) {

			// Run variables
			String FullPath = null;
			String CompanyCNPJ = null;
			String SRVCompanyPath = null;
			String ParsedText = "";
			String TipeOfFile = null;
			boolean ExtraSpedFiles = false;

			// Create source aliases
			String file_name = source_files[indexOfFiles].getName();
			String file_path = source_files[indexOfFiles].getAbsolutePath();


			if (file_name.matches("(?i).*\\.(pdf|PDF|txt|TXT)$")) {
				// Open Current file
				File CurrentFile = new File(file_path);

				try {
					// Is TXT
					if (file_name.matches("(?i).*\\.(txt|TXT)$")) {
						BufferedReader br = new BufferedReader(new FileReader(source_files[indexOfFiles]));
						int counter = 0;
						while (counter < 10) {
							ParsedText = ParsedText + br.readLine() + System.lineSeparator();
							counter++;
						}
						br.close();
						counter = 0;
						// Is PDF
					} else {

						ParsedText = data.PDFToString(CurrentFile);
						ParsedText = data.normalize(ParsedText);

					}

					
					// Get tipe of of Document
					TipeOfFile = data.fileType(ParsedText);
					if (TipeOfFile == null) {
						log.ErrorFound(logWrite, file_name, ParsedText, "Tipo de arquivo não encontrado");
						LOG_Error++;
						continue;
					}
					
					// Get Day, Mount and Year of Document
					date CurrentDocDate = data.DocumentPeriod(ParsedText, TipeOfFile);
					if (CurrentDocDate.year() == null) {
						log.ErrorFound(logWrite, file_name, ParsedText, "Data do ducumento não encontrada");
						LOG_Error++;
						continue;
					}
					
					// Get company CNPJ from Document
					CompanyCNPJ = data.CompanyCNPJ(ParsedText);	
					if (CompanyCNPJ == null) {
						log.ErrorFound(logWrite, file_name, ParsedText, "CNPJ não encontrado no arquivo");
						LOG_Error++;
						continue;
					}
					
					// Get Path based on file CNPJ
					SRVCompanyPath = data.ConvertCNPJtoPath(CompanyCNPJ, PathToSrv);
					if (SRVCompanyPath == null) {
						log.ErrorFound(logWrite, file_name, ParsedText, "Caminho não encontrado no servidor");
						LOG_Error++;
						continue;
					}
					
					// If is a TXT file
					if (file_name.matches("(?i).*\\.(txt|TXT)$")) {

						// Get txt path
						FullPath = IO.mountPath("win", true, SRVCompanyPath, TipeOfFile, CurrentDocDate.year(), file_name);

						// Generate REC path
						File sourceREC = new File(file_path.replaceFirst("[.][^.]+$", ".REC"));

						// Check if direcory contains a rec file
						if (sourceREC.exists())
							ExtraSpedFiles = true;
					}

					// If is a pdf File
					else if (file_name.matches("(?i).*\\.(pdf|PDF)$")) {

						String DocName = TipeOfFile + " " + CurrentDocDate.FullDate() + ".pdf";

						// Create path to document server
						FullPath = IO.mountPath("win", true, SRVCompanyPath, TipeOfFile, CurrentDocDate.year(), DocName);
					}
					
					// Add to log current document information
					System.out.println("company " + SRVCompanyPath);
					log.File(logWrite, SRVCompanyPath.replaceAll(".+?(?=-)..",""), TipeOfFile, file_path, FullPath);

					// Create folder path
					String directoryPath = IO.mountPath("win", false, SRVCompanyPath, TipeOfFile, CurrentDocDate.year());

					File documentServerPath = new File(directoryPath);

					// Check for errors
					if (FullPath.matches("(?i).*\\.(nulo|null)$")) {
						log.ErrorFound(logWrite, file_name, ParsedText, null);
						LOG_Error++;
					}
					
					// If path does not exists, create-it
					else if (!documentServerPath.exists()) {

						// Try to create dirs
						try {
							documentServerPath.mkdirs();
							logWrite.write("Diretorio criado com sucesso");
							logWrite.newLine();
						}

						// Log access error
						catch (SecurityException SE) {
							log.ErrorFound(logWrite, file_name, ParsedText, "Não foi possivel acessar o diretório");
							LOG_Error++;
							continue;
						}
					}

					// Get source file
					File source = new File(file_path);

					try {
						// If all identification is correct, move file renaming and deleting from source
						if (!FullPath.contains("null") && source.exists()) {

							File destination = new File(FullPath);
							IO.copyFile(source, destination);

							source.delete();
							LOG_Success++;
						}
						
						else {
							LOG_Error++;
						}

						// If is SPED, try to move extra files with it
						if (ExtraSpedFiles) {

							File sourceREC = new File(file_path.replaceFirst("[.][^.]+$", ".REC"));
							File destinationREC = new File(FullPath.replaceFirst("[.][^.]+$", ".REC"));

							logWrite.write("Destino: " + destinationREC);
							logWrite.newLine();

							IO.copyFile(sourceREC, destinationREC);
							sourceREC.delete();

							LOG_Success++;
						}
					} catch (Exception e55) {
						e55.printStackTrace();
						LOG_Error++;
					}
					logWrite.write(
							"------------------------------------------------------------------------------------------------------------------------------------------------------------------");
					source = null;

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		logWrite.close();
		logCreate.close();

		// Write Log header data
		if (source_files.length > 0 && (LOG_Success + LOG_Error) > 0)
			log.statystics(LOG_Success, LOG_Error, logPath);

		// Delete generated log
		else {
			File log = new File(logPath);
			log.delete();
		}
		System.out.println("END OF RUN");
	}

}