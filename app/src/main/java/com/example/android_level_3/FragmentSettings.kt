package com.example.android_level_3

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import com.example.android_level_3.databinding.FragmentSettingsBinding
import com.example.android_level_3.viewmodel.MainViewModel
import com.google.android.material.snackbar.Snackbar

class FragmentSettings : Fragment() {

    private lateinit var binding: FragmentSettingsBinding

    private val viewModel: MainViewModel by activityViewModels()
    private val sharedPreferences by lazy { requireActivity().getSharedPreferences(Const.PREFERENCES_SETTINGS,
        AppCompatActivity.MODE_PRIVATE) }

    private var connectionErrorSnackbar: Snackbar? = null
    private var isVisibleProgressBar = MutableLiveData(false)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)

        setUserDataToUI()
        setObservers()
        setButtonListener()

        if (sharedPreferences?.getBoolean(Const.PREFERENCES_AUTOLOGIN, false) == true) {
            refreshUserDataAfterAutoLogin()
        }
        return binding.root

//      стало не нужным (в задании уроаня 4) ввиду использования TabLayout (пока оставить)
//        binding.btnSettingsViewContacts.setOnClickListener {
//            findNavController().navigate(R.id.action_fragmentSettings_to_fragmentContactsList)
//        }
    }

    private fun setObservers() {
        isVisibleProgressBar.observe(viewLifecycleOwner) { visibility ->
            if (visibility) binding.progressBar.visibility = View.VISIBLE
            else binding.progressBar.visibility = View.GONE
        }

        viewModel.authorisationResult.observe(viewLifecycleOwner) { serverResponse ->
            if (serverResponse == null) {                                                           // показываем Snackbar с ошибкой и даем возможность перезапустить
                connectionErrorSnackbar = createErrorConnectionSnackBar()
                connectionErrorSnackbar?.show()
            } else if (serverResponse.isSuccessful) {

                // сохраняем обновленные данные AccessToken и RefreshToken (после авторизации)
                sharedPreferences.edit().apply {
                    putString(Const.PREFERENCES_ACCESS_TOKEN, serverResponse.body()?.data?.accessToken.toString())
                    putString(Const.PREFERENCES_REFRESH_TOKEN, serverResponse.body()?.data?.refreshToken.toString())
                }.apply()

                viewModel.token.value = "Bearer ${serverResponse.body()?.data?.accessToken}"
                viewModel.userId.value = serverResponse.body()?.data?.user?.id

            } else {
                // TODO - возможно вместо авторизации делать REFRESH TOKEN (на какой ответ ориентироваться?)
                connectionErrorSnackbar = createErrorConnectionSnackBar()
                connectionErrorSnackbar?.show()
            }
        }
    }

    // авторизация, если был "автологин"
    private fun refreshUserDataAfterAutoLogin() {
        viewModel.getAuthorisation(
            email = sharedPreferences?.getString(Const.PREFERENCES_EMAIL, null).toString(),
            password = sharedPreferences.getString(Const.PREFERENCES_PASSWORD, null).toString(),
            isVisibleProgressBar)
    }

    private fun createErrorConnectionSnackBar(): Snackbar {
        return Snackbar.make(binding.root, "Problem with connection...", Snackbar.LENGTH_INDEFINITE)
            .setActionTextColor(requireActivity().getColor(R.color.orange_color))
            .setAction("TRY AGAIN") {
                refreshUserDataAfterAutoLogin()
            }
    }

    // подстановка данных о пользователе (при автологине)
    private fun setUserDataToUI() {
        with(binding) {
            sharedPreferences.getString(Const.PREFERENCES_USER_NAME, null)?.let {tvProfileName.text = it}
            sharedPreferences.getString(Const.PREFERENCES_USER_CAREER, null)?.let { tvProfileProfession.text = it }
            sharedPreferences.getString(Const.PREFERENCES_USER_ADDRESS, null)?.let { tvProfileAddress.text = it }
            viewModel.token.value = "Bearer ${sharedPreferences.getString(Const.PREFERENCES_ACCESS_TOKEN, "")}"
            viewModel.userId.value = sharedPreferences.getInt(Const.PREFERENCES_USER_ID, 0)
        }
    }

    private fun setButtonListener() {

        // выход на страницу авторизации по кнопке Logout + очистка от функции "автологина"
        binding.btnMyProfileLogOut.setOnClickListener {
            sharedPreferences.edit()?.apply {
                clear()
            }?.apply()
            requireActivity().finish()
        }
    }

}