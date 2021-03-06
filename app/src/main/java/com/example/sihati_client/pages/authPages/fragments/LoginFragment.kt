package com.example.sihati_client.pages.authPages.fragments

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.sihati_client.R
import com.example.sihati_client.databinding.FragmentLoginBinding
import com.example.sihati_client.pages.mainPage.MainActivity
import com.example.sihati_client.viewModels.AuthViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth


class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var viewModel: AuthViewModel
    private lateinit var progressDialog: ProgressDialog
    private var email=""
    private var firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Check if user is signed in (non-null) and update UI accordingly.
        viewModel = ViewModelProvider(
            this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        )[AuthViewModel::class.java]

        progressDialog = ProgressDialog(requireContext())
        progressDialog.setTitle("S'il vous plaît, attendez...")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.login.setOnClickListener {
            if(binding.email.text.toString().isNotEmpty()
                &&binding.password.text.toString().isNotEmpty()){
                viewModel.signIn(binding.email.text.toString(),
                    binding.password.text.toString(),requireActivity())
            }else Toast.makeText(requireActivity(),"please fill your email and password", Toast.LENGTH_LONG).show()
        }

        binding.forgetPassword.setOnClickListener {
            setupEmailDialog()
        }
    }

    @SuppressLint("InflateParams")
    private fun setupEmailDialog(){
        val dialog = BottomSheetDialog(requireContext(),R.style.BottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.email_bottom_sheet_content, null)
        view.findViewById<Button>(R.id.send).setOnClickListener {
            email = view.findViewById<TextInputEditText>(R.id.email).text.toString().trim()

            if(email.isEmpty()){
                Toast.makeText(requireContext(),"Entrez un Email...",Toast.LENGTH_LONG).show()
            }
            else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                Toast.makeText(requireContext(),"Modèle d'e-mail invalide...",Toast.LENGTH_LONG).show()
            }
            else{
                recoverPassword(dialog)
            }
        }
        dialog.setContentView(view)
        dialog.show()
    }

    private fun recoverPassword(dialog: BottomSheetDialog) {
        progressDialog.setMessage("Envoi de l'instruction de réinitialisation du mot de passe à $email")
        progressDialog.show()
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(requireContext(),"Instructions envoyées à \n$email",Toast.LENGTH_LONG).show()
                dialog.dismiss()
            }
            .addOnFailureListener { e->
                progressDialog.dismiss()
                Toast.makeText(requireContext(),"Échec de l'envoi en raison de ${e.message}",Toast.LENGTH_LONG).show()
            }
    }
}