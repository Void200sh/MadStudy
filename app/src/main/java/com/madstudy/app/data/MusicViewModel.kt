package com.madstudy.app.data

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class MusicTrack(val uri: String, val title: String)

@Serializable
private data class SavedMusic(
    val tracks: List<MusicTrack> = emptyList(),
    val current: String? = null,
    val shuffle: Boolean = false,
    val repeat: Boolean = false
)

data class MusicUi(
    val current: MusicTrack? = null,
    val playing: Boolean = false,
    val position: Long = 0L,
    val duration: Long = 0L,
    val shuffle: Boolean = false,
    val repeat: Boolean = false
)

private val Context.musicPrefs by preferencesDataStore(name = "madstudy_music")

/**
 * Pemutar musik milik Activity, bukan milik layar Music.
 * Player tidak di-release saat pindah tab, jadi lagu tetap berjalan
 * dan playlist tidak hilang.
 */
class MusicViewModel(app: Application) : AndroidViewModel(app) {
    private val json = Json { ignoreUnknownKeys = true }
    private val prefsKey = stringPreferencesKey("music_json")

    private val _tracks = MutableStateFlow<List<MusicTrack>>(emptyList())
    val tracks: StateFlow<List<MusicTrack>> = _tracks

    private val _ui = MutableStateFlow(MusicUi())
    val ui: StateFlow<MusicUi> = _ui

    private var ticker: Job? = null

    val player: ExoPlayer = ExoPlayer.Builder(app).build().apply {
        setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build(),
            true
        )
        setHandleAudioBecomingNoisy(true)
    }

    init {
        viewModelScope.launch {
            val saved = runCatching {
                val raw = app.musicPrefs.data.map { it[prefsKey] }.first()
                if (raw.isNullOrBlank()) SavedMusic() else json.decodeFromString<SavedMusic>(raw)
            }.getOrDefault(SavedMusic())

            _tracks.value = saved.tracks
            player.shuffleModeEnabled = saved.shuffle
            player.repeatMode = if (saved.repeat) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
            _ui.value = _ui.value.copy(shuffle = saved.shuffle, repeat = saved.repeat)

            saved.current?.let { uri -> saved.tracks.firstOrNull { it.uri == uri } }?.let { track ->
                _ui.value = _ui.value.copy(current = track)
                player.setMediaItem(MediaItem.fromUri(track.uri))
                player.prepare()
            }
        }

        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _ui.value = _ui.value.copy(playing = isPlaying)
                if (isPlaying) startTicker() else {
                    ticker?.cancel()
                    ticker = null
                    updateProgress()
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val uri = mediaItem?.localConfiguration?.uri?.toString() ?: return
                _tracks.value.firstOrNull { it.uri == uri }?.let { track ->
                    _ui.value = _ui.value.copy(current = track, position = 0L, duration = 0L)
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) = updateProgress()
        })
    }

    fun addTracks(uris: List<Uri>) {
        if (uris.isEmpty()) return
        val context = getApplication<Application>()
        val known = _tracks.value.map { it.uri }.toSet()
        val added = uris.mapNotNull { uri ->
            if (uri.toString() in known) return@mapNotNull null
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            MusicTrack(uri.toString(), displayName(context, uri))
        }
        if (added.isEmpty()) return
        _tracks.value = _tracks.value + added
        persist()
    }

    fun play(track: MusicTrack) {
        player.setMediaItem(MediaItem.fromUri(track.uri))
        player.prepare()
        player.play()
        _ui.value = _ui.value.copy(current = track, position = 0L, duration = 0L)
        persist()
    }

    fun toggle() {
        val current = _ui.value.current
        if (current == null) {
            _tracks.value.firstOrNull()?.let { play(it) }
            return
        }
        if (player.isPlaying) player.pause() else player.play()
    }

    fun step(delta: Int) {
        val list = _tracks.value
        if (list.isEmpty()) return
        val index = list.indexOfFirst { it.uri == _ui.value.current?.uri }
        val next = ((if (index < 0) 0 else index + delta) % list.size + list.size) % list.size
        play(list[next])
    }

    fun toggleShuffle() {
        val next = !_ui.value.shuffle
        player.shuffleModeEnabled = next
        _ui.value = _ui.value.copy(shuffle = next)
        persist()
    }

    fun toggleRepeat() {
        val next = !_ui.value.repeat
        player.repeatMode = if (next) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
        _ui.value = _ui.value.copy(repeat = next)
        persist()
    }

    private fun startTicker() {
        if (ticker?.isActive == true) return
        ticker = viewModelScope.launch {
            while (_ui.value.playing) {
                updateProgress()
                delay(400)
            }
        }
    }

    private fun updateProgress() {
        val duration = player.duration.takeIf { it > 0L && it != C.TIME_UNSET } ?: 0L
        _ui.value = _ui.value.copy(
            position = player.currentPosition.coerceAtLeast(0L),
            duration = duration
        )
    }

    private fun persist() {
        val snapshot = SavedMusic(
            tracks = _tracks.value,
            current = _ui.value.current?.uri,
            shuffle = _ui.value.shuffle,
            repeat = _ui.value.repeat
        )
        viewModelScope.launch {
            runCatching {
                getApplication<Application>().musicPrefs.edit {
                    it[prefsKey] = json.encodeToString(snapshot)
                }
            }
        }
    }

    override fun onCleared() {
        ticker?.cancel()
        player.release()
        super.onCleared()
    }
}

private fun displayName(context: Context, uri: Uri): String {
    return try {
        context.contentResolver.query(uri, null, null, null, null)?.use { c ->
            val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (idx >= 0 && c.moveToFirst()) c.getString(idx) else null
        } ?: uri.lastPathSegment?.substringAfterLast('/') ?: "Audio"
    } catch (e: Exception) {
        uri.lastPathSegment?.substringAfterLast('/') ?: "Audio"
    }
}
