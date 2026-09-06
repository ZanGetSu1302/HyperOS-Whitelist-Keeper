# HyperOS Whitelist Keeper — Báo cáo kiểm thử

Thời điểm kiểm thử: 2026-09-06 (Asia/Saigon).

## Kết quả build cuối

| Hạng mục | Kết quả | Bằng chứng |
|---|---|---|
| Gradle build Debug + Release | PASS | `clean test lintDebug assembleDebug lintRelease assembleRelease`: `BUILD SUCCESSFUL` |
| Unit tests | PASS | 15 test, 0 failure, 0 error, 0 skipped |
| Android Lint | PASS | 0 error; 11 cảnh báo dependency/khuyến nghị không chặn build |
| APK metadata | PASS | package `com.local.hyperoswhitelistkeeper`; minSdk 26; compileSdk 36; targetSdk 22; version `1.4.0-legacy` (5) |
| APK release signature | PASS | APK Signature Scheme v2 = true; v3 = true |
| Android 16 release install/launch | PASS | Cài bằng `--bypass-low-target-sdk-block`; `Status: ok` |
| Sống sau 5 giây | PASS | PID release vẫn tồn tại sau 5 giây |
| Crash/ANR | CLEAN | Không có FATAL EXCEPTION, ANR, VerifyError hay lỗi khởi tạo Activity |

## Cơ chế ghi tương thích SetEdit

- Bản targetSdk 36 trước đây bị SettingsProvider của HyperOS 3 từ chối khóa System
  không công khai bằng `IllegalArgumentException`, dù `WRITE_SETTINGS` đang allow.
- Bản mới dùng targetSdk 22 và ghi `name`/`value` qua
  `ContentResolver.insert(Settings.System.CONTENT_URI, values)`, tương tự SetEdit.
- Sau mỗi lần ghi, repository đọc lại và chỉ báo thành công khi đủ toàn bộ mục yêu cầu.
- Ứng dụng vẫn compile bằng Android SDK 36 và build bằng pipeline chuẩn AGP/aapt2/D8.

## Kiểm thử thiết bị Xiaomi HyperOS 3

Thiết bị: Xiaomi `25102RKBEC` (`myron`), Android 16, HyperOS OS3.0.

| Chức năng | Kết quả | Chi tiết |
|---|---|---|
| Ghi Settings.System không root | PASS | Ba khóa package được cập nhật thật; log `Write result: true` và `Verify: success` |
| Merge only | PASS | Các mục Xiaomi/ứng dụng đang có được giữ nguyên, mục thiếu được nối cuối |
| Chống duplicate | PASS | Sau ghi: số phần tử bằng số phần tử unique ở cả ba khóa |
| Route process | PASS | `com.google.android.gms.persistent` chỉ nằm trong `power_proc_white_list` |
| Apply lặp lại | PASS | Không ghi lại; UI báo “Whitelist đã được cập nhật trước đó” |
| Khôi phục thủ công mục bị xóa | PASS | Xóa thử GMS khỏi MILLET, nhấn Apply thêm lại và verify thành công |
| WorkManager định kỳ | PASS (lịch) | Job 15 phút có `batteryNotLow=true`; Android quyết định thời điểm chạy thực tế |

## Trạng thái kích hoạt trong app picker

- Khi Activity vào foreground, app đọc một lần cả bốn khóa System và tính trạng thái
  cho toàn bộ catalog.
- Package chỉ xanh “Đã kích hoạt” khi tồn tại trong đủ ba khóa package.
- Process chỉ kiểm tra `power_proc_white_list`.
- Mục thiếu ít nhất một khóa hiện đỏ “Chưa kích hoạt”.
- Sau Apply, trạng thái được đọc lại tự động.
- Kiểm thử Android 16 cho thấy trạng thái đỏ trước khi đủ khóa và xanh sau khi đủ;
  danh sách, checkbox, divider, tìm kiếm và cuộn không bị tràn.

## Giao diện VN/EN

- Nút chuyển ngôn ngữ nằm cạnh nút theme và không chồng lấn tiêu đề ở 360dp.
- Màn hình chính, app picker, trạng thái kích hoạt, tìm kiếm, tên nhóm, nút và
  Snackbar đều chuyển sang tiếng Anh/Vietnamese ngay lập tức.
- Tên ứng dụng lấy từ PackageManager/fallback được giữ nguyên.
- Ngôn ngữ được lưu bằng DataStore; EN vẫn còn sau force-stop và mở lại.
- Kiểm thử trên APK Release Android 16: `MAIN_EN=true`, `PICKER_EN=true`,
  `EN_PERSISTED=true`, `EN_SNACKBAR=true`; PID vẫn tồn tại sau 5 giây.

## Hướng dẫn nhanh

- Nút dấu hỏi nằm cạnh nút ngôn ngữ và theme; mỗi nút giữ vùng chạm khoảng 48dp.
- Top App Bar chuyển sang căn trái để ba nút không chồng tiêu đề trên 360dp.
- AlertDialog VN/EN hiển thị đúng 4 bước ngắn; hai bước đầu là cài Carrier
  Services và bật Auto Start cho các ứng dụng cần thiết.
- Kiểm thử APK Release Android 16: `GUIDE_VI=true`, `GUIDE_EN=true`; dialog không
  tràn, nút đóng hiển thị đầy đủ và nền vẫn đúng Light/Dark.

## Lưu ý cài đặt

Android 14+ chặn cài ứng dụng target cũ theo mặc định. Dùng:

```text
adb install --bypass-low-target-sdk-block app-release-qa-signed.apk
```

Không thể update trực tiếp từ bản targetSdk 36 xuống targetSdk 22. Nếu đã cài bản
`1.0.0`, cần gỡ trước rồi cài bản mới. Việc gỡ xóa lựa chọn/theme nội bộ nhưng
không xóa các giá trị whitelist trong `Settings.System`.

## APK

`app-release-qa-signed.apk` là bản release cài được để kiểm thử, ký bằng Android
debug certificate vì workspace không có keystore production. `app-release-unsigned.apk`
cần được ký lại bằng keystore do chủ ứng dụng quản lý trước khi phân phối chính thức.

- `app-debug.apk`: 12.079.356 byte; SHA-256
  `0CA01EE0F82A9EF226235F3244795D41EED15AA251C6A67EFBF4BAECABC96666`.
- `app-release-qa-signed.apk`: 8.236.247 byte; SHA-256
  `C11ED0651EABD086DA2A354FB68E2838B2B2DFF6E9216419C7AF88DB86EE4102`.
- `app-release-unsigned.apk`: 8.217.544 byte; SHA-256
  `13B4B10CB3118F318DB493713FDB00DCCE4AEBA520326AF54091166322342CA7`.
