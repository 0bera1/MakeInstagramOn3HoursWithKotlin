package com.example.sharephoto.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sharephoto.R
import com.example.sharephoto.adapter.PostAdapter
import com.example.sharephoto.data.Post
import com.example.sharephoto.databinding.FragmentFeedBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore


class FeedFragment : Fragment() ,PopupMenu.OnMenuItemClickListener {
    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!
    private lateinit var popup: PopupMenu
    private lateinit var auth: FirebaseAuth
    private lateinit var db : FirebaseFirestore
    val postList : ArrayList<Post> = arrayListOf()
    private var adapter : PostAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        db = Firebase.firestore
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
    _binding = FragmentFeedBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fab.setOnClickListener {
            clickOnFab(it)
        }
        FireStoreVeriAl()
        adapter = PostAdapter(postList)
        binding.feedRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.feedRecyclerView.adapter = adapter
    }
    private fun clickOnFab (view: View){
        popup = PopupMenu(requireContext(), view)
        val inflater = popup.menuInflater
        inflater.inflate(R.menu.popup_menu, popup.menu)
        popup.setOnMenuItemClickListener(this)
        popup.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    @SuppressLint("NotifyDataSetChanged")
    private fun FireStoreVeriAl(){
        db.collection("Post").orderBy("date",Query.Direction.DESCENDING).addSnapshotListener { value, error ->
            if (error != null) {
                Toast.makeText(requireContext(),error.localizedMessage,Toast.LENGTH_LONG).show()
            }else{
                if (value != null){
                    if (!value.isEmpty){
                        postList.clear()
                        //boş değilse
                        val documents = value.documents
                        for (document in documents){
                            //val kullaniciyorum = document.get("kullaniciyorum") as String //casting
                            //println(kullaniciyorum)
                            val kullaniciemail = document.get("kullaniciemail") as String
                            val downloadURL = document.get("downloadURL") as String
                            val kullaniciyorum = document.get("kullaniciyorum") as String

                            val post = Post(kullaniciemail,kullaniciyorum,downloadURL)
                            postList.add(post)
                        }
                        adapter?.notifyDataSetChanged()
                    }
                }

            }

        }
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.yuklemeItem){
            findNavController().navigate(R.id.yuklemeFragment)
        }else if (item?.itemId == R.id.cikisItem){
            auth.signOut()
            findNavController().navigate(R.id.kullaniciFragment)
        }
        return true
    }

}