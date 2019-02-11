import com.ibm.icu.util.Calendar;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.time.Year;
import java.util.Date;
import java.util.Properties;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

public class Organizador_Mensais
{
  public static final String DATE_FORMAT_NOW = "yyyyMMdd HHmm";
  
  public Organizador_Mensais() {}
  
  public static void EscreveTopoLog(BufferedWriter bw, String data_atual) throws IOException
  {
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
  
  public static String[] IdenData(String TipoFile) {
    String ifformatter0 = null;String ifformatter1 = null;String DataF = null;
    
    if (TipoFile == "DESTDA") {
      ifformatter0 = "Mes Referencia %s/%d";
      DataF = "%s.%d";
    } else if (TipoFile == "DSN") {
      ifformatter0 = "Principal\r\n%s/%d";
      DataF = "%s.%d";
    } else if (TipoFile == "DEISS") {
      ifformatter0 = "Ano e Mes de Referencia:    %d/  %s";
      DataF = "%s.%d";
    } else if (TipoFile == "DEFIS") {
      ifformatter0 = "Periodo abrangido pela Declaracao: 01/%s/%d a %d/12/%d";
      DataF = "%d";
    } else if (TipoFile == "RDI") {
      ifformatter0 = "Periodo: %s/%d";
      DataF = "%s.%d";
    } else if (TipoFile == "NFG") {
      ifformatter0 = "periodo de 01/%s/%d a %d/%s/%d";
      DataF = "%s.%d";
    } else if (TipoFile == "OPCAO SIMPLES") {
      ifformatter0 = "Ano-calendario: %d";
      DataF = "%d";
    } else if (TipoFile == "SPED CONTRIBUICOES") {
      ifformatter0 = "%d/%s/%d";
      ifformatter1 = "%d%s%d";
      DataF = "%s.%d";
    } else if (TipoFile == "DMS") {
      ifformatter0 = "Valor do Faturamento:\r\n%s/%d";
      ifformatter1 = "Contribuinte:\r\n%s/%d";
      DataF = "%s.%d";
    }
    


    return new String[] { ifformatter0, ifformatter1, DataF };
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
  
  public static String tipodearquivo(String parsedText, String TipoFile)
  {
    if (parsedText.contains("DeSTDA")) {
      TipoFile = "DESTDA";
    } else if (parsedText.contains("PGDAS-D")) {
      TipoFile = "DSN";
    } else if (parsedText.contains("DEISS")) {
      TipoFile = "DEISS";
    } else if (parsedText.contains("DEFIS")) {
      TipoFile = "DEFIS";
    } else if (parsedText.contains("Valor total do ICMS a recolher")) {
      TipoFile = "EFD ICMS IPI";
    } else if (parsedText.contains("Recibo de declaracao de ISS")) {
      TipoFile = "RDI";
    } else if (parsedText.contains("NFG")) {
      TipoFile = "NFG";
    } else if (parsedText.contains("DMS")) {
      TipoFile = "DMS";
    } else if (parsedText.contains("Opcao pelo Regime de Apuracao de Receitas")) {
      TipoFile = "OPCAO SIMPLES";
    } else if (parsedText.contains("RECIBO DE ENTREGA DE ESCRITURACAO FISCAL DIGITAL - CONTRIBUICOES")) {
      TipoFile = "SPED CONTRIBUICOES";
    } else if (parsedText.contains("|0100|EVANDRO ZANOTTO|64791602072")) {
      TipoFile = "SPED CONTRIBUICOES";
    } else {
      System.out.println("Tipo não encontrado");
    }
    return TipoFile;
  }
  
  public static String naoSobrescrever(String Destination, File[] listOfFiles, int i, int REC)
  {
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
  
  public static void estatisticas(int iteracaoComSucesso, int iteracaoComErros, int iteracaoTotal, String logPath)
    throws IOException
  {
    String input = "";
    


    BufferedReader readLog = new BufferedReader(new FileReader(logPath));
    String line;
    while ((line = readLog.readLine()) != null) { 
      input = input + line + System.lineSeparator();
    }
    
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
  
  public static String data_agora()
  {
    String DATE_FORMAT_NOW = "yyyyMMdd HHmm";
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
  
  public static void main(String[] args) throws IOException { int REC = 0;
    String FullPath = null;
    String RECFILE = null;
    String pathtoread = "\\\\10.1.20.30\\Organizador\\";
    //String pathtosrv = "\\\\10.1.20.13\\setores\\GERAL\\Documentos empresariais\\Documentos\\";
    String pathtosrv = "smb://10.1.20.13/setores/GERAL/Documentos empresariais/Documentos/";

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
    int iteracaoComSucesso = 0;int iteracaoComErros = 0;int iteracaoTotal = 0;int counter = 0;
    String anoDocumento = null;
    
    File suposicao = null;
    boolean verdadeiro = false;
    

    File directory = new File(pathtoread);
	File[] listOfFiles = directory.listFiles();
	
	long length = 0;
	
	for (File file : directory.listFiles()) {       
		if (file.isFile())
            length += file.length();
    }
	
    EscreveTopoLog(bw, data_agora());
    

    for (int i = 0; i < length; i++) {
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
            parsedText = java.text.Normalizer.normalize(parsedText, java.text.Normalizer.Form.NFD);
            parsedText = parsedText.replaceAll("[^\\p{ASCII}]", "");
            parsedText = parsedText.replaceAll("&", "E");
          }
          
          TipoFile = tipodearquivo(parsedText, TipoFile);
          pdDoc.close();
          
          parsedText = excecoes(parsedText, TipoFile);
          
          String[] ret = data_mes_ano(parsedText, TipoFile, anoDocumento);
          
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
              verdadeiro = true;
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
          bw.write("Origem:  " + listOfFiles[i].getAbsolutePath());
          bw.newLine();
          bw.write("Destino: " + FullPath);
          bw.newLine();
          


          if (TipoFile == "OPCAO SIMPLES") {
            TipoFile = "OPÇÃO SIMPLES";
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
          
          if ((verdadeiro) && (listOfFiles[i].getName().matches("(?i).*\\.(txt|TXT|reg|REG)$"))) {
            bw.write("Destino: " + RECFILE);
            bw.newLine();
          }
          try
          {
            if ((!FullPath.contains("null")) && (source.exists())) {
              File destination = new File(FullPath);
              copyFile(source, destination);
              System.out.println("this is source" + source);
              source.delete();
              iteracaoComSucesso++;
            }
            if (verdadeiro) {
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
          verdadeiro = false;
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
    if (listOfFiles.length > 0) {
      estatisticas(iteracaoComSucesso, iteracaoComErros, iteracaoTotal, logPath);
    }
    else {
      File log = new File(logPath);
      log.delete();
    }
    System.out.println("END OF RUN");
  }
  

  private static String EmpresaGetName(String ArquivoProcessado, String pathtosrv)
  {
    File pasta = new File(pathtosrv);
    File[] pastas_SRV = pasta.listFiles();
    String EmpresaNome = null;
    
    for (int index = 0; index < pastas_SRV.length; index++) {
      if (ArquivoProcessado.contains(pastas_SRV[index].getName())) {
        EmpresaNome = pastas_SRV[index].getName();
      }
    }
    return EmpresaNome;
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
      source = new RandomAccessFile(sourceFile, "rw").getChannel();
      destination = new RandomAccessFile(destFile, "rw").getChannel();
      long position = 0L;
      long count = source.size();
      source.transferTo(position, count, destination);
    }
    finally {
      source.close();
      destination.close();
    }
  }
  
  public static String excecoes(String parsedText, String TipoFile)
  {
    try {
      Properties prop = new Properties();
      FileInputStream exeptions = new FileInputStream(
        "\\\\10.1.20.30\\excecoesOrganizador\\" + TipoFile + ".properties");
      prop.load(exeptions);
      for (int c = 1; c <= prop.size() / 2; c++) {
        parsedText = parsedText.replaceAll(prop.getProperty(new StringBuilder(String.valueOf(c)).append("A").toString()), 
          prop.getProperty(new StringBuilder(String.valueOf(c)).append("B").toString()));
      }
      exeptions.close();
    }
    catch (IOException localIOException) {}
    return parsedText;
  }
  
  public static String[] data_mes_ano(String parsedText, String TipoFile, String anoDocumento)
  {
    int tentativa = 0;
    
    Calendar calendar = Calendar.getInstance();
    

    Date today = new Date();
    calendar.setTime(today);
    calendar.set(2, calendar.get(2));
    calendar.set(5, 1);
    calendar.add(5, -1);
    

    String Datadodocumento = null;
    
    String[] mes0A = { "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12" };
    String[] mes0B = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12" };
    String[] mes0C = { "Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez" };
    
    String[] retF = IdenData(TipoFile);
    String ifformatter0 = retF[0];
    String ifformatter1 = retF[1];
    String dateFormatter = retF[2];
    
    for (int ano = 1950; ano < 2100; ano++) {
      for (int mesI = 0; mesI < 12; mesI++) {
        for (int dia = 0; dia < 32; dia++)
        {
          if (tentativa < 1) {
            ano = Year.now().getValue();
            mesI = calendar.get(2) - 1;
            dia = Calendar.getInstance().getActualMaximum(5);
            tentativa = 1;
          }
          if (TipoFile == "DESTDA") {
            if (parsedText.contains(String.format(ifformatter0, new Object[] { mes0A[mesI], Integer.valueOf(ano) }))) {
              anoDocumento = String.valueOf(ano);
              Datadodocumento = String.format(dateFormatter, new Object[] { mes0A[mesI], Integer.valueOf(ano) });
              break;
            }
          } else if (TipoFile == "DSN") {
            if (parsedText.contains(String.format(ifformatter0, new Object[] { mes0A[mesI], Integer.valueOf(ano) }))) {
              anoDocumento = String.valueOf(ano);
              Datadodocumento = String.format(dateFormatter, new Object[] { mes0A[mesI], Integer.valueOf(ano) });
              break;
            }
          } else if (TipoFile == "NFG") {
            if (parsedText.contains(String.format(ifformatter0, new Object[] { mes0A[mesI], Integer.valueOf(ano), Integer.valueOf(dia), mes0A[mesI], Integer.valueOf(ano) }))) {
              anoDocumento = String.valueOf(ano);
              Datadodocumento = String.format(dateFormatter, new Object[] { mes0A[mesI], Integer.valueOf(ano) });
              break;
            }
          } else if (TipoFile == "SPED CONTRIBUICOES") {
            if (!parsedText.contains(String.format(ifformatter0, new Object[] { Integer.valueOf(dia), mes0A[mesI], Integer.valueOf(ano) }))) {
              if (!parsedText.contains(String.format(ifformatter1, new Object[] { Integer.valueOf(dia), mes0A[mesI], Integer.valueOf(ano) }))) {}
            } else {
              anoDocumento = String.valueOf(ano);
              Datadodocumento = String.format(dateFormatter, new Object[] { mes0A[mesI], Integer.valueOf(ano) });
              break;
            }
          } else if (TipoFile == "RDI") {
            if (parsedText.contains(String.format(ifformatter0, new Object[] { mes0C[mesI], Integer.valueOf(ano) }))) {
              anoDocumento = String.valueOf(ano);
              Datadodocumento = String.format(dateFormatter, new Object[] { mes0A[mesI], Integer.valueOf(ano) });
              break;
            }
          } else if (TipoFile == "DEISS") {
            if (parsedText.contains(String.format(ifformatter0, new Object[] { Integer.valueOf(ano), mes0B[mesI] }))) {
              anoDocumento = String.valueOf(ano);
              Datadodocumento = String.format(dateFormatter, new Object[] { mes0A[mesI], Integer.valueOf(ano) });
              break;
            }
          } else if (TipoFile == "OPCAO SIMPLES") {
            if (parsedText.contains(String.format(ifformatter0, new Object[] { Integer.valueOf(ano) }))) {
              anoDocumento = String.valueOf(ano);
              Datadodocumento = String.format(dateFormatter, new Object[] { Integer.valueOf(ano) });
              break;
            }
          } else if (TipoFile == "DEFIS") {
            if (parsedText.contains(String.format(ifformatter0, new Object[] { mes0A[mesI], Integer.valueOf(ano), Integer.valueOf(dia), Integer.valueOf(ano) }))) {
              anoDocumento = String.valueOf(ano);
              Datadodocumento = String.format(dateFormatter, new Object[] { Integer.valueOf(ano) });
              break;
            }
          } else if (TipoFile == "DMS") {
            if (!parsedText.contains(String.format(ifformatter0, new Object[] { mes0A[mesI], Integer.valueOf(ano) }))) { if (!parsedText.contains(String.format(ifformatter1, new Object[] { mes0A[mesI], Integer.valueOf(ano) }))) {}
            } else { anoDocumento = String.valueOf(ano);
              Datadodocumento = String.format(dateFormatter, new Object[] { mes0A[mesI], Integer.valueOf(ano) });
              break;
            }
          }
          else {
            System.out.println("Identificador de tipo de arquivo não encontrado.");
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
}