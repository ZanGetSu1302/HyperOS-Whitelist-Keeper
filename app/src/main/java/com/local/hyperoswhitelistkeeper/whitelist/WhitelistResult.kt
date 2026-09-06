package com.local.hyperoswhitelistkeeper.whitelist

sealed interface WhitelistResult {
    data class Success(
        val addedCount: Int,
        val unchangedCount: Int,
    ) : WhitelistResult

    data object AlreadyApplied : WhitelistResult

    data object PermissionMissing : WhitelistResult

    data class Failure(
        val exception: Throwable?,
    ) : WhitelistResult
}
