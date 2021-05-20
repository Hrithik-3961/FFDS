package com.codechef.ffds

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.aotasoft.taggroup.TagGroup
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
        setContentView(R.layout.update_profile_activity)

        val tinyDB = TinyDB(this)

        binding = UpdateProfileActivityBinding.inflate(layoutInflater)

        setDefaultData(tinyDB)

        binding.apply {
            uploadDp.setOnClickListener {
                val gallery: Intent = Intent()
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
                Log.d("tag123", "inside add on click")
                handleTags(tags, tinyDB)
            }

            tagView.setOnTagClickListener(object : TagGroup.OnTagClickListener {

                /* override fun onTagCrossClick(position: Int) {
                     val tag = tagView.getTagText(position)
                     tags.remove(tag)
                     tinyDB.putListString("Expectations", tags as ArrayList<String>?)
                     tagView.tags = tags
                 }*/

                override fun onTagClick(tagGroup: TagGroup?, tag: String?, position: Int) {

                }

            })

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
            val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageURI)
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
            Log.d("tag0", tag)
            if (tag.isNotEmpty()) {
                tags.add(tag)
                Log.d("tag", tags.toString())
                tinyDB.putListString("Expectations", tags as ArrayList<String>?)
                tagView.tag = tag
            }
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
