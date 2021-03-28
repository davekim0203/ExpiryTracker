package com.davek.expirydatetracker.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.DatePicker
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.davek.expirydatetracker.R
import com.davek.expirydatetracker.databinding.FragmentItemDetailBinding
import com.davek.expirydatetracker.viewmodel.ItemDetailViewModel
import com.davek.expirydatetracker.viewmodel.ItemDetailViewModel.Companion.SNACKBAR_ID_ITEM_DELETED
import com.davek.expirydatetracker.viewmodel.ItemDetailViewModel.Companion.SNACKBAR_ID_NAME_REQUIRED
import com.davek.expirydatetracker.viewmodel.ItemDetailViewModel.Companion.SNACKBAR_ID_NOTIFICATION_ERROR
import com.davek.expirydatetracker.viewmodel.ItemListViewModel.Companion.DEFAULT_ITEM_ID
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class ItemDetailFragment : Fragment(), DatePickerDialog.OnDateSetListener {

    private lateinit var binding: FragmentItemDetailBinding
    private val itemDetailViewModel: ItemDetailViewModel by viewModels()
    private val args: ItemDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_item_detail, container, false
        )
        binding.itemDetailVM = itemDetailViewModel
        binding.lifecycleOwner = viewLifecycleOwner
        itemDetailViewModel.loadItem(args.foodItemId)
        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeViewModels()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.item_detail_fragment_menu, menu)
        if (args.foodItemId == DEFAULT_ITEM_ID) {
            menu.findItem(R.id.menu_delete).isVisible = false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            R.id.menu_save -> {
                itemDetailViewModel.onSaveButtonClick()
                true
            }
            R.id.menu_delete -> {
                showDeleteFoodItemDialog()
                true
            }
            else -> false
        }

    private fun subscribeViewModels() {
        itemDetailViewModel.navigateToItemList.observe(viewLifecycleOwner, {
            findNavController().navigateUp()
        })

        itemDetailViewModel.showDatePicker.observe(viewLifecycleOwner, {
            showDatePickerDialog(it)
        })

        itemDetailViewModel.showSnackbar.observe(viewLifecycleOwner, {
            when (it) {
                SNACKBAR_ID_ITEM_DELETED -> Snackbar.make(
                    binding.root,
                    getString(R.string.item_deleted_snackbar_message),
                    Snackbar.LENGTH_SHORT
                ).show()
                SNACKBAR_ID_NAME_REQUIRED -> Snackbar.make(
                    binding.root,
                    getString(R.string.name_required_snackbar_message),
                    Snackbar.LENGTH_SHORT
                ).show()
                SNACKBAR_ID_NOTIFICATION_ERROR -> Snackbar.make(
                    binding.root,
                    getString(R.string.notification_error_snackbar_message),
                    Snackbar.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun showDatePickerDialog(currentDate: Date) {
        val cal = Calendar.getInstance()
        cal.time = currentDate
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(requireContext(), this, year, month, day).show()
    }

    private fun showDeleteFoodItemDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.delete_food_item_confirm_dialog_title))
            .setMessage(getString(R.string.delete_food_item_confirm_dialog_message_detail_fragment))
            .setPositiveButton(getString(R.string.delete_food_item_confirm_dialog_positive_button)) { dialog, _ ->
                itemDetailViewModel.deleteFoodItem(requireContext())
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.dialog_negative_button)) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        itemDetailViewModel.onDateSelected(year, month, dayOfMonth)
    }
}