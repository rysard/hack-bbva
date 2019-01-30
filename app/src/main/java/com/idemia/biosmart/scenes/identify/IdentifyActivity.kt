package com.idemia.biosmart.scenes.identify

import android.graphics.BitmapFactory
import android.os.Bundle
import com.idemia.biosmart.R
import com.idemia.biosmart.base.android.BaseActivity
import com.idemia.biosmart.scenes.user_info.UserInfoActivity
import com.idemia.biosmart.utils.AppCache
import kotlinx.android.synthetic.main.activity_identify.*

/**
 *  Identify Activity
 *  BioSmart
 *  Created by alfredo on 12/17/18.
 *  Copyright (c) 2018 Alfredo. All rights reserved.
 */
class IdentifyActivity : BaseActivity(), IdentifyDisplayLogic {
    private lateinit var interactor: IdentifyBusinessLogic    // Interactor
    private lateinit var router: IdentifyRoutingLogic         // Router

    companion object {
        private val TAG = "IdentifyActivity"
    }

    override fun resourceLayoutId(): Int = R.layout.activity_identify
    override fun hideActionBar(): Boolean = false
    override fun hideNavigationBar(): Boolean = false

    override fun onResume() {
        super.onResume()

        // TODO: Create a use case "retrieve selfie"
        AppCache.facePhoto?.let { photo ->
            val data = photo.jpegImage
            val bmp = BitmapFactory.decodeByteArray(data, 0, data!!.size)
            image_view_selfie.setImageBitmap(bmp)
        }
    }

    override fun onLoadActivity(savedInstanceState: Bundle?) {
        float_button_selfie.setOnClickListener { goToNextScene(IdentifyModels.Operation.CAPTURE_FACE) }
        button_start_process.setOnClickListener { goToNextScene(IdentifyModels.Operation.START_PROCESS) }
        button_capture_fingers.setOnClickListener {
            if(switch_enable_contactless.isChecked)
                goToNextScene(IdentifyModels.Operation.CAPTURE_FINGERS_CONTACTLESS)
            else
                goToNextScene(IdentifyModels.Operation.CAPTURE_FINGERS)
        }
    }

    override fun inject() {
        val activity = this
        this.interactor = IdentifyInteractor()
        this.router = IdentifyRouter()
        val presenter = IdentifyPresenter()
        (this.interactor as IdentifyInteractor).setPresenter(presenter)
        presenter.setActivity(activity)
        (router as IdentifyRouter).setActivity(this)
    }

    //region USECASE - Go to next scene
    private fun goToNextScene(operation: IdentifyModels.Operation){
        if(isDataValid()){
            val request = IdentifyModels.GoToNextScene.Request(operation)
            interactor.goToNextScene(request)
        }else{
            showToast(getString(R.string.label_no_biometric_data))
        }
    }

    override fun displayGoToNextScene(viewModel: IdentifyModels.GoToNextScene.ViewModel) {
        when(viewModel.operation){
            IdentifyModels.Operation.CAPTURE_FACE -> { router.routeToCaptureFaceScene() }
            IdentifyModels.Operation.CAPTURE_FINGERS -> { router.routeToCaptureFingersMsoScene() }
            IdentifyModels.Operation.CAPTURE_FINGERS_CONTACTLESS -> { router.routeToCaptureFingersScene() }
            IdentifyModels.Operation.START_PROCESS -> { router.routeToStartProcessScene(UserInfoActivity.IDENTIFY_USER) }
        }
    }
    //endregion

    private fun isDataValid(): Boolean {
        // return (AppCache.facePhoto != null || AppCache.imageListLeft!=null || AppCache.imageListRight!=null)
        return true
    }
}

/**
 *  Identify Display Logic
 *  BioSmart
 *  Created by alfredo on 12/17/18.
 *  Copyright (c) 2018 Alfredo. All rights reserved.
 */
interface IdentifyDisplayLogic {
    fun displayGoToNextScene(viewModel: IdentifyModels.GoToNextScene.ViewModel)
}