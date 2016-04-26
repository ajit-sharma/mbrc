package com.kelsos.mbrc.repository.library

import com.kelsos.mbrc.RemoteDatabase
import com.kelsos.mbrc.dao.ArtistDao
import com.kelsos.mbrc.dao.ArtistDao_Table
import com.kelsos.mbrc.dao.views.GenreArtistView
import com.kelsos.mbrc.dao.views.GenreArtistView_ViewTable
import com.raizlabs.android.dbflow.runtime.TransactionManager
import com.raizlabs.android.dbflow.sql.language.SQLite
import com.raizlabs.android.dbflow.structure.BaseModel
import rx.Observable
import rx.functions.Action1

class ArtistRepositoryImpl : ArtistRepository {
    override fun getArtistsByGenreId(id: Long): Observable<List<GenreArtistView>> {
        return Observable.create<List<GenreArtistView>> { subscriber ->
            val genreArtists = SQLite.select()
                    .from(GenreArtistView::class.java)
                    .where(GenreArtistView_ViewTable.genre_id.eq(id))
                    .queryList()
            subscriber.onNext(genreArtists)
            subscriber.onCompleted()
        }
    }

    override fun getPageObservable(offset: Int, limit: Int): Observable<List<ArtistDao>> {
        return Observable.defer {
            Observable.just(getPage(offset, limit))
        }
    }

    override fun getAllObservable(): Observable<List<ArtistDao>> = Observable.defer {
        Observable.just(getAll())
    }

    override fun getPage(offset: Int, limit: Int): List<ArtistDao> {
        return SQLite.select()
                .from(ArtistDao::class.java)
                .limit(limit)
                .offset(offset)
                .orderBy(ArtistDao_Table.name, true)
                .queryList()
    }

    override fun getAll(): List<ArtistDao> = SQLite.select()
            .from(ArtistDao::class.java)
            .orderBy(ArtistDao_Table.name, true)
            .queryList()

    override fun getById(id: Long): ArtistDao {
        return SQLite.select()
                .from(ArtistDao::class.java)
                .where(ArtistDao_Table.id.`is`(id))
                .querySingle()
    }

    override fun save(items: List<ArtistDao>) {
        TransactionManager.transact(RemoteDatabase.NAME) {
            Observable.from(items).forEach({ it.save() })
        }
    }

    override fun save(item: ArtistDao) {
        item.save()
    }

    override fun count(): Long {
        return SQLite.selectCountOf().from(ArtistDao::class.java).count()
    }
}
