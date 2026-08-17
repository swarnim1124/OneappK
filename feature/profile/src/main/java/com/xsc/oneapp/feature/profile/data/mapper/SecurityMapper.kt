package com.xsc.oneapp.feature.profile.data.mapper

import com.xsc.oneapp.feature.profile.data.remote.dto.BackupCodesResultDTO
import com.xsc.oneapp.feature.profile.data.remote.dto.MfaEnrollmentDTO
import com.xsc.oneapp.feature.profile.data.remote.dto.MfaMethodDTO
import com.xsc.oneapp.feature.profile.domain.model.BackupCodesResult
import com.xsc.oneapp.feature.profile.domain.model.MfaEnrollment
import com.xsc.oneapp.feature.profile.domain.model.MfaMethod

object SecurityMapper {
    fun toDomain(dto: MfaMethodDTO): MfaMethod = MfaMethod(
        id = dto.id ?: "",
        mfaTypeId = dto.mfaTypeId ?: "",
        isPrimary = dto.isPrimary == "true",
        isActive = dto.isActive == "true"
    )

    fun toDomain(dto: MfaEnrollmentDTO): MfaEnrollment = MfaEnrollment(
        mfaType = dto.mfaType ?: "",
        provisioningUri = dto.provisioningUri ?: "",
        backupCodes = dto.backupCodes ?: emptyList(),
        enrollmentChallengeId = dto.enrollmentChallengeId ?: ""
    )

    fun toDomain(dto: BackupCodesResultDTO): BackupCodesResult = BackupCodesResult(
        backupCodes = dto.backupCodes ?: emptyList()
    )
}
