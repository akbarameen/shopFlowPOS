package com.matechmatrix.shopflowpos.feature.purchase.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.matechmatrix.shopflowpos.core.model.Product
import com.matechmatrix.shopflowpos.db.ProductQueriesQueries

class PurchaseProductPagingSource(
    private val queries: ProductQueriesQueries,
    private val query  : String,
    private val mapper : (com.matechmatrix.shopflowpos.db.Product) -> Product
) : PagingSource<Long, Product>() {
    override fun getRefreshKey(state: PagingState<Long, Product>): Long? =
        state.anchorPosition?.let { state.closestPageToPosition(it)?.prevKey?.plus(state.config.pageSize) }

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, Product> = try {
        val offset = params.key ?: 0L
        val limit  = params.loadSize.toLong()
        val rows   = if (query.isBlank())
            queries.getAllActive(limit, offset).executeAsList()
        else
            queries.searchProducts(query, limit, offset).executeAsList()
        val products = rows.map(mapper)
        LoadResult.Page(products, if (offset == 0L) null else offset - limit, if (products.size < limit) null else offset + limit)
    } catch (e: Exception) { LoadResult.Error(e) }
}