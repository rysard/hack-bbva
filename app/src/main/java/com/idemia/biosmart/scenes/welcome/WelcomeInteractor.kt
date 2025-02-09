package com.idemia.biosmart.scenes.welcome

import android.util.Log
import com.idemia.morphobiosmart.utils.DisposableManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 *  Welcome Interactor
 *  BioSmart
 *  Created by alfredo on 12/11/18.
 *  Copyright (c) 2018 Alfredo. All rights reserved.
 */
class WelcomeInteractor @Inject constructor(var presenter: WelcomePresentationLogic) : WelcomeBusinessLogic {
    private val worker = WelcomeWorker()

    companion object {
        val TAG = "WelcomeInteractor"
    }

    //region Generate license
    override fun generateLicense(request: WelcomeModels.GenerateLicense.Request) {
        // Call WS to generate license file bin (Service Provider)
        worker.generateLicense(request.serviceProviderUrl)?.let { observable ->
            DisposableManager.add(
                observable.subscribe({ response ->
                    val activationData = response.bytes()
                    val mResponse = WelcomeModels.GenerateLicense.Response(true, activationData)
                    presenter.presentGenerateLicense(mResponse)
                },{ throwable ->
                    val response = WelcomeModels.GenerateLicense.Response(false)
                    Log.e(TAG,"Error generating BIN File License due: ", throwable)
                    presenter.presentGenerateLicense(response)
                })
            )
        } ?: run {
            val throwable = WelcomeWorker.throwable
            val response = WelcomeModels.GenerateLicense.Response(false, null, throwable)
            Log.e(TAG,"Error generating BIN File License due: ", throwable)
            presenter.presentGenerateLicense(response)
        }
    }
    //endregion

    //region Create LKMS License
    override fun createLKMSLicense(request: WelcomeModels.ActivateBinFileLicenseToLkms.Request) {
        val disposable = worker.createLKMSLicense(request)
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ lkmsLicense ->
                val response = WelcomeModels.ActivateBinFileLicenseToLkms.Response(true, lkmsLicense)
                presenter.presentCreateLKMSLicense(response)
            },{ throwable ->
                Log.e(TAG, "License not activated due: ", throwable)
                val response = WelcomeModels.ActivateBinFileLicenseToLkms.Response(false, null, throwable)
                presenter.presentCreateLKMSLicense(response)
            })
        DisposableManager.add(disposable)
    }
    //endregion

    //region Activate LKMS License on device
    override fun activateLkmsLicenseOnDevice(request: WelcomeModels.ActivateLkmsLicenseOnDevice.Request) {
        val response = worker.activateLkmsLicenseOnDevice(request)
        presenter.presentActivateLkmsLicenseOnDevice(response)
    }
    //endregion

    //region Start Process
    override fun startProcess(request: WelcomeModels.StartEnrollment.Request) {
        val response = WelcomeModels.StartEnrollment.Response(request.operation)
        presenter.presentStartProcess(response)
    }
    //endregion
}


/**
 *  Welcome Business Logic
 *  BioSmart
 *  Created by alfredo on 12/11/18.
 *  Copyright (c) 2018 requestAlfredo. All rights reserved.
 */
interface WelcomeBusinessLogic {
    /**
     * Generate License from Service Provider
     * @param requuest A GenerateLicense Request to send
     * */
    fun generateLicense(request: WelcomeModels.GenerateLicense.Request)

    /**
     * Create LKMS License
     * @param  request A [WelcomeModels.ActivateBinFileLicenseToLkms.Request] request
     */
    fun createLKMSLicense(request: WelcomeModels.ActivateBinFileLicenseToLkms.Request)

    /**
     * Activate LKMS Licese on device
     */
    fun activateLkmsLicenseOnDevice(request: WelcomeModels.ActivateLkmsLicenseOnDevice.Request)

    /**
     * Start next process
     */
    fun startProcess(request: WelcomeModels.StartEnrollment.Request)
}
