package com.davek.expirydatetracker.fragment

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.AdapterView
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.davek.expirydatetracker.R
import com.davek.expirydatetracker.adapter.ItemAdapter
import com.davek.expirydatetracker.database.FoodItem
import com.davek.expirydatetracker.databinding.FragmentItemListBinding
import com.davek.expirydatetracker.util.SwipeToDeleteCallback
import com.davek.expirydatetracker.util.getCalendarInstance
import com.davek.expirydatetracker.viewmodel.ItemListViewModel
import com.davek.expirydatetracker.viewmodel.ItemListViewModel.Companion.DEFAULT_ITEM_ID
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ItemListFragment : Fragment() {

    private lateinit var binding: FragmentItemListBinding
    private val itemListViewModel: ItemListViewModel by viewModels()
    private lateinit var itemAdapter: ItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_item_list, container, false
        )
        binding.itemListVM = itemListViewModel
        binding.lifecycleOwner = viewLifecycleOwner
        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeViewModels()
        setupRecyclerView()
        loadSort()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.item_list_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            R.id.menu_sort -> {
                showSortDialog()
                true
            }
            else -> false
        }

    private fun subscribeViewModels() {
        itemListViewModel.navigateToItemDetail.observe(viewLifecycleOwner, {
            findNavController().navigate(
                ItemListFragmentDirections.actionItemListFragmentToItemDetailFragment(
                    it, getString(
                        if (it == DEFAULT_ITEM_ID) R.string.add_item_title
                        else R.string.edit_item_title
                    )
                )
            )
        })

        itemListViewModel.showSnackbar.observe(viewLifecycleOwner, {
            Snackbar.make(
                binding.root,
                getString(R.string.item_deleted_snackbar_message),
                Snackbar.LENGTH_SHORT
            ).show()
        })
    }

    private fun setupRecyclerView() {
        val viewModel = binding.itemListVM
        if (viewModel != null) {
            itemAdapter = ItemAdapter(itemListViewModel, getCalendarInstance().time.time)
            binding.foodItemList.adapter = itemAdapter
            binding.foodItemList.addItemDecoration(
                DividerItemDecoration(
                    context,
                    DividerItemDecoration.VERTICAL
                )
            )

            val swipeHandler = object : SwipeToDeleteCallback(requireContext()) {
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    showDeleteFoodItemDialog(itemAdapter.getFoodItemByPosition(viewHolder.adapterPosition))
                }
            }
            val itemTouchHelper = ItemTouchHelper(swipeHandler)
            itemTouchHelper.attachToRecyclerView(binding.foodItemList)
        } else {
            Log.e("TAG", "ItemListViewModel not initialized when attempting to set up ItemAdapter")
        }
    }

    private fun showDeleteFoodItemDialog(foodItem: FoodItem) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.delete_food_item_confirm_dialog_title))
            .setMessage(
                String.format(
                    getString(R.string.delete_food_item_confirm_dialog_message),
                    foodItem.name
                )
            )
            .setPositiveButton(getString(R.string.delete_food_item_confirm_dialog_positive_button)) { dialog, _ ->
                itemListViewModel.deleteFoodItem(requireContext(), foodItem.itemId)
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.dialog_negative_button)) { dialog, _ ->
                dialog.dismiss()
            }
            .setOnDismissListener {
                itemAdapter.notifyDataSetChanged()
            }
            .create()
            .show()
    }

    private fun showSortDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.sort_dialog_title))
            .setPositiveButton(getString(R.string.menu_sort)) { dialog: DialogInterface, _: Int ->
                val checkedItemPosition = (dialog as AlertDialog).listView.checkedItemPosition
                if (checkedItemPosition != AdapterView.INVALID_POSITION) {
                    itemListViewModel.setSortType(checkedItemPosition)
                    saveSort(checkedItemPosition)
                }
            }
            .setNegativeButton(getString(R.string.dialog_negative_button), null)
            .setSingleChoiceItems(
                R.array.sort_by_options,
                itemListViewModel.sortType.value?.value ?: 0,
                null
            )
            .show()
    }

    private fun saveSort(checkedItemPosition: Int) {
        val sharedPreferences = activity?.getSharedPreferences(PREF_SORT, Context.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()
        editor?.putInt(PREF_SORT, checkedItemPosition)?.apply()
    }

    private fun loadSort() {
        val sharedPreferences = activity?.getSharedPreferences(PREF_SORT, Context.MODE_PRIVATE)
        val savedSort = sharedPreferences?.getInt(PREF_SORT, 0)
        savedSort?.let {
            itemListViewModel.setSortType(it)
        }
    }

    companion object {
        private const val PREF_SORT = "pref_sort"
    }
}