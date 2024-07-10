package com.example.android_level_3.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android_level_3.constants.RequestConst
import com.example.android_level_3.old_classes.TestContact
import com.example.android_level_3.retrofit.Retrofit
import com.example.android_level_3.retrofit.RetrofitServerApi
import com.example.android_level_3.retrofit.model.Contact
import com.example.android_level_3.retrofit.model.ContactId
import com.example.android_level_3.retrofit.model.ContactsServerResponse
import com.example.android_level_3.retrofit.model.CreateUserModel
import com.example.android_level_3.retrofit.model.ExtensionServerResponse
import com.example.android_level_3.retrofit.model.ServerResponse
import com.example.android_level_3.retrofit.model.UserAuthorisationEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import retrofit2.Response
import java.net.ConnectException

open class SharedViewModel: ViewModel() {

    private val contactList = MutableLiveData<List<TestContact>>()
    val observableContactList: LiveData<List<TestContact>> = contactList

    val tabLayoutVisibility = MutableLiveData<Boolean>(true)

    var serverApi: RetrofitServerApi

    // данные юзера необходимые для дальнейших запросов
    val token = MutableLiveData<String>()
    val userId = MutableLiveData<Int>()

    // поля состояния видимости для режима поиска (список всех юзеров, список контактов)
    val isActiveSearchAddContact = MutableLiveData<Boolean>(false)
    val isActiveSearchUserContacts = MutableLiveData<Boolean>(false)

    // списки контактов/юзеров удовлетворящих поисковому запросу
    val filteredContactsList = MutableLiveData<MutableList<Contact>>(mutableListOf())
    val filteredUserList = MutableLiveData<MutableList<Contact>>(mutableListOf())

    // наблюдаемые переменные, куда приходят результаты запроса к серверу
    val registrationResult = MutableLiveData<Response<ServerResponse>?>()
    val authorisationResult = MutableLiveData<Response<ServerResponse>?>()
    val getUserContactsResult = MutableLiveData<Response<ContactsServerResponse>?>()
    var getAllUsersResult = MutableLiveData<Response<ExtensionServerResponse>?>()
    var addToContactListResult = MutableLiveData<Response<ContactsServerResponse>?>()
    var deleteFromContactsResult = MutableLiveData<Response<ContactsServerResponse>?>()

    // список всех контактов пользователя
    val listWithAllContacts = MutableLiveData<List<Contact>>()
    // список ВСЕХ доступных для добавления юзеров
    val listOfAllUsers = MutableLiveData<List<Contact>>()
    // ID контактов, для RecView И отображения галочек (что они уже в списке)
    var userContactsIdList = mutableSetOf<Int>()
    // ID выбранных контактов для группового удаления
    val listSelectedContactsForGroupDelete = MutableLiveData<MutableSet<Int>>(mutableSetOf())

    val isVisibleProgressBarInFragmentAddContact = MutableLiveData(false)
    val isVisibleProgressBarInFragmentContactsList = MutableLiveData(false)
    val isVisibleProgressBarInFragmentSettings = MutableLiveData(false)
    val isVisibleProgressBarInAuthorizationActivity = MutableLiveData(false)
    val isVisibleProgressBarInRegistrationActivity = MutableLiveData(false)


    init {
        serverApi = Retrofit.createRetrofitApi()
    }

    fun registerNewUser(newUserData: CreateUserModel, progressBar: MutableLiveData<Boolean>) {
        viewModelScope.launch {
            progressBar.postValue(true)
            val job = withTimeoutOrNull(RequestConst.REQUEST_MAX_DELAY) {
                try {
                    val serverResponse = serverApi.registerNewUser(newUserData)
                    delay(RequestConst.REQUEST_DELAY)
                    registrationResult.postValue(serverResponse)
                } catch (e: ConnectException) {
                    registrationResult.postValue(null)
                }
            }
            if (job == null) {
                registrationResult.postValue(null)
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
                        UserAuthorisationEntity(email = email, password = password))
                    delay(RequestConst.REQUEST_DELAY)
                    authorisationResult.postValue(serverResponse)
                } catch (e: ConnectException) {
                    authorisationResult.postValue(null)
                }
            }
            if (job == null) {
                authorisationResult.postValue(null)
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
                    getUserContactsResult.postValue(serverResponse)
                } catch (e: ConnectException) {
                    getUserContactsResult.postValue(null)
                }
            }
            if (job == null) {
                getUserContactsResult.postValue(null)
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
                    getAllUsersResult.postValue(serverResponse)
                } catch (e: ConnectException) {
                    getAllUsersResult.postValue(null)
                }
            }
            if (job == null) {
                getAllUsersResult.postValue(null)
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
                        accessToken = token, contactId = ContactId(contactId))
                    delay(RequestConst.REQUEST_DELAY)
                    addToContactListResult.postValue(serverResponse)
                } catch (e: ConnectException) {
                    addToContactListResult.postValue(null)
                }
            }
            if (job == null) {
                addToContactListResult.postValue(null)
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
                    deleteFromContactsResult.postValue(serverResponse)
                } catch (e: ConnectException) {
                    deleteFromContactsResult.postValue(null)
                }
            }
            if (job == null) {
                deleteFromContactsResult.postValue(null)
            }
            progressBar.postValue(false)
        }
    }

}