package test.work.services.impl;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import test.work.entities.Batch;
import test.work.repositories.BatchRepository;
import test.work.services.TransactionImportService;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Date;

import static java.nio.file.Files.newBufferedReader;

@Getter
@Service
public class TransactionImportServiceImpl implements TransactionImportService {

	private static final Logger LOGGER = LoggerFactory.getLogger(TransactionImportServiceImpl.class);

	@Autowired
	BatchRepository batchRepository;

	@Override
	public boolean process(Path fileToImport) {
		try(BufferedReader bufferedReader = newBufferedReader(fileToImport)) {
			Batch batch = new Batch(fileToImport.getFileName().toString(), Date.from(Instant.now()));
			getBatchRepository().save(batch);
			return true;
		}
		catch(IOException e) {
			LOGGER.error("Error occurred while loading file: {}", fileToImport.getFileName(), e);
		}
		catch(NullPointerException e) {
			LOGGER.error("Import path cannot be null", e);
		}
		return false;
	}

}
