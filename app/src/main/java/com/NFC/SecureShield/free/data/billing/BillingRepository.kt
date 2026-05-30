package com.NFC.SecureShield.free.data.billing

import android.app.Activity
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Mock: Sempre Pro per la versione Free
    private val _isPro = MutableStateFlow(true)
    val isPro: StateFlow<Boolean> = _isPro.asStateFlow()

    // Mock: Lista prodotti vuota
    private val _productDetails = MutableStateFlow<List<Any>>(emptyList())
    val productDetails: StateFlow<List<Any>> = _productDetails.asStateFlow()

    fun launchBillingFlow(activity: Activity, productDetails: Any, offerToken: String) {
        // Mock: Nessuna azione
    }
}
