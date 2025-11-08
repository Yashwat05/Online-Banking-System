package com.bankapp.ui;

import com.bankapp.model.Admin;
import com.bankapp.model.User;
import com.bankapp.service.AdminService;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AdminDashboard extends JFrame {

    private final Admin currentAdmin;
    private final AdminService adminService;

    private final DefaultTableModel tableModel;
    private final JTable userTable;

    public AdminDashboard(Admin admin) {
        // Apply FlatLaf Look & Feel
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatLaf.");
        }

        this.currentAdmin = admin;
        this.adminService = new AdminService();

        setTitle("Admin Dashboard - " + admin.getFullName());
        setSize(950, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(245, 247, 250));

        // Header
        JLabel header = new JLabel("Welcome, " + admin.getFullName(), SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.setForeground(new Color(25, 118, 210));
        header.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(header, BorderLayout.NORTH);

        // Table setup
        String[] cols = {"User ID", "Name", "Email", "Status", "Created At"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        userTable = new JTable(tableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.setRowHeight(25);
        userTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        userTable.getTableHeader().setBackground(new Color(25, 118, 210));
        userTable.getTableHeader().setForeground(Color.WHITE);
        userTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        add(new JScrollPane(userTable), BorderLayout.CENTER);

        // Buttons
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        pnl.setBackground(new Color(245, 247, 250));

        JButton approveBtn = createStyledButton("Approve User", new Color(39, 174, 96));
        JButton blockBtn = createStyledButton("Block User", new Color(230, 126, 34));
        JButton deleteBtn = createStyledButton("Delete User", new Color(192, 57, 43));
        JButton refreshBtn = createStyledButton("Refresh", new Color(52, 152, 219));
        JButton logsBtn = createStyledButton("View Admin Logs", new Color(142, 68, 173));
        JButton logoutBtn = createStyledButton("Logout", new Color(44, 62, 80));

        pnl.add(approveBtn);
        pnl.add(blockBtn);
        pnl.add(deleteBtn);
        pnl.add(refreshBtn);
        pnl.add(logsBtn);

        // ✅ NEW: Create Admin Button (only for SUPER_ADMIN)
        if ("SUPER_ADMIN".equalsIgnoreCase(currentAdmin.getRole())) {
            JButton createAdminBtn = createStyledButton("Create New Admin", new Color(25, 118, 210));
            createAdminBtn.addActionListener(e -> openCreateAdminDialog());
            pnl.add(createAdminBtn);
        }

        pnl.add(logoutBtn);
        add(pnl, BorderLayout.SOUTH);

        // Button actions
        refreshBtn.addActionListener(e -> loadUsers());
        approveBtn.addActionListener(e -> handleApprove());
        blockBtn.addActionListener(e -> handleBlock());
        deleteBtn.addActionListener(e -> handleDelete());
        logsBtn.addActionListener(e -> showLogs());
        logoutBtn.addActionListener(e -> {
            dispose();
            new AdminLoginFrame().setVisible(true);
        });

        // Load users initially
        loadUsers();
        setVisible(true);
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor);
            }
        });
        return btn;
    }

    private void loadUsers() {
        tableModel.setRowCount(0);
        List<User> users = adminService.getAllUsers();
        if (users.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No users found.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        for (User u : users) {
            tableModel.addRow(new Object[]{
                    u.getUserId(),
                    u.getName(),
                    u.getEmail(),
                    u.getStatus(),
                    u.getCreatedAt()
            });
        }
    }

    private Integer getSelectedUserId() {
        int row = userTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a user first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return (Integer) tableModel.getValueAt(row, 0);
    }

    private void handleApprove() {
        Integer userId = getSelectedUserId();
        if (userId == null) return;
        boolean ok = adminService.approveUser(currentAdmin.getAdminId(), userId);
        if (ok) {
            JOptionPane.showMessageDialog(this, "User approved successfully!");
            loadUsers();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to approve user.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleBlock() {
        Integer userId = getSelectedUserId();
        if (userId == null) return;
        boolean ok = adminService.blockUser(currentAdmin.getAdminId(), userId);
        if (ok) {
            JOptionPane.showMessageDialog(this, "User blocked successfully!");
            loadUsers();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to block user.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDelete() {
        Integer userId = getSelectedUserId();
        if (userId == null) return;
        int confirm = JOptionPane.showConfirmDialog(this, "Delete user ID " + userId + "?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        boolean ok = adminService.deleteUser(currentAdmin.getAdminId(), userId);
        if (ok) {
            JOptionPane.showMessageDialog(this, "User deleted successfully!");
            loadUsers();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to delete user.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showLogs() {
        String logs = adminService.getRecentAdminLogs(currentAdmin.getAdminId(), 100);
        JTextArea area = new JTextArea(logs);
        area.setFont(new Font("Consolas", Font.PLAIN, 13));
        area.setEditable(false);
        JScrollPane sp = new JScrollPane(area);
        sp.setPreferredSize(new Dimension(850, 400));
        JOptionPane.showMessageDialog(this, sp, "Recent Admin Logs", JOptionPane.INFORMATION_MESSAGE);
    }

    // New: Admin Creation Dialog
    private void openCreateAdminDialog() {
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JTextField fullNameField = new JTextField();
        JTextField emailField = new JTextField();

        formPanel.add(new JLabel("Username:"));
        formPanel.add(usernameField);
        formPanel.add(new JLabel("Password:"));
        formPanel.add(passwordField);
        formPanel.add(new JLabel("Full Name:"));
        formPanel.add(fullNameField);
        formPanel.add(new JLabel("Email:"));
        formPanel.add(emailField);

        int option = JOptionPane.showConfirmDialog(
                this,
                formPanel,
                "Create New Admin",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (option == JOptionPane.OK_OPTION) {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            String fullName = fullNameField.getText().trim();
            String email = emailField.getText().trim();

            if (username.isEmpty() || password.isEmpty() || fullName.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required!", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean ok = adminService.registerAdmin(username, password, fullName, email);
            if (ok) {
                JOptionPane.showMessageDialog(this, "New admin created successfully!");
                adminService.getRecentAdminLogs(currentAdmin.getAdminId(), 10);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to create admin. Username might already exist.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
