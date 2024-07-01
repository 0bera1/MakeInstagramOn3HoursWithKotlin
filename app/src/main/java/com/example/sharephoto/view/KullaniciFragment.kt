package com.example.sharephoto.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.sharephoto.R
import com.example.sharephoto.databinding.FragmentKullaniciBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth


class KullaniciFragment : Fragment() {
    private var _binding: FragmentKullaniciBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentKullaniciBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.kayitButton.setOnClickListener {
            kayit(it)
        }
        binding.girisButton.setOnClickListener {
            giris(it)
        }
        val guncelKullanici = auth.currentUser
        if (guncelKullanici != null) {
            findNavController().navigate(R.id.feedGecis)
        }
        binding.unuttumTextView.setOnClickListener {
            val kullaniciemail = binding.eMailText.text.toString()
            if (kullaniciemail.isNotEmpty()){
                auth.sendPasswordResetEmail(kullaniciemail).addOnCompleteListener { task ->
                    //Şifre sıfırlama e postası başarıyla gönderildi
                    if (task.isSuccessful) {
                        Toast.makeText(requireContext(), "Şifre sıfırlama e-postası gönderildi", Toast.LENGTH_LONG).show()
                    } else {
                        //Hata durumunda işlemleri
                        Toast.makeText(requireContext(), task.exception?.localizedMessage, Toast.LENGTH_LONG).show()
                    }

                }
                } else {
                    Toast.makeText(requireContext(), "Lütfen e-posta adresinizi girin", Toast.LENGTH_LONG).show()
            }

        }
    }
    fun kayit(view: View) {
        val kullaniciemail = binding.eMailText.text.toString()
        val password = binding.sifreText.text.toString()
        if (kullaniciemail.isNotEmpty() && password.isNotEmpty()){
            auth.createUserWithEmailAndPassword(kullaniciemail, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Kullanıcı oluşturuldu
                    findNavController().navigate(R.id.feedGecis)
                }

            }.addOnFailureListener { exception ->
                // Hata durumunda işlemleri
                Toast.makeText(requireContext(), exception.localizedMessage, Toast.LENGTH_LONG).show()
            }
        }
    }
    fun giris(view: View) {
        val kullaniciemail = binding.eMailText.text.toString()
        val password = binding.sifreText.text.toString()
        if (kullaniciemail.isNotEmpty() && password.isNotEmpty()){
            auth.signInWithEmailAndPassword(kullaniciemail, password).addOnSuccessListener {
                    // Kullanıcı giriş yaptı
                    findNavController().navigate(R.id.feedGecis)
            }.addOnFailureListener { exception ->
                // Hata durumunda işlemleri
                Toast.makeText(requireContext(), exception.localizedMessage, Toast.LENGTH_LONG).show()
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}