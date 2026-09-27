# Dexcom Data Collector - Android Frontend

An advanced Android client built to interface seamlessly with the [Glucose Data Collector Backend](https://github.com/ermanalper/glucose-data-collector). This application not only visualizes real-time glucose metrics but also acts as a central control hub for all devices connected to the backend's Server-Sent Events (SSE) tunnel.

## 🔗 Backend Repository
The core backend service, API endpoints, and SSE tunneling logic required for this frontend can be found here: 
👉 **[glucose-data-collector](https://github.com/ermanalper/glucose-data-collector)**

## Features
* **Glucose Visualization:** Renders real-time glucose data from the backend into interactive charts.
* **Timeframe Comparisons:** Compares glucose charts across different time periods side-by-side for trend analysis.
* **Insulin & Meal Tracking:** Logs and displays insulin intake and meal timestamps alongside glucose data.
* **Alarm Management:** Remotely acknowledge and disable backend glucose level alarms directly from the mobile app.
* **SSE Network Topology:** Monitors and lists all active frontend clients currently connected to the backend's SSE tunnel.
* **Remote Protocol Execution:** Triggers specific test protocols on other connected clients via the backend (e.g., executing rhythmic buzzer patterns on an ESP32 client).

## Tech Stack
* **UI Framework:** Android (Jetpack Compose)
* **Network:** Server-Sent Events (SSE) & REST API
* **Hardware Integration:** Remote protocol triggering for ESP32 and other IoT clients

# Dexcom Data Collector - Android Frontend

[Glucose Data Collector Backend](https://github.com/ermanalper/glucose-data-collector) ile tam entegre çalışan gelişmiş bir Android istemcisi. Bu uygulama glukoz metriklerini gerçek zamanlı olarak görselleştirmenin yanı sıra, sunucunun Server-Sent Events (SSE) tüneline bağlı tüm cihazlar için merkezi bir kontrol noktası olarak çalışır.

## 🔗 Backend Deposu
Bu frontend uygulamasının çalışması için gereken temel backend servisi, API uç noktaları ve SSE tünel yapısına bu repodan ulaşabilirsiniz:
👉 **[glucose-data-collector](https://github.com/ermanalper/glucose-data-collector)**

## Özellikler
* **Glukoz Görselleştirme:** Backend'den gelen eşzamanlı glukoz verilerini etkileşimli grafiklere döker.
* **Zaman Dilimi Karşılaştırmaları:** Trend analizi için farklı zaman aralıklarına ait glukoz grafiklerini yan yana karşılaştırmalı olarak sunar.
* **İnsülin ve Öğün Takibi:** İnsülin alımlarını ve öğün zamanlarını glukoz verileri ile birlikte kaydeder ve grafik üzerinde gösterir.
* **Alarm Yönetimi:** Backend üzerindeki glukoz seviyesi alarmlarını uygulama üzerinden uzaktan kontrol eder ve kapatır.
* **SSE Ağ İzleme:** Backend'in SSE tüneline bağlı aktif durumdaki diğer tüm frontend istemcilerini görüntüler.
* **Uzaktan Protokol Çalıştırma:** Ağa bağlı diğer istemcilerde test protokollerini tetikler (örneğin, bir ESP32 buzzer istemcisinde belirli bir ritimde ses çalma komutu gönderme).

## Tech Stack
* **Kullanıcı Arayüzü:** Android (Jetpack Compose)
* **Ağ & İletişim:** Server-Sent Events (SSE) ve REST API
* **Donanım Entegrasyonu:** ESP32 ve diğer IoT istemcileri için uzaktan tetikleme
