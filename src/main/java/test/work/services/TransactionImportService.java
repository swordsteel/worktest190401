package test.work.services;

import java.nio.file.Path;
import java.util.regex.Pattern;

public interface TransactionImportService {

	boolean process(Path fileToImport);

	static boolean validTransactionAmount(String amoun) {
		if(Pattern.compile("^[+-]?\\d+\\.\\d{2}").matcher(amoun).find()) {
			return true;
		}
		return false;
	}

}
