import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

public class log {
	public static void Header(BufferedWriter bw, String data_atual) throws IOException {

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

	public static void File(BufferedWriter bw, String EmpresaNome, String TipoFile, String SourcePath, String DestPath)
			throws IOException {

		bw.newLine();
		bw.write("Empresa: " + EmpresaNome);
		bw.newLine();
		bw.write("Tipo: " + TipoFile);
		bw.newLine();
		bw.write("Origem:  " + SourcePath);
		bw.newLine();
		bw.write("Destino: " + DestPath);
		bw.newLine();
	}

	public static void ErrorFound(BufferedWriter bw, String DOCPath,  String parsedText, String ErrorMsg) throws IOException {

		bw.newLine();
		bw.write("Erro encontrado no arquivo: " + DOCPath);
		bw.newLine();
		
		if (ErrorMsg != null) {
			bw.write("Descrição do erro: " + ErrorMsg);
			bw.newLine();
		}

		bw.write("Conteudo do arquivo:");
		bw.newLine();
		bw.newLine();
		bw.write(parsedText);
		bw.newLine();
		bw.write("Fim do Erro no doc.");
		bw.newLine();
		bw.write("-------------------------------------------------------------------------------");
	}

	public static void statystics(int Success, int Errors, String logPath) throws IOException {
		String input = "";
		int iteracaoTotal = Success + Errors;
		
		BufferedReader readLog = new BufferedReader(new FileReader(logPath));
		String line = null;
		
		while ((line = readLog.readLine()) != null)
			input = input + line + System.lineSeparator();
		
		int min = iteracaoTotal * 20 / 60;
		int seg = iteracaoTotal * 20 - min * 60;
		String TimeSaved = min + ":" + seg;

		Errors = iteracaoTotal - Success;

		String LogSuccess = Integer.toString(Success);
		String LogError = Integer.toString(Errors);
		String LogTotal = Integer.toString(iteracaoTotal);

		if (iteracaoTotal == 0) {
			FileOutputStream out = new FileOutputStream(logPath);
			readLog.close();
			out.write(input.getBytes());
			out.close();
			System.exit(0);
		} else if ((min == 0) && (seg > 0)) {
			input = input.replace("L+2", LogTotal);
			input = input.replace("L+3", LogSuccess);
			input = input.replace("L+4", LogError);
			input = input.replace("L+5 minutos", TimeSaved + " segundos");
		} else {
			input = input.replace("L+2", LogTotal);
			input = input.replace("L+3", LogSuccess);
			input = input.replace("L+4", LogError);
			input = input.replace("L+5", TimeSaved);
		}
		FileOutputStream out = new FileOutputStream(logPath);
		out.write(input.getBytes());
		out.close();
		readLog.close();
		System.out.println(input);
	}


}
