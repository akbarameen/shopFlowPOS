package com.matechmatrix.shopflowpos.feature.inventory.presentation

import com.matechmatrix.shopflowpos.core.common.base.MviViewModel
import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.common.util.IdGenerator
import com.matechmatrix.shopflowpos.core.model.Product
import com.matechmatrix.shopflowpos.core.model.enums.ProductCategory
import com.matechmatrix.shopflowpos.core.model.enums.ProductCondition
import com.matechmatrix.shopflowpos.feature.inventory.domain.repository.InventoryRepository
import kotlinx.datetime.Clock

class InventoryViewModel(
    private val repo: InventoryRepository
) : MviViewModel<InventoryState, InventoryIntent, InventoryEffect>(InventoryState()) {

    init { onIntent(InventoryIntent.Load) }

    override suspend fun handleIntent(intent: InventoryIntent) {
        when (intent) {
            InventoryIntent.Load -> loadProducts()

            is InventoryIntent.Search -> {
                setState { copy(searchQuery = intent.query) }
                applyFilter()
            }

            is InventoryIntent.FilterByCategory -> {
                setState { copy(selectedCategory = intent.category) }
                applyFilter()
            }

            InventoryIntent.ShowAddDialog -> setState {
                copy(
                    showAddDialog = true, editingProduct = null,
                    formName = "", formBrand = "", formModel = "", formImei = "",
                    formPtaStatus = "NA", formBarcode = "", formCategory = ProductCategory.PHONE,
                    formCondition = ProductCondition.NEW, formCostPrice = "", formSalePrice = "",
                    formStock = "", formLowStockAlert = "5", formDescription = "", formError = null
                )
            }

            is InventoryIntent.ShowEditDialog -> setState {
                val p = intent.product
                copy(
                    showAddDialog = true, editingProduct = p,
                    formName = p.name, formBrand = p.brand, formModel = p.model,
                    formImei = p.imei ?: "", formPtaStatus = p.ptaStatus,
                    formBarcode = p.barcode ?: "",
                    formCategory = p.category, formCondition = p.condition,
                    formCostPrice = p.costPrice.toString(), formSalePrice = p.sellingPrice.toString(),
                    formStock = p.stock.toString(), formLowStockAlert = p.lowStockThreshold.toString(),
                    formDescription = p.description ?: "", formError = null
                )
            }

            InventoryIntent.DismissDialog -> setState { copy(showAddDialog = false, editingProduct = null, formError = null) }

            InventoryIntent.SaveProduct -> saveProduct()

            is InventoryIntent.ConfirmDelete -> setState { copy(showDeleteConfirm = intent.productId) }

            InventoryIntent.DeleteProduct -> {
                val id = state.value.showDeleteConfirm ?: return
                setState { copy(showDeleteConfirm = null) }
                repo.deactivateProduct(id)
                loadProducts()
            }

            is InventoryIntent.ShowStockDialog -> setState { copy(showStockDialog = intent.productId) }

            is InventoryIntent.UpdateStock -> {
                repo.updateStock(intent.productId, intent.newStock)
                setState { copy(showStockDialog = null) }
                loadProducts()
            }

            InventoryIntent.DismissError -> setState { copy(error = null, successMessage = null) }

            // Form field updates
            is InventoryIntent.FormName        -> setState { copy(formName = intent.v) }
            is InventoryIntent.FormBrand       -> setState { copy(formBrand = intent.v) }
            is InventoryIntent.FormModel       -> setState { copy(formModel = intent.v) }
            is InventoryIntent.FormImei        -> setState { copy(formImei = intent.v) }
            is InventoryIntent.FormPtaStatus   -> setState { copy(formPtaStatus = intent.v) }
            is InventoryIntent.FormBarcode     -> setState { copy(formBarcode = intent.v) }
            is InventoryIntent.FormCategory    -> setState { copy(formCategory = intent.v) }
            is InventoryIntent.FormCondition   -> setState { copy(formCondition = intent.v) }
            is InventoryIntent.FormCostPrice   -> setState { copy(formCostPrice = intent.v) }
            is InventoryIntent.FormSalePrice   -> setState { copy(formSalePrice = intent.v) }
            is InventoryIntent.FormStock       -> setState { copy(formStock = intent.v) }
            is InventoryIntent.FormLowStockAlert -> setState { copy(formLowStockAlert = intent.v) }
            is InventoryIntent.FormDescription -> setState { copy(formDescription = intent.v) }
        }
    }

    private suspend fun loadProducts() {
        setState { copy(isLoading = true) }
        val showCost = repo.getShowCostPrice()
        val currency = repo.getCurrencySymbol()
        when (val r = repo.getAllProducts()) {
            is AppResult.Success -> {
                setState {
                    copy(
                        isLoading = false, products = r.data,
                        filteredProducts = applyFilters(r.data, searchQuery, selectedCategory),
                        showCostPrice = showCost, currencySymbol = currency
                    )
                }
            }
            is AppResult.Error -> setState { copy(isLoading = false, error = r.message) }
            else -> setState { copy(isLoading = false) }
        }
    }

    private fun applyFilter() {
        setState { copy(filteredProducts = applyFilters(products, searchQuery, selectedCategory)) }
    }

    private fun applyFilters(all: List<Product>, query: String, cat: ProductCategory?): List<Product> {
        return all
            .filter { if (query.isBlank()) true else it.name.contains(query, ignoreCase = true) || it.barcode?.contains(query) == true }
            .filter { if (cat == null) true else it.category == cat }
    }

    private suspend fun saveProduct() {
        val s = state.value
        if (s.formName.isBlank()) {
            setState { copy(formError = "Product name is required") }
            return
        }
        val salePrice = s.formSalePrice.toDoubleOrNull()
        if (salePrice == null || salePrice <= 0) {
            setState { copy(formError = "Enter a valid sale price") }
            return
        }
        val costPrice = s.formCostPrice.toDoubleOrNull() ?: 0.0
        val stock     = s.formStock.toIntOrNull() ?: 0
        val lowAlert  = s.formLowStockAlert.toIntOrNull() ?: 5

        val product = Product(
            id            = s.editingProduct?.id ?: IdGenerator.generate(),
            name          = s.formName.trim(),
            brand         = s.formBrand.trim(),
            model         = s.formModel.trim(),
            imei          = s.formImei.takeIf { it.isNotBlank() },
            barcode       = s.formBarcode.takeIf { it.isNotBlank() },
            category      = s.formCategory,
            condition     = s.formCondition,
            ptaStatus     = s.formPtaStatus,
            costPrice     = costPrice,
            sellingPrice  = salePrice,
            stock         = stock,
            lowStockThreshold = lowAlert,
            description   = s.formDescription.takeIf { it.isNotBlank() },
            isActive      = true,
            createdAt     = s.editingProduct?.createdAt ?: Clock.System.now().toEpochMilliseconds(),
            updatedAt     = Clock.System.now().toEpochMilliseconds()
        )

        val result = if (s.editingProduct != null) repo.updateProduct(product) else repo.insertProduct(product)
        when (result) {
            is AppResult.Success -> {
                setState { copy(showAddDialog = false, editingProduct = null) }
                setEffect(InventoryEffect.ShowToast(if (s.editingProduct != null) "Product updated" else "Product added"))
                loadProducts()
            }
            is AppResult.Error -> setState { copy(formError = result.message) }
            else -> {}
        }
    }
}
