package com.kelsos.mbrc.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.google.inject.Inject;
import com.kelsos.mbrc.R;
import com.kelsos.mbrc.adapters.ArtistEntryAdapter;
import com.kelsos.mbrc.constants.Const;
import com.kelsos.mbrc.constants.Protocol;
import com.kelsos.mbrc.constants.ProtocolEventType;
import com.kelsos.mbrc.data.Queue;
import com.kelsos.mbrc.data.UserAction;
import com.kelsos.mbrc.data.library.Artist;
import com.kelsos.mbrc.events.MessageEvent;
import com.kelsos.mbrc.events.general.SearchDefaultAction;
import com.kelsos.mbrc.ui.widgets.EmptyRecyclerView;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import roboguice.RoboGuice;

public class BrowseArtistFragment extends Fragment implements ArtistEntryAdapter.MenuItemSelectedListener {
  @Inject
  Bus bus;
  @BindView(R.id.search_recycler_view)
  EmptyRecyclerView recycler;
  @BindView(R.id.empty_view)
  LinearLayout emptyView;
  @Inject
  private ArtistEntryAdapter adapter;

  private String mDefault;

  @Subscribe
  public void handleSearchDefaultAction(SearchDefaultAction action) {
    mDefault = action.getAction();
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    RoboGuice.getInjector(getContext()).injectMembers(this);
  }

  @Override
  public void onStart() {
    super.onStart();
    adapter.init();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.ui_fragment_library_search, container, false);
    ButterKnife.bind(this, view);
    return view;
  }

  @Override
  public void onResume() {
    super.onResume();
    bus.register(this);
  }

  @Override
  public void onPause() {
    super.onPause();
    bus.unregister(this);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    recycler.setHasFixedSize(true);
    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
    recycler.setLayoutManager(layoutManager);
    adapter.setMenuItemSelectedListener(this);
    recycler.setAdapter(adapter);
    recycler.setEmptyView(emptyView);
  }

  @Override
  public void onMenuItemSelected(MenuItem menuItem, Artist entry) {
    final String qContext = Protocol.LibraryQueueArtist;
    final String gSub = Protocol.LibraryArtistAlbums;
    String query = entry.getArtist();

    UserAction ua = null;
    switch (menuItem.getItemId()) {
      case R.id.popup_artist_queue_next:
        ua = new UserAction(qContext, new Queue(Queue.NEXT, query));
        break;
      case R.id.popup_artist_queue_last:
        ua = new UserAction(qContext, new Queue(Queue.LAST, query));
        break;
      case R.id.popup_artist_play:
        ua = new UserAction(qContext, new Queue(Queue.NOW, query));
        break;
      case R.id.popup_artist_album:
        ua = new UserAction(gSub, query);
        break;
      default:
        break;
    }

    if (ua != null) {
      bus.post(new MessageEvent(ProtocolEventType.UserAction, ua));
    }
  }

  @Override
  public void onItemClicked(Artist artist) {
    if (!mDefault.equals(Const.SUB)) {
      bus.post(new MessageEvent(ProtocolEventType.UserAction,
          new UserAction(Protocol.LibraryQueueArtist, new Queue(mDefault, artist.getArtist()))));
    } else {
      bus.post(new MessageEvent(ProtocolEventType.UserAction,
          new UserAction(Protocol.LibraryArtistAlbums, artist.getArtist())));
    }
  }
}