package com.example.sihati_client.pages.mainPage.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.sihati_client.R
import com.example.sihati_client.database.User
import com.example.sihati_client.databinding.FragmentHealthStatusBinding
import com.example.sihati_client.viewModels.AuthViewModel
import com.example.sihati_client.viewModels.ScheduleViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class HealthStatusFragment : Fragment() {

    private lateinit var binding: FragmentHealthStatusBinding
    private lateinit var authViewModel: AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHealthStatusBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authViewModel = ViewModelProvider(
            this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        )[AuthViewModel::class.java]
        //this button is temporary used to logout
        binding.settings.setOnClickListener {
            authViewModel.signOut(requireActivity())
        }


        val mainViewModel = ViewModelProvider(
            this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        )[ScheduleViewModel::class.java]
        mainViewModel.init()
        mainViewModel.profile?.observe(requireActivity()){
            user -> UpdateViews(user)
        }
    }

    private fun UpdateViews(user: User?) {
        binding.name.text = user?.name
        binding.id.text = user?.id
        binding.status.text = user?.status
        when(user?.status){
            "Positive" -> Glide.with(this).load(R.drawable.logo_red).into(binding.logo)
            "Negative" -> Glide.with(this).load(R.drawable.logo_green).into(binding.logo)
            "Pending" -> Glide.with(this).load(R.drawable.logo_grey).into(binding.logo)
            "Not Tested" -> Glide.with(this).load(R.drawable.logo_yellow).into(binding.logo)
        }
    }

}