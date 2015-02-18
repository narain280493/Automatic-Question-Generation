package distractorgeneration;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class GrammerCheck {
	public static void main(String[] args) throws IOException {
    
		String string = "January 2, 2010";
    	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH);
    	DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH);
    	
    	LocalDate date = LocalDate.parse(string, formatter);
    	
    	System.out.println(date.format(formatter1)); 
    }
}
