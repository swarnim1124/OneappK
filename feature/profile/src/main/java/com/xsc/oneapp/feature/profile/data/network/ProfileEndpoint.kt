package com.xsc.oneapp.feature.profile.data.network

object ProfileEndpoint {
    const val MODULE = "m_profile"
    
    object SubModules {
        const val PERSONAL_DETAIL = "sm_personal_detail"
        const val FAMILY_DETAIL = "sm_family_detail"
        const val MEDICAL_DETAIL = "sm_medical_detail"
        const val SETTINGS = "sm_settings"
        const val MFA = "sm_mfa"
    }
    
    object Actions {
        const val PERSONAL_DETAIL = "personalDetail"
        const val ACADEMIC_DETAIL = "academicDetail"
        const val FAMILY_DETAIL = "familyDetail"
        const val EMERGENCY_CONTACT = "emergencyContact"
        const val MEDICAL_DETAIL = "medicalDetail"
        const val USER_PREFERENCE = "userPreference"
        const val MFA = "mfa"
    }
    
    object ActionTypes {
        const val ADD = "add"
        const val VIEW = "view"
        const val UPDATE = "update"
        const val DELETE = "delete"
    }
}
