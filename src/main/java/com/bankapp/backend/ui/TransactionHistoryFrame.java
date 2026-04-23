package com.bankapp.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.io.File;

import com.formdev.flatlaf.FlatLightLaf;
import com.bankapp.dao.TransactionDAO;
import com.bankapp.model.Transaction;
import com.bankapp.model.User;
import com.bankapp.util.TransactionExcelExporter; // <-- added import

public class TransactionHistoryFrame extends JFrame {

    private final User currentUser;
    private final TransactionDAO transactionDAO;

    private JComboBox<String> typeDropdown;
    private JTextField fromDateField, toDateField, minAmtField, maxAmtField;
    private JTable txnTable;
    private DefaultTableModel tableModel;

    public TransactionHistoryFrame(User user) {
        this.currentUser = user;
        this.transactionDAO = new TransactionDAO();

        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ignored) {}

        setTitle("Transaction History - " + user.getName());
        setSize(850, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(245, 247, 250));

        // Filter Panel
        JPanel filterPanel = new JPanel(new GridLayout(2, 6, 10, 10));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filter Transactions"));
        filterPanel.setBackground(new Color(245, 247, 250));

        typeDropdown = new JComboBox<>(new String[]{"All", "DEPOSIT", "WITHDRAWAL", "TRANSFER"});
        fromDateField = new JTextField("YYYY-MM-DD");
        toDateField = new JTextField("YYYY-MM-DD");
        minAmtField = new JTextField("Min Amount");
        maxAmtField = new JTextField("Max Amount");

        JButton applyFilterBtn = new JButton("Apply Filter");
        JButton resetBtn = new JButton("Reset");
        JButton exportXMLBtn = new JButton("Export to XML");
        JButton exportExcelBtn = new JButton("Export to Excel"); // new button

        filterPanel.add(new JLabel("Type:"));
        filterPanel.add(typeDropdown);
        filterPanel.add(new JLabel("From Date:"));
        filterPanel.add(fromDateField);
        filterPanel.add(new JLabel("To Date:"));
        filterPanel.add(toDateField);
        filterPanel.add(new JLabel("Min Amount:"));
        filterPanel.add(minAmtField);
        filterPanel.add(new JLabel("Max Amount:"));
        filterPanel.add(maxAmtField);
        filterPanel.add(applyFilterBtn);
        filterPanel.add(resetBtn);

        // Table
        tableModel = new DefaultTableModel(
                new Object[]{"Date", "Type", "From Account", "To Account", "Amount"}, 0);
        txnTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(txnTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Transaction Records"));

        // Load transactions initially
        loadTransactions(null, null, null, null, null);

        // Bottom export panel
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(exportXMLBtn);
        bottomPanel.add(exportExcelBtn);

        // Layout
        add(filterPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // Button actions
        applyFilterBtn.addActionListener(e -> applyFilters());
        resetBtn.addActionListener(e -> {
            typeDropdown.setSelectedIndex(0);
            fromDateField.setText("YYYY-MM-DD");
            toDateField.setText("YYYY-MM-DD");
            minAmtField.setText("Min Amount");
            maxAmtField.setText("Max Amount");
            loadTransactions(null, null, null, null, null);
        });

        exportXMLBtn.addActionListener(e -> exportToXML());
        exportExcelBtn.addActionListener(e -> exportToExcel()); // new action
    }

    private void loadTransactions(String type, String start, String end, Double min, Double max) {
        tableModel.setRowCount(0);
        List<Transaction> txns = transactionDAO.getFilteredTransactions(
                currentUser.getUserId(), type, start, end, min, max);

        if (txns.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No transactions found for selected filters.");
        } else {
            for (Transaction t : txns) {
                tableModel.addRow(new Object[]{
                        t.getTxnDate(), t.getTxnType(), t.getFromAccount(), t.getToAccount(), "₹" + t.getAmount()
                });
            }
        }
    }

    private void applyFilters() {
        String type = typeDropdown.getSelectedItem().toString();
        type = type.equals("All") ? null : type;

        String from = fromDateField.getText().trim();
        String to = toDateField.getText().trim();
        Double min = parseDoubleSafe(minAmtField.getText());
        Double max = parseDoubleSafe(maxAmtField.getText());

        if (from.equals("YYYY-MM-DD")) from = null;
        if (to.equals("YYYY-MM-DD")) to = null;

        loadTransactions(type, from, to, min, max);
    }

    private Double parseDoubleSafe(String val) {
        try {
            if (val == null || val.isEmpty() || val.contains("Amount")) return null;
            return Double.parseDouble(val);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // --- KEEP YOUR EXISTING XML EXPORT LOGIC AS IS ---
    private void exportToXML() {
        try {
            String type = typeDropdown.getSelectedItem().toString();
            type = type.equals("All") ? null : type;

            String from = fromDateField.getText().trim();
            String to = toDateField.getText().trim();
            Double min = parseDoubleSafe(minAmtField.getText());
            Double max = parseDoubleSafe(maxAmtField.getText());

            List<Transaction> filteredTxns = transactionDAO.getFilteredTransactions(
                    currentUser.getUserId(), type, from, to, min, max
            );

            if (filteredTxns.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No transactions to export.");
                return;
            }

            String filePath = "exports/filtered_transactions_" + currentUser.getUserId() + ".xml";
            boolean ok = com.bankapp.util.TransactionXMLExporter.exportToXML(filteredTxns, filePath);

            JOptionPane.showMessageDialog(this,
                    ok ? "Exported to " + filePath : "Export failed.",
                    "XML Export", ok ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    // --- NEW EXCEL EXPORT INTEGRATION ---
    private void exportToExcel() {
        try {
            String type = typeDropdown.getSelectedItem().toString();
            type = type.equals("All") ? null : type;

            String from = fromDateField.getText().trim();
            String to = toDateField.getText().trim();
            Double min = parseDoubleSafe(minAmtField.getText());
            Double max = parseDoubleSafe(maxAmtField.getText());

            List<Transaction> filteredTxns = transactionDAO.getFilteredTransactions(
                    currentUser.getUserId(), type, from, to, min, max
            );

            if (filteredTxns.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No transactions to export.");
                return;
            }

            File file = TransactionExcelExporter.exportToExcel(filteredTxns);
            if (file != null) {
                JOptionPane.showMessageDialog(this,
                        "Transactions exported to:\n" + file.getAbsolutePath(),
                        "Excel Export", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Excel export failed.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}
