package com.ub.utils.ui.biometric

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.ub.security.BiometryAuthenticator
import com.ub.utils.BaseApplication
import com.ub.utils.R
import com.ub.utils.databinding.FragmentBiometricBinding
import com.ub.utils.launchAndRepeatWithViewLifecycle
import dev.chrisbanes.insetter.applyInsetter
import kotlinx.coroutines.launch
import java.util.Arrays

class BiometricFragment : Fragment(R.layout.fragment_biometric) {

    private val viewModel: BiometricViewModel by viewModels {
        BaseApplication.appComponent.viewModelFactory
    }

    private var binding: FragmentBiometricBinding? = null

    private var biometryAuthenticator: BiometryAuthenticator? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentBiometricBinding.bind(view)

        biometryAuthenticator = BiometryAuthenticator(this)

        binding?.doDecrypt?.setOnClickListener {
            viewModel.doDecrypt()
        }
        binding?.doEncrypt?.setOnClickListener {
            val valueToEncrypt = (binding?.inputField?.text?.toString()?: "").toByteArray()
            viewLifecycleOwner.lifecycleScope.launch {
                val result = biometryAuthenticator?.authAndSave(
                    valueToSave = valueToEncrypt,
                    keyName = "androidCore",
                    negativeText = "Cancel",
                    title = "Saving test value"
                )
                when {
                    result?.isSuccess == true -> viewModel.saveEncryptedValue(result.getOrNull()!!)
                    result?.isFailure == true -> viewModel.onError(result.exceptionOrNull()!!)
                }
            }
        }
        binding?.doEncrypt?.isEnabled = binding?.inputField?.text?.toString().isNullOrEmpty()
        binding?.inputField?.addTextChangedListener(
            afterTextChanged = { text ->
                binding?.doEncrypt?.isEnabled = text?.isNotEmpty() == true
            }
        )
        binding?.root?.applyInsetter {
            type(statusBars = true) {
                padding()
            }
        }

        launchAndRepeatWithViewLifecycle {
            launch {
                viewModel.biometryAvailabilityFlow.collect { isAvailable ->
                    binding?.inputField?.isEnabled = isAvailable
                    if (!isAvailable) {
                        binding?.errors?.isVisible = true
                        binding?.errors?.setText(R.string.biometry_is_not_available)
                    }
                }
            }
            launch {
                viewModel.encryptedValueFlow.collect { encryptedValue ->
                    binding?.encryptedText?.text = Arrays.toString(encryptedValue?.ciphertext)
                    binding?.doDecrypt?.isEnabled = encryptedValue != null
                    if (encryptedValue != null) {
                        binding?.errors?.text = null
                    }
                }
            }
            launch {
                viewModel.errorFlow.collect { error ->
                    binding?.errors?.text = error
                    binding?.errors?.isVisible = !error.isNullOrEmpty()
                }
            }
            launch {
                viewModel.toDecryptFlow.collect { toDecryptValue ->
                    val result = biometryAuthenticator?.authAndRestore(
                        keyName = "androidCore",
                        encryptedValue = toDecryptValue.ciphertext,
                        iv = toDecryptValue.initializationVector,
                        title = "Restoring test value",
                        negativeText = "Cancel"
                    )
                    when {
                        result?.isSuccess == true -> binding?.inputField?.setText(String(result.getOrNull()!!))
                        result?.isFailure == true -> viewModel.onError(result.exceptionOrNull()!!)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}