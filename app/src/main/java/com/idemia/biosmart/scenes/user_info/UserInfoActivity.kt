package com.idemia.biosmart.scenes.user_info

import android.widget.Toast
import com.idemia.biosmart.R
import com.idemia.morphobiosmart.android.BaseActivity
import com.idemia.morphobiosmart.utils.Base64
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import com.idemia.biosmart.scenes.enrolment_details.view.fragments.MatchPersonToPersonDataFragment
import com.idemia.biosmart.scenes.user_info.view.adapters.ViewPageUserInfoAdapter
import com.idemia.biosmart.scenes.user_info.view.fragments.UserInfoDataFragment
import com.idemia.biosmart.scenes.user_info.view.fragments.UserInfoTechnicalDetailsFragment
import com.idemia.morphobiosmart.utils.IDMProgress
import kotlinx.android.synthetic.main.activity_userinfo.*

/**
 *  UserInfo Activity
 *  BioSmart
 *  Created by alfredo on 12/17/18.
 *  Copyright (c) 2018 Alfredo. All rights reserved.
 */
class UserInfoActivity : BaseActivity(), UserInfoDisplayLogic {
    private lateinit var interactor: UserInfoBusinessLogic    // Interactor
    private lateinit var router: UserInfoRoutingLogic         // Router

    companion object {
        private val TAG = "UserInfoActivity"
        const val AUTHENTICATE_USER = 0x01
        const val IDENTIFY_USER = 0x02
        const val KEY_OPERATION_TYPE = "OPERATION_TYPE"
    }

    override fun resourceLayoutId(): Int = R.layout.activity_userinfo
    override fun hideActionBar(): Boolean = false
    override fun hideNavigationBar(): Boolean = false

    //region VARS - Local variables
    private val userInfoDataFragment = UserInfoDataFragment()
    private val userInfoTechnicalDetailsFragment = UserInfoTechnicalDetailsFragment()
    private val matchPersonToPersonDataFragment = MatchPersonToPersonDataFragment()
    private val viewPageUserInfoAdapter = ViewPageUserInfoAdapter(supportFragmentManager)
    //endregion

    //region BASE ACTIVITY - On load activity
    override fun onLoadActivity(savedInstanceState: Bundle?) {
        initButton()    // Init ui button

        val operation = intent.getIntExtra(KEY_OPERATION_TYPE, 0x00)    // Select operation type
        verifyOperationType(operation)
    }
    //endregion

    //region BASE ACTIVITY - A "dependency injection"
    override fun inject() {
        val activity = this
        this.interactor = UserInfoInteractor()
        this.router = UserInfoRouter()
        val presenter = UserInfoPresenter()
        (this.interactor as UserInfoInteractor).setPresenter(presenter)
        presenter.setActivity(activity)
        (router as UserInfoRouter).setActivity(this)
    }
    //endregion

    //region USE CASE - Authenticate user
    private fun authenticateUser(){
        loader = IDMProgress(
            this,
            "Authenticating Person",
            getString(R.string.label_please_wait)
        ).kProgress
        loader?.show()
        Log.i(TAG, "authenticateUser")
        val request = UserInfoModels.AuthenticateUser.Request(this@UserInfoActivity)
        interactor.authenticateUser(request)
    }

    override fun displayAuthenticateUser(viewModel: UserInfoModels.AuthenticateUser.ViewModel) {
        when(viewModel.authenticationResponse.code){
            200 -> {
                val candidateId = viewModel.authenticationResponse.personId
                userInfoTechnicalDetailsFragment.bind(viewModel.authenticationResponse)
                matchPersonToPersonDataFragment.bind(
                    viewModel.authenticationResponse.authenticatePerson!!.candidates,
                    viewModel.authenticationResponse.authenticatePerson.noHitRank)
                loader?.dismiss()
                search(candidateId!!)
            }
            400 -> {
                val message = getString(R.string.fatal_user_biometry_info_incomplete)
                showToast(message)
                tv_message_response.text = message
                loader?.dismiss()
                finish()
            }
            404 -> {
                val message = viewModel.authenticationResponse.authenticatePerson!!.message
                showToast(message)
                tv_message_response.text = message
                userInfoTechnicalDetailsFragment.bind(viewModel.authenticationResponse)
                loader?.dismiss()
            }
            else -> {
                val message = getString(R.string.fatal_unknown_error, "Error on displayAuthenticateUser() method")
                showToast(message)
                tv_message_response.text = message
                loader?.dismiss()
            }
        }
    }
    //endregion

    //region USE CASE - Identify User
    private fun identifyUser(){
        loader = IDMProgress(
            this,
            "Identifying Person",
            getString(R.string.label_please_wait)
        ).kProgress
        loader?.show()
        Log.i(TAG, "identifyUser")
        val request = UserInfoModels.IdentifyUser.Request(this@UserInfoActivity)
        interactor.identifyUser(request)
    }

    override fun displayIdentifyUser(viewModel: UserInfoModels.IdentifyUser.ViewModel) {
        when(viewModel.identifyResponse.code){
            200 -> {
                val candidateId = viewModel.identifyResponse.matchPersonToPerson!!.candidates[0].id
                userInfoTechnicalDetailsFragment.bind(viewModel.identifyResponse)
                matchPersonToPersonDataFragment.bind(
                    viewModel.identifyResponse.matchPersonToPerson.candidates,
                    viewModel.identifyResponse.matchPersonToPerson.noHitRank)
                loader?.dismiss()
                search(candidateId)
            }
            400 -> {
                val message = getString(R.string.fatal_user_biometry_info_incomplete)
                showToast(message)
                tv_message_response.text = message
                loader?.dismiss()
                finish()
            }
            404 -> {
                val message = viewModel.identifyResponse.message
                showToast(message)
                tv_message_response.text = message
                userInfoTechnicalDetailsFragment.bind(viewModel.identifyResponse)
                loader?.dismiss()
            }
            else -> {
                val message = getString(R.string.fatal_unknown_error, "Error on displayIdentifyUser() method")
                showToast(message)
                tv_message_response.text = message
                loader?.dismiss()
            }
        }
    }
    //endregion

    //region USE CASE - Search User in DB
    /**
     * Search User in DB
     */
    private fun search(username: String) {
        loader = IDMProgress(
            this,
            "Getting User Info",
            getString(R.string.label_please_wait)
        ).kProgress
        loader?.show()
        val searchPersonRequest = UserInfoModels.SearchPersonRequest(username, 1)
        val request = UserInfoModels.Search.Request(this@UserInfoActivity, searchPersonRequest)
        interactor.search(request)
    }

    override fun displaySearch(viewModel: UserInfoModels.Search.ViewModel) {
        if(viewModel.userFound){
            val message = getString(R.string.message_user_found)
            displaySearchSuccess(message , viewModel.user!!)
            tv_message_response.text = getString(R.string.message_user_found)
        }else{
            val message = getString(R.string.message_user_not_found)
            displaySearchNotFound(message)
            tv_message_response.text = message
        }
    }

    private fun displaySearchSuccess(message: String, user: UserInfoModels.User){
        Toast.makeText(applicationContext, message.toLowerCase().capitalize(), Toast.LENGTH_LONG).show()
        user.photo?.let {
            val photo = Base64.decode(user.photo)
            val options = BitmapFactory.Options()
            options.inMutable = true
            val bmp = BitmapFactory.decodeByteArray(photo, 0, photo.size, options)
            image_view_photo.setImageBitmap(bmp)
        }
        userInfoDataFragment.dataBinding(user)
        loader?.dismiss()
    }

    private fun displaySearchNotFound(message: String){
        tv_message_response.text = message
        Toast.makeText(applicationContext, message.toLowerCase().capitalize(), Toast.LENGTH_LONG).show()
        userInfoDataFragment.dataBinding(null)
        loader?.dismiss()
    }
    //endregion

    //region USE CASE - Display Error
    override fun displayError(viewModel: UserInfoModels.Error.ViewModel) {
        Log.e(TAG, "ERROR: ${viewModel.throwable.message} with code ${viewModel.errorCode}", viewModel.throwable)
        val message = viewModel.throwable.localizedMessage
        tv_message_response.text = message
        Toast.makeText(applicationContext, message , Toast.LENGTH_LONG).show()
        loader?.dismiss()
        finish()
    }
    //endregion

    //region UI - Init view pager
    /**
     * View pager initialization
     * @param withMatchPersonToPersonTab True to show "Match person to person tab"
     */
    private fun initViewPager(withMatchPersonToPersonTab: Boolean){
        viewPageUserInfoAdapter.addFragment(userInfoDataFragment, getString(R.string.user_info_label_user_info))
        viewPageUserInfoAdapter.addFragment(userInfoTechnicalDetailsFragment, getString(R.string.user_info_label_technical_details))
        if(withMatchPersonToPersonTab){
            viewPageUserInfoAdapter.addFragment(matchPersonToPersonDataFragment, getString(R.string.user_info_label_match_person_to_person_process))
        }
        view_pager.adapter = viewPageUserInfoAdapter
        tab_layout.setupWithViewPager(view_pager)
    }
    //endregion

    //region UI - Init ui button
    private fun initButton(){
        button_finish.setOnClickListener {
            finish()
        }
    }
    //endregion

    //region VERIFY OPERATION TYPE
    /**
     * Verify operation
     * @param type Operation Type
     */
    private fun verifyOperationType(type: Int){
        when(type){
            AUTHENTICATE_USER -> {
                authenticateUser()
                initViewPager(false)
            }
            IDENTIFY_USER -> {
                identifyUser()
                initViewPager(true)
            }
            else -> showToast(getString(R.string.fatal_invalid_operation))
        }
    }
    //endregion
}

/**
 *  UserInfo Display Logic
 *  BioSmart
 *  Created by alfredo on 12/17/18.
 *  Copyright (c) 2018 Alfredo. All rights reserved.
 */
interface UserInfoDisplayLogic {
    fun displayAuthenticateUser(viewModel: UserInfoModels.AuthenticateUser.ViewModel)
    fun displayIdentifyUser(viewModel: UserInfoModels.IdentifyUser.ViewModel)
    fun displaySearch(viewModel: UserInfoModels.Search.ViewModel)
    fun displayError(viewModel: UserInfoModels.Error.ViewModel)
}