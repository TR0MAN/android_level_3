package com.example.android_level_3

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import com.example.android_level_3.databinding.FragmentSettingsBinding
import com.example.android_level_3.retrofit.model.ServerResponse
import com.example.android_level_3.retrofit.model.UserAuthorisationEntity
import com.example.android_level_3.retrofit.model.UserData
import com.example.android_level_3.viewmodel.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import retrofit2.Response
import java.net.ConnectException

class FragmentSettings : Fragment() {

    private lateinit var binding: FragmentSettingsBinding

    private val viewModel: MainViewModel by activityViewModels()
    private val sharedPreferences by lazy { requireActivity().getSharedPreferences(Const.PREFERENCES_SETTINGS,
        AppCompatActivity.MODE_PRIVATE) }

    private var isVisibleProgressBar = MutableLiveData(false)
    private var authorisationAction = MutableLiveData<Response<ServerResponse>?>()
    private var userData = MutableLiveData<UserData?>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)

        parseUserData()
        setObservers()
        setButtonListener()

        if (sharedPreferences?.getBoolean(Const.PREFERENCES_AUTOLOGIN, false) == true) {
            CoroutineScope(Dispatchers.IO).launch {
                refreshUserDataAfterAutoLogin(
                    email = sharedPreferences?.getString(Const.PREFERENCES_EMAIL, null).toString(),
                    password = sharedPreferences.getString(Const.PREFERENCES_PASSWORD, null).toString()
                )
            }
        }

        return binding.root

//      стало не нужным (в задании уроаня 4) ввиду использования TabLayout (пока оставить)
//        binding.btnSettingsViewContacts.setOnClickListener {
//            findNavController().navigate(R.id.action_fragmentSettings_to_fragmentContactsList)
//        }
    }

    private fun setObservers() {
        isVisibleProgressBar.observe(requireActivity()) {
            if (it) binding.progressBar.visibility = View.VISIBLE
            else binding.progressBar.visibility = View.GONE
        }

        authorisationAction.observe(requireActivity()) { serverResponse ->

            // если пришел NULL, показываем Snackbar с ошибкой и даем возможность перезапустить
            if (serverResponse == null) {
                Toast.makeText(requireActivity(), "Connection problem....", Toast.LENGTH_SHORT).show()
            } else if (serverResponse.isSuccessful) {

                // убираем ProgressBar и переходим в ПРОФИЛЬ
                // ответ ОК (такой USER ЕСТЬ)
                // пришли данные о USER + AccessToken и RefreshToken
                Log.d("TAG", "ServerResponse = OK")
                Log.d("TAG", "ServerResponse.message = [${serverResponse.message()}]")
                Log.d("TAG", "ServerResponse.body()?.message = [${serverResponse.body()?.message}]")
                Log.d("TAG", "ServerResponse.body()?.status = [${serverResponse.body()?.status}]")
                Log.d("TAG","ServerResponse.body()?.data?.accessToken = [${serverResponse.body()?.data?.accessToken}]")

                // сохраняем данные о юзере
                userData.value = serverResponse.body()?.data

            } else {
                // TODO - обработать вариат неудачной авторизации (перезапустить или отлавливать)
                Log.d("TAG", "ServerResponse = ERROR")
                Log.d("TAG", "ServerResponse.message = [${serverResponse.message()}]")
                Log.d("TAG", "ServerResponse.code = [${serverResponse.code()}]")
                Log.d("TAG", "ServerResponse.errorBody = [${serverResponse.errorBody()}]")

            }
        }
    }

    private suspend fun refreshUserDataAfterAutoLogin(email: String, password: String) {
        Log.d("TAG", "FUN authorizationRequest() -> INSIDE")

        // показываем ProgressBar
        isVisibleProgressBar.postValue(true)

        // если запрос дольше 5 сек, возвращаем NULL
        val job = withTimeoutOrNull(5000L) {
            try {
                val serverResponse = viewModel.serverApi.authoriseUser(
                    UserAuthorisationEntity(
                        email = email,
                        password = password
                    )
                )
                delay(2000L)     // иммитация запроса (DELETE)
                authorisationAction.postValue(serverResponse)
            } catch (e: ConnectException) {                         // ловим проблему с отключенным И-нетом и перезапускаем
                authorisationAction.postValue(null)
            }
        }
        // если долгий запрос, возвращаем NULL и пробуем перезапустить
        if (job == null) {
            authorisationAction.postValue(null)
        }
        // убираем ProgressBar
        isVisibleProgressBar.postValue(false)

    }

    private fun parseUserData() {
        with(binding) {
            sharedPreferences.getString(Const.PREFERENCES_USER_NAME, null)?.let {tvProfileName.text = it}
            sharedPreferences.getString(Const.PREFERENCES_USER_CAREER, null)?.let { tvProfileProfession.text = it }
            sharedPreferences.getString(Const.PREFERENCES_USER_ADDRESS, null)?.let { tvProfileAddress.text = it }

            // TODO - DELETE AFTER TEST
            Log.d("TAG", "FRAGMENT[email]= ${sharedPreferences.getString(Const.PREFERENCES_EMAIL, "")}")
            Log.d("TAG", "FRAGMENT[pass]= ${sharedPreferences.getString(Const.PREFERENCES_PASSWORD, "")}")
            Log.d("TAG", "FRAGMENT[access_token]= ${sharedPreferences.getString(Const.PREFERENCES_AUTHORISATION_TOKEN, "")}")
            Log.d("TAG", "FRAGMENT[ref_token]= ${sharedPreferences.getString(Const.PREFERENCES_REFRESH_TOKEN, "")}")
        }
    }

    private fun setButtonListener() {
        // выход на страницу авторизации по кнопке Logout
        binding.btnMyProfileLogOut.setOnClickListener {
            sharedPreferences.edit()?.apply {
                clear()
            }?.apply()
            requireActivity().finish()
        }
    }

}