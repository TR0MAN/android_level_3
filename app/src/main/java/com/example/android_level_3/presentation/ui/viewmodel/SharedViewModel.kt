package com.example.android_level_3.presentation.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android_level_3.domain.constants.RequestConst
import com.example.android_level_3.data.SharedPreferencesStorage
import com.example.android_level_3.data.retrofit.Retrofit
import com.example.android_level_3.data.retrofit.RetrofitServerApi
import com.example.android_level_3.data.retrofit.model.Contact
import com.example.android_level_3.data.retrofit.model.ContactId
import com.example.android_level_3.data.retrofit.model.ContactsServerResponse
import com.example.android_level_3.data.retrofit.model.CreateUserModel
import com.example.android_level_3.data.retrofit.model.ExtensionServerResponse
import com.example.android_level_3.data.retrofit.model.ServerResponse
import com.example.android_level_3.data.retrofit.model.UserAuthorisationEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import retrofit2.Response
import java.net.ConnectException

class SharedViewModel(
    val dataStorage: SharedPreferencesStorage
): ViewModel() {

    val tabLayoutVisibility = MutableLiveData<Boolean>(true)

    var serverApi: RetrofitServerApi

    // user data for server requests
    private val _token = MutableLiveData<String>()
    val token: LiveData<String> = _token

    private val _userId = MutableLiveData<Int>()
    val userId: LiveData<Int> = _userId

    // state of fields visibility for search mode (list of all users and list of contacts)
    val isActiveSearchAddContact = MutableLiveData<Boolean>(false)
    val isActiveSearchUserContacts = MutableLiveData<Boolean>(false)

    // list of contacts and users after filtering by keyword
    val filteredContactsList = MutableLiveData<MutableList<Contact>>(mutableListOf())
    val filteredUserList = MutableLiveData<MutableList<Contact>>(mutableListOf())

    // observed variables, which coming server responses
    private val _registrationResult = MutableLiveData<Response<ServerResponse>?>()
    val registrationResult: LiveData<Response<ServerResponse>?> = _registrationResult

    private val _authorisationResult = MutableLiveData<Response<ServerResponse>?>()
    val authorisationResult: LiveData<Response<ServerResponse>?> = _authorisationResult

    private val _getUserContactsResult = MutableLiveData<Response<ContactsServerResponse>?>()
    val getUserContactsResult: LiveData<Response<ContactsServerResponse>?> = _getUserContactsResult

    private val _getAllUsersResult = MutableLiveData<Response<ExtensionServerResponse>?>()
    val getAllUsersResult: LiveData<Response<ExtensionServerResponse>?> = _getAllUsersResult

    private val _addToContactListResult = MutableLiveData<Response<ContactsServerResponse>?>()
    val addToContactListResult: LiveData<Response<ContactsServerResponse>?> =_addToContactListResult

    private val _deleteFromContactsResult = MutableLiveData<Response<ContactsServerResponse>?>()
    val deleteFromContactsResult: LiveData<Response<ContactsServerResponse>?> = _deleteFromContactsResult

    // list with all user contacts
    val listWithAllContacts = MutableLiveData<List<Contact>>()
    // list of all contacts available for adding
    val listOfAllUsers = MutableLiveData<List<Contact>>()
    // list of contact ID, which have already been added
    var userContactsIdList = mutableSetOf<Int>()
    // list of contacts ID for group deleting
    val listSelectedContactsForGroupDelete = MutableLiveData<MutableSet<Int>>(mutableSetOf())

    // variables for show/hide progressbar when a request to the server occurs
    val isVisibleProgressBarInFragmentAddContact = MutableLiveData(false)
    val isVisibleProgressBarInFragmentContactsList = MutableLiveData(false)
    val isVisibleProgressBarInFragmentSettings = MutableLiveData(false)
    val isVisibleProgressBarInAuthorizationActivity = MutableLiveData(false)
    val isVisibleProgressBarInRegistrationActivity = MutableLiveData(false)

    init {
        serverApi = Retrofit.createRetrofitApi()
        Log.d("TAG", "--- ViewModel CREATED, $this")
        Log.d("TAG", "--- ViewModel CREATED, [dataStorage = $dataStorage]\n\n")
    }

    fun registerNewUser(newUserData: CreateUserModel, progressBar: MutableLiveData<Boolean>) {
        viewModelScope.launch {
            progressBar.postValue(true)
            val job = withTimeoutOrNull(RequestConst.REQUEST_MAX_DELAY) {
                try {
                    val serverResponse = serverApi.registerNewUser(newUserData)
                    delay(RequestConst.REQUEST_DELAY)
                    _registrationResult.postValue(serverResponse)
                } catch (e: ConnectException) {
                    _registrationResult.postValue(null)
                }
            }
            if (job == null) {
                _registrationResult.postValue(null)
            }
            progressBar.postValue(false)
        }
    }

    fun getAuthorisation(email: String, password: String, progressBar: MutableLiveData<Boolean>) {
        viewModelScope.launch {
            progressBar.postValue(true)
            val job = withTimeoutOrNull(RequestConst.REQUEST_MAX_DELAY) {
                try {
                    val serverResponse = serverApi.authoriseUser(
                        UserAuthorisationEntity(email = email, password = password)
                    )
                    delay(RequestConst.REQUEST_DELAY)
                    _authorisationResult.postValue(serverResponse)
                } catch (e: ConnectException) {
                    _authorisationResult.postValue(null)
                }
            }
            if (job == null) {
                _authorisationResult.postValue(null)
            }
            progressBar.postValue(false)
        }
    }

    fun getUserContacts(userId: Int, token: String, progressBar: MutableLiveData<Boolean>) {
        viewModelScope.launch {
            progressBar.postValue(true)
            val job = withTimeoutOrNull(RequestConst.REQUEST_MAX_DELAY) {
                try {
                    val serverResponse = serverApi.getUserContacts(userId, token)
                    delay(RequestConst.REQUEST_DELAY)
                    _getUserContactsResult.postValue(serverResponse)
                } catch (e: ConnectException) {
                    _getUserContactsResult.postValue(null)
                }
            }
            if (job == null) {
                _getUserContactsResult.postValue(null)
            }
            progressBar.postValue(false)
        }
    }

    fun getAllUsers(token: String, progressBar: MutableLiveData<Boolean>) {
        viewModelScope.launch {
            progressBar.postValue(true)
            val job = withTimeoutOrNull(RequestConst.REQUEST_MAX_DELAY) {
                try {
                    val serverResponse = serverApi.getAllUsers(token)
                    delay(RequestConst.REQUEST_DELAY)
                    _getAllUsersResult.postValue(serverResponse)
                } catch (e: ConnectException) {
                    _getAllUsersResult.postValue(null)
                }
            }
            if (job == null) {
                _getAllUsersResult.postValue(null)
            }
            progressBar.postValue(false)
        }
    }

    fun addToContactList(userId: Int, token: String, contactId: Int, progressBar: MutableLiveData<Boolean>) {
        viewModelScope.launch {
            progressBar.postValue(true)
            val job = withTimeoutOrNull(RequestConst.REQUEST_MAX_DELAY) {
                try {
                    val serverResponse =
                        serverApi.addContactToUserContactList( userId = userId,
                        accessToken = token, contactId = ContactId(contactId)
                        )
                    delay(RequestConst.REQUEST_DELAY)
                    _addToContactListResult.postValue(serverResponse)
                } catch (e: ConnectException) {
                    _addToContactListResult.postValue(null)
                }
            }
            if (job == null) {
                _addToContactListResult.postValue(null)
            }
            progressBar.postValue(false)
        }
    }

    fun deleteFromContacts(userId: Int, token: String, idForDelete: Int, progressBar: MutableLiveData<Boolean>) {
        viewModelScope.launch {
            progressBar.postValue(true)
            val job = withTimeoutOrNull(RequestConst.REQUEST_MAX_DELAY) {
                try {
                    val serverResponse = serverApi.deleteContactFromUserList(
                        userId = userId, accessToken = token, contactIdForDelete = idForDelete)
                    delay(RequestConst.REQUEST_DELAY)
                    _deleteFromContactsResult.postValue(serverResponse)
                } catch (e: ConnectException) {
                    _deleteFromContactsResult.postValue(null)
                }
            }
            if (job == null) {
                _deleteFromContactsResult.postValue(null)
            }
            progressBar.postValue(false)
        }
    }

    fun updateTokenAndUserId(accessToken: String?, id: Int?) {
        accessToken?.let { _token.value = "Bearer $it" }
        id?.let { _userId.value = it }
    }

}