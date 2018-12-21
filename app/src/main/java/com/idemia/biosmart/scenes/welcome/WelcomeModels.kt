package com.idemia.biosmart.scenes.welcome;

import android.content.Context
import com.morpho.lkms.android.sdk.lkms_core.license.ILkmsLicense

/**
 *  Welcome Models
 *  BioSmart
 *  Created by alfredo on 12/11/18.
 *  Copyright (c) 2018 Alfredo. All rights reserved.
 */
class WelcomeModels {
    // Generate License use case
    class GenerateLicense {
        class Request
        data class Response(var generated: Boolean, var activationData: ByteArray? = null)
        data class ViewModel(var generated: Boolean = false, var activationData: ByteArray? = null)
    }

    // Activate Bin File License to LKMS
    class ActivateBinFileLicenseToLkms {
        /**
         * @param activationData The activation data retrieved from server
         * @param applicationContext [Context] Application Context
         * @param lkmsUrl The LKMS server URL
         * */
        data class Request(val activationData: ByteArray,
                           val applicationContext: Context,
                           val lkmsUrl: String = "https://service-intg.dictao.com/lkms-server-app")
        data class Response(var activated: Boolean, val lkmsLicense: ILkmsLicense? = null)
        data class ViewModel(var activated: Boolean,  val lkmsLicense: ILkmsLicense? = null)
    }

    // Activate Lkms License On Device
    class ActivateLkmsLicenseOnDevice {
        class Request(val applicationContext: Context)
        data class Response(var isLicenseValid: Boolean, val lkmsLicense: ILkmsLicense? = null)
        data class ViewModel(var isLicenseValid: Boolean, val lkmsLicense: ILkmsLicense? = null)
    }

    // Start enrolment usecase
    class StartEnrollment {
        data class Request(val operation: Operation)
        class Response(val operation: Operation)
        class ViewModel(val operation: Operation)
    }

    enum class Operation{
        ENROLMENT,
        AUTHENTICATION,
        IDENTIFY,
        SETTINGS
    }
}