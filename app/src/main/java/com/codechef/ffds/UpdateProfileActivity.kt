package com.codechef.ffds

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.codechef.ffds.databinding.UpdateProfileActivityBinding
import java.io.*
import java.util.*
import kotlin.collections.ArrayList


class UpdateProfileActivity : AppCompatActivity() {

    private lateinit var binding: UpdateProfileActivityBinding
    private val viewModel =
        ViewModelProvider(this, UserViewModelFactory(application)).get(UserViewModel::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = UpdateProfileActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setDefaultData()

        binding.apply {
            uploadDp.setOnClickListener {
                val gallery = Intent()
                gallery.type = "image/*"
                gallery.action = Intent.ACTION_GET_CONTENT
                val resultLauncher =
                    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

                        if (result.resultCode == Activity.RESULT_OK) {
                            val data = result.data
                            val imageURI: Uri = data?.data!!
                            val bitmap = if (android.os.Build.VERSION.SDK_INT >= 29)
                                ImageDecoder.decodeBitmap(
                                    ImageDecoder.createSource(
                                        contentResolver,
                                        imageURI
                                    )
                                )
                            else
                                MediaStore.Images.Media.getBitmap(contentResolver, imageURI)
                            binding.dp.setImageBitmap(bitmap)
                            val path = saveToInternalStorage(bitmap)
                            viewModel.update(Profile(imagePath = path!!))
                        }

                    }
                resultLauncher.launch(Intent.createChooser(gallery, "Select profile photo"))
            }

            val tags = ArrayList<String>()
            Collections.addAll(tags, *tagView.tags)
            add.setOnClickListener {
                handleTags(tags)
            }

            tagView.setOnTagClickListener { _, tag, _ ->
                tags.remove(tag)
                viewModel.update(Profile(expectations = tags))
                tagView.setTags(tags)
            }

            uploadTimeTable.setOnClickListener {
                startActivity(Intent(this@UpdateProfileActivity, TimeTable::class.java))
            }

            saveProfile.setOnClickListener {

                val user = Profile(
                    bio = bio.text.toString().trim(),
                    name = yourName.text.toString().trim(),
                    phone = phoneNoEdit.text.toString()
                )
                viewModel.update(user)

                startActivity(Intent(baseContext, MainActivity::class.java))
                finish()
            }
        }
    }

    private fun setDefaultData() {

        binding.apply {

            var user = viewModel.getUserData()

            bio.setText(user.bio)
            yourName.setText(user.name)
            phoneNoEdit.text = user.phone
            tagView.setTags(user.expectations)

            viewModel.update(user)
            try {
                user = viewModel.getUserData()
                dp.setImageBitmap(loadImageFromStorage(user.imagePath))
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        }
    }

    private fun handleTags(tags: ArrayList<String>) {
        binding.apply {
            val tag = addTags.text.toString().trim()
            if (tag.isNotEmpty()) {
                if (!tags.contains(tag)) {
                    tags.add(tag)
                    viewModel.update(Profile(expectations = tags))
                } else
                    Toast.makeText(
                        this@UpdateProfileActivity,
                        "Tag already present",
                        Toast.LENGTH_SHORT
                    ).show()
            }
            tagView.setTags(tags)
            addTags.text = null
        }
    }

    private fun saveToInternalStorage(bitmapImage: Bitmap): String? {
        val cw = ContextWrapper(applicationContext)
        val directory: File = cw.getDir("FFDS", Context.MODE_PRIVATE)
        val myPath = File(directory, "profileImage.jpg")
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(myPath)
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                fos?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return directory.absolutePath
    }

    @Throws(FileNotFoundException::class)
    private fun loadImageFromStorage(path: String): Bitmap? {
        val f = File(path, "profileImage.jpg")
        return BitmapFactory.decodeStream(FileInputStream(f))
    }
}
