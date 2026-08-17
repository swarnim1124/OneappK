package com.xsc.oneapp.feature.profile.domain.repository

import com.xsc.oneapp.feature.profile.domain.model.BackupCodesResult
import com.xsc.oneapp.feature.profile.domain.model.MfaEnrollment
import com.xsc.oneapp.feature.profile.domain.model.MfaMethod

interface SecurityRepository {
    suspend fun getMfaMethods(): List<MfaMethod>
    suspend fun initiateMfaEnrollment(): MfaEnrollment
    suspend fun finalizeMfaEnrollment(challengeId: String, otp: String): Boolean
    suspend fun regenerateBackupCodes(methodId: String): BackupCodesResult
    suspend fun disableMfa(methodId: String): Boolean
}
