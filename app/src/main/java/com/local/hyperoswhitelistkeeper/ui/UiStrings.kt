package com.local.hyperoswhitelistkeeper.ui

import com.local.hyperoswhitelistkeeper.data.AppLanguage
import com.local.hyperoswhitelistkeeper.data.ThemeMode

data class UiStrings(
    val protectedApps: String,
    val selectedApps: (Int) -> String,
    val automaticRepair: String,
    val runOnBoot: String,
    val runOnBootDescription: String,
    val scheduledRun: String,
    val scheduledRunDescription: String,
    val applying: String,
    val applyWhitelist: String,
    val chooseApps: String,
    val done: String,
    val searchHint: String,
    val addApp: String,
    val selectAll: String,
    val addAppTitle: String,
    val packageNameLabel: String,
    val appNameLabel: String,
    val invalidPackageName: String,
    val duplicatePackage: String,
    val save: String,
    val cancel: String,
    val selectedHeader: String,
    val otherAppsHeader: String,
    val noAppsFound: String,
    val checking: String,
    val activated: String,
    val notActivated: String,
    val updating: String,
    val updateSuccess: String,
    val alreadyApplied: String,
    val permissionRequired: String,
    val updateFailure: String,
    val exactAlarmPermissionRequired: String,
    val themeDescription: (ThemeMode) -> String,
    val languageButton: String,
    val languageDescription: String,
    val donateDescription: String,
    val guideTitle: String,
    val guideBody: String,
    val guideClose: String,
    val guideDescription: String,
)

fun uiStrings(language: AppLanguage): UiStrings = when (language) {
    AppLanguage.VI -> UiStrings(
        protectedApps = "Ứng dụng được bảo vệ",
        selectedApps = { count -> "$count ứng dụng đã chọn" },
        automaticRepair = "Tự động khôi phục whitelist",
        runOnBoot = "Chạy khi khởi động",
        runOnBootDescription = "Kiểm tra và sửa một lần sau khi bật máy",
        scheduledRun = "Chạy theo lịch",
        scheduledRunDescription = "Mỗi ngày vào đúng 08:00 và 12:00",
        applying = "Đang áp dụng…",
        applyWhitelist = "Áp dụng whitelist",
        chooseApps = "Chọn ứng dụng",
        done = "Xong",
        searchHint = "Tìm kiếm ứng dụng...",
        addApp = "+ Thêm ứng dụng",
        selectAll = "Chọn tất cả",
        addAppTitle = "Thêm ứng dụng mới",
        packageNameLabel = "Tên Package",
        appNameLabel = "Tên App",
        invalidPackageName = "Package không đúng định dạng",
        duplicatePackage = "Package này đã có trong danh sách",
        save = "Lưu",
        cancel = "Hủy",
        selectedHeader = "ĐÃ CHỌN",
        otherAppsHeader = "ỨNG DỤNG KHÁC",
        noAppsFound = "Không tìm thấy ứng dụng",
        checking = "Đang kiểm tra…",
        activated = "Đã kích hoạt",
        notActivated = "Chưa kích hoạt",
        updating = "Đang cập nhật whitelist…",
        updateSuccess = "Đã cập nhật whitelist",
        alreadyApplied = "Whitelist đã được cập nhật trước đó",
        permissionRequired = "Cần quyền sửa cài đặt hệ thống",
        updateFailure = "Không thể cập nhật whitelist",
        exactAlarmPermissionRequired = "Hãy cho phép Báo thức và lời nhắc để chạy đúng giờ",
        themeDescription = { mode ->
            when (mode) {
                ThemeMode.SYSTEM -> "Giao diện theo hệ thống. Nhấn để chuyển sang sáng"
                ThemeMode.LIGHT -> "Giao diện sáng. Nhấn để chuyển sang tối"
                ThemeMode.DARK -> "Giao diện tối. Nhấn để theo hệ thống"
            }
        },
        languageButton = "EN",
        languageDescription = "Switch to English",
        donateDescription = "Mở mã QR Donate",
        guideTitle = "Hướng dẫn nhanh",
        guideBody = "1. Cài Carrier Services từ CH Play.\n" +
            "2. Bật Tự khởi động (Auto Start) cho các app cần thiết.\n" +
            "3. Tick các app cần bảo vệ.\n" +
            "4. Nhấn Áp dụng; trạng thái xanh là xong.",
        guideClose = "Đã hiểu",
        guideDescription = "Mở hướng dẫn sử dụng",
    )

    AppLanguage.EN -> UiStrings(
        protectedApps = "Protected apps",
        selectedApps = { count -> if (count == 1) "1 app selected" else "$count apps selected" },
        automaticRepair = "Automatically restore whitelist",
        runOnBoot = "Run on boot",
        runOnBootDescription = "Check and repair once after the phone starts",
        scheduledRun = "Scheduled run",
        scheduledRunDescription = "Every day at exactly 08:00 and 12:00",
        applying = "Applying…",
        applyWhitelist = "Apply whitelist",
        chooseApps = "Choose apps",
        done = "Done",
        searchHint = "Search apps...",
        addApp = "+ Add app",
        selectAll = "Select all",
        addAppTitle = "Add a new app",
        packageNameLabel = "Package name",
        appNameLabel = "App name",
        invalidPackageName = "Invalid package name",
        duplicatePackage = "This package is already in the list",
        save = "Save",
        cancel = "Cancel",
        selectedHeader = "SELECTED",
        otherAppsHeader = "OTHER APPS",
        noAppsFound = "No apps found",
        checking = "Checking…",
        activated = "Activated",
        notActivated = "Not activated",
        updating = "Updating whitelist…",
        updateSuccess = "Whitelist updated",
        alreadyApplied = "Whitelist was already up to date",
        permissionRequired = "Permission to modify system settings is required",
        updateFailure = "Unable to update whitelist",
        exactAlarmPermissionRequired = "Allow Alarms & reminders to run at exact times",
        themeDescription = { mode ->
            when (mode) {
                ThemeMode.SYSTEM -> "System theme. Tap to switch to light"
                ThemeMode.LIGHT -> "Light theme. Tap to switch to dark"
                ThemeMode.DARK -> "Dark theme. Tap to follow system"
            }
        },
        languageButton = "VN",
        languageDescription = "Chuyển sang tiếng Việt",
        donateDescription = "Open Donate QR code",
        guideTitle = "Quick guide",
        guideBody = "1. Install Carrier Services from Google Play.\n" +
            "2. Enable Auto Start for the required apps.\n" +
            "3. Select the apps to protect.\n" +
            "4. Tap Apply; green means activated.",
        guideClose = "Got it",
        guideDescription = "Open the quick guide",
    )
}
