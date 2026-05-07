package com.nfcsecurity.domain.usecase.vpn

import android.content.Context
import android.content.Intent
import com.nfcsecurity.service.NfcVpnService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class StopVpnUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    operator fun invoke() {
        val intent = Intent(context, NfcVpnService::class.java).apply {
            action = NfcVpnService.ACTION_STOP
        }
        context.startService(intent)
    }
}
