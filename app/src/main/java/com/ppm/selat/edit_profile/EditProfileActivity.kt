package com.ppm.selat.edit_profile

import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageView
import com.canhub.cropper.options
import com.ppm.selat.R
import com.ppm.selat.core.data.Resource
import com.ppm.selat.databinding.ActivityEditProfileBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class EditProfileActivity : AppCompatActivity() {

    private val editProfileViewModel: EditProfileViewModel by viewModel()
    private lateinit var binding: ActivityEditProfileBinding

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            val uriContent = result.uriContent
            val uriFilePath = result.getUriFilePath(this) // optional usage
            showPickDialog(uriContent!!)
        } else {
            val exception = result.error
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        initDataAndValidForm()
        setUpListener()
    }

    private fun initDataAndValidForm() {
        binding.editedFullName.visibility = View.GONE
        binding.editedEmail.visibility = View.GONE

        editProfileViewModel.userDataStream.asLiveData().observe(this) {
            binding.userName.text = it.name
        }

        lifecycleScope.launch {
            val data = editProfileViewModel.userDataStream.first()

            editProfileViewModel.nameInit = data.name.toString()
            editProfileViewModel.nameFlow.value = data.name.toString()
            binding.editTextFullName.setText(data.name.toString())

            editProfileViewModel.emailInit = data.email.toString()
            editProfileViewModel.emailFlow.value = data.email.toString()
            binding.editTextEmail.setText(data.email.toString())
            // PLAN
            binding.editTextEmail.isFocusable = false
            binding.editTextEmail.isEnabled = false

            editProfileViewModel.phoneInit = data.phone.toString()
            editProfileViewModel.phoneFlow.value = data.phone.toString()
            binding.editTextPhone.setText(data.phone.toString())

            Glide.with(this@EditProfileActivity)
                .load(data.photoUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.circleImageView)
        }

        with(binding) {
            editTextFullName.doAfterTextChanged {
                editProfileViewModel.nameFlow.value = it.toString().trim()
                if (editProfileViewModel.checkNameIsChanged()) {
                    binding.editedFullName.visibility = View.VISIBLE
                    binding.cancelSaveFullName.visibility = View.VISIBLE
                } else {
                    binding.editedFullName.visibility = View.GONE
                    binding.cancelSaveFullName.visibility = View.GONE
                }
                Log.d("EditProfileActivity", editProfileViewModel.checkNameIsChanged().toString())
            }
            editTextEmail.doAfterTextChanged {
                editProfileViewModel.emailFlow.value = it.toString().trim()
//                if (editProfileViewModel.checkEmailIsChanged()) {
//                    binding.editedEmail.visibility = View.VISIBLE
//                    binding.cancelSaveEmail.visibility = View.VISIBLE
//                } else {
//                    binding.editedEmail.visibility = View.GONE
//                    binding.cancelSaveEmail.visibility = View.GONE
//                }
                Log.d("EditProfileActivity", editProfileViewModel.checkEmailIsChanged().toString())
            }
            editTextPhone.doAfterTextChanged {
                editProfileViewModel.phoneFlow.value = it.toString().trim()
                if (editProfileViewModel.checkPhoneIsChanged()) {
                    binding.editedPhone.visibility = View.VISIBLE
                    binding.cancelSavePhone.visibility = View.VISIBLE
                } else {
                    binding.editedPhone.visibility = View.GONE
                    binding.cancelSavePhone.visibility = View.GONE
                }
                Log.d("EditProfileActivity", editProfileViewModel.checkEmailIsChanged().toString())
            }
        }
    }

    private fun setUpListener() {
        binding.backButton.setOnClickListener {
            finish()
        }
        binding.editPhotoButton.setOnClickListener {
            pickImageForProfilePicture()
        }

        // full name changed handler
        binding.cancelFullNameButton.setOnClickListener {
            dismissKeyboard()
            binding.editTextFullName.setText(editProfileViewModel.nameInit)
        }
        binding.saveFullNameButton.setOnClickListener {
            dismissKeyboard()
            if (isNetworkAvailable()) {
                val dialogLoading = startLoadingDialog("Simpan nama...")
                editProfileViewModel.saveNewName().observe(this) { result ->
                    if (result != null) {
                        when (result) {
                            is Resource.Loading -> {}
                            is Resource.Success -> {
                                Toast.makeText(this, "Nama diperbarui", Toast.LENGTH_SHORT).show()
                                editProfileViewModel.nameInit = editProfileViewModel.nameFlow.value
                                binding.editedFullName.visibility = View.GONE
                                binding.cancelSaveFullName.visibility = View.GONE
                                dialogLoading.dismiss()
                            }
                            is Resource.Error -> {
                                Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                                dialogLoading.dismiss()
                            }
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Tidak dapat terhubung ke internet", Toast.LENGTH_SHORT).show()
            }
        }

        // email changed handler
//        binding.cancelEmailButton.setOnClickListener {
//            dismissKeyboard()
//            binding.editTextEmail.setText(editProfileViewModel.emailInit)
//        }
//        binding.saveEmailButton.setOnClickListener {
//        }

        // phone changed handler
        binding.cancelPhoneButton.setOnClickListener {
            dismissKeyboard()
            binding.editTextPhone.setText(editProfileViewModel.phoneInit)
        }
        binding.savePhoneButton.setOnClickListener {
            dismissKeyboard()
            if (isNetworkAvailable()) {
                val dialogLoading = startLoadingDialog("Simpan nomor...")
                editProfileViewModel.saveNewPhone().observe(this) { result ->
                    if (result != null) {
                        when (result) {
                            is Resource.Loading -> {}
                            is Resource.Success -> {
                                Toast.makeText(this, "Nomor diperbarui", Toast.LENGTH_SHORT).show()
                                editProfileViewModel.phoneInit = editProfileViewModel.phoneFlow.value
                                binding.editedPhone.visibility = View.GONE
                                binding.cancelSavePhone.visibility = View.GONE
                                dialogLoading.dismiss()
                            }
                            is Resource.Error -> {
                                Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                                dialogLoading.dismiss()
                            }
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Tidak dapat terhubung ke internet", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun pickImageForProfilePicture() {
        cropImage.launch(
            options {
                setToolbarColor(Color.BLACK)
                setImageSource(includeGallery = true, includeCamera = false)
                setCropShape(CropImageView.CropShape.OVAL)
                setGuidelines(CropImageView.Guidelines.ON)
                setAspectRatio(1, 1)
                setFixAspectRatio(true)
                setOutputCompressFormat(Bitmap.CompressFormat.PNG)
            }
        )
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager: ConnectivityManager =
            this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo()!!
            .isConnected()
    }

    private fun startLoadingDialog(textDesc: String) : AlertDialog {
        val dialog: AlertDialog
        val builder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_loading, null)

        builder.setView(dialogView)

        dialog = builder.create()
        dialog.window?.decorView?.setBackgroundResource(R.drawable.bg_dialog_border)
        dialog.window?.setLayout(600, WindowManager.LayoutParams.WRAP_CONTENT)
        dialog.window?.setGravity(Gravity.CENTER)
        dialog.setCanceledOnTouchOutside(false)

        val textLoading = dialogView.findViewById<TextView>(R.id.text_desc_cpi)
        textLoading.text = textDesc
        dialog.show()

        return dialog
    }

    private fun showPickDialog(photo: Uri)  {
        val dialog: AlertDialog
        val builder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_pick_photo, null)

        builder.setView(dialogView)

        dialog = builder.create()
        dialog.window?.decorView?.setBackgroundResource(R.drawable.bg_dialog_border)
        dialog.window?.setLayout(800, WindowManager.LayoutParams.WRAP_CONTENT)
        dialog.window?.setGravity(Gravity.CENTER)
        dialog.setCanceledOnTouchOutside(false)

        val cancelButton = dialogView.findViewById<TextView>(R.id.cancel_button)
        cancelButton.setOnClickListener{
            dialog.dismiss()
        }
        val okButton = dialogView.findViewById<TextView>(R.id.ok_button)
        okButton.setOnClickListener{
            dialog.dismiss()
            changeProfilePicture(photo)
        }
        val imageView = dialogView.findViewById<ImageView>(R.id.image_temp)

        Glide.with(dialogView)
            .load(photo)
            .into(imageView)

        dialog.show()
    }

    private fun changeProfilePicture(photo: Uri) {
        if (isNetworkAvailable()) {
            val dialogLoading = startLoadingDialog("Simpan foto...")
            editProfileViewModel.photoFlow.value = photo
            editProfileViewModel.saveNewProfile().observe(this) { result ->
                if (result != null) {
                    when (result) {
                        is Resource.Loading -> {}
                        is Resource.Success -> {
                            Toast.makeText(this, "Foto profil diperbarui", Toast.LENGTH_SHORT).show()
                            dialogLoading.dismiss()
                            finish()
                        }
                        is Resource.Error -> {
                            Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                            dialogLoading.dismiss()
                        }
                    }
                }
            }
        } else {
            Toast.makeText(this, "Tidak dapat terhubung ke internet", Toast.LENGTH_SHORT).show()
        }
    }

    private fun dismissKeyboard() {
        val imm =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(this.currentFocus?.windowToken, 0)
    }
}