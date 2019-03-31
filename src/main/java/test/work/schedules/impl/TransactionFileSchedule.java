package test.work.schedules.impl;

import lombok.Getter;
import org.springframework.context.annotation.PropertySource;
import test.work.schedules.Schedule;
import test.work.services.TransactionFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Getter
@PropertySource("transaction.properties")
public class TransactionFileSchedule implements Schedule {

	@Autowired
	TransactionFileService transactionFileService;

	@Override
	@Scheduled(fixedRateString = "${scheduled}")
	public void executeTask() {
		getTransactionFileService().scanFolder();
	}

}
