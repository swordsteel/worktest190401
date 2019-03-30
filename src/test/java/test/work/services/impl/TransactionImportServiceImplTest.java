package test.work.services.impl;

import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import test.work.services.TransactionImportService;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Getter
@SpringBootTest
class TransactionImportServiceImplTest {

	@TestConfiguration
	static class TransactionImportServiceImplTestContextConfiguration {

		@Bean
		public TransactionImportService transactionImportService() {
			return new TransactionImportServiceImpl() {
			};
		}

	}

	@Autowired
	private TransactionImportService transactionImportService;

	@Test
	void whenImportingFile_thenExpectTrue() throws IOException {
		Path processFile = new ClassPathResource("files/tiny.csv").getFile().toPath();
		assertTrue(getTransactionImportService().process(processFile));
	}

}
