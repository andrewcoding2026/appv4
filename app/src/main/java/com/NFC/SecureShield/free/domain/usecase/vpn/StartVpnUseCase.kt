package com.NFC.SecureShield.free.domain.usecase.vpn

import android.content.Context
import android.content.Intent
import android.net.VpnService
import com.NFC.SecureShield.free.service.NfcVpnService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class StartVpnUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun prepareIntent(): Intent? = VpnService.prepare(context)

    fun start() {
        val intent = Intent(context, NfcVpnService::class.java).apply {
            action = NfcVpnService.ACTION_START
        }
        context.startService(intent)
    }
}
