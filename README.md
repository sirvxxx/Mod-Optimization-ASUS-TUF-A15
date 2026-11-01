# Custom Performance Tuner (Fabric, client-side)

Mod client-side untuk Minecraft 26.1.2 / 26.2. Fokus: mengurangi beban entity, block entity, dan partikel
tanpa mengganti renderer, jadi berjalan berdampingan dengan Sodium / Iris.

## Cara membuat .jar (tanpa install apa pun)
1. Buat repo GitHub baru, upload SEMUA isi folder ini (termasuk `.github`; jika folder itu tidak ikut, buat
   file `.github/workflows/build.yml` lewat Add file > Create new file lalu paste isinya).
2. Tab **Actions** -> tunggu centang hijau -> **Artifacts** -> unduh `custom-perf-tuner-mc26.2` (atau `mc26.1.2`).
3. Taruh jar di folder `mods` bersama Fabric API.

## Pemakaian
- Tombol **K** atau `/perftune` membuka menu. Preset: **Balanced / Ultra FPS / Quality** (berubah ke *Custom* otomatis
  bila kamu mengubah pengaturan manual).
- `/perftune preset ultra_fps` - ganti preset  |  `/perftune status` - FPS, level governor, hook yang aktif.
- Config: `.minecraft/config/custom_perf_tuner.json` (0 = nonaktif / tanpa batas).
- Bahasa mengikuti bahasa game (English / Indonesia).

## Fitur
| Fitur | Cara kerja |
|---|---|
| Entity culling | Setelah frustum culling vanilla: entity di luar batas jarak tidak digambar (item/XP, armor stand, mob). Player, penumpang, entity glowing, dan entity < 12 blok tidak pernah di-cull. |
| Occlusion culling | 3 raycast (kepala/tengah/kaki) dari kamera; hanya jika semuanya terhalang entity di-skip. Hasil di-cache 5 tick. Default hanya aktif di Ultra FPS. |
| Block entity culling | Sign, banner, chest, dst. di luar jarak tidak digambar. Beacon tidak pernah di-cull. |
| Particle limit | Maksimum partikel BARU per tick; turun otomatis saat FPS rendah. |
| Adaptive governor | Memantau FPS; jika di bawah target 3 detik berturut-turut, jarak culling & budget partikel mengecil bertahap (level 0-3), naik lagi saat FPS pulih. Tidak mengubah render distance. |
| Worker priority | Menurunkan prioritas thread worker Minecraft (chunk meshing / world gen / IO) agar render thread tidak kalah rebutan CPU. |
| Preset opsi vanilla | Menerapkan render distance, simulation distance, entity scaling, partikel, biome blend, mipmap. |

## Keterbatasan (jujur)
- Ini TIDAK mempercepat shader pack. Beban shader ada di GPU (RTX 3050); yang ditekan mod ini hanyalah kerja CPU
  (entity, block entity, partikel). Untuk FPS shader: kecilkan shadow resolution/distance dan render distance.
- Prioritas thread tidak mengurangi total pekerjaan CPU, hanya urutannya.
- Hook mixin memakai nama class internal Minecraft. Jika sebuah nama berubah di versi lain, fitur terkait mati
  dengan aman (tanpa crash). Menu menampilkan status hook: `E✓ BE✓ P✓` = aktif, `✗` = tidak terpanggil.
  Catatan: `✗` juga muncul sebelum ada entity/partikel/block entity yang diproses.
