package com.kelsos.mbrc.presenters;

import com.google.inject.Inject;
import com.kelsos.mbrc.annotations.MetaDataType;
import com.kelsos.mbrc.annotations.Queue;
import com.kelsos.mbrc.domain.Artist;
import com.kelsos.mbrc.interactors.LibraryArtistInteractor;
import com.kelsos.mbrc.interactors.QueueInteractor;
import com.kelsos.mbrc.ui.views.BrowseArtistView;
import roboguice.util.Ln;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class BrowseArtistPresenter {
  private BrowseArtistView view;
  @Inject private LibraryArtistInteractor interactor;
  @Inject private QueueInteractor queueInteractor;

  public void bind(BrowseArtistView view) {
    this.view = view;
  }

  public void load() {
    interactor.execute().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(artists -> {
      view.update(artists);
    }, Ln::v);
  }

  public void queue(Artist artist, @Queue.Action String action) {
    queueInteractor.execute(MetaDataType.ARTIST, action, (int) artist.getId())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(success -> {
          if (success) {
            view.showEnqueueSuccess();
          } else {
            view.showEnqueueFailure();
          }
        }, throwable -> {
          view.showEnqueueFailure();
        });
  }
}
