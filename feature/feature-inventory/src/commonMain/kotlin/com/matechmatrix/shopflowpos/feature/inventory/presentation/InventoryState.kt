package com.matechmatrix.shopflowpos.feature.inventory.presentation

import androidx.paging.PagingData
import com.matechmatrix.shopflowpos.core.model.Product
import com.matechmatrix.shopflowpos.core.model.enums.ProductCategory
import com.matechmatrix.shopflowpos.core.model.enums.ProductCondition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

// ─── State ────────────────────────────────────────────────────────────────────

data class InventoryState(
    // List
    val pagedProducts  : Flow<PagingData<Product>> = emptyFlow(),
    val searchQuery    : String        = "",
    val selectedCategory: ProductCategory? = null,
    val isLoading      : Boolean       = false,
    val error          : String?       = null,

    // Settings
    val showCostPrice  : Boolean       = false,
    val currencySymbol : String        = "Rs.",

    // Low-stock badge (loaded separately)
    val lowStockCount  : Int           = 0,

    // Sheet visibility
    val showProductSheet: Boolean      = false,
    val editingProduct  : Product?     = null,

    // Stock dialog
    val showStockDialog : String?      = null,  // productId

    // Delete confirm
    val showDeleteConfirm: String?     = null,  // productId

    // ── Form ──────────────────────────────────────────────────────────────────
    val formName         : String           = "",
    val formBrand        : String           = "",
    val formModel        : String           = "",
    val formImei         : String           = "",
    val formPtaStatus    : String           = "NA",
    val formBarcode      : String           = "",
    val formCategory     : ProductCategory  = ProductCategory.PHONE,
    val formCondition    : ProductCondition = ProductCondition.NEW,
    val formCostPrice    : String           = "",
    val formSalePrice    : String           = "",
    val formStock        : String           = "",
    val formLowStockAlert: String           = "3",
    val formDescription  : String           = "",
    // Specs
    val formColor        : String  = "",
    val formStorageGb    : String  = "",
    val formRamGb        : String  = "",
    val formRomGb        : String  = "",
    val formBatteryMah   : String  = "",
    val formScreenSize   : String  = "",
    val formProcessor    : String  = "",

    val formError        : String? = null,
    val isSaving         : Boolean = false,
)

// ─── Intent ───────────────────────────────────────────────────────────────────

sealed class InventoryIntent {
    data object Load                                         : InventoryIntent()
    data class  Search(val query: String)                    : InventoryIntent()
    data class  FilterByCategory(val category: ProductCategory?) : InventoryIntent()

    // Sheet
    data object ShowAddSheet                                 : InventoryIntent()
    data class  ShowEditSheet(val product: Product)          : InventoryIntent()
    data object DismissSheet                                 : InventoryIntent()
    data object SaveProduct                                  : InventoryIntent()

    // Stock
    data class  ShowStockDialog(val productId: String)       : InventoryIntent()
    data class  UpdateStock(val productId: String, val newStock: Int) : InventoryIntent()

    // Delete
    data class  ConfirmDelete(val productId: String)         : InventoryIntent()
    data object DeleteProduct                                : InventoryIntent()

    // Form fields
    data class FormName        (val v: String)          : InventoryIntent()
    data class FormBrand       (val v: String)          : InventoryIntent()
    data class FormModel       (val v: String)          : InventoryIntent()
    data class FormImei        (val v: String)          : InventoryIntent()
    data class FormPtaStatus   (val v: String)          : InventoryIntent()
    data class FormBarcode     (val v: String)          : InventoryIntent()
    data class FormCategory    (val v: ProductCategory) : InventoryIntent()
    data class FormCondition   (val v: ProductCondition): InventoryIntent()
    data class FormCostPrice   (val v: String)          : InventoryIntent()
    data class FormSalePrice   (val v: String)          : InventoryIntent()
    data class FormStock       (val v: String)          : InventoryIntent()
    data class FormLowStockAlert(val v: String)         : InventoryIntent()
    data class FormDescription (val v: String)          : InventoryIntent()
    data class FormColor       (val v: String)          : InventoryIntent()
    data class FormStorageGb   (val v: String)          : InventoryIntent()
    data class FormRamGb       (val v: String)          : InventoryIntent()
    data class FormRomGb       (val v: String)          : InventoryIntent()
    data class FormBatteryMah  (val v: String)          : InventoryIntent()
    data class FormScreenSize  (val v: String)          : InventoryIntent()
    data class FormProcessor   (val v: String)          : InventoryIntent()
}

// ─── Effect ───────────────────────────────────────────────────────────────────

sealed class InventoryEffect {
    data class ShowToast(val message: String) : InventoryEffect()
}