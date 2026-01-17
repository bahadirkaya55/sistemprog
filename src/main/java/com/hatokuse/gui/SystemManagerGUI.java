package com.hatokuse.gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.concurrent.*;

/**
 * HaToKuSe Sistem Yönetim Paneli
 * Maven build, Lider ve Üye sunucu yönetimi tek GUI'den
 */
public class SystemManagerGUI extends JFrame {

    // Renkler
    private static final Color PRIMARY_COLOR = new Color(52, 73, 94);
    private static final Color SUCCESS_COLOR = new Color(39, 174, 96);
    private static final Color ERROR_COLOR = new Color(192, 57, 43);
    private static final Color WARNING_COLOR = new Color(243, 156, 18);
    private static final Color BG_COLOR = new Color(236, 240, 241);
    private static final Color CARD_COLOR = Color.WHITE;

    // Process yönetimi
    private Process leaderProcess;
    private ConcurrentHashMap<String, Process> memberProcesses = new ConcurrentHashMap<>();
    private ExecutorService executor = Executors.newCachedThreadPool();

    // GUI Bileşenleri
    private JTextArea logArea;
    private JButton buildButton;
    private JButton leaderButton;
    private JLabel leaderStatus;
    private JPanel membersPanel;
    private JSpinner memberPortSpinner;
    private JTextField memberIdField;

    // Proje dizini
    private final String projectDir;

    public SystemManagerGUI() {
        this.projectDir = System.getProperty("user.dir");
        initializeUI();
    }

    public SystemManagerGUI(String projectDir) {
        this.projectDir = projectDir;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("HaToKuSe - Sistem Yönetim Paneli");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 800);
        setLocationRelativeTo(null);
        setBackground(BG_COLOR);

        // Shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(this::stopAllProcesses));

        // Ana panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(BG_COLOR);
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Üst - Başlık
        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);

        // Orta - Kontroller
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        centerPanel.setBackground(BG_COLOR);
        centerPanel.add(createBuildPanel());
        centerPanel.add(createServerPanel());
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Alt - Log
        mainPanel.add(createLogPanel(), BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PRIMARY_COLOR);
        panel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel title = new JLabel("🖥️ HaToKuSe Sistem Yönetim Paneli");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Maven • Lider • Üyeler • Tek Noktadan Yönetim");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(189, 195, 199));

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);
        textPanel.add(title);
        textPanel.add(subtitle);

        panel.add(textPanel, BorderLayout.WEST);

        // Client GUI butonu
        JButton clientButton = createStyledButton("İstemci Aç", new Color(155, 89, 182));
        clientButton.addActionListener(e -> openClientGUI());
        panel.add(clientButton, BorderLayout.EAST);

        return panel;
    }

    private JPanel createBuildPanel() {
        JPanel card = createCard("🔨 Proje Derleme (Maven)");
        card.setLayout(new BorderLayout(10, 10));

        // Bilgi
        JTextArea info = new JTextArea();
        info.setEditable(false);
        info.setBackground(CARD_COLOR);
        info.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        info.setText(
                "Proje Dizini:\n" + projectDir + "\n\n" +
                        "Bu işlem şunları yapacak:\n" +
                        "• mvn clean package -DskipTests\n" +
                        "• JAR dosyası oluşturma\n" +
                        "• Bağımlılıkları dahil etme\n\n" +
                        "⏱️ Tahmini süre: 15-30 saniye");
        card.add(info, BorderLayout.CENTER);

        // Buton
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(CARD_COLOR);

        buildButton = createStyledButton("Projeyi Derle", SUCCESS_COLOR);
        buildButton.setPreferredSize(new Dimension(200, 45));
        buildButton.addActionListener(e -> runMavenBuild());
        buttonPanel.add(buildButton);

        card.add(buttonPanel, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createServerPanel() {
        JPanel card = createCard("🚀 Sunucu Yönetimi");
        card.setLayout(new BorderLayout(10, 10));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(CARD_COLOR);

        // Lider Bölümü
        JPanel leaderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        leaderPanel.setBackground(CARD_COLOR);
        leaderPanel.setBorder(BorderFactory.createTitledBorder("Lider Sunucu"));

        leaderButton = createStyledButton("Lider Başlat", SUCCESS_COLOR);
        leaderButton.addActionListener(e -> toggleLeader());
        leaderPanel.add(leaderButton);

        leaderStatus = new JLabel("● Durduruldu");
        leaderStatus.setForeground(ERROR_COLOR);
        leaderStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        leaderPanel.add(leaderStatus);

        leaderPanel.add(new JLabel("   Port: 5000 (Client) / 5001 (gRPC)"));

        content.add(leaderPanel);
        content.add(Box.createVerticalStrut(10));

        // Üye Ekleme Bölümü
        JPanel addMemberPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        addMemberPanel.setBackground(CARD_COLOR);
        addMemberPanel.setBorder(BorderFactory.createTitledBorder("Yeni Üye Ekle"));

        addMemberPanel.add(new JLabel("ID:"));
        memberIdField = new JTextField("member1", 8);
        addMemberPanel.add(memberIdField);

        addMemberPanel.add(new JLabel("Port:"));
        memberPortSpinner = new JSpinner(new SpinnerNumberModel(5002, 5002, 5099, 1));
        addMemberPanel.add(memberPortSpinner);

        JButton addMemberBtn = createStyledButton("Üye Ekle", PRIMARY_COLOR);
        addMemberBtn.addActionListener(e -> addMember());
        addMemberPanel.add(addMemberBtn);

        content.add(addMemberPanel);
        content.add(Box.createVerticalStrut(10));

        // Aktif Üyeler Bölümü
        membersPanel = new JPanel();
        membersPanel.setLayout(new BoxLayout(membersPanel, BoxLayout.Y_AXIS));
        membersPanel.setBackground(CARD_COLOR);
        membersPanel.setBorder(BorderFactory.createTitledBorder("Aktif Üyeler"));

        JLabel noMembers = new JLabel("Henüz üye eklenmedi");
        noMembers.setForeground(Color.GRAY);
        membersPanel.add(noMembers);

        JScrollPane membersScroll = new JScrollPane(membersPanel);
        membersScroll.setPreferredSize(new Dimension(0, 80));
        membersScroll.setBorder(null);
        content.add(membersScroll);
        content.add(Box.createVerticalStrut(10));

        // Yük Testi Bölümü
        JPanel loadTestPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        loadTestPanel.setBackground(CARD_COLOR);
        loadTestPanel.setBorder(BorderFactory.createTitledBorder("Yük Testi"));

        loadTestPanel.add(new JLabel("Mesaj Sayısı:"));
        JSpinner messageCountSpinner = new JSpinner(new SpinnerNumberModel(100, 10, 10000, 100));
        messageCountSpinner.setPreferredSize(new Dimension(80, 25));
        loadTestPanel.add(messageCountSpinner);

        JButton testButton = createStyledButton("Test Başlat", WARNING_COLOR);
        testButton.addActionListener(e -> runLoadTest((Integer) messageCountSpinner.getValue(), testButton));
        loadTestPanel.add(testButton);

        JButton statsButton = createStyledButton("Dağılım Göster", PRIMARY_COLOR);
        statsButton.addActionListener(e -> showDistribution());
        loadTestPanel.add(statsButton);

        content.add(loadTestPanel);

        card.add(content, BorderLayout.CENTER);

        // Tümünü Durdur
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBackground(CARD_COLOR);

        JButton stopAllBtn = createStyledButton("Tümünü Durdur", ERROR_COLOR);
        stopAllBtn.addActionListener(e -> stopAllProcesses());
        bottomPanel.add(stopAllBtn);

        card.add(bottomPanel, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createLogPanel() {
        JPanel panel = createCard("📋 Sistem Günlüğü");
        panel.setLayout(new BorderLayout());
        panel.setPreferredSize(new Dimension(0, 180));

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        logArea.setBackground(new Color(44, 62, 80));
        logArea.setForeground(new Color(236, 240, 241));

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton clearBtn = new JButton("Günlüğü Temizle");
        clearBtn.addActionListener(e -> logArea.setText(""));
        panel.add(clearBtn, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createCard(String title) {
        JPanel card = new JPanel();
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        new LineBorder(new Color(189, 195, 199), 1, true),
                        title,
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        new Font("Segoe UI", Font.BOLD, 13),
                        PRIMARY_COLOR),
                new EmptyBorder(10, 10, 10, 10)));
        return card;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(130, 35));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.darker());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    // ==================== İŞLEVLER ====================

    private void runMavenBuild() {
        buildButton.setEnabled(false);
        buildButton.setText("Derleniyor...");
        log("🔨 Maven build başlatılıyor...");

        executor.submit(() -> {
            try {
                // Windows için mvn.cmd, diğer OS için mvn
                // NOT: "clean" kullanılmıyor çünkü GUI çalışırken JAR dosyası kilitli olabilir
                String mvnCmd = System.getProperty("os.name").toLowerCase().contains("win") ? "mvn.cmd" : "mvn";
                ProcessBuilder pb = new ProcessBuilder(mvnCmd, "package", "-DskipTests");
                pb.directory(new File(projectDir));
                pb.redirectErrorStream(true);

                Process process = pb.start();

                // Çıktı okuma
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    final String logLine = line;
                    SwingUtilities.invokeLater(() -> log(logLine));
                }

                int exitCode = process.waitFor();

                SwingUtilities.invokeLater(() -> {
                    buildButton.setEnabled(true);
                    buildButton.setText("Projeyi Derle");

                    if (exitCode == 0) {
                        log("✅ Maven build başarılı!");
                        JOptionPane.showMessageDialog(this,
                                "Proje başarıyla derlendi!\nJAR dosyası hazır.",
                                "Başarılı", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        log("❌ Maven build başarısız! (Exit code: " + exitCode + ")");
                        JOptionPane.showMessageDialog(this,
                                "Build başarısız oldu.\nGünlüğü kontrol edin.",
                                "Hata", JOptionPane.ERROR_MESSAGE);
                    }
                });

            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    buildButton.setEnabled(true);
                    buildButton.setText("Projeyi Derle");
                    log("❌ Hata: " + e.getMessage());
                    JOptionPane.showMessageDialog(this,
                            "Maven çalıştırılamadı: " + e.getMessage(),
                            "Hata", JOptionPane.ERROR_MESSAGE);
                });
            }
        });
    }

    private void toggleLeader() {
        if (leaderProcess != null && leaderProcess.isAlive()) {
            stopLeader();
        } else {
            startLeader();
        }
    }

    private void startLeader() {
        log("🚀 Lider sunucu başlatılıyor...");

        executor.submit(() -> {
            try {
                String jarPath = projectDir + "/target/hatokuse-1.0-SNAPSHOT-jar-with-dependencies.jar";

                if (!new File(jarPath).exists()) {
                    SwingUtilities.invokeLater(() -> {
                        log("❌ JAR dosyası bulunamadı! Önce projeyi derleyin.");
                        JOptionPane.showMessageDialog(this,
                                "JAR dosyası bulunamadı!\nÖnce 'Projeyi Derle' butonuna tıklayın.",
                                "Hata", JOptionPane.ERROR_MESSAGE);
                    });
                    return;
                }

                ProcessBuilder pb = new ProcessBuilder(
                        "java", "-cp", jarPath, "com.hatokuse.leader.LeaderServer");
                pb.directory(new File(projectDir));
                pb.redirectErrorStream(true);

                leaderProcess = pb.start();

                SwingUtilities.invokeLater(() -> {
                    leaderButton.setText("Lider Durdur");
                    leaderButton.setBackground(ERROR_COLOR);
                    leaderStatus.setText("● Çalışıyor");
                    leaderStatus.setForeground(SUCCESS_COLOR);
                });

                // Çıktı okuma
                BufferedReader reader = new BufferedReader(new InputStreamReader(leaderProcess.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    final String logLine = "[LEADER] " + line;
                    SwingUtilities.invokeLater(() -> log(logLine));
                }

                SwingUtilities.invokeLater(() -> {
                    leaderButton.setText("Lider Başlat");
                    leaderButton.setBackground(SUCCESS_COLOR);
                    leaderStatus.setText("● Durduruldu");
                    leaderStatus.setForeground(ERROR_COLOR);
                    log("⚠️ Lider sunucu durdu.");
                });

            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    log("❌ Lider başlatma hatası: " + e.getMessage());
                });
            }
        });
    }

    private void stopLeader() {
        if (leaderProcess != null && leaderProcess.isAlive()) {
            leaderProcess.destroy();
            log("⏹️ Lider sunucu durduruldu.");
        }
    }

    private void addMember() {
        String memberId = memberIdField.getText().trim();
        int port = (Integer) memberPortSpinner.getValue();

        if (memberId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Üye ID boş olamaz!", "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (memberProcesses.containsKey(memberId)) {
            JOptionPane.showMessageDialog(this, "Bu ID zaten kullanımda!", "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }

        log("🚀 Üye başlatılıyor: " + memberId + " (port: " + port + ")");

        executor.submit(() -> {
            try {
                String jarPath = projectDir + "/target/hatokuse-1.0-SNAPSHOT-jar-with-dependencies.jar";

                if (!new File(jarPath).exists()) {
                    SwingUtilities.invokeLater(() -> {
                        log("❌ JAR dosyası bulunamadı!");
                        JOptionPane.showMessageDialog(this,
                                "JAR dosyası bulunamadı!\nÖnce projeyi derleyin.",
                                "Hata", JOptionPane.ERROR_MESSAGE);
                    });
                    return;
                }

                ProcessBuilder pb = new ProcessBuilder(
                        "java", "-cp", jarPath, "com.hatokuse.member.MemberServer",
                        "--id", memberId, "--port", String.valueOf(port));
                pb.directory(new File(projectDir));
                pb.redirectErrorStream(true);

                Process process = pb.start();
                memberProcesses.put(memberId, process);

                SwingUtilities.invokeLater(() -> {
                    addMemberToPanel(memberId, port);
                    memberPortSpinner.setValue(port + 1);
                    memberIdField.setText("member" + (memberProcesses.size() + 1));
                });

                // Çıktı okuma
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    final String logLine = "[" + memberId + "] " + line;
                    SwingUtilities.invokeLater(() -> log(logLine));
                }

                SwingUtilities.invokeLater(() -> {
                    log("⚠️ " + memberId + " durdu.");
                    removeMemberFromPanel(memberId);
                });

            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    log("❌ Üye başlatma hatası: " + e.getMessage());
                });
            }
        });
    }

    private void addMemberToPanel(String memberId, int port) {
        // İlk ekleme ise "Henüz üye yok" yazısını kaldır
        if (membersPanel.getComponentCount() == 1 &&
                membersPanel.getComponent(0) instanceof JLabel) {
            membersPanel.removeAll();
        }

        JPanel memberRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        memberRow.setBackground(CARD_COLOR);
        memberRow.setName(memberId);
        memberRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        JLabel statusDot = new JLabel("●");
        statusDot.setForeground(SUCCESS_COLOR);
        memberRow.add(statusDot);

        memberRow.add(new JLabel(memberId + " (Port: " + port + ")"));

        JButton stopBtn = new JButton("Durdur");
        stopBtn.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        stopBtn.addActionListener(e -> stopMember(memberId));
        memberRow.add(stopBtn);

        membersPanel.add(memberRow);
        membersPanel.revalidate();
        membersPanel.repaint();
    }

    private void removeMemberFromPanel(String memberId) {
        for (Component comp : membersPanel.getComponents()) {
            if (comp instanceof JPanel && memberId.equals(comp.getName())) {
                membersPanel.remove(comp);
                break;
            }
        }
        memberProcesses.remove(memberId);

        if (membersPanel.getComponentCount() == 0) {
            JLabel noMembers = new JLabel("Henüz üye eklenmedi");
            noMembers.setForeground(Color.GRAY);
            membersPanel.add(noMembers);
        }

        membersPanel.revalidate();
        membersPanel.repaint();
    }

    private void stopMember(String memberId) {
        Process process = memberProcesses.get(memberId);
        if (process != null && process.isAlive()) {
            process.destroy();
            log("⏹️ " + memberId + " durduruldu.");
        }
    }

    private void stopAllProcesses() {
        log("⏹️ Tüm sunucular durduruluyor...");

        stopLeader();

        for (String memberId : memberProcesses.keySet()) {
            stopMember(memberId);
        }

        log("✅ Tüm sunucular durduruldu.");
    }

    private void openClientGUI() {
        SwingUtilities.invokeLater(() -> {
            ClientGUI client = new ClientGUI();
            client.setVisible(true);
        });
    }

    private void runLoadTest(int messageCount, JButton testButton) {
        if (leaderProcess == null || !leaderProcess.isAlive()) {
            JOptionPane.showMessageDialog(this,
                    "Lider sunucu çalışmıyor!\nÖnce lideri başlatın.",
                    "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (memberProcesses.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Hiç üye eklenmedi!\nÖnce en az 2 üye ekleyin.",
                    "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }

        testButton.setEnabled(false);
        testButton.setText("Test...");
        log("🧪 Yük testi başlatılıyor: " + messageCount + " mesaj");

        executor.submit(() -> {
            try {
                java.net.Socket socket = new java.net.Socket("127.0.0.1", 5000);
                java.io.PrintWriter writer = new java.io.PrintWriter(socket.getOutputStream(), true);
                java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(socket.getInputStream()));

                int successCount = 0;
                int errorCount = 0;
                long startTime = System.currentTimeMillis();

                for (int i = 1; i <= messageCount; i++) {
                    String command = "SET test" + i + " Test mesaji " + i;
                    writer.println(command);
                    String response = reader.readLine();

                    if (response != null && response.startsWith("OK")) {
                        successCount++;
                    } else {
                        errorCount++;
                    }

                    // Her 100 mesajda log
                    if (i % 100 == 0) {
                        final int current = i;
                        SwingUtilities
                                .invokeLater(() -> log("📨 " + current + "/" + messageCount + " mesaj gönderildi"));
                    }
                }

                socket.close();

                long duration = System.currentTimeMillis() - startTime;
                final int success = successCount;
                final int errors = errorCount;

                SwingUtilities.invokeLater(() -> {
                    testButton.setEnabled(true);
                    testButton.setText("Test Başlat");
                    log("✅ Yük testi tamamlandı!");
                    log("   Başarılı: " + success + ", Hatalı: " + errors);
                    log("   Süre: " + duration + " ms (" + (messageCount * 1000 / Math.max(1, duration)) + " msg/s)");

                    JOptionPane.showMessageDialog(this,
                            "Yük Testi Tamamlandı!\n\n" +
                                    "Gönderilen: " + messageCount + " mesaj\n" +
                                    "Başarılı: " + success + "\n" +
                                    "Hatalı: " + errors + "\n" +
                                    "Süre: " + duration + " ms\n" +
                                    "Hız: " + (messageCount * 1000 / Math.max(1, duration)) + " msg/s\n\n" +
                                    "'Dağılım Göster' ile mesajların üyelere dağılımını görebilirsiniz.",
                            "Yük Testi Sonucu", JOptionPane.INFORMATION_MESSAGE);
                });

            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    testButton.setEnabled(true);
                    testButton.setText("Test Başlat");
                    log("❌ Yük testi hatası: " + e.getMessage());
                    JOptionPane.showMessageDialog(this,
                            "Yük testi hatası: " + e.getMessage(),
                            "Hata", JOptionPane.ERROR_MESSAGE);
                });
            }
        });
    }

    private void showDistribution() {
        StringBuilder report = new StringBuilder();
        report.append("📊 MESAJ DAĞILIM RAPORU\n");
        report.append("=".repeat(40) + "\n\n");

        File membersDir = new File(projectDir + "/data/members");

        if (!membersDir.exists() || !membersDir.isDirectory()) {
            JOptionPane.showMessageDialog(this,
                    "Üye veri dizini bulunamadı!\nHenüz mesaj gönderilmemiş olabilir.",
                    "Bilgi", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        File[] memberDirs = membersDir.listFiles(File::isDirectory);

        if (memberDirs == null || memberDirs.length == 0) {
            JOptionPane.showMessageDialog(this,
                    "Üye veri dizinleri boş!",
                    "Bilgi", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int totalMessages = 0;
        java.util.Map<String, Integer> distribution = new java.util.TreeMap<>();

        for (File memberDir : memberDirs) {
            // .msg dosyalarını doğrudan üye dizininde ara
            File[] msgFiles = memberDir.listFiles((dir, name) -> name.endsWith(".msg"));
            int count = msgFiles != null ? msgFiles.length : 0;
            distribution.put(memberDir.getName(), count);
            totalMessages += count;
        }

        report.append("Toplam Mesaj: " + totalMessages + "\n");
        report.append("Aktif Üye: " + distribution.size() + "\n\n");
        report.append("-".repeat(40) + "\n");

        for (java.util.Map.Entry<String, Integer> entry : distribution.entrySet()) {
            String memberId = entry.getKey();
            int count = entry.getValue();
            double percentage = totalMessages > 0 ? (count * 100.0 / totalMessages) : 0;

            // Progress bar
            int barLength = (int) (percentage / 5);
            String bar = "█".repeat(barLength) + "░".repeat(20 - barLength);

            report.append(String.format("%-10s [%s] %5d (%5.1f%%)\n",
                    memberId, bar, count, percentage));
        }

        report.append("-".repeat(40) + "\n");

        // Dağılım analizi
        int avg = totalMessages / Math.max(1, distribution.size());
        int maxDev = 0;
        for (int count : distribution.values()) {
            maxDev = Math.max(maxDev, Math.abs(count - avg));
        }
        double devPercent = avg > 0 ? (maxDev * 100.0 / avg) : 0;

        report.append("\n📈 Analiz:\n");
        report.append("   Ortalama: " + avg + " mesaj/üye\n");
        report.append("   Maksimum Sapma: " + maxDev + " (%" + String.format("%.1f", devPercent) + ")\n");

        if (devPercent < 15) {
            report.append("   ✅ Dağılım MÜKEMMEL - Round-robin çalışıyor!\n");
        } else if (devPercent < 30) {
            report.append("   ⚠️ Dağılım İYİ - Küçük sapma var\n");
        } else {
            report.append("   ❌ Dağılım DENGESİZ - Kontrol edin\n");
        }

        log(report.toString());

        // Dialog göster
        JTextArea textArea = new JTextArea(report.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(450, 350));

        JOptionPane.showMessageDialog(this, scrollPane,
                "Mesaj Dağılım Raporu", JOptionPane.PLAIN_MESSAGE);
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = java.time.LocalTime.now().toString().substring(0, 8);
            logArea.append("[" + timestamp + "] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Varsayılan kullan
        }

        SwingUtilities.invokeLater(() -> {
            SystemManagerGUI gui = new SystemManagerGUI();
            gui.setVisible(true);
        });
    }
}
