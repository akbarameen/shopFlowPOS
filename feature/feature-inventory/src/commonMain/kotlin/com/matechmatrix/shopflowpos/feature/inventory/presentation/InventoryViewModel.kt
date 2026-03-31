package com.matechmatrix.shopflowpos.feature.inventory.presentation

import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.matechmatrix.shopflowpos.core.common.base.MviViewModel
import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.common.util.IdGenerator
import com.matechmatrix.shopflowpos.core.model.Product
import com.matechmatrix.shopflowpos.core.model.enums.ProductCategory
import com.matechmatrix.shopflowpos.core.model.enums.ProductCondition
import com.matechmatrix.shopflowpos.feature.inventory.domain.usecase.DeleteProductUseCase
import com.matechmatrix.shopflowpos.feature.inventory.domain.usecase.GetInventorySettingsUseCase
import com.matechmatrix.shopflowpos.feature.inventory.domain.usecase.GetLowStockProductsUseCase
import com.matechmatrix.shopflowpos.feature.inventory.domain.usecase.GetPagedProductsUseCase
import com.matechmatrix.shopflowpos.feature.inventory.domain.usecase.SaveProductUseCase
import com.matechmatrix.shopflowpos.feature.inventory.domain.usecase.UpdateStockUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.datetime.Clock

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class InventoryViewModel(
    private val getPagedProducts    : GetPagedProductsUseCase,
    private val getLowStock         : GetLowStockProductsUseCase,
    private val getSettings         : GetInventorySettingsUseCase,
    private val saveProduct         : SaveProductUseCase,
    private val updateStockUseCase  : UpdateStockUseCase,
    private val deleteProductUseCase: DeleteProductUseCase,
) : MviViewModel<InventoryState, InventoryIntent, InventoryEffect>(InventoryState()) {

    private val searchFlow     = MutableStateFlow("")
    private val categoryFlow   = MutableStateFlow<ProductCategory?>(null)
    private val refreshTrigger = MutableStateFlow(0) // Used to re-trigger Paging flow on data change

    init {
        // Wire paged flow — reacts to search/category/trigger changes
        combine(
            searchFlow.debounce(300).distinctUntilChanged(),
            categoryFlow,
            refreshTrigger
        ) { q, cat, _ -> q to cat }
            .flatMapLatest { (q, cat) -> getPagedProducts(q, cat).cachedIn(viewModelScope) }
            .onEach { paged -> setState { copy(pagedProducts = kotlinx.coroutines.flow.flowOf(paged)) } }
            .launchIn(viewModelScope)

        onIntent(InventoryIntent.Load)
    }

    override suspend fun handleIntent(intent: InventoryIntent) {
        when (intent) {

            InventoryIntent.Load -> loadSettings()

            is InventoryIntent.Search -> {
                searchFlow.value = intent.query
                setState { copy(searchQuery = intent.query) }
            }

            is InventoryIntent.FilterByCategory -> {
                categoryFlow.value = intent.category
                setState { copy(selectedCategory = intent.category) }
            }

            // ── Sheet ─────────────────────────────────────────────────────────
            InventoryIntent.ShowAddSheet -> setState {
                copy(
                    showProductSheet  = true,
                    editingProduct    = null,
                    formName          = "", formBrand       = "", formModel   = "",
                    formImei          = "", formPtaStatus   = "NA", formBarcode = "",
                    formCategory      = ProductCategory.PHONE,
                    formCondition     = ProductCondition.NEW,
                    formCostPrice     = "", formSalePrice   = "",
                    formStock         = "1", formLowStockAlert = "3",
                    formDescription   = "",
                    formColor         = "", formStorageGb = "", formRamGb = "",
                    formRomGb         = "", formBatteryMah = "", formScreenSize = "",
                    formProcessor     = "",
                    formError         = null,
                )
            }

            is InventoryIntent.ShowEditSheet -> {
                val p = intent.product
                setState {
                    copy(
                        showProductSheet  = true,
                        editingProduct    = p,
                        formName          = p.name,
                        formBrand         = p.brand,
                        formModel         = p.model,
                        formImei          = p.imei ?: "",
                        formPtaStatus     = p.ptaStatus,
                        formBarcode       = p.barcode ?: "",
                        formCategory      = p.category,
                        formCondition     = p.condition,
                        formCostPrice     = p.costPrice.toString(),
                        formSalePrice     = p.sellingPrice.toString(),
                        formStock         = p.stock.toString(),
                        formLowStockAlert = p.lowStockThreshold.toString(),
                        formDescription   = p.description ?: "",
                        formColor         = p.color ?: "",
                        formStorageGb     = p.storageGb?.toString() ?: "",
                        formRamGb         = p.ramGb?.toString() ?: "",
                        formRomGb         = p.romGb?.toString() ?: "",
                        formBatteryMah    = p.batteryMah?.toString() ?: "",
                        formScreenSize    = p.screenSizeInch?.toString() ?: "",
                        formProcessor     = p.processor ?: "",
                        formError         = null,
                    )
                }
            }

            InventoryIntent.DismissSheet -> setState {
                copy(showProductSheet = false, editingProduct = null, formError = null)
            }

            InventoryIntent.SaveProduct -> doSaveProduct()

            // ── Stock ─────────────────────────────────────────────────────────
            is InventoryIntent.ShowStockDialog ->
                setState { copy(showStockDialog = intent.productId.ifBlank { null }) }

            is InventoryIntent.UpdateStock -> {
                when (val r = updateStockUseCase(intent.productId, intent.newStock)) {
                    is AppResult.Success -> {
                        setState { copy(showStockDialog = null) }
                        setEffect(InventoryEffect.ShowToast("Stock updated"))
                        refreshTrigger.value++
                        refreshLowStockCount()
                    }
                    is AppResult.Error -> setEffect(InventoryEffect.ShowToast(r.message))
                    else -> {}
                }
            }

            // ── Delete ────────────────────────────────────────────────────────
            is InventoryIntent.ConfirmDelete ->
                setState { copy(showDeleteConfirm = intent.productId.ifBlank { null }) }

            InventoryIntent.DeleteProduct -> {
                val id = state.value.showDeleteConfirm ?: return
                setState { copy(showDeleteConfirm = null) }
                when (val r = deleteProductUseCase(id)) {
                    is AppResult.Success -> {
                        setEffect(InventoryEffect.ShowToast("Product removed"))
                        refreshTrigger.value++
                        refreshLowStockCount()
                    }
                    is AppResult.Error   -> setEffect(InventoryEffect.ShowToast(r.message))
                    else -> {}
                }
            }

            // ── Form fields ───────────────────────────────────────────────────
            is InventoryIntent.FormName         -> setState { copy(formName = intent.v) }
            is InventoryIntent.FormBrand        -> setState { copy(formBrand = intent.v) }
            is InventoryIntent.FormModel        -> setState { copy(formModel = intent.v) }
            is InventoryIntent.FormImei         -> setState { copy(formImei = intent.v) }
            is InventoryIntent.FormPtaStatus    -> setState { copy(formPtaStatus = intent.v) }
            is InventoryIntent.FormBarcode      -> setState { copy(formBarcode = intent.v) }
            is InventoryIntent.FormCategory     -> setState { copy(formCategory = intent.v, formImei = "", formError = null) }
            is InventoryIntent.FormCondition    -> setState { copy(formCondition = intent.v) }
            is InventoryIntent.FormCostPrice    -> setState { copy(formCostPrice = intent.v) }
            is InventoryIntent.FormSalePrice    -> setState { copy(formSalePrice = intent.v) }
            is InventoryIntent.FormStock        -> setState { copy(formStock = intent.v) }
            is InventoryIntent.FormLowStockAlert-> setState { copy(formLowStockAlert = intent.v) }
            is InventoryIntent.FormDescription  -> setState { copy(formDescription = intent.v) }
            is InventoryIntent.FormColor        -> setState { copy(formColor = intent.v) }
            is InventoryIntent.FormStorageGb    -> setState { copy(formStorageGb = intent.v) }
            is InventoryIntent.FormRamGb        -> setState { copy(formRamGb = intent.v) }
            is InventoryIntent.FormRomGb        -> setState { copy(formRomGb = intent.v) }
            is InventoryIntent.FormBatteryMah   -> setState { copy(formBatteryMah = intent.v) }
            is InventoryIntent.FormScreenSize   -> setState { copy(formScreenSize = intent.v) }
            is InventoryIntent.FormProcessor    -> setState { copy(formProcessor = intent.v) }
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private suspend fun loadSettings() {
        val settings = getSettings()
        setState { copy(showCostPrice = settings.showCostPrice, currencySymbol = settings.currencySymbol) }
        refreshLowStockCount()
    }

    private suspend fun refreshLowStockCount() {
        val r = getLowStock()
        if (r is AppResult.Success) setState { copy(lowStockCount = r.data.size) }
    }

    private suspend fun doSaveProduct() {
        val s = state.value
        setState { copy(isSaving = true, formError = null) }

        val isNew   = s.editingProduct == null
        val product = Product(
            id                = s.editingProduct?.id ?: IdGenerator.generate(),
            name              = s.formName.trim(),
            brand             = s.formBrand.trim(),
            model             = s.formModel.trim(),
            imei              = s.formImei.takeIf { it.isNotBlank() },
            barcode           = s.formBarcode.takeIf { it.isNotBlank() },
            category          = s.formCategory,
            condition         = s.formCondition,
            ptaStatus         = s.formPtaStatus,
            costPrice         = s.formCostPrice.toDoubleOrNull() ?: 0.0,
            sellingPrice      = s.formSalePrice.toDoubleOrNull() ?: 0.0,
            // IMEI-tracked items always have stock = 1 (one physical unit)
            stock             = if (s.formCategory.hasImei) 1
            else s.formStock.toIntOrNull() ?: 0,
            lowStockThreshold = if (s.formCategory.hasImei) 0
            else s.formLowStockAlert.toIntOrNull() ?: 3,
            description       = s.formDescription.takeIf { it.isNotBlank() },
            isActive          = true,
            createdAt         = s.editingProduct?.createdAt ?: Clock.System.now().toEpochMilliseconds(),
            updatedAt         = Clock.System.now().toEpochMilliseconds(),
            color             = s.formColor.takeIf { it.isNotBlank() },
            storageGb         = s.formStorageGb.toIntOrNull(),
            ramGb             = s.formRamGb.toIntOrNull(),
            romGb             = s.formRomGb.toIntOrNull(),
            batteryMah        = s.formBatteryMah.toIntOrNull(),
            screenSizeInch    = s.formScreenSize.toFloatOrNull(),
            processor         = s.formProcessor.takeIf { it.isNotBlank() },
        )

        when (val r = saveProduct(product, isNew)) {
            is AppResult.Success -> {
                setState { copy(isSaving = false, showProductSheet = false, editingProduct = null) }
                setEffect(InventoryEffect.ShowToast(if (isNew) "Product added" else "Product updated"))
                refreshTrigger.value++ // REFRESH DATA
                refreshLowStockCount()
            }
            is AppResult.Error -> setState { copy(isSaving = false, formError = r.message) }
            else               -> setState { copy(isSaving = false) }
        }
    }
}