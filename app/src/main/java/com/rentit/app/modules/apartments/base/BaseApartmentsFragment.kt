package com.rentit.app.modules.apartments.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rentit.app.databinding.FragmentApartmentsBinding
import com.rentit.app.models.apartment.ApartmentModel
import com.rentit.app.modules.apartments.ApartmentsViewModel
import com.rentit.app.modules.apartments.adapter.ApartmentsRecyclerAdapter
import com.rentit.app.modules.apartments.adapter.OnItemClickListener
import kotlinx.coroutines.launch

abstract class BaseApartmentsFragment : Fragment() {
    private lateinit var binding: FragmentApartmentsBinding
    private lateinit var recyclerView: RecyclerView
    protected lateinit var viewModel: ApartmentsViewModel
    protected lateinit var adapter: ApartmentsRecyclerAdapter
    protected lateinit var progressBar: ProgressBar

    protected open suspend fun preparations() = Unit
    protected abstract fun setupApartmentsAdapter(): ApartmentsRecyclerAdapter
    protected abstract fun observeApartments()
    protected abstract fun setupApartmentsAdapterListener(): OnItemClickListener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentApartmentsBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[ApartmentsViewModel::class.java]

        progressBar = binding.progressBar

        lifecycleScope.launch {
            preparations()
            viewModel.setAllApartments()

            recyclerView = binding.rvApartmentsFragmentList
            recyclerView.setHasFixedSize(true)
            recyclerView.layoutManager = LinearLayoutManager(context)
            adapter = setupApartmentsAdapter()
            adapter.listener = setupApartmentsAdapterListener()
            recyclerView.adapter = adapter

            observeApartments()
        }

        binding.pullToRefresh.setOnRefreshListener {
            reloadData()
        }

        ApartmentModel.instance.apartmentsListLoadingState.observe(viewLifecycleOwner) { state ->
            binding.pullToRefresh.isRefreshing = state == ApartmentModel.LoadingState.LOADING
        }

        return binding.root
    }

    private fun reloadData() {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            viewModel.refreshAllApartments()
        }
        progressBar.visibility = View.GONE
    }
}