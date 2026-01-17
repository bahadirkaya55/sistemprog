package com.hatokuse.gui;

import com.hatokuse.config.ConfigReader;
import com.hatokuse.protocol.HaToKuSeProtocol;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.TimerTask;

/**
 * HaToKuSe Yönetim Paneli
 * Tüm sistemi tek ekrandan yönetir:
 * - Lider ve üye sunucuları başlatma/durdurma
 * - Bağlı üyeleri görme
 * - Mesaj gönderme/alma
 */
public class ManagementPanel extends JFrame {

    // Renkler
    private static final Color PRIMARY = new Color(41, 128, 185);
    private static final Color SUCCESS = new Color(39, 174, 96);
    private static final Color WARNING = new Color(241, 196, 15);
    private static final Color DANGER = new Color(192, 57, 43);
    private static final Color DARK_BG = new Color(44, 62, 80);
    private static final Color CARD_BG = new Color(52, 73, 94);
    private static final Color LIGHT_BG = new Color(236, 240, 241);
    private static final Color TEXT_LIGHT = new Color(236, 240, 241);

    // Sunucu işlemleri
    private Process leaderProcess;
    private Map<String, Process> memberProcesses = new ConcurrentHashMap<>();
    private int tolerance = 2;

    // Client bağlantısı
    private Socket clientSocket;
    private BufferedReader clientReader;
    private PrintWriter clientWriter;
    private boolean clientConnected = false;

    // GUI Bileşenleri
    private JButton startLeaderBtn, stopLeaderBtn;
    private JButton addMemberBtn;
    private JLabel leaderStatusLabel;
    private JLabel clientStatusLabel;
    private DefaultTableModel membersTableModel;
    private JTable membersTable;
    private JTextField messageIdField;
    private JTextArea messageContentArea;
    private JTextArea logArea;
    private JLabel toleranceLabel, memberCountLabel, messageCountLabel;
    private int nextMemberPort = 5002;
    private int messageCounter = 0;

    // Timer
    private java.util.Timer refreshTimer;

    public ManagementPanel() {
        loadConfig();
        initializeUI();
        startRefreshTimer();
    }

    private void loadConfig() {
        try {
            ConfigReader config = new ConfigReader();
            config.loadToleranceConfig();
            tolerance = config.getTolerance();
        } catch (IOException e) {
            tolerance = 2;
        }
    }

    private void initializeUI() {
        setTitle("HaToKuSe Yönetim Paneli");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 800);
        setLocationRelativeTo(null);

        // Ana panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(DARK_BG);
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Üst - Başlık ve durum
        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);

        // Sol - Sunucu kontrolü ve üye listesi
        JPanel leftPanel = new JPanel(new BorderLayout(10, 10));
        leftPanel.setBackground(DARK_BG);
        leftPanel.setPreferredSize(new Dimension(350, 0));
        leftPanel.add(createServerControlPanel(), BorderLayout.NORTH);
        leftPanel.add(createMembersPanel(), BorderLayout.CENTER);
        mainPanel.add(leftPanel, BorderLayout.WEST);

        // Orta - Mesaj işlemleri
        mainPanel.add(createMessagePanel(), BorderLayout.CENTER);

        // Alt - Log
        mainPanel.add(createLogPanel(), BorderLayout.SOUTH);

        add(mainPanel);

        // Menü
        setJMenuBar(createMenuBar());

        // Kapanırken sunucuları durdur
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                stopAllServers();
            }
        });
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("Dosya");
        JMenuItem exitItem = new JMenuItem("Çıkış");
        exitItem.addActionListener(e -> {
            stopAllServers();
            System.exit(0);
        });
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);

        JMenu helpMenu = new JMenu("Yardım");
        JMenuItem infoItem = new JMenuItem("Kullanım Kılavuzu");
        infoItem.addActionListener(e -> new InfoDialog(this).setVisible(true));
        helpMenu.add(infoItem);

        JMenuItem aboutItem = new JMenuItem("Hakkında");
        aboutItem.addActionListener(e -> showAbout());
        helpMenu.add(aboutItem);
        menuBar.add(helpMenu);

        return menuBar;
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 0));
        panel.setBackground(DARK_BG);
        panel.setBorder(new EmptyBorder(0, 0, 10, 0));

        // Başlık
        JLabel title = new JLabel("🖥️ HaToKuSe Yönetim Paneli");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(TEXT_LIGHT);
        panel.add(title, BorderLayout.WEST);

        // İstatistikler
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        statsPanel.setBackground(DARK_BG);

        toleranceLabel = createStatLabel("🛡️ Tolerans: " + tolerance);
        memberCountLabel = createStatLabel("👥 Üye: 0");
        messageCountLabel = createStatLabel("📨 Mesaj: 0");

        statsPanel.add(toleranceLabel);
        statsPanel.add(memberCountLabel);
        statsPanel.add(messageCountLabel);

        panel.add(statsPanel, BorderLayout.EAST);

        return panel;
    }

    private JLabel createStatLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(TEXT_LIGHT);
        label.setBackground(CARD_BG);
        label.setOpaque(true);
        label.setBorder(new EmptyBorder(8, 15, 8, 15));
        return label;
    }

    private JPanel createServerControlPanel() {
        JPanel panel = createCard("🚀 Sunucu Kontrolü");
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Lider kontrol
        JPanel leaderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        leaderPanel.setBackground(CARD_BG);

        leaderStatusLabel = new JLabel("● Durdu");
        leaderStatusLabel.setForeground(DANGER);
        leaderStatusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));

        startLeaderBtn = createBtn("Lider Başlat", SUCCESS);
        startLeaderBtn.addActionListener(e -> startLeader());

        stopLeaderBtn = createBtn("Durdur", DANGER);
        stopLeaderBtn.addActionListener(e -> stopLeader());
        stopLeaderBtn.setEnabled(false);

        leaderPanel.add(new JLabel("Lider Sunucu:"));
        leaderPanel.add(leaderStatusLabel);
        leaderPanel.add(Box.createHorizontalStrut(10));
        leaderPanel.add(startLeaderBtn);
        leaderPanel.add(stopLeaderBtn);

        panel.add(leaderPanel);

        // Ayırıcı
        panel.add(Box.createVerticalStrut(10));
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        panel.add(sep);
        panel.add(Box.createVerticalStrut(10));

        // Üye ekleme
        JPanel memberAddPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        memberAddPanel.setBackground(CARD_BG);

        addMemberBtn = createBtn("+ Üye Ekle", PRIMARY);
        addMemberBtn.addActionListener(e -> addMember());
        addMemberBtn.setEnabled(false);

        JButton removeAllBtn = createBtn("Tümünü Kaldır", WARNING);
        removeAllBtn.addActionListener(e -> removeAllMembers());

        memberAddPanel.add(addMemberBtn);
        memberAddPanel.add(removeAllBtn);

        panel.add(memberAddPanel);

        // İstemci bağlantısı
        panel.add(Box.createVerticalStrut(10));
        JSeparator sep2 = new JSeparator();
        sep2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        panel.add(sep2);
        panel.add(Box.createVerticalStrut(10));

        JPanel clientPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        clientPanel.setBackground(CARD_BG);

        clientStatusLabel = new JLabel("● Bağlı Değil");
        clientStatusLabel.setForeground(DANGER);
        clientStatusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JButton connectClientBtn = createBtn("İstemci Bağla", PRIMARY);
        connectClientBtn.addActionListener(e -> toggleClientConnection());

        clientPanel.add(new JLabel("İstemci:"));
        clientPanel.add(clientStatusLabel);
        clientPanel.add(Box.createHorizontalStrut(10));
        clientPanel.add(connectClientBtn);

        panel.add(clientPanel);

        return panel;
    }

    private JPanel createMembersPanel() {
        JPanel panel = createCard("👥 Kayıtlı Üyeler");
        panel.setLayout(new BorderLayout());

        String[] columns = { "ID", "Port", "Durum", "Mesaj" };
        membersTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        membersTable = new JTable(membersTableModel);
        membersTable.setBackground(CARD_BG);
        membersTable.setForeground(TEXT_LIGHT);
        membersTable.setGridColor(new Color(100, 100, 100));
        membersTable.setRowHeight(30);
        membersTable.getTableHeader().setBackground(PRIMARY);
        membersTable.getTableHeader().setForeground(Color.WHITE);
        membersTable.setSelectionBackground(PRIMARY.darker());

        // Durum sütunu için renklendirme
        membersTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBackground(isSelected ? PRIMARY.darker() : CARD_BG);
                if ("Aktif".equals(value)) {
                    setForeground(SUCCESS);
                } else {
                    setForeground(DANGER);
                }
                return this;
            }
        });

        JScrollPane scrollPane = new JScrollPane(membersTable);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(CARD_BG);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Seçili üyeyi kaldır butonu
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.setBackground(CARD_BG);
        JButton removeBtn = createBtn("Seçili Üyeyi Kaldır", DANGER);
        removeBtn.addActionListener(e -> removeSelectedMember());
        btnPanel.add(removeBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createMessagePanel() {
        JPanel panel = createCard("📨 Mesaj İşlemleri");
        panel.setLayout(new BorderLayout(10, 10));

        // Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(CARD_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Mesaj ID
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        JLabel idLabel = new JLabel("Mesaj ID:");
        idLabel.setForeground(TEXT_LIGHT);
        formPanel.add(idLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        messageIdField = new JTextField(20);
        formPanel.add(messageIdField, gbc);

        // Otomatik ID butonu
        gbc.gridx = 2;
        gbc.weightx = 0;
        JButton autoIdBtn = new JButton("Otomatik");
        autoIdBtn.addActionListener(e -> {
            messageIdField.setText(String.valueOf(++messageCounter));
        });
        formPanel.add(autoIdBtn, gbc);

        // Mesaj içeriği
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        JLabel contentLabel = new JLabel("Mesaj:");
        contentLabel.setForeground(TEXT_LIGHT);
        formPanel.add(contentLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        messageContentArea = new JTextArea(6, 30);
        messageContentArea.setLineWrap(true);
        messageContentArea.setWrapStyleWord(true);
        JScrollPane msgScroll = new JScrollPane(messageContentArea);
        formPanel.add(msgScroll, gbc);

        panel.add(formPanel, BorderLayout.CENTER);

        // Butonlar
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnPanel.setBackground(CARD_BG);

        JButton setBtn = createBtn("📤 SET - Kaydet", SUCCESS);
        setBtn.setPreferredSize(new Dimension(150, 40));
        setBtn.addActionListener(e -> executeSet());

        JButton getBtn = createBtn("📥 GET - Getir", PRIMARY);
        getBtn.setPreferredSize(new Dimension(150, 40));
        getBtn.addActionListener(e -> executeGet());

        JButton clearBtn = createBtn("🗑️ Temizle", new Color(149, 165, 166));
        clearBtn.setPreferredSize(new Dimension(120, 40));
        clearBtn.addActionListener(e -> {
            messageIdField.setText("");
            messageContentArea.setText("");
        });

        btnPanel.add(setBtn);
        btnPanel.add(getBtn);
        btnPanel.add(clearBtn);

        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createLogPanel() {
        JPanel panel = createCard("📋 İşlem Günlüğü");
        panel.setLayout(new BorderLayout());
        panel.setPreferredSize(new Dimension(0, 150));

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setBackground(new Color(30, 40, 50));
        logArea.setForeground(new Color(46, 204, 113));
        logArea.setBorder(new EmptyBorder(5, 5, 5, 5));

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton clearLogBtn = new JButton("Günlüğü Temizle");
        clearLogBtn.addActionListener(e -> logArea.setText(""));
        panel.add(clearLogBtn, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createCard(String title) {
        JPanel card = new JPanel();
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        new LineBorder(new Color(100, 100, 100), 1, true),
                        title,
                        TitledBorder.LEFT, TitledBorder.TOP,
                        new Font("Segoe UI", Font.BOLD, 13),
                        TEXT_LIGHT),
                new EmptyBorder(10, 10, 10, 10)));
        return card;
    }

    private JButton createBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(bg.darker());
            }

            public void mouseExited(MouseEvent e) {
                btn.setBackground(bg);
            }
        });

        return btn;
    }

    // ==================== Sunucu İşlemleri ====================

    private void startLeader() {
        try {
            String jarPath = "target/hatokuse-1.0-SNAPSHOT-jar-with-dependencies.jar";
            ProcessBuilder pb = new ProcessBuilder(
                    "java", "-cp", jarPath, "com.hatokuse.leader.LeaderServer");
            pb.directory(new File("."));
            pb.redirectErrorStream(true);
            leaderProcess = pb.start();

            // Log okuyucu thread
            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(leaderProcess.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        log("[LEADER] " + line);
                    }
                } catch (IOException e) {
                    // Process kapandı
                }
            }).start();

            leaderStatusLabel.setText("● Çalışıyor");
            leaderStatusLabel.setForeground(SUCCESS);
            startLeaderBtn.setEnabled(false);
            stopLeaderBtn.setEnabled(true);
            addMemberBtn.setEnabled(true);

            log("✓ Lider sunucu başlatıldı (port 5000, gRPC: 5001)");

        } catch (IOException e) {
            log("✗ Lider başlatma hatası: " + e.getMessage());
            showError("Lider başlatılamadı: " + e.getMessage());
        }
    }

    private void stopLeader() {
        if (leaderProcess != null) {
            leaderProcess.destroyForcibly();
            leaderProcess = null;
        }

        disconnectClient();

        leaderStatusLabel.setText("● Durdu");
        leaderStatusLabel.setForeground(DANGER);
        startLeaderBtn.setEnabled(true);
        stopLeaderBtn.setEnabled(false);
        addMemberBtn.setEnabled(false);

        log("○ Lider sunucu durduruldu");
    }

    private void addMember() {
        String memberId = "member" + (memberProcesses.size() + 1);
        int port = nextMemberPort++;

        try {
            String jarPath = "target/hatokuse-1.0-SNAPSHOT-jar-with-dependencies.jar";
            ProcessBuilder pb = new ProcessBuilder(
                    "java", "-cp", jarPath, "com.hatokuse.member.MemberServer",
                    "--id", memberId, "--port", String.valueOf(port));
            pb.directory(new File("."));
            pb.redirectErrorStream(true);
            Process process = pb.start();

            memberProcesses.put(memberId, process);
            membersTableModel.addRow(new Object[] { memberId, port, "Aktif", 0 });

            // Log okuyucu
            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        log("[" + memberId + "] " + line);
                    }
                } catch (IOException e) {
                    // Process kapandı
                }
            }).start();

            log("✓ " + memberId + " eklendi (port: " + port + ")");
            updateStats();

        } catch (IOException e) {
            log("✗ Üye ekleme hatası: " + e.getMessage());
            showError("Üye eklenemedi: " + e.getMessage());
        }
    }

    private void removeSelectedMember() {
        int row = membersTable.getSelectedRow();
        if (row < 0) {
            showError("Lütfen bir üye seçin!");
            return;
        }

        String memberId = (String) membersTableModel.getValueAt(row, 0);
        Process process = memberProcesses.remove(memberId);
        if (process != null) {
            process.destroyForcibly();
        }
        membersTableModel.removeRow(row);

        log("○ " + memberId + " kaldırıldı");
        updateStats();
    }

    private void removeAllMembers() {
        for (Process p : memberProcesses.values()) {
            p.destroyForcibly();
        }
        memberProcesses.clear();
        membersTableModel.setRowCount(0);

        log("○ Tüm üyeler kaldırıldı");
        updateStats();
    }

    private void stopAllServers() {
        removeAllMembers();
        stopLeader();
    }

    // ==================== İstemci İşlemleri ====================

    private void toggleClientConnection() {
        if (clientConnected) {
            disconnectClient();
        } else {
            connectClient();
        }
    }

    private void connectClient() {
        try {
            clientSocket = new Socket("localhost", 5000);
            clientReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            clientWriter = new PrintWriter(clientSocket.getOutputStream(), true);
            clientConnected = true;

            clientStatusLabel.setText("● Bağlı");
            clientStatusLabel.setForeground(SUCCESS);

            log("✓ İstemci bağlandı");

        } catch (IOException e) {
            log("✗ Bağlantı hatası: " + e.getMessage());
            showError("Bağlantı kurulamadı!\nLider sunucunun çalıştığından emin olun.");
        }
    }

    private void disconnectClient() {
        try {
            if (clientReader != null)
                clientReader.close();
            if (clientWriter != null)
                clientWriter.close();
            if (clientSocket != null)
                clientSocket.close();
        } catch (IOException e) {
            // Ignore
        }
        clientConnected = false;
        clientStatusLabel.setText("● Bağlı Değil");
        clientStatusLabel.setForeground(DANGER);
    }

    private void executeSet() {
        if (!clientConnected) {
            showError("Önce istemciyi bağlayın!");
            return;
        }

        String id = messageIdField.getText().trim();
        String content = messageContentArea.getText().trim();

        if (id.isEmpty() || content.isEmpty()) {
            showError("Mesaj ID ve içerik boş olamaz!");
            return;
        }

        String cmd = HaToKuSeProtocol.createSetCommand(id, content);
        log("→ " + cmd);
        clientWriter.println(cmd);

        try {
            String response = clientReader.readLine();
            log("← " + response);

            if (response != null && response.startsWith("OK")) {
                JOptionPane.showMessageDialog(this, "Mesaj başarıyla kaydedildi!", "Başarılı",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                showError("Kayıt başarısız: " + response);
            }
        } catch (IOException e) {
            showError("Okuma hatası: " + e.getMessage());
            disconnectClient();
        }
    }

    private void executeGet() {
        if (!clientConnected) {
            showError("Önce istemciyi bağlayın!");
            return;
        }

        String id = messageIdField.getText().trim();
        if (id.isEmpty()) {
            showError("Mesaj ID boş olamaz!");
            return;
        }

        String cmd = HaToKuSeProtocol.createGetCommand(id);
        log("→ " + cmd);
        clientWriter.println(cmd);

        try {
            String response = clientReader.readLine();
            log("← " + response);

            if (response != null && response.startsWith("OK")) {
                String msg = response.length() > 3 ? response.substring(3).trim() : "";
                messageContentArea.setText(msg);
                JOptionPane.showMessageDialog(this, "Mesaj getirildi!", "Başarılı", JOptionPane.INFORMATION_MESSAGE);
            } else {
                showError("Mesaj bulunamadı: " + response);
            }
        } catch (IOException e) {
            showError("Okuma hatası: " + e.getMessage());
            disconnectClient();
        }
    }

    // ==================== Yardımcı ====================

    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            String time = java.time.LocalTime.now().toString().substring(0, 8);
            logArea.append("[" + time + "] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private void updateStats() {
        memberCountLabel.setText("👥 Üye: " + memberProcesses.size());
    }

    private void startRefreshTimer() {
        refreshTimer = new java.util.Timer();
        refreshTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkProcessHealth();
            }
        }, 5000, 5000);
    }

    private void checkProcessHealth() {
        // Üyelerin durumunu kontrol et
        for (int i = 0; i < membersTableModel.getRowCount(); i++) {
            String id = (String) membersTableModel.getValueAt(i, 0);
            Process p = memberProcesses.get(id);
            if (p != null && !p.isAlive()) {
                membersTableModel.setValueAt("Durdu", i, 2);
            }
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Hata", JOptionPane.ERROR_MESSAGE);
    }

    private void showAbout() {
        JOptionPane.showMessageDialog(this,
                "HaToKuSe Yönetim Paneli\n\n" +
                        "Dağıtık, hata-tolere mesaj kayıt sistemi.\n" +
                        "Tüm sistemi tek ekrandan yönetin.\n\n" +
                        "Versiyon: 1.0",
                "Hakkında", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }

        SwingUtilities.invokeLater(() -> {
            ManagementPanel panel = new ManagementPanel();
            panel.setVisible(true);
        });
    }
}
