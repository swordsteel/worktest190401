package test.work.services.impl;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import lombok.Getter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;
import test.work.services.TransactionFileService;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Getter
@PropertySource("classpath:transaction.properties")
@SpringBootTest
class TransactionFileServiceImplTest {

	@TestConfiguration
	static class TransactionFileServiceImplTestContextConfiguration {

		@Bean
		public TransactionFileService transactionFileService() {
			return new TransactionFileServiceImpl() {

				@Override
				public Path getWatchFolder() {
					return TransactionFileServiceImplTest.jimfsWatchFolder;
				}

			};
		}

	}

	private static Path jimfsWatchFolder;

	private FileSystem fileSystem;
	@Value("${folder.watch}")
	private String watchFolder;
	@Autowired
	private TransactionFileService transactionFileService;

	@BeforeEach
	void setUp() throws IOException {
		fileSystem = Jimfs.newFileSystem(Configuration.unix());
		jimfsWatchFolder = fileSystem.getPath(watchFolder);
		Files.createDirectory(jimfsWatchFolder);
	}

	@AfterEach
	void tearDown() throws IOException {
		fileSystem.close();
	}

	@Test
	void whenReadingFolder_thenFindFile() throws IOException {
		Files.copy(new ClassPathResource("files/img.jpg").getFile().toPath(), jimfsWatchFolder.resolve("img.jpg"));
		long noFiles = ((TransactionFileServiceImpl) transactionFileService).getFilesInWatchFolder().get().count();
		assertEquals(1, noFiles);
	}

}
