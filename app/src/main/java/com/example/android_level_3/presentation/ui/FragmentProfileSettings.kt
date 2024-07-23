package com.example.android_level_3.presentation.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.android_level_3.R
import com.example.android_level_3.databinding.FragmentSettingsBinding
import com.example.android_level_3.presentation.ui.viewmodel.SharedViewModel
import com.example.android_level_3.presentation.utils.ext.gone
import com.example.android_level_3.presentation.utils.ext.visible
import com.google.android.material.snackbar.Snackbar

class FragmentProfileSettings : Fragment() {

    private lateinit var binding: FragmentSettingsBinding

    private val viewModel: SharedViewModel by activityViewModels()

    private var connectionErrorSnackbar: Snackbar? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root

//      стало не нужным (в задании уроаня 4) ввиду использования TabLayout (пока оставить)
//        binding.btnSettingsViewContacts.setOnClickListener {
//            findNavController().navigate(R.id.action_fragmentSettings_to_fragmentContactsList)
//        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("TAG", "FragmentSettings -> onViewCreated")
        Log.d("TAG", "FragmentSettings -> USE[$viewModel]")
        Log.d("TAG", "FragmentSettings -> DATA STORAGE -> [${viewModel.dataStorage}]")
        Log.d("TAG", "FragmentSettings [END] -> -----------------------------------")

        setUserDataToUI()
        setObservers()
        setButtonListener()

        checkingAutoLogin()
    }

    private fun checkingAutoLogin() {
        if (viewModel.dataStorage.checkingAuthorizationNeed()) {
            refreshUserDataAfterAutoLogin()
        }
    }

    private fun setObservers() {

        viewModel.isVisibleProgressBarInFragmentSettings.observe(viewLifecycleOwner) { visibility ->
            if (visibility) binding.progressBar.visible()
            else binding.progressBar.gone()
        }

        viewModel.authorisationResult.observe(viewLifecycleOwner) { serverResponse ->
            if (serverResponse == null) {
                connectionErrorSnackbar = createErrorConnectionSnackBar()
                connectionErrorSnackbar?.show()
            } else if (serverResponse.isSuccessful) {

                // save updated data AccessToken and RefreshToken (after authorisation)
                viewModel.dataStorage.updateTokens(
                    accessToken = serverResponse.body()?.data?.accessToken.toString(),
                    refreshToken = serverResponse.body()?.data?.refreshToken.toString()
                )

                viewModel.updateTokenAndUserId(
                    accessToken = serverResponse.body()?.data?.accessToken,
                    id = serverResponse.body()?.data?.user?.id )

//                viewModel.token.value = "Bearer ${serverResponse.body()?.data?.accessToken}"
//                viewModel.userId.value = serverResponse.body()?.data?.user?.id

            } else {
                // TODO - возможно вместо авторизации делать REFRESH TOKEN (на какой ответ ориентироваться?)
                connectionErrorSnackbar = createErrorConnectionSnackBar()
                connectionErrorSnackbar?.show()
            }
        }
    }

    // make authorisation if autologin be checked
    private fun refreshUserDataAfterAutoLogin() {
        viewModel.getAuthorisation(
            email = viewModel.dataStorage.getUserEmailFromStorage(),
            password = viewModel.dataStorage.getUserPasswordFromStorage(),
            progressBar = viewModel.isVisibleProgressBarInFragmentSettings)
    }

    private fun createErrorConnectionSnackBar(): Snackbar {
        return Snackbar.make(
            binding.root,
            getString(R.string.connection_error_snackbar_message), Snackbar.LENGTH_INDEFINITE
        )
            .setActionTextColor(requireActivity().getColor(R.color.orange_color))
            .setAction(getString(R.string.connection_error_snackbar_action_button_text)) {
                refreshUserDataAfterAutoLogin()
            }
    }

    // insert user data in fields (when be checked autologin)
    private fun setUserDataToUI() {
        with(binding) {
            tvProfileName.text = viewModel.dataStorage.getUserNameFromStorage()
            tvProfileProfession.text = viewModel.dataStorage.getUserCareerFromStorage()
            tvProfileAddress.text = viewModel.dataStorage.getUserAddressFromStorage()
            viewModel.updateTokenAndUserId(
                accessToken = viewModel.dataStorage.getUserAccessTokenFromStorage(),
                id = viewModel.dataStorage.getUserIdFromStorage()
            )

//            viewModel.token.value = viewModel.dataStorage.getUserAccessTokenFromStorage()
//            viewModel.userId.value = viewModel.dataStorage.getUserIdFromStorage()
        }
    }


    private fun setButtonListener() {
        // logout from account and clearing "autologin" data
        binding.btnMyProfileLogOut.setOnClickListener {
            viewModel.dataStorage.clearSharedPreferencesData()
            requireActivity().finish()
        }
    }

}