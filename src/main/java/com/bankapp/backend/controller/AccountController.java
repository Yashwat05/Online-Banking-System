package com.bankapp.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import com.bankapp.backend.dao.UserDAO;
import com.bankapp.backend.model.User;
import com.bankapp.backend.services.BankService;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final UserDAO userDAO = new UserDAO();

    @GetMapping("/balance")
    public double getBalance(@RequestParam String accNo,
                             HttpServletRequest request) {

        String email = (String) request.getAttribute("userEmail");

        User user = userDAO.getUserByEmail(email);

        BankService bankService = new BankService(user);

        return bankService.getBalance(accNo);
    }

    @PostMapping("/deposit")
    public String deposit(@RequestParam String accNo,
                          @RequestParam double amount,
                          HttpServletRequest request) {

        String email = (String) request.getAttribute("userEmail");

        User user = userDAO.getUserByEmail(email);

        BankService bankService = new BankService(user);

        return bankService.deposit(accNo, amount) ? "Success" : "Failed";
    }
}