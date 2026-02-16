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

/**
 * BaseApartmentsFragment
 * 
 * Abstract base class that provides common functionality for all apartment list fragments.
 * Implements Template Method pattern to allow subclasses to customize specific behaviors
 * while maintaining consistent overall structure.
 * 
 * Subclasses must implement:
 * - setupApartmentsAdapter(): Define which apartments to display
 * - observeApartments(): Define how to respond to data changes
 * - setupApartmentsAdapterListener(): Define click behavior
 */
abstract class BaseApartmentsFragment : Fragment() {
    private lateinit var binding: FragmentApartmentsBinding
    private lateinit var recyclerView: RecyclerView
    protected lateinit var viewModel: ApartmentsViewModel
    protected lateinit var adapter: ApartmentsRecyclerAdapter
    protected lateinit var progressBar: ProgressBar
    
    // Optional hook for pre-loading preparations (fetch user data)
    protected open suspend fun preparations() = Unit
    
    // Must return configured adapter with appropriate apartment list
    protected abstract fun setupApartmentsAdapter(): ApartmentsRecyclerAdapter
    
    // Must setup LiveData observation and adapter updates
    protected abstract fun observeApartments()
    
    // Must return click listener for apartment items
    protected abstract fun setupApartmentsAdapterListener(): OnItemClickListener

    /**
     * Creates and initializes the fragment's view.
     * Sets up RecyclerView, ViewModel, loading states, and pull-to-refresh.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentApartmentsBinding.inflate(inflater, container, false) //Connects Kotlin code to the XML layout.
        viewModel = ViewModelProvider(this)[ApartmentsViewModel::class.java] // Connects the Fragment to its data source.

        progressBar = binding.progressBar
        
        // Show loading spinner initially
        progressBar.visibility = View.VISIBLE
        binding.pullToRefresh.visibility = View.GONE

        // Set up RecyclerView immediately to avoid "No adapter attached" warning
        recyclerView = binding.rvApartmentsFragmentList
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)

        lifecycleScope.launch {
            preparations()
            viewModel.setAllApartments()

            // Set up adapter after data is ready
            adapter = setupApartmentsAdapter()
            adapter.listener = setupApartmentsAdapterListener()
            recyclerView.adapter = adapter

            observeApartments()
        }

        binding.pullToRefresh.setOnRefreshListener {
            reloadData()
        }

        // Observe loading state to show/hide spinner
        ApartmentModel.instance.apartmentsListLoadingState.observe(viewLifecycleOwner) { state ->
            when (state) {
                ApartmentModel.LoadingState.LOADING -> {
                    progressBar.visibility = View.VISIBLE
                    binding.pullToRefresh.visibility = View.GONE
                }
                ApartmentModel.LoadingState.LOADED, ApartmentModel.LoadingState.ERROR -> {
                    progressBar.visibility = View.GONE
                    binding.pullToRefresh.visibility = View.VISIBLE
                }
            }
            binding.pullToRefresh.isRefreshing = state == ApartmentModel.LoadingState.LOADING
        }

        return binding.root
    }

    // reloads apartment data from remote source.
    private fun reloadData() {
        lifecycleScope.launch {
            viewModel.refreshAllApartments()
        }
    }
}