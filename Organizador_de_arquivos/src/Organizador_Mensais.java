import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.text.Normalizer;
import java.time.Month;
import java.time.Year;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.SimpleDateFormat;
import com.ibm.icu.util.Calendar;

public class Organizador_Mensais {

	public static void EscreveTopoLog(BufferedWriter bw, Date date, DateFormat dateFormat) throws IOException {
		bw.write("Data: " + dateFormat.format(date));
		bw.newLine(); // Linha dois - empresas processadas
		bw.write("Arquivos processadas: L+2");
		bw.newLine(); // Linha tres - com sucesso
		bw.write("Arquivos processados com sucesso: L+3");
		bw.newLine(); // linha quatro - com erros
		bw.write("Arquivos processados com erros: L+4");
		bw.newLine(); // linha quatro - economizado
		bw.write("Tempo poupado: L+5 minutos");
		bw.newLine();
		bw.newLine();
	}

	public static String[] IdenData(String TipoFile) {
		String ifFormatter = null, DataF = null;

		if (TipoFile == "DESTDA") {
			ifFormatter = "Mes Referencia " + "%s/%d";
			DataF = "%s.%d";
		} else if (TipoFile == "DSN") {
			ifFormatter = "Principal" + "\r\n" + "%s/%d";
			DataF = "%s.%d";
		} else if (TipoFile == "DEISS") {
			ifFormatter = "Ano e Mes de Referencia:    " + "%d/  %s";
			DataF = "%s.%d";
		} else if (TipoFile == "DEFIS") {
			ifFormatter = "Periodo abrangido pela Declaracao: 01/" + "%s/%d" + " a 31/12/%d";
			DataF = "%d";
		} else if (TipoFile == "RDI") {
			ifFormatter = "Periodo: " + "%s/%d";
			DataF = "%s.%d";
		} else if (TipoFile == "NFG") {
			ifFormatter = "periodo de 01/%s/%d a %d/%s/%d";
			DataF = "%s.%d";
		} else if (TipoFile == "OPÇÃO SIMPLES") {
			ifFormatter = "Ano-calendario: %d";
			DataF = "%d";
		} else if (TipoFile == "SPED CONTRIBUICOES") {
			ifFormatter = "01/%s/%d a %d/%s/%d";
			DataF = "%s.%d";
		}

		/*
		 * } else if (TipoFile == "DMS") { Formater = "Valor do Faturamento:" + "\r\n" +
		 * mes0A[mesI] + "%s/%d";
		 */

		// mover todos os formatdores do detecta data pra ca

		return new String[] { ifFormatter, DataF };
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

	public static String tipodearquivo(String parsedText, String TipoFile) {
		if (parsedText.contains("DeSTDA")) {
			TipoFile = ("DESTDA");
		} else if (parsedText.contains("PGDAS-D")) {
			TipoFile = ("DSN");
		} else if (parsedText.contains("DEISS")) {
			TipoFile = ("DEISS");
		} else if (parsedText.contains("DEFIS")) {
			TipoFile = ("DEFIS");
		} else if (parsedText.contains("Valor total do ICMS a recolher")) {
			TipoFile = ("EFD ICMS IPI");
		} else if (parsedText.contains("Recibo de declaracao de ISS")) {
			TipoFile = ("RDI");
		} else if (parsedText.contains("NFG")) {
			TipoFile = ("NFG");
		} else if (parsedText.contains("DMS")) {
			TipoFile = ("DMS");
		} else if (parsedText.contains("Opcao pelo Regime de Apuracao de Receitas")) {
			TipoFile = ("OPÇÃO SIMPLES");
		} else if (parsedText.contains("RECIBO DE ENTREGA DE ESCRITURACAO FISCAL DIGITAL - CONTRIBUICOES")) {
			TipoFile = ("SPED CONTRIBUICOES");
		} else {
		}
		return TipoFile;
	}

	public static void estatisticas(int iteracaoComSucesso, int iteracaoComErros, int iteracaoTotal, String logPath)
			throws IOException {
		File log = new File(logPath);
		String Sucesso, Erros, Total, iteracaoMin, line, input = "";
		// ================Escrever estatisticas no log========================//

		BufferedReader readLog = new BufferedReader(new FileReader(logPath));

		while ((line = readLog.readLine()) != null) {
			input += line + System.lineSeparator();
		}

		int min = (iteracaoTotal * 20) / 60;
		int seg = (iteracaoTotal * 20) - (min * 60);
		iteracaoMin = (min + ":" + seg);

		iteracaoComErros = iteracaoTotal - iteracaoComSucesso;

		Sucesso = Integer.toString(iteracaoComSucesso);
		Erros = Integer.toString(iteracaoComErros);
		Total = Integer.toString(iteracaoTotal);

		if (iteracaoTotal == 0) {
			readLog.close();
			FileOutputStream out = new FileOutputStream(logPath);
			out.write(input.getBytes());
			out.close();
			log.delete();
			System.exit(0);
		} else if (min == 0 && seg > 0) {
			input = input.replace("L+2", Total);// Total
			input = input.replace("L+3", Sucesso);// Sucesso
			input = input.replace("L+4", Erros);// Erros
			input = input.replace("L+5 minutos", iteracaoMin + " segundos");// Seg
		} else {
			input = input.replace("L+2", Total);// Total
			input = input.replace("L+3", Sucesso);// Sucesso
			input = input.replace("L+4", Erros);// Erros
			input = input.replace("L+5", iteracaoMin);// Minutos
		}

		
		FileOutputStream out = new FileOutputStream(logPath);
		out.write(input.getBytes());
		out.close();
		readLog.close();
		System.out.println(input);
	}

	public static void main(String args[]) throws IOException {

		String FullPath;
		Date date = new Date();
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
		String pathtoread = "\\\\10.1.1.135\\ORGANIZADOR_DE_ARQUIVOS\\"; // Caminho a ser lido.
		String pathtosrv = "\\\\10.1.20.13\\setores\\GERAL\\Documentos empresariais\\Documentos\\";
		String logPath = (pathtoread + "log\\" + "log " + dateFormat.format(date) + ".txt");
		String EmpresaCaminho = null;
		String Datadodocumento = null;
		BufferedWriter bw = null;
		FileWriter fw = null;
		PDFParser parser = null;
		PDDocument pdDoc = null;
		COSDocument cosDoc = null;
		String TipoFile = null;
		String parsedText;
		fw = new FileWriter(logPath);
		bw = new BufferedWriter(fw);
		String EmpresaNome = null;
		int iteracaoComSucesso = 0, iteracaoComErros = 0, iteracaoTotal = 0;
		String anoDocumento = null;
		PDFTextStripper pdfStripper;

		// Cria Array de pdfs da pasta de leitura //
		File folder = new File(pathtoread);
		File[] listOfFiles = folder.listFiles();

		EscreveTopoLog(bw, date, dateFormat);

		// Processa documento por documento na pasta do Organizar //
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile() && listOfFiles[i].getName().matches("(?i).*\\.(pdf|PDF)$")) {

				// Converte pdf para txt para analise //

				String fileName = listOfFiles[i].getAbsolutePath();
				File file = new File(fileName);
				try {
					parser = new PDFParser(new FileInputStream(file));
					parser.parse();
					cosDoc = parser.getDocument();
					pdfStripper = new PDFTextStripper();
					pdDoc = new PDDocument(cosDoc);
					parsedText = pdfStripper.getText(pdDoc);
					parsedText = Normalizer.normalize(parsedText, Normalizer.Form.NFD);
					parsedText = parsedText.replaceAll("[^\\p{ASCII}]", "");
					parsedText = parsedText.replaceAll("&", "E");

					// ======================================Classificar=======================================================//
					TipoFile = tipodearquivo(parsedText, TipoFile);

					if (TipoFile == null) {
						bw.write("Tipo não encontrado:");
						bw.newLine();
						bw.write(parsedText);
						bw.write("-------------------end--of--bug-----------------------");
					}

					// ================Roda Exceçoes============//
					parsedText = process(parsedText, TipoFile);
					// pega mea e ano do documento
					String[] ret = date(parsedText, TipoFile, anoDocumento);

					anoDocumento = ret[0];
					Datadodocumento = ret[1];

					// =======Descobrir nome da empresa=========//
					EmpresaNome = EmpresaGetName(parsedText, pathtosrv, EmpresaNome);

					EmpresaCaminho = filial(parsedText, pathtosrv, EmpresaCaminho);

					FullPath = (pathtosrv + "" + EmpresaNome + "\\" + TipoFile + "\\" + anoDocumento + "\\" + TipoFile
							+ " " + Datadodocumento + ".pdf");

					// ===================================log==================================================================//
					// =================Nome empresa e origem doc========================//
					bw.newLine();
					bw.write("Empresa: " + EmpresaNome);
					bw.newLine();
					bw.write("Origem:  " + listOfFiles[i].getAbsolutePath());
					bw.newLine();
					bw.write("Destino: " + FullPath);
					bw.newLine();

					// =================================Vericiar se existe e criar
					// diretório===================================//
					File PathWOFileF = new File(
							pathtosrv + EmpresaCaminho + "\\" + TipoFile + "\\" + anoDocumento + "\\");

					if (FullPath.contains("null")) {
						ErroArquivo(bw, parsedText);
					} else if (!PathWOFileF.exists()) {
						boolean result = false;
						try {
							PathWOFileF.mkdirs();
							result = true;
						} catch (SecurityException se) {
						}
						if (result == true) {
							bw.write("Diretorio criado com sucesso");
							bw.newLine();
						}
					}

					String filegravar = (FullPath);
					File destination = new File(filegravar);

					// ===================================Função para não
					// sobrescrever=========================================//
					int fileNo = 0;
					while (destination.exists()) {
						fileNo++;
						String ifExists = filegravar.replaceAll(".pdf", "(" + fileNo + ").pdf");
						destination = new File(ifExists);
					}
				//	System.out.println(listOfFiles[i] + ".REC");

					File source = new File(listOfFiles[i].getAbsolutePath());

					/*
					 * if (!FullPath.contains("null")) { copyFile(source, destination);
					 * iteracaoComSucesso++; source.delete(); }
					 */

					bw.write(
							"------------------------------------------------------------------------------------------------------------------------------------------------------------------");
					iteracaoTotal++;

					if (parsedText != null)
						parsedText = null;
					if (cosDoc != null)
						pdDoc.close();
					if (cosDoc != null)
						cosDoc.close();
					if (pdDoc != null)
						pdDoc.close();
					if (cosDoc != null)
						cosDoc.close();
					EmpresaNome = null;
					anoDocumento = null;
					TipoFile = null;
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (listOfFiles[i].isFile() && listOfFiles[i].getName().matches("(?i).*\\.(txt|TXT)$")) {

			}
		} // close for

		// ================Fecha log para escrever
		// estatisticas========================//
		bw.close();
		fw.close();

		estatisticas(iteracaoComSucesso, iteracaoComErros, iteracaoTotal, logPath);

	}// close main

	// Função para descobrir nome da empresa //
	private static String EmpresaGetName(String ArquivoProcessado, String pathtosrv, String EmpresaNome)
			throws IOException {
		int i = 0;

		File folder = new File(pathtosrv);
		File[] listaEmpresasSRV = folder.listFiles();

		for (i = 0; i < listaEmpresasSRV.length; i++) {
			if (ArquivoProcessado.contains(listaEmpresasSRV[i].getName())) {
				EmpresaNome = listaEmpresasSRV[i].getName();
			}
		}
		return EmpresaNome;
	}

	private static String filial(String ArquivoProcessado, String pathtosrv, String EmpresaCaminho) throws IOException {
		if (ArquivoProcessado.contains("LUIZ ALBERTO PELLIN")) {
			EmpresaCaminho = EmpresaCaminho + "\\1 MATRIZ";
		}
		if (ArquivoProcessado.contains("12.883.198/0001-88")) {
			EmpresaCaminho = EmpresaCaminho + "\\CONFIDENZA NOVO";
		}
		if (ArquivoProcessado.contains("05.242.070/0001-70")) {
			EmpresaCaminho = EmpresaCaminho + "\\CONFIDENZA VELHO";
		} else {
		}
		return EmpresaCaminho;
	}

	// ========================Move arquivo para a pasta da empresa
	// correta(sometimes)=============================//
	@SuppressWarnings("resource")
	public static void copyFile(File sourceFile, File destFile) throws IOException {
		if (!destFile.exists()) {
			destFile.createNewFile();
		}

		FileChannel source = null;
		FileChannel destination = null;
		try {
			source = new RandomAccessFile(sourceFile, "rw").getChannel();
			destination = new RandomAccessFile(destFile, "rw").getChannel();
			long position = 0;
			long count = source.size();
			source.transferTo(position, count, destination);
		} finally {
			if (source != null) {
				source.close();
			}
			if (destination != null) {
				destination.close();
			}
		}
	}

	// ==========resolve as exesões gramaticas cadastradas e encontra o periodo no
	// documento=======================//
	public static String process(String parsedText, String TipoFile) {
		try {
			Properties prop = new Properties();
			final FileInputStream exeptions = new FileInputStream(
					"\\\\10.1.1.135\\excecoesOrganizador\\" + TipoFile + ".properties");
			prop.load(exeptions);
			for (int c = 1; c <= (prop.size() / 2); c++) {
				parsedText = parsedText.replaceAll("" + prop.getProperty(c + "A") + "",
						"" + prop.getProperty(c + "B") + "");
			}
			exeptions.close();
		} catch (IOException ex) {
		}
		return parsedText;
	}

	public static String[] date(String parsedText, String TipoFile, String anoDocumento) {

		int mesI, ano, dia, tentativa = 0;

		Calendar calendar = Calendar.getInstance();
		int anoAtual = Year.now().getValue();
		int mesAtual = calendar.get(Calendar.MONTH);

		Date today = new Date();
		calendar.setTime(today);
		calendar.set(Calendar.MONTH, mesAtual);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		calendar.add(Calendar.DATE, -1);

		Date lastDayOfMonth = calendar.getTime();
		DateFormat sdf = new SimpleDateFormat("dd");

		String Datadodocumento = null;

		String[] mes0A = new String[] { "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12" };
		String[] mes0B = new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12" };
		String[] mes0C = new String[] { "Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov","Dez" };

		String[] retF = IdenData(TipoFile);
		String ifformatter = retF[0];
		String dateFormatter = retF[1];
		
		search: for (ano = 1950; ano < 2100; ano++) {
			for (mesI = 0; mesI < 12; mesI++) {
				for (dia = 0; dia < 32; dia++) {
					if (tentativa < 1) {
						ano = anoAtual;
						mesI = mesAtual - 1;
						dia = Integer.parseInt(sdf.format(lastDayOfMonth));
						tentativa = 1;
					}
					if (TipoFile == "DESTDA") {
						if (parsedText.contains(String.format(ifformatter, mes0A[mesI], ano))) {
							anoDocumento = String.valueOf(ano);
							Datadodocumento = String.format(dateFormatter, mes0A[mesI], ano);
							break search;
						}
					} else if (TipoFile == "DSN") {
						if (parsedText.contains(String.format(ifformatter, mes0A[mesI], ano))) {
							anoDocumento = String.valueOf(ano);
							Datadodocumento = String.format(dateFormatter, mes0A[mesI], ano);
							break search;
						}

					} else if (TipoFile == "NFG") {
						if (parsedText.contains(String.format(ifformatter, mes0A[mesI], ano, dia, mes0A[mesI], ano))) {
							anoDocumento = String.valueOf(ano);
							Datadodocumento = String.format(dateFormatter, mes0A[mesI], ano);
							break search;
						}

					} else if (TipoFile == "SPED CONTRIBUICOES") {
						if (parsedText.contains(String.format(ifformatter, mes0A[mesI], ano, dia, mes0A[mesI], ano))) {
							anoDocumento = String.valueOf(ano);
							Datadodocumento = String.format(dateFormatter, mes0A[mesI], ano);
							break search;
						}
					} else if (TipoFile == "RDI") {
						if (parsedText.contains(String.format(ifformatter, mes0C[mesI], ano))) {
							anoDocumento = String.valueOf(ano);
							Datadodocumento = String.format(dateFormatter, mes0A[mesI], ano);
							break search;
						}
					} else if (TipoFile == "DEISS") {
						if (parsedText.contains(String.format(ifformatter, ano, mes0B[mesI]))) {
							anoDocumento = String.valueOf(ano);
							Datadodocumento = String.format(dateFormatter, mes0A[mesI], ano);
							break search;
						}
					} else if (TipoFile == "OPÇÃO SIMPLES") {
						if (parsedText.contains(String.format(ifformatter, ano))) {
							anoDocumento = String.valueOf(ano);
							Datadodocumento = String.format(dateFormatter, ano);
							break search;
						}
					}

					else if (TipoFile == "DEFIS") {
						if (parsedText.contains(String.format(ifformatter, mes0A[mesI], ano, ano))) {
							anoDocumento = String.valueOf(ano);
							Datadodocumento = String.format(dateFormatter, ano);
							break search;
						}
					} else if (TipoFile == "DMS") {
						if (parsedText.contains("Valor do Faturamento:" + "\r\n" + mes0A[mesI] + "/" + ano)) {
							anoDocumento = String.valueOf(ano);
							Datadodocumento = (mes0A[mesI] + "." + ano);
							break search;
						} else if (parsedText.contains("Contribuinte:" + "\r\n" + mes0A[mesI] + "/" + ano)) {
							anoDocumento = String.valueOf(ano);
							Datadodocumento = String.format(dateFormatter, mes0A[mesI], ano);
							break search;
						}
					}

					if (tentativa == 1) {
						ano = 1950;
						dia = 27;
						mesI = 0;
						tentativa = 2;
					}
				}
			}
		}
		return new String[] { anoDocumento, Datadodocumento };
	}

	class MyFormatter extends Formatter {
		@Override
		public String format(LogRecord record) {
			StringBuilder builder = new StringBuilder();
			builder.append(record.getLevel() + ": ");
			builder.append(formatMessage(record));
			builder.append(System.lineSeparator());
			return builder.toString();
		}
	}
}
