package com.kelsos.mbrc.data.model;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kelsos.mbrc.enums.PlayState;
import com.kelsos.mbrc.events.Events;
import com.kelsos.mbrc.events.actions.ButtonPressedEvent;
import com.kelsos.mbrc.events.ui.ScrobbleChange;
import com.kelsos.mbrc.events.ui.ShuffleChange;
import com.kelsos.mbrc.net.Notification;
import com.kelsos.mbrc.rest.RemoteApi;
import com.kelsos.mbrc.rest.responses.SuccessResponse;
import com.kelsos.mbrc.util.Logger;
import roboguice.util.Ln;
import rx.Observable;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

@Singleton
public class PlayerState {
    public static final int MAX_VOLUME = 100;
    public static final int LOWEST_NOT_ZERO = 10;
    public static final int GREATEST_NOT_MAX = 90;
    public static final int STEP = 10;
    public static final int MOD = 10;
    public static final int BASE = 0;
    public static final int LIMIT = 5;
    public static final String ALL = "All";
    public static final String PLAYING = "playing";
    public static final String PAUSED = "paused";
    public static final String STOPPED = "stopped";
    private final RemoteApi api;
    private final BehaviorSubject<PlayState> playerStateBehaviorSubject;
    private final BehaviorSubject<Integer> volumeBehaviourSubject;
    boolean repeatActive;
    boolean shuffleActive;
    boolean isScrobblingActive;
    boolean isMuteActive;
    private int volume;
    private PlayState playState;


    @Inject
    public PlayerState(RemoteApi api) {
        this.api = api;
        playerStateBehaviorSubject = BehaviorSubject.create(PlayState.STOPPED);
        volumeBehaviourSubject = BehaviorSubject.create(BASE);

        Events.Messages.subscribeOn(Schedulers.io())
                .filter(msg -> msg.getType().equals(Notification.VOLUME_CHANGED))
                .flatMap(resp -> api.getVolume())
                .subscribe(resp -> setVolume(resp.getValue()), Logger::ProcessThrowable);

        api.getPlayerStatus()
                .subscribe(resp -> {
                    setRepeatState(resp.getRepeat());
                    setVolume(resp.getVolume());
                    setPlayState(resp.getState());
                }, Logger::ProcessThrowable);

        Events.Messages.subscribeOn(Schedulers.io())
                .filter(msg -> msg.getType().equals(Notification.PLAY_STATUS_CHANGED))
                .flatMap(resp -> api.getPlaystate())
                .subscribe(resp -> setPlayState(resp.getValue()), Logger::ProcessThrowable);

        SubscribeToButtonEvent(ButtonPressedEvent.Button.PREVIOUS, api.playPrevious());
        SubscribeToButtonEvent(ButtonPressedEvent.Button.NEXT, api.playNext());
        SubscribeToButtonEvent(ButtonPressedEvent.Button.STOP, api.playbackStop());
        SubscribeToButtonEvent(ButtonPressedEvent.Button.PLAYPAUSE, api.playPause());

        Events.ButtonPressedNotification.subscribeOn(Schedulers.io())
                .filter(event -> event.getType().equals(ButtonPressedEvent.Button.SHUFFLE))
                .flatMap(event -> api.toggleShuffleState())
                .subscribe(resp -> setShuffleState(resp.isEnabled()),
                        Logger::ProcessThrowable);

        Events.ButtonPressedNotification.subscribeOn(Schedulers.io())
                .filter(event -> event.getType().equals(ButtonPressedEvent.Button.REPEAT))
                .flatMap(event -> api.changeRepeatMode())
                .subscribe(resp -> setRepeatState(resp.getValue()),
                        Logger::ProcessThrowable);


    }


    private void SubscribeToButtonEvent(ButtonPressedEvent.Button button, Observable<SuccessResponse> apiRequest) {
        Events.ButtonPressedNotification.subscribeOn(Schedulers.io())
                .filter(event -> event.getType().equals(button))
                .flatMap(event -> apiRequest)
                .subscribe(r -> Ln.d(r.isSuccess()), Logger::ProcessThrowable);
    }

    public void setRepeatState(String repeatButtonActive) {
        repeatActive = (repeatButtonActive.equals(ALL));
        Ln.d("Repeat value %s", repeatButtonActive);
    }

    public void setShuffleState(boolean shuffleButtonActive) {
        shuffleActive = shuffleButtonActive;
        new ShuffleChange(isShuffleActive());
    }

    public void setScrobbleState(boolean scrobbleButtonActive) {
        isScrobblingActive = scrobbleButtonActive;
        new ScrobbleChange(isScrobblingActive);
    }

    public void setMuteState(boolean isMuteActive) {
        this.isMuteActive = isMuteActive;
    }

    public void setPlayState(String playState) {
        PlayState newState = PlayState.UNDEFINED;
        if (playState.equalsIgnoreCase(PLAYING)) {
            newState = PlayState.PLAYING;
        } else if (playState.equalsIgnoreCase(STOPPED)) {
            newState = PlayState.STOPPED;
        } else if (playState.equalsIgnoreCase(PAUSED)) {
            newState = PlayState.PAUSED;
        }
        this.playState = newState;
        onPlayStateChange(newState);
    }

    public boolean isRepeatActive() {
        return repeatActive;
    }

    public boolean isShuffleActive() {
        return shuffleActive;
    }

    private void reduceVolume() {
        if (volume >= LOWEST_NOT_ZERO) {
            int mod = volume % MOD;
            int newVolume;

            if (mod == BASE) {
                newVolume = volume - STEP;
            } else if (mod < LIMIT) {
                newVolume = volume - (STEP + mod);
            } else {
                newVolume = volume - mod;
            }

            api.updateVolume(newVolume);
        }
    }

    private void increaseVolume() {
        if (volume <= GREATEST_NOT_MAX) {
            int mod = volume % MOD;
            int newVolume;

            if (mod == BASE) {
                newVolume = volume + STEP;
            } else if (mod < LIMIT) {
                newVolume = volume + (STEP - mod);
            } else {
                newVolume = volume + ((2 * STEP) - mod);
            }

            api.updateVolume(newVolume);
        }
    }

    private void onPlayStateChange(PlayState playState) {
        playerStateBehaviorSubject.onNext(playState);
    }

    public Observable<PlayState> playState() {
        return playerStateBehaviorSubject.asObservable();
    }

    private void onVolumeChange(int volume) {
        volumeBehaviourSubject.onNext(volume);
    }

    public Observable<Integer> getVolume() {
        return volumeBehaviourSubject.asObservable();
    }

    public void setVolume(int volume) {
        if (volume != this.volume) {
            this.volume = volume;
            onVolumeChange(volume);
        }
    }
}