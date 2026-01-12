# HaToKuSe - Hata Tolere Kuyruk Servisi

Dağıtık, hata-tolere mesaj kayıt sistemi. Java ve gRPC kullanılarak geliştirilmiştir.

## Özellikler

- ✅ Java programlama dili
- ✅ gRPC gönderim fonksiyonları
- ✅ Dağıtık, hata-tolere abonelik sistemi
- ✅ HaToKuSe protokolü (sınıf tabanlı)
- ✅ Lider sunucu - Üye sunucu mimarisi
- ✅ Text tabanlı istemci komutları (SET/GET)
- ✅ tolerance.conf dosyasından tolerans okuma
- ✅ Mesajların diske kaydedilmesi
- ✅ Lider-Üye arası protobuf haberleşme
- ✅ Dinamik üye sistemi (n sayıda giriş/çıkış)
- ✅ Lider periyodik mesaj sayısı raporlama
- ✅ Üyeler periyodik mesaj sayısı raporlama
- ✅ Yük dağılımı (load balancing)
- ✅ Hata toleransı (crash recovery)

## Disk Kayıt Biçimleri (IO Modları)

Sistem 4 farklı disk kayıt biçimini destekler:

| Mod | Açıklama | Kullanım Alanı |
|-----|----------|----------------|
| **STANDARD** | Normal `Files.write/read` | Basit kullanım |
| **BUFFERED** | `BufferedOutputStream/BufferedInputStream` | Genel amaçlı, varsayılan |
| **ZERO_COPY** | `FileChannel` ile kernel bypass | Yüksek throughput |
| **MEMORY_MAPPED** | `MappedByteBuffer` ile bellek eşleme | Büyük dosyalar |

**Zero-Copy İlkesi:** `FileChannel.transferTo/transferFrom` ile CPU kullanımı minimize edilir.


## Gereksinimler

- Java 17+
- Maven 3.6+(maven kurulumu ve path ekleme için video https://www.youtube.com/watch?v=lLEA8xwBSF4 )

## Derleme

```bash
mvn clean compile
mvn package -DskipTests
```

## Çalıştırma

### 1. Lider Sunucu Başlatma

```bash
java -cp target/hatokuse-1.0-SNAPSHOT-jar-with-dependencies.jar com.hatokuse.leader.LeaderServer
```

Seçenekler:
- `--client-port <port>` - İstemci bağlantı portu (varsayılan: 5000)
- `--grpc-port <port>` - gRPC sunucu portu (varsayılan: 5001)
- `--tolerance <n>` - Hata tolerans değeri

### 2. Üye Sunucu Başlatma

```bash
# Üye 1
java -cp target/hatokuse-1.0-SNAPSHOT-jar-with-dependencies.jar com.hatokuse.member.MemberServer --id member1 --port 5002

# Üye 2
java -cp target/hatokuse-1.0-SNAPSHOT-jar-with-dependencies.jar com.hatokuse.member.MemberServer --id member2 --port 5003

# Üye 3
java -cp target/hatokuse-1.0-SNAPSHOT-jar-with-dependencies.jar com.hatokuse.member.MemberServer --id member3 --port 5004

# Üye 4
java -cp target/hatokuse-1.0-SNAPSHOT-jar-with-dependencies.jar com.hatokuse.member.MemberServer --id member4 --port 5005
```

Seçenekler:
- `--id <member_id>` - Üye kimliği
- `--port <port>` - gRPC sunucu portu
- `--leader-host <host>` - Lider adresi (varsayılan: localhost)
- `--leader-port <port>` - Lider gRPC portu (varsayılan: 5001)
- `--data-dir <path>` - Veri dizini

### 3. İstemci Başlatma

```bash
# İnteraktif mod (terminal)
java -cp target/hatokuse-1.0-SNAPSHOT-jar-with-dependencies.jar com.hatokuse.client.HaToKuSeClient

# Toplu test (1000 mesaj)
java -cp target/hatokuse-1.0-SNAPSHOT-jar-with-dependencies.jar com.hatokuse.client.HaToKuSeClient --batch 1000
```

### 4. GUI İstemci (Önerilen)

```bash
# Modern grafiksel arayüz
java -cp target/hatokuse-1.0-SNAPSHOT-jar-with-dependencies.jar com.hatokuse.gui.ClientGUI
```

GUI özellikleri:
- Bağlantı yönetimi
- SET/GET işlemleri
- İşlem günlüğü
- Detaylı kullanım kılavuzu (Yardım → Kullanım Kılavuzu)

### 5. Lider Dashboard (İzleme)

```bash
# Sistem izleme paneli
java -cp target/hatokuse-1.0-SNAPSHOT-jar-with-dependencies.jar com.hatokuse.gui.LeaderDashboard
```

## Protokol

### Komutlar (İstemci → Lider)

```
SET <message_id> <message>
GET <message_id>
```

### Yanıtlar (Lider → İstemci)

```
OK
OK <message>
ERROR <error_message>
```

## Test Senaryoları

### Test 1: Tolerance=2, 5 Terminal

1. `tolerance.conf` dosyasında `tolerance=2` ayarla
2. 1 lider + 4 üye başlat
3. 1000 SET mesajı gönder
4. Yük dağılımını kontrol et (500-500 bölünmeli)
5. Bir üyeyi kapat ve GET ile mesaj al

### Test 2: Tolerance=3, 7 Terminal

1. `tolerance.conf` dosyasında `tolerance=3` ayarla
2. 1 lider + 6 üye başlat
3. 9000 SET mesajı gönder
4. Yük dağılımını kontrol et (4500-4500 bölünmeli)
5. 2 üyeyi kapat ve GET ile mesaj al

## Mimari

```
┌─────────────┐     TCP/Text      ┌─────────────┐
│   İstemci   │ ─────────────────▶│    Lider    │
└─────────────┘  SET/GET komutlar └──────┬──────┘
                                         │
                                         │ gRPC/Protobuf
                    ┌────────────────────┼────────────────────┐
                    ▼                    ▼                    ▼
             ┌────────────┐       ┌────────────┐       ┌────────────┐
             │   Üye 1    │       │   Üye 2    │       │   Üye N    │
             └─────┬──────┘       └─────┬──────┘       └─────┬──────┘
                   │                    │                    │
                   ▼                    ▼                    ▼
              [Disk]               [Disk]               [Disk]
```

## Lisans

MIT
