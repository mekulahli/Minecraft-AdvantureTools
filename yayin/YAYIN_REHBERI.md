# Yayın rehberi

Bu rehber, modu herkese açık yayınlamak için yapılacakları sırasıyla anlatır. Hazır dosyalar:

| Dosya | Ne işe yarar |
|---|---|
| `README.md` / `README.tr.md` | İngilizce / Türkçe kullanım kılavuzu (GitHub ana sayfası) |
| `CHANGELOG.md` | Sürüm geçmişi (iki dilli) |
| `LICENSE` | MIT lisansı |
| `docs/images/banner.png` | 1280x640 kapak (GitHub sosyal önizleme, galeri) |
| `docs/images/icon.png` | 512x512 mod ikonu (Modrinth / CurseForge logosu) |
| `docs/images/items_en.png`, `esyalar_tr.png` | Tüm eşyaların tanıtım görseli |
| `docs/images/recipes/*.png` | 20 tarif resmi (yazısız, iki dilde de kullanılır) |
| `yayin/modrinth.md` | Modrinth formunun bütün alanları |
| `yayin/curseforge.md` | CurseForge formunun bütün alanları |
| `.github/ISSUE_TEMPLATE/` | GitHub'da iki dilli hata bildirimi ve öneri şablonları |
| `build/libs/maceraaletleri-0.3.0.jar` | Yüklenecek mod dosyası |

---

## Adım 1: Son test

Yayından önce her aleti bir kez oyunda deneyin:

```bash
./gradlew runClient
```

- [ ] Kanca: bloğa çekilme, Shift ile sallanma, Boşluk/S ile ipte tırmanma, moba atma
- [ ] 3D manevra takımı: sol/sağ tık, iki kanca birden, Boşluk ile itki, gaz bitince tüp takılması
- [ ] Planör: düşerken açılma, **F5 ile başın üstünde görünmesi**, kamp ateşi üstünde yükselme
- [ ] Çift zıplama botları: havada ikinci zıplama, ayakta görünmesi
- [ ] Sırt çantası: açma, Shift + sağ tıkla sırta takma, **F5 ile sırtta görünmesi**, B tuşu, yükseltme tarifi (içindekiler korunuyor mu), toplama modülü
- [ ] İp merdiven: uçurum kenarında açılma, tırmanma, toplama
- [ ] Zipline, işaret fişeği (gece), kaşif dürbünü, ışınlanma pusulası, mıknatıs (M tuşu), eldiven
- [ ] Uyku tulumu: gece uyuma; ardından ölüp doğma noktasının değişmediğini görme
- [ ] Ölüm pusulası: Survival'da ölüp yeniden doğunca envantere gelmesi
- [ ] Yeni dünyada rehber kitabın gelmesi
- [ ] Survival'da birkaç tarifin tarif kitabında görünmesi

Bir sorun çıkarsa yayından önce düzeltelim.

---

## Adım 2: Ekran görüntüleri ve video

Oyun içi görüntüler indirme sayısını en çok etkileyen şey. Hazırlık için oyunda:

1. Yeni bir dünya: **Creative**, dünya tipi olarak manzaralı bir biyom seçin (dağ, orman).
2. Sohbete şunları yazın:
   ```
   /time set noon
   /weather clear
   ```
3. **F1** arayüzü gizler, **F5** üçüncü şahıs görünümüne geçer, **F2** ekran görüntüsü alır.
   Görüntüler `run\screenshots\` klasörüne kaydedilir.

### Çekilecek görüntüler

| # | Sahne | Nasıl |
|---|---|---|
| 1 | **3D manevra takımıyla havada** (galeride "Featured") | İki ağaca kanca atın, aralarında savrulurken F5 + F2 |
| 2 | Planörle kamp ateşinin üstünde yükselme | Bir tepeye kamp ateşi koyun, üstünden süzülün; F5 ile önden |
| 3 | Sırtında elmas çanta olan oyuncu | F5'e iki kez basıp önden görünüm, sonra arkadan |
| 4 | Kaşif dürbünüyle parlayan cevherler | Bir mağarada kullanın, F1 ile arayüzü gizleyin |
| 5 | Uçurumdan sarkan ip merdiven | Yüksek bir kayalıkta, biraz uzaktan |
| 6 | Gece işaret fişeği ışığı ve dumanı | `/time set midnight`, açık bir alana atın |
| 7 | Vadi üzerinde zipline | İki tepe arasına kurun, kayarken çekin |
| 8 | Çanta penceresi (elmas çanta, 54 slot) | Dolu bir çantayı açın |
| 9 | Başarımlar sekmesi | L tuşu → Macera Aletleri |

### Video (çok önerilir)
- Windows'ta **Win + G** (Xbox Game Bar) ya da **OBS Studio** ile kayıt alın.
- 30–60 saniye yeter: 3D manevra takımı ile savrulma → planörle süzülme → kamp ateşiyle yükselme → zipline.
- YouTube'a yükleyip bağlantıyı Modrinth ve CurseForge açıklamasının en üstüne koyun.

---

## Adım 3: GitHub'a yükleme (kaynak kod ve kılavuz)

Mod sayfalarındaki "Source", "Issues" ve açıklamadaki resimler için gerekli.

1. [github.com](https://github.com) → **New repository** → adı `macera-aletleri`, **Public**, README eklemeden oluşturun.
2. Proje klasöründe (IntelliJ terminali ya da PowerShell):
   ```bash
   git init
   ```
   ```bash
   git add .
   ```
   ```bash
   git commit -m "Macera Aletleri 0.3.0"
   ```
   ```bash
   git branch -M main
   ```
   ```bash
   git remote add origin https://github.com/KULLANICI/macera-aletleri.git
   ```
   ```bash
   git push -u origin main
   ```
   (`KULLANICI` yerine GitHub kullanıcı adınızı yazın.)
3. GitHub'da depo **Settings → General → Social preview** bölümüne `docs/images/banner.png` yükleyin.
4. `yayin/modrinth.md` ve `yayin/curseforge.md` içindeki `KULLANICI` yazan yerleri kullanıcı adınızla değiştirin.
5. Oyun içi Mods ekranında bağlantı görünsün isterseniz `src/main/templates/META-INF/neoforge.mods.toml` dosyasında şu iki satırın başındaki `#` işaretini kaldırıp adresleri yazın, sonra jar dosyasını yeniden derleyin:
   ```toml
   issueTrackerURL="https://github.com/KULLANICI/macera-aletleri/issues"
   displayURL="https://github.com/KULLANICI/macera-aletleri"
   ```

Git kullanmak istemezseniz bu adımı atlayabilirsiniz. O zaman mod sayfası açıklamalarındaki resim satırlarını silin ve görselleri galeriye yükleyin.

---

## Adım 4: Modrinth

`yayin/modrinth.md` dosyasını açıp sırayla ilerleyin:
1. **Create a project** → Name, URL, Summary → Visibility: **Public**.
2. **Icon**, **Description**, **Tags**, **Environment**, **License**, **Links**.
3. **Gallery:** önce `banner.png` ve `items_en.png`, sonra ekran görüntüleri.
4. **Versions → Create version:** jar, 0.3.0, **Beta**, NeoForge, 26.2, Curios → **Optional**.
5. **Submit for review.** Onay genelde birkaç saat ile 1–2 gün sürer.

## Adım 5: CurseForge

`yayin/curseforge.md` dosyasını açıp sırayla ilerleyin. Aynı jar dosyasını, aynı sürüm numarasıyla yükleyin.

---

## Adım 6: Duyuru

Onaylandıktan sonra modu duyurmak için:
- **Reddit:** r/feedthebeast ve r/Minecraft (mod videosu ya da GIF ile paylaşın; kurallarını okuyun).
- **Discord:** NeoForge ve Modrinth sunucularında mod tanıtım kanalları.
- **Türk toplulukları:** Türkçe Minecraft Discord sunucuları ve forumları. Tam Türkçe çeviri güçlü bir tanıtım noktası.

---

## Adım 7: Yayından sonra

- **Hata bildirimleri:** GitHub Issues ve Modrinth/CurseForge yorumlarını takip edin. Bir hata gelirse `logs/latest.log` dosyasını isteyin; şablon bunu zaten soruyor.
- **Yeni sürüm çıkarmak:**
  1. `gradle.properties` → `mod_version` değerini artırın (hata düzeltmesi: `0.3.1`, yeni özellik: `0.4.0`).
  2. `./gradlew build`
  3. `CHANGELOG.md` dosyasına yeni bölüm ekleyin.
  4. Modrinth ve CurseForge'a yeni jar dosyasını yükleyin.
- **Beta'dan çıkış:** Birkaç hafta ciddi hata bildirimi gelmezse sonraki sürümü **Release** olarak işaretleyin (örneğin `1.0.0`).
- **Yeni Minecraft sürümü çıktığında:** Oyuncular güncelleme bekler; NeoForge sürümünü ve MDK'yı güncellemek gerekir.
