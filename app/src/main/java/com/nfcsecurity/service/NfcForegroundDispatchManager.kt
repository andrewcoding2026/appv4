package com.nfcsecurity.service

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import androidx.core.content.IntentCompat
import com.nfcsecurity.domain.model.NfcTagInfo
import com.nfcsecurity.domain.usecase.nfc.ParseNfcTagUseCase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NfcForegroundDispatchManager @Inject constructor(
    private val nfcAdapter: NfcAdapter?,
    private val parseNfcTag: ParseNfcTagUseCase
) {

    fun enableForegroundDispatch(activity: Activity) {
        nfcAdapter ?: return
        val pendingIntent = PendingIntent.getActivity(
            activity, 0,
            Intent(activity, activity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        nfcAdapter.enableForegroundDispatch(activity, pendingIntent, null, null)
    }

    fun disableForegroundDispatch(activity: Activity) {
        nfcAdapter?.disableForegroundDispatch(activity)
    }

    fun handleIntent(intent: Intent): NfcTagInfo? {
        val action = intent.action ?: return null
        if (action !in listOf(
                NfcAdapter.ACTION_TAG_DISCOVERED,
                NfcAdapter.ACTION_NDEF_DISCOVERED,
                NfcAdapter.ACTION_TECH_DISCOVERED
            )
        ) return null
        val tag = IntentCompat.getParcelableExtra(intent, NfcAdapter.EXTRA_TAG, Tag::class.java)
            ?: return null
        return parseNfcTag(tag)
    }
}
