package com.local.hyperoswhitelistkeeper.data

import android.content.Context
import android.content.pm.PackageManager
import com.local.hyperoswhitelistkeeper.model.AppEntry
import com.local.hyperoswhitelistkeeper.model.EntryType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AppCatalog {
    const val PERSISTENT_PROCESS = "com.google.android.gms.persistent"

    val defaultSelection: Set<String> = linkedSetOf(
        "app.revanced.android.gms",
        "com.google.android.gm",
        "com.mi.health",
        "com.facebook.orca",
        "com.zing.zalo",
        "com.mbmobile",
        "com.google.android.gms",
    )

    // Keep this list close to the product catalog. distinct() intentionally makes
    // duplicate source entries harmless while preserving the requested order.
    val ids: List<String> = listOf(
        "app.revanced.android.gms",
        "com.google.android.gms",
        "com.google.android.gsf",
        "com.google.android.gsf.login",
        "com.google.android.syncadapters.contacts",
        "com.google.android.syncadapters.calendar",
        "com.xiaomi.xmsf",
        "com.xiaomi.xmsfkeeper",
        "com.agribank.emobile",
        "com.google.android.projection.gearhead",
        "com.vnpay.bidv",
        "com.android.chrome",
        "com.chrome.beta",
        "com.discord",
        "com.facebook.katana",
        "com.fptplay",
        "com.google.android.gm",
        "com.google.android.gm.lite",
        "com.grabtaxi.passenger",
        "com.grabtaxi.passenger.lite",
        "com.lazada.android",
        "jp.naver.line.android",
        "com.mbbank.mbbank",
        "com.mbmobile",
        "com.facebook.orca",
        "com.mservice.momotransfer",
        "com.microsoft.office.outlook",
        "com.microsoft.outlooklite",
        "com.microsoft.office.outlook.beta",
        "com.microsoft.teams",
        "com.microsoft.teams.lite",
        "com.microsoft.teams.beta",
        "com.shb.SHBMBanking",
        "com.shopee.vn",
        "com.shopee.lite",
        "com.skype.raider",
        "vn.com.techcombank.bb.app",
        "org.telegram.messenger",
        "com.zhiliaoapp.musically",
        "com.tpb.mb.gprsandroid",
        "com.vcb",
        "com.vietinbank.ipay",
        "com.bplus.vtpay",
        "com.vib.myvib",
        "com.viber.voip",
        "com.zing.zalo.pc",
        "org.thunderdog.challegram",
        "com.vnpay.app",
        "vn.com.vpbank.mobile",
        "com.vtvgo",
        "com.tencent.mm",
        "com.whatsapp",
        "com.whatsapp.w4b",
        "com.google.android.youtube",
        "com.google.android.apps.youtube.mango",
        "com.zing.zalo",
        "com.zing.zalo.lite",
        "com.instagram.android",
        "com.twitter.android",
        "com.linkedin.android",
        "com.google.android.apps.photos",
        "com.google.android.ext.services",
        "com.huawei.health",
        PERSISTENT_PROCESS,
        "com.google.android.gm",
        "com.mi.health",
    ).distinct()

    private val fallbackLabels = mapOf(
        "app.revanced.android.gms" to "microG Services",
        "com.google.android.gms" to "Google Play services",
        "com.google.android.gsf" to "Google Services Framework",
        "com.google.android.gsf.login" to "Google Account Manager",
        "com.google.android.syncadapters.contacts" to "Google Contacts Sync",
        "com.google.android.syncadapters.calendar" to "Google Calendar Sync",
        "com.xiaomi.xmsf" to "Xiaomi Service Framework",
        "com.xiaomi.xmsfkeeper" to "Xiaomi Service Framework Keeper",
        "com.agribank.emobile" to "Agribank Plus",
        "com.google.android.projection.gearhead" to "Android Auto",
        "com.vnpay.bidv" to "BIDV SmartBanking",
        "com.android.chrome" to "Chrome",
        "com.chrome.beta" to "Chrome Beta",
        "com.discord" to "Discord",
        "com.facebook.katana" to "Facebook",
        "com.fptplay" to "FPT Play",
        "com.google.android.gm" to "Gmail",
        "com.google.android.gm.lite" to "Gmail Lite",
        "com.grabtaxi.passenger" to "Grab",
        "com.grabtaxi.passenger.lite" to "Grab Lite",
        "com.lazada.android" to "Lazada",
        "jp.naver.line.android" to "LINE",
        "com.mbbank.mbbank" to "MBBank",
        "com.mbmobile" to "MB Bank",
        "com.facebook.orca" to "Messenger",
        "com.mservice.momotransfer" to "MoMo",
        "com.microsoft.office.outlook" to "Microsoft Outlook",
        "com.microsoft.outlooklite" to "Outlook Lite",
        "com.microsoft.office.outlook.beta" to "Outlook Beta",
        "com.microsoft.teams" to "Microsoft Teams",
        "com.microsoft.teams.lite" to "Teams Lite",
        "com.microsoft.teams.beta" to "Teams Beta",
        "com.shb.SHBMBanking" to "SHB Mobile",
        "com.shopee.vn" to "Shopee",
        "com.shopee.lite" to "Shopee Lite",
        "com.skype.raider" to "Skype",
        "vn.com.techcombank.bb.app" to "Techcombank Mobile",
        "org.telegram.messenger" to "Telegram",
        "com.zhiliaoapp.musically" to "TikTok",
        "com.tpb.mb.gprsandroid" to "TPBank Mobile",
        "com.vcb" to "Vietcombank Digibank",
        "com.vietinbank.ipay" to "VietinBank iPay",
        "com.bplus.vtpay" to "Viettel Money",
        "com.vib.myvib" to "MyVIB",
        "com.viber.voip" to "Viber",
        "com.zing.zalo.pc" to "Zalo PC",
        "org.thunderdog.challegram" to "Telegram X",
        "com.vnpay.app" to "VNPAY",
        "vn.com.vpbank.mobile" to "VPBank NEO",
        "com.vtvgo" to "VTV Go",
        "com.tencent.mm" to "WeChat",
        "com.whatsapp" to "WhatsApp",
        "com.whatsapp.w4b" to "WhatsApp Business",
        "com.google.android.youtube" to "YouTube",
        "com.google.android.apps.youtube.mango" to "YouTube Create",
        "com.zing.zalo" to "Zalo",
        "com.zing.zalo.lite" to "Zalo Lite",
        "com.instagram.android" to "Instagram",
        "com.twitter.android" to "X",
        "com.linkedin.android" to "LinkedIn",
        "com.google.android.apps.photos" to "Google Photos",
        "com.google.android.ext.services" to "Android Services Library",
        "com.huawei.health" to "Huawei Health",
        PERSISTENT_PROCESS to "Google Play services — Persistent process",
        "com.mi.health" to "Mi Fitness",
    )

    fun typeOf(id: String): EntryType =
        if (id == PERSISTENT_PROCESS) EntryType.PROCESS else EntryType.PACKAGE

    suspend fun resolveEntries(context: Context): List<AppEntry> = withContext(Dispatchers.IO) {
        val packageManager = context.packageManager
        ids.map { id ->
            val label = if (typeOf(id) == EntryType.PACKAGE) {
                installedLabel(packageManager, id)
            } else {
                null
            } ?: fallbackLabels[id] ?: id

            AppEntry(id = id, label = label, type = typeOf(id))
        }
    }

    private fun installedLabel(packageManager: PackageManager, packageName: String): String? =
        try {
            packageManager.getApplicationLabel(
                packageManager.getApplicationInfo(packageName, 0),
            ).toString().takeIf(String::isNotBlank)
        } catch (_: PackageManager.NameNotFoundException) {
            null
        } catch (_: SecurityException) {
            null
        }
}
