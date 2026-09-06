package com.local.hyperoswhitelistkeeper.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppCatalogTest {
    @Test
    fun `valid Android package names are accepted`() {
        assertTrue(AppCatalog.isValidPackageName("com.example.app"))
        assertTrue(AppCatalog.isValidPackageName("vn.com.example_app.client2"))
    }

    @Test
    fun `invalid Android package names are rejected`() {
        assertFalse(AppCatalog.isValidPackageName(""))
        assertFalse(AppCatalog.isValidPackageName("example"))
        assertFalse(AppCatalog.isValidPackageName("com.example app"))
        assertFalse(AppCatalog.isValidPackageName("1com.example.app"))
    }
}
