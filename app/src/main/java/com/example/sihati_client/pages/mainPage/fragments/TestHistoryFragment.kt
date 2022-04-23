package com.example.sihati_client.pages.mainPage.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sihati_client.R
import com.example.sihati_client.adapters.ScheduleAdapter
import com.example.sihati_client.adapters.TestAdapter
import com.example.sihati_client.databinding.FragmentSchedulesBinding
import com.example.sihati_client.databinding.FragmentTestHistoryBinding
import com.example.sihati_client.viewModels.ScheduleViewModel
import com.example.sihati_client.viewModels.TestViewModel

class TestHistoryFragment : Fragment() {

    private lateinit var binding: FragmentTestHistoryBinding
    lateinit var scheduleViewModel: ScheduleViewModel
    lateinit var testViewModel: TestViewModel
    private lateinit var testAdapter :TestAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentTestHistoryBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        scheduleViewModel = ViewModelProvider(
            this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        )[ScheduleViewModel::class.java]

        testViewModel = ViewModelProvider(
            this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        )[TestViewModel::class.java]

        testViewModel.init()

        recyclerViewSetup()
    }

    private fun recyclerViewSetup() {
        // on below line we are setting layout
        // manager to our recycler view.
        binding.recyclerView.layoutManager = LinearLayoutManager(activity)
        // on below line we are initializing our adapter class.
        testAdapter = TestAdapter(requireActivity(),scheduleViewModel)

        // on below line we are setting
        // adapter to our recycler view.
        binding.recyclerView.adapter = testAdapter
        binding.recyclerView.setHasFixedSize(true)

        testViewModel.testsReady?.observe(requireActivity()){ list ->
            list?.let {
                // on below line we are updating our list.
                testAdapter.updateList(it)
            }

        }
    }
}