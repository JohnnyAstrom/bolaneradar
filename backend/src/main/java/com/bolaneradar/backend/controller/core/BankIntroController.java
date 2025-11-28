package com.bolaneradar.backend.controller.core;

import com.bolaneradar.backend.dto.core.BankIntroDto;
import com.bolaneradar.backend.service.core.BankIntroService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/banks")
@CrossOrigin
public class BankIntroController {

    private final BankIntroService bankIntroService;

    public BankIntroController(BankIntroService bankIntroService) {
        this.bankIntroService = bankIntroService;
    }

    @GetMapping("/{bankKey}/intro")
    public BankIntroDto getBankIntro(@PathVariable String bankKey) {
        return bankIntroService.getBankIntro(bankKey);
    }
}