package com.example.sharephoto.view

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.sharephoto.R
import com.example.sharephoto.databinding.FragmentYuklemeBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import java.util.UUID


@Suppress("DEPRECATION")
class YuklemeFragment : Fragment() {
    private var _binding: FragmentYuklemeBinding? = null
    private val binding get() = _binding!!
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private var selectedImg: Uri? = null
    private var selectedImgBitmap: Bitmap? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var db : FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerLaunchers()
        auth = Firebase.auth
        storage = Firebase.storage
        db = Firebase.firestore
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentYuklemeBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.paylasButton.setOnClickListener {
            PaylasbuttonTikla(it)

        }
        binding.imageView2.setOnClickListener {
            gorselSec(it)
        }

    }

    private fun gorselSec(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            //read media img
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(),
                        Manifest.permission.READ_MEDIA_IMAGES
                    )
                ) {
                    //izin mantığını kullanıcıya göster
                    Snackbar.make(view, "İzin Gerekli", Snackbar.LENGTH_INDEFINITE)
                        .setAction("İzin Ver", View.OnClickListener {
                            //izin iste
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                        }).show()
                } else {
                    //izin istememiz lazım
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
            } else {
                //izin var
                //galeriye git kodu
                val intentToGaller =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGaller)
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(),
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                ) {
                    //izin mantığını kullanıcıya göster
                    Snackbar.make(view, "İzin Gerekli", Snackbar.LENGTH_INDEFINITE)
                        .setAction("İzin Ver", View.OnClickListener {
                            //izin iste
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }).show()
                } else {
                    //izin istememiz lazım
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            } else {
                //izin var
                //galeriye git kodu
                val intentToGaller =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGaller)


            }
        }
    }
    fun PaylasbuttonTikla(view: View) {

        val uuid = UUID.randomUUID()
        val gorselAdi = "${uuid}.jpg"


        val referance = storage.reference
        val gorselReferans = referance.child("images").child(gorselAdi)
        if (selectedImg != null) {
            gorselReferans.putFile(selectedImg!!).addOnSuccessListener {
                //url alma işlemi
                gorselReferans.downloadUrl.addOnSuccessListener {
                    if (auth.currentUser != null) {
                        val downloadURL = it.toString()
                        //veri tabanına kaydetmeliyiz
                        val postMap = hashMapOf<String, Any>()
                        postMap.put("downloadURL", downloadURL)
                        postMap.put("kullaniciemail", auth.currentUser!!.email.toString())
                        postMap.put("kullaniciyorum", binding.commentText.text.toString())
                        postMap.put("date", com.google.firebase.Timestamp.now())

                        db.collection("Post").add(postMap).addOnSuccessListener {
                            //veri database'e yüklenmiş olur
                            findNavController().navigate(R.id.paylastanFeedGecis)
                        }.addOnFailureListener {
                            Toast.makeText(requireContext(), it.localizedMessage, Toast.LENGTH_SHORT)
                                .show()
                        }

                    }
                }
            }.addOnFailureListener{
                Toast.makeText(requireContext(), it.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }


            private fun registerLaunchers() {
                activityResultLauncher =
                    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                        if (result.resultCode == RESULT_OK) {
                            val intentFromResult = result.data
                            if (intentFromResult != null) {
                                selectedImg = intentFromResult.data
                                try {
                                    if (Build.VERSION.SDK_INT >= 28) {
                                        val source = ImageDecoder.createSource(
                                            requireActivity().contentResolver,
                                            selectedImg!!
                                        )
                                        selectedImgBitmap = ImageDecoder.decodeBitmap(source)
                                        binding.imageView2.setImageBitmap(selectedImgBitmap)
                                    } else {
                                        selectedImgBitmap = MediaStore.Images.Media.getBitmap(
                                            requireActivity().contentResolver,
                                            selectedImg
                                        )
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
                permissionLauncher =
                    registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                        if (it) {
                            //izin var
                            //galeriye git kodu
                            val intentToGaller =
                                Intent(
                                    Intent.ACTION_PICK,
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                                )
                            activityResultLauncher.launch(intentToGaller)
                        } else {
                            //izin yok
                            Toast.makeText(requireContext(), "İzin Gerekli", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }




                fun onDestroyView() {
                    super.onDestroyView()
                    _binding = null
                }
            }
        }



