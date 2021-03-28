package com.davek.expirydatetracker.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.davek.expirydatetracker.database.DataResult
import com.davek.expirydatetracker.database.FoodItem
import com.davek.expirydatetracker.database.FoodItemRepository
import com.davek.expirydatetracker.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ItemListViewModel @Inject constructor(
    private val foodItemRepository: FoodItemRepository
) : ViewModel() {

    private var currentDate: Date

    private val _showSnackbar = SingleLiveEvent<Any>()
    val showSnackbar: LiveData<Any>
        get() = _showSnackbar

    private val _navigateToItemDetail = SingleLiveEvent<Long>()
    val navigateToItemDetail: LiveData<Long>
        get() = _navigateToItemDetail

    private val _sortType = MutableLiveData<SortType>()
    val sortType: LiveData<SortType>
        get() = _sortType

    private val _sortedFoodItems: LiveData<List<FoodItem>> =
        Transformations.switchMap(sortType) { sort ->
            Transformations.switchMap(foodItemRepository.observeSortedFoodItems(sort)) { result ->
                val items = MutableLiveData<List<FoodItem>>()
                if (result is DataResult.Success) {
                    items.value = result.data
                } else {
                    items.value = emptyList()
                    Log.e("TAG", "Error while loading food items")
                }
                items
            }
        }
    val foodItems: LiveData<List<FoodItem>>
        get() = _sortedFoodItems

    init {
        _sortType.value = DEFAULT_SORT_TYPE
        currentDate = getCalendarInstance().time
    }

    val expireSoonCount: LiveData<Int> = Transformations.map(foodItems) {
        var count = 0
        for (item in it) {
            if (getRemainingDays(item.expiryDate.time, currentDate.time) in 0..7) count++
        }
        count
    }

    val expiredCount: LiveData<Int> = Transformations.map(foodItems) {
        var count = 0
        for (item in it) {
            if (getRemainingDays(item.expiryDate.time, currentDate.time) < 0) count++
        }
        count
    }

    fun onAddButtonClick() {
        _navigateToItemDetail.value = DEFAULT_ITEM_ID
    }

    fun onFoodItemClick(foodItemId: Long) {
        _navigateToItemDetail.value = foodItemId
    }

    fun setSortType(type: Int) {
        _sortType.value = when (type) {
            SortType.REMAINING_DAYS_ASC.value -> SortType.REMAINING_DAYS_ASC
            SortType.REMAINING_DAYS_DESC.value -> SortType.REMAINING_DAYS_DESC
            SortType.QUANTITY_ASC.value -> SortType.QUANTITY_ASC
            SortType.QUANTITY_DESC.value -> SortType.QUANTITY_DESC
            SortType.NAME_ASC.value -> SortType.NAME_ASC
            SortType.NAME_DESC.value -> SortType.NAME_DESC
            else -> SortType.REMAINING_DAYS_ASC
        }
    }

    fun deleteFoodItem(context: Context, foodItemId: Long) = viewModelScope.launch {
        foodItemRepository.deleteFoodItemById(foodItemId)
        cancelAlarm(context, foodItemId.toInt())
        _showSnackbar.call()
    }

    companion object {
        const val DEFAULT_ITEM_ID = -1L
        val DEFAULT_SORT_TYPE = SortType.REMAINING_DAYS_ASC
    }
}