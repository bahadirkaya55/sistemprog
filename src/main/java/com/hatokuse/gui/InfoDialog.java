package com.hatokuse.gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * Detaylı kullanım kılavuzu dialog'u
 */
public class InfoDialog extends JDialog {

    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color BG_COLOR = new Color(236, 240, 241);

    public InfoDialog(JFrame parent) {
        super(parent, "HaToKuSe Kullanım Kılavuzu", true);
        initializeUI();
    }

    private void initializeUI() {
        setSize(700, 600);
        setLocationRelativeTo(getParent());
        setBackground(BG_COLOR);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // Genel Bakış
        tabbedPane.addTab("🏠 Genel Bakış", createOverviewPanel());

        // Başlangıç
        tabbedPane.addTab("🚀 Başlangıç", createGettingStartedPanel());

        // Komutlar
        tabbedPane.addTab("📝 Komutlar", createCommandsPanel());

        // Mimari
        tabbedPane.addTab("🏗️ Mimari", createArchitecturePanel());

        // Test Senaryoları
        tabbedPane.addTab("🧪 Test", createTestPanel());

        // SSS
        tabbedPane.addTab("❓ SSS", createFAQPanel());

        add(tabbedPane);

        // Kapat butonu
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("Kapat");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        JTextPane textPane = createStyledTextPane();
        textPane.setText(
                "HaToKuSe - Hata Tolere Kuyruk Servisi\n\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                        "HaToKuSe, dağıtık ve hata-tolere bir mesaj kayıt sistemidir.\n\n" +
                        "📌 TEMEL ÖZELLİKLER\n\n" +
                        "   ✓ Dağıtık Mimari: Mesajlar birden fazla sunucuda saklanır\n" +
                        "   ✓ Hata Toleransı: Sunucu çökse bile veriler korunur\n" +
                        "   ✓ Yük Dengeleme: Mesajlar üyelere dengeli dağıtılır\n" +
                        "   ✓ Dinamik Üyelik: Yeni sunucular sisteme eklenebilir\n" +
                        "   ✓ Text Tabanlı Protokol: Basit SET/GET komutları\n\n" +
                        "📌 SİSTEM BİLEŞENLERİ\n\n" +
                        "   🔷 Lider Sunucu: İstemci isteklerini alır ve işler\n" +
                        "   🔷 Üye Sunucular: Mesajları diske kaydeder\n" +
                        "   🔷 İstemci (Bu GUI): Mesaj gönderir ve alır\n\n" +
                        "📌 TOLERANS KAVRAMI\n\n" +
                        "   Tolerans değeri, her mesajın kaç farklı üyede\n" +
                        "   saklanacağını belirler.\n\n" +
                        "   Örnek: tolerance=2 ise, her mesaj 2 üyede tutulur.\n" +
                        "   Bir üye çökse bile mesaj diğerinden alınabilir.\n");

        panel.add(new JScrollPane(textPane), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createGettingStartedPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        JTextPane textPane = createStyledTextPane();
        textPane.setText(
                "Sistemi Çalıştırma\n\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                        "📌 ADIM 1: Lider Sunucuyu Başlatın\n\n" +
                        "   Yeni bir terminal açın ve şu komutu çalıştırın:\n\n" +
                        "   java -cp target/hatokuse-1.0-SNAPSHOT-jar-with-dependencies.jar \\\n" +
                        "        com.hatokuse.leader.LeaderServer\n\n" +
                        "   Lider, port 5000 (istemci) ve 5001 (gRPC) üzerinde çalışır.\n\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                        "📌 ADIM 2: Üye Sunucuları Başlatın\n\n" +
                        "   Her üye için ayrı terminal açın:\n\n" +
                        "   # Üye 1\n" +
                        "   java -cp target/hatokuse-1.0-SNAPSHOT-jar-with-dependencies.jar \\\n" +
                        "        com.hatokuse.member.MemberServer --id member1 --port 5002\n\n" +
                        "   # Üye 2\n" +
                        "   java -cp target/hatokuse-1.0-SNAPSHOT-jar-with-dependencies.jar \\\n" +
                        "        com.hatokuse.member.MemberServer --id member2 --port 5003\n\n" +
                        "   Her üye için farklı --id ve --port kullanın.\n\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                        "📌 ADIM 3: Bu GUI'yi Kullanın\n\n" +
                        "   1. 'Lider Sunucu' alanına: localhost:5000 yazın\n" +
                        "   2. 'Bağlan' butonuna tıklayın\n" +
                        "   3. Mesaj ID ve içerik girin\n" +
                        "   4. SET veya GET butonuna tıklayın\n\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                        "📌 TOLERANS AYARLAMA\n\n" +
                        "   tolerance.conf dosyasında:\n   tolerance=2\n\n" +
                        "   Bu değer, sistemi başlatmadan önce ayarlanmalıdır.\n");

        panel.add(new JScrollPane(textPane), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCommandsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        JTextPane textPane = createStyledTextPane();
        textPane.setText(
                "Kullanılabilir Komutlar\n\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                        "📌 SET KOMUTU\n\n" +
                        "   Format: SET <mesaj_id> <mesaj_içeriği>\n\n" +
                        "   Açıklama:\n" +
                        "   - Mesajı sisteme kaydeder\n" +
                        "   - Tolerans sayısı kadar üyeye replike eder\n" +
                        "   - Tüm üyeler kayıt tamamlayınca OK döner\n\n" +
                        "   Örnek:\n" +
                        "   SET 100 Merhaba Dünya!\n" +
                        "   SET 101 Bu bir test mesajıdır.\n\n" +
                        "   Yanıtlar:\n" +
                        "   OK             → Kayıt başarılı\n" +
                        "   ERROR <sebep>  → Kayıt başarısız\n\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                        "📌 GET KOMUTU\n\n" +
                        "   Format: GET <mesaj_id>\n\n" +
                        "   Açıklama:\n" +
                        "   - Mesajı ID'ye göre getirir\n" +
                        "   - Mesajı tutan üyelerden birine sorar\n" +
                        "   - Üye çökmüşse başka üyeyi dener\n\n" +
                        "   Örnek:\n" +
                        "   GET 100\n\n" +
                        "   Yanıtlar:\n" +
                        "   OK <mesaj>     → Mesaj bulundu\n" +
                        "   ERROR <sebep>  → Mesaj bulunamadı\n\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                        "📌 DEL KOMUTU\n\n" +
                        "   Format: DEL <mesaj_id>\n\n" +
                        "   Açıklama:\n" +
                        "   - Mesajı tüm üyelerden siler\n" +
                        "   - Silme işlemi geri alınamaz\n" +
                        "   - Tümünü Sil ile toplu silme yapılabilir\n\n" +
                        "   Örnek:\n" +
                        "   DEL 100\n\n" +
                        "   Yanıtlar:\n" +
                        "   OK             → Silme başarılı\n" +
                        "   ERROR <sebep>  → Silme başarısız\n\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                        "📌 GUI KULLANIMI\n\n" +
                        "   Bu GUI'de komutları manuel yazmanıza gerek yok.\n" +
                        "   Sadece 'Mesaj ID' ve 'Mesaj' alanlarını doldurun\n" +
                        "   ve ilgili butona tıklayın.\n");

        panel.add(new JScrollPane(textPane), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createArchitecturePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        JTextPane textPane = createStyledTextPane();
        textPane.setText(
                "Sistem Mimarisi\n\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                        "📌 MİMARİ DİYAGRAM\n\n" +
                        "   ┌─────────────┐      TCP/Text       ┌─────────────┐\n" +
                        "   │   İstemci   │ ──────────────────► │    Lider    │\n" +
                        "   │   (Bu GUI)  │    SET/GET komut    │   Sunucu    │\n" +
                        "   └─────────────┘                     └──────┬──────┘\n" +
                        "                                              │\n" +
                        "                                              │ gRPC/Protobuf\n" +
                        "                    ┌───────────────────┬─────┴─────┬───────────────────┐\n" +
                        "                    ▼                   ▼           ▼                   ▼\n" +
                        "              ┌──────────┐       ┌──────────┐ ┌──────────┐       ┌──────────┐\n" +
                        "              │  Üye 1   │       │  Üye 2   │ │  Üye 3   │       │  Üye N   │\n" +
                        "              └────┬─────┘       └────┬─────┘ └────┬─────┘       └────┬─────┘\n" +
                        "                   │                  │            │                  │\n" +
                        "                   ▼                  ▼            ▼                  ▼\n" +
                        "                [Disk]             [Disk]       [Disk]             [Disk]\n\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                        "📌 VERİ AKIŞI (SET)\n\n" +
                        "   1. İstemci → Lider: SET 100 MesajIcerigi\n" +
                        "   2. Lider, tolerans değerine göre üye seçer\n" +
                        "   3. Lider → Üyeler: gRPC ile mesaj gönderir\n" +
                        "   4. Üyeler mesajı diske kaydeder\n" +
                        "   5. Üyeler → Lider: Onay gönderir\n" +
                        "   6. Lider → İstemci: OK\n\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                        "📌 VERİ AKIŞI (GET)\n\n" +
                        "   1. İstemci → Lider: GET 100\n" +
                        "   2. Lider, mesajı tutan üyeleri bulur\n" +
                        "   3. Lider → Üye: gRPC ile mesaj ister\n" +
                        "   4. Üye çökmüşse → Sonraki üyeyi dener\n" +
                        "   5. Üye → Lider: Mesaj içeriği\n" +
                        "   6. Lider → İstemci: OK MesajIcerigi\n\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                        "📌 YÜK DENGELEME\n\n" +
                        "   Sistem, round-robin grup bazlı yük dengeleme uygular.\n\n" +
                        "   Örnek (tolerance=2, 4 üye):\n" +
                        "   - Mesaj 1 → Üye 1, Üye 2\n" +
                        "   - Mesaj 2 → Üye 3, Üye 4\n" +
                        "   - Mesaj 3 → Üye 1, Üye 2\n" +
                        "   - Mesaj 4 → Üye 3, Üye 4\n\n" +
                        "   Bu sayede mesajlar eşit dağılır.\n");

        panel.add(new JScrollPane(textPane), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createTestPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        JTextPane textPane = createStyledTextPane();
        textPane.setText(
                "Test Senaryoları\n\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                        "📌 TEST 1: TEMEL İŞLEVSELLİK (Tolerance=2)\n\n" +
                        "   1. tolerance.conf → tolerance=2\n" +
                        "   2. 1 Lider + 4 Üye başlat (5 terminal)\n" +
                        "   3. 1000 mesaj gönder\n" +
                        "   4. Beklenen sonuç:\n" +
                        "      - 500 mesaj Üye 1-2'de\n" +
                        "      - 500 mesaj Üye 3-4'te\n\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                        "📌 TEST 2: CRASH RECOVERY\n\n" +
                        "   1. SET 500 TestMesaji komutu gönder\n" +
                        "   2. Mesaj Üye 3 ve Üye 4'te kayıtlı\n" +
                        "   3. Üye 3'ü kapat (terminal kapat)\n" +
                        "   4. GET 500 komutu gönder\n" +
                        "   5. Beklenen: Mesaj Üye 4'ten alınır\n\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                        "📌 TEST 3: DİNAMİK ÜYE (Tolerance=3)\n\n" +
                        "   1. tolerance.conf → tolerance=3\n" +
                        "   2. 1 Lider + 6 Üye başlat (7 terminal)\n" +
                        "   3. 9000 mesaj gönder\n" +
                        "   4. Beklenen sonuç:\n" +
                        "      - 4500 mesaj Üye 1-2-3'te\n" +
                        "      - 4500 mesaj Üye 4-5-6'da\n" +
                        "   5. Yeni Üye 7 ekle\n" +
                        "   6. Yeni mesajlar gelince dengelenir\n\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                        "📌 TOPLU TEST (Bu GUI ile)\n\n" +
                        "   Terminal'den toplu test yapmak için:\n\n" +
                        "   java -cp target/hatokuse-1.0-SNAPSHOT-jar-with-dependencies.jar \\\n" +
                        "        com.hatokuse.client.HaToKuSeClient --batch 1000\n\n" +
                        "   Bu komut 1000 SET işlemi yapar.\n");

        panel.add(new JScrollPane(textPane), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createFAQPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        JTextPane textPane = createStyledTextPane();
        textPane.setText(
                "Sık Sorulan Sorular\n\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                        "❓ Bağlantı kuramıyorum, ne yapmalıyım?\n\n" +
                        "   ✓ Lider sunucunun çalıştığından emin olun\n" +
                        "   ✓ Port numarasının doğru olduğunu kontrol edin (varsayılan: 5000)\n" +
                        "   ✓ Güvenlik duvarı ayarlarını kontrol edin\n\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                        "❓ Mesaj kaydedilmedi hatası alıyorum?\n\n" +
                        "   ✓ En az 'tolerance' kadar üye çalışıyor olmalı\n" +
                        "   ✓ Örn: tolerance=2 ise en az 2 üye gerekli\n" +
                        "   ✓ Üyelerin lidere başarıyla kayıt olduğunu kontrol edin\n\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                        "❓ Mesaj bulunamadı hatası alıyorum?\n\n" +
                        "   ✓ Mesajın daha önce SET ile kaydedilmiş olması gerekir\n" +
                        "   ✓ Mesajı tutan tüm üyeler çökmüş olabilir\n" +
                        "   ✓ Doğru mesaj ID kullandığınızdan emin olun\n\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                        "❓ Tolerans değerini nasıl değiştiririm?\n\n" +
                        "   1. tolerance.conf dosyasını düzenleyin\n" +
                        "   2. tolerance=3 gibi yeni değer yazın\n" +
                        "   3. Lider sunucuyu yeniden başlatın\n\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                        "❓ Yeni üye nasıl eklerim?\n\n" +
                        "   Herhangi bir zamanda yeni üye başlatabilirsiniz:\n" +
                        "   java ... MemberServer --id yeniUye --port 5006\n\n" +
                        "   Üye otomatik olarak lidere kayıt olur.\n" +
                        "   Yeni mesajlar bu üyeye de dağıtılmaya başlar.\n\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                        "❓ Mesajlar nerede saklanıyor?\n\n" +
                        "   Her üye kendi mesajlarını şurada saklar:\n" +
                        "   ./data/members/<uye_id>/\n\n" +
                        "   Her mesaj ayrı bir .msg dosyası olarak tutulur.\n\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                        "❓ 'Address already in use' hatası alıyorum?\n\n" +
                        "   Port zaten başka bir işlem tarafından kullanılıyor.\n" +
                        "   Çözüm:\n" +
                        "   1. netstat -ano | findstr :5001 ile PID bulun\n" +
                        "   2. taskkill /PID <numara> /F ile sonlandırın\n" +
                        "   3. Veya Görev Yöneticisi'nden Java işlemini kapatın\n\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                        "❓ Mesaj nasıl silinir?\n\n" +
                        "   DEL komutu ile mesaj silebilirsiniz:\n" +
                        "   - 'DEL - Sil' butonu: Tek mesaj siler\n" +
                        "   - 'Tümünü Sil' butonu: Tüm mesajları siler\n" +
                        "   Silme işlemi geri alınamaz!\n");

        panel.add(new JScrollPane(textPane), BorderLayout.CENTER);
        return panel;
    }

    private JTextPane createStyledTextPane() {
        JTextPane textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setFont(new Font("Consolas", Font.PLAIN, 13));
        textPane.setBackground(Color.WHITE);
        textPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        return textPane;
    }
}
