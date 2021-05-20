package com.codechef.ffds

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
import androidx.appcompat.app.AppCompatActivity
import com.codechef.ffds.databinding.UpdateProfileActivityBinding
import java.io.*
import java.util.*
import kotlin.collections.ArrayList


class UpdateProfileActivity : AppCompatActivity() {

    companion object {
        const val PICK_IMAGE = 1
    }

    private lateinit var binding: UpdateProfileActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = UpdateProfileActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val tinyDB = TinyDB(this)

        setDefaultData(tinyDB)

        binding.apply {
            uploadDp.setOnClickListener {
                val gallery = Intent()
                gallery.type = "image/*"
                gallery.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(
                    Intent.createChooser(gallery, "Select profile photo"),
                    PICK_IMAGE
                )
            }

            val tags = ArrayList<String>()
            Collections.addAll(tags, *tagView.tags)
            add.setOnClickListener {
                handleTags(tags, tinyDB)
            }

            tagView.setOnTagClickListener { _, tag, _ ->
                tags.remove(tag)
                tinyDB.putListString("Expectations", tags as ArrayList<String>?)
                tagView.setTags(tags)
            }

            uploadTimeTable.setOnClickListener {
                startActivity(Intent(this@UpdateProfileActivity, TimeTable::class.java))
            }

            saveProfile.setOnClickListener {
                tinyDB.putString("Bio", bio.text.toString().trim())
                tinyDB.putString("Name", yourName.text.toString().trim())
                tinyDB.putString("PhoneNo", phoneNoEdit.text.toString())
                startActivity(Intent(baseContext, MainActivity::class.java))
                finish()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            val imageURI: Uri = data?.data!!
            val bitmap = if (android.os.Build.VERSION.SDK_INT >= 29)
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, imageURI))
            else
                MediaStore.Images.Media.getBitmap(contentResolver, imageURI)
            binding.dp.setImageBitmap(bitmap)
            val path = saveToInternalStorage(bitmap)
            val tinyDB = TinyDB(this)
            tinyDB.putString("ImagePath", path)
        }
    }

    private fun setDefaultData(tinyDB: TinyDB) {

        binding.apply {
            bio.setText(tinyDB.getString("Bio"))
            yourName.setText(tinyDB.getString("Name"))
            phoneNoEdit.text = tinyDB.getString("PhoneNo")
            tagView.setTags(tinyDB.getListString("Expectations"))
            try {
                dp.setImageBitmap(loadImageFromStorage(tinyDB.getString("ImagePath")))
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        }
    }

    private fun handleTags(tags: ArrayList<String>, tinyDB: TinyDB) {
        binding.apply {
            val tag = addTags.text.toString().trim()
            if (tag.isNotEmpty()) {
                if (!tags.contains(tag)) {
                    tags.add(tag)
                    tinyDB.putListString("Expectations", tags as ArrayList<String>?)
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
