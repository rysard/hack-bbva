package com.idemia.biosmart.scenes.authenticate

/**
 *  Authenticate Models
 *  BioSmart
 *  Created by alfredo on 12/17/18.
 *  Copyright (c) 2018 Alfredo. All rights reserved.
 */
class AuthenticateModels {

    enum class RequestCode(val value: Int){
        REQUEST_CODE_FACE(0x64),
        REQUEST_CODE_HAND_LEFT(0xC8),
        REQUEST_CODE_HAND_RIGHT(0x12C)
    }

    enum class Operation {
        CAPTURE_FINGERS,
        CAPTURE_FINGERS_CONTACTLESS,
        CAPTURE_FACE,
        START_PROCESS
    }

    //region Go to next scene
    //
    class GoToNextScene {
        data class Request(val operation: AuthenticateModels.Operation)
        class Response(val operation: AuthenticateModels.Operation)
        class ViewModel(val operation: AuthenticateModels.Operation)
    }
    //endregion
}