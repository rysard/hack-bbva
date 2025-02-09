package com.idemia.biosmart.scenes.face_info

/**
 *  FaceInfo Interactor
 *  BioSmart
 *  Created by alfredo on 1/4/19.
 *  Copyright (c) 2019 Alfredo. All rights reserved.
 */
class FaceInfoInteractor : FaceInfoBusinessLogic {
    private val worker = FaceInfoWorker()
    private var presenter: FaceInfoPresentationLogic = FaceInfoPresenter()

    fun setPresenter(presenter: FaceInfoPresentationLogic) {
        this.presenter = presenter
    }

    //region Set display this tutorial
    override fun setDisplayThisTutorial(request: FaceInfoModels.SetDisplayThisTutorial.Request) {
        worker.setDisplayThisTutorial(request)
        val response = FaceInfoModels.SetDisplayThisTutorial.Response(request.doNotShowTutorial)
        presenter.presentSetDisplayThisTutorial(response)
    }
    //endregion

    //region Display this tutorial
    override fun displayThisTutorial(request: FaceInfoModels.DisplayThisTutorial.Request) {
        val flag = worker.shouldDisplayTutorial(request)
        val response = FaceInfoModels.DisplayThisTutorial.Response(flag)
        presenter.presentDisplayThisTutorial(response)
    }
    //endregion

    //region Go to next scene
    override fun goToNextScene(request: FaceInfoModels.GoToNextScene.Request) {
        val response = FaceInfoModels.GoToNextScene.Response()
        presenter.presentGoToNextScene(response)
    }
    //endregion
}


/**
 *  FaceInfo Business Logic
 *  BioSmart
 *  Created by alfredo on 1/4/19.
 *  Copyright (c) 2019 requestAlfredo. All rights reserved.
 */
interface FaceInfoBusinessLogic {
    fun setDisplayThisTutorial(request: FaceInfoModels.SetDisplayThisTutorial.Request)
    fun displayThisTutorial(request: FaceInfoModels.DisplayThisTutorial.Request)
    fun goToNextScene(request: FaceInfoModels.GoToNextScene.Request)
}
