package com.simplecity.amp_library.ui.detail;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.annimon.stream.Stream;
import com.simplecity.amp_library.R;
import com.simplecity.amp_library.model.Album;
import com.simplecity.amp_library.model.Playlist;
import com.simplecity.amp_library.model.Song;
import com.simplecity.amp_library.ui.modelviews.SongView;
import com.simplecity.amp_library.utils.ComparisonUtils;
import com.simplecity.amp_library.utils.MenuUtils;
import com.simplecity.amp_library.utils.Operators;
import com.simplecity.amp_library.utils.PlaceholderProvider;
import com.simplecity.amp_library.utils.SortManager;
import com.simplecityapps.recycler_adapter.model.ViewModel;

import java.util.Collections;
import java.util.List;

import io.reactivex.Single;

public class PlaylistDetailFragment extends BaseDetailFragment {

    public static String ARG_PLAYLIST = "playlist";

    private Playlist playlist;

    public static PlaylistDetailFragment newInstance(Playlist playlist) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_PLAYLIST, playlist);
        PlaylistDetailFragment fragment = new PlaylistDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        playlist = (Playlist) getArguments().getSerializable(ARG_PLAYLIST);
    }

    @Override
    boolean canPlaySlideshow() {
        return true;
    }

    @Override
    boolean showSongOverflowRemoveButton() {
        return playlist.canEdit;
    }

    @Override
    void songRemoved(int position, Song song) {
        adapter.removeItem(position);
        playlist.removeSong(song, null);
    }

    @Override
    void setSongSortOrder(int sortOrder) {
        SortManager.getInstance().setPlaylistDetailSongsSortOrder(sortOrder);
    }

    @Override
    int getSongSortOrder() {
        return SortManager.getInstance().getPlaylistDetailSongsSortOrder();
    }

    @Override
    void setSongsAscending(boolean ascending) {
        SortManager.getInstance().setPlaylistDetailSongsAscending(ascending);
    }

    @Override
    boolean getSongsAscending() {
        return SortManager.getInstance().getPlaylistDetailSongsAscending();
    }

    @Override
    void setAlbumSortOrder(int sortOrder) {
        SortManager.getInstance().setPlaylistDetailAlbumsSortOrder(sortOrder);
    }

    @Override
    int getAlbumSort() {
        return SortManager.getInstance().getPlaylistDetailAlbumsSortOrder();
    }

    @Override
    void setAlbumsAscending(boolean ascending) {
        SortManager.getInstance().setPlaylistDetailAlbumsAscending(ascending);
    }

    @Override
    boolean getAlbumsAscending() {
        return SortManager.getInstance().getPlaylistDetailAlbumsAscending();
    }

    @NonNull
    @Override
    public Single<List<Song>> getSongs() {
        return playlist.getSongsObservable().first(Collections.emptyList()).map(songs -> {
            sortSongs(songs);

            int songSortOrder = getSongSortOrder();
            if (songSortOrder == SortManager.SongSort.DETAIL_DEFAULT) {
                if (playlist.type == Playlist.Type.MOST_PLAYED) {
                    Collections.sort(songs, (a, b) -> ComparisonUtils.compareInt(b.playCount, a.playCount));
                }
                if (playlist.canEdit) {
                    Collections.sort(songs, (a, b) -> ComparisonUtils.compareLong(a.playlistSongPlayOrder, b.playlistSongPlayOrder));
                }
            }
            return songs;
        });
    }

    @NonNull
    @Override
    public List<ViewModel> getSongViewModels(List<Song> songs) {
        List<ViewModel> viewModels = super.getSongViewModels(songs);
        return Stream.of(viewModels)
                .filter(viewModel -> viewModel instanceof SongView)
                .map(viewModel -> {
                    ((SongView) viewModel).showPlayCount(true);
                    return viewModel;
                }).toList();
    }

    @NonNull
    @Override
    public Single<List<Album>> getAlbums() {
        return getSongs().map(Operators::songsToAlbums).map(albums -> {
            sortAlbums(albums);
            return albums;
        });
    }

    @Override
    protected void setupToolbarMenu(Toolbar toolbar) {
        super.setupToolbarMenu(toolbar);

        MenuUtils.setupPlaylistMenu(toolbar, playlist);

        toolbar.getMenu().findItem(R.id.playPlaylist).setVisible(false);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (MenuUtils.handleMenuItemClicks(getContext(), item, playlist)) {
            return true;
        }
        return super.onMenuItemClick(item);
    }

    @NonNull
    @Override
    protected String getToolbarTitle() {
        return playlist.name;
    }

    @NonNull
    @Override
    Drawable getPlaceHolderDrawable() {
        return PlaceholderProvider.getInstance().getPlaceHolderDrawable(playlist.name, true);
    }

    @Override
    protected String screenName() {
        return "PlaylistDetailFragment";
    }
}