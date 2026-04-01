package com.matechmatrix.shopflowpos.feature.ledger.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.matechmatrix.shopflowpos.core.model.LedgerEntry
import com.matechmatrix.shopflowpos.db.LedgerQueriesQueries

private const val STARTING_KEY = 0L

class LedgerPagingSource(
    private val queries: LedgerQueriesQueries,
    private val startMs: Long,
    private val endMs: Long,
    private val mapper: (com.matechmatrix.shopflowpos.db.Ledger_entry) -> LedgerEntry
) : PagingSource<Long, LedgerEntry>() {

    override fun getRefreshKey(state: PagingState<Long, LedgerEntry>): Long? =
        state.anchorPosition?.let { anchor ->
            val page = state.closestPageToPosition(anchor)
            page?.prevKey?.plus(state.config.pageSize)
                ?: page?.nextKey?.minus(state.config.pageSize)
        }

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, LedgerEntry> = try {
        val offset = params.key ?: STARTING_KEY
        val limit = params.loadSize.toLong()

        val rows = queries.getLedgerByDateRangePaged(
            from = startMs,
            to = endMs,
            limit = limit,
            offset = offset
        ).executeAsList()

        val entries = rows.map(mapper)
        LoadResult.Page(
            data = entries,
            prevKey = if (offset == STARTING_KEY) null else offset - limit,
            nextKey = if (entries.size < limit) null else offset + limit
        )
    } catch (e: Exception) {
        LoadResult.Error(e)
    }
}
