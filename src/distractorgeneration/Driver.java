package distractorgeneration;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;



public class Driver {
    public static void main(String[] args) throws Exception {
    	String string = "January 2, 2010";
    	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH);
    	LocalDate date = LocalDate.parse(string, formatter);
    	System.out.println(date); // 2010-01-02
	}
}
