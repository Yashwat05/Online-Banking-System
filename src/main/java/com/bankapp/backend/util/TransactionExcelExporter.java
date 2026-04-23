package com.bankapp.util;

import com.bankapp.model.Transaction;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.List;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class TransactionExcelExporter {

    /**
     * Exports a list of transactions to an Excel (.xlsx) file inside /exports directory.
     * Returns the generated File object on success, or null on failure.
     */
    public static File exportToExcel(List<Transaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            System.out.println("No transactions available to export.");
            return null;
        }

        FileOutputStream fos = null;
        Workbook workbook = new XSSFWorkbook();

        try {
            // Ensure export directory exists
            File exportDir = new File("exports");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }

            // Generate unique filename
            String fileName = "transactions_" + System.currentTimeMillis() + ".xlsx";
            File outputFile = new File(exportDir, fileName);

            // Create sheet and header
            Sheet sheet = workbook.createSheet("Transactions");
            Row header = sheet.createRow(0);

            String[] headers = {"User ID", "From Account", "To Account", "Amount", "Transaction Type", "Transaction Date"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                CellStyle headerStyle = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                headerStyle.setFont(font);
                headerStyle.setAlignment(HorizontalAlignment.CENTER);
                cell.setCellStyle(headerStyle);
            }

            // Date formatter for Timestamps
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            // Write transaction data
            int rowNum = 1;
            for (Transaction txn : transactions) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(txn.getUserId());
                row.createCell(1).setCellValue(safe(txn.getFromAccount()));
                row.createCell(2).setCellValue(safe(txn.getToAccount()));
                row.createCell(3).setCellValue(txn.getAmount());
                row.createCell(4).setCellValue(safe(txn.getTxnType()));
                row.createCell(5).setCellValue(
                        txn.getTxnDate() != null ? sdf.format(txn.getTxnDate()) : ""
                );
            }

            // Auto-size all columns for better readability
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write to file
            fos = new FileOutputStream(outputFile);
            workbook.write(fos);

            System.out.println("Transactions exported successfully to: " + outputFile.getAbsolutePath());
            return outputFile;

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Excel export failed: " + e.getMessage());
            return null;
        } finally {
            try {
                if (fos != null) fos.close();
                workbook.close();
            } catch (Exception ignored) {}
        }
    }

    // Null safety helper
    private static String safe(String val) {
        return val != null ? val : "";
    }
}
