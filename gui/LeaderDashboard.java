package com.hatokuse.gui;

import com.hatokuse.config.ConfigReader;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.Timer;

/**
 * Lider Sunucu Dashboard GUI
 * Sistem durumunu görsel olarak gösterir
 */
public class LeaderDashboard extends JFrame {

    // Renkler
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color SUCCESS_COLOR = new Color(39, 174, 96);
    private static final Color WARNING_COLOR = new Color(241, 196, 15);
    private static final Color ERROR_COLOR = new Color(192, 57, 43);
    private static final Color BG_COLOR = new Color(44, 62, 80);
    private static final Color CARD_COLOR = new Color(52, 73, 94);
    private static final Color TEXT_COLOR = new Color(236, 240, 241);

    // GUI Bileşenleri
    private JLabel toleranceLabel;
    private JLabel memberCountLabel;
    private JLabel messageCountLabel;
    private JPanel membersPanel;
    private JTextArea logArea;
    private JLabel statusLabel;

    // Veriler
    private int tolerance = 2;
    private Map<String, MemberStatus> members = new HashMap<>();

    public LeaderDashboard() {
        loadConfig();
        initializeUI();
        startUpdateTimer();
    }

    private void loadConfig() {
        try {
            ConfigReader config = new ConfigReader();
            config.loadToleranceConfig();
            tolerance = config.getTolerance();
        } catch (IOException e) {
            // Varsayılan kullan
        }
    }

    private void initializeUI() {
        setTitle("HaToKuSe Lider Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_COLOR);

        // Ana panel
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(BG_COLOR);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Üst - Başlık
        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);

        // Orta - İstatistikler ve Üyeler
        JPanel centerPanel = new JPanel(new BorderLayout(15, 15));
        centerPanel.setBackground(BG_COLOR);
        centerPanel.add(createStatsPanel(), BorderLayout.NORTH);
        centerPanel.add(createMembersPanel(), BorderLayout.CENTER);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Alt - Log
        mainPanel.add(createLogPanel(), BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_COLOR);

        // Başlık
        JLabel titleLabel = new JLabel("🖥️ HaToKuSe Lider Dashboard");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(TEXT_COLOR);

        // Durum
        statusLabel = new JLabel("● Çalışıyor");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statusLabel.setForeground(SUCCESS_COLOR);

        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(statusLabel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 15, 0));
        panel.setBackground(BG_COLOR);

        // Tolerans kartı
        panel.add(createStatCard("🛡️ Tolerans", String.valueOf(tolerance), PRIMARY_COLOR));

        // Üye sayısı kartı
        JPanel memberCard = createStatCard("👥 Aktif Üye", "0", SUCCESS_COLOR);
        memberCountLabel = (JLabel) ((JPanel) memberCard.getComponent(0)).getComponent(1);
        panel.add(memberCard);

        // Mesaj sayısı kartı
        JPanel messageCard = createStatCard("📨 Toplam Mesaj", "0", WARNING_COLOR);
        messageCountLabel = (JLabel) ((JPanel) messageCard.getComponent(0)).getComponent(1);
        panel.add(messageCard);

        return panel;
    }

    private JPanel createStatCard(String title, String value, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(3, 0, 0, 0, accentColor),
                new EmptyBorder(15, 20, 15, 20)));

        JPanel content = new JPanel(new GridLayout(2, 1, 0, 5));
        content.setBackground(CARD_COLOR);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setForeground(new Color(149, 165, 166));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valueLabel.setForeground(TEXT_COLOR);

        content.add(titleLabel);
        content.add(valueLabel);

        card.add(content, BorderLayout.CENTER);

        return card;
    }

    private JPanel createMembersPanel() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(BG_COLOR);

        JLabel titleLabel = new JLabel("📋 Kayıtlı Üyeler");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setBorder(new EmptyBorder(10, 0, 10, 0));
        container.add(titleLabel, BorderLayout.NORTH);

        membersPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        membersPanel.setBackground(BG_COLOR);

        // Örnek üyeler (gerçek uygulamada dinamik olacak)
        addMemberCard("member1", "localhost:5002", 0, true);
        addMemberCard("member2", "localhost:5003", 0, true);
        addMemberCard("member3", "localhost:5004", 0, false);
        addMemberCard("member4", "localhost:5005", 0, true);

        JScrollPane scrollPane = new JScrollPane(membersPanel);
        scrollPane.setBorder(null);
        scrollPane.setBackground(BG_COLOR);
        scrollPane.getViewport().setBackground(BG_COLOR);
        container.add(scrollPane, BorderLayout.CENTER);

        return container;
    }

    private void addMemberCard(String id, String address, int messageCount, boolean active) {
        JPanel card = new JPanel(new BorderLayout(10, 5));
        card.setBackground(CARD_COLOR);
        card.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Sol - İkon ve ID
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(CARD_COLOR);

        JLabel iconLabel = new JLabel(active ? "🟢" : "🔴");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        leftPanel.add(iconLabel, BorderLayout.WEST);

        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.setBackground(CARD_COLOR);
        infoPanel.setBorder(new EmptyBorder(0, 10, 0, 0));

        JLabel idLabel = new JLabel(id);
        idLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        idLabel.setForeground(TEXT_COLOR);

        JLabel addressLabel = new JLabel(address);
        addressLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        addressLabel.setForeground(new Color(149, 165, 166));

        infoPanel.add(idLabel);
        infoPanel.add(addressLabel);
        leftPanel.add(infoPanel, BorderLayout.CENTER);

        card.add(leftPanel, BorderLayout.CENTER);

        // Sağ - Mesaj sayısı
        JPanel rightPanel = new JPanel(new GridLayout(2, 1));
        rightPanel.setBackground(CARD_COLOR);

        JLabel countLabel = new JLabel(String.valueOf(messageCount), SwingConstants.RIGHT);
        countLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        countLabel.setForeground(TEXT_COLOR);

        JLabel countTitle = new JLabel("mesaj", SwingConstants.RIGHT);
        countTitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        countTitle.setForeground(new Color(149, 165, 166));

        rightPanel.add(countLabel);
        rightPanel.add(countTitle);

        card.add(rightPanel, BorderLayout.EAST);

        membersPanel.add(card);

        members.put(id, new MemberStatus(id, address, messageCount, active));
    }

    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_COLOR);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setPreferredSize(new Dimension(0, 150));

        JLabel titleLabel = new JLabel("📝 Sistem Günlüğü");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(TEXT_COLOR);
        panel.add(titleLabel, BorderLayout.NORTH);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setBackground(new Color(30, 40, 50));
        logArea.setForeground(new Color(46, 204, 113));
        logArea.setBorder(new EmptyBorder(5, 5, 5, 5));

        // Demo loglar
        logArea.append("[21:42:00] Lider sunucu başlatıldı\n");
        logArea.append("[21:42:01] Tolerans değeri: " + tolerance + "\n");
        logArea.append("[21:42:05] member1 kayıt oldu (localhost:5002)\n");
        logArea.append("[21:42:06] member2 kayıt oldu (localhost:5003)\n");
        logArea.append("[21:42:10] member4 kayıt oldu (localhost:5005)\n");

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void startUpdateTimer() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> updateStats());
            }
        }, 0, 5000);
    }

    private void updateStats() {
        int activeCount = (int) members.values().stream().filter(m -> m.active).count();
        int totalMessages = members.values().stream().mapToInt(m -> m.messageCount).sum();

        memberCountLabel.setText(String.valueOf(activeCount));
        messageCountLabel.setText(String.valueOf(totalMessages));
    }

    public void log(String message) {
        SwingUtilities.invokeLater(() -> {
            String time = java.time.LocalTime.now().toString().substring(0, 8);
            logArea.append("[" + time + "] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private static class MemberStatus {
        String id;
        String address;
        int messageCount;
        boolean active;

        MemberStatus(String id, String address, int messageCount, boolean active) {
            this.id = id;
            this.address = address;
            this.messageCount = messageCount;
            this.active = active;
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Varsayılan kullan
        }

        SwingUtilities.invokeLater(() -> {
            LeaderDashboard dashboard = new LeaderDashboard();
            dashboard.setVisible(true);
        });
    }
}
