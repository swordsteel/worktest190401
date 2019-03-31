package test.work.services.impl;

import lombok.Getter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import test.work.entities.Batch;
import test.work.entities.Description;
import test.work.entities.Transaction;
import test.work.repositories.BatchRepository;
import test.work.repositories.DescriptionRepository;
import test.work.repositories.TransactionRepository;
import test.work.services.TransactionImportService;

import java.io.IOException;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static java.nio.file.Files.newBufferedReader;
import static test.work.services.TransactionImportService.validTransactionAmount;

@Getter
@Service
public class TransactionImportServiceImpl implements TransactionImportService {

	private static final CSVFormat CSV_FORMAT = CSVFormat.DEFAULT.withHeader("id", "date", "description", "amount");
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
	private static final Logger LOGGER = LoggerFactory.getLogger(TransactionImportServiceImpl.class);

	@Getter
	protected static class ImportCSV {

		private Batch batch;
		private List<Description> descriptions;
		private List<Transaction> transactions;
		private int rows;

		public ImportCSV(Path fileToImport) {
			this.batch = new Batch(fileToImport.getFileName().toString(), Date.from(Instant.now()));
			this.descriptions = new ArrayList<>();
			this.transactions = new ArrayList<>();
			this.rows = 0;
		}

		public void increaseRowCount() {
			rows++;
		}

	}

	@Autowired
	BatchRepository batchRepository;
	@Autowired
	DescriptionRepository descriptionRepository;
	@Autowired
	TransactionRepository transactionRepository;

	@Override
	public boolean process(Path fileToImport) {
		try(CSVParser csvParser = new CSVParser(newBufferedReader(fileToImport), CSV_FORMAT.withTrim())) {
			if(getBatchRepository().findByNK(fileToImport.getFileName().toString()).isPresent()) {
				LOGGER.error("File by name {} are already processed", fileToImport.getFileName());
				return false;
			}
			ImportCSV importCSV = new ImportCSV(fileToImport);
			processCSV(csvParser, importCSV);
			importCSV.getBatch().setTotalNumberOfRows(importCSV.getRows());
			importCSV.getBatch().setNumberOfNewTrans(importCSV.getTransactions().size());
			getBatchRepository().save(importCSV.getBatch());
			getDescriptionRepository().saveAll(importCSV.getDescriptions());
			getTransactionRepository().saveAll(importCSV.getTransactions());
			return true;
		}
		catch(IllegalStateException e) {
			LOGGER.error("Not a valid CSV format in file: {}", fileToImport.getFileName());
		}
		catch(IOException e) {
			LOGGER.error("Error occurred while loading file: {}", fileToImport.getFileName(), e);
		}
		catch(NullPointerException e) {
			LOGGER.error("Import path cannot be null", e);
		}
		return false;
	}

	protected void processCSV(CSVParser csvParser, ImportCSV importCSV) {
		for(CSVRecord record : csvParser) {
			importCSV.increaseRowCount();
			try {
				int id = makeTransactionID(record);
				Date date = makeTransactionDate(record);
				long amount = makeTransactionAmount(record);
				if(getTransactionRepository().findByNK(id).isPresent()) {
					LOGGER.error(
							"Transaction already registered. Id {} in file {}",
							id,
							importCSV.getBatch().getFilename()
					);
					return;
				}
				Description description = makeTransactionDescription(record, importCSV);
				importCSV.getTransactions().add(new Transaction(id, date, amount, description, importCSV.getBatch()));
			}
			catch(IllegalArgumentException | ParseException e) {
				LOGGER.error(
						"Invalid transaction record. Record number {} in file {}. Exception: {}",
						record.getRecordNumber(),
						importCSV.getBatch().getFilename(),
						e.getMessage()
				);
			}
		}
	}

	protected long makeTransactionAmount(CSVRecord record) {
		long amount;
		if(!validTransactionAmount(record.get("amount"))) {
			throw new IllegalArgumentException("Format Amount need two decimals. Amount: " + record.get("amount"));
		}
		amount = Long.parseLong(record.get("amount").replace(".", ""));
		return amount;
	}

	protected Date makeTransactionDate(CSVRecord record) throws ParseException {
		return DATE_FORMAT.parse(record.get("date"));
	}

	protected Description makeTransactionDescription(CSVRecord record, ImportCSV importCSV) {
		Optional<Description> findDescription = getDescriptionRepository().findByNK(record.get("description"));
		if(findDescription.isPresent()) {
			return findDescription.get();
		}
		for(Description description : importCSV.getDescriptions()) {
			if(description.getDescription().equals(record.get("description"))) {
				return description;
			}
		}
		Description description = new Description(record.get("description"), importCSV.getBatch());
		importCSV.getDescriptions().add(description);
		return description;
	}

	protected int makeTransactionID(CSVRecord record) {
		return Integer.parseInt(record.get("id"));
	}

}
