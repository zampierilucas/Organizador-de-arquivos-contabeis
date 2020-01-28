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

	public static void ErrorFound(BufferedWriter bw, String parsedText) throws IOException {

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

	public static void statystics(int iteracaoComSucesso, int iteracaoComErros, int iteracaoTotal, String logPath)
			throws IOException {
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
		} else if ((min == 0) && (seg > 0)) {
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

}
