package com.kelsos.mbrc.interactors;

import com.google.inject.Inject;
import com.kelsos.mbrc.dto.track.Position;
import com.kelsos.mbrc.services.api.TrackService;

import rx.Single;

public class TrackPositionInteractorImpl implements TrackPositionInteractor {
  @Inject private TrackService api;
  @Override
  public Single<Position> execute() {
    return api.getCurrentPosition();
  }
}
