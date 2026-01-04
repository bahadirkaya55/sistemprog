package com.example.family;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.List;

/**
 * HaToKuSe - Tek Kullanıcı Test Arayüzü
 * 
 * Bu arayüz ile lider, üyeler ve client'ı tek ekrandan yönetebilirsiniz.
 */
public class HaToKuSeGUI extends JFrame {

    // Renkler
    private static final Color DARK_BG = new Color(30, 30, 40);
    private static final Color PANEL_BG = new Color(45, 45, 60);
    private static final Color ACCENT_GREEN = new Color(80, 200, 120);
    private static final Color ACCENT_BLUE = new Color(70, 130, 220);
    private static final Color ACCENT_ORANGE = new Color(255, 160, 50);
    private static final Color ACCENT_RED = new Color(220, 80, 80);
    private static final Color TEXT_PRIMARY = new Color(240, 240, 250);
    private static final Color TEXT_SECONDARY = new Color(160, 160, 180);

    // Bileşenler
    private JTextArea logArea;
    private JLabel leaderStatusLabel;
    private JLabel member1StatusLabel;
    private JLabel member2StatusLabel;
    private JButton startAllButton;
    private JButton stopAllButton;
    private JTextField messageIdField;
    private JTextField messageBodyField;
    private JTextField autoCountField;
    private JComboBox<String> writeModeCombo;
    private JLabel statsLabel;

    // Süreçler
    private Process leaderProcess;
    private Process member1Process;
    private Process member2Process;
    private Socket clientSocket;
    private PrintWriter clientOut;
    private BufferedReader clientIn;
    private ExecutorService executor = Executors.newCachedThreadPool();
    private ScheduledExecutorService statsScheduler = Executors.newSingleThreadScheduledExecutor();
    private volatile boolean isRunning = false;

    public HaToKuSeGUI() {
        initUI();
        startStatsUpdater();
    }

    private void initUI() {
        setTitle("HaToKuSe - Dağıtık Disk Register");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 650);
        setLocationRelativeTo(null);
        getContentPane().setBackground(DARK_BG);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(DARK_BG);
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Üst - Başlık ve Durum
        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);

        // Sol - Kontroller
        mainPanel.add(createControlPanel(), BorderLayout.WEST);

        // Orta - Log
        mainPanel.add(createLogPanel(), BorderLayout.CENTER);

        // Alt - Client
        mainPanel.add(createClientPanel(), BorderLayout.SOUTH);

        add(mainPanel);

        // Kapatma işlemi
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                stopAll();
            }
        });
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(DARK_BG);
        panel.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel titleLabel = new JLabel("🖥️ HaToKuSe Test Paneli", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(TEXT_PRIMARY);
        panel.add(titleLabel, BorderLayout.WEST);

        statsLabel = new JLabel("Hazır", SwingConstants.RIGHT);
        statsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statsLabel.setForeground(TEXT_SECONDARY);
        panel.add(statsLabel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(PANEL_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(60, 60, 80), 1, true),
                new EmptyBorder(15, 15, 15, 15)));
        panel.setPreferredSize(new Dimension(220, 0));

        // Durum Paneli
        panel.add(createSectionLabel("📊 Sistem Durumu"));

        leaderStatusLabel = createStatusLabel("Lider (5000)", "⚫ Kapalı");
        panel.add(leaderStatusLabel);

        member1StatusLabel = createStatusLabel("Üye-1 (7001)", "⚫ Kapalı");
        panel.add(member1StatusLabel);

        member2StatusLabel = createStatusLabel("Üye-2 (7002)", "⚫ Kapalı");
        panel.add(member2StatusLabel);

        panel.add(Box.createVerticalStrut(20));

        // I/O Modu
        panel.add(createSectionLabel("💾 I/O Modu"));
        writeModeCombo = new JComboBox<>(new String[] { "buffered", "unbuffered", "zerocopy", "memorymapped" });
        styleComboBox(writeModeCombo);
        panel.add(writeModeCombo);

        panel.add(Box.createVerticalStrut(20));

        // Başlat/Durdur
        panel.add(createSectionLabel("🎮 Kontrol"));

        startAllButton = new JButton("▶ TÜMÜNÜ BAŞLAT");
        styleButton(startAllButton, ACCENT_GREEN);
        startAllButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        startAllButton.addActionListener(e -> startAll());
        panel.add(startAllButton);

        panel.add(Box.createVerticalStrut(10));

        stopAllButton = new JButton("⏹ TÜMÜNÜ DURDUR");
        styleButton(stopAllButton, ACCENT_RED);
        stopAllButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        stopAllButton.setEnabled(false);
        stopAllButton.addActionListener(e -> stopAll());
        panel.add(stopAllButton);

        panel.add(Box.createVerticalGlue());

        // Bilgi
        JLabel infoLabel = new JLabel("<html><center>tolerance.conf: 2<br>Üye sayısı: 2</center></html>");
        infoLabel.setForeground(TEXT_SECONDARY);
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(infoLabel);

        return panel;
    }

    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setBackground(DARK_BG);

        JLabel logLabel = createSectionLabel("📋 Sistem Logları");
        panel.add(logLabel, BorderLayout.NORTH);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBackground(new Color(25, 25, 35));
        logArea.setForeground(TEXT_PRIMARY);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(new LineBorder(new Color(60, 60, 80), 1, true));
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton clearButton = new JButton("🗑 Temizle");
        styleButton(clearButton, new Color(80, 80, 100));
        clearButton.addActionListener(e -> logArea.setText(""));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(DARK_BG);
        btnPanel.add(clearButton);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createClientPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panel.setBackground(PANEL_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(60, 60, 80), 1, true),
                new EmptyBorder(10, 15, 10, 15)));

        // Mesaj ID
        panel.add(createFieldLabel("Mesaj ID:"));
        messageIdField = createStyledTextField("msg1");
        messageIdField.setPreferredSize(new Dimension(80, 30));
        panel.add(messageIdField);

        // Mesaj İçeriği
        panel.add(createFieldLabel("İçerik:"));
        messageBodyField = createStyledTextField("Test mesajı");
        messageBodyField.setPreferredSize(new Dimension(150, 30));
        panel.add(messageBodyField);

        // SET butonu
        JButton setBtn = new JButton("📤 SET");
        styleButton(setBtn, ACCENT_BLUE);
        setBtn.addActionListener(e -> sendSet());
        panel.add(setBtn);

        // GET butonu
        JButton getBtn = new JButton("📥 GET");
        styleButton(getBtn, ACCENT_ORANGE);
        getBtn.addActionListener(e -> sendGet());
        panel.add(getBtn);

        // Ayırıcı
        panel.add(new JLabel("  │  "));

        // Otomatik test
        panel.add(createFieldLabel("Adet:"));
        autoCountField = createStyledTextField("100");
        autoCountField.setPreferredSize(new Dimension(60, 30));
        panel.add(autoCountField);

        JButton autoBtn = new JButton("🚀 OTO TEST");
        styleButton(autoBtn, ACCENT_GREEN);
        autoBtn.addActionListener(e -> runAutoTest());
        panel.add(autoBtn);

        return panel;
    }

    private void startAll() {
        String writeMode = (String) writeModeCombo.getSelectedItem();
        log("═══════════════════════════════════════════════════");
        log("🚀 Sistem başlatılıyor... (I/O: " + writeMode + ")");
        log("═══════════════════════════════════════════════════");

        executor.submit(() -> {
            try {
                // 1. Lider başlat
                startLeader(writeMode);
                Thread.sleep(2000);

                // 2. Üye 1 başlat
                startMember(7001, writeMode);
                Thread.sleep(1000);

                // 3. Üye 2 başlat
                startMember(7002, writeMode);
                Thread.sleep(1000);

                isRunning = true;
                SwingUtilities.invokeLater(() -> {
                    startAllButton.setEnabled(false);
                    stopAllButton.setEnabled(true);
                    writeModeCombo.setEnabled(false);
                });

                log("✓ Tüm bileşenler başlatıldı!");
                log("───────────────────────────────────────────────────");

            } catch (Exception e) {
                log("❌ Başlatma hatası: " + e.getMessage());
            }
        });
    }

    private void startLeader(String writeMode) throws Exception {
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
        String jarPath = "target" + File.separator + "distributed-disk-register-0.0.1-SNAPSHOT.jar";

        ProcessBuilder pb = new ProcessBuilder(javaBin, "-jar", jarPath, "leader", writeMode);
        pb.directory(new File("."));
        pb.redirectErrorStream(true);
        leaderProcess = pb.start();

        // Log okuyucu
        executor.submit(() -> readProcessOutput(leaderProcess, "LİDER"));

        updateStatus(leaderStatusLabel, "🟢 Çalışıyor");
        log("[LİDER] Başlatıldı (Port: 5000, gRPC: 6000)");
    }

    private void startMember(int port, String writeMode) throws Exception {
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
        String jarPath = "target" + File.separator + "distributed-disk-register-0.0.1-SNAPSHOT.jar";

        ProcessBuilder pb = new ProcessBuilder(javaBin, "-jar", jarPath, "member", String.valueOf(port), writeMode);
        pb.directory(new File("."));
        pb.redirectErrorStream(true);

        Process p = pb.start();

        if (port == 7001) {
            member1Process = p;
            updateStatus(member1StatusLabel, "🟢 Çalışıyor");
        } else {
            member2Process = p;
            updateStatus(member2StatusLabel, "🟢 Çalışıyor");
        }

        executor.submit(() -> readProcessOutput(p, "ÜYE-" + port));
        log("[ÜYE-" + port + "] Başlatıldı");
    }

    private void readProcessOutput(Process process, String prefix) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                final String msg = line;
                // Sadece önemli logları göster
                if (msg.contains("✓") || msg.contains("✗") || msg.contains("mesaj") ||
                        msg.contains("Başlat") || msg.contains("kaydo")) {
                    log("[" + prefix + "] " + msg);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void stopAll() {
        log("⏹ Sistem durduruluyor...");

        if (leaderProcess != null) {
            leaderProcess.destroyForcibly();
            updateStatus(leaderStatusLabel, "⚫ Kapalı");
        }
        if (member1Process != null) {
            member1Process.destroyForcibly();
            updateStatus(member1StatusLabel, "⚫ Kapalı");
        }
        if (member2Process != null) {
            member2Process.destroyForcibly();
            updateStatus(member2StatusLabel, "⚫ Kapalı");
        }

        disconnectClient();

        isRunning = false;
        startAllButton.setEnabled(true);
        stopAllButton.setEnabled(false);
        writeModeCombo.setEnabled(true);

        log("✓ Sistem durduruldu.");
    }

    private void connectClient() throws Exception {
        if (clientSocket == null || clientSocket.isClosed()) {
            clientSocket = new Socket("localhost", 5000);
            clientOut = new PrintWriter(clientSocket.getOutputStream(), true);
            clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        }
    }

    private void disconnectClient() {
        try {
            if (clientIn != null)
                clientIn.close();
            if (clientOut != null)
                clientOut.close();
            if (clientSocket != null)
                clientSocket.close();
        } catch (Exception ignored) {
        }
        clientSocket = null;
    }

    private void sendSet() {
        executor.submit(() -> {
            try {
                connectClient();
                String id = messageIdField.getText().trim();
                String body = messageBodyField.getText().trim();
                String cmd = "SET " + id + " " + body;

                clientOut.println(cmd);
                String resp = clientIn.readLine();

                log("→ " + cmd);
                log("← " + resp);
            } catch (Exception e) {
                log("❌ SET hatası: " + e.getMessage());
                disconnectClient();
            }
        });
    }

    private void sendGet() {
        executor.submit(() -> {
            try {
                connectClient();
                String id = messageIdField.getText().trim();
                String cmd = "GET " + id;

                clientOut.println(cmd);
                String resp = clientIn.readLine();

                log("→ " + cmd);
                log("← " + resp);
            } catch (Exception e) {
                log("❌ GET hatası: " + e.getMessage());
                disconnectClient();
            }
        });
    }

    private void runAutoTest() {
        executor.submit(() -> {
            try {
                int count = Integer.parseInt(autoCountField.getText().trim());
                log("═══════════════════════════════════════════════════");
                log("🚀 Otomatik test: " + count + " mesaj gönderiliyor...");

                Socket sock = new Socket("localhost", 5000);
                PrintWriter out = new PrintWriter(sock.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));

                int success = 0;
                long start = System.currentTimeMillis();

                for (int i = 1; i <= count; i++) {
                    out.println("SET msg" + i + " TestMesaj" + i);
                    String resp = in.readLine();
                    if (resp != null && resp.startsWith("OK"))
                        success++;

                    if (i % 25 == 0) {
                        final int progress = i;
                        log("  ▸ İlerleme: " + progress + "/" + count);
                    }
                }

                long duration = System.currentTimeMillis() - start;
                double rate = count / (duration / 1000.0);

                log("═══════════════════════════════════════════════════");
                log("✓ TEST TAMAMLANDI!");
                log("  • Toplam: " + count + " | Başarılı: " + success);
                log("  • Süre: " + duration + " ms");
                log("  • Hız: " + String.format("%.1f", rate) + " mesaj/sn");
                log("═══════════════════════════════════════════════════");

                in.close();
                out.close();
                sock.close();

            } catch (Exception e) {
                log("❌ Test hatası: " + e.getMessage());
            }
        });
    }

    private void startStatsUpdater() {
        statsScheduler.scheduleAtFixedRate(() -> {
            try {
                File leaderDir = new File("data/leader");
                File member1Dir = new File("data/member-7001");
                File member2Dir = new File("data/member-7002");

                int leaderCount = leaderDir.exists() ? leaderDir.listFiles().length : 0;
                int m1Count = member1Dir.exists() ? member1Dir.listFiles().length : 0;
                int m2Count = member2Dir.exists() ? member2Dir.listFiles().length : 0;

                String stats = String.format("Lider: %d | Üye1: %d | Üye2: %d mesaj",
                        leaderCount, m1Count, m2Count);

                SwingUtilities.invokeLater(() -> statsLabel.setText(stats));
            } catch (Exception ignored) {
            }
        }, 2, 3, TimeUnit.SECONDS);
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private void updateStatus(JLabel label, String status) {
        SwingUtilities.invokeLater(() -> {
            String text = label.getText().split(":")[0] + ": " + status;
            label.setText(text);
        });
    }

    // === Stil Metodları ===

    private JLabel createSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(TEXT_PRIMARY);
        label.setBorder(new EmptyBorder(5, 0, 8, 0));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JLabel createStatusLabel(String name, String status) {
        JLabel label = new JLabel(name + ": " + status);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(TEXT_SECONDARY);
        label.setBorder(new EmptyBorder(3, 5, 3, 0));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(TEXT_SECONDARY);
        return label;
    }

    private JTextField createStyledTextField(String text) {
        JTextField field = new JTextField(text);
        field.setBackground(new Color(35, 35, 50));
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(TEXT_PRIMARY);
        field.setFont(new Font("Consolas", Font.PLAIN, 12));
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(70, 70, 90), 1, true),
                new EmptyBorder(4, 6, 4, 6)));
        return field;
    }

    private void styleComboBox(JComboBox<?> combo) {
        combo.setBackground(new Color(35, 35, 50));
        combo.setForeground(TEXT_PRIMARY);
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        combo.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(100, 32));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(bg.brighter());
            }

            public void mouseExited(MouseEvent e) {
                btn.setBackground(bg);
            }
        });
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> new HaToKuSeGUI().setVisible(true));
    }
}
