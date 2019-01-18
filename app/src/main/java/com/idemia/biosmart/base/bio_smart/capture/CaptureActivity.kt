package com.idemia.biosmart.base.bio_smart.capture

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.idemia.biosmart.base.android.BaseActivity
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.morpho.mph_bio_sdk.android.sdk.msc.data.ICaptureOptions
import morpho.urt.msc.mscengine.MorphoSurfaceView
import java.lang.Exception

/**
 *  Capture Activity
 *  BioSmart
 *  Created by alfredo on 1/2/19.
 *  Copyright (c) 2019 Alfredo. All rights reserved.
 */
abstract class CaptureActivity : BaseActivity(), CaptureDisplayLogic {
    private lateinit var interactor: CaptureBusinessLogic    // Interactor
    private lateinit var router: CaptureRoutingLogic         // Router

    protected var timeBeforeStartCapture = 5

    companion object {
        private const val TAG = "CaptureActivity"
    }

    //region A "Dependency Injection"
    override fun inject() {
        val activity = this
        this.interactor = CaptureInteractor()
        this.router = CaptureRouter()
        val presenter = CapturePresenter()
        (this.interactor as CaptureInteractor).setPresenter(presenter)
        presenter.setActivity(activity)
        (router as CaptureRouter).setActivity(this)
    }
    //endregion

    //region Morpho Finger Capture Variables
    private lateinit var surfaceView: MorphoSurfaceView                           // Morpho surface view is "the surface" where preview displays
    private lateinit var captureOptions: ICaptureOptions                          // Used to set capture options like capture mode, timeout, etc...
    private var appCaptureOptions: CaptureModels.AppCaptureOptions? = null        // To store local capture options
    //endregion

    //region Mandatory methods to implement
    /**
     * Use this method to select your Surface Ui
     * @return The surface ui Resource (ex: R.id.morphoSurfaceView)
     */
    protected abstract fun surfaceViewLayout(): Int

    /**
     * This method is called when capture handler and match handler was initialized successfully.
     * Now you can start your capture.
     */
    protected abstract fun readyForCapture()

    /**
     * Select capture handler type to cast
     */
    protected abstract val handlerType: CaptureModels.CaptureHanlderType
    //endregion

    //region On Load Activity (called within "onCreate() method")
    override fun onLoadActivity(savedInstanceState: Bundle?) {
        try{
            val surfaceViewResource = surfaceViewLayout()
            surfaceView = findViewById(surfaceViewResource)
        }catch (e: Exception){
            Log.e(TAG, "Error: ", e)
        }
    }
    //endregion

    //region Listener for permission
    private val listener = object : PermissionListener {
        override fun onPermissionGranted(response: PermissionGrantedResponse) {
            // 1.- Request for capturing Options
            requestCaptureOptions()

            // 2.- Create Capture Handler
            createCaptureHandler()
        }

        override fun onPermissionDenied(response: PermissionDeniedResponse) {
            Toast.makeText(applicationContext,
                "A Required permission was denied by user: ${response.permissionName}",
                Toast.LENGTH_LONG).show()
        }

        override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest, token: PermissionToken) {
            Toast.makeText(applicationContext,
                "Permission ${permission.name} was denied. To enable please go to Applications and allow camera permissions.",
                Toast.LENGTH_LONG).show()
        }
    }
    //endregion

    //region Android Lifecycle
    override fun onResume() {
        super.onResume()
        Dexter.withActivity(this@CaptureActivity)
            .withPermission(Manifest.permission.CAMERA).
                withListener(listener).withErrorListener { error ->
                Log.e(TAG, "Error with camera permission: ${error.name}")
            }.check()
    }

    override fun onPause() {
        super.onPause()
        destroyHandlers()
    }

    override fun onDestroy() {
        super.onDestroy()
        destroyHandlers()
        // Destroy surface view
        surfaceView.onDestroy()
    }
    //endregion

    //region Read Preferences
    private fun readPreferences(){
        val request = CaptureModels.ReadPreferences.Request(this@CaptureActivity, handlerType)
        interactor.readPreferences(request)
    }

    override fun displayReadPreferences(viewModel: CaptureModels.ReadPreferences.ViewModel) {
        appCaptureOptions = viewModel.appCaptureOptions
        timeBeforeStartCapture = viewModel.timeBeforeStartCapture
    }
    //endregion

    //region Request for capturing options
    open fun requestCaptureOptions(){
        // Read capturing options from settings
        readPreferences()
        appCaptureOptions?.let {
            val request = CaptureModels.RequestCaptureOptions.Request(it, handlerType)
            interactor.requestCaptureOptions(request)
        }
    }

    override fun displayCaptureOptions(viewModel: CaptureModels.RequestCaptureOptions.ViewModel) {
        captureOptions = viewModel.options
    }
    //endregion

    //region Create Capture Handler
    private fun createCaptureHandler(){
        val request = CaptureModels.CreateCaptureHandler.Request(handlerType, this@CaptureActivity, captureOptions)
        interactor.createCaptureHandler(request)
    }

    override fun displayCreateCaptureHandler(viewModel: CaptureModels.CreateCaptureHandler.ViewModel) {
        // 3.- Create Matcher Handler
        createMatcherHandler()
    }
    //endregion

    //region Create Matcher Handler
    private fun createMatcherHandler(){
        val request = CaptureModels.CreateMatcherHandler.Request(this@CaptureActivity)
        interactor.createMatcherHandler(request)
    }

    override fun displayCreateMatcherHandler(viewModel: CaptureModels.CreateMatcherHandler.ViewModel) {
        // 4.- Ready for capture
        readyForCapture()
    }
    //endregion

    //region Start Capture
    /**
     * Use this method to start a new capture
     */
    protected fun startCapture(){
        val request = CaptureModels.StartCapture.Request()
        interactor.startCapture(request)
    }
    //endregion}

    //region Stop Capture
    /**
     * Use this method to stop a capture
     */
    protected fun stopCapture(){
        val request = CaptureModels.StopCapture.Request()
        interactor.stopCapture(request)
    }
    //endregion

    //region Destroy Handlers
    private fun destroyHandlers(){
        val request = CaptureModels.DestroyHandlers.Request()
        interactor.destroyHandlers(request)
    }

    override fun displayDestroyHandlers(viewModel: CaptureModels.DestroyHandlers.ViewModel) {
        Log.i(TAG, "Handlers destroyed!")
    }
    //endregion

    //region LISTENER - BioCaptureFeedbackListener
    abstract override fun displayCaptureInfo(viewModel: CaptureModels.CaptureInfo.ViewModel)
    //endregion

    //region LISTENER - BioCaptureResultListener
    abstract override fun displayCaptureFinish(viewModel: CaptureModels.CaptureFinish.ViewModel)
    abstract override fun displayCaptureSuccess(viewModel: CaptureModels.CaptureSuccess.ViewModel)
    abstract override fun displayCaptureFailure(viewModel: CaptureModels.CaptureFailure.ViewModel)
    //endregion

    //region Display Error
    abstract override fun displayError(viewModel: CaptureModels.Error.ViewModel)
    //endregion
}

/**
 *  Capture Display Logic
 *  BioSmart
 *  Created by alfredo on 1/2/19.
 *  Copyright (c) 2019 Alfredo. All rights reserved.
 */
interface CaptureDisplayLogic {
    fun displayReadPreferences(viewModel: CaptureModels.ReadPreferences.ViewModel)

    fun displayCaptureOptions(viewModel: CaptureModels.RequestCaptureOptions.ViewModel)

    fun displayCreateCaptureHandler(viewModel: CaptureModels.CreateCaptureHandler.ViewModel)

    fun displayCreateMatcherHandler(viewModel: CaptureModels.CreateMatcherHandler.ViewModel)

    fun displayDestroyHandlers(viewModel: CaptureModels.DestroyHandlers.ViewModel)

    fun displayCaptureInfo(viewModel: CaptureModels.CaptureInfo.ViewModel)

    fun displayCaptureFinish(viewModel: CaptureModels.CaptureFinish.ViewModel)

    fun displayCaptureSuccess(viewModel: CaptureModels.CaptureSuccess.ViewModel)

    fun displayCaptureFailure(viewModel: CaptureModels.CaptureFailure.ViewModel)

    fun displayError(viewModel: CaptureModels.Error.ViewModel)
}