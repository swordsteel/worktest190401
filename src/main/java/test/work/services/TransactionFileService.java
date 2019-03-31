package test.work.services;

import java.time.Instant;
import java.util.function.Supplier;

public interface TransactionFileService {

	Supplier<String> defaultPrefix = () -> Instant.now().toEpochMilli() + "_";

	void scanFolder();

}
