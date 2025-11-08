package com.bankapp.ui;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import com.formdev.flatlaf.FlatLightLaf;
import com.bankapp.model.User;
import com.bankapp.model.Account;
import com.bankapp.service.BankService;
import com.bankapp.dao.AccountDAO;

public class Dashboard extends JFrame {

    private final User currentUser;
    private final BankService bankService;
    private final AccountDAO accountDAO;

    private JComboBox<String> accountDropdown;
    private List<Account> userAccounts;

    public Dashboard(User user) {
        //Apply FlatLaf theme
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatLaf theme");
        }

        this.currentUser = user;
        this.bankService = new BankService(user);
        this.accountDAO = new AccountDAO();

        setTitle("Dashboard - Welcome " + user.getName());
        setSize(700, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        //Colors & Fonts
        Color primaryColor = new Color(41, 128, 185);
        Color bgColor = new Color(245, 247, 250);
        Font titleFont = new Font("Segoe UI", Font.BOLD, 22);
        Font btnFont = new Font("Segoe UI", Font.PLAIN, 16);

        // Welcome label
        JLabel welcomeLabel = new JLabel("Welcome, " + user.getName() + "!", JLabel.CENTER);
        welcomeLabel.setFont(titleFont);
        welcomeLabel.setForeground(primaryColor);
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));

        // Account selection panel
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        topPanel.setBackground(bgColor);

        JLabel selectLabel = new JLabel("Select Account:");
        selectLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        accountDropdown = new JComboBox<>();
        accountDropdown.setPreferredSize(new Dimension(250, 30));
        loadUserAccounts();

        topPanel.add(selectLabel);
        topPanel.add(accountDropdown);

        // Buttons layout
        JPanel buttonPanel = new JPanel(new GridLayout(4, 2, 20, 20));
        buttonPanel.setBackground(bgColor);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        JButton depositBtn = createStyledButton("Deposit", primaryColor, btnFont);
        JButton withdrawBtn = createStyledButton("Withdraw", new Color(230, 126, 34), btnFont);
        JButton transferBtn = createStyledButton("Transfer", new Color(52, 152, 219), btnFont);
        JButton checkBalBtn = createStyledButton("Check Balance", new Color(39, 174, 96), btnFont);
        JButton historyBtn = createStyledButton("Transaction History", new Color(52, 152, 219), btnFont);
        JButton changePwdBtn = createStyledButton("Change Password", new Color(142, 68, 173), btnFont);
        JButton newAccBtn = createStyledButton("Open New Account", new Color(52, 73, 94), btnFont);
        JButton logoutBtn = createStyledButton("Logout", new Color(192, 57, 43), btnFont);

        // Add all buttons to grid layout
        buttonPanel.add(depositBtn);
        buttonPanel.add(withdrawBtn);
        buttonPanel.add(transferBtn);
        buttonPanel.add(checkBalBtn);
        buttonPanel.add(historyBtn);
        buttonPanel.add(changePwdBtn);
        buttonPanel.add(newAccBtn);
        buttonPanel.add(logoutBtn);

        // Add components to frame
        add(welcomeLabel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.SOUTH);

        // Button actions
        depositBtn.addActionListener(e -> new TransactionFrame(currentUser, "DEPOSIT").setVisible(true));
        withdrawBtn.addActionListener(e -> new TransactionFrame(currentUser, "WITHDRAW").setVisible(true));
        transferBtn.addActionListener(e -> new TransactionFrame(currentUser, "TRANSFER").setVisible(true));
        checkBalBtn.addActionListener(e -> showCurrentBalance());
        historyBtn.addActionListener(e -> new TransactionHistoryFrame(currentUser).setVisible(true));
        changePwdBtn.addActionListener(e -> handleChangePassword());
        newAccBtn.addActionListener(e -> openNewAccount());
        logoutBtn.addActionListener(e -> logout());

        setVisible(true);
    }

    // Load user's accounts into dropdown
    private void loadUserAccounts() {
        userAccounts = accountDAO.getAccountsByUserId(currentUser.getUserId());
        accountDropdown.removeAllItems();

        if (userAccounts.isEmpty()) {
            accountDropdown.addItem("No accounts found");
        } else {
            for (Account acc : userAccounts) {
                accountDropdown.addItem(acc.getAccountNumber() + " (" + acc.getAccountType() + ")");
            }
        }
    }

    // Check balance (dialog)
    private void showCurrentBalance() {
        int index = accountDropdown.getSelectedIndex();
        if (index >= 0 && index < userAccounts.size()) {
            Account acc = userAccounts.get(index);
            double balance = accountDAO.getBalance(currentUser.getUserId(), acc.getAccountNumber());
            JOptionPane.showMessageDialog(this,
                    "Account: " + acc.getAccountNumber() +
                            "\nType: " + acc.getAccountType() +
                            "\nBalance: ₹" + balance,
                    "Account Balance",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // Change password
    private void handleChangePassword() {
        String newPwd = JOptionPane.showInputDialog(this, "Enter your new password:");
        if (newPwd != null && !newPwd.trim().isEmpty()) {
            boolean updated = bankService.changePassword(currentUser.getUserId(), newPwd);
            JOptionPane.showMessageDialog(this,
                    updated ? "Password updated successfully!" : "Failed to update password.",
                    "Change Password",
                    updated ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
        }
    }

    // Open new account
    private void openNewAccount() {
        new NewAccountFrame(currentUser).setVisible(true);
        loadUserAccounts(); // Refresh list
    }

    // Logout
    private void logout() {
        JOptionPane.showMessageDialog(this, "You’ve been logged out successfully!");
        dispose();
        new LoginFrame().setVisible(true);
    }

    // Button styling helper
    private JButton createStyledButton(String text, Color bgColor, Font font) {
        JButton button = new JButton(text);
        button.setFont(font);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        button.setOpaque(true);

        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
        return button;
    }
}
