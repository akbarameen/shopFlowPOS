package com.matechmatrix.shopflowpos.feature.inventory.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.matechmatrix.shopflowpos.core.model.Product
import com.matechmatrix.shopflowpos.core.model.enums.ProductCategory
import com.matechmatrix.shopflowpos.db.ProductQueriesQueries

private const val STARTING_KEY = 0L

class ProductPagingSource(
    private val queries  : ProductQueriesQueries,
    private val query    : String,
    private val category : ProductCategory?,
    private val mapper   : (com.matechmatrix.shopflowpos.db.Product) -> Product
) : PagingSource<Long, Product>() {

    override fun getRefreshKey(state: PagingState<Long, Product>): Long? {
        return state.anchorPosition?.let { anchor ->
            val page = state.closestPageToPosition(anchor)
            page?.prevKey?.plus(state.config.pageSize) ?: page?.nextKey?.minus(state.config.pageSize)
        }
    }

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, Product> {
        return try {
            val offset = params.key ?: STARTING_KEY
            val limit  = params.loadSize.toLong()

            val rows = when {
                query.isNotBlank() -> queries.searchProducts(
                    query  = query,
                    limit  = limit,
                    offset = offset
                ).executeAsList()

                category != null -> queries.getByCategory(
                    category = category.name,
                    limit    = limit,
                    offset   = offset
                ).executeAsList()

                else -> queries.getAllActive(
                    limit  = limit,
                    offset = offset
                ).executeAsList()
            }

            val products = rows.map(mapper)

            LoadResult.Page(
                data     = products,
                prevKey  = if (offset == STARTING_KEY) null else offset - limit,
                nextKey  = if (products.size < limit) null else offset + limit
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}