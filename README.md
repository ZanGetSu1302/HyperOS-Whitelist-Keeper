# HyperOS Whitelist Keeper

Ứng dụng không cần root giúp giữ các ứng dụng đã chọn trong whitelist hệ thống
của Xiaomi HyperOS. Hỗ trợ giao diện tiếng Việt/Anh, Light/Dark và tự kiểm tra
trạng thái kích hoạt khi mở ứng dụng.

## Hướng dẫn sử dụng

1. Cài **Carrier Services** từ CH Play.
2. Bật **Tự khởi động (Auto Start)** cho các ứng dụng cần thiết.
3. Mở Whitelist Keeper và tick các ứng dụng cần bảo vệ.
4. Nhấn **Áp dụng whitelist**. Trạng thái xanh **Đã kích hoạt** là hoàn tất.

Nút `EN/VN`, trợ giúp `?` và đổi giao diện sáng/tối nằm trên thanh trên cùng.
Trạng thái đỏ **Chưa kích hoạt** nghĩa là ứng dụng còn thiếu trong ít nhất một
whitelist; hãy nhấn **Áp dụng whitelist** lần nữa.

### Quick guide (English)

1. Install **Carrier Services** from Google Play.
2. Enable **Auto Start** for the required apps.
3. Select the apps you want to protect.
4. Tap **Apply whitelist**. Green **Activated** means setup is complete.

## Cài đặt APK

Android 14 trở lên yêu cầu cài bản tương thích SetEdit bằng ADB:

```text
adb install --bypass-low-target-sdk-block app-release-qa-signed.apk
```

Nếu thiết bị đang có bản `1.0.0` target SDK 36, cần gỡ bản cũ trước vì Android
không cho hạ target SDK khi cập nhật trực tiếp:

```text
adb uninstall com.local.hyperoswhitelistkeeper
adb install --bypass-low-target-sdk-block app-release-qa-signed.apk
```

Việc gỡ ứng dụng chỉ xóa lựa chọn/theme/ngôn ngữ nội bộ, không xóa whitelist đã
ghi trong `Settings.System`.

## Buy me a coffee ☕

Nếu ứng dụng hữu ích, bạn có thể ủng hộ tác giả qua VietQR. Cảm ơn bạn!

<p align="center">
  <img src="docs/assets/buy-me-a-coffee-vietqr.png" alt="Buy me a coffee via VietQR" width="360">
</p>

## Cấu hình kỹ thuật

- Application ID: `com.local.hyperoswhitelistkeeper`
- Minimum SDK: 26
- Compile SDK: 36 (Android 16)
- Target SDK: 22 (chế độ tương thích SetEdit)
- Version: `1.4.0-legacy` (`versionCode 5`)
- Jetpack Compose Material 3, DataStore và WorkManager

`targetSdk 22` là chủ ý kỹ thuật: Android chặn ứng dụng target API 23+ sửa một
số khóa System không công khai dù đã cấp `WRITE_SETTINGS`. Ứng dụng dùng cơ chế
tương thích của SetEdit, ghi qua `ContentResolver.insert` rồi đọc lại để xác minh.
Do target thấp, APK không phù hợp để phát hành qua Google Play.

Ứng dụng chỉ thêm mục còn thiếu, không thay thế dữ liệu hiện có và không tạo
trùng lặp. Package được kiểm tra trong ba whitelist package;
`com.google.android.gms.persistent` chỉ được xử lý trong `power_proc_white_list`.

## Build và kiểm tra

```text
./gradlew clean test lintDebug assembleDebug
./gradlew test lintRelease assembleRelease
```

APK Debug nằm tại `app/build/outputs/apk/debug/app-debug.apk`. APK Release do
Gradle tạo ra là unsigned; cần ký bằng keystore của chủ ứng dụng trước khi phát
hành. `artifacts/app-release-qa-signed.apk` chỉ dùng certificate debug để cài thử.

