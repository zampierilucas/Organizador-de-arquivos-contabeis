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
import java.text.Normalizer;
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


public class Organizador_Mensais {
	
	static String FileString; 				 //Variavel para converte a edita de File para String, o nome da empresa para o pesquisa no objeto empresa.
	static String EmpresaCaminho;			 //caminho a ser arquivado, depois de pathsrv.
	static String FullPath; 				 //caminho completo para ser arquivado.
	static String anoDocumento;
	static String Datadodocumento;
	static String Empresaname;
	static String TipoFile;		
	//======================================CAMINHOS-SERVIDOR=========================================================//
	static String pathtoread = "\\\\10.1.1.135\\ORGANIZADOR_DE_ARQUIVOS\\"; //Caminho a ser lido.
	static String pathtosrv = "\\\\10.1.20.13\\setores\\GERAL\\Documentos empresariais\\Documentos\\"; //caminho para o servidor
	static String Global;
	static File[] fList;
	static File[] listOfFiles;
	static int iteracaoComSucesso = 0;	
	static int iteracaoComErros = 0;	
	static int iteracaoTotal = 0;	
	static Date date = new Date();
	static DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
	
	 public static void main(String args[]) throws IOException {
		BufferedWriter bw = null;
		FileWriter fw = null;
		PDFParser parser = null;
	    PDDocument pdDoc = null;
	    COSDocument cosDoc = null;
	    
		String fwPath =(pathtoread + "log\\" + "log " + dateFormat.format(date) + ".txt");
		fw = new FileWriter(fwPath);
		bw = new BufferedWriter(fw);
		File log = new File(fwPath);
	    PDFTextStripper pdfStripper;
		    
	    //==========================Cria Array de pdfs da pasta de leitura============================================//
	    File folder = new File(pathtoread);
	    File[] listOfFiles = folder.listFiles();
	  
	  
	    //====================================Inicio do log===========================================================//
		 bw.write("Data: " + dateFormat.format(date));
		 bw.newLine(); //Linha dois - empresas processadas
		 bw.write("Arquivos processadas: L+2");
		 bw.newLine(); //Linha tres - com sucesso
		 bw.write("Arquivos processados com sucesso: L+3");
		 bw.newLine(); //linha quatro - com erros
		 bw.write("Arquivos processados com erros: L+4");
		 bw.newLine(); //linha quatro - economizado
		 bw.write("Tempo poupado: L+5 minutos");
		 bw.newLine();
		 bw.newLine();
		 
		 
		 //======================Processa documento por documento na poosta do Organizar==============================//
		 for (int i = 0; i < listOfFiles.length; i++) {
		   if (listOfFiles[i].isFile() && listOfFiles[i].getName().matches("(?i).*\\.(pdf|PDF)$")) {
			   
			//=================Converte pdf para txt para analise========================//
		    String parsedText;
		    String fileName = listOfFiles[i].getAbsolutePath();
		    File file = new File(fileName);
		    try {
	          parser = new PDFParser(new FileInputStream(file));
	          System.out.println("file:" + file);
	          parser.parse();
	          cosDoc = parser.getDocument();
	          pdfStripper = new PDFTextStripper();
	          pdDoc = new PDDocument(cosDoc);
	          parsedText = pdfStripper.getText(pdDoc);
	          parsedText = Normalizer.normalize(parsedText, Normalizer.Form.NFD);
		      parsedText = parsedText.replaceAll("[^\\p{ASCII}]", "");
		      parsedText = parsedText.replaceAll("&", "E");
		    

			//======================================Classificar=======================================================//		    
		    if (parsedText.contains("DeSTDA")){ 		//DESTDA
		        TipoFile = ("DESTDA");
		        parsedText = process(parsedText);
		    }
	        else if (parsedText.contains("PGDAS-D")){	//SIMPLES MENSAL
	        	TipoFile = ("DSN");
	        	parsedText = process(parsedText);
		    } 
	        else if (parsedText.contains("DEISS")){  	//DEISS AP
	        	TipoFile = ("DEISS");
	        	parsedText = process(parsedText);
		    }
	        else if (parsedText.contains("DEFIS")){		
	        	TipoFile = ("DEFIS");
	        	parsedText = process(parsedText);
		    }
	        else if (parsedText.contains("Valor total do ICMS a recolher")){	//SPED
	        	TipoFile = ("EFD ICMS IPI");
	        	parsedText = process(parsedText);
	        }
		    else if (parsedText.contains("Recibo de declaracao de ISS")){		//ISS IPE
	        	TipoFile = ("RDI");
	        	parsedText = process(parsedText);
	        }
	        else if (parsedText.contains("NFG")){		//NFG
	        	TipoFile = ("NFG");
	        	parsedText = process(parsedText);
	        }
	        else if (parsedText.contains("DMS")){		//DMS
	        	TipoFile = ("DMS"); 
	        	parsedText = process(parsedText); 
		    }
	        else if (parsedText.contains("Opcao pelo Regime de Apuracao de Receitas")){
	        	TipoFile = ("OPÇÃO SIMPLES"); 
	        	parsedText = process(parsedText); 
		    }
		    else{
			    bw.write("Tipo não encontrado:");  
			 	bw.newLine();
			    bw.write(parsedText);
			    bw.write("-------------------end--of--bug-----------------------");  
		    }
	            
		    //========================Descobrir nome da empresa=======================================================//	
		    EmpresaGetName(parsedText);
	        
	        //===================================log==================================================================//
	        //=================Nome empresa e origem doc========================//
	        bw.newLine();
 	        bw.write("Empresa: " + Empresaname);
	        bw.newLine();
	        bw.write("Origem:  " + listOfFiles[i].getAbsolutePath()); 
	        bw.newLine();

	        //=========================Destino doc==============================//
	        FullPath = (EmpresaCaminho + "\\" + TipoFile + "\\" + anoDocumento + "\\" + TipoFile + " " + Datadodocumento + ".pdf");
	        bw.write("Destino: " + FullPath); 
	        bw.newLine();
			
	        //=================================Vericiar se existe e criar diretório===================================//
	        File PathWOFileF = new File(EmpresaCaminho + "\\" +  TipoFile + "\\" + anoDocumento + "\\");
			if (FullPath.contains("null")){
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
			else if (!PathWOFileF.exists()) {
			    boolean result = false;
			    try{
			    	PathWOFileF.mkdirs();
			    	result = true;
			    } catch(SecurityException se){}        
			    if(result == true) {    
			    	bw.write("Diretorio criado com sucesso");
			      	bw.newLine();
			    }
			}
			        
			String filegravar = (FullPath);		        
	        File destination = new File(filegravar);
	        
	        //===================================Função para não sobrescrever=========================================//
	        int fileNo = 0;
	        while(destination.exists()) { 
	            fileNo++; 
	            String ifExists = filegravar.replaceAll(".pdf", "(" + fileNo + ").pdf");   
	            destination = new File(ifExists);
	        } 
	        
	        File source = new File(listOfFiles[i].getAbsolutePath());
	      
	        if (!FullPath.contains("null")){
	        	copyFile(source,destination);
	        	iteracaoComSucesso++;
	            source.delete();  
			}
	        
	        bw.write("------------------------------------------------------------------------------------------------------------------------------------------------------------------");  	        
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
			EmpresaCaminho = null;
			Empresaname = null;
			Datadodocumento = null;
			anoDocumento =  null;
			TipoFile= null;
		    } catch (Exception e) { e.printStackTrace();  }    
   }         
} // close for  
		 
		//================Fecha log para escrever estatisticas========================//
		bw.close();
		fw.close();
		
		//================Escrever estatisticas no log========================//
		BufferedReader readLog = new BufferedReader(new FileReader(fwPath));
		String line;
		String input = "";
		while((line = readLog.readLine()) != null)
		{
		    input += line + System.lineSeparator();
		}
		String iteracaoMin;
		int min = (iteracaoTotal * 20)/60;
		int seg = (iteracaoTotal * 20) - (min * 60);
		iteracaoMin = (min + ":" + seg);
		
		iteracaoComErros =  iteracaoTotal - iteracaoComSucesso;
		
		String Sucesso = Integer.toString(iteracaoComSucesso);
		String Erros = Integer.toString(iteracaoComErros);
		String Total = Integer.toString(iteracaoTotal);
		if (iteracaoTotal == 0) {
		 try {
			readLog.close();
			FileOutputStream out = new FileOutputStream(fwPath);
			out.write(input.getBytes());
		    out.close();
		    log.delete();
		} catch (Exception e3) { e3.printStackTrace(); }
	   	 	System.exit(0);
		}
		else if(min==0 && seg > 0) {
			input = input.replace("L+2", Total);//Total
			input = input.replace("L+3", Sucesso);//Sucesso
			input = input.replace("L+4", Erros);//Erros
			input = input.replace("L+5 minutos", iteracaoMin + " segundos");//Seg
		} 
		else {
			input = input.replace("L+2", Total);//Total
			input = input.replace("L+3", Sucesso);//Sucesso
			input = input.replace("L+4", Erros);//Erros
			input = input.replace("L+5", iteracaoMin);//Minutos
		}
			FileOutputStream out = new FileOutputStream(fwPath);
			out.write(input.getBytes());
			out.close();
			readLog.close();
			System.out.println("\nDone");	
}// close main

		
	 	//===============================Função para descobrir nome da empresa========================================//
		private static void EmpresaGetName(String ArquivoProcessado) throws IOException {
			int i = 0;
			int found = 0;
			
			File folder = new File(pathtosrv);
			File[] listaEmpresasSRV = folder.listFiles();
			  
			for (i = 0; i < listaEmpresasSRV.length;i++) {
		     if(ArquivoProcessado.contains(listaEmpresasSRV[i].getName()))
		     {
		       	 EmpresaCaminho = listaEmpresasSRV[i].getPath();
		       	 Empresaname = listaEmpresasSRV[i].getName();
		    	 
		    	 //EXCECOES
		    	 if(ArquivoProcessado.contains("LUIZ ALBERTO PELLIN")) {
		    		 EmpresaCaminho = EmpresaCaminho + "\\1 MATRIZ";
		    	 }
		    	 if (ArquivoProcessado.contains("12.883.198/0001-88")) {
		    		 EmpresaCaminho = EmpresaCaminho + "\\CONFIDENZA NOVO";
		    	 }
		    	 if (ArquivoProcessado.contains("05.242.070/0001-70")) {
		    		 EmpresaCaminho = EmpresaCaminho + "\\CONFIDENZA VELHO";
		    	 }
		    	 else 
		    	 found = 1;	   
		     } 	
		 	 else  if (i>=listaEmpresasSRV.length && found == 0){}	
			} 		
		}
	
	
		//========================Move arquivo para a pasta da empresa correta(sometimes)=============================//
		@SuppressWarnings("resource")
		public static void copyFile(File sourceFile, File destFile) throws IOException {
		     if(!destFile.exists()) {
		      destFile.createNewFile();
		     }
	
		     FileChannel source = null;
		     FileChannel destination = null;
		     try {
		      source = new RandomAccessFile(sourceFile,"rw").getChannel();
		      destination = new RandomAccessFile(destFile,"rw").getChannel();
		      long position = 0;
		      long count    = source.size();
		      source.transferTo(position, count, destination);
		     } 
		     finally {
		      if(source != null) {
		       source.close();
		      }
		      if(destination != null) {
		       destination.close();
		      }
		    }
		 }

	
		//==========resolve as exesões gramaticas cadastradas e encontra o periodo no documento=======================//
		public static String process(String parsedText_Process)
		{
			 int mesI = 01;
			 int ano = 2000;
			 String[] mes0A = new String[] { "01", "02" , "03", "04" ,"05" ,"06" ,"07" ,"08" ,"09" ,"10" ,"11" ,"12" };
			 String[] mes0B = new String[] { "1", "2" , "3", "4" ,"5" ,"6" ,"7" ,"8" ,"9" ,"10" ,"11" ,"12" };
			 String[] mes0C = new String[] { "Jan", "Fev" , "Mar", "Abr" ,"Mai" ,"Jun" ,"Jul" ,"Ago" ,"Set" ,"Out" ,"Nov" ,"Dez" };
			
			try {
			  Properties prop = new Properties();
			  final FileInputStream exeptions = new FileInputStream("\\\\10.1.1.135\\excecoesOrganizador\\"+ TipoFile +".properties");
			  prop.load(exeptions);	
						
			  for(int c=1;c<=(prop.size()/2);c++) {
				parsedText_Process = parsedText_Process.replaceAll("" + prop.getProperty(c + "A") + "","" + prop.getProperty(c + "B") + "");
			  }
			  	exeptions.close();
			 } catch (IOException ex) {}
			ano = 1950;
			if (TipoFile == "DESTDA") {
			 for (;ano<2100; ano++) {
			  for (mesI=0;mesI<12;mesI++) {
			   if (parsedText_Process.contains("Mes Referencia " + mes0A[mesI] + "/" + ano)){
				anoDocumento = String.valueOf(ano);
				Datadodocumento = (mes0A[mesI] + "." + ano);
			   break;
			}}}}
			else if	(TipoFile == "DSN") {
			 for (;ano<2100; ano++) {
			  for (mesI=0;mesI<12;mesI++) {
			   if (parsedText_Process.contains("Principal" + "\r\n" + mes0A[mesI] + "/" + ano)){
				anoDocumento = String.valueOf(ano);
				Datadodocumento = (mes0A[mesI] + "." + ano);
				break;
			}}}}
			else if	(TipoFile == "NFG") {
			 for (;ano<2100; ano++) {
			  for (mesI=0;mesI<12;mesI++) {
			   for (int dia=28;dia<32;dia++) {
				if (parsedText_Process.contains("periodo de 01/" + mes0A[mesI] + "/" + ano + " a " + dia + "/" + mes0A[mesI] + "/" + ano )){
				 anoDocumento = String.valueOf(ano);
				 Datadodocumento = (mes0A[mesI] + "." + ano);
				 break;
			}}}}}	
			else if	(TipoFile == "DMS") { 
			 for (;ano<2100; ano++) {
			  for (mesI=0;mesI<12;mesI++) {
			   if (parsedText_Process.contains("Valor do Faturamento:" + "\r\n" + mes0A[mesI] + "/" + ano)){
				anoDocumento = String.valueOf(ano);
				Datadodocumento = (mes0A[mesI] + "." + ano);
				break; 
			}
			   else if (parsedText_Process.contains("Contribuinte:" + "\r\n" + mes0A[mesI] + "/" + ano)){
				anoDocumento = String.valueOf(ano);
				Datadodocumento = (mes0A[mesI] + "." + ano);
				break;  
		    }}}} 
			else if	(TipoFile == "EFD ICMS IPI") {
			 for (;ano<2100; ano++) {
			  for (mesI=0;mesI<12;mesI++) {			  //01/04/2017 a 30/04/2017
			   for (int dia=28;dia<32;dia++) {
				if (parsedText_Process.contains("01/" + mes0A[mesI] + "/" +  ano + " a " + dia + "/" + mes0A[mesI] + "/" + ano)){
				 anoDocumento = String.valueOf(ano);
				 Datadodocumento = (mes0A[mesI] + "." + ano);
				break;
			}}}}}
			else if	(TipoFile == "DEFIS") {
				 for (;ano<2100; ano++) {
				  for (mesI=0;mesI<12;mesI++) {
				   if (parsedText_Process.contains("Periodo abrangido pela Declaracao: 01/" + mes0A[mesI] + "/" + ano + " a 31/12/" + ano)){
					anoDocumento = String.valueOf(ano);
					Datadodocumento = ("" + ano);
					break;
				}}}}
			else if	(TipoFile == "RDI") {
			 for (;ano<2100; ano++) {
			  for (mesI=0;mesI<12;mesI++) {			  //Periodo: Jul/2018
				if (parsedText_Process.contains("Periodo: " + mes0C[mesI] + "/" +  ano)){
				 anoDocumento = String.valueOf(ano);
			     Datadodocumento = (mes0A[mesI] + "." + ano);
			    break;
			}}}}
			else if	(TipoFile == "DEISS") {
			  for (;ano<2100; ano++) {
			   for (mesI=0;mesI<12;mesI++) {
				if (parsedText_Process.contains("Ano e Mes de Referencia:    " + ano + "/  " + mes0B[mesI])){
				 anoDocumento = String.valueOf(ano);
				 Datadodocumento = ("0" + mes0B[mesI] + "." + ano);
				break;
			}}}}
			else if	(TipoFile == "OPÇÃO SIMPLES") {
				  for (;ano<2100; ano++) {
				   for (mesI=0;mesI<12;mesI++) {
					if (parsedText_Process.contains("Ano-calendario: " + ano)){
					 anoDocumento = String.valueOf(ano);
					 Datadodocumento = "" + ano;
					break;
			}}}}
			return parsedText_Process;		
		}	
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
	




	

