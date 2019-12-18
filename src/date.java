import java.text.SimpleDateFormat;
import java.util.Calendar;

public class date {
    public static String now() {
        String DATE_FORMAT_NOW = "yyyyMMdd_HHmm";
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        return sdf.format(cal.getTime());
    }

    public static String day() {
        String DATE_FORMAT_DIA = "dd";
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_DIA);
        return sdf.format(cal.getTime());
    }
    

      public static String set_AnoDocumento(int ano) {
        return String.valueOf(ano);
      }       
    
      public static String ConvertDate(String dateFormatter, int ano, String[] mes0A, int mesI) {
        return String.format(dateFormatter, new Object[] { mes0A[mesI], Integer.valueOf(ano) });
      }
      
      public static String ConvertDate(String dateFormatter, int ano) {
          return String.format(dateFormatter, new Object[] {Integer.valueOf(ano) });
      }
      
}
