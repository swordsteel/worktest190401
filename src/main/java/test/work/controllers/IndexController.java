package test.work.controllers;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import test.work.repositories.TransactionRepository;

@Getter
@Controller
public class IndexController {

	@Autowired
	TransactionRepository transactionRepository;

	@RequestMapping("/")
	public String list(Model model) {
		model.addAttribute("transactions", getTransactionRepository().findAll());
		return "index";
	}

}
