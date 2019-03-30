package test.work.services;

import java.nio.file.Path;

public interface TransactionImportService {

	boolean process(Path fileToImport);

}
