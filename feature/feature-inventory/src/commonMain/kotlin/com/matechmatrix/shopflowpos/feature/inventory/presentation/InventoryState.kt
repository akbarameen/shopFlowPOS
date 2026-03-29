package com.matechmatrix.shopflowpos.feature.inventory.presentation

import com.matechmatrix.shopflowpos.core.model.Product
import com.matechmatrix.shopflowpos.core.model.enums.ProductCategory
import com.matechmatrix.shopflowpos.core.model.enums.ProductCondition

data class InventoryState(
    val isLoading: Boolean = true,
    val products: List<Product> = emptyList(),
    val filteredProducts: List<Product> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: ProductCategory? = null,
    val showAddDialog: Boolean = false,
    val editingProduct: Product? = null,
    val showDeleteConfirm: String? = null,   // productId
    val showStockDialog: String? = null,     // productId
    val showCostPrice: Boolean = false,
    val totalInventoryValue: Double = 0.0,
    val totalInventorySellingValue: Double = 0.0,
    val currencySymbol: String = "Rs.",
    val error: String? = null,
    val successMessage: String? = null,
    // Form fields for add/edit
    val formName: String = "",
    val formBrand: String = "",
    val formModel: String = "",
    val formImei: String = "",
    val formPtaStatus: String = "NA",
    val formBarcode: String = "",
    val formCategory: ProductCategory = ProductCategory.PHONE,
    val formCondition: ProductCondition = ProductCondition.NEW,
    val formCostPrice: String = "",
    val formSalePrice: String = "",
    val formStock: String = "",
    val formLowStockAlert: String = "5",
    val formDescription: String = "",
    val formError: String? = null
) {
    val totalValue get() = products.sumOf { it.costPrice * it.stock }
    val totalSellingValue get() = products.sumOf { it.sellingPrice * it.stock }
}

sealed class InventoryIntent {
    data object Load : InventoryIntent()
    data class Search(val query: String) : InventoryIntent()
    data class FilterByCategory(val category: ProductCategory?) : InventoryIntent()
    data object ShowAddDialog : InventoryIntent()
    data class ShowEditDialog(val product: Product) : InventoryIntent()
    data object DismissDialog : InventoryIntent()
    data object SaveProduct : InventoryIntent()
    data class ConfirmDelete(val productId: String) : InventoryIntent()
    data object DeleteProduct : InventoryIntent()
    data class ShowStockDialog(val productId: String) : InventoryIntent()
    data class UpdateStock(val productId: String, val newStock: Int) : InventoryIntent()
    data object DismissError : InventoryIntent()
    // Form field updates
    data class FormName(val v: String) : InventoryIntent()
    data class FormBrand(val v: String) : InventoryIntent()
    data class FormModel(val v: String) : InventoryIntent()
    data class FormImei(val v: String) : InventoryIntent()
    data class FormPtaStatus(val v: String) : InventoryIntent()
    data class FormBarcode(val v: String) : InventoryIntent()
    data class FormCategory(val v: ProductCategory) : InventoryIntent()
    data class FormCondition(val v: ProductCondition) : InventoryIntent()
    data class FormCostPrice(val v: String) : InventoryIntent()
    data class FormSalePrice(val v: String) : InventoryIntent()
    data class FormStock(val v: String) : InventoryIntent()
    data class FormLowStockAlert(val v: String) : InventoryIntent()
    data class FormDescription(val v: String) : InventoryIntent()
}

sealed class InventoryEffect {
    data class ShowToast(val message: String) : InventoryEffect()
}
