package test.work.services.impl;

import lombok.Getter;
import org.springframework.stereotype.Service;
import test.work.services.TransactionImportService;

import java.nio.file.Path;

@Getter
@Service
public class TransactionImportServiceImpl implements TransactionImportService {

	@Override
	public boolean process(Path fileToImport) {
		return true;
	}

}
