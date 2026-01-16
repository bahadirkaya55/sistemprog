package com.hatokuse.gui;

import com.hatokuse.protocol.HaToKuSeProtocol;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

/**
 * HaToKuSe İstemci GUI
 * Modern, kullanımı kolay grafiksel arayüz
 */
public class ClientGUI extends JFrame {

    // Bağlantı
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private boolean connected = false;

    // GUI Bileşenleri
    private JTextField hostField;
    private JTextField portField;
    private JButton connectButton;
    private JTextField messageIdField;
    private JTextArea messageArea;
    private JTextArea logArea;
    private JButton setButton;
    private JButton getButton;
    private JButton clearButton;
    private JLabel statusLabel;
    private JPanel connectionPanel;

    // Renkler
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color SUCCESS_COLOR = new Color(39, 174, 96);
    private static final Color ERROR_COLOR = new Color(192, 57, 43);
    private static final Color BG_COLOR = new Color(236, 240, 241);
    private static final Color CARD_COLOR = Color.WHITE;

    public ClientGUI() {
        initializeUI();
    }

    private void initializeUI() {
        setTitle("HaToKuSe İstemci - Dağıtık Mesaj Kayıt Servisi");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(850, 650);
        setLocationRelativeTo(null);
        setBackground(BG_COLOR);

        // Ana panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(BG_COLOR);
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Üst bölüm - Başlık ve Bağlantı
        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);

        // Orta bölüm - Mesaj işlemleri
        mainPanel.add(createMessagePanel(), BorderLayout.CENTER);

        // Alt bölüm - Log
        mainPanel.add(createLogPanel(), BorderLayout.SOUTH);

        add(mainPanel);

        // Menü bar
        setJMenuBar(createMenuBar());
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // Dosya menüsü
        JMenu fileMenu = new JMenu("Dosya");
        JMenuItem exitItem = new JMenuItem("Çıkış");
        exitItem.addActionListener(e -> {
            disconnect();
            System.exit(0);
        });
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);

        // Yardım menüsü
        JMenu helpMenu = new JMenu("Yardım");
        JMenuItem infoItem = new JMenuItem("Kullanım Kılavuzu");
        infoItem.addActionListener(e -> showInfoDialog());
        helpMenu.add(infoItem);

        JMenuItem aboutItem = new JMenuItem("Hakkında");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);

        menuBar.add(helpMenu);

        return menuBar;
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(10, 10));
        headerPanel.setBackground(BG_COLOR);

        // Başlık
        JLabel titleLabel = new JLabel("HaToKuSe İstemci");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(PRIMARY_COLOR);

        // Bağlantı paneli
        connectionPanel = createConnectionPanel();

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(connectionPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createConnectionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(189, 195, 199), 1, true),
                new EmptyBorder(5, 10, 5, 10)));

        panel.add(new JLabel("Lider Sunucu:"));

        hostField = new JTextField("localhost", 10);
        hostField.setToolTipText("Lider sunucu adresi");
        panel.add(hostField);

        panel.add(new JLabel(":"));

        portField = new JTextField("5000", 5);
        portField.setToolTipText("Lider sunucu portu");
        panel.add(portField);

        connectButton = createStyledButton("Bağlan", SUCCESS_COLOR);
        connectButton.addActionListener(e -> toggleConnection());
        panel.add(connectButton);

        statusLabel = new JLabel("● Bağlı Değil");
        statusLabel.setForeground(ERROR_COLOR);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(statusLabel);

        return panel;
    }

    private JPanel createMessagePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG_COLOR);

        // Sol - Mesaj gönderme
        JPanel sendPanel = createCard("Mesaj İşlemleri");
        sendPanel.setLayout(new BorderLayout(10, 10));

        // Form paneli
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(CARD_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Mesaj ID
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Mesaj ID:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1;
        messageIdField = new JTextField(20);
        messageIdField.setToolTipText("Benzersiz mesaj kimliği (sayı)");
        formPanel.add(messageIdField, gbc);

        // Mesaj içeriği
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Mesaj:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        messageArea = new JTextArea(5, 20);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setToolTipText("Mesaj içeriği");
        JScrollPane messageScroll = new JScrollPane(messageArea);
        formPanel.add(messageScroll, gbc);

        sendPanel.add(formPanel, BorderLayout.CENTER);

        // Buton container - iki satır
        JPanel buttonContainer = new JPanel();
        buttonContainer.setLayout(new BoxLayout(buttonContainer, BoxLayout.Y_AXIS));
        buttonContainer.setBackground(CARD_COLOR);

        // İlk satır - Ana butonlar
        JPanel buttonRow1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonRow1.setBackground(CARD_COLOR);

        setButton = createStyledButton("SET - Kaydet", PRIMARY_COLOR);
        setButton.addActionListener(e -> executeSet());
        setButton.setEnabled(false);
        buttonRow1.add(setButton);

        getButton = createStyledButton("GET - Getir", new Color(155, 89, 182));
        getButton.addActionListener(e -> executeGet());
        getButton.setEnabled(false);
        buttonRow1.add(getButton);

        clearButton = createStyledButton("Temizle", new Color(149, 165, 166));
        clearButton.addActionListener(e -> {
            messageIdField.setText("");
            messageArea.setText("");
        });
        buttonRow1.add(clearButton);

        JButton browseButton = createStyledButton("Mesajları Göster", new Color(52, 73, 94));
        browseButton.addActionListener(e -> showStoredMessages());
        buttonRow1.add(browseButton);

        buttonContainer.add(buttonRow1);

        // İkinci satır - Silme butonları
        JPanel buttonRow2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonRow2.setBackground(CARD_COLOR);

        JButton delButton = createStyledButton("DEL - Sil", ERROR_COLOR);
        delButton.addActionListener(e -> executeDel());
        buttonRow2.add(delButton);

        JButton deleteAllButton = createStyledButton("Tümünü Sil", new Color(139, 0, 0));
        deleteAllButton.addActionListener(e -> executeDeleteAll());
        buttonRow2.add(deleteAllButton);

        buttonContainer.add(buttonRow2);

        sendPanel.add(buttonContainer, BorderLayout.SOUTH);

        panel.add(sendPanel, BorderLayout.CENTER);

        // Sağ - Hızlı yardım
        JPanel helpPanel = createCard("Hızlı Yardım");
        helpPanel.setPreferredSize(new Dimension(200, 0));
        helpPanel.setLayout(new BorderLayout());

        JTextArea helpText = new JTextArea();
        helpText.setEditable(false);
        helpText.setBackground(CARD_COLOR);
        helpText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        helpText.setText(
                "📝 SET Komutu:\n" +
                        "Mesajı sunucuya kaydeder.\n" +
                        "Tolerans sayısı kadar üyeye\n" +
                        "replike edilir.\n\n" +
                        "📖 GET Komutu:\n" +
                        "Mesajı ID'ye göre getirir.\n" +
                        "Crash durumunda diğer\n" +
                        "üyelerden alınır.\n\n" +
                        "�️ DEL Komutu:\n" +
                        "Mesajı tüm üyelerden siler.\n" +
                        "Tümünü Sil ile toplu\n" +
                        "silme yapılabilir.\n\n" +
                        "💡 İpucu:\n" +
                        "Mesajları Göster ile\n" +
                        "kayıtlı mesajları görün.");
        helpPanel.add(helpText, BorderLayout.CENTER);

        panel.add(helpPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createLogPanel() {
        JPanel panel = createCard("İşlem Günlüğü");
        panel.setLayout(new BorderLayout());
        panel.setPreferredSize(new Dimension(0, 150));

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setBackground(new Color(44, 62, 80));
        logArea.setForeground(new Color(236, 240, 241));

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Temizle butonu
        JButton clearLogButton = new JButton("Günlüğü Temizle");
        clearLogButton.addActionListener(e -> logArea.setText(""));
        panel.add(clearLogButton, BorderLayout.SOUTH);

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
                        new Font("Segoe UI", Font.BOLD, 12),
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
        button.setPreferredSize(new Dimension(120, 35));

        // Hover efekti
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

    private void toggleConnection() {
        if (connected) {
            disconnect();
        } else {
            connect();
        }
    }

    private void connect() {
        String host = hostField.getText().trim();
        int port;

        try {
            port = Integer.parseInt(portField.getText().trim());
        } catch (NumberFormatException e) {
            showError("Geçersiz port numarası!");
            return;
        }

        try {
            socket = new Socket(host, port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            connected = true;

            // UI güncelle
            connectButton.setText("Bağlantıyı Kes");
            connectButton.setBackground(ERROR_COLOR);
            statusLabel.setText("● Bağlı");
            statusLabel.setForeground(SUCCESS_COLOR);
            hostField.setEnabled(false);
            portField.setEnabled(false);
            setButton.setEnabled(true);
            getButton.setEnabled(true);

            log("✓ Bağlantı kuruldu: " + host + ":" + port);

        } catch (IOException e) {
            showError("Bağlantı hatası: " + e.getMessage());
            log("✗ Bağlantı hatası: " + e.getMessage());
        }
    }

    private void disconnect() {
        try {
            if (reader != null)
                reader.close();
            if (writer != null)
                writer.close();
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            // Ignore
        }

        connected = false;

        // UI güncelle
        connectButton.setText("Bağlan");
        connectButton.setBackground(SUCCESS_COLOR);
        statusLabel.setText("● Bağlı Değil");
        statusLabel.setForeground(ERROR_COLOR);
        hostField.setEnabled(true);
        portField.setEnabled(true);
        setButton.setEnabled(false);
        getButton.setEnabled(false);

        log("○ Bağlantı kesildi");
    }

    private void executeSet() {
        String messageId = messageIdField.getText().trim();
        String message = messageArea.getText().trim();

        if (messageId.isEmpty()) {
            showError("Mesaj ID boş olamaz!");
            return;
        }

        if (message.isEmpty()) {
            showError("Mesaj içeriği boş olamaz!");
            return;
        }

        String command = HaToKuSeProtocol.createSetCommand(messageId, message);
        log("→ " + command);

        writer.println(command);

        try {
            String response = reader.readLine();
            log("← " + response);

            if (response != null && response.startsWith("OK")) {
                showSuccess("Mesaj başarıyla kaydedildi!");
            } else {
                showError("Kayıt başarısız: " + response);
            }
        } catch (IOException e) {
            showError("Okuma hatası: " + e.getMessage());
            disconnect();
        }
    }

    private void executeGet() {
        String messageId = messageIdField.getText().trim();

        if (messageId.isEmpty()) {
            showError("Mesaj ID boş olamaz!");
            return;
        }

        String command = HaToKuSeProtocol.createGetCommand(messageId);
        log("→ " + command);

        writer.println(command);

        try {
            String response = reader.readLine();
            log("← " + response);

            if (response != null && response.startsWith("OK")) {
                String content = response.length() > 3 ? response.substring(3).trim() : "";
                messageArea.setText(content);
                showSuccess("Mesaj getirildi!");
            } else {
                showError("Mesaj bulunamadı: " + response);
            }
        } catch (IOException e) {
            showError("Okuma hatası: " + e.getMessage());
            disconnect();
        }
    }

    private void executeDel() {
        String messageId = messageIdField.getText().trim();

        if (messageId.isEmpty()) {
            showError("Mesaj ID boş olamaz!");
            return;
        }

        // Silme onayı iste
        int confirm = JOptionPane.showConfirmDialog(this,
                "\"" + messageId + "\" mesajını silmek istediğinize emin misiniz?\n" +
                        "Bu işlem geri alınamaz.",
                "Silme Onayı", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        if (!connected) {
            showError("Önce sunucuya bağlanın!");
            return;
        }

        String command = HaToKuSeProtocol.createDelCommand(messageId);
        log("→ " + command);

        writer.println(command);

        try {
            String response = reader.readLine();
            log("← " + response);

            if (response != null && response.startsWith("OK")) {
                messageIdField.setText("");
                messageArea.setText("");
                showSuccess("Mesaj başarıyla silindi!");
            } else {
                showError("Silme başarısız: " + response);
            }
        } catch (IOException e) {
            showError("Okuma hatası: " + e.getMessage());
            disconnect();
        }
    }

    private void executeDeleteAll() {
        // Tüm mesajları disk üzerinden oku
        java.io.File membersDir = new java.io.File("./data/members");

        if (!membersDir.exists() || !membersDir.isDirectory()) {
            showError("Mesaj dizini bulunamadı!");
            return;
        }

        // Benzersiz mesaj ID'lerini topla
        java.util.Set<String> messageIds = new java.util.HashSet<>();
        java.io.File[] memberDirs = membersDir.listFiles(java.io.File::isDirectory);
        if (memberDirs != null) {
            for (java.io.File memberDir : memberDirs) {
                java.io.File[] msgFiles = memberDir.listFiles((dir, name) -> name.endsWith(".msg"));
                if (msgFiles != null) {
                    for (java.io.File msgFile : msgFiles) {
                        messageIds.add(msgFile.getName().replace(".msg", ""));
                    }
                }
            }
        }

        if (messageIds.isEmpty()) {
            showError("Silinecek mesaj bulunamadı!");
            return;
        }

        // Onay iste
        int confirm = JOptionPane.showConfirmDialog(this,
                "Toplam " + messageIds.size() + " benzersiz mesaj silinecek.\n" +
                        "Bu işlem geri alınamaz!\n\n" +
                        "Devam etmek istiyor musunuz?",
                "Tümünü Sil Onayı", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        if (!connected) {
            showError("Önce sunucuya bağlanın!");
            return;
        }

        // Tüm mesajları sil
        int successCount = 0;
        int errorCount = 0;

        for (String msgId : messageIds) {
            String command = HaToKuSeProtocol.createDelCommand(msgId);
            writer.println(command);

            try {
                String response = reader.readLine();
                if (response != null && response.startsWith("OK")) {
                    successCount++;
                } else {
                    errorCount++;
                }
            } catch (IOException e) {
                errorCount++;
            }
        }

        log("Tümünü sil tamamlandı: " + successCount + " başarılı, " + errorCount + " hatalı");

        if (successCount > 0) {
            showSuccess("Toplam " + successCount + " mesaj silindi!\n" +
                    (errorCount > 0 ? errorCount + " mesaj silinemedi." : ""));
        } else {
            showError("Hiçbir mesaj silinemedi!");
        }
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append("[" + java.time.LocalTime.now().toString().substring(0, 8) + "] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Hata", JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Başarılı", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showInfoDialog() {
        InfoDialog dialog = new InfoDialog(this);
        dialog.setVisible(true);
    }

    private void showAboutDialog() {
        String about = "HaToKuSe - Hata Tolere Kuyruk Servisi\n\n" +
                "Dağıtık, hata-tolere mesaj kayıt sistemi.\n" +
                "Java ve gRPC kullanılarak geliştirilmiştir.\n\n" +
                "Versiyon: 1.0\n" +
                "© 2026";

        JOptionPane.showMessageDialog(this, about, "Hakkında", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showStoredMessages() {
        // Göreceli yol kullan - storage ile aynı
        java.io.File membersDir = new java.io.File("./data/members");

        if (!membersDir.exists() || !membersDir.isDirectory()) {
            JOptionPane.showMessageDialog(this,
                    "Mesaj dizini bulunamadı!\nHenüz mesaj kaydedilmemiş olabilir.",
                    "Bilgi", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Tüm mesajları topla
        java.util.List<String[]> messages = new java.util.ArrayList<>();

        java.io.File[] memberDirs = membersDir.listFiles(java.io.File::isDirectory);
        if (memberDirs != null) {
            for (java.io.File memberDir : memberDirs) {
                // .msg dosyalarını doğrudan üye dizininde ara
                java.io.File[] msgFiles = memberDir.listFiles((dir, name) -> name.endsWith(".msg"));
                if (msgFiles != null) {
                    for (java.io.File msgFile : msgFiles) {
                        try {
                            String msgId = msgFile.getName().replace(".msg", "");
                            String content = new String(java.nio.file.Files.readAllBytes(msgFile.toPath()));
                            // İlk 50 karakteri göster
                            if (content.length() > 50) {
                                content = content.substring(0, 50) + "...";
                            }
                            messages.add(new String[] { msgId, memberDir.getName(), content.trim() });
                        } catch (Exception e) {
                            // Okuma hatası, atla
                        }
                    }
                }
            }
        }

        if (messages.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Henüz kaydedilmiş mesaj yok!",
                    "Bilgi", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Tablo oluştur
        String[] columns = { "Mesaj ID", "Üye", "İçerik" };
        String[][] data = messages.toArray(new String[0][]);

        JTable table = new JTable(data, columns);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(25);
        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(80);
        table.getColumnModel().getColumn(2).setPreferredWidth(300);

        // Tablo seçildiğinde mesajı getir
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    messageIdField.setText((String) table.getValueAt(row, 0));
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(500, 300));

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(new JLabel("Toplam " + messages.size() + " mesaj bulundu (benzersiz değil, tüm kopyalar):"),
                BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(new JLabel("Bir mesaj seçip GET ile içeriğini alabilirsiniz."), BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(this, panel, "Kayıtlı Mesajlar", JOptionPane.PLAIN_MESSAGE);
    }

    public static void main(String[] args) {
        // Look and Feel ayarla
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Varsayılan kullan
        }

        SwingUtilities.invokeLater(() -> {
            ClientGUI gui = new ClientGUI();
            gui.setVisible(true);
        });
    }
}
